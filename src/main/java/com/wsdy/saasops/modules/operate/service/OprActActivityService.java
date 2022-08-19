
package com.wsdy.saasops.modules.operate.service;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_10_DATE;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.allActivityCode;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.birthdayCode;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.lotteryCode;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.mbrRebateCode;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.otherCode;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.preferentialCode;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.redPacketRainCode;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.registerGiftCode;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.upgradeBonusCode;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.vipPrivilegesCode;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.waterRebatesCode;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.firstChargeCode;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.wsdy.saasops.api.modules.activity.dto.HuPengRebateDto;
import com.wsdy.saasops.modules.activity.dto.FirstChargeDto;
import org.apache.commons.collections4.CollectionUtils;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.member.dao.MbrRebateFriendsMapper;
import com.wsdy.saasops.modules.member.dao.MbrRebateFriendsRewardMapper;
import com.wsdy.saasops.modules.member.dto.RebateFriendsDetailsDto;
import com.wsdy.saasops.modules.member.dto.RebateFriendsPersonalDto;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import com.wsdy.saasops.modules.member.entity.MbrRebateFriends;
import com.wsdy.saasops.modules.member.entity.MbrRebateFriendsReward;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.member.service.*;
import com.wsdy.saasops.modules.operate.dto.*;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.apisys.service.TGmApiService;
import com.wsdy.saasops.api.modules.user.dto.ActApplyDto;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.ActivityConstants;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.config.MessagesConfig;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.listener.BizEventType;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.mapper.AgentMapper;
import com.wsdy.saasops.modules.analysis.mapper.AnalysisMapper;
import com.wsdy.saasops.modules.api.dto.DepotCatDto;
import com.wsdy.saasops.modules.fund.dto.CountEntity;
import com.wsdy.saasops.modules.lottery.dto.LotteryActivityDto;
import com.wsdy.saasops.modules.mbrRebateAgent.dto.MbrRebateAgentRuleDto;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dto.RebateDto;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.operate.dao.OprActActivityMapper;
import com.wsdy.saasops.modules.operate.dao.OprActBlacklistMapper;
import com.wsdy.saasops.modules.operate.dao.OprActBonusMapper;
import com.wsdy.saasops.modules.operate.dao.OprActCatActivityMapper;
import com.wsdy.saasops.modules.operate.dao.OprActLabelMapper;
import com.wsdy.saasops.modules.operate.dao.OprActRuleMapper;
import com.wsdy.saasops.modules.operate.dao.TOpActtmplMapper;
import com.wsdy.saasops.modules.operate.dto.mixActivity.MixActivityDto;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.OprActBlacklist;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import com.wsdy.saasops.modules.operate.entity.OprActCatActivity;
import com.wsdy.saasops.modules.operate.entity.OprActLabel;
import com.wsdy.saasops.modules.operate.entity.OprActRule;
import com.wsdy.saasops.modules.operate.entity.TOpActtmpl;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class OprActActivityService {

    @Autowired
    private OperateActivityMapper operateMapper;
    @Autowired
    private OprActActivityMapper actActivityMapper;
    @Autowired
    private OprActRuleMapper oprActRuleMapper;
    @Autowired
    private TOpActtmplMapper acttmplMapper;
    @Autowired
    private OprActBonusMapper oprActBonusMapper;
    @Autowired
    private MessagesConfig messagesConfig;
    @Autowired
    private MbrAccountService mbrAccountService;
    @Autowired
    private OprActActivityCastService oprActActivityCastService;
    @Autowired
    private QiNiuYunUtil qiNiuYunUtil;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private OprActCatActivityMapper actCatActivityMapper;
    @Autowired
    private TOpActtmplService tOpActtmplService;
    @Autowired
    private AccountWaterCastService accountWaterCastService;
    @Autowired
    private AccountWaterSettlementService waterSettlementService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private OprActBlacklistMapper oprActBlacklistMapper;
    @Autowired
    private TGmGameService tGmGameService;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private OprRescueActivityService oprRescueActivityService;
    @Autowired
    private OprRegisterActivityService oprRegisterActivityService;
    @Autowired
    private OprMemDayActivityService oprMemDayActivityService;
    @Autowired
    private OprAppDownloadActivityService oprAppDownloadActivityService;
    @Autowired
    private OprApplyfirstDepositService applyfirstDepositService;
    @Autowired
    private OprDepositSentService depositSentService;
    @Autowired
    private OprVipDepositSentService vipDepositSentService;
    @Autowired
    private OprBettingGiftService bettingGiftService;
    @Autowired
    private OprActLabelMapper actLabelMapper;
    @Autowired
    private OprBirthdayActivityService birthdayActivityService;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    @Autowired
    private OprMixActivityService oprMixActivityService;
    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private TGmApiService tGmApiService;
    @Autowired
    private AnalysisMapper analysisMapper;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private MbrRebateFriendsRewardMapper mbrRebateFriendsRewardMapper;
    @Autowired
    private MbrRebateFriendsMapper mbrRebateFriendsMapper;
    @Autowired
    private MbrWalletService walletService;


    @Autowired
    private AuditAccountService auditAccountService;

    public OprActActivity activityInfo(Integer id) {
        OprActActivity actActivity = new OprActActivity();
        actActivity.setId(id);
        OprActActivity activity = Optional.ofNullable(
                operateMapper.findOprActActivityList(actActivity)
                        .stream().findAny()).get().orElse(null);
        OprActCatActivity catActivity = new OprActCatActivity();
        catActivity.setActivityId(activity.getId());
        List<OprActCatActivity> activityList = actCatActivityMapper.select(catActivity);
        if (Collections3.isNotEmpty(activityList)) {
            List<Integer> actIds = activityList.stream()
                    .map(OprActCatActivity::getCatId).collect(Collectors.toList());
            activity.setActIds(actIds);
            if (!StringUtils.isEmpty(activity.getMbLogoUrl())) {
                activity.setMbLogoUrl(tGmApiService.queryGiniuyunUrl() + activity.getMbLogoUrl());
            }
            if (!StringUtils.isEmpty(activity.getPcLogoUrl())) {
                activity.setPcLogoUrl(tGmApiService.queryGiniuyunUrl() + activity.getPcLogoUrl());
            }
        }
        return activity;
    }

    public PageUtils queryListPage(OprActActivity oprActBase, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<OprActActivity> list = operateMapper.findOprActActivityList(oprActBase);
        return BeanUtil.toPagedResult(list);
    }

    /**
     * 红利列表查询活动list
     *
     * @param oprActBase
     * @param pageNo
     * @param pageSize
     * @return
     */
    public PageUtils getActivitiesWithAuditCount(OprActActivity oprActBase, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<OprActActivity> list = operateMapper.getActivitiesWithAuditCount(oprActBase);
        return BeanUtil.toPagedResult(list);
    }

    public List<OprActActivity> activityListAll(OprActActivity activity) {
        activity.setAvailable((byte) Constants.EVNumber.one);
        return operateMapper.findOprActActivityList(activity);
    }

    public void save(ActivityDto activityDto, String userName, MultipartFile uploadPcFile, MultipartFile uploadMbFile, Long userId, String ip) {
        OprActActivity activity = new Gson().fromJson(activityDto.getActivity().toString(), OprActActivity.class);
        getImageUrl(uploadPcFile, uploadMbFile, activity);
        activity.setCreateUser(userName);
        setUseState(activity);
        saveActivtiy(activity);
        mbrAccountLogService.bonusAddInfo(activity, userName, userId, ip);
    }

    private void saveActivtiy(OprActActivity activity) {
        activity.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        activity.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        activity.setModifyUser(activity.getCreateUser());
        activity.setAvailable(Constants.Available.enable);
        activity.setIsdel(Constants.EVNumber.zero);
        setUseState(activity);
        actActivityMapper.insert(activity);
    }


    /**
     * 修改活动状态 优惠券状态 活动有效期年月日
     */
    public void updateActivityState() {
        List<OprActActivity> activities = operateMapper.findActivityBySatatus();
        if (Collections3.isNotEmpty(activities)) {
            activities.forEach(ac -> setUseState(ac));
            activities.stream().forEach(ac -> {
                OprActActivity actActivity = new OprActActivity();
                actActivity.setId(ac.getId());
                actActivity.setUseState(ac.getUseState());
                actActivityMapper.updateByPrimaryKeySelective(actActivity);
                if (ac.getUseState() == Constants.EVNumber.two) {
                    operateMapper.updateBounsState(ac.getId(), Constants.EVNumber.zero, "活动已失效，系统拒绝通过此红利申请");
                }
            });
        }
    }

    /**
     * 修改活动状态 优惠券状态 活动有效期年月日
     */
    public void updateActivityStateEx() {
        List<OprActActivity> activities = operateMapper.findActivityBySatatus();
        if (Collections3.isNotEmpty(activities)) {
            activities.forEach(ac -> setUseStateEx(ac));
            activities.stream().forEach(ac -> {
                OprActActivity actActivity = new OprActActivity();
                actActivity.setId(ac.getId());
                actActivity.setUseState(ac.getUseState());
                actActivityMapper.updateByPrimaryKeySelective(actActivity);
                if (ac.getUseState() == Constants.EVNumber.two) {
                    operateMapper.updateBounsState(ac.getId(), Constants.EVNumber.zero, "活动已失效，系统拒绝通过此红利申请");
                }
            });
        }
    }

    private void setUseState(OprActActivity activity) {
        Date start = DateUtil.parse(activity.getUseStart(), FORMAT_10_DATE);
        Date data = DateUtil.parse(getCurrentDate(FORMAT_10_DATE), FORMAT_10_DATE);
        Date end = DateUtil.parse(activity.getUseEnd(), FORMAT_10_DATE);
        int startTime = data.compareTo(start);
        int endTime = data.compareTo(end);
        activity.setUseState(Constants.EVNumber.two);
        if ((startTime == 0 || startTime == 1) && (endTime == 0 || endTime == -1)) {
            activity.setUseState(Constants.EVNumber.one);
        }
        if (startTime == -1 && endTime == -1) {
            activity.setUseState(Constants.EVNumber.zero);
        }
    }

    private void setUseStateEx(OprActActivity activity) {
        Date start = DateUtil.parse(activity.getUseStart(), FORMAT_10_DATE);
        Date data = DateUtil.parse(DateUtil.getPastDate(-1, FORMAT_10_DATE), FORMAT_10_DATE);    // 当前时间往后推一天
        Date dataForStart = DateUtil.parse(DateUtil.getPastDate(0, FORMAT_10_DATE), FORMAT_10_DATE);
        Date end = DateUtil.parse(activity.getUseEnd(), FORMAT_10_DATE);
        int startTime = dataForStart.compareTo(start);
        int endTime = data.compareTo(end);
        activity.setUseState(Constants.EVNumber.two);
        if ((startTime == 0 || startTime == 1) && (endTime == 0 || endTime == -1)) {
            activity.setUseState(Constants.EVNumber.one);
        }
        if (startTime == -1 && endTime == -1) {
            activity.setUseState(Constants.EVNumber.zero);
        }
    }

    private void getImageUrl(MultipartFile uploadPcFile, MultipartFile uploadMbFile, OprActActivity activity) {
        if (Objects.nonNull(uploadPcFile)) {
            String fileName;
            try {
                byte[] fileBuff = IOUtils.toByteArray(uploadPcFile.getInputStream());
                fileName = qiNiuYunUtil.uploadFileKey(fileBuff);
            } catch (Exception e) {
                throw new RRException(e.getMessage());
            }
            activity.setPcRemoteFileName(fileName);
            activity.setPcLogoUrl(fileName);
        }
        if (Objects.nonNull(uploadMbFile)) {
            String fileName;
            try {
                byte[] fileBuff = IOUtils.toByteArray(uploadMbFile.getInputStream());
                fileName = qiNiuYunUtil.uploadFileKey(fileBuff);
            } catch (Exception e) {
                throw new RRException(e.getMessage());
            }
            activity.setMbRemoteFileName(fileName);
            activity.setMbLogoUrl(fileName);
        }
    }

    /**
     * 校验规则
     *
     * @param actTmplId 活动模板表id
     * @param object    规则字符串
     * @param isSign    是否校验规则唯一性 true 校验 false 不校验
     */
    private void checkoutJson(Integer actTmplId, String object, Boolean isSign) {
        TOpActtmpl acttmpl = acttmplMapper.selectByPrimaryKey(actTmplId);
        // 校验规则json
        try {
            Gson gson = new Gson();
            if (TOpActtmpl.preferentialCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, JPreferentialDto.class);
            }
            if (TOpActtmpl.registerCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, JRegisterDto.class);
            }
            if (TOpActtmpl.depositSentCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, JDepositSentDto.class);
            }
            if (TOpActtmpl.waterRebatesCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, JWaterRebatesDto.class);
            }
            if (TOpActtmpl.contentCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, JContentDto.class);
            }
            if (TOpActtmpl.mbrRebateCode.equals(acttmpl.getTmplCode())) {
                RebateDto rebateDto  = gson.fromJson(object, RebateDto.class);
                if(rebateDto == null){
                    throw new R200Exception("好友推荐活动规则无法解析!");
                }

                if(StringUtil.isEmpty(rebateDto.getStartTime()) || StringUtil.isEmpty(rebateDto.getEndTime())){
                    throw new R200Exception("请配置活动奖励计算时间!");
                }

                if(rebateDto.getMinVipLevel() == null || rebateDto.getMaxVipLevel() == null ){
                    throw new R200Exception("请配置VIP参与等级!");
                }
                if(rebateDto.getMinVipLevel() > rebateDto.getMaxVipLevel()  ){
                    throw new R200Exception("最低等级不能大于最高等级!");
                }

            }
            if (TOpActtmpl.bettingGiftCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, BettingGiftDto.class);
            }
            if (TOpActtmpl.rescueCode.equals(acttmpl.getTmplCode())) {
                Type jsonType = new TypeToken<ActRuleBaseDto<ActRescueRuleDto>>() {
                }.getType();
                gson.fromJson(object, jsonType);
            }
            // 注册送
            if (TOpActtmpl.registerGiftCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, RegisterGiftDto.class);
            }
            // 会员日
            if (TOpActtmpl.memDayGiftCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, MemDayGiftDto.class);
            }
            if (otherCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, JOtherDto.class);
            }
            //升级礼金
            if (upgradeBonusCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, JUpgradeBonusDto.class);
            }
            //生日礼金
            if (birthdayCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, JbirthdayDto.class);
            }
            //VIP特权
            if (TOpActtmpl.vipPrivilegesCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, JDepositSentDto.class);
            }
            // VIP红包
            if (TOpActtmpl.vipPrivilegesCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, MemDayGiftDto.class);
            }
            // 抽奖
            if (TOpActtmpl.lotteryCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, LotteryActivityDto.class);
            }

            // 红包雨
            if (TOpActtmpl.redPacketRainCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, RedPacketRainDto.class);
            }

            // 全民代理
            if (TOpActtmpl.mbrRebateAgentCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, MbrRebateAgentRuleDto.class);
            }

            // 混合活动
            if (TOpActtmpl.mixActivityCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, MixActivityDto.class);
            }
            
            // APP下载彩金
            if (TOpActtmpl.appDownloadGiftCode.equals(acttmpl.getTmplCode())) {
            	gson.fromJson(object, AppDownloadGiftDto.class);
            }

            // 呼朋换友
            if(TOpActtmpl.mbrRebateHuPengCode.equals(acttmpl.getTmplCode())){
                gson.fromJson(object, HuPengRebateDto.class);
            }

            // 首存送返上级
            if(firstChargeCode.equals(acttmpl.getTmplCode())){
                gson.fromJson(object, FirstChargeDto.class);
            }

        } catch (Exception e) {
            throw new RRException(messagesConfig.getValue("activity.data"));
        }

        // 校验规则是否唯一：当且进存在一个同类型有效规则
        if (vipPrivilegesCode.equals(acttmpl.getTmplCode()) && Boolean.TRUE.equals(isSign)) {
            int count = operateMapper.findActRuleCount(acttmpl.getTmplCode());
            if (count > 0) {
                throw new R200Exception("已存在VIP特权，不可再次添加！");
            }
        }
        if (vipPrivilegesCode.equals(acttmpl.getTmplCode()) && Boolean.TRUE.equals(isSign)) {
            int count = operateMapper.findActRuleCount(acttmpl.getTmplCode());
            if (count > 0) {
                throw new R200Exception("已存在VIP红包，不可再次添加！");
            }
        }
        if (birthdayCode.equals(acttmpl.getTmplCode()) && Boolean.TRUE.equals(isSign)) {
            int count = operateMapper.findActRuleCount(acttmpl.getTmplCode());
            if (count > 0) {
                throw new R200Exception("已存在生日礼金，不可再次添加！");
            }
        }
        if (preferentialCode.equals(acttmpl.getTmplCode()) && Boolean.TRUE.equals(isSign)) {
            int count = operateMapper.findActRuleCount(acttmpl.getTmplCode());
            if (count > 0) {
                throw new R200Exception("已存在首存送，不可再次添加！");
            }
        }
        if (birthdayCode.equals(acttmpl.getTmplCode()) && Boolean.TRUE.equals(isSign)) {
            int count = operateMapper.findActRuleCount(acttmpl.getTmplCode());
            if (count > 0) {
                throw new R200Exception("已存在生日礼金，不可再次添加！");
            }
        }
        if (upgradeBonusCode.equals(acttmpl.getTmplCode()) && Boolean.TRUE.equals(isSign)) {
            int count = operateMapper.findActRuleCount(acttmpl.getTmplCode());
            if (count > 0) {
                throw new R200Exception("已存在升级礼金，不可再次添加！");
            }
        }
        if (mbrRebateCode.equals(acttmpl.getTmplCode()) && Boolean.TRUE.equals(isSign)) {
            int count = operateMapper.findActRuleCount(acttmpl.getTmplCode());
            if (count > 0) {
                throw new R200Exception("已存在好友返利活动，不可再次添加！");
            }
        }
        if (waterRebatesCode.equals(acttmpl.getTmplCode()) && Boolean.TRUE.equals(isSign)) {
            int count = operateMapper.findActRuleCount(acttmpl.getTmplCode());
            if (count > 0) {
                throw new R200Exception("已存在返水活动，不可再次添加！");
            }
        }
        if (registerGiftCode.equals(acttmpl.getTmplCode()) && Boolean.TRUE.equals(isSign)) {
            int count = operateMapper.findActRuleCount(acttmpl.getTmplCode());
            if (count > 0) {
                throw new R200Exception("已存在注册送活动，不可再次添加！");
            }
        }
        if (lotteryCode.equals(acttmpl.getTmplCode()) && Boolean.TRUE.equals(isSign)) {
            int count = operateMapper.findActRuleCount(acttmpl.getTmplCode());
            if (count > 0) {
                throw new R200Exception("已存在抽奖活动，不可再次添加！");
            }
        }
        // 红包雨
        if (redPacketRainCode.equals(acttmpl.getTmplCode()) && Boolean.TRUE.equals(isSign)) {
            int count = operateMapper.findActRuleCount(acttmpl.getTmplCode());
            if (count > 0) {
                throw new R200Exception("已存在红包雨活动，不可再次添加！");
            }
        }

        // 首存送返上级
        if (firstChargeCode.equals(acttmpl.getTmplCode()) && Boolean.TRUE.equals(isSign)) {
            int count = operateMapper.findActRuleCount(acttmpl.getTmplCode());
            if (count > 0) {
                throw new R200Exception("已存在首存送返上级，不可再次添加！");
            }
        }
    }

    public OprActActivity queryObject(Integer id) {
        return operateMapper.findOprActActivity(id);
    }


    /**
     * 根据条件，获得活动/优惠数据
     *
     * @param actCatId   活动分类
     * @param accountId  会员id，为null表示未登录
     * @param terminal   客户端类型 0 PC(默认)  1 mobile
     * @param discount   null/0优惠页面(默认)；1我的优惠页面（登录）
     * @param buttonShow 我的优惠页活动状态:-1表示全部，1进行中(默认)，0已失效
     * @param actCatId   1热门活动 3全部活动 4人气活动 7往期活动
     * @return
     */
    public PageUtils webActivityList(Integer pageNo, Integer pageSize, Integer actCatId,
                                     Integer accountId, Byte terminal, Integer discount, Integer buttonShow, String tmplCode, Integer isShow,
                                     String ip) {

        // 1. 查询参数处理
        if (StringUtils.isEmpty(terminal)) {
            terminal = ApiConstants.Terminal.pc;
        }
        MbrAccount mbrAccount = null;
        if (!StringUtils.isEmpty(accountId)) {
            mbrAccount = mbrAccountService.getAccountInfo(accountId);
        }
        discount = nonNull(discount) ? discount : Constants.EVNumber.zero;
        buttonShow = nonNull(buttonShow) ? buttonShow : Constants.EVNumber.one;
        PageHelper.startPage(pageNo, pageSize);

        // 2. 查询符合参数的活动
        List<OprActActivity> activityList = operateMapper.findWebActList(
                actCatId, accountId, terminal, discount, buttonShow, getCurrentDate(FORMAT_10_DATE), tmplCode, isShow);

        // 3.设置我的优惠按钮状态： 4不显示（默认），0已失效, 1立即领取，2立即存款，3已领取 --> 新版：只处理返回1和4
        // 其中4： 优惠列表表示不显示立即领取按钮，我的优惠表示不显示(领取入口在我的优惠里才需要处理为1）
        // 优惠列表时活动的展示，我的优惠是领取的展示
        setButtonShow(activityList, mbrAccount);
        // 4.返回数据：我的优惠/活动页面
        if (Constants.EVNumber.one == discount) {   // 我的优惠页面展示数据
            // 4.1 返回新增游戏类别
            List<DepotCatDto> list = tGmGameService.getCategoryAndDepotRelation(CommonUtil.getSiteCode(), Constants.EVNumber.zero);
            // 过滤不展示（4）的活动
            List<OprActActivity> visibleActList = activityList.stream().filter(at -> at.getButtonShow() != (byte) Constants.EVNumber.four).collect(Collectors.toList());
            visibleActList.stream().forEach(at -> at.setDepotCatDtoList(list));

            // 前后端使用同接口，只有有IP的时候才查是否可领取
            if (StringUtil.isNotEmpty(ip)) {
                // 判断用户是否可领取改活动，并展示可领取的等级
                for (OprActActivity opr : visibleActList) {
                    checkApplyActivity(accountId, opr, CommonUtil.getSiteCode(), ip);
                }
            }

            return BeanUtil.toPagedResult(visibleActList);
        } else {    // 管理后台活动页面展示数据
            return BeanUtil.toPagedResult(activityList);
        }
    }

    private void setButtonShow(List<OprActActivity> activityList, MbrAccount mbrAccount) {
        if (Collections3.isNotEmpty(activityList)) {
            activityList.stream().forEach(at -> {
                if (!StringUtils.isEmpty(at.getMbLogoUrl())) {
                    at.setMbLogoUrl(tGmApiService.queryGiniuyunUrl() + at.getMbLogoUrl());
                }
                if (!StringUtils.isEmpty(at.getPcLogoUrl())) {
                    at.setPcLogoUrl(tGmApiService.queryGiniuyunUrl() + at.getPcLogoUrl());
                }
                if (nonNull(mbrAccount)) {  // 登录状态
                    if (nonNull(at.getRuleId())) {
                        // 具体规则设置具体展示数据和按钮状态： 返回1/4/null(异常)   4不显示（默认）， 1立即领取
                        Integer isPass = checkoutActivityMsg(at, mbrAccount.getId());
                        at.setButtonShow(nonNull(isPass) ? isPass.byteValue() : at.getButtonShow());
                    }
                    if (at.getUseState() == Constants.EVNumber.two) {   // 活动失效
                        at.setButtonShow((byte) Constants.EVNumber.zero);
                    }
                } else {    // 非登录状态
                    if (at.getUseState() == Constants.EVNumber.two) {   // 活动失效
                        at.setButtonShow((byte) Constants.EVNumber.zero);
                    }
                }
            });
        }
    }

    private Integer checkoutActivityMsg(OprActActivity actActivity, int accountId) {
        Integer message = null;
        // 首存送
        if (TOpActtmpl.preferentialCode.equals(actActivity.getTmplCode())) {
            message = applyfirstDepositService.checkoutApplyFirstDeposit(actActivity, accountId);
        }
        // 存就送
        if (TOpActtmpl.depositSentCode.equals(actActivity.getTmplCode()) || TOpActtmpl.vipPrivilegesCode.equals(actActivity.getTmplCode())) {
            message = depositSentService.checkoutApplyDepositSentForNotMix(actActivity, accountId);
        }
        // 投就送
        if (TOpActtmpl.bettingGiftCode.equals(actActivity.getTmplCode())) {
            message = bettingGiftService.checkoutBettingGiftStatus(actActivity, accountId);
        }
        // 救援金
        if (TOpActtmpl.rescueCode.equals(actActivity.getTmplCode())) {
            message = oprRescueActivityService.checkoutRescueStatus(actActivity, accountId);
        }
        // 注册送
        if (TOpActtmpl.registerGiftCode.equals(actActivity.getTmplCode())) {
            message = oprRegisterActivityService.checkoutRegisterGiftStatus(actActivity, accountId);    // 返回不显示或立即领取
        }
        // 会员日
        if (TOpActtmpl.memDayGiftCode.equals(actActivity.getTmplCode())) {
            message = oprMemDayActivityService.checkoutMemDayGiftStatus(actActivity, accountId);    // 返回不显示或立即领取
        }
        // App下载彩金
        if (TOpActtmpl.appDownloadGiftCode.equals(actActivity.getTmplCode())) {
        	message = oprAppDownloadActivityService.checkoutMemDayGiftStatus(actActivity, accountId);    // 返回不显示或立即领取
        }
        // 生日礼金
        if (TOpActtmpl.birthdayCode.equals(actActivity.getTmplCode())) {
            message = birthdayActivityService.checkoutBirthdayActivityStatus(actActivity, accountId);    // 返回不显示或立即领取
        }
        // 混合活动 AQ0000024
        if (TOpActtmpl.mixActivityCode.equals(actActivity.getTmplCode())) {
            message = oprMixActivityService.checkoutMixActivityStatus(actActivity, accountId);    // 返回不显示或立即领取 4不显示， 1立即领取
        }
        return message;
    }

    public PageUtils findAccountBonusList(String startTime, String endTime, Integer accountId, Integer pageNo,
                                          Integer pageSize, Integer status, Integer activityId) {
        OprActBonus bonus = new OprActBonus();
        bonus.setStartTime(startTime);
        bonus.setEndTime(endTime);
        bonus.setAccountId(accountId);
        bonus.setFinancialCode(OrderConstants.FUND_ORDER_CODE_AA);  // 人工增加 里的 优惠类别--> 属于优惠（线下优惠活动）
        bonus.setStatus(status);
        bonus.setActivityId(activityId);

        // 判断三公标志 这个查询要在PageHelper.startPage 设置之前，否则会出问题
        SysSetting setting = sysSettingService.getSysSetting(SystemConstants.EG_SANGONG_FLG);

        PageHelper.startPage(pageNo, pageSize);

        List<OprActBonus> bonuses = Lists.newArrayList();
        if (Objects.nonNull(setting) && Objects.nonNull(setting.getSysvalue()) && String.valueOf(Constants.EVNumber.one).equals(setting.getSysvalue())) {
            bonuses = operateMapper.findAccountBonusListEgSanGong(bonus);
        } else {
            bonuses = operateMapper.findAccountBonusList(bonus);
//            if (null!=bonuses&&bonuses.size()>0){
//                for (OprActBonus oprActBonus:bonuses) { //彩金钱包备注拿添加时日志
//                    String memo = oprActBonus.getMemo();
//                   if (null!=memo&&memo.equals(OrderConstants.AGENT_MSF)){
//                       oprActBonus.setMemo(oprActBonus.getApplicationMemo());
//                   }
//                }
//            }
        }

        return BeanUtil.toPagedResult(bonuses);
    }

    public PageUtils activityAuditList(OprActBonus bonus, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<OprActBonus> auditDtoList = operateMapper.findWaterBonusList(bonus);
        for (OprActBonus oprActBonus:auditDtoList) {
            if (oprActBonus.getIsBlack().equals("0")){
                oprActBonus.setIsBlack("否");
            }else{
                oprActBonus.setIsBlack("是");
            }
        }
        return BeanUtil.toPagedResult(auditDtoList);
    }


    public SysFileExportRecord activityAuditListExport(OprActBonus model, Long userId, String module) {
        // 处理异步下载记录
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecordEx(userId, module);

        if (Objects.nonNull(record) && "success".equals(record.getSaveFlag())) {
            // 异步查询数据并导出
            String siteCode = CommonUtil.getSiteCode();
            activityAuditListExportAsyn(model, userId, module, siteCode);
        }
        return record;
    }

    private void dealAndTransData(OprActBonus dto) {
    }

    public void activityAuditListExportAsyn(OprActBonus model, Long userId, String module, String siteCode) {
        CompletableFuture.runAsync(() -> {
            ThreadLocalCache.setSiteCodeAsny(siteCode);

            log.info("export==userId==" + userId + "==module==" + module + "==异步查询处理==start");
            Long startTime1 = System.currentTimeMillis();

            // 这个key是saveAsynFileExportRecord 里的key
//            String key = RedisConstants.EXCEL_EXPORT + CommonUtil.getSiteCode() + module + userId;
            String key = RedisConstants.EXCEL_EXPORT + CommonUtil.getSiteCode() + module;
            List<Map<String, Object>> list = null;
            try {
                // 查询数据
                log.info("export==userId==" + userId + "==module==" + module + "==查询==start");
                Long startTime = System.currentTimeMillis();
                List<OprActBonus> lists = operateMapper.findWaterBonusList(model);
                log.info("export==userId==" + userId + "==module==" + module + "==查询==end==time==" + (System.currentTimeMillis() - startTime));

                SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sd.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                list = lists.stream().map(e -> {
                    dealAndTransData(e);
                    Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                    return entityMap;
                }).collect(Collectors.toList());
                // 此处显示清理
                lists.clear();
            } catch (R200Exception e) {
                redisService.del(key);
                log.info("export==userId==" + userId + "==module==" + module + "==error==" + e);
            } catch (Exception e) {
                redisService.del(key);
                log.info("export==userId==" + userId + "==module==" + module + "==error==" + e);
            }
            if (CollectionUtils.isEmpty(list)) {
                redisService.del(key);
                log.info("export==userId==" + userId + "==module==" + module + "==error==没有可导出的数据!");
            }

            // 同步执行生成excel+上传文件:
            log.info("export==userId==" + userId + "==module==" + module + "==同步执行上传文件==start==list.size==" + list.size());
            Long startTime2 = System.currentTimeMillis();
            try {
                sysFileExportRecordService.exportExcelSynByModule(userId, list, module, siteCode);
                log.info("export==userId==" + userId + "==module==" + module + "==同步执行上传文件==end==time==" + (System.currentTimeMillis() - startTime2));
            } catch (Exception e) {
                redisService.del(key);
                log.info("export==userId==" + userId + "==module==" + module + "==error==同步执行上传文件异常!");
            }
            log.info("export==userId==" + userId + "==module==" + module + "==异步查询处理==end==time==" + (System.currentTimeMillis() - startTime1));
        });
    }


    public PageUtils waterAuditList(OprActBonus bonus, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<ActivityWaterDetailDto> auditDtoList = operateMapper.waterAuditList(bonus);
        if (Collections3.isNotEmpty(auditDtoList)) {
            List<Integer> ids = auditDtoList.stream().map(ActivityWaterDetailDto::getId).collect(Collectors.toList());
            List<ActivityWaterCatDto> waterCatDtos = operateMapper.depotAuditListQry(ids);
            Map<Integer, List<ActivityWaterCatDto>> waterDepotGroupingBy =
                    waterCatDtos.stream().collect(
                            Collectors.groupingBy(ActivityWaterCatDto::getBonusId));

            for (Integer bonusId : waterDepotGroupingBy.keySet()) {
                auditDtoList.stream().forEach(dto -> {
                    if (dto.getId().equals(bonusId)) {
                        dto.setStatisticsByDepot(waterDepotGroupingBy.get(bonusId));
                        Map<Integer, List<ActivityWaterCatDto>> waterCatGroupingBy =
                                waterDepotGroupingBy.get(bonusId).stream().collect(
                                        Collectors.groupingBy(ActivityWaterCatDto::getCatId));
                        List<ActivityWaterCatDto> waterCatDtoList = Lists.newArrayList();
                        for (Integer catIdKey : waterCatGroupingBy.keySet()) {
                            List<ActivityWaterCatDto> catList = waterCatGroupingBy.get(catIdKey);
                            ActivityWaterCatDto waterCatDto = new ActivityWaterCatDto();
                            BigDecimal amountTotal = new BigDecimal(0);
                            BigDecimal validBetTotal = new BigDecimal(0);
                            for (ActivityWaterCatDto interWaterCatDto : catList) {
                                amountTotal = amountTotal.add(interWaterCatDto.getAmount());
                                validBetTotal = validBetTotal.add(interWaterCatDto.getValidbet());
                            }
                            waterCatDto.setCatId(catList.get(0).getCatId());
                            waterCatDto.setCatName(catList.get(0).getCatName());
                            waterCatDto.setTime(catList.get(0).getTime());
                            waterCatDto.setBonusId(catList.get(0).getBonusId());
                            waterCatDto.setAmount(amountTotal);
                            waterCatDto.setValidbet(validBetTotal);
                            waterCatDtoList.add(waterCatDto);
                        }
                        dto.setStatisticsByCat(waterCatDtoList);
                    }
                });
            }
        }
        return BeanUtil.toPagedResult(auditDtoList);
    }

    public PageUtils waterList(OprActBonus bonus, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<ActivityWaterTotalDto> auditDtoList = operateMapper.waterList(bonus);
        return BeanUtil.toPagedResult(auditDtoList);
    }

    public void activityAuditMsg(ActBonusAuditDto bonusAuditDto, String siteCode, String userName) {
        if (Collections3.isNotEmpty(bonusAuditDto.getBonusAuditListDtos())) {
            bonusAuditDto.getBonusAuditListDtos().stream().forEach(bs -> {
                OprActBonus actBonus = oprActBonusMapper.selectByPrimaryKey(bs.getBonuseId());
                OprActActivity actActivity = operateMapper.findOprActActivity(actBonus.getActivityId());
                BizEvent bizEvent = new BizEvent(this, siteCode, actBonus.getAccountId(), null);
                bizEvent.setUserName(userName);
                if (waterRebatesCode.equals(actActivity.getTmplCode())) {
                    return;
                } else {
                    if (Constants.EVNumber.one == actBonus.getStatus()) {
                        bizEvent.setEventType(BizEventType.PROMOTE_VERIFY_SUCCESS);
                    } else if (Constants.EVNumber.zero == actBonus.getStatus()) {
                        bizEvent.setEventType(BizEventType.PROMOTE_VERIFY_FAILED);
                    } else {
                        return;
                    }
                    bizEvent.setAcvitityName(actActivity.getActivityName());
                    bizEvent.setAcvitityMoney(actBonus.getBonusAmount());
                    applicationEventPublisher.publishEvent(bizEvent);
                }
            });
        }
    }

    public void isActivity(Integer id, String tmplCode, String memo, Integer status, String userName, ActBonusAuditDto bonusAuditDto) {

        OprActBonus actBonus = oprActBonusMapper.selectByPrimaryKey(id);
        if (Constants.EVNumber.two != actBonus.getStatus()) {
            throw new R200Exception("只能审核待处理的订单");
        }
        OprActBonus oldBonus = new OprActBonus();
        oldBonus.setStatus(actBonus.getStatus());

        actBonus.setStatus(status);
        actBonus.setAuditUser(userName);
        actBonus.setModifyAmountUser(userName);
        actBonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));

        // 允许备注为空
        if (!StringUtil.isEmpty(memo)) {
            actBonus.setMemo(memo);
        }
        if (Constants.EVNumber.zero == status) {
            oprActBonusMapper.updateByPrimaryKey(actBonus);
            // 操作日志_拒绝
            if (waterRebatesCode.equals(tmplCode)) {
                // 操作日志 返水列表审核
                waterSettlementService.esUpdateWaterBybonusId(CommonUtil.getSiteCode(), actBonus.getLoginName(), actBonus.getId());
                mbrAccountLogService.waterActivityAudit(actBonus, oldBonus, bonusAuditDto.getActivityName());
            } else {
                // 操作日志 红利列表审核
                mbrAccountLogService.bonusActivityAudit(actBonus, oldBonus, bonusAuditDto.getActivityName());
            }
            return;
        }
        if (waterRebatesCode.equals(tmplCode)) {
            accountWaterCastService.grantOprActBonus(actBonus, CommonUtil.getSiteCode());
            // 操作日志 返水列表审核
            mbrAccountLogService.waterActivityAudit(actBonus, oldBonus, bonusAuditDto.getActivityName());
        }
        // 存就送/VIP特权存就送/首存送
        if (TOpActtmpl.depositSentCode.equals(tmplCode) || TOpActtmpl.preferentialCode.equals(tmplCode) || TOpActtmpl.vipPrivilegesCode.equals(tmplCode)) {
            String financialCode = TOpActtmpl.depositSentCode.equals(tmplCode)
                    ? OrderConstants.ACTIVITY_DEPOSITSENT : OrderConstants.ACTIVITY_PREFERENTIAL;
            // 存就送/VIP特权存就送
            if (TOpActtmpl.vipPrivilegesCode.equals(tmplCode) || TOpActtmpl.depositSentCode.equals(tmplCode)) {
                financialCode = OrderConstants.ACTIVITY_DEPOSITSENT;
                actBonus.setCatId(bonusAuditDto.getCatId());
                oprActActivityCastService.auditDepositSentBonus(actBonus,
                        financialCode, bonusAuditDto.getActivityName(), Boolean.FALSE);
                // 首存送
            } else {
                oprActActivityCastService.auditOprActBonus(actBonus,
                        financialCode, bonusAuditDto.getActivityName(), Boolean.FALSE);
            }
            // 操作日志 红利列表审核
            mbrAccountLogService.bonusActivityAudit(actBonus, oldBonus, bonusAuditDto.getActivityName());
        }

        if (TOpActtmpl.bettingGiftCode.equals(tmplCode) || TOpActtmpl.rescueCode.equals(tmplCode)) {// 投就送/救援金
            actBonus.setDepositedAmount(null);  //
            String financialCode = TOpActtmpl.bettingGiftCode.equals(tmplCode)
                    ? OrderConstants.ACTIVITY_BETTINGGIFT : OrderConstants.ACTIVITY_RESCUE;
            oprActActivityCastService.auditOprActBonus(actBonus, financialCode, bonusAuditDto.getActivityName(), Boolean.FALSE);
            // 操作日志 红利列表审核
            mbrAccountLogService.bonusActivityAudit(actBonus, oldBonus, bonusAuditDto.getActivityName());
        }
        if (TOpActtmpl.registerGiftCode.equals(tmplCode)) {// 注册送
            actBonus.setDepositedAmount(null);
            oprActActivityCastService.auditOprActBonus(actBonus, OrderConstants.ACTIVITY_REGISTER, bonusAuditDto.getActivityName(), Boolean.FALSE);
            // 操作日志 红利列表审核
            mbrAccountLogService.bonusActivityAudit(actBonus, oldBonus, bonusAuditDto.getActivityName());
        }
        if (TOpActtmpl.memDayGiftCode.equals(tmplCode)) {// 会员日
            actBonus.setDepositedAmount(null);
            oprActActivityCastService.auditOprActBonus(actBonus, OrderConstants.ACTIVITY_MEMDAY, bonusAuditDto.getActivityName(), Boolean.FALSE);
            // 操作日志 红利列表审核
            mbrAccountLogService.bonusActivityAudit(actBonus, oldBonus, bonusAuditDto.getActivityName());
        }
        if (TOpActtmpl.appDownloadGiftCode.equals(tmplCode)) {// APP下载彩金
        	actBonus.setDepositedAmount(null);
        	oprActActivityCastService.auditOprActBonus(actBonus, OrderConstants.ACTIVITY_APPDOWNLOAD, bonusAuditDto.getActivityName(), Boolean.FALSE);
        	// 操作日志 红利列表审核
        	mbrAccountLogService.bonusActivityAudit(actBonus, oldBonus, bonusAuditDto.getActivityName());
        }
        if (TOpActtmpl.vipRedenvelopeCode.equals(tmplCode)) {// VIP每月红包
            actBonus.setDepositedAmount(null);
            oprActActivityCastService.auditOprActBonus(actBonus, OrderConstants.ACTIVITY_MYHB, bonusAuditDto.getActivityName(), Boolean.FALSE);
            mbrAccountLogService.bonusActivityAudit(actBonus, oldBonus, bonusAuditDto.getActivityName());
        }
        if (TOpActtmpl.upgradeBonusCode.equals(tmplCode)) {// 升级礼金
            oprActActivityCastService.auditOprActBonus(actBonus, OrderConstants.ACTIVITY_SJL, bonusAuditDto.getActivityName(), Boolean.FALSE);
            // 操作日志 红利列表审核
            mbrAccountLogService.bonusActivityAudit(actBonus, oldBonus, bonusAuditDto.getActivityName());
        }
        if (TOpActtmpl.birthdayCode.equals(tmplCode)) {// 生日礼金
            oprActActivityCastService.auditOprActBonus(actBonus, OrderConstants.ACTIVITY_SRLJ, bonusAuditDto.getActivityName(), Boolean.FALSE);
            // 操作日志 红利列表审核
            mbrAccountLogService.bonusActivityAudit(actBonus, oldBonus, bonusAuditDto.getActivityName());
        }
        if (TOpActtmpl.otherCode.equals(tmplCode)) {// 其他
            oprActActivityCastService.auditOprActBonus(actBonus, OrderConstants.ACTIVITY_QT, bonusAuditDto.getActivityName(), Boolean.FALSE);
            // 操作日志 红利列表审核
            mbrAccountLogService.bonusActivityAudit(actBonus, oldBonus, bonusAuditDto.getActivityName());
        }
        if (TOpActtmpl.lotteryCode.equals(tmplCode)) {// 抽奖
            if (actBonus.getPrizetype() == Constants.EVNumber.zero) {
                oprActActivityCastService.auditOprActBonus(actBonus, OrderConstants.ACTIVITY_CJL, bonusAuditDto.getActivityName(), Boolean.FALSE);
            } else {
                oprActActivityCastService.auditLotteryActBonus(actBonus);
            }
            // 操作日志 红利列表审核
            mbrAccountLogService.bonusActivityAudit(actBonus, oldBonus, bonusAuditDto.getActivityName());
        }
        if (TOpActtmpl.redPacketRainCode.equals(tmplCode)) {// 红包雨
            actBonus.setDepositedAmount(null);
            oprActActivityCastService.auditOprActBonus(actBonus, OrderConstants.ACTIVITY_HBY, bonusAuditDto.getActivityName(), Boolean.FALSE);
            // 操作日志 红利列表审核
            mbrAccountLogService.bonusActivityAudit(actBonus, oldBonus, bonusAuditDto.getActivityName());
        }
        if (TOpActtmpl.mixActivityCode.equals(tmplCode)) {  // 混合规则
//            // 子规则，存就送
//            if(TOpActtmpl.depositSentCode.equals(actBonus.getSubRuleTmplCode())){
//                actActivityCastService.auditDepositSentBonus(actBonus,
//                        OrderConstants.ACTIVITY_HH, bonusAuditDto.getActivityName(), Boolean.FALSE);
//            }
//            // 子规则，投就送/救援金
//            if(TOpActtmpl.rescueCode.equals(actBonus.getSubRuleTmplCode())
//            || TOpActtmpl.bettingGiftCode.equals(actBonus.getSubRuleTmplCode())){
//
//                actBonus.setDepositedAmount(null);
//                actActivityCastService.auditOprActBonus(actBonus, OrderConstants.ACTIVITY_HH, bonusAuditDto.getActivityName(), Boolean.FALSE);
//            }
//            // 子规则，其他
//            if(TOpActtmpl.otherCode.equals(actBonus.getSubRuleTmplCode())){
//                actActivityCastService.auditOprActBonus(actBonus, OrderConstants.ACTIVITY_HH, bonusAuditDto.getActivityName(), Boolean.FALSE);
//            }
            actBonus.setDepositedAmount(null);
            oprActActivityCastService.auditOprActBonus(actBonus, OrderConstants.ACTIVITY_HH, bonusAuditDto.getActivityName(), Boolean.FALSE);
            // 操作日志 红利列表审核
            mbrAccountLogService.bonusActivityAudit(actBonus, oldBonus, bonusAuditDto.getActivityName());
        }
    }

    public void activityModifyAmount(OprActBonus dto, SysUserEntity user) {
        // 校验
        OprActBonus actBonus = oprActBonusMapper.selectByPrimaryKey(dto.getId());
        if (Constants.EVNumber.two != actBonus.getStatus()) {
            throw new R200Exception("只能调整待处理的订单");
        }

        // 保留老的数据
        OprActBonus oldBonus = new OprActBonus();
        oldBonus.setBonusAmount(actBonus.getBonusAmount());      // 赠送金额
//        oldBonus.setDiscountAudit(actBonus.getDiscountAudit());  // 流水倍数
        oldBonus.setAuditAmount(actBonus.getAuditAmount());      // 流水金额

        // 构造更新对象
        actBonus.setBonusAmount(dto.getBonusAmount());                      // 赠送金额
//        actBonus.setDiscountAudit(dto.getDiscountAudit());                  // 流水倍数
        actBonus.setAuditAmount(dto.getAuditAmount());                      // 流水金额
        actBonus.setModifyAmountUser(user.getUsername());                   // 调整人
        actBonus.setModifyAmountTime(getCurrentDate(FORMAT_18_DATE_TIME));  // 调整时间
        actBonus.setModifyAmountMemo(dto.getModifyAmountMemo());            // 调整备注

        // 更新
        oprActBonusMapper.updateByPrimaryKeySelective(actBonus);

        // 记录日志
        mbrAccountLogService.activityModifyAmount(actBonus, oldBonus);
    }

    /**
     * 
     * 		编辑，保存活动是查询，如果当前活动规则已经有对应的活动并且状态非失效即：isSelect = true
     * @return
     */
    public List<OprActRule> ruleList() {
    	// 所有的活动类型对象
    	List<OprActRule> findRuleActivityList = operateMapper.findRuleActivityList();
    	//  有活动的活动类型的id
    	List<Integer> findExistRuleIds = operateMapper.findExistRuleIds();
    	for (OprActRule findRuleActivity : findRuleActivityList) {
    		// 有活动的活动类型包含当前id: isSelect = false
    		boolean contains = findExistRuleIds.contains(findRuleActivity.getId());
    		findRuleActivity.setIsSelect(!contains);
    	}
    	
        return findRuleActivityList;
    }

    public PageUtils activityRuleList(OprActRule actRule, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<OprActRule> list = operateMapper.findoActRuleList(actRule);
        return BeanUtil.toPagedResult(list);
    }

    public OprActRule activityRuleInfo(Integer id) {
        return oprActRuleMapper.selectByPrimaryKey(id);
    }

    public void saveActivityRule(OprActRule actRule) {
        // 校验：规则json
        checkoutJson(actRule.getActTmplId(), actRule.getRule(), Boolean.TRUE);
        actRule.setTime(getCurrentDate(FORMAT_18_DATE_TIME));
        actRule.setIsSelfHelp(Constants.EVNumber.zero);
        actRule.setIsLimit(Constants.EVNumber.zero);
        oprActRuleMapper.insert(actRule);

        mbrAccountLogService.saveActivityRule(actRule);
    }

    public void updateActivityRule(OprActRule actRule) {
        // 校验：绑定活动的规则不能禁用
        checkoutRuleAvailable(actRule);
        // 校验：规则json
        checkoutJson(actRule.getActTmplId(), actRule.getRule(), Boolean.FALSE);
        actRule.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        oprActRuleMapper.updateByPrimaryKeySelective(actRule);

        // 操作日志
        mbrAccountLogService.updateActivityRule(actRule);
    }

    public void updateAvailableActivityRule(OprActRule actRule) {
        checkoutRuleAvailable(actRule);
        OprActRule oprActRule = new OprActRule();
        oprActRule.setId(actRule.getId());
        oprActRule.setAvailable(actRule.getAvailable());
        // 获得修改前的操作规则
        OprActRule actRuleOld = oprActRuleMapper.selectByPrimaryKey(actRule.getId());
        oprActRuleMapper.updateByPrimaryKeySelective(oprActRule);

        // 操作日志
        mbrAccountLogService.updateAvailableActivityRule(actRule, actRuleOld);
    }

    private void checkoutRuleAvailable(OprActRule actRule) {
        if (actRule.getAvailable() == Constants.EVNumber.zero) {
            OprActActivity actActivity = new OprActActivity();
            actActivity.setRuleId(actRule.getId());
            int count = actActivityMapper.selectCount(actActivity);
            if (count > 0) {
                throw new R200Exception("绑定活动的规则不能禁用");
            }
        }
    }


    public void saveActivity(OprActActivity activity, MultipartFile uploadPcFile, MultipartFile uploadMbFile, String ip, Long userId) {
        getImageUrl(uploadPcFile, uploadMbFile, activity);
        saveActivtiy(activity);
        updateActivityState();
        saveActCatActivity(activity.getActIds(), activity.getId());
        mbrAccountLogService.bonusAddInfo(activity, activity.getCreateUser(), userId, ip);
    }

    private void saveActCatActivity(List<Integer> actIds, Integer id) {
        if (Collections3.isNotEmpty(actIds)) {
            List<OprActCatActivity> actCatActivities = Lists.newArrayList();
            actIds.stream().forEach(as -> {
                OprActCatActivity activity = new OprActCatActivity();
                activity.setActivityId(id);
                activity.setCatId(as);
                actCatActivities.add(activity);
            });
            actCatActivityMapper.insertList(actCatActivities);
        }
    }

    public void updateActivity(OprActActivity activity, MultipartFile uploadPcFile, MultipartFile uploadMbFile, String ip, Long userId) {

        OprActActivity oprActActivity = actActivityMapper.selectByPrimaryKey(activity.getId());
        if (Objects.nonNull(uploadPcFile) && StringUtil.isNotEmpty(oprActActivity.getPcRemoteFileName())) {
            qiNiuYunUtil.deleteFile(oprActActivity.getPcRemoteFileName());
        }
        if (Objects.nonNull(uploadMbFile) && StringUtil.isNotEmpty(oprActActivity.getMbRemoteFileName())) {
            qiNiuYunUtil.deleteFile(oprActActivity.getMbRemoteFileName());
        }

        activityUpdateEntitySet(activity, oprActActivity);
        getImageUrl(uploadPcFile, uploadMbFile, oprActActivity);
        if (isNull(uploadPcFile)) {
            oprActActivity.setPcRemoteFileName(null);
            oprActActivity.setPcLogoUrl(null);
        }
        if (isNull(uploadMbFile)) {
            oprActActivity.setMbRemoteFileName(null);
            oprActActivity.setMbLogoUrl(null);
        }
        oprActActivity.setLabelId(activity.getLabelId());
        oprActActivity.setIsOnline(activity.getIsOnline());
        actActivityMapper.updateByPrimaryKey(oprActActivity);

        if (oprActActivity.getUseState() == Constants.EVNumber.two) {
            operateMapper.updateBounsState(oprActActivity.getId(), Constants.EVNumber.zero, "活动已失效，系统拒绝通过此红利申请");
        }

        OprActCatActivity actCatActivity = new OprActCatActivity();
        actCatActivity.setActivityId(activity.getId());
        actCatActivityMapper.delete(actCatActivity);
        saveActCatActivity(activity.getActIds(), activity.getId());

        mbrAccountLogService.updateBonusEditInfo(activity, activity, activity.getModifyUser(), userId, ip);
    }

    private void activityUpdateEntitySet(OprActActivity newActivity, OprActActivity oldActivity) {
        oldActivity.setActivityName(newActivity.getActivityName());
        oldActivity.setUseStart(newActivity.getUseStart());
        oldActivity.setUseEnd(newActivity.getUseEnd());
        oldActivity.setIsShow(newActivity.getIsShow());
        oldActivity.setRuleId(newActivity.getRuleId());
        oldActivity.setSort(newActivity.getSort());
        if (Constants.EVNumber.one == newActivity.getEnablePc()) {
            oldActivity.setContent(newActivity.getContent());
        } else {
            oldActivity.setContent(null);
        }
        if (Constants.EVNumber.one == newActivity.getEnableMb()) {
            oldActivity.setMbContent(newActivity.getMbContent());
        } else {
            oldActivity.setMbContent(null);
        }
        oldActivity.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        oldActivity.setModifyUser(newActivity.getModifyUser());
        oldActivity.setEnablePc(newActivity.getEnablePc());
        oldActivity.setEnableMb(newActivity.getEnableMb());
        setUseState(oldActivity);
    }

    /**
     * 红利申请统计
     *
     * @param oprActBonus
     * @return
     */
    public List<CountEntity> activityAuditCountByStatus(OprActBonus oprActBonus) {
        List<CountEntity> list = operateMapper.activityAuditCountByStatus(oprActBonus);
        return list;
    }

    /**
     * 查询可用活动类型以及其下的活动
     *
     * @return
     */
    public List<TOpTmplDto> getActivityAndCat() {
        TOpActtmpl prama = new TOpActtmpl();
        prama.setAvailable((byte) 1);
        List<TOpActtmpl> tOpActtmpls = tOpActtmplService.queryListCond(prama);
        List<TOpActtmpl> ableTmpl = tOpActtmpls.stream().filter(t -> !waterRebatesCode.equals(t.getTmplCode())).collect(Collectors.toList());
        List<TOpTmplDto> tmplDtos = ableTmpl.stream().map(tmpl -> {
            TOpTmplDto tmplDto = new TOpTmplDto();
            BeanUtils.copyProperties(tmpl, tmplDto);
            List<OprActActivity> catActList = operateMapper.findActivityByTmplId(tmpl.getId());
            List<Map<String, Object>> actNameAndIds = getActivityInfoMap(catActList);
            tmplDto.setActActivities(actNameAndIds);
            return tmplDto;
        }).collect(Collectors.toList());
        return tmplDtos;
    }

    private List<Map<String, Object>> getActivityInfoMap(List<OprActActivity> catActList) {
        List<Map<String, Object>> actNameAndIds = catActList.stream().map(act -> {
            Map<String, Object> map = new HashMap<>(2);
            map.put("activityId", act.getId());
            map.put("activityName", act.getActivityName());
            map.put("ruleId", act.getRuleId());
            return map;
        }).collect(Collectors.toList());
        return actNameAndIds;
    }

    public void deleteDisableRule(OprActRule actRule, String userName) {
        OprActRule rule = oprActRuleMapper.selectByPrimaryKey(actRule);
        if (Objects.isNull(rule)) {
            throw new RRException("查无该规则！");
        }
        if (rule.getAvailable() == 1) {
            throw new RRException("开启状态的规则不可删除！");
        }
        rule.setIsDelete(Constants.EVNumber.one);
        rule.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        rule.setModifyUser(userName);
        oprActRuleMapper.updateByPrimaryKey(rule);

        // 操作日志
        mbrAccountLogService.deleteDisableRule(rule);

    }

    public void saveOprActBlacklist(OprActBlacklist blacklist) {
        Integer accountId = -1;
        String accountName = "";
        if (blacklist.getIsAgent() == Constants.EVNumber.zero) {
            MbrAccount param = new MbrAccount();
            param.setLoginName(blacklist.getLoginName());
            MbrAccount account = mbrAccountService.queryObjectCond(param);
            if (isNull(account)) {
                throw new R200Exception("用户不存在");
            }
            accountId = account.getId();
            accountName = account.getLoginName();
        }

        //假如是添加代理则需要判断是否是代理账号
        if (blacklist.getIsAgent() == Constants.EVNumber.one) {
            AgentAccount agent = agentMapper.getAgentByAccount(blacklist.getLoginName());
            if (isNull(agent)) {
                throw new R200Exception("代理不存在");
            }
            accountId = agent.getId();
            accountName = agent.getLoginName();
        }
        if (isBlackList(accountId, blacklist.getTmplCode())) {
            throw new R200Exception("用户已在黑名单，不能重复添加");
        }

        String key = RedisConstants.ACTIVITY_BLACKLIST + CommonUtil.getSiteCode() + blacklist.getLoginName();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, CommonUtil.getSiteCode(), 200, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(isExpired)) {
            try {
                blacklist.setAllCode(Constants.EVNumber.zero);
                //判断是否添加的是否全部活动
                if (blacklist.getTmplCode().equals(allActivityCode)) {
                    blacklist.setAllCode(Constants.EVNumber.one);
                }
                blacklist.setAccountId(accountId);
                oprActBlacklistMapper.insert(blacklist);

                mbrAccountLogService.addBlackListLog(blacklist.getIsAgent(), accountName);
            } finally {
                redisService.del(key);
            }
        }
    }

    public Boolean isBlackList(Integer accountId, String tmplCode) {
        OprActBlacklist param = new OprActBlacklist();
        param.setAccountId(accountId);
        param.setTmplCode(tmplCode);
        int blackCount = oprActBlacklistMapper.selectCount(param);
        if (blackCount > 0) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public PageUtils getBlackList(OprActBlacklist blacklist, int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        PageHelper.orderBy("createtime");
        List<OprActBlacklist> blacklists = oprActBlacklistMapper.select(blacklist);
        return BeanUtil.toPagedResult(blacklists);
    }

    public void deleteBlackList(OprActBlacklist blacklist) {
        OprActBlacklist bl = oprActBlacklistMapper.selectByPrimaryKey(blacklist);
        if(bl!= null){
            oprActBlacklistMapper.delete(blacklist);
            mbrAccountLogService.delBlackListLog(bl.getIsAgent(), bl.getLoginName());
        }
    }

    public void setSelfHelp(OprActRule actRule) {

        OprActRule param = new OprActRule();
        param.setIsSelfHelp(Constants.EVNumber.one);
        param.setIsDelete(Constants.EVNumber.zero);
        param.setTmplCode(TOpActtmpl.waterRebatesCode);
        param.setId(actRule.getId());
        int count = operateMapper.findSelfActRuleCount(param);
        if (count > 0) {
            throw new R200Exception("已存在开启洗码的返水优惠活动，请先关闭！");
        }
        oprActRuleMapper.updateByPrimaryKeySelective(actRule);

        OprActRule oprActRule = oprActRuleMapper.selectByPrimaryKey(actRule.getId());
        actRule.setRuleName(oprActRule.getRuleName());
        // 操作日志
        mbrAccountLogService.updateWaterSelf(actRule);
    }

    public void deleteAgentWaterRule(OprActRule rule) {
        OprActRule oldRule = oprActRuleMapper.selectByPrimaryKey(rule.getId());
        if (nonNull(oldRule) && StringUtil.isNotEmpty(oldRule.getRule())) {
            JWaterRebatesDto rebatesDto = jsonUtil.fromJson(oldRule.getRule(), JWaterRebatesDto.class);
            JWaterRebatesNeDto rebatesNeDto = rebatesDto.getRebatesNeDto();
            if (nonNull(rebatesNeDto)) {
                List<JWaterRebatesAgentDto> agentDtoList = rebatesNeDto.getAgentDtoList();
                if (Collections3.isNotEmpty(agentDtoList)) {
                    List<JWaterRebatesAgentDto> agentDtoListNew = agentDtoList.stream()
                            .filter(agentDto -> !rule.getAgentId().equals(agentDto.getAgentId()))
                            .collect(Collectors.toList());
                    rebatesNeDto.setAgentDtoList(agentDtoListNew);
                    OprActRule newRule = new OprActRule();
                    newRule.setId(oldRule.getId());
                    newRule.setRule(jsonUtil.toJson(rebatesDto));
                    oprActRuleMapper.updateByPrimaryKeySelective(newRule);
                }
            }
        }
    }

    /**
     * 申请活动活动申请页面
     */
    public BigDecimal applyActivity(int accountId, ActApplyDto actApplyDto, String siteCode, String ip) {

        MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
        // 检查该会员是否所有活动黑名单
        if (isBlackList(accountId, TOpActtmpl.allActivityCode)) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        //检查该会员的上级代理是否是所有活动代理黑名单
        if (valAgentBackList(account, TOpActtmpl.allActivityCode)) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }


        Integer activityId = actApplyDto.getActivityId();
        String key = RedisConstants.APPLY_ACTIVITY + siteCode + accountId + activityId ;
        try {
            Boolean isExpired = redisService.setRedisExpiredTimeBo(key, accountId, 200, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(isExpired)) {
                OprActActivity actActivity = operateMapper.findOprActActivity(activityId);
                String isActivity = checkoutActivity(actActivity, Boolean.TRUE);
                if (nonNull(isActivity)) {
                    throw new R200Exception(isActivity);
                }
                // 首存送
                if (TOpActtmpl.preferentialCode.equals(actActivity.getTmplCode())) {
                    applyfirstDepositService.applyfirstDeposit(actActivity, accountId, ip);
                }
                // 存就送
                if (TOpActtmpl.depositSentCode.equals(actActivity.getTmplCode())) {
                    depositSentService.applyDepositSentBonus(actActivity, accountId, ip);
                }
                // VIP特权存就送
                if (TOpActtmpl.vipPrivilegesCode.equals(actActivity.getTmplCode())) {
                    if (isNull(actApplyDto.getCatId())) {
                        throw new R200Exception("请选择场馆");
                    }
                    actActivity.setCatId(actApplyDto.getCatId());
                    vipDepositSentService.applyDepositSentBonus(actActivity, accountId, ip);
                }
                // 投就送
                if (TOpActtmpl.bettingGiftCode.equals(actActivity.getTmplCode())) {
                    bettingGiftService.applyBettingGiftBonus(actActivity, accountId, ip);
                }
                // 救援金
                if (TOpActtmpl.rescueCode.equals(actActivity.getTmplCode())) {
                    oprRescueActivityService.applyRescue(actActivity, accountId, ip);
                }
                // 注册送
                if (TOpActtmpl.registerGiftCode.equals(actActivity.getTmplCode())) {
                    oprRegisterActivityService.applyRegisterGift(actActivity, accountId, ip);
                }
                // 会员日
                if (TOpActtmpl.memDayGiftCode.equals(actActivity.getTmplCode())) {
                    oprMemDayActivityService.applyMemDayGift(actActivity, accountId, ip);
                }
                // app下载彩金
                if (TOpActtmpl.appDownloadGiftCode.equals(actActivity.getTmplCode())) {
                	oprAppDownloadActivityService.applyMemDayGift(actActivity, accountId, ip);
                }
                // 生日礼金
                if (TOpActtmpl.birthdayCode.equals(actActivity.getTmplCode())) {
                    birthdayActivityService.applyBirthday(actActivity, accountId, ip);
                }
                // 混合活动
                if (TOpActtmpl.mixActivityCode.equals(actActivity.getTmplCode())) {
                    if (StringUtil.isEmpty(actApplyDto.getSubRuleTmplCode())) {
                        throw new R200Exception("混合活动子规则code不为空");
                    }
                    oprMixActivityService.applyMixActivity(actActivity, actApplyDto.getSubRuleTmplCode(), accountId, ip);
                }
            }
            return BigDecimal.ZERO;
        } finally {
            redisService.del(key);
        }
    }

    /**
     * 查看用户是否能申请当前活动
     */
    public OprActActivity checkApplyActivity(int accountId, OprActActivity opr, String siteCode, String ip) {

        MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
        // 检查该会员是否所有活动黑名单
        if (isBlackList(accountId, TOpActtmpl.allActivityCode)) {
            log.info("检查会员{}是否可领活动，会员在活动黑名单", account.getLoginName());
            opr.setCanApply(Constants.EVNumber.zero);
            return opr;
        }
        //检查该会员的上级代理是否是所有活动代理黑名单
        if (valAgentBackList(account, TOpActtmpl.allActivityCode)) {
            log.info("检查会员{}是否可领活动，会员上级代理在活动黑名单", account.getLoginName());
            opr.setCanApply(Constants.EVNumber.zero);
            return opr;
        }

        Integer activityId = opr.getId();
        OprActActivity actActivity = operateMapper.findOprActActivity(activityId);
        String isActivity = checkoutActivity(actActivity, Boolean.TRUE);
        if (nonNull(isActivity)) {
            log.info("检查会员{}是否可领活动，活动ID{}，获取活动为空", account.getLoginName(), activityId);
            opr.setCanApply(Constants.EVNumber.zero);
            return opr;
        }
        // 首存送
        if (TOpActtmpl.preferentialCode.equals(actActivity.getTmplCode())) {
            applyfirstDepositService.checkfirstDeposit(actActivity, accountId, ip);
        }
        // 存就送
        if (TOpActtmpl.depositSentCode.equals(actActivity.getTmplCode())) {
            depositSentService.checkDepositSentBonus(actActivity, accountId, ip);
        }
        // VIP特权存就送
        /*if (TOpActtmpl.vipPrivilegesCode.equals(actActivity.getTmplCode())) {
            if (isNull(opr.getCatId())) {
                opr.setCanApply(Constants.EVNumber.zero);
                return opr;
            }
            actActivity.setCatId(opr.getCatId());
            vipDepositSentService.checkDepositSentBonus(actActivity, accountId, ip);
        }*/
        // 投就送
        if (TOpActtmpl.bettingGiftCode.equals(actActivity.getTmplCode())) {
            bettingGiftService.checkBettingGiftBonus(actActivity, accountId, ip);
        }
        opr.setCanApply(actActivity.getCanApply());
        opr.setCanApplyBonus(actActivity.getCanApplyBonus());
        opr.setActivityAlready(actActivity.getActivityAlready());
        return opr;
    }

    public String checkoutActivity(OprActActivity actActivity, Boolean isUseState) {
        if (Objects.isNull(actActivity)) {
            return "活动不存在";
        }
        if (Boolean.TRUE.equals(isUseState) && Constants.EVNumber.one != actActivity.getUseState()) {
            return "活动必须为进行中";
        }
        if (Constants.EVNumber.zero == actActivity.getAvailable()) {
            return "活动已经禁用";
        }
        return null;
    }

    public PageUtils claimedActivities(Integer pageNo, Integer pageSize, Integer accountId) {
        PageHelper.startPage(pageNo, pageSize);
        // 获取已领取的bonus,含混合活动子规则
        List<OprActActivity> activityList = operateMapper.getClaimedActivities(accountId);
        MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
        Integer actLevelId = account.getActLevelId();
        List<DepotCatDto> depotCatlist = tGmGameService.getCategoryAndDepotRelation(CommonUtil.getSiteCode(), Constants.EVNumber.zero);
        for (OprActActivity act : activityList) {
            //将最高领取金额展示成实际已领取金额
            BigDecimal claimedAmount = act.getAmountMax();
            if (nonNull(act.getRule())) {
                // 平台范围
                act.setDepotCatDtoList(depotCatlist);
                switch (act.getTmplCode()) {
                    case TOpActtmpl.preferentialCode:
                        oprActActivityCastService.setDonateAmountMax(act, actLevelId);
                    case TOpActtmpl.vipPrivilegesCode:
                        oprActActivityCastService.setDonateAmountMax(act, actLevelId);
                    case TOpActtmpl.depositSentCode:
                        oprActActivityCastService.setDonateAmountMax(act, actLevelId);
                    case TOpActtmpl.bettingGiftCode:
                        BettingGiftDto giftDto = jsonUtil.fromJson(act.getRule(), BettingGiftDto.class);
                        List<BettingGiftRuleDto> ruleDtos = giftDto.getBettingGiftRuleDtos();
                        bettingGiftService.setBettingGiftAmountMax(act, ruleDtos);
                    case TOpActtmpl.rescueCode:
                        Type jsonType = new TypeToken<ActRuleBaseDto<ActRescueRuleDto>>() {
                        }.getType();
                        ActRuleBaseDto<ActRescueRuleDto> baseDto = jsonUtil.fromJson(act.getRule(), jsonType);
                        List<ActRescueRuleDto> rescueRuleDtoList = baseDto.getRuleDtos();
                        oprRescueActivityService.setRescueAmountMax(act, rescueRuleDtoList);
                    case TOpActtmpl.mixActivityCode:        // 混合活动
                        // 判断是否领取完所有子规则
                        boolean isAllSubClaime = oprMixActivityService.checkClaimeAll(act);
                        act.setIsAllSubClaime(isAllSubClaime);
                }
            }
            act.setAmountMax(claimedAmount);//将最高领取金额展示成实际已领取金额
        }

        return BeanUtil.toPagedResult(activityList);
    }


    public List<OprActLabel> oprActLabelList() {
        return actLabelMapper.selectAll();
    }


    public PageUtils bonusAndTaskList(String startTime, String endTime, Integer accountId,
                                      Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        OprActBonus bonus = new OprActBonus();
        bonus.setStartTime(startTime);
        bonus.setEndTime(endTime);
        bonus.setAccountId(accountId);
        bonus.setFinancialCode(OrderConstants.FUND_ORDER_CODE_AA);  // 人工增加 里的 优惠类别--> 属于优惠（线下优惠活动）
        //bonus.setStatus(Constants.EVNumber.one);    // 成功的红利

        List<OprActBonus> bonuses = operateMapper.bonusAndTaskList(bonus);
        return BeanUtil.toPagedResult(bonuses);
    }

    //检查会员是否存在上级代理被添加进活动黑名单
    public boolean valAgentBackList(MbrAccount account, String code) {

        //找出该活动的所有代理黑名单列表
        OprActBlacklist blacklist = new OprActBlacklist();
        blacklist.setIsAgent(Constants.EVNumber.one);
        blacklist.setTmplCode(code);
        List<OprActBlacklist> blacklists = oprActBlacklistMapper.select(blacklist);
        boolean tag = false;

        for (int b = 0; b < blacklists.size(); b++) {
            if (account.getCagencyId().equals(blacklists.get(b).getAccountId())) {
                tag = true;
                return tag;
            }
        }

        return tag;
    }


    public List<OprActActivity> getAllActivitiesWithAuditCount(OprActActivity oprActBase) {
        List<OprActActivity> list = operateMapper.getActivitiesWithAuditCount(oprActBase);
        return list;
    }

    public SysFileExportRecord allActivityAuditListExport(List<OprActActivity> list, Long userId, String module) {
        List<Integer> ids = new ArrayList<>();
        for (OprActActivity act : list) {
            ids.add(act.getId());
        }
        OprActBonus model = new OprActBonus();
        model.setIds(ids);
        // 处理异步下载记录
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecordEx(userId, module);

        if (Objects.nonNull(record) && "success".equals(record.getSaveFlag())) {
            // 异步查询数据并导出
            String siteCode = CommonUtil.getSiteCode();
            activityAuditListExportAsyn(model, userId, module, siteCode);
        }
        return record;
    }

    public PageUtils friendRebateRewardList(String loginName, String startTime, String endTime, Integer groupId, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<RebateFriendsRewardDto> rebateFriendsRewardDtoList = operateMapper.friendRebateRewardList(loginName, groupId, startTime, endTime);
        return BeanUtil.toPagedResult(rebateFriendsRewardDtoList);
    }


    public PageUtils friendRebateList(String loginName, String startTime, String endTime, Integer groupId, Integer pageNo, Integer pageSize, String firstChargeStartTime,String firstChargeEndTime){
        PageHelper.startPage(pageNo, pageSize);
        List<RebateFriendsDto> rebateFriendsDtoList = operateMapper.friendRebateList(loginName, groupId, startTime, endTime, firstChargeStartTime, firstChargeEndTime);
        return BeanUtil.toPagedResult(rebateFriendsDtoList);
    }

    public PageUtils friendRebateRewardDetails(String loginName, String startTime, String endTime, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<RebateFriendsDetailsDto> rebateFriendsDetailsDtoList = operateMapper.friendRebateRewardDetails(loginName, startTime, endTime);
        return BeanUtil.toPagedResult(rebateFriendsDetailsDtoList);
    }


    public BigDecimal friendRebateRewardDetailsSummary(String loginName, String startTime, String endTime){
            return operateMapper.friendRebateRewardDetailsSummary(loginName, startTime, endTime);
    }


    public PageUtils friendRebatePersonalList(String loginName,  Integer pageNo, Integer pageSize){
        PageHelper.startPage(pageNo, pageSize);
        List<RebateFriendsPersonalDto> rebateFriendsPersonalDtoList = operateMapper.friendRebatePersonalList(loginName);
        return BeanUtil.toPagedResult(rebateFriendsPersonalDtoList);
    }

    public void addFriendRebateReward(AddFriendRebateRewardDto addFriendRebateRewardDto, String userName){
        MbrAccount account = new MbrAccount();
        account.setLoginName(addFriendRebateRewardDto.getLoginName());
        MbrAccount mbrAccount = accountMapper.selectOne(account);
        if(mbrAccount == null) {
            throw new R200Exception("账号不存在");
        }
            Integer subAccountId = mbrMapper.getSubAccountId(mbrAccount.getId(), addFriendRebateRewardDto.getSubLoginName());

            if(subAccountId == null || subAccountId <= 0){
                throw new R200Exception("绑定好友不存在");
            }

          String incomeTime = DateUtil.format(new Date(), FORMAT_10_DATE);
          MbrRebateFriends mbrRebateFriends = new MbrRebateFriends();
          mbrRebateFriends.setLoginName(addFriendRebateRewardDto.getLoginName());
          mbrRebateFriends.setAccountId(mbrAccount.getId());
          mbrRebateFriends.setAmount(BigDecimal.ZERO);
          mbrRebateFriends.setReward(addFriendRebateRewardDto.getAmount());
          mbrRebateFriends.setCreateTime(DateUtil.format(new Date(), DateUtil.FORMAT_18_DATE_TIME));
          mbrRebateFriends.setSubLoginName(addFriendRebateRewardDto.getSubLoginName());
          mbrRebateFriends.setSubAccountId(subAccountId);
          mbrRebateFriends.setOperationType(1);
          mbrRebateFriends.setType(addFriendRebateRewardDto.getRewardType());
          mbrRebateFriends.setIncomeTime(incomeTime);
          mbrRebateFriendsMapper.insert(mbrRebateFriends);

          MbrRebateFriendsReward mbrRebateFriendsReward = new MbrRebateFriendsReward();
          mbrRebateFriendsReward.setLoginName(mbrAccount.getLoginName());
          mbrRebateFriendsReward.setAccountId(mbrAccount.getId());
          mbrRebateFriendsReward.setReward(addFriendRebateRewardDto.getAmount());
          mbrRebateFriendsReward.setType(addFriendRebateRewardDto.getRewardType());
          mbrRebateFriendsReward.setCreater(userName);
          mbrRebateFriendsReward.setCreateTime(DateUtil.format(new Date(), DateUtil.FORMAT_18_DATE_TIME));
          mbrRebateFriendsReward.setMemo(addFriendRebateRewardDto.getMemo());
          mbrRebateFriendsReward.setGiveOutTime(DateUtil.format(new Date(), DateUtil.FORMAT_18_DATE_TIME));
          mbrRebateFriendsReward.setOperationType(1);
          mbrRebateFriendsReward.setOrderNo(String.valueOf(new SnowFlake().nextId()));
          mbrRebateFriendsReward.setIncomeTime(incomeTime);
          mbrRebateFriendsReward.setStatus(1);
          MbrBillDetail billDetail =  giveOutFriendRebateReward(addFriendRebateRewardDto.getAmount(),mbrAccount.getId(), mbrAccount.getLoginName(),  addFriendRebateRewardDto.getAudit() , addFriendRebateRewardDto.getAuditMultiple());
          if(billDetail != null){
              mbrRebateFriendsReward.setBillDetailId(billDetail.getId());
          }
          mbrRebateFriendsRewardMapper.insert(mbrRebateFriendsReward);

        mbrAccountLogService.addFriendRebateReward(addFriendRebateRewardDto, userName);
    }


    public void reduceFriendRebateReward(ReduceFriendRebateRewardDto reduceFriendRebateRewardDto, String userName, String ip){
        MbrAccount account = new MbrAccount();
        account.setLoginName(reduceFriendRebateRewardDto.getLoginName());
        MbrAccount mbrAccount = accountMapper.selectOne(account);
        if(mbrAccount == null) {
            throw new R200Exception("账号不存在");
        }
        String incomeTime = DateUtil.format(new Date(), FORMAT_10_DATE);
        MbrRebateFriendsReward mbrRebateFriendsReward = new MbrRebateFriendsReward();
        mbrRebateFriendsReward.setLoginName(mbrAccount.getLoginName());
        mbrRebateFriendsReward.setAccountId(mbrAccount.getId());
        mbrRebateFriendsReward.setReward(reduceFriendRebateRewardDto.getAmount().negate());
        mbrRebateFriendsReward.setType(reduceFriendRebateRewardDto.getRewardType());
        mbrRebateFriendsReward.setCreater(userName);
        mbrRebateFriendsReward.setIncomeTime(incomeTime);
        mbrRebateFriendsReward.setCreateTime(DateUtil.format(new Date(), DateUtil.FORMAT_18_DATE_TIME));
        mbrRebateFriendsReward.setMemo(reduceFriendRebateRewardDto.getMemo());
        mbrRebateFriendsReward.setGiveOutTime(DateUtil.format(new Date(), DateUtil.FORMAT_18_DATE_TIME));
        mbrRebateFriendsReward.setOperationType(1);
        mbrRebateFriendsReward.setOrderNo(String.valueOf(new SnowFlake().nextId()));
        mbrRebateFriendsReward.setStatus(1);

        MbrBillDetail mbrBillDetail =  walletService.castWalletAndBillDetail(mbrAccount.getLoginName(), mbrAccount.getId(), OrderConstants.ACCOUNT_REBATE_FA, reduceFriendRebateRewardDto.getAmount(), String.valueOf(new SnowFlake().nextId()), Boolean.FALSE,null,null);

        if (Objects.isNull(mbrBillDetail)) {
            throw new R200Exception("人工减少余额不足");
        }else{
            mbrRebateFriendsReward.setBillDetailId(mbrBillDetail.getId());
        }

        if(reduceFriendRebateRewardDto.getAudit() != null && reduceFriendRebateRewardDto.getAudit() ==1){
            auditAccountService.clearAccountAudit(account.getLoginName(), userName, null, reduceFriendRebateRewardDto.getMemo(), ip);
        }

        mbrRebateFriendsRewardMapper.insert(mbrRebateFriendsReward);
        mbrAccountLogService.reduceFriendRebateReward(reduceFriendRebateRewardDto, userName);
    }


    private MbrBillDetail giveOutFriendRebateReward(BigDecimal amount, Integer accountId, String loginName ,Integer audit,  Integer auditMultiple){
        MbrBillDetail mbrBillDetail = walletService.castWalletAndBillDetail(loginName, accountId, OrderConstants.ACCOUNT_REBATE_FA, amount, String.valueOf(new SnowFlake().nextId()), Boolean.TRUE,null,null);
        if (audit > Constants.EVNumber.zero &&  auditMultiple> Constants.EVNumber.zero ) {
            auditAccountService.insertAccountAudit(accountId, amount, null, new BigDecimal(auditMultiple), null, null, null, Constants.EVNumber.seven);
        }
        return mbrBillDetail;
    }

}
