package com.wsdy.saasops.modules.member.service;

import com.alibaba.fastjson.JSONObject;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.FriendRebateConstants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.listener.BizEventType;
import com.wsdy.saasops.modules.fund.dao.FundAuditMapper;
import com.wsdy.saasops.modules.fund.entity.FundAudit;
import com.wsdy.saasops.modules.member.dao.*;
import com.wsdy.saasops.modules.member.dto.*;
import com.wsdy.saasops.modules.member.entity.*;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.TOpActtmpl;
import com.wsdy.saasops.modules.operate.service.OprActActivityCastService;
import com.wsdy.saasops.modules.operate.service.OprActActivityService;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.annotations.Param;

import org.omg.PortableInterceptor.INACTIVE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wsdy.saasops.common.utils.DateUtil.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@Slf4j
public class AccountRebateCastNewService {


    public static final  int ZR = 3;    //真人
    public static final  int TY = 1;    //体育
    public static final  int DZ = 5;    //电子
    public static final  int QP = 6;    //棋牌
    public static final  int CP = 12;   //彩票
    public static final  int DJ = 9;    //电竞

    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private MbrRebateReportNewMapper rebateReportNewMapper;
    @Autowired
    private MbrWalletService walletService;
    @Autowired
    private FundAuditMapper fundAuditMapper;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private RedisService redisService;
    @Autowired
    private OprActActivityService oprActActivityService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private MbrRebateActDataMapper mbrRebateActDataMapper;
    @Autowired
    private MbrActivityLevelMapper mbrActivityLevelMapper;
    @Autowired
    private MbrRebateFriendsMapper mbrRebateFriendsMapper;
    @Autowired
    private MbrRebateFriendsRewardMapper mbrRebateFriendsRewardMapper;
    @Autowired
    private OprActActivityCastService oprActActivityCastService;

    public Integer isCastAccountRebate() {
        MbrRebateReportNew report = new MbrRebateReportNew();
        report.setReportTime(getCurrentDate(FORMAT_10_DATE));
        int count = rebateReportNewMapper.selectCount(report);
        return count;
    }

    public Integer isCastFriendRebate(String calcDay, Integer activityId){
        return mbrMapper.isCastFriendRebate(calcDay, activityId);
    }


    /**
     * 获得符合层级和游戏分类的返利规则：最低投注/返利比例
     * @param report     按游戏类别和会员的有效投注
     * @param levelDto   按等级返利规则
     * @return
     */
    private RebateCatDto getRebateCat(MbrRebateReportNew report,RebateLevelDto levelDto){
//
//        Optional<RebateMbrDepthDto> optionalDepthDto = levelDto.getDepthDtoList().stream().filter(depthDto ->
//                report.getDepth().equals(depthDto.getDepth())).findFirst();
//        if(optionalDepthDto.isPresent()){
//            RebateMbrDepthDto depthDto = optionalDepthDto.get();
//            Optional<RebateCatDto> optionalCatDto = depthDto.getCatDtoList().stream().filter(catDto ->
//                    report.getCatId().equals(catDto.getCatId())).findFirst();
//            if(optionalCatDto.isPresent()){
//                return optionalCatDto.get();
//            }
//        }
       return null;
    }


    /**
     * 获得昨日下级会员有有效投注的会员列表(会员符合活动等级/计算深度)
     * @param accountLevel      会员活动等级
     * @param rebateCastDepth   站点返利计算层级深度
     * @return
     */
    public List<MbrAccount> getRebateMbrList(Integer accountLevel,Integer rebateCastDepth){
        return mbrMapper.getRebateMbrList(accountLevel,getPastDate(1,FORMAT_10_DATE),Constants.EVNumber.one,rebateCastDepth);
    }

    /**
     * 根据游戏分类和下级会员计算并获得有效投注
     * @param supAccountId      上级会员ID
     * @param rebateCastDepth   站点返利计算层级深度
     * @return
     */
    public List<MbrRebateReportNew> getSubMbrRptBetList(Integer supAccountId,Integer rebateCastDepth){
        List<MbrRebateReportNew> rptBetDayModels = mbrMapper.getSubMbrRptRebateList(getPastDate(1,FORMAT_10_DATE),supAccountId,Constants.EVNumber.one,rebateCastDepth);
        return rptBetDayModels;
    }

    /**
     *
     * @param calcDay
     * @param actStartDay
     * @param actEndDay
     * @return
     */
    public List<CalcRebateFirstChargeDto> getFriendsRebateFirstCharge(String calcDay, String actStartDay, String actEndDay){
        return mbrMapper.findFriendsRebateFirstCharge(calcDay, actStartDay, actEndDay);
    }

    /**
     *
     * @param calcDay
     * @param actStartDay
     * @param actEndDay
     * @return
     */
    public List<CalcRebateUpgradeVipDto> getFriendsRebateUpgradeVip(String calcDay, String actStartDay,  String actEndDay){
        return mbrMapper.findFriendsRebateUpgradeVip(calcDay, actStartDay, actEndDay);
    }


    /**
     * 查询活动期玩家，某天的有效下注
     * @param calcDay  计算日
     * @param actStartDay 活动开始时间
     * @param actEndDay 活动结束时间
     * @return   有效下注
     */
    public List<CalcRebateValidBetDto> getFriendsRebateValidBet(String calcDay, String actStartDay,  String actEndDay){
        return mbrMapper.findFriendsRebateValidBet(calcDay, actStartDay, actEndDay);
    }

    /**
     *
     * @param calcDay
     * @param actStartDay
     * @param actEndDay
     * @return
     */
    public List<CalcRebateChargeDto> getFriendsRebateCharge(String calcDay, String actStartDay,  String actEndDay){
        return mbrMapper.findFriendsRebateCharge(calcDay, actStartDay, actEndDay);
    }



    public Map getFriendRebateCount(@Param("calcDay") String calcDay, @Param("accountId") Integer accountId){
        return mbrMapper.findFriendRebateCount(calcDay, accountId);
    }


    /**
     *
     * @param actStartDay
     * @param actEndDay
     * @param rebateChargeDtoList
     * @return
     */
    public Map<String, Object> getFriendRebateChargeCount( Integer accountId, String actStartDay,String actEndDay, String chargeDay, List<RebateChargeDto> rebateChargeDtoList ){
        return mbrMapper.findFriendRebateChargeCount(accountId, actStartDay, actEndDay, chargeDay, rebateChargeDtoList);
    }


    public Integer[] delete(String time){
        Integer deleteRewardCount= mbrMapper.deleteMbrRebateFriendsReward(time);
        Integer deleteDetailsCount = mbrMapper.deleteMbrRebateFriends(time);
        return new Integer[]{deleteRewardCount, deleteDetailsCount};
    }

