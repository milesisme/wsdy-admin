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

        // 更新管家已读标志
        MbrMessageInfo upInfo = new MbrMessageInfo();
        upInfo.setMessageId(mbrMessageInfo.getMessageId());
        upInfo.setIsReadSys(Constants.EVNumber.one);
        mbrMessageInfoMapper.updateMessageList(upInfo);

        return BeanUtil.toPagedResult(messageInfos);
    }

    public R messageSend(MbrMessageInfo mbrMessageInfo, MultipartFile uploadMessageFile) {
        // 处理上传图片
        if (nonNull(uploadMessageFile)) {
            try {
                byte[] fileBuff = IOUtils.toByteArray(uploadMessageFile.getInputStream());
                String fileName = qiNiuYunUtil.uploadFileKey(fileBuff);
                mbrMessageInfo.setImageUrl(fileName);
            } catch (Exception e) {
                log.error("messageSend-上传图片出错", e);
                throw new RRException("上传图片出错");
            }
        }
        mbrMessageInfo.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        mbrMessageInfo.setIsRead(Constants.EVNumber.zero);      // 未读
        mbrMessageInfo.setIsReadSys(Constants.EVNumber.one);    // 已读
        mbrMessageInfo.setIsDelete(Constants.EVNumber.zero);    // 未删除

        //此处创建逻辑
        List<MbrMessage> messageList = mbrMapper.findMbrListByNameAll(mbrMessageInfo);
        if (Collections3.isEmpty(messageList)) {
            throw new RRException("无会员信息");
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

                //messageInfo插入
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
            // 处理上传图片
            if (nonNull(uploadMessageFile)) {
                try {
                    byte[] fileBuff = IOUtils.toByteArray(uploadMessageFile.getInputStream());
                    String fileName = qiNiuYunUtil.uploadFileKey(fileBuff);
                    mbrMessageInfo.setImageUrl(fileName);
                } catch (Exception e) {
                    log.info("messageSend==messageSendBatch==上传图片出错==" + e);
                    throw new RRException("上传图片出错");
                }
            }

            // 异步插入消息并推送
            String siteCode = CommonUtil.getSiteCode();
            CompletableFuture.runAsync(() -> {
                ThreadLocalCache.setSiteCodeAsny(siteCode);
                try{
                    // 查询符合推送的数据: 推送的是限制有安装app
                    List<MbrMessage> messageList;
                    if(Integer.valueOf(Constants.EVNumber.one).equals(mbrMessageInfo.getIsPush())){
                        messageList = mbrMapper.findPushMbrListByName(mbrMessageInfo);
//                        messageList = mbrMapper.findMbrListByName(mbrMessageInfo);

                    }else{
                        messageList = mbrMapper.findMbrListByName(mbrMessageInfo);
                    }

                    if (Collections3.isEmpty(messageList)) {
                        log.info("messageSend==messageSendBatch==无会员信息" );
        //                throw new RRException("无会员信息");
                        return ;
                    }
                    // 消息发送
                    Long startTime = System.currentTimeMillis();
                    log.info("messageSend==messageSendBatch==startTime==" + getCurrentDate(FORMAT_18_DATE_TIME) + "===size==" + messageList.size());
                    messageSendAsync(messageList, mbrMessageInfo);
                    long costTime = System.currentTimeMillis() - startTime;
                    log.info("messageSend==messageSendBatch==endTime==" + getCurrentDate(FORMAT_18_DATE_TIME) + "===size==" + messageList.size() + "==costTime==" + costTime);

                    // 更新完数据库后再去推送
                    if(Integer.valueOf(Constants.EVNumber.one).equals(mbrMessageInfo.getIsPush())){
                        messagePushBatchSyn(mbrMessageInfo,siteCode,messageList);
                    }
                    // 清理数据
                    messageList.clear();
                }finally {
                    // 删除key
                    redisService.del(key);
                }
            });
        } catch (RRException e) {
            // 删除key
            redisService.del(key);
            throw e;
        } catch (Exception e) {
            // 删除key
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

            // 推送所有设备: 广播
            if(Integer.valueOf(Constants.EVNumber.one).equals(mbrMessageInfo.getIsAllDevice())){
                JPushNotificationDto dto = new JPushNotificationDto();
                dto.setMsgTitle(mbrMessageInfo.getPushTitle());     // 标题
                dto.setMsgContent(mbrMessageInfo.getPushContent()); // 内容
                dto.setTimeToLive(mbrMessageInfo.getTimeToLive());  // 极光推送离线保存时长

                jPushUtil.pushToAll(dto);
            }else{  // 推送指定会员： loginname
    //            String siteCode = CommonUtil.getSiteCode();
    //            mbrMessageInfo.setSiteCode(siteCode);
//                List<String> alias = mbrMapper.findMbrList(mbrMessageInfo);
                List<String> alias = messageList.stream().map(MbrMessage::getLoginName).collect(Collectors.toList());
                if (Collections3.isEmpty(alias)) {
                    return;
                }
                JPushNotificationDto dto = new JPushNotificationDto();
                dto.setMsgTitle(mbrMessageInfo.getPushTitle());     // 标题
                dto.setMsgContent(mbrMessageInfo.getPushContent()); // 内容
                dto.setAlias(alias);                                // 会员名
                dto.setTimeToLive(mbrMessageInfo.getTimeToLive());  // 极光推送离线保存时长
                // 文件推送接口20次/min，推送600次/min, 而别名推送单次1000条，所以低于1000用别名推送，大于1000用文件推送
    //            if(alias.size() < 1000){
    //                jPushUtil.pushToAliasList(dto);
    //            }else{
    //                jPushUtil.pushToAliasListByFile(dto);
    //            }
    //                jPushUtil.pushToAliasListByFile(dto);
                jPushUtil.pushToAliasList(dto);

                // 清理数据
                alias.clear();
            }
            long costTime = System.currentTimeMillis() - startTime;
            log.info("messageSend==JPush==messagePushBatchSyn==endTime==" + getCurrentDate(FORMAT_18_DATE_TIME) + "==costTime==" + costTime);
//        });
    }

    public void messageSendAsync(List<MbrMessage> messageList, MbrMessageInfo mbrMessageInfo) {
        // mbrMessageInfo 设置
        mbrMessageInfo.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        mbrMessageInfo.setIsRead(Constants.EVNumber.zero);      // 未读
        mbrMessageInfo.setIsReadSys(Constants.EVNumber.one);    // 已读
        mbrMessageInfo.setIsDelete(Constants.EVNumber.zero);    // 未删除

        // 统一处理时间和已读状态
        String time = getCurrentDate(FORMAT_18_DATE_TIME);
        messageList.stream().forEach(
                ms -> {
                    ms.setTime(time);
                    ms.setIsRevert(1 == mbrMessageInfo.getIsSign() ? 1 : ms.getIsRevert());
                }
        );

        // 插入组：mbrMessage
        List<MbrMessage> insertList = messageList.stream().filter(
                mbrMessage -> Objects.isNull(mbrMessage.getId())
        ).collect(Collectors.toList());

        log.info("messageSend==messageSendAsync==插入组mbrMessage size==" + insertList.size());

        // 更新组：mbrMessage
        List<MbrMessage> updateList = messageList.stream().filter(
                mbrMessage -> Objects.nonNull(mbrMessage.getId())
        ).collect(Collectors.toList());

        log.info("messageSend==messageSendAsync==更新组mbrMessage size==" + updateList.size());

        // 更新组：mbrMessageInfo
        List<MbrMessageInfo> updateInfoList = new ArrayList<>();
        updateList.stream().forEach(
                ms -> {
                    MbrMessageInfo tmp = new MbrMessageInfo();
                    tmp.setMessageId(ms.getId());       // 更新组原本就有messageid
                    tmp.setTextContent(mbrMessageInfo.getTextContent());
                    tmp.setImageUrl(mbrMessageInfo.getImageUrl());
                    tmp.setCreateTime(mbrMessageInfo.getCreateTime());
                    tmp.setCreateUser(mbrMessageInfo.getCreateUser());
                    tmp.setIsSign(mbrMessageInfo.getIsSign());
                    tmp.setIsRead(mbrMessageInfo.getIsRead());
                    tmp.setIsReadSys(mbrMessageInfo.getIsReadSys());
                    tmp.setIsDelete(mbrMessageInfo.getIsDelete());
                    tmp.setExpirationTime(mbrMessageInfo.getExpirationTime());  // 过期时间
                    updateInfoList.add(tmp);
                }
        );

        // 对插入组执行插入mbrMessage和插入mbrMessageInfo
        if(Collections3.isNotEmpty(insertList)){
            // 批量插入mbrMessage
            List<List<MbrMessage>> groupByLength = Lists.partition(insertList,50);
            groupByLength.stream().forEach(
                    tmp -> {
                        mbrMessageInfoMapper.batchInsertMbrMessage(tmp);
                    }
            );

            // 获取刚刚插入的mbrMessage
            MbrMessage qryTemp =  new MbrMessage();
            qryTemp.setTime(time);
            List<MbrMessage> tmpList = mbrMessageMapper.select(qryTemp);

            // 插入组：mbrMessageInfo
            List<MbrMessageInfo> insertInfoList = new ArrayList<>();
            tmpList.stream().forEach(
                    ms -> {
                        MbrMessageInfo tmp = new MbrMessageInfo();
                        tmp.setMessageId(ms.getId());       // 刚刚插入的messageid
                        tmp.setTextContent(mbrMessageInfo.getTextContent());
                        tmp.setImageUrl(mbrMessageInfo.getImageUrl());
                        tmp.setCreateTime(mbrMessageInfo.getCreateTime());
                        tmp.setCreateUser(mbrMessageInfo.getCreateUser());
                        tmp.setIsSign(mbrMessageInfo.getIsSign());
                        tmp.setIsRead(mbrMessageInfo.getIsRead());
                        tmp.setIsReadSys(mbrMessageInfo.getIsReadSys());
                        tmp.setIsDelete(mbrMessageInfo.getIsDelete());
                        tmp.setExpirationTime(mbrMessageInfo.getExpirationTime());  // 过期时间
                        insertInfoList.add(tmp);
                    }
            );

            // 批量插入MbrMessageInfo
            List<List<MbrMessageInfo>> insertGroupByLength = Lists.partition(insertInfoList,50);
            insertGroupByLength.stream().forEach(
                    tmp -> {
                        mbrMessageInfoMapper.batchInsertMbrMessageInfo(tmp);
                    }
            );

            // 数据清理
            insertList.clear();
            groupByLength.clear();
            tmpList.clear();
            insertInfoList.clear();
            insertGroupByLength.clear();
        }


        // 对更新组执行更新mbrMessage和插入mbrMessageInfo
        if(Collections3.isNotEmpty(updateList)){
            // 对更新组根据是否回复分组
            Map<Integer,List<MbrMessage>> updateListGroup = updateList.stream().collect(Collectors.groupingBy(MbrMessage::getIsRevert));
            // 更新mbrMessage
            for(Integer isRevert : updateListGroup.keySet()){
                if(Integer.valueOf(Constants.EVNumber.one).equals(isRevert)){   // 已回复
                    List<MbrMessage> updateListIsRevert = updateListGroup.get(isRevert);
                    // 批量更新mbrMessage
                    // 获取messageid组
                    List<Integer> messageIds = updateListIsRevert.stream().map(
                            ls ->{
                                return ls.getId();
                            }
                    ).collect(Collectors.toList());

                    // 批量更新mbrMessage
                    List<List<Integer>> groupByLength = Lists.partition(messageIds,50);
                    MbrMessage mbrMessage = updateListIsRevert.get(0);
                    mbrMessage.setIsRevert(isRevert);
                    groupByLength.stream().forEach(
                            tmp -> {
                                mbrMessage.setGroups(tmp);
                                mbrMessageInfoMapper.batchUpdateMbrMessage(mbrMessage);
                            }
                    );
                    // 数据清理
//                    updateListIsRevert.clear();
                    messageIds.clear();
                    groupByLength.clear();
                }
                if(Integer.valueOf(Constants.EVNumber.zero).equals(isRevert)){   // 待回复
                    List<MbrMessage> updateListIsRevert = updateListGroup.get(isRevert);
                    // 批量更新mbrMessage
                    // 获取messageid组
                    List<Integer> messageIds = updateListIsRevert.stream().map(
                            ls ->{
                                return ls.getId();
                            }
                    ).collect(Collectors.toList());

                    // 批量更新mbrMessage
                    List<List<Integer>> groupByLength = Lists.partition(messageIds,50);
                    MbrMessage mbrMessage = updateListIsRevert.get(0);
                    mbrMessage.setIsRevert(isRevert);
                    groupByLength.stream().forEach(
                            tmp -> {
                                mbrMessage.setGroups(tmp);
                                mbrMessageInfoMapper.batchUpdateMbrMessage(mbrMessage);
                            }
                    );

                    // 数据清理
//                    updateListIsRevert.clear();
                    messageIds.clear();
                    groupByLength.clear();
                }
            }

            // 数据清理
            updateListGroup.clear();

            // 批量插入MbrMessageInfo
            List<List<MbrMessageInfo>> insertGroupByLength = Lists.partition(updateInfoList,50);
            insertGroupByLength.stream().forEach(
                    tmp -> {
                        mbrMessageInfoMapper.batchInsertMbrMessageInfo(tmp);
                    }
            );

            // 数据清理
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

            if(Objects.nonNull(dto.getMbrIsRead()) && dto.getMbrIsRead().equals(1)){    // 查询会员未读消息
                info.setIsRead(Constants.EVNumber.zero);
            }
            info.setCreateTime(DateUtil.format(DateUtil.getDateBefore(new Date(),14),DateUtil.FORMAT_10_DATE)); //只查最近2周的
            PageHelper.startPage(pageNo, pageSize);
            PageHelper.orderBy(" createTime desc");

            List<MbrMessageInfo> messageInfos = mbrMessageInfoMapper.selectMbrMessageInfo(info);
            messageInfos.stream().forEach(ms -> {
                if (StringUtils.isNotEmpty(ms.getImageUrl())) {
                    ms.setImageUrl(gmApiService.queryGiniuyunUrl() + ms.getImageUrl());
                }
            });

            if (isNull(dto.getIsRead()) || dto.getIsRead().equals(0) ) {
                // 此处更新用户消息状态为已读 ，此处逻辑主要是为以前的消息管家里的系统消息一次性设置为会员已读
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
                // 增加管家是否已读/是否删除
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
                        throw new RRException("上传图片出错");
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
        mbrMessageInfo.setCreateTime(DateUtil.format(DateUtil.getDateBefore(new Date(),14),DateUtil.FORMAT_10_DATE)); //只查最近2周的
        int count = mbrMapper.findMessageCountByAccountId(mbrMessageInfo);
        return count;
    }

    /**
     * 会员留言统计
     *
     * @param mbrMessage
     * @return
     */
    public List<CountEntity> messageCountByIsRevert(MbrMessage mbrMessage) {
        List<CountEntity> list = mbrMapper.messageCountByIsRevert(mbrMessage);
        return list;
    }

    /**
     * 删除会员留言板留言
     *
     * @param id
     */
    public void messageDelete(Integer id) {
        MbrMessageInfo messageInfo = new MbrMessageInfo();
        messageInfo.setId(id);
        messageInfo.setIsDelete(Constants.EVNumber.one);
        mbrMessageInfoMapper.updateByPrimaryKeySelective(messageInfo);
    }

    // 设置消息会员已读
    public void setMessageMbrRead(MbrMessageInfo info){
        // 0 单条通知置为已读； 1 会员所有通知置为已读
        if(Integer.valueOf(Constants.EVNumber.zero).equals(info.getSetReadType()) || Objects.isNull(info.getSetReadType())){
            info.setIsRead(Constants.EVNumber.one); // 设置为已读
            mbrMessageInfoMapper.updateByPrimaryKeySelective(info);
        }
        if(Integer.valueOf(Constants.EVNumber.one).equals(info.getSetReadType())){
            info.setIsRead(Constants.EVNumber.one);     // 设置为会员已读
            info.setIsSign(Constants.EVNumber.two);    // 2通知
            mbrMessageInfoMapper.setMessageMbrRead(info);
        }
    }

    public void messageDeleteExpiration(String siteCode){
        log.info("【"+siteCode + "】messageDeleteExpiration==" +siteCode + "==start" );
        String key = RedisConstants.BATCH_MESSAGE_DELETE + siteCode ;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, siteCode, 20, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(isExpired)) {
            ThreadLocalCache.setSiteCodeAsny(siteCode);

            // 清理过期消息
            mbrMessageInfoMapper.messageDeleteExpiration();

            redisService.del(key);
        }
        log.info("【"+siteCode + "】messageDeleteExpiration==" +siteCode + "==end" );
    }

	public Boolean deleteMessageInfoById(Integer accountId, MbrMessageInfo dto) {
		
		MbrMessageInfo mbrMessageInfo = new MbrMessageInfo();
		mbrMessageInfo.setId(dto.getId());
		
		// 已存在的通知
		MbrMessageInfo selectByPrimaryKey = mbrMessageInfoMapper.selectOne(mbrMessageInfo);
		if (selectByPrimaryKey == null) {
			return true;
		}
		
		// 通知对象
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
