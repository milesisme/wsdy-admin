package com.wsdy.saasops.modules.member.service;

import com.google.common.collect.Lists;
import com.wsdy.saasops.api.modules.apisys.service.TGmApiService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.ColumnAuthConstants;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.common.utils.jpush.JPushNotificationDto;
import com.wsdy.saasops.common.utils.jpush.JPushUtil;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.fund.dto.CountEntity;
import com.wsdy.saasops.modules.member.dao.MbrMessageInfoMapper;
import com.wsdy.saasops.modules.member.dao.MbrMessageMapper;
import com.wsdy.saasops.modules.member.entity.MbrMessage;
import com.wsdy.saasops.modules.member.entity.MbrMessageInfo;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@Slf4j
@Service
@Transactional
public class MbrMessageService {

    @Autowired
    private MbrMessageMapper mbrMessageMapper;
    @Autowired
    private MbrMessageInfoMapper mbrMessageInfoMapper;
    @Autowired
    private QiNiuYunUtil qiNiuYunUtil;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private JPushUtil jPushUtil;
    @Autowired
    private TGmApiService gmApiService;


    public PageUtils messageList(MbrMessage mbrMessage, Integer pageNo, Integer pageSize, Long userId) {
        int count = mbrMapper.findUserMenuId(userId, ColumnAuthConstants.MEMBER__MENUNAME_ID);
        if (count >0){
            mbrMessage.setIsRealName(Constants.EVNumber.one);
        }
        PageHelper.startPage(pageNo, pageSize);
        List<MbrMessage> list = mbrMapper.findMbrMessageList(mbrMessage);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils messageInfo(MbrMessageInfo mbrMessageInfo) {
        List<MbrMessageInfo> messageInfos = mbrMessageInfoMapper.fineMbrMessageV2Info(mbrMessageInfo);
        messageInfos.stream().forEach(ms -> {
            if (StringUtils.isNotEmpty(ms.getImageUrl())) {
                ms.setImageUrl(gmApiService.queryGiniuyunUrl() + ms.getImageUrl());
            }
        });

        // ????????????????????????
        MbrMessageInfo upInfo = new MbrMessageInfo();
        upInfo.setMessageId(mbrMessageInfo.getMessageId());
        upInfo.setIsReadSys(Constants.EVNumber.one);
        mbrMessageInfoMapper.updateMessageList(upInfo);

        return BeanUtil.toPagedResult(messageInfos);
    }

    public R messageSend(MbrMessageInfo mbrMessageInfo, MultipartFile uploadMessageFile) {
        // ??????????????????
        if (nonNull(uploadMessageFile)) {
            try {
                byte[] fileBuff = IOUtils.toByteArray(uploadMessageFile.getInputStream());
                String fileName = qiNiuYunUtil.uploadFileKey(fileBuff);
                mbrMessageInfo.setImageUrl(fileName);
            } catch (Exception e) {
                log.error("messageSend-??????????????????", e);
                throw new RRException("??????????????????");
            }
        }
        mbrMessageInfo.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        mbrMessageInfo.setIsRead(Constants.EVNumber.zero);      // ??????
        mbrMessageInfo.setIsReadSys(Constants.EVNumber.one);    // ??????
        mbrMessageInfo.setIsDelete(Constants.EVNumber.zero);    // ?????????

        //??????????????????
        List<MbrMessage> messageList = mbrMapper.findMbrListByNameAll(mbrMessageInfo);
        if (Collections3.isEmpty(messageList)) {
            throw new RRException("???????????????");
        }
        for (MbrMessage mbrMessage : messageList) {
            String key = RedisConstants.ACCOUNT_MESSAGE_KEY + CommonUtil.getSiteCode() + mbrMessageInfo.getCreateUser();
            Boolean isExpired = redisService.setRedisExpiredTimeBo(key, mbrMessage.getAccountId(), 10, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(isExpired)) {
                mbrMessage.setTime(getCurrentDate(FORMAT_18_DATE_TIME));
                mbrMessage.setIsRevert(1 == mbrMessageInfo.getIsSign() ? 1 : mbrMessage.getIsRevert());
                if (Objects.isNull(mbrMessage.getId())) {
                    mbrMessageMapper.insert(mbrMessage);
                } else {
                    mbrMessageMapper.updateByPrimaryKey(mbrMessage);
                }

                //messageInfo??????
                mbrMessageInfo.setMessageId(mbrMessage.getId());
                mbrMessageInfoMapper.insert(mbrMessageInfo);
                mbrMessageInfo.setId(null);
            }
            redisService.del(key);
        }
        return R.ok();
    }

    public R messageSendBatch(MbrMessageInfo mbrMessageInfo, MultipartFile uploadMessageFile,String key ) {
        try {
            // ??????????????????
            if (nonNull(uploadMessageFile)) {
                try {
                    byte[] fileBuff = IOUtils.toByteArray(uploadMessageFile.getInputStream());
                    String fileName = qiNiuYunUtil.uploadFileKey(fileBuff);
                    mbrMessageInfo.setImageUrl(fileName);
                } catch (Exception e) {
                    log.info("messageSend==messageSendBatch==??????????????????==" + e);
                    throw new RRException("??????????????????");
                }
            }

            // ???????????????????????????
            String siteCode = CommonUtil.getSiteCode();
            CompletableFuture.runAsync(() -> {
                ThreadLocalCache.setSiteCodeAsny(siteCode);
                try{
                    // ???????????????????????????: ???????????????????????????app
                    List<MbrMessage> messageList;
                    if(Integer.valueOf(Constants.EVNumber.one).equals(mbrMessageInfo.getIsPush())){
                        messageList = mbrMapper.findPushMbrListByName(mbrMessageInfo);
//                        messageList = mbrMapper.findMbrListByName(mbrMessageInfo);

                    }else{
                        messageList = mbrMapper.findMbrListByName(mbrMessageInfo);
                    }

                    if (Collections3.isEmpty(messageList)) {
                        log.info("messageSend==messageSendBatch==???????????????" );
        //                throw new RRException("???????????????");
                        return ;
                    }
                    // ????????????
                    Long startTime = System.currentTimeMillis();
                    log.info("messageSend==messageSendBatch==startTime==" + getCurrentDate(FORMAT_18_DATE_TIME) + "===size==" + messageList.size());
                    messageSendAsync(messageList, mbrMessageInfo);
                    long costTime = System.currentTimeMillis() - startTime;
                    log.info("messageSend==messageSendBatch==endTime==" + getCurrentDate(FORMAT_18_DATE_TIME) + "===size==" + messageList.size() + "==costTime==" + costTime);

                    // ?????????????????????????????????
                    if(Integer.valueOf(Constants.EVNumber.one).equals(mbrMessageInfo.getIsPush())){
                        messagePushBatchSyn(mbrMessageInfo,siteCode,messageList);
                    }
                    // ????????????
                    messageList.clear();
                }finally {
                    // ??????key
                    redisService.del(key);
                }
            });
        } catch (RRException e) {
            // ??????key
            redisService.del(key);
            throw e;
        } catch (Exception e) {
            // ??????key
            redisService.del(key);
            throw e;
        }
        return R.ok();
    }

    public void messagePushBatchSyn(MbrMessageInfo mbrMessageInfo,String siteCode,List<MbrMessage> messageList ) {
//        CompletableFuture.runAsync(() -> {
//            ThreadLocalCache.setSiteCodeAsny(siteCode);
            Long startTime = System.currentTimeMillis();
            log.info("messageSend==JPush==messagePushBatchSyn==startTime==" + getCurrentDate(FORMAT_18_DATE_TIME));

            // ??????????????????: ??????
            if(Integer.valueOf(Constants.EVNumber.one).equals(mbrMessageInfo.getIsAllDevice())){
                JPushNotificationDto dto = new JPushNotificationDto();
                dto.setMsgTitle(mbrMessageInfo.getPushTitle());     // ??????
                dto.setMsgContent(mbrMessageInfo.getPushContent()); // ??????
                dto.setTimeToLive(mbrMessageInfo.getTimeToLive());  // ??????????????????????????????

                jPushUtil.pushToAll(dto);
            }else{  // ????????????????????? loginname
    //            String siteCode = CommonUtil.getSiteCode();
    //            mbrMessageInfo.setSiteCode(siteCode);
//                List<String> alias = mbrMapper.findMbrList(mbrMessageInfo);
                List<String> alias = messageList.stream().map(MbrMessage::getLoginName).collect(Collectors.toList());
                if (Collections3.isEmpty(alias)) {
                    return;
                }
                JPushNotificationDto dto = new JPushNotificationDto();
                dto.setMsgTitle(mbrMessageInfo.getPushTitle());     // ??????
                dto.setMsgContent(mbrMessageInfo.getPushContent()); // ??????
                dto.setAlias(alias);                                // ?????????
                dto.setTimeToLive(mbrMessageInfo.getTimeToLive());  // ??????????????????????????????
                // ??????????????????20???/min?????????600???/min, ?????????????????????1000??????????????????1000????????????????????????1000???????????????
    //            if(alias.size() < 1000){
    //                jPushUtil.pushToAliasList(dto);
    //            }else{
    //                jPushUtil.pushToAliasListByFile(dto);
    //            }
    //                jPushUtil.pushToAliasListByFile(dto);
                jPushUtil.pushToAliasList(dto);

                // ????????????
                alias.clear();
            }
            long costTime = System.currentTimeMillis() - startTime;
            log.info("messageSend==JPush==messagePushBatchSyn==endTime==" + getCurrentDate(FORMAT_18_DATE_TIME) + "==costTime==" + costTime);
//        });
    }

    public void messageSendAsync(List<MbrMessage> messageList, MbrMessageInfo mbrMessageInfo) {
        // mbrMessageInfo ??????
        mbrMessageInfo.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        mbrMessageInfo.setIsRead(Constants.EVNumber.zero);      // ??????
        mbrMessageInfo.setIsReadSys(Constants.EVNumber.one);    // ??????
        mbrMessageInfo.setIsDelete(Constants.EVNumber.zero);    // ?????????

        // ?????????????????????????????????
        String time = getCurrentDate(FORMAT_18_DATE_TIME);
        messageList.stream().forEach(
                ms -> {
                    ms.setTime(time);
                    ms.setIsRevert(1 == mbrMessageInfo.getIsSign() ? 1 : ms.getIsRevert());
                }
        );

        // ????????????mbrMessage
        List<MbrMessage> insertList = messageList.stream().filter(
                mbrMessage -> Objects.isNull(mbrMessage.getId())
        ).collect(Collectors.toList());

        log.info("messageSend==messageSendAsync==?????????mbrMessage size==" + insertList.size());

        // ????????????mbrMessage
        List<MbrMessage> updateList = messageList.stream().filter(
                mbrMessage -> Objects.nonNull(mbrMessage.getId())
        ).collect(Collectors.toList());

        log.info("messageSend==messageSendAsync==?????????mbrMessage size==" + updateList.size());

        // ????????????mbrMessageInfo
        List<MbrMessageInfo> updateInfoList = new ArrayList<>();
        updateList.stream().forEach(
                ms -> {
                    MbrMessageInfo tmp = new MbrMessageInfo();
                    tmp.setMessageId(ms.getId());       // ?????????????????????messageid
                    tmp.setTextContent(mbrMessageInfo.getTextContent());
                    tmp.setImageUrl(mbrMessageInfo.getImageUrl());
                    tmp.setCreateTime(mbrMessageInfo.getCreateTime());
                    tmp.setCreateUser(mbrMessageInfo.getCreateUser());
                    tmp.setIsSign(mbrMessageInfo.getIsSign());
                    tmp.setIsRead(mbrMessageInfo.getIsRead());
                    tmp.setIsReadSys(mbrMessageInfo.getIsReadSys());
                    tmp.setIsDelete(mbrMessageInfo.getIsDelete());
                    tmp.setExpirationTime(mbrMessageInfo.getExpirationTime());  // ????????????
                    updateInfoList.add(tmp);
                }
        );

        // ????????????????????????mbrMessage?????????mbrMessageInfo
        if(Collections3.isNotEmpty(insertList)){
            // ????????????mbrMessage
            List<List<MbrMessage>> groupByLength = Lists.partition(insertList,50);
            groupByLength.stream().forEach(
                    tmp -> {
                        mbrMessageInfoMapper.batchInsertMbrMessage(tmp);
                    }
            );

            // ?????????????????????mbrMessage
            MbrMessage qryTemp =  new MbrMessage();
            qryTemp.setTime(time);
            List<MbrMessage> tmpList = mbrMessageMapper.select(qryTemp);

            // ????????????mbrMessageInfo
            List<MbrMessageInfo> insertInfoList = new ArrayList<>();
            tmpList.stream().forEach(
                    ms -> {
                        MbrMessageInfo tmp = new MbrMessageInfo();
                        tmp.setMessageId(ms.getId());       // ???????????????messageid
                        tmp.setTextContent(mbrMessageInfo.getTextContent());
                        tmp.setImageUrl(mbrMessageInfo.getImageUrl());
                        tmp.setCreateTime(mbrMessageInfo.getCreateTime());
                        tmp.setCreateUser(mbrMessageInfo.getCreateUser());
                        tmp.setIsSign(mbrMessageInfo.getIsSign());
                        tmp.setIsRead(mbrMessageInfo.getIsRead());
                        tmp.setIsReadSys(mbrMessageInfo.getIsReadSys());
                        tmp.setIsDelete(mbrMessageInfo.getIsDelete());
                        tmp.setExpirationTime(mbrMessageInfo.getExpirationTime());  // ????????????
                        insertInfoList.add(tmp);
                    }
            );

            // ????????????MbrMessageInfo
            List<List<MbrMessageInfo>> insertGroupByLength = Lists.partition(insertInfoList,50);
            insertGroupByLength.stream().forEach(
                    tmp -> {
                        mbrMessageInfoMapper.batchInsertMbrMessageInfo(tmp);
                    }
            );

            // ????????????
            insertList.clear();
            groupByLength.clear();
            tmpList.clear();
            insertInfoList.clear();
            insertGroupByLength.clear();
        }


        // ????????????????????????mbrMessage?????????mbrMessageInfo
        if(Collections3.isNotEmpty(updateList)){
            // ????????????????????????????????????
            Map<Integer,List<MbrMessage>> updateListGroup = updateList.stream().collect(Collectors.groupingBy(MbrMessage::getIsRevert));
            // ??????mbrMessage
            for(Integer isRevert : updateListGroup.keySet()){
                if(Integer.valueOf(Constants.EVNumber.one).equals(isRevert)){   // ?????????
                    List<MbrMessage> updateListIsRevert = updateListGroup.get(isRevert);
                    // ????????????mbrMessage
                    // ??????messageid???
                    List<Integer> messageIds = updateListIsRevert.stream().map(
                            ls ->{
                                return ls.getId();
                            }
                    ).collect(Collectors.toList());

                    // ????????????mbrMessage
                    List<List<Integer>> groupByLength = Lists.partition(messageIds,50);
                    MbrMessage mbrMessage = updateListIsRevert.get(0);
                    mbrMessage.setIsRevert(isRevert);
                    groupByLength.stream().forEach(
                            tmp -> {
                                mbrMessage.setGroups(tmp);
                                mbrMessageInfoMapper.batchUpdateMbrMessage(mbrMessage);
                            }
                    );
                    // ????????????
//                    updateListIsRevert.clear();
                    messageIds.clear();
                    groupByLength.clear();
                }
                if(Integer.valueOf(Constants.EVNumber.zero).equals(isRevert)){   // ?????????
                    List<MbrMessage> updateListIsRevert = updateListGroup.get(isRevert);
                    // ????????????mbrMessage
                    // ??????messageid???
                    List<Integer> messageIds = updateListIsRevert.stream().map(
                            ls ->{
                                return ls.getId();
                            }
                    ).collect(Collectors.toList());

                    // ????????????mbrMessage
                    List<List<Integer>> groupByLength = Lists.partition(messageIds,50);
                    MbrMessage mbrMessage = updateListIsRevert.get(0);
                    mbrMessage.setIsRevert(isRevert);
                    groupByLength.stream().forEach(
                            tmp -> {
                                mbrMessage.setGroups(tmp);
                                mbrMessageInfoMapper.batchUpdateMbrMessage(mbrMessage);
                            }
                    );

                    // ????????????
//                    updateListIsRevert.clear();
                    messageIds.clear();
                    groupByLength.clear();
                }
            }

            // ????????????
            updateListGroup.clear();

            // ????????????MbrMessageInfo
            List<List<MbrMessageInfo>> insertGroupByLength = Lists.partition(updateInfoList,50);
            insertGroupByLength.stream().forEach(
                    tmp -> {
                        mbrMessageInfoMapper.batchInsertMbrMessageInfo(tmp);
                    }
            );

            // ????????????
            updateInfoList.clear();
            insertGroupByLength.clear();
        }
    }

    public void messageUpdate(MbrMessageInfo mbrMessageInfo) {
        MbrMessageInfo messageInfo = mbrMessageInfoMapper.selectByPrimaryKey(mbrMessageInfo.getId());
        if (nonNull(messageInfo)) {
            messageInfo.setTextContent(mbrMessageInfo.getTextContent());
            messageInfo.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
            messageInfo.setCreateUser(mbrMessageInfo.getCreateUser());
            mbrMessageInfoMapper.updateByPrimaryKeySelective(mbrMessageInfo);
            updateMbrMessage(mbrMessageInfo);
        }
    }

    private void updateMbrMessage(MbrMessageInfo mbrMessageInfo) {
        MbrMessage mbrMessage = mbrMessageMapper.selectByPrimaryKey(mbrMessageInfo.getMessageId());
        mbrMessage.setIsRevert(Constants.EVNumber.one);
        mbrMessage.setTime(getCurrentDate(FORMAT_18_DATE_TIME));
        mbrMessageMapper.updateByPrimaryKey(mbrMessage);
    }

    public PageUtils accountMessageList(Integer pageNo, Integer pageSize, Integer accountId, MbrMessageInfo dto) {
        MbrMessage mbrMessage = new MbrMessage();
        mbrMessage.setAccountId(accountId);
        MbrMessage message = mbrMessageMapper.selectOne(mbrMessage);
        if (nonNull(message)) {
            MbrMessageInfo info = new MbrMessageInfo();
            info.setMessageId(message.getId());
            info.setMsgType(dto.getMsgType());
            info.setIsDelete(Constants.EVNumber.zero);

            if(Objects.nonNull(dto.getMbrIsRead()) && dto.getMbrIsRead().equals(1)){    // ????????????????????????
                info.setIsRead(Constants.EVNumber.zero);
            }
            info.setCreateTime(DateUtil.format(DateUtil.getDateBefore(new Date(),14),DateUtil.FORMAT_10_DATE)); //????????????2??????
            PageHelper.startPage(pageNo, pageSize);
            PageHelper.orderBy(" createTime desc");

            List<MbrMessageInfo> messageInfos = mbrMessageInfoMapper.selectMbrMessageInfo(info);
            messageInfos.stream().forEach(ms -> {
                if (StringUtils.isNotEmpty(ms.getImageUrl())) {
                    ms.setImageUrl(gmApiService.queryGiniuyunUrl() + ms.getImageUrl());
                }
            });

            if (isNull(dto.getIsRead()) || dto.getIsRead().equals(0) ) {
                // ??????????????????????????????????????? ????????????????????????????????????????????????????????????????????????????????????????????????
                info.setIsRead(Constants.EVNumber.one);
                mbrMapper.updateMessageList(info);
            }

            return BeanUtil.toPagedResult(messageInfos);
        }
        return null;
    }

    public String accountMessageSend(Integer accountId, String loginName, MultipartFile uploadMessageFile, String textContent) {
        if (Objects.isNull(uploadMessageFile) && StringUtils.isEmpty(textContent)) {
            return null;
        }
        String resut;
        while (true) {
            String key = RedisConstants.ACCOUNT_MESSAGE_KEY + CommonUtil.getSiteCode() + accountId;
            Boolean isExpired = redisService.setRedisExpiredTimeBo(key, accountId, 10, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(isExpired)) {
                MbrMessageInfo messageInfo = new MbrMessageInfo();
                messageInfo.setAccountId(accountId);
                messageInfo.setIsSign(Constants.EVNumber.zero);
                messageInfo.setCreateUser(loginName);
                messageInfo.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
                messageInfo.setTextContent(textContent);
                messageInfo.setIsRead(Constants.EVNumber.one);
                // ????????????????????????/????????????
                messageInfo.setIsReadSys(Constants.EVNumber.zero);
                messageInfo.setIsDelete(Constants.EVNumber.zero);

                MbrMessage mbrMessage = new MbrMessage();
                mbrMessage.setAccountId(accountId);
                MbrMessage message = mbrMessageMapper.selectOne(mbrMessage);
                if (Objects.isNull(message)) {
                    MbrMessage mbrMessage1 = new MbrMessage();
                    mbrMessage1.setAccountId(accountId);
                    mbrMessage1.setLoginName(loginName);
                    mbrMessage1.setTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    mbrMessage1.setIsRevert(Constants.EVNumber.zero);
                    mbrMessageMapper.insert(mbrMessage1);
                    messageInfo.setMessageId(mbrMessage1.getId());
                } else {
                    messageInfo.setMessageId(message.getId());
                    message.setTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    message.setIsRevert(Constants.EVNumber.zero);
                    mbrMessageMapper.updateByPrimaryKey(message);
                }
                if (nonNull(uploadMessageFile)) {
                    try {
                        byte[] fileBuff = IOUtils.toByteArray(uploadMessageFile.getInputStream());
                        String fileName = qiNiuYunUtil.uploadFileKey(fileBuff);
                        messageInfo.setImageUrl(fileName);
                      /*  byte[] bs = CommonUtil.getImagesByte(uploadMessageFile);
                        String fileName = qiNiuYunUtil.uploadFileKey(bs);
                        messageInfo.setImageUrl(fileName);*/
                    } catch (Exception e) {
                        log.error("messageSend", e);
                        throw new RRException("??????????????????");
                    }
                }
                mbrMessageInfoMapper.insert(messageInfo);
                redisService.del(key);
                resut = nonNull(messageInfo.getImageUrl())
                        ? gmApiService.queryGiniuyunUrl() + messageInfo.getImageUrl() : messageInfo.getTextContent();
                break;
            }
        }
        return resut;
    }

    public int messageUnread(Integer accountId, MbrMessageInfo info) {
        MbrMessageInfo mbrMessageInfo = new MbrMessageInfo();
        mbrMessageInfo.setAccountId(accountId);
        mbrMessageInfo.setIsRead(Constants.EVNumber.zero);
        mbrMessageInfo.setMsgType(info.getMsgType());
        mbrMessageInfo.setCreateTime(DateUtil.format(DateUtil.getDateBefore(new Date(),14),DateUtil.FORMAT_10_DATE)); //????????????2??????
        int count = mbrMapper.findMessageCountByAccountId(mbrMessageInfo);
        return count;
    }

    /**
     * ??????????????????
     *
     * @param mbrMessage
     * @return
     */
    public List<CountEntity> messageCountByIsRevert(MbrMessage mbrMessage) {
        List<CountEntity> list = mbrMapper.messageCountByIsRevert(mbrMessage);
        return list;
    }

    /**
     * ???????????????????????????
     *
     * @param id
     */
    public void messageDelete(Integer id) {
        MbrMessageInfo messageInfo = new MbrMessageInfo();
        messageInfo.setId(id);
        messageInfo.setIsDelete(Constants.EVNumber.one);
        mbrMessageInfoMapper.updateByPrimaryKeySelective(messageInfo);
    }

    // ????????????????????????
    public void setMessageMbrRead(MbrMessageInfo info){
        // 0 ??????????????????????????? 1 ??????????????????????????????
        if(Integer.valueOf(Constants.EVNumber.zero).equals(info.getSetReadType()) || Objects.isNull(info.getSetReadType())){
            info.setIsRead(Constants.EVNumber.one); // ???????????????
            mbrMessageInfoMapper.updateByPrimaryKeySelective(info);
        }
        if(Integer.valueOf(Constants.EVNumber.one).equals(info.getSetReadType())){
            info.setIsRead(Constants.EVNumber.one);     // ?????????????????????
            info.setIsSign(Constants.EVNumber.two);    // 2??????
            mbrMessageInfoMapper.setMessageMbrRead(info);
        }
    }

    public void messageDeleteExpiration(String siteCode){
        log.info("???"+siteCode + "???messageDeleteExpiration==" +siteCode + "==start" );
        String key = RedisConstants.BATCH_MESSAGE_DELETE + siteCode ;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, siteCode, 20, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(isExpired)) {
            ThreadLocalCache.setSiteCodeAsny(siteCode);

            // ??????????????????
            mbrMessageInfoMapper.messageDeleteExpiration();

            redisService.del(key);
        }
        log.info("???"+siteCode + "???messageDeleteExpiration==" +siteCode + "==end" );
    }

	public Boolean deleteMessageInfoById(Integer accountId, MbrMessageInfo dto) {
		
		MbrMessageInfo mbrMessageInfo = new MbrMessageInfo();
		mbrMessageInfo.setId(dto.getId());
		
		// ??????????????????
		MbrMessageInfo selectByPrimaryKey = mbrMessageInfoMapper.selectOne(mbrMessageInfo);
		if (selectByPrimaryKey == null) {
			return true;
		}
		
		// ????????????
		MbrMessage mbrMessage = new MbrMessage();
		mbrMessage.setAccountId(accountId);
		mbrMessage.setId(selectByPrimaryKey.getMessageId());
		MbrMessage selectOne = mbrMessageMapper.selectOne(mbrMessage);
		if (selectOne == null) {
			return true;
		}
		
		selectByPrimaryKey.setIsDelete(1);
		mbrMessageInfoMapper.updateByPrimaryKey(selectByPrimaryKey);
		return true;
	}
}