    public void general(String genDay){
        //生成数据账号
        Map<Integer, String > map = new HashMap<>();

        // 父 tes9999
        map.put(25896, "test9933");
         map.put(25897, "htest9933");
         map.put(25898, "dtest9911");
         map.put(25904, "dtest9922");
         map.put(25910, "dtest9288");

        // 父 dtest9911
         map.put(25899, "htest9111");
         map.put(25900,  "htest9112");
         map.put(25901 , "dtest9199");


         // dtest9199
        map.put(25902 , "htest9191");
         map.put(25903 , "htest9192");

    // dtest9922
        map.put(25905 , "htest9211");
        map.put(25906 , "htest9212");
        map.put(25907 , "dtest9299");

        // dtest9299
        map.put(25908 , "htest9291");
        map.put(25909 , "htest9292");


        //dtest9288
        map.put(25911 , "htest9281");
        map.put(25912 , "htest9282");
        map.put(25913 , "dtest9289");



        for (Map.Entry<Integer, String > entry : map.entrySet() ){
            Calendar cal = Calendar.getInstance();

            if(StringUtil.isNotEmpty(genDay)){
                String[] date = genDay.split("-");
                cal.set(Integer.valueOf(date[0]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));

            }


            cal.set(Calendar.HOUR_OF_DAY,  (int)(Math.random() * (23- 0 + 1)));
            cal.set(Calendar.MINUTE,  (int)(Math.random() * (59- 0 + 1)));
            mbrMapper.procValidbet(entry.getValue(), new BigDecimal( 10 + (int)(Math.random() * (10000- 10 + 1))), DateUtil.format(cal.getTime(), DateUtil.FORMAT_18_DATE_TIME), DateUtil.format(cal.getTime(), FORMAT_10_DATE));


            cal.add(Calendar.DATE, -1);
            cal.set(Calendar.HOUR_OF_DAY,  (int)(Math.random() * (23- 0 + 1)));
            cal.set(Calendar.MINUTE,  (int)(Math.random() * (59- 0 + 1)));


            AccountLogDto accountLogDto = new AccountLogDto();
            accountLogDto.setItem("自动升级");
            accountLogDto.setStatus("成功");

            int start = 1 +  (int)(Math.random() * (8- 1 + 1));
            accountLogDto.setAfterChange(String.format("%s:VIP%d>>VIP%d", entry.getValue(), 0, start ));
            accountLogDto.setOperatorType(2);
            accountLogDto.setOperatorUser("系统审核");
            mbrMapper.procUpgrade(entry.getKey(),  DateUtil.format(cal.getTime(), DateUtil.FORMAT_18_DATE_TIME), jsonUtil.toJson(accountLogDto));


            cal.set(Calendar.HOUR_OF_DAY,  (int)(Math.random() * (23- 0 + 1)));
            cal.set(Calendar.MINUTE,  (int)(Math.random() * (59- 0 + 1)));
            mbrMapper.procCharge(new BigDecimal( 10 + (int)(Math.random() * (10000- 10 + 1))), entry.getKey(), DateUtil.format(cal.getTime(), DateUtil.FORMAT_18_DATE_TIME));
        }

    }


    /**
     *  按活动等级计算返利，颗粒度会员
     * @param siteCode  站点siteCode
     * @param levelDto  按等级返利规则
     * @param rebateCastDepth   站点返利计算层级深度
     * @param rebateDto 返利规则
     */
    public void castMbrRebateByLevel(String siteCode,RebateLevelDto levelDto,Integer rebateCastDepth,RebateDto rebateDto){
        // 获得昨日下级会员有有效投注的会员(会员符合活动等级/计算深度)
        List<MbrAccount> supAccounts = getRebateMbrList(levelDto.getLevel(),rebateCastDepth);
        // 按会员计算该会员下级会员返利
        supAccounts.stream().forEach(supAccount->{
            CompletableFuture.runAsync(() -> {
                castMbrRebate(siteCode,supAccount,levelDto,rebateCastDepth,rebateDto);
            });
        });
    }


    /**
     * 计算每个会员获得的返利
     * @param siteCode 站点siteCode
     * @param supAccount 上级会员
     * @param levelDto   按等级返利规则
     * @param rebateCastDepth 站点返利计算层级深度
     * @param rebateDto 返利规则
     */
    @Transactional
    public void castMbrRebate(String siteCode,MbrAccount supAccount,RebateLevelDto levelDto,Integer rebateCastDepth,RebateDto rebateDto){
        ThreadLocalCache.setSiteCodeAsny(siteCode);

        if(oprActActivityService.isBlackList(supAccount.getId(), TOpActtmpl.mbrRebateCode)||oprActActivityService.isBlackList(supAccount.getId(), TOpActtmpl.allActivityCode)){
            log.info(supAccount.getLoginName()+"是好友返利活动黑名单会员");
            return;
        }


            //判断是否存在vip活动代理黑名单
            if (oprActActivityService.valAgentBackList(supAccount,TOpActtmpl.mbrRebateCode)){
                log.info(supAccount.getLoginName()+"上级代理存在好友返利活动黑名单");
                return;
            }
            //判断是否存在所有活动黑名单
            if (oprActActivityService.valAgentBackList(supAccount,TOpActtmpl.allActivityCode)){
                log.info(supAccount.getLoginName()+"上级代理存在所有活动黑名单");
                return;
            }

        log.info(supAccount.getLoginName()+":开始计算返利");
        String key = RedisConstants.ACCOUNT_REBATE + siteCode+"_"+supAccount.getId();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, supAccount.getId(), 10, TimeUnit.MINUTES);
        if(Boolean.TRUE.equals(isExpired)) {
            // 判断该会员当日是否计算过下级会员返利
            if(getSupMbrIsCastedRebate(supAccount.getId())){
                redisService.del(key);
                return;
            }
            List<MbrRebateReportNew> reportNewList = new ArrayList<>();
            // 根据游戏分类和下级会员计算并获得有效投注
            List<MbrRebateReportNew> rptBetDayModels = getSubMbrRptBetList(supAccount.getId(), rebateCastDepth);
            // 根据有效投注和层级规则，计算每个游戏分类给上级的返利
            rptBetDayModels.stream().forEach(model -> {
                MbrRebateReportNew rebateReportNew = castTopRebate(model, levelDto, supAccount);
                if (nonNull(rebateReportNew)) {
                    reportNewList.add(rebateReportNew);
                }
            });
            // 计算总返利并入库返利记录数据和钱包增加
            if (CollectionUtils.isNotEmpty(reportNewList)) {
                BigDecimal subRebateAmount = reportNewList.stream().map(MbrRebateReportNew::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                FundAudit fundAudit = rebateToMbrAccount(subRebateAmount, supAccount, rebateDto);
                // 批量入库返利表
                batchInsert(reportNewList, fundAudit.getId());
                log.info(supAccount.getLoginName()+":结束计算返利");
            }
            redisService.del(key);
        }

    }


    private boolean getSupMbrIsCastedRebate(Integer supAccountId){
        MbrRebateReportNew report = new MbrRebateReportNew();
        report.setReportTime(getCurrentDate(FORMAT_10_DATE));
        report.setAccountId(supAccountId);
        int count = rebateReportNewMapper.selectCount(report);

        return count > 0 ? Boolean.TRUE:Boolean.FALSE;
    }

    private void batchInsert(List<MbrRebateReportNew> reportNewList,Integer auditId){
        Integer batchSize = 1000;
        Integer count = reportNewList.size()/batchSize;
        if(count*batchSize<reportNewList.size()){
            count +=1;
        }
        for(int i = 0 ;i<count;i++){
            List<MbrRebateReportNew> rptBetDayModelsTmp = new ArrayList<>();
            Integer lowIndex = i*batchSize;
            Integer highIndex = (i+1)*batchSize;
            if(highIndex>reportNewList.size()){
                highIndex = reportNewList.size();
            }
            for(int j = lowIndex;j<highIndex;j++){
                reportNewList.get(j).setAuditId(auditId);
                rptBetDayModelsTmp.add(reportNewList.get(j));
            }
            rebateReportNewMapper.insertList(rptBetDayModelsTmp);
        }
    }

    /**
     * 计算每个游戏分类给上及的返利
     * @param model      按游戏类别和会员的有效投注
     * @param levelDto   按等级返利规则
     * @param supAccount 上级会员
     * @return 返利对象
     */
    private MbrRebateReportNew castTopRebate(MbrRebateReportNew model,RebateLevelDto levelDto,MbrAccount supAccount){
        // 获得符合层级和游戏分类的返利规则：最低投注/返利比例
        RebateCatDto catDto = getRebateCat(model,levelDto);
        // 根据规则，计算返利
        if(nonNull(catDto)){
            BigDecimal minValidBet = catDto.getValidBet() == null ? BigDecimal.ZERO:catDto.getValidBet();
            catDto.setTopRebate(isNull(catDto.getTopRebate()) ? BigDecimal.ZERO : catDto.getTopRebate());
            if (( model.getValidbet().compareTo(minValidBet) != -1)
                    &&  catDto.getTopRebate().compareTo(BigDecimal.ZERO) == 1) {
                MbrRebateReportNew rebateReport = new MbrRebateReportNew();
                rebateReport.setValidbet(model.getValidbet());
                rebateReport.setAccountId(supAccount.getId());
                rebateReport.setLoginName(supAccount.getLoginName());
                rebateReport.setReportTime(getCurrentDate(FORMAT_10_DATE));
                rebateReport.setCatId(catDto.getCatId());
                rebateReport.setDepth(model.getDepth());
                BigDecimal contributeAmount = catDto.getTopRebate().divide(new BigDecimal(Constants.ONE_HUNDRED)).multiply(model.getValidbet());
                rebateReport.setAmount(contributeAmount);
                rebateReport.setSubAccountId(model.getSubAccountId());
                rebateReport.setSubLoginName(model.getSubLoginName());
                return rebateReport;
            }
        }
        return null;
    }


    public FundAudit rebateToMbrAccount(BigDecimal amount, MbrAccount supAccount,RebateDto rebateDto){

        FundAudit fundAudit = new FundAudit();
        fundAudit.setAccountId(supAccount.getId());
        fundAudit.setAmount(amount);
        fundAudit.setMemo("返利增加");
        fundAudit.setFinancialCode(OrderConstants.ACCOUNT_REBATE_FA);
        fundAudit.setLoginName(supAccount.getLoginName());
        fundAudit.setStatus(Constants.EVNumber.one);
        fundAudit.setDepositType(Constants.EVNumber.three);
        fundAudit.setOrderNo(String.valueOf(new SnowFlake().nextId()));
        fundAudit.setOrderPrefix(OrderConstants.FUND_ORDER_AUDIT);
        fundAudit.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        fundAudit.setCreateUser(Constants.SYSTEM_USER);
        fundAudit.setModifyTime(fundAudit.getCreateTime());
        fundAudit.setModifyUser(Constants.SYSTEM_USER);
        fundAudit.setAuditTime(fundAudit.getCreateTime());
        fundAudit.setAuditUser(Constants.SYSTEM_USER);
        MbrBillDetail mbrBillDetail = walletService.castWalletAndBillDetail(fundAudit.getLoginName(),
                fundAudit.getAccountId(), fundAudit.getFinancialCode(), fundAudit.getAmount(),
                fundAudit.getOrderNo(), Boolean.TRUE,null,null);
        fundAudit.setBillDetailId(mbrBillDetail.getId());
        fundAuditMapper.insert(fundAudit);

//        if (nonNull(rebateDto) && rebateDto.getAuditType() == Constants.EVNumber.one) {
        if (nonNull(rebateDto) && nonNull(rebateDto.getAuditMultiple()) && rebateDto.getAuditMultiple().intValue() > Constants.EVNumber.zero ) {
            auditAccountService.insertAccountAudit(supAccount.getId(),
                    amount, null, new BigDecimal(rebateDto.getAuditMultiple()), null,
                    null, null, Constants.EVNumber.one);
        }

        applicationEventPublisher.publishEvent(
                new BizEvent(this, CommonUtil.getSiteCode(), supAccount.getId(), BizEventType.ACCOUNT_REBATE,
                        amount, fundAudit.getOrderPrefix() + fundAudit.getOrderNo()));
        return fundAudit;
    }

    // 统计首充返利信息
    public void statsFirstCharge(String calcDay, String actStartTime, String actEndTime, Integer activityId,  Map<Integer, Map<Integer, List<MbrRebateFriends>>> mbrRebateFriendsMap, String incomeDay, String siteCode, StringBuilder stringBuilder){
        List<CalcRebateFirstChargeDto>  friendsRebateFirstCharge = getFriendsRebateFirstCharge(calcDay, actStartTime, actEndTime);
        String msg = "[friendRebate_" + siteCode  + "]"+"===================查询好友首充=================== \r\n" + "  查询条件: calcDay = "+ calcDay +" actStartTime=" + actStartTime + " actEndTime=" + actEndTime+ "\r\n" +"  查询结果: "+ friendsRebateFirstCharge.size()+ "条"+ "\r\n";
        log.info(msg);
        stringBuilder.append(msg);
        stringBuilder.append(" 结果明细: "  + "\r\n" );
        for(int i = 0; i < friendsRebateFirstCharge.size() ; i ++ ){
            stringBuilder.append("  "+ (i+1) + " " + JSONObject.toJSONString(friendsRebateFirstCharge.get(i)) + "\r\n");
        }
        for(CalcRebateFirstChargeDto calcRebateFirstDto: friendsRebateFirstCharge ){
            MbrRebateFriends mbrRebateFriends = initMbrRebateFriend(mbrRebateFriendsMap, calcRebateFirstDto.getLoginName(), calcRebateFirstDto.getAccountId(), calcRebateFirstDto.getSubLoginName(), calcRebateFirstDto.getSubAccountId(),activityId, calcRebateFirstDto.getActLevelId(), incomeDay);
            mbrRebateFriends.setType(FriendRebateConstants.FC.getValue());
            mbrRebateFriends.setAmount(calcRebateFirstDto.getFirstCharge());
            mbrRebateFriends.setEventTime(calcRebateFirstDto.getFirstChargeTime());
        }
    }

    //有效下注返利信息
    public void statsValidBet(String calcDay, String actStartTime, String actEndTime, Integer activityId, Map<Integer, Map<Integer, List<MbrRebateFriends>>> mbrRebateFriendsMap, String incomeDay, String siteCode, StringBuilder stringBuilder){
        List<CalcRebateValidBetDto>  friendsRebateValidBet = getFriendsRebateValidBet(calcDay, actStartTime, actEndTime);
        String msg = "[friendRebate_" + siteCode  + "]"+"===================查询下注返利=================== \r\n" + "  查询条件: calcDay = "+ calcDay +" actStartTime=" + actStartTime + " actEndTime=" + actEndTime+ "\r\n" +"  查询结果: "+ friendsRebateValidBet.size()+ "条"+ "\r\n";
        log.info(msg);
        stringBuilder.append(msg);
        stringBuilder.append(" 结果明细:"  + "\r\n" );
        for(int i = 0; i < friendsRebateValidBet.size() ; i ++ ){
            stringBuilder.append("  "+ (i+1) + " " + JSONObject.toJSONString(friendsRebateValidBet.get(i)) + "\r\n");
        }
        for(CalcRebateValidBetDto calcRebateValidBetDto: friendsRebateValidBet ) {
            MbrRebateFriends mbrRebateFriends =  initMbrRebateFriend(mbrRebateFriendsMap, calcRebateValidBetDto.getLoginName(), calcRebateValidBetDto.getAccountId(), calcRebateValidBetDto.getSubLoginName(), calcRebateValidBetDto.getSubAccountId(),activityId , calcRebateValidBetDto.getActLevelId(), incomeDay);
            Integer catId = Constants.depotCatGameTypeMap.get(calcRebateValidBetDto.getGameCategory());
               if(catId == null){
                   continue;
               }
            mbrRebateFriends.setOriginType(catId);
            switch (catId) {
                case ZR:
                   mbrRebateFriends.setAmount(calcRebateValidBetDto.getValidBet());
                   mbrRebateFriends.setType(FriendRebateConstants.ZR.getValue());
                    break;
                case TY:
                    mbrRebateFriends.setAmount(calcRebateValidBetDto.getValidBet());
                    mbrRebateFriends.setType(FriendRebateConstants.TY.getValue());
                    break;
                case DJ:
                    mbrRebateFriends.setAmount(calcRebateValidBetDto.getValidBet());
                    mbrRebateFriends.setType(FriendRebateConstants.DJ.getValue());
                    break;
                case DZ:
                    mbrRebateFriends.setAmount(calcRebateValidBetDto.getValidBet());
                    mbrRebateFriends.setType(FriendRebateConstants.DZ.getValue());
                    break;
                case QP:
                    mbrRebateFriends.setAmount(calcRebateValidBetDto.getValidBet());
                    mbrRebateFriends.setType(FriendRebateConstants.QP.getValue());
                    break;
                case CP:
                    mbrRebateFriends.setAmount(calcRebateValidBetDto.getValidBet());
                    mbrRebateFriends.setType(FriendRebateConstants.CP.getValue());
                    break;
            }
        }
    }

    // 好友VIP升级信息
    public void statsFriendsRebateUpgradeVip(String calcDay, String actStartTime, String actEndTime, Integer activityId, Map<Integer, Map<Integer, List<MbrRebateFriends>>> mbrRebateFriendsMap, String incomeDay, String siteCode, StringBuilder stringBuilder){
        List<CalcRebateUpgradeVipDto> friendsRebateUpgradeVip = getFriendsRebateUpgradeVip(calcDay, actStartTime, actEndTime);
        String msg = "[friendRebate_" + siteCode  + "]"+"===================查询VIP升级=================== \r\n" + "  查询条件: calcDay = "+ calcDay +" actStartTime=" + actStartTime + " actEndTime=" + actEndTime+ "\r\n" +"  查询结果: "+ friendsRebateUpgradeVip.size()+ "条"+ "\r\n";
        log.info(msg);
        stringBuilder.append(msg);
        stringBuilder.append(" 结果明细:"   + "\r\n" );

        for(int i = 0; i < friendsRebateUpgradeVip.size() ; i ++ ){
            stringBuilder.append("  "+ (i+1) + " " + JSONObject.toJSONString(friendsRebateUpgradeVip.get(i)) + "\r\n");
        }
        for(CalcRebateUpgradeVipDto calcRebateUpgradeVipDto: friendsRebateUpgradeVip ){
            String vipUpgradeInfo =  calcRebateUpgradeVipDto.getVipUpgradeInfo();
            if(StringUtil.isEmpty(vipUpgradeInfo)){
                continue;
            }
            AccountLogDto accountLogDto =jsonUtil.fromJson(vipUpgradeInfo, AccountLogDto.class);
            if(accountLogDto == null  ||  StringUtil.isEmpty(accountLogDto.getAfterChange())){
                continue;
            }
            String str = null;
            // 初次筛选
            if("会员列表".equals(calcRebateUpgradeVipDto.getModulename())){
                Pattern p=Pattern.compile("星级:VIP[0-9]*\\s*\\>\\s*VIP[0-9]*");
                Matcher m =p.matcher(accountLogDto.getAfterChange());
                    if(m.find()){
                    str = m.group();
                }
                if(StringUtil.isEmpty(str)){
                    continue;
                }
            }else{
                str = accountLogDto.getAfterChange();
            }
            // 匹配等级
            Pattern p=Pattern.compile("(?<=VIP)[0-9]*");
            Matcher m =p.matcher(str);
            Integer startLevel = (m.find() ? Integer.valueOf(m.group().trim()) : -1);
            Integer endLevel = (m.find() ? Integer.valueOf(m.group().trim()) : -1);

            //匹配升级成功
            if( startLevel > -1 && endLevel > -1){
                Integer subAccountId = calcRebateUpgradeVipDto.getSubAccountId();
                Integer accountId = calcRebateUpgradeVipDto.getAccountId();

                // 玩家当前等级 -- 降级处理
                Integer  curLevel= mbrMapper.findAccountLevel(subAccountId);
                endLevel = endLevel > curLevel ?  curLevel:endLevel;

                if(startLevel >= endLevel){
                    continue;
                }

                Integer maxVip =  mbrMapper.getMbrRebateFriendsMaxVip(accountId, subAccountId, activityId);
                if (maxVip == null || endLevel > maxVip){
                    MbrRebateFriends mbrRebateFriends = initMbrRebateFriend(mbrRebateFriendsMap, calcRebateUpgradeVipDto.getLoginName(), calcRebateUpgradeVipDto.getAccountId(), calcRebateUpgradeVipDto.getSubLoginName(), subAccountId, activityId,  calcRebateUpgradeVipDto.getActLevelId(), incomeDay);
                    mbrRebateFriends.setType(FriendRebateConstants.VIP.getValue());
                    startLevel = maxVip == null ? startLevel : maxVip;
                    mbrRebateFriends.setFromVip(startLevel);
                    mbrRebateFriends.setToVip(endLevel);
                }
            }
        }
    }

    // 累计充值
    public void statsFriendsRebateCharge(String calcDay, String actStartTime, String actEndTime,Integer activityId,  Map<Integer, Map<Integer, List<MbrRebateFriends>>> mbrRebateFriendsMap, String incomeDay, String siteCode, StringBuilder stringBuilder){
        List<CalcRebateChargeDto> friendsRebateCharge = getFriendsRebateCharge(calcDay, actStartTime, actEndTime);
        String msg = "[friendRebate_" + siteCode  + "]"+ "===================查询累计充值统计=================== \r\n" + "  查询条件: calcDay = "+ calcDay +" actStartTime=" + actStartTime + " actEndTime=" + actEndTime+ "\r\n" +"  查询结果: "+ friendsRebateCharge.size()+ "条"+ "\r\n";
        log.info(msg);
        stringBuilder.append(msg);
        stringBuilder.append(" 结果明细:"   + "\r\n" );
        for(int i = 0; i < friendsRebateCharge.size() ; i ++ ){
            stringBuilder.append("  "+ (i+1) + " " + JSONObject.toJSONString(friendsRebateCharge.get(i)) + "\r\n");
        }
        for(CalcRebateChargeDto calcRebateChargeDto: friendsRebateCharge){
            Integer subAccountId =calcRebateChargeDto.getSubAccountId();
            MbrRebateFriends mbrRebateFriends = initMbrRebateFriend(mbrRebateFriendsMap, calcRebateChargeDto.getLoginName(), calcRebateChargeDto.getAccountId(), calcRebateChargeDto.getSubLoginName(), subAccountId, activityId ,calcRebateChargeDto.getActLevelId(), incomeDay);
            mbrRebateFriends.setType(FriendRebateConstants.TD.getValue());
            mbrRebateFriends.setAmount(calcRebateChargeDto.getTotalDeposit());
        }
    }


    public void calcRebateVipUpgradeReward(Map<Integer, RebateVipDto> rebateVipDtoMap, MbrRebateFriends mbrRebateFriends, MbrRebateFriendsReward mbrRebateFriendsReward){
            for(int i = mbrRebateFriends.getFromVip() + 1; i <= mbrRebateFriends.getToVip(); i ++){
                RebateVipDto rebateVipDto = rebateVipDtoMap.get(i);
                mbrRebateFriends.setReward(mbrRebateFriends.getReward().add(rebateVipDto.getAward()));
                mbrRebateFriendsReward.setReward(mbrRebateFriendsReward.getReward().add(rebateVipDto.getAward()));
            }
    }

    public void calcRebateFirstChargeReward(RebateFirstChargeDto rebateFirstChargeDto,MbrRebateFriends mbrRebateFriends, MbrRebateFriendsReward mbrRebateFriendsReward, MbrRebateFriendsReward mbrRebateFriendsReward2, MbrRebateFriendsReward mbrRebateFriendsReward3){
            if(mbrRebateFriends.getAmount().compareTo(rebateFirstChargeDto.getMinCharge()) >= 0 &&  mbrRebateFriends.getAmount().compareTo(rebateFirstChargeDto.getMaxCharge()) <=0){
                BigDecimal referrerReward =   mbrRebateFriends.getAmount().multiply(rebateFirstChargeDto.getReferrer()).divide(new BigDecimal(100));
                referrerReward = referrerReward.setScale(2, BigDecimal.ROUND_DOWN);
                mbrRebateFriends.setReward(referrerReward);
                mbrRebateFriendsReward.setReward(mbrRebateFriendsReward.getReward().add(referrerReward));

                BigDecimal refereeReward =   mbrRebateFriends.getAmount().multiply(rebateFirstChargeDto.getReferee()).divide(new BigDecimal(100));
                mbrRebateFriendsReward2.setReward(mbrRebateFriendsReward2.getReward().add(refereeReward));

                mbrRebateFriendsReward3.setReward(mbrRebateFriendsReward3.getReward().add(refereeReward));
            }
    }

    public void calcRebateValidBetReward(Map<Integer, RebateCatDto> rebateCatDtoMap, MbrRebateFriends mbrRebateFriends, MbrRebateFriendsReward mbrRebateFriendsReward, Integer flagType){
        RebateCatDto rebateCatDto  = rebateCatDtoMap.get(flagType);
        if(rebateCatDto != null){
            if(mbrRebateFriends.getAmount().compareTo(rebateCatDto.getValidBet()) >= 0){
                BigDecimal validBetReward =  mbrRebateFriends.getAmount().multiply(rebateCatDto.getTopRebate()).divide(new BigDecimal(100));
                validBetReward = validBetReward.setScale(2, BigDecimal.ROUND_DOWN);
                mbrRebateFriends.setReward(validBetReward);
                mbrRebateFriendsReward.setReward(mbrRebateFriendsReward.getReward().add(mbrRebateFriends.getReward()));
            }
        }
    }

    // 计算充值奖励
    public Object[] calcRebateChargeReward(Integer accountId, String startTime, String endTime, Integer activityId,String chargeDay,  List<RebateChargeDto> rebateChargeDtoList){

        Map<String, Object> friendRebateChargeCountMap =  getFriendRebateChargeCount(accountId, startTime,endTime, chargeDay,rebateChargeDtoList);

        List<RebateChargeDto> list = new ArrayList<>();

        if(friendRebateChargeCountMap == null || friendRebateChargeCountMap.size() == 0){
            return new Object[]{list, null, null};
        }
        List<MbrRebateFriendsReward> mbrRebateFriendsRewardList =  mbrMapper.getMbrRebateFriendsReward(accountId,FriendRebateConstants.CHARGE.getValue(), activityId );
        Map<String, Boolean>  map = new HashMap<>();
        for(MbrRebateFriendsReward reward : mbrRebateFriendsRewardList){
            if(reward.getContent().indexOf(",") > 0){
                String[] keys =  reward.getContent().split(",");
                for(int i = 0; i < keys.length; i ++){
                    map.put(keys[i], Boolean.TRUE);
                }
            }else{
                map.put(reward.getContent(),Boolean.TRUE);
            }
        }


        for(RebateChargeDto rebateChargeDto : rebateChargeDtoList){
            Long num = (Long) friendRebateChargeCountMap.get(rebateChargeDto.getName());
            String key = rebateChargeDto.getNum()+"_"+ rebateChargeDto.getMinCharge();
            Boolean b = map.get(key);
            if(b == null){
                if(num >= rebateChargeDto.getNum()){
                    list.add(rebateChargeDto);
                }
            }
        }
        return new Object[]{list, friendRebateChargeCountMap.get("loginName").toString(), friendRebateChargeCountMap};
    }

    // 计算发放返利
    public Map<Integer,Map<Integer, List<MbrRebateFriendsReward>>> calcRebateReward(Map<Integer, Map<Integer, List<MbrRebateFriends>>>mbrRebateFriendsMap , Map<Integer, RebateLevelDto> levelDtoMap, String startTime, String endTime, Integer activityId, String incomeDay,  Integer startVipLevel, Integer endVipLevel,String siteCode, StringBuilder stringBuilder){
        // 发放奖励结果缓存
        Map<Integer,Map<Integer, List<MbrRebateFriendsReward>>>  mbrRebateFriendsRewardMap = new HashMap<>();
        Map<Integer, MbrActivityLevel> mbrActivityLevelMap = getIntegerMbrActivityLevelMap();
        if(mbrRebateFriendsMap!= null && mbrRebateFriendsMap.size() > 0){
            for(Map.Entry<Integer, Map<Integer, List<MbrRebateFriends>>> entryMap: mbrRebateFriendsMap.entrySet()){
                Map<Integer, List<MbrRebateFriends>> map = entryMap.getValue();
                Integer accountId = entryMap.getKey();
                MbrAccount  supAccount = mbrMapper.findMbrLevelAndAgyInfoById(accountId);

                Integer accountLevel = supAccount.getAccountLevel();
                // 判断玩家VIP等级是否可以参与计算奖励
                if(accountLevel< startVipLevel || accountLevel > endVipLevel){
                    continue;
                }

                if(oprActActivityService.isBlackList(supAccount.getId(), TOpActtmpl.mbrRebateCode)||oprActActivityService.isBlackList(supAccount.getId(), TOpActtmpl.allActivityCode)){
                    log.info(supAccount.getLoginName()+"是好友返利活动黑名单会员");
                    continue;
                }

                //判断是否存在vip活动代理黑名单
                if (oprActActivityService.valAgentBackList(supAccount,TOpActtmpl.mbrRebateCode)){
                    log.info(supAccount.getLoginName()+"上级代理存在好友返利活动黑名单");
                    continue;
                }
                //判断是否存在所有活动黑名单
                if (oprActActivityService.valAgentBackList(supAccount,TOpActtmpl.allActivityCode)){
                    log.info(supAccount.getLoginName()+"上级代理存在所有活动黑名单");
                    continue;
                }

                MbrActivityLevel mbrActivityLevel = mbrActivityLevelMap.get(accountLevel);
                RebateLevelDto rebateLevelDto = levelDtoMap.get(mbrActivityLevel.getAccountLevel());
                String msgStart ="[friendRebate_" + siteCode  + "]"+ "===================开始计算：accountId = "+ accountId +  " 玩家VIP等级 = " + mbrActivityLevel.getAccountLevel()+ " 玩家奖励配置:" + JSONObject.toJSONString(rebateLevelDto) + "玩家奖励=================== \r\n";
                log.info(msgStart);
                stringBuilder.append(msgStart);

                Map<Integer, List<MbrRebateFriendsReward>> rewardMap = getIntegerMbrRebateFriendsRewardMap(mbrRebateFriendsRewardMap, accountId);
                for (Map.Entry<Integer, List<MbrRebateFriends>> entry: map.entrySet()){
                    for(MbrRebateFriends mbrRebateFriends : entry.getValue()){
                        String loginName = mbrRebateFriends.getLoginName();
                        Integer subAccountId = mbrRebateFriends.getSubAccountId();
                        int type = mbrRebateFriends.getType();

                        // 累计充值没有奖励 跳过计算
                        if(type == FriendRebateConstants.TD.getValue()){
                            continue;
                        }
                        int parentType = type;
                        // 子类有效下注奖励汇总成 有效下注
                        if(type == FriendRebateConstants.TY.getValue() || type == FriendRebateConstants.DZ.getValue() || type == FriendRebateConstants.DJ.getValue() || type == FriendRebateConstants.QP.getValue() || type == FriendRebateConstants.CP.getValue() || type == FriendRebateConstants.ZR.getValue()){
                            parentType = FriendRebateConstants.VB.getValue();
                        }
                        MbrRebateFriendsReward mbrRebateFriendsReward = getMbrRebateFriendsReward(activityId, accountId, loginName, rewardMap, parentType, incomeDay);

                        String calcBeforeReward = mbrRebateFriendsReward.toString();
                        String calcInfoBefore = mbrRebateFriends.toString();

                        if(type == FriendRebateConstants.FC.getValue()){
                            if (rebateLevelDto!= null && !isNull(rebateLevelDto.getRebateFirstChargeDto())) {
                                // 计算首充
                                MbrRebateFriendsReward mbrRebateFriendsReward2 = getMbrRebateFriendsReward(activityId, mbrRebateFriends.getAccountId(), mbrRebateFriends.getLoginName(), rewardMap, FriendRebateConstants.FRIEND.getValue(), incomeDay);

                                Map<Integer, List<MbrRebateFriendsReward>> rewardMap2 = getIntegerMbrRebateFriendsRewardMap(mbrRebateFriendsRewardMap, mbrRebateFriends.getSubAccountId());
                                MbrRebateFriendsReward mbrRebateFriendsReward3 = getMbrRebateFriendsReward(activityId, mbrRebateFriends.getSubAccountId(), mbrRebateFriends.getSubLoginName(), rewardMap2, FriendRebateConstants.FC.getValue(), incomeDay);

                                String selfRewardBefore = mbrRebateFriendsReward2.toString();
                                calcRebateFirstChargeReward(rebateLevelDto.getRebateFirstChargeDto(), mbrRebateFriends, mbrRebateFriendsReward, mbrRebateFriendsReward2, mbrRebateFriendsReward3);
                                String msg = "[friendRebate_" + siteCode  + "]"+ "  计算玩家首充 "+subAccountId+ "\r\n" +"  计算前-> 自身奖励 = " +calcBeforeReward + " 好友奖励:" + selfRewardBefore + "\r\n"  + "  首充条件奖励条件：" + JSONObject.toJSONString(rebateLevelDto.getRebateFirstChargeDto()) + "\r\n"  +"  玩家奖励信息：" +calcInfoBefore + "\r\n"  +  "  计算后-> 自身奖励:" + mbrRebateFriendsReward.toString() +" 好友奖励:" + mbrRebateFriendsReward2.toString() +"\r\n" +"\r\n";
                                log.info(msg);
                                stringBuilder.append(msg);
                            }else {
                                mbrRebateFriends.setReward(BigDecimal.ZERO);
                            }
                        }else if(type == FriendRebateConstants.VIP.getValue()){

                            if(rebateLevelDto!= null && rebateLevelDto.getRebateVipDtoMap()!= null && rebateLevelDto.getRebateVipDtoMap().size() > 0){
                                // 计算VIP奖励奖励
                                calcRebateVipUpgradeReward(rebateLevelDto.getRebateVipDtoMap(), mbrRebateFriends, mbrRebateFriendsReward);
                                String msg = "[friendRebate_" + siteCode  + "]"+ "  计算玩家VIP "+subAccountId+ "\r\n" +"  计算前-> 自身奖励 = " +calcBeforeReward + "\r\n"  +"  VIP条件奖励条件：" + JSONObject.toJSONString(rebateLevelDto.getRebateVipDtoMap()) +"\r\n"  +"  玩家奖励信息：" +calcInfoBefore + "\r\n"  +  "  计算后-> 自身奖励:" + mbrRebateFriendsReward.toString() +"\r\n" +"\r\n";
                                log.info(msg);
                                stringBuilder.append(msg);
                            }else {
                                mbrRebateFriends.setReward(BigDecimal.ZERO);
                            }

                        }else if(type == FriendRebateConstants.TY.getValue() || type == FriendRebateConstants.DZ.getValue() || type == FriendRebateConstants.DJ.getValue() || type == FriendRebateConstants.QP.getValue() || type == FriendRebateConstants.CP.getValue()|| type == FriendRebateConstants.ZR.getValue()){
                            // 计算有效下注

                            if(rebateLevelDto!= null && rebateLevelDto.getCatDtoMap()!= null && rebateLevelDto.getCatDtoMap().size() > 0){
                                calcRebateValidBetReward(rebateLevelDto.getCatDtoMap(), mbrRebateFriends, mbrRebateFriendsReward, mbrRebateFriends.getOriginType());
                                String msg = "[friendRebate_" + siteCode  + "]"+  "  计算有效下注 "+subAccountId+ "\r\n" +"  计算前-> 自身奖励 = " +calcBeforeReward + "\r\n"  + "  有效下注奖励条件：" + JSONObject.toJSONString(rebateLevelDto.getCatDtoMap().get(mbrRebateFriends.getOriginType())) +"\r\n"  +"  玩家奖励信息：" +calcInfoBefore + "\r\n"  +  "  计算后-> 自身奖励:" + mbrRebateFriendsReward.toString() +"\r\n" +"\r\n";
                                log.info(msg);
                                stringBuilder.append(msg);
                            }else {
                                mbrRebateFriends.setReward(BigDecimal.ZERO);
                            }
                        }
                    }
                }
            }
        }

        // 充值奖励计算
        List<Integer> accountIds = mbrMapper.getFriendRebateAccountIdList(incomeDay, startTime, endTime);
        if(accountIds != null){
            for (Integer accountId :  accountIds){
                MbrAccount  supAccount = mbrMapper.findMbrLevelAndAgyInfoById(accountId);
                Integer accountLevel = supAccount.getAccountLevel();
                // 判断玩家VIP等级是否可以参与计算奖励
                if(accountLevel< startVipLevel || accountLevel > endVipLevel){
                    continue;
                }

                if(oprActActivityService.isBlackList(supAccount.getId(), TOpActtmpl.mbrRebateCode)||oprActActivityService.isBlackList(supAccount.getId(), TOpActtmpl.allActivityCode)){
                    log.info(supAccount.getLoginName()+"是好友返利活动黑名单会员");
                    continue;
                }

                //判断是否存在vip活动代理黑名单
                if (oprActActivityService.valAgentBackList(supAccount,TOpActtmpl.mbrRebateCode)){
                    log.info(supAccount.getLoginName()+"上级代理存在好友返利活动黑名单");
                    continue;
                }
                //判断是否存在所有活动黑名单
                if (oprActActivityService.valAgentBackList(supAccount,TOpActtmpl.allActivityCode)){
                    log.info(supAccount.getLoginName()+"上级代理存在所有活动黑名单");
                    continue;
                }

                MbrActivityLevel mbrActivityLevel = mbrActivityLevelMap.get(accountLevel);
                RebateLevelDto rebateLevelDto = levelDtoMap.get(mbrActivityLevel.getAccountLevel());

                String chargeDay = String.format("%s %s", incomeDay, "23:59:59");

                if(rebateLevelDto.getRebateChargeDtoList() != null && rebateLevelDto.getRebateChargeDtoList().size() >0){
                    Object[] rt = calcRebateChargeReward(accountId ,startTime, endTime ,activityId, chargeDay, rebateLevelDto.getRebateChargeDtoList());
                    List<RebateChargeDto> rebateChargeDtoList = (List<RebateChargeDto>) rt[0];
                    if(rebateChargeDtoList.size() > 0){
                        // 计算充值奖励
                        for(RebateChargeDto rebateChargeDto : rebateChargeDtoList){

                            // 邀请数量大于0才参与计算
                            if(rebateChargeDto.getNum() > 0){
                                MbrRebateFriendsReward mbrRebateFriendsReward = getMbrRebateFriendsReward(activityId, accountId, (String)rt[1], mbrRebateFriendsRewardMap.get(accountId), FriendRebateConstants.CHARGE.getValue(), incomeDay);
                                String calcBeforeReward = mbrRebateFriendsReward.toString();
                                StringBuilder builder = new StringBuilder();
                                builder.append(rebateChargeDto.getNum());
                                builder.append("_");
                                builder.append(rebateChargeDto.getMinCharge());
                                mbrRebateFriendsReward.setContent(builder.toString());
                                mbrRebateFriendsReward.setReward(rebateChargeDto.getAward());
                                mbrRebateFriendsReward.setAuditMultiple(rebateChargeDto.getMultiple());
                                String msg = "[friendRebate_" + siteCode  + "]"+ "  计算充值 "+accountId+ "\r\n" +"  计算前-> 自身奖励 = " +calcBeforeReward + "\r\n"  + "  计算奖励条件：" + JSONObject.toJSONString(rt[2]) +"\r\n"    +  "  计算后-> 自身奖励:" + mbrRebateFriendsReward.toString() +"\r\n" +"\r\n";
                                log.info(msg);
                                stringBuilder.append(msg);
                            }
                        }
                    }
                }
            }
        }

        return mbrRebateFriendsRewardMap;
    }

    private Map<Integer, List<MbrRebateFriendsReward>> getIntegerMbrRebateFriendsRewardMap(Map<Integer, Map<Integer, List<MbrRebateFriendsReward>>> mbrRebateFriendsRewardMap, Integer accountId) {
        Map<Integer, List<MbrRebateFriendsReward>> rewardMap = mbrRebateFriendsRewardMap.get(accountId);
        if(rewardMap == null){
            rewardMap = new HashMap<>();
            mbrRebateFriendsRewardMap.put(accountId, rewardMap);
        }
        return rewardMap;
    }


    public void statsFriends(Integer activityId,  Map<Integer,Map<Integer, List<MbrRebateFriendsReward>>> mbrRebateFriendsRewardMap, String friendCalcDay,  String  incomeDay, StringBuilder builder){
        builder.append( "\r\n" + "===================开始统计好友===================" + "\r\n");
        int i = 1;
        for(Map.Entry<Integer,Map<Integer, List<MbrRebateFriendsReward>>> entry : mbrRebateFriendsRewardMap.entrySet()){
            Integer accountId = entry.getKey();
            Map map = getFriendRebateCount(friendCalcDay, accountId);
            Long num = (Long)map.get("num");
            String loginName = (String) map.get("loginName");
            if(num > 0) {
                builder.append( " " + i + " 玩家ID:"+ accountId + " 好友数: " + num + "\r\n");
                i++ ;
                MbrRebateFriendsReward mbrRebateFriendsReward = getMbrRebateFriendsReward(activityId, accountId, loginName, mbrRebateFriendsRewardMap.get(accountId), FriendRebateConstants.NUM.getValue() , incomeDay);
                mbrRebateFriendsReward.setInviteNum(num.intValue());
            }
        }
    }

    private MbrRebateFriendsReward getMbrRebateFriendsReward(Integer activityId, Integer accountId, String loginName,  Map<Integer, List<MbrRebateFriendsReward>> rewardMap, int type, String incomeDay) {
        List<MbrRebateFriendsReward> mbrRebateFriendsRewardList  = rewardMap.get(type);
        if(mbrRebateFriendsRewardList == null){
            mbrRebateFriendsRewardList = new ArrayList<>();
            rewardMap.put(type, mbrRebateFriendsRewardList);
        }

        MbrRebateFriendsReward mbrRebateFriendsReward = null;
        if(type == FriendRebateConstants.CHARGE.getValue()){
            mbrRebateFriendsReward = getNewRebateFriendsReward(activityId, accountId, loginName, type, incomeDay);
            mbrRebateFriendsRewardList.add(mbrRebateFriendsReward);
        }else{
             if(mbrRebateFriendsRewardList.size() > 0 ){
                 mbrRebateFriendsReward =  mbrRebateFriendsRewardList.get(0);
             }
            if(mbrRebateFriendsReward == null){
                mbrRebateFriendsReward = getNewRebateFriendsReward(activityId, accountId, loginName, type, incomeDay);
                mbrRebateFriendsRewardList.add(mbrRebateFriendsReward);
            }
        }
        return mbrRebateFriendsReward;
    }

    private MbrRebateFriendsReward getNewRebateFriendsReward(Integer activityId, Integer accountId, String loginName, int type, String incomeDay) {
        MbrRebateFriendsReward mbrRebateFriendsReward;
        mbrRebateFriendsReward = new MbrRebateFriendsReward();
        mbrRebateFriendsReward.setType(type);
        mbrRebateFriendsReward.setAccountId(accountId);
        mbrRebateFriendsReward.setLoginName(loginName);
        mbrRebateFriendsReward.setStatus(1);
        mbrRebateFriendsReward.setReward(BigDecimal.ZERO);
        mbrRebateFriendsReward.setOrderNo(String.valueOf(new SnowFlake().nextId()));
        mbrRebateFriendsReward.setIncomeTime(incomeDay);
        mbrRebateFriendsReward.setCreateTime( DateUtil.format(new Date(), FORMAT_18_DATE_TIME));

        mbrRebateFriendsReward.setActivityId(activityId);
        mbrRebateFriendsReward.setOperationType(0);
        mbrRebateFriendsReward.setCreater(Constants.SYSTEM_USER);
        mbrRebateFriendsReward.setAuditMultiple(BigDecimal.ZERO);

        return mbrRebateFriendsReward;
    }


    public void saveFriendRebate(Map<Integer, Map<Integer, List<MbrRebateFriends>>>  mbrRebateFriendsMap, String calcDay){
            List<MbrRebateFriends> list = new ArrayList<>();
            for(Map.Entry<Integer, Map<Integer, List<MbrRebateFriends>>>  entryMap: mbrRebateFriendsMap.entrySet()){
                for(Map.Entry<Integer, List<MbrRebateFriends>>  entry: entryMap.getValue().entrySet()){
                    List<MbrRebateFriends> mbrRebateFriendsList = entry.getValue();
                    for(MbrRebateFriends mbrRebateFriends : mbrRebateFriendsList){
                        Integer type = mbrRebateFriends.getType();
                        if(type == FriendRebateConstants.VIP.getValue() || type == FriendRebateConstants.FC.getValue() ||  type == FriendRebateConstants.CHARGE.getValue() ||
                                type == FriendRebateConstants.DJ.getValue()  || type == FriendRebateConstants.ZR.getValue() || type == FriendRebateConstants.DZ.getValue()  || type == FriendRebateConstants.QP.getValue() || type == FriendRebateConstants.CP.getValue() || type == FriendRebateConstants.TY.getValue()){
                            // 大于0才保存
                            if(mbrRebateFriends.getReward().compareTo(BigDecimal.ZERO) != 1){
                                    continue;
                            }
                        }
                        Integer count =  mbrRebateFriendsMapper.getMbrRebateFriendsCount(mbrRebateFriends.getSubAccountId(), mbrRebateFriends.getActivityId(), calcDay, type);
                        if(count <= 0 ){
                            list.add(mbrRebateFriends);
                        }
                    }
                }
            }

        // 批量保存
            if(list.size() > 0){
                int pageSize = 1000;
                List<MbrRebateFriends> saveList = new ArrayList<>();
                for(int i = 1; i <= list.size(); i ++){
                    if(i % pageSize == 0){
                        mbrRebateFriendsMapper.insertList(saveList);
                        saveList.clear();
                    }else{
                        saveList.add(list.get(i - 1));
                    }
                }
                if(saveList.size() > 0){
                    mbrRebateFriendsMapper.insertList(saveList);
                }
            }
    }

    public void saveAndGiveOutFriendRebateReward(Map<Integer, Map<Integer, List<MbrRebateFriendsReward>>> mbrRebateFriendsRewardMap, RebateDto rebateDto, String calcDay, String siteCode){
        List<MbrRebateFriendsReward> mbrRebateFriendsRewardList = new ArrayList<>();
        for(Map.Entry<Integer, Map<Integer, List<MbrRebateFriendsReward>>> entry : mbrRebateFriendsRewardMap.entrySet()){
            for (Map.Entry<Integer, List<MbrRebateFriendsReward>> en: entry.getValue().entrySet()){
                List<MbrRebateFriendsReward> list  = en.getValue();
                for (MbrRebateFriendsReward mbrRebateFriendsReward : list ) {
                    Integer type = mbrRebateFriendsReward.getType();
                    Integer count;
                    if(type == FriendRebateConstants.CHARGE.getValue()){
                        count =  mbrRebateFriendsRewardMapper.getMbrRebateFriendsRewardCount(mbrRebateFriendsReward.getAccountId(), mbrRebateFriendsReward.getActivityId(), null, type, mbrRebateFriendsReward.getContent());
                    }else{
                        count = mbrRebateFriendsRewardMapper.getMbrRebateFriendsRewardCount(mbrRebateFriendsReward.getAccountId(), mbrRebateFriendsReward.getActivityId(), calcDay, type, mbrRebateFriendsReward.getContent());
                    }
                     //查询是否有发奖
                    if(count <= 0 ){
                        MbrBillDetail mbrBillDetail = null;
                        if(type == FriendRebateConstants.CHARGE.getValue() || type == FriendRebateConstants.VB.getValue() || type == FriendRebateConstants.VIP.getValue() || type == FriendRebateConstants.FC.getValue() ){
                            //判断奖励大于才保存
                            if(mbrRebateFriendsReward.getReward().compareTo(BigDecimal.ZERO) ==1){
                                BigDecimal auditMultiple = null;
                                if(type == FriendRebateConstants.CHARGE.getValue() ){
                                    auditMultiple = mbrRebateFriendsReward.getAuditMultiple();
                                }else{
                                    Integer am = rebateDto.getAuditMultiple();
                                    auditMultiple = new BigDecimal(am == null ? 0 : am) ;
                                }

                                 String msg = String.format( "[friendRebate_%s] 发放[type=%d]类型奖励金额[%.2f]给玩家[accountId=%s,loginName=%s] %d倍稽核" ,siteCode, mbrRebateFriendsReward.getType(), mbrRebateFriendsReward.getReward().doubleValue(), mbrRebateFriendsReward.getAccountId(), mbrRebateFriendsReward.getLoginName(), auditMultiple.intValue());
                                 log.info(msg);
                                 mbrBillDetail =  giveOutFriendRebateReward(mbrRebateFriendsReward.getReward(), mbrRebateFriendsReward.getAccountId(), mbrRebateFriendsReward.getLoginName(), auditMultiple);
                                if(mbrBillDetail != null){
                                    mbrRebateFriendsReward.setBillDetailId(mbrBillDetail.getId());
                                }
                                mbrRebateFriendsReward.setGiveOutTime(DateUtil.format(new Date(), FORMAT_18_DATE_TIME));
                                mbrRebateFriendsRewardList.add(mbrRebateFriendsReward);
                            }
                        }else{
                            mbrRebateFriendsRewardList.add(mbrRebateFriendsReward);
                        }

                    }
                }
            }
        }

        if(mbrRebateFriendsRewardList.size() > 0){
            int pageSize = 1000;
            List<MbrRebateFriendsReward> saveList = new ArrayList<>();
            for(int i = 1; i <= mbrRebateFriendsRewardList.size(); i ++){
                if(i % pageSize == 0){
                    mbrRebateFriendsRewardMapper.insertList(saveList);
                    saveList.clear();
                }else{
                    saveList.add(mbrRebateFriendsRewardList.get(i - 1));
                }
            }
            if(saveList.size() > 0){
                mbrRebateFriendsRewardMapper.insertList(saveList);
            }
        }
    }

    private MbrBillDetail giveOutFriendRebateReward(BigDecimal amount, Integer accountId, String loginName ,BigDecimal auditMultiple){
        MbrBillDetail mbrBillDetail =  walletService.castWalletAndBillDetail(loginName, accountId, OrderConstants.ACCOUNT_REBATE_FA, amount, String.valueOf(new SnowFlake().nextId()), Boolean.TRUE,null,null);
        if (auditMultiple.intValue() > Constants.EVNumber.zero ) {
            auditAccountService.insertAccountAudit(accountId, amount, null, auditMultiple, null, null, null, Constants.EVNumber.seven);
        }
        return mbrBillDetail;
    }



    private Map<Integer, MbrActivityLevel> getIntegerMbrActivityLevelMap() {
        List<MbrActivityLevel> mbrActivityLevelList = mbrActivityLevelMapper.selectAll();
        Map<Integer, MbrActivityLevel>  mbrActivityLevelMap = new HashMap<>();

        for(MbrActivityLevel mbrActivityLevel : mbrActivityLevelList){
            mbrActivityLevelMap.put(mbrActivityLevel.getAccountLevel(), mbrActivityLevel);
        }
        return mbrActivityLevelMap;
    }

    private MbrRebateFriends initMbrRebateFriend(Map<Integer, Map<Integer, List<MbrRebateFriends>>> mbrRebateFriendsMap, String loginName, Integer accountId,  String subLoginName, Integer subAccountId,Integer activityId, Integer activityLevelId, String incomeDay) {
        Map<Integer, List<MbrRebateFriends>>  map = mbrRebateFriendsMap.get(accountId);
        if(map == null){
            map = new HashMap<>();
            mbrRebateFriendsMap.put(accountId, map);
        }
        List<MbrRebateFriends> mbrRebateFriendsList = map.get(subAccountId);
        if(mbrRebateFriendsList == null ){
            mbrRebateFriendsList = new ArrayList<>();
            map.put(subAccountId, mbrRebateFriendsList);
        }
        MbrRebateFriends mbrRebateFriends = new MbrRebateFriends();
        mbrRebateFriends.setSubAccountId(subAccountId);
        mbrRebateFriends.setSubLoginName(subLoginName);
        mbrRebateFriends.setLoginName(loginName);
        mbrRebateFriends.setAccountId(accountId);
        mbrRebateFriends.setReward(BigDecimal.ZERO);
        mbrRebateFriends.setIncomeTime(incomeDay);
        mbrRebateFriends.setCreateTime(DateUtil.format(new Date(), FORMAT_18_DATE_TIME));
        mbrRebateFriends.setActivityId(activityId);
        mbrRebateFriends.setAmount(BigDecimal.ZERO);
        mbrRebateFriends.setOperationType(0);
        mbrRebateFriendsList.add(mbrRebateFriends);
        return mbrRebateFriends;
    }



    public String friendRebate(String siteCode, String clacDay){
        try {
            return doFriendRebate(siteCode, clacDay);
        }catch (Exception e){
            log.error("好友推荐计算异常【friendRebate_" + siteCode + "】:" + e.getMessage(), e);
        }
        return null;
    }

    private String doFriendRebate(String siteCode, String clacDay){
        log.info("好友推荐计算开始【friendRebate_" + siteCode + "】");

        StringBuilder builder = new StringBuilder(siteCode);
        builder.append("==站点计算日志=="+"\r\n");

            // 判断是否有有可用的返利活动
            OprActActivity actActivity = oprActActivityCastService.getRebateAct();
            if (isNull(actActivity) || StringUtil.isEmpty(actActivity.getRule())) {
                log.info("好友推荐活动不存在【friendRebate_" + siteCode + "】");
                return "活动不存在";
            }

            Calendar cal = Calendar.getInstance();
            if(StringUtil.isNotEmpty(clacDay)){
                String[] date = clacDay.split("-");
                cal.set(Integer.valueOf(date[0]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));
            }

            //缓存奖励信息
            Map<Integer, Map<Integer, List<MbrRebateFriends>>> mbrRebateFriendsMap = new HashMap<>();

            String friendCalcDay = DateUtil.format(cal.getTime(), DateUtil.FORMAT_10_DATE);

            cal.add(Calendar.DATE, -1);
            String validBetCalcDay = DateUtil.format(cal.getTime(), DateUtil.FORMAT_10_DATE);
            String yesterday = DateUtil.format(cal.getTime(), DateUtil.FORMAT_18_DATE_TIME);
            String incomeDay = DateUtil.format(cal.getTime(), FORMAT_10_DATE);

            // 判断当日是否计算过返利
            //int count = rebateCastNewService.isCastFriendRebate(incomeDay, actActivity.getId());
            //if (count > 0) {
            //    return "当天奖励已计算过";
            // }
            // 获得返利规则
            RebateDto rebateDto = jsonUtil.fromJson(actActivity.getRule(), RebateDto.class);
            if (isNull(rebateDto)) {
                log.info("好友推荐配置解析错误【friendRebate_" + siteCode + "】");
                return "配置不存在";
            }

            String starTime = rebateDto.getStartTime();
            String endTime = rebateDto.getEndTime();


            //统计首充奖励信息
            this.statsFirstCharge(yesterday, starTime, endTime, actActivity.getId(), mbrRebateFriendsMap, incomeDay, siteCode, builder);

            //统计有效下注奖励信息
            this.statsValidBet(validBetCalcDay, starTime, endTime, actActivity.getId(), mbrRebateFriendsMap,incomeDay,  siteCode, builder);

            // 计算VIP升级奖励信息
            this.statsFriendsRebateUpgradeVip(yesterday, starTime, endTime, actActivity.getId(), mbrRebateFriendsMap, incomeDay, siteCode, builder);

            // 累计充值奖励信息
            this.statsFriendsRebateCharge(yesterday, starTime, endTime, actActivity.getId(), mbrRebateFriendsMap,incomeDay , siteCode, builder);

            // MAP缓存缓存返利规则
            rebateDto.toMap();
            Map<Integer, Map<Integer, List<MbrRebateFriendsReward>>> mbrRebateFriendsRewardMap = this.calcRebateReward(mbrRebateFriendsMap, rebateDto.getLevelDtoMap(), starTime, endTime, actActivity.getId(), incomeDay,  rebateDto.getMinVipLevel(), rebateDto.getMaxVipLevel(), siteCode,  builder);

            // 统计好友数量
            this.statsFriends(actActivity.getId(), mbrRebateFriendsRewardMap, friendCalcDay, incomeDay, builder);

            saveAndGiveOut(siteCode, mbrRebateFriendsMap, incomeDay, rebateDto, mbrRebateFriendsRewardMap);


        log.info("结束好友返利计算【friendRebate_" + siteCode + "】");
        return builder.toString();
    }


    @Transactional
    private void saveAndGiveOut(String siteCode, Map<Integer, Map<Integer, List<MbrRebateFriends>>> mbrRebateFriendsMap, String incomeDay, RebateDto rebateDto, Map<Integer, Map<Integer, List<MbrRebateFriendsReward>>> mbrRebateFriendsRewardMap) {
        // 保存奖励统计信息
        this.saveFriendRebate(mbrRebateFriendsMap, incomeDay);

        // 奖励记录和发放奖励
        this.saveAndGiveOutFriendRebateReward(mbrRebateFriendsRewardMap, rebateDto, incomeDay, siteCode);
    }


}
