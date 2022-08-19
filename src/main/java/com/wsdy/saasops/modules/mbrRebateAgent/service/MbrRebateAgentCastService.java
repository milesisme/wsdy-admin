package com.wsdy.saasops.modules.mbrRebateAgent.service;

import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.mbrRebateAgent.dto.*;
import com.wsdy.saasops.modules.mbrRebateAgent.entity.MbrRebateAgentBonus;
import com.wsdy.saasops.modules.mbrRebateAgent.entity.MbrRebateAgentDay;
import com.wsdy.saasops.modules.mbrRebateAgent.entity.MbrRebateAgentMonth;
import com.wsdy.saasops.modules.mbrRebateAgent.mapper.MbrRebateAgentBonusMapper;
import com.wsdy.saasops.modules.mbrRebateAgent.mapper.MbrRebateAgentDayMapper;
import com.wsdy.saasops.modules.mbrRebateAgent.mapper.MbrRebateAgentMapper;
import com.wsdy.saasops.modules.mbrRebateAgent.mapper.MbrRebateAgentMonthMapper;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.constants.Constants.ONE_HUNDRED;
import static com.wsdy.saasops.common.utils.DateUtil.*;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.mbrRebateAgentCode;
import static java.util.Objects.isNull;


@Slf4j
@Service
public class MbrRebateAgentCastService {
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private MbrRebateAgentMapper mbrRebateAgentMapper;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private OperateActivityMapper operateActivityMapper;
    @Autowired
    private MbrRebateAgentDayMapper mbrRebateAgentDayMapper;
    @Autowired
    private MbrRebateAgentMonthMapper mbrRebateAgentMonthMapper;
    @Autowired
    private MbrRebateAgentBonusMapper mbrRebateAgentBonusMapper;
    @Autowired
    private RedisService redisService;

    /**
     *  日表-计算
     * @param siteCode
     */
    public void mbrRebateAgentCast(String siteCode) {
        log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==start");

        // 判断是否有有可用的全民代理活动
        OprActActivity actActivity = getRebateAct();
        if (isNull(actActivity) || StringUtil.isEmpty(actActivity.getRule())) {
            log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==无活动");
            return;
        }
        // 获得规则
        MbrRebateAgentRuleDto rebateDto = jsonUtil.fromJson(actActivity.getRule(), MbrRebateAgentRuleDto.class);
        if (Objects.isNull(rebateDto) || Objects.isNull(rebateDto.getRuleScopeDtos())) {
            log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==无rebateDto");
            return;
        }
        // 获得层级规则
        List<MbrRebateAgentRuleLevelDto> ruleScopeDtos = rebateDto.getRuleScopeDtos();
        if(Objects.isNull(ruleScopeDtos) || ruleScopeDtos.size() == Constants.EVNumber.zero){
            log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==无层级规则");
            return;
        }

        // 查询计算深度
        SysSetting sysSetting = sysSettingService.getSysSetting(SystemConstants.REBATE_CAST_DEPTH_AGENT);
        if(Objects.isNull(sysSetting) || Objects.isNull(sysSetting.getSysvalue())){
            log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==未配置计算深度");
            return;
        }
        Integer rebateCastDepth = Integer.valueOf(sysSetting.getSysvalue());
        log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==rebateCastDepth==" + rebateCastDepth);

        // 加锁，避免等幂删除和新增冲突
        String key = RedisConstants.MBR_REBATE_AGENT_CAST_DAY + siteCode;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, siteCode, 60, TimeUnit.MINUTES);
        if (Boolean.TRUE.equals(isExpired)) {
            log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==计算开始");
            Long startTime = System.currentTimeMillis();

            HashMap<String,Boolean> r = new HashMap<String, Boolean>();  // 异常时删除所有数据
            r.put("isException",false);
            try {
                // 判断当日是否已经计算日表
                String yesterdayStartTime = DateUtil.getPastDate(Constants.EVNumber.one, DateUtil.FORMAT_10_DATE);
                MbrRebateAgentDay mbrRebateAgentDay = new MbrRebateAgentDay();
                mbrRebateAgentDay.setCreateTimeEx(yesterdayStartTime);
                List<MbrRebateAgentDay> list = mbrRebateAgentDayMapper.select(mbrRebateAgentDay);
                // 如果存在昨日计算的，则清除昨日的重新计算
                if (!Collections3.isEmpty(list)) {
                    log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==删除昨日重新计算");
                    mbrRebateAgentDayMapper.delete(mbrRebateAgentDay);
                }

                // 查询计算深度内的会员
                MbrRebateAgentQryDto qryDto = new MbrRebateAgentQryDto();
                qryDto.setRebateCastDepth(rebateCastDepth);
                List<MbrRebateAgentQryDto> listAll = mbrRebateAgentMapper.qryMbrRebateAgentDepthList(qryDto);

                // 处理掉上面查询出的多余的数据
                listAll = listAll.stream().filter(
                        deleteDto ->
                                !(deleteDto.getChildNodeId().equals(deleteDto.getParentId())
                                    && deleteDto.getMaxDepth().compareTo(Constants.EVNumber.zero) != Constants.EVNumber.zero)
                ).collect(Collectors.toList());

                log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==listAll.size==" + listAll.size());

                // 先按深度降序排序，再分组，得到排序后的分组
                listAll = listAll.stream().sorted(Comparator.comparingInt(MbrRebateAgentQryDto::getMaxDepth).reversed()).collect(Collectors.toList());
                LinkedHashMap<Integer, List<MbrRebateAgentQryDto>> groupBy =
                        listAll.stream().collect(Collectors.groupingBy(MbrRebateAgentQryDto::getMaxDepth, LinkedHashMap::new, Collectors.toList()));

                // 按层级顺行计算，同层级并行计算
                for (Integer maxDetph : groupBy.keySet()) {
                    log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==maxDepth==" + maxDetph + "==start");
                    List<MbrRebateAgentQryDto> dtos = groupBy.get(maxDetph);
                    List<CompletableFuture<Integer>> cfs = new ArrayList<>();
                    dtos.forEach(dto -> {
                            try{
                                cfs.add(depthCalculation(dto, siteCode, maxDetph));
                            }catch (Exception ex){
                                log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==maxDepth==" + maxDetph + "==loginname==" + dto.getLoginName() + "==异常");
                                throw ex;
                            }
                        }
                    );
                    CompletableFuture<Void> all = CompletableFuture.allOf(cfs.toArray(new CompletableFuture[cfs.size()]));
                    all.whenComplete((ok,ex)->{
                        if (ex != null) {
                            log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==maxDepth==" + maxDetph + "==异常==ex==" + ex);
                            r.put("isException",true);
                        }
                    });

                    log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==maxDepth==" + maxDetph + "==等待计算完毕");
                    all.join();
                    log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==maxDepth==" + maxDetph + "==计算完毕");
                    log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==maxDepth==" + maxDetph + "==end");

                    // 出现异常，直接结束
                    if(r.get("isException")){
                        break;
                    }
                }
                log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==end");
            }catch(Exception ex){
                log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==计算发生异常==" + ex);
                r.put("isException",true);
            } finally {
                redisService.del(key);
            }

            if(r.get("isException")){
                log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==计算发生异常==删除计算数据==start" );
                String yesterdayStartTime = DateUtil.getPastDate(Constants.EVNumber.one, DateUtil.FORMAT_10_DATE);
                MbrRebateAgentDay mbrRebateAgentDay = new MbrRebateAgentDay();
                mbrRebateAgentDay.setCreateTimeEx(yesterdayStartTime);
                mbrRebateAgentDayMapper.delete(mbrRebateAgentDay);
                log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==计算发生异常==删除计算数据==end" );
            }
            log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==计算结束==time==" + (System.currentTimeMillis()-startTime));
        }
        log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==end");
    }

    /**
     * 月表-计算
     * @param siteCode
     */
    public void mbrRebateAgentCastMonth(String siteCode) {
        log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==start");

        // 判断是否有有可用的全民代理活动
        OprActActivity actActivity = getRebateAct();
        if (isNull(actActivity) || StringUtil.isEmpty(actActivity.getRule())) {
            log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==无活动");
            return;
        }
        // 获得规则
        MbrRebateAgentRuleDto rebateDto = jsonUtil.fromJson(actActivity.getRule(), MbrRebateAgentRuleDto.class);
        if (Objects.isNull(rebateDto) || Objects.isNull(rebateDto.getRuleScopeDtos())) {
            log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==无rebateDto");
            return;
        }
        // 获得层级规则
        List<MbrRebateAgentRuleLevelDto> ruleScopeDtos = rebateDto.getRuleScopeDtos();
        if(Objects.isNull(ruleScopeDtos) || ruleScopeDtos.size() == Constants.EVNumber.zero){
            log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==无层级规则");
            return;
        }

        // 查询计算深度
        SysSetting sysSetting = sysSettingService.getSysSetting(SystemConstants.REBATE_CAST_DEPTH_AGENT);
        if(Objects.isNull(sysSetting) || Objects.isNull(sysSetting.getSysvalue())){
            log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==未配置计算深度");
            return;
        }
        Integer rebateCastDepth = Integer.valueOf(sysSetting.getSysvalue());
        log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==rebateCastDepth==" + rebateCastDepth);

        String key = RedisConstants.MBR_REBATE_AGENT_CAST_MONTH + siteCode;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, siteCode, 60, TimeUnit.MINUTES);
        if (Boolean.TRUE.equals(isExpired)) {
            log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==计算开始");
            Long startTime = System.currentTimeMillis();

            HashMap<String,Boolean> r = new HashMap<String, Boolean>();  // 异常时删除所有数据
            r.put("isException",false);

            try {
                // 判断是否计算过下级会员返利,且已处理
                String yesterdayStartTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_6_DATE,1,0);
                MbrRebateAgentBonus mbrRebateAgentBonus = new MbrRebateAgentBonus();
                mbrRebateAgentBonus.setCreateTimeEx(yesterdayStartTime);
                List<MbrRebateAgentBonus> list =  mbrRebateAgentMapper.getDealStatusList(mbrRebateAgentBonus);
                // 存在处理过的，则不再计算
                if (!Collections3.isEmpty(list)) {
                    log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==存在上个月计算");
                    return;
                }

                // 删除本月的month和bonus
                mbrRebateAgentBonusMapper.delete(mbrRebateAgentBonus);
                MbrRebateAgentMonth mbrRebateAgentMonth = new MbrRebateAgentMonth();
                mbrRebateAgentMonth.setCreateTimeEx(yesterdayStartTime);
                mbrRebateAgentMonthMapper.delete(mbrRebateAgentMonth);

                // 查询计算深度内的会员
                MbrRebateAgentQryDto qryDto = new MbrRebateAgentQryDto();
                qryDto.setRebateCastDepth(rebateCastDepth);
                List<MbrRebateAgentQryDto> listAll = mbrRebateAgentMapper.qryMbrRebateAgentDepthList(qryDto);

                // 处理掉上面查询出的多余的数据
                listAll = listAll.stream().filter(
                        deleteDto ->
                                !(deleteDto.getChildNodeId().equals(deleteDto.getParentId())
                                        && deleteDto.getMaxDepth().compareTo(Constants.EVNumber.zero) != Constants.EVNumber.zero)
                ).collect(Collectors.toList());

                // 先按深度降序排序，再分组，得到排序后的分组
                listAll = listAll.stream().sorted(Comparator.comparingInt(MbrRebateAgentQryDto::getMaxDepth).reversed()).collect(Collectors.toList());
                LinkedHashMap<Integer, List<MbrRebateAgentQryDto>> groupBy =
                        listAll.stream().collect(Collectors.groupingBy(MbrRebateAgentQryDto::getMaxDepth, LinkedHashMap::new,Collectors.toList()));

                log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==listAll.size==" + listAll.size());


                // 按层级顺行计算，同层级并行计算
                for(Integer maxDetph : groupBy.keySet()){
                    List<MbrRebateAgentQryDto> dtos = groupBy.get(maxDetph);
                    List<CompletableFuture<Integer>> cfs = new ArrayList<>();
                    dtos.forEach(dto -> {
                            try{
                                CompletableFuture<Integer> result = depthCalculationMonth(dto,ruleScopeDtos,siteCode,actActivity,maxDetph);
                                cfs.add(result);
                            }catch (Exception ex){
                                log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==maxDepth==" + maxDetph + "==loginname==" + dto.getLoginName() + "==异常");
                                throw ex;
                            }
                        }
                    );
                    CompletableFuture<Void> all = CompletableFuture.allOf(cfs.toArray(new CompletableFuture[cfs.size()]));
                    all.whenComplete((ok,ex)->{
                        if (ex != null) {
                            log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==maxDepth==" + maxDetph + "==异常==ex==" + ex);
                            r.put("isException",true);
                        }
                    });

                    log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==maxDepth==" + maxDetph + "==等待计算完毕");
                    all.join();
                    log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==maxDepth==" + maxDetph + "==计算完毕");
                    log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==maxDepth==" + maxDetph + "==end");

                    // 出现异常，直接结束
                    if(r.get("isException")){
                        break;
                    }
                }
                log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==end");
            }catch(Exception ex){
                log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==计算发生异常==" + ex);
                r.put("isException",true);
            } finally {
                redisService.del(key);
            }

            if(r.get("isException")){
                log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==计算发生异常==删除计算数据==start" );
                String yesterdayStartTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_6_DATE,1,0);
                MbrRebateAgentBonus mbrRebateAgentBonus = new MbrRebateAgentBonus();
                mbrRebateAgentBonus.setCreateTimeEx(yesterdayStartTime);
                mbrRebateAgentBonusMapper.delete(mbrRebateAgentBonus);
                MbrRebateAgentMonth mbrRebateAgentMonth = new MbrRebateAgentMonth();
                mbrRebateAgentMonth.setCreateTimeEx(yesterdayStartTime);
                mbrRebateAgentMonthMapper.delete(mbrRebateAgentMonth);
                log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==计算发生异常==删除计算数据==end" );
            }
            log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==计算结束==time==" + (System.currentTimeMillis()-startTime));
        }
    }


    /**
     * 日表-层级会员计算
     * @param dto  计算的会员数据
     */
    @Async("depthCalculationAsyncExecutor")
    @Transactional
    public CompletableFuture<Integer> depthCalculation(MbrRebateAgentQryDto dto,String siteCode,Integer maxDetph){
        ThreadLocalCache.setSiteCodeAsny(siteCode);
        try{
            log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==maxDepth==" + maxDetph + "==loginname==" + dto.getLoginName() + "==start");
            // 构建日表对象
            MbrRebateAgentDay mbrRebateAgentDay = getMbrRebateAgentDay(dto);

            // 公共查询对象
            MbrRebateAgentQryDto qryDto = new MbrRebateAgentQryDto();
            qryDto.setLoginName(dto.getLoginName());
            qryDto.setChildNodeId(dto.getChildNodeId());

            // 计算时间处理
            // 获得昨天时间
            String yesterdayStartTime = DateUtil.getPastDate(Constants.EVNumber.one,DateUtil.FORMAT_10_DATE);
            String yesterdayStartTimeEx = yesterdayStartTime +" 00:00:00";
            String yesterdayEndTime = DateUtil.getCurrentDate(DateUtil.FORMAT_10_DATE);
            String yesterdayEndTimeEx = yesterdayEndTime +" 00:00:00";

            // 获得月时间
            // 先判断是不是本月第一天
            String monthStartTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_10_DATE,0,0);  // 当月第一天
            if(yesterdayEndTime.equals(monthStartTime)){  // 如果今天天是本月第一天
                // 则计算上个月第一天到今天
                monthStartTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_10_DATE,1,0);     //上月第一天
            }
            String monthStartTimeEx = monthStartTime +" 00:00:00";

            // 获取昨日自身派彩和有效投注
            qryDto.setStartTime(yesterdayStartTime);
            qryDto.setEndTime(yesterdayStartTime);
            MbrRebateAgentQryDto resultDto = mbrRebateAgentMapper.getValidPayout(qryDto);
            mbrRebateAgentDay.setPayout(resultDto.getPayout());                                    // 昨日自身游戏派彩
            mbrRebateAgentDay.setValidbet(resultDto.getValidbet());                                // 昨日有效投注

            // 获取月累计自身派彩和有效投注
            qryDto.setStartTime(monthStartTime);
            qryDto.setEndTime(yesterdayStartTime);
            resultDto = mbrRebateAgentMapper.getValidPayout(qryDto);
            mbrRebateAgentDay.setPayoutMonth(resultDto.getPayout());                                // 月累积自身游戏派彩
            mbrRebateAgentDay.setValidbetMonth(resultDto.getValidbet());                            // 月累积有效投注

            // 获得昨日自身红利
            qryDto.setStartTime(yesterdayStartTimeEx);
            qryDto.setEndTime(yesterdayEndTimeEx);
            BigDecimal bonusAmount = mbrRebateAgentMapper.qryMbrBonus(qryDto);
            mbrRebateAgentDay.setBonusAmount(bonusAmount);

            // 获得月累积自身红利
            qryDto.setStartTime(monthStartTimeEx);
            qryDto.setEndTime(yesterdayEndTimeEx);
            bonusAmount = mbrRebateAgentMapper.qryMbrBonus(qryDto);
            mbrRebateAgentDay.setBonusAmountMonth(bonusAmount);

            // 昨日 贡献上级的有效派彩=昨日自身游戏派彩+昨日自身红利  可以为正可以为负
            BigDecimal validPayoutForParent = CommonUtil.adjustScale(mbrRebateAgentDay.getPayout().add(mbrRebateAgentDay.getBonusAmount()));
            mbrRebateAgentDay.setValidPayoutForParent(validPayoutForParent);

            // 月累积 贡献上级的有效派彩=月累积自身游戏派彩+月累积自身红利  可以为正可以为负
            validPayoutForParent = CommonUtil.adjustScale(mbrRebateAgentDay.getPayoutMonth().add(mbrRebateAgentDay.getBonusAmountMonth()));
            mbrRebateAgentDay.setValidPayoutForParentMonth(validPayoutForParent);

            // 非代理会员
            if(dto.getAgyflag().equals(Constants.EVNumber.zero)){
                // 下级直属会员(普通会员+代理会员)贡献的 派彩 合计
                mbrRebateAgentDay.setValidPayoutFromChildMember(BigDecimal.ZERO);
                mbrRebateAgentDay.setValidPayoutFromChildMemberMonth(BigDecimal.ZERO);
                // 下级代理会员贡献的 有效派彩 合计
                mbrRebateAgentDay.setValidPayoutFromChildMemberAgent(BigDecimal.ZERO);
                mbrRebateAgentDay.setValidPayoutFromChildMemberAgentMonth(BigDecimal.ZERO);
                // 下级贡献的有效派彩=validpayoutfromchildmember+validpayoutfromchildmemberagent
                mbrRebateAgentDay.setValidPayoutFromChild(BigDecimal.ZERO);
                mbrRebateAgentDay.setValidPayoutFromChildMonth(BigDecimal.ZERO);
            }

            // 代理会员
            if(dto.getAgyflag().equals(Constants.EVNumber.one)){
                qryDto.setCreateTime(mbrRebateAgentDay.getCreateTimeEx());
                // 昨日
                // 昨日下级直属会员(普通会员+代理会员)贡献的 有效派彩 合计
                BigDecimal validPayoutFromChildMember = mbrRebateAgentMapper.getValidPayoutFromChildMember(qryDto);
                mbrRebateAgentDay.setValidPayoutFromChildMember(validPayoutFromChildMember);

                // 昨日下级代理会员贡献的 有效派彩 合计
                BigDecimal validPayoutFromChildMemberAgent = mbrRebateAgentMapper.getValidPayoutFromChildMemberAgent(qryDto);
                mbrRebateAgentDay.setValidPayoutFromChildMemberAgent(validPayoutFromChildMemberAgent);

                // 昨日下级贡献的有效派彩=validpayoutfromchildmember+validpayoutfromchildmemberagent
                BigDecimal validPayoutFromChild = validPayoutFromChildMember.add(validPayoutFromChildMemberAgent);
                if(validPayoutFromChild.signum() < 0){    // 负数
                    mbrRebateAgentDay.setValidPayoutFromChild(validPayoutFromChild);
                }else{
                    mbrRebateAgentDay.setValidPayoutFromChild(BigDecimal.ZERO);
                }

                // 月累积
                // 月累积下级直属会员(普通会员+代理会员)贡献的 有效派彩 合计
                validPayoutFromChildMember = mbrRebateAgentMapper.getValidPayoutFromChildMemberMonth(qryDto);
                mbrRebateAgentDay.setValidPayoutFromChildMember(validPayoutFromChildMember);

                // 月累积下级代理会员贡献的 有效派彩 合计
                validPayoutFromChildMemberAgent = mbrRebateAgentMapper.getValidPayoutFromChildMemberAgentMonth(qryDto);
                mbrRebateAgentDay.setValidPayoutFromChildMemberAgent(validPayoutFromChildMemberAgent);

                // 月累积下级贡献的有效派彩=validpayoutfromchildmember+validpayoutfromchildmemberagent
                validPayoutFromChild = validPayoutFromChildMember.add(validPayoutFromChildMemberAgent);
                if(validPayoutFromChild.signum() < 0){    // 负数
                    mbrRebateAgentDay.setValidPayoutFromChildMonth(validPayoutFromChild);
                }else{
                    mbrRebateAgentDay.setValidPayoutFromChildMonth(BigDecimal.ZERO);
                }
            }

            // 插入
            mbrRebateAgentDayMapper.insertSelective(mbrRebateAgentDay);
            log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==maxDepth==" + maxDetph + "==loginname==" + dto.getLoginName() + "==data==" + jsonUtil.toJson(mbrRebateAgentDay));
            log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==maxDepth==" + maxDetph + "==loginname==" + dto.getLoginName() + "==end");
        }catch (Exception e){
            log.info("mbrRebateAgentCast==siteCode==" + siteCode + "==maxDepth==" + maxDetph + "==loginname==" + dto.getLoginName() + "==depthCalculation执行异常==" , e);
            throw e;
        }

        return CompletableFuture.completedFuture(1);
    }


    /**
     * 月表-层级会员计算
     * @param dto  计算的会员数据
     * @param ruleScopeDtos 规则dto
     */
    @Async("depthCalculationMonthAsyncExecutor")
    @Transactional
    public CompletableFuture<Integer> depthCalculationMonth(MbrRebateAgentQryDto dto, List<MbrRebateAgentRuleLevelDto> ruleScopeDtos,
                                                            String siteCode, OprActActivity actActivity,Integer maxDetph){
        ThreadLocalCache.setSiteCodeAsny(siteCode);
        try {
            log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==maxDepth==" + maxDetph + "==loginname==" + dto.getLoginName() + "==start");
            // 月表对象
            MbrRebateAgentMonth mbrRebateAgentMonth = getMbrRebateAgentMonth(dto,actActivity);

            // 公共查询对象
            MbrRebateAgentQryDto qryDto = new MbrRebateAgentQryDto();
            qryDto.setLoginName(dto.getLoginName());
            qryDto.setChildNodeId(dto.getChildNodeId());

            // 计算时间处理
            // 获得昨天时间
            String yesterdayStartTime = DateUtil.getPastDate(Constants.EVNumber.one,DateUtil.FORMAT_10_DATE);
            String yesterdayStartTimeEx = yesterdayStartTime +" 00:00:00";
            String yesterdayEndTime = DateUtil.getCurrentDate(DateUtil.FORMAT_10_DATE);
            String yesterdayEndTimeEx = yesterdayEndTime +" 00:00:00";

            // 获得月时间
            // 先判断是不是本月第一天
            String monthStartTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_10_DATE,1,0);          // 上月第一天 yyyy-mm-dd
            String monthStartTimeEx = monthStartTime +" 00:00:00";
            String monthEndTime = DateUtil.getEndOfMonth(DateUtil.FORMAT_10_DATE, 1, 0);            // 上月最后一天 yyyy-mm-dd
            String monthEndTimeEx = DateUtil.getFirstOfMonth(DateUtil.FORMAT_18_DATE_TIME, 0, 0);   // 本月第一天 yyyy-mm-dd hh:mi:ss

            // 自身派彩和有效投注
            qryDto.setStartTime(monthStartTime);
            qryDto.setEndTime(monthEndTime);
            MbrRebateAgentQryDto resultDto = mbrRebateAgentMapper.getValidPayout(qryDto);
            mbrRebateAgentMonth.setPayout(resultDto.getPayout());                                    // 自身游戏派彩
            mbrRebateAgentMonth.setValidbet(resultDto.getValidbet());                                // 有效投注

            // 获得自身红利
            qryDto.setStartTime(monthStartTimeEx);
            qryDto.setEndTime(monthEndTimeEx);
            BigDecimal bonusAmount = mbrRebateAgentMapper.qryMbrBonus(qryDto);
            mbrRebateAgentMonth.setBonusAmount(bonusAmount);

            // 自身的有效派彩=自身游戏派彩+自身红利  正则为0
            BigDecimal validPayoutForSelf = CommonUtil.adjustScale(mbrRebateAgentMonth.getPayout().add(bonusAmount));
            if (validPayoutForSelf.signum() < 0) {    // 负数
                mbrRebateAgentMonth.setValidPayoutForSelf(validPayoutForSelf.abs());
            } else {
                mbrRebateAgentMonth.setValidPayoutForSelf(BigDecimal.ZERO);
            }
            // 贡献上级的有效派彩=自身游戏派彩+自身红利  可以为正可以为负
            BigDecimal validPayoutForParent = CommonUtil.adjustScale(mbrRebateAgentMonth.getPayout().add(bonusAmount));
            mbrRebateAgentMonth.setValidPayoutForParent(validPayoutForParent);

            // 非代理会员
            if(dto.getAgyflag().equals(Constants.EVNumber.zero)){
                // 下级直属会员(普通会员+代理会员)贡献的 派彩 合计
                mbrRebateAgentMonth.setValidPayoutFromChildMember(BigDecimal.ZERO);
                // 下级代理会员贡献的 有效派彩 合计
                mbrRebateAgentMonth.setValidPayoutFromChildMemberAgent(BigDecimal.ZERO);
                // 下级贡献的有效派彩=validpayoutfromchildmember+validpayoutfromchildmemberagent
                mbrRebateAgentMonth.setValidPayoutFromChild(BigDecimal.ZERO);
                // 自身返利比例
                mbrRebateAgentMonth.setCommissionRatio(BigDecimal.ZERO);
                // 自身返利=自身的有效派彩 * 自身返利比例
                mbrRebateAgentMonth.setRebate(BigDecimal.ZERO);
                // 下级提成比例
                mbrRebateAgentMonth.setCommissionRatioSub(BigDecimal.ZERO);
                // 贡献上级的奖金: 计算自身时，不计算这个
                mbrRebateAgentMonth.setBonusAmountExForParent(BigDecimal.ZERO);
                // 自身获得下级的奖金总计
                mbrRebateAgentMonth.setBonusAmountExfromChildTotal(BigDecimal.ZERO);
                // 奖金百分比
                mbrRebateAgentMonth.setBonusPercent(BigDecimal.ZERO);
                // 下级代理实发总计
                mbrRebateAgentMonth.setRebateChildTotal(BigDecimal.ZERO);
                // 自身获得的实际下级佣金初算=下级贡献的有效派彩*(下级提成比例/100)
                BigDecimal rebateFromChild = mbrRebateAgentMonth.getValidPayoutFromChild().multiply(mbrRebateAgentMonth.getCommissionRatioSub().divide(new BigDecimal(ONE_HUNDRED), 4, RoundingMode.DOWN));
                mbrRebateAgentMonth.setRebateFromChild(rebateFromChild);
                // 自身获得的实际下级佣金实际=初算-下级代理实发总计
                BigDecimal rebateFromChildActual = mbrRebateAgentMonth.getRebateFromChild().subtract(mbrRebateAgentMonth.getRebateChildTotal());
                mbrRebateAgentMonth.setRebateFromChildActual(rebateFromChildActual);

                // 自身实发=自身返利+自身获得下级的奖金总计+自身获得的实际下级佣金实际
                BigDecimal rebateTotal = mbrRebateAgentMonth.getRebate().add(mbrRebateAgentMonth.getBonusAmountExfromChildTotal()).add(mbrRebateAgentMonth.getRebateFromChildActual());
                mbrRebateAgentMonth.setRebateTotal(rebateTotal);

                // 自身实发给上级计算值=自身返利rebate  + 自身获得的实际下级佣金实际rebateFromChildActual
                BigDecimal rebateTotalForParent = mbrRebateAgentMonth.getRebate().add(mbrRebateAgentMonth.getRebateFromChildActual());
                mbrRebateAgentMonth.setRebateTotalForParent(rebateTotalForParent);
            }

            // 代理会员
            if(dto.getAgyflag().equals(Constants.EVNumber.one)){
                qryDto.setCreateTime(mbrRebateAgentMonth.getCreateTimeEx());

                // 下级直属会员(普通会员+代理会员)贡献的 有效派彩 合计
                BigDecimal validPayoutFromChildMember = mbrRebateAgentMapper.getValidPayoutFromChildMemberEx(qryDto);
                mbrRebateAgentMonth.setValidPayoutFromChildMember(validPayoutFromChildMember);

                // 下级代理会员贡献的 有效派彩 合计
                BigDecimal validPayoutFromChildMemberAgent = mbrRebateAgentMapper.getValidPayoutFromChildMemberAgentEx(qryDto);
                mbrRebateAgentMonth.setValidPayoutFromChildMemberAgent(validPayoutFromChildMemberAgent);

                // 下级贡献的有效派彩=validpayoutfromchildmember+validpayoutfromchildmemberagent
                BigDecimal validPayoutFromChild = validPayoutFromChildMember.add(validPayoutFromChildMemberAgent);
                if(validPayoutFromChild.signum() < 0){    // 负数
                    mbrRebateAgentMonth.setValidPayoutFromChild(validPayoutFromChild);
                }else{
                    mbrRebateAgentMonth.setValidPayoutFromChild(BigDecimal.ZERO);
                }

                // 获得层级对应的规则
                MbrRebateAgentRuleLevelDto ruleDto = getRuleByAgyLevelId(ruleScopeDtos,dto.getAgyLevelId());
                if(Objects.isNull(ruleDto)){
                    log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==maxDepth==" + maxDetph + "==loginname==" + dto.getLoginName() + "==ruleDto为null");
                    return CompletableFuture.completedFuture(1);
                }
                // 奖金比例
                BigDecimal bonusPercent = ruleDto.getBonusPercent();
                mbrRebateAgentMonth.setBonusPercent(bonusPercent);
                // 自身返利比例
                BigDecimal commissionRatio = getCommissionRatioByAgyLevelId(ruleDto,mbrRebateAgentMonth);
                mbrRebateAgentMonth.setCommissionRatio(commissionRatio);
                // 自身返利=自身的有效派彩 * 自身返利比例
                BigDecimal rebate =  mbrRebateAgentMonth.getValidPayoutForSelf().multiply(mbrRebateAgentMonth.getCommissionRatio().divide(new BigDecimal(ONE_HUNDRED), 4, RoundingMode.DOWN));
                mbrRebateAgentMonth.setRebate(rebate);
                // 获得下级提成比例O
                BigDecimal commissionRatioSub = getCommissionRatioSubByAgyLevelId(ruleDto,mbrRebateAgentMonth);
                mbrRebateAgentMonth.setCommissionRatioSub(commissionRatioSub);

                // 下级代理实发总计
                BigDecimal rebateChildTotal = mbrRebateAgentMapper.getRebateChildTotal(qryDto);
                mbrRebateAgentMonth.setRebateChildTotal(rebateChildTotal);

                // 自身获得的实际下级佣金初算=下级贡献的有效派彩绝对值*(下级提成比例/100)
                BigDecimal rebateFromChild = mbrRebateAgentMonth.getValidPayoutFromChild().abs().multiply(mbrRebateAgentMonth.getCommissionRatioSub().divide(new BigDecimal(ONE_HUNDRED), 4, RoundingMode.DOWN));
                mbrRebateAgentMonth.setRebateFromChild(rebateFromChild);



//                // 第一版：自身获得的实际下级佣金实际=初算-下级代理实发总计-所有下级代理累计实际有效输赢为正的情况下的该代理的下级实得佣金合计； 所有下级代理会员 当累计实际有效输赢为正的情况下的，所有的该下级代理会员的的下级实得佣金的合计
//                BigDecimal allSubMemAgentRebateChildTotal = mbrRebateAgentMapper.getAllSubMemAgentRebateChildTotal(qryDto);

                // 第二版：自身获得的实际下级佣金实际=初算-下级代理实发总计+所有下级代理中同一条线的累计实际有效输赢为正的情况下的最高级的代理的该代理的下级实得佣金合计
                // 1. 先查出累计实际有效输赢为正(实际是也可能是正好0)的所有下级代理会员
                List<MbrRebateAgentMonth> allSubMemAgentRebateChildList = mbrRebateAgentMapper.getAllSubMemAgentRebateChildList(qryDto);
                // 2. 找出最上级并计算合计
                BigDecimal allSubMemAgentRebateChildTotal = getAllSubMemAgentRebateChildTotal(allSubMemAgentRebateChildList);
                // 3. 自身获得的实际下级佣金实际=初算-下级代理实发总计+所有下级代理中同一条线的累计实际有效输赢为正的情况下的最高级的代理的该代理的下级实得佣金合计
                BigDecimal rebateFromChildActual = rebateFromChild.subtract(rebateChildTotal).add(allSubMemAgentRebateChildTotal);

                if (rebateFromChildActual.compareTo(new BigDecimal(Constants.EVNumber.zero)) > 0) {    // 正数 自身获得的实际下级佣金实际
                    mbrRebateAgentMonth.setRebateFromChildActual(rebateFromChildActual);
                }else{
                    mbrRebateAgentMonth.setRebateFromChildActual(new BigDecimal(Constants.EVNumber.zero));
                }

                // 贡献上级的奖金-初始0，只能由上级来计算
                mbrRebateAgentMonth.setBonusAmountExForParent(BigDecimal.ZERO);
                // 自身获得下级的奖金总计: 此处去计算该上级所有下级的奖金，更新
                // 获得所有代理下级
                List<MbrRebateAgentMonth> subMemAgentList = mbrRebateAgentMapper.getSubMemAgent(qryDto);
                // 获得所有直属下级(直属会员+直属代理会员)的有效投注总计
                BigDecimal subTotalValidBet = mbrRebateAgentMapper.getSubTotalValidBet(qryDto);
                // 计算下级的奖金
                for(MbrRebateAgentMonth month : subMemAgentList){
                    // 直属下级合计和上级都有流水且大于3000（含3000)
                    if(subTotalValidBet.compareTo(new BigDecimal(3000)) >=0 && mbrRebateAgentMonth.getValidbet().compareTo(new BigDecimal(3000)) >=0 ){
                        BigDecimal bonusAmountExForParent = BigDecimal.ZERO;
                        if(month.getCommissionRatioSub().compareTo(mbrRebateAgentMonth.getCommissionRatioSub()) >= 0){    // 下级代理佣金比例大于上级的当下提成比例
                            bonusAmountExForParent = bonusAmountExForParent.add(month.getRebateFromChildActual().multiply(bonusPercent.divide(new BigDecimal(ONE_HUNDRED), 4, RoundingMode.DOWN)));
                        }
                        if(month.getCommissionRatio().compareTo(mbrRebateAgentMonth.getCommissionRatioSub()) >= 0){    // 下级返利比例大于上级的当下提成比例
                            bonusAmountExForParent = bonusAmountExForParent.add(month.getRebate().multiply(bonusPercent.divide(new BigDecimal(ONE_HUNDRED), 4, RoundingMode.DOWN)));
                        }
                        // 更新下级的贡献上级的奖金
                        if(bonusAmountExForParent.compareTo(BigDecimal.ZERO) > 0){
                            month.setBonusAmountExForParent(bonusAmountExForParent);
                            mbrRebateAgentMonthMapper.updateByPrimaryKeySelective(month);
                        }
                    }
                }

                // 自身获得下级的奖金总计
                BigDecimal bonusAmountExfromChildTotal = mbrRebateAgentMapper.getBonusAmountExfromChildTotal(qryDto);
                mbrRebateAgentMonth.setBonusAmountExfromChildTotal(bonusAmountExfromChildTotal);

                // 自身实发=自身返利+自身获得下级的奖金总计+自身获得的实际下级佣金实际
                BigDecimal rebateTotal = mbrRebateAgentMonth.getRebate().add(mbrRebateAgentMonth.getBonusAmountExfromChildTotal());
//                if (mbrRebateAgentMonth.getRebateFromChildActual().compareTo(new BigDecimal(Constants.EVNumber.zero)) > 0) {    // 自身获得的实际下级佣金实际 负数不计入实发
//                    rebateTotal = rebateTotal.add(mbrRebateAgentMonth.getRebateFromChildActual());
//                }
                rebateTotal = rebateTotal.add(mbrRebateAgentMonth.getRebateFromChildActual());
                mbrRebateAgentMonth.setRebateTotal(rebateTotal);

                // 自身实发给上级计算值=自身返利rebate  + 自身获得的实际下级佣金实际rebateFromChildActual
//                if (mbrRebateAgentMonth.getRebateFromChildActual().compareTo(new BigDecimal(Constants.EVNumber.zero)) > 0) {    // 自身获得的实际下级佣金实际 负数不计入给贡献上级的实发
//                    BigDecimal rebateTotalForParent = mbrRebateAgentMonth.getRebate().add(mbrRebateAgentMonth.getRebateFromChildActual());
//                    mbrRebateAgentMonth.setRebateTotalForParent(rebateTotalForParent);
//                }else{
//                    mbrRebateAgentMonth.setRebateTotalForParent(mbrRebateAgentMonth.getRebate());
//                }

                BigDecimal rebateTotalForParent = mbrRebateAgentMonth.getRebate().add(mbrRebateAgentMonth.getRebateFromChildActual());
                mbrRebateAgentMonth.setRebateTotalForParent(rebateTotalForParent);
            }

            // 插入
            mbrRebateAgentMonthMapper.insert(mbrRebateAgentMonth);
            // 处理审核表
            if(mbrRebateAgentMonth.getAgyflag().equals(Constants.EVNumber.one)) {
                // 插入审核表
                MbrRebateAgentBonus bonus = insertMbrRebateAgentBonus(mbrRebateAgentMonth,actActivity);
                // 更新Month表
                mbrRebateAgentMonth.setBonusId(bonus.getId());
                mbrRebateAgentMonthMapper.updateByPrimaryKey(mbrRebateAgentMonth);
            }
            log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==maxDepth==" + maxDetph + "==loginname==" + dto.getLoginName() + "==data==" + jsonUtil.toJson(mbrRebateAgentMonth));
            log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==maxDepth==" + maxDetph + "==loginname==" + dto.getLoginName() + "==end");
        }catch (Exception e){
            log.info("mbrRebateAgentCastMonth==siteCode==" + siteCode + "==maxDepth==" + maxDetph + "==loginname==" + dto.getLoginName() + "==depthCalculation执行异常==" , e);
            throw e;
        }
        return CompletableFuture.completedFuture(1);
    }

    public MbrRebateAgentBonus insertMbrRebateAgentBonus(MbrRebateAgentMonth dto,OprActActivity actActivity){
        MbrRebateAgentBonus bonus = new MbrRebateAgentBonus();
        bonus.setAccountId(dto.getAccountId());                     // 会员ID
        bonus.setLoginName(dto.getLoginName());                     // 会员名
        bonus.setCreateUser("system");                              // 创建人
        bonus.setCreateTime(dto.getCreateTime());   // 创建时间  yyyy-MM-dd HH:mm:ss
        bonus.setCreateTimeEx(dto.getCreateTimeEx());       // 创建时间  yyyy-mm
        bonus.setOrderPrefix(OrderConstants.ACTIVITY_QMDL);         // 订单前缀
        bonus.setOrderNo(new SnowFlake().nextId());                 // 订单号
        bonus.setFinancialCode(OrderConstants.ACTIVITY_QMDL);       // 财务code
        bonus.setStatus(Constants.EVNumber.two);                    // 审核状态  2待审核
        bonus.setRebateTotal(dto.getRebateTotal());                 // 自身实发 实发总额
        bonus.setAgyLevelId(dto.getAgyLevelId());                   // 代理会员级别id
        bonus.setRuleId(actActivity.getRuleId());                   // 活动规则ID
        bonus.setActivityId(actActivity.getId());                   // 活动id
        bonus.setMbrRebateAgentMonthId(dto.getId());                // mbrRebateAgentMonth表id
        // 插入表
        mbrRebateAgentBonusMapper.insert(bonus);

        return bonus;
    }

    public OprActActivity getRebateAct() {
        OprActActivity actActivity = new OprActActivity();
        actActivity.setTmplCode(mbrRebateAgentCode);
        actActivity.setUseStart(getPastDate(Constants.EVNumber.one, FORMAT_10_DATE));
        actActivity.setIsdel(Constants.EVNumber.zero);
        List<OprActActivity> oprActActivities = operateActivityMapper.findWaterActivity(actActivity);
        if (CollectionUtils.isNotEmpty(oprActActivities)) {
            return oprActActivities.get(0);
        }
        return null;
    }

    public BigDecimal getCommissionRatioByAgyLevelId(MbrRebateAgentRuleLevelDto ruleDto, MbrRebateAgentMonth mbrRebateAgentMonth){
        List<MbrRebateAgentRuleLevelSelfDto> mbrRebateAgentRuleSelfDto = ruleDto.getMbrRebateAgentRuleSelfDtos(); // 自身有效输赢提成比例
        // 排序：倒序
        mbrRebateAgentRuleSelfDto.sort((r1, r2) -> r2.getAmountMax().compareTo(r1.getAmountMax()));

        // 自身返利比例
        BigDecimal commissionRatio = BigDecimal.ZERO;
        for(MbrRebateAgentRuleLevelSelfDto selfDto : mbrRebateAgentRuleSelfDto){
            // 大于等于左区间
            if(mbrRebateAgentMonth.getValidPayoutForSelf().compareTo(selfDto.getAmountMin()) >= 0){
                commissionRatio = selfDto.getDonateAmount();
                break;
            }
        }
        return commissionRatio;
    }

    public BigDecimal getCommissionRatioSubByAgyLevelId(MbrRebateAgentRuleLevelDto ruleDto, MbrRebateAgentMonth mbrRebateAgentMonth){
        List<MbrRebateAgentRuleLevelSubDto> mbrRebateAgentRuleSubDto = ruleDto.getMbrRebateAgentRuleSubDtos();    // 下级代理会员佣金比例
        // 排序：倒序
        mbrRebateAgentRuleSubDto.sort((r1, r2) -> r2.getAmountMax().compareTo(r1.getAmountMax()));

        // 下级提成比例
        BigDecimal commissionRatioSub = BigDecimal.ZERO;
        for(MbrRebateAgentRuleLevelSubDto ruleSubDto : mbrRebateAgentRuleSubDto){
            // 大于等于左区间
            if(mbrRebateAgentMonth.getValidPayoutFromChild().abs().compareTo(ruleSubDto.getAmountMin()) >= 0){
                commissionRatioSub = ruleSubDto.getDonateAmount();
                break;
            }
        }
        return commissionRatioSub;
    }

    public MbrRebateAgentRuleLevelDto getRuleByAgyLevelId(List<MbrRebateAgentRuleLevelDto> ruleScopeDtos, Integer agyLevelId){
        MbrRebateAgentRuleLevelDto ruleDto = null;
        for(MbrRebateAgentRuleLevelDto rule : ruleScopeDtos){
            if(rule.getAgyLevelId() == agyLevelId){
                ruleDto = rule;
            }
        }
        return ruleDto;
    }

    private MbrRebateAgentDay getMbrRebateAgentDay(MbrRebateAgentQryDto dto){
        MbrRebateAgentDay mbrRebateAgentDay = new MbrRebateAgentDay();
        mbrRebateAgentDay.setAccountId(dto.getChildNodeId());                               // 会员id
        mbrRebateAgentDay.setLoginName(dto.getLoginName());                                 // 会员名
        mbrRebateAgentDay.setParentId(dto.getParentId());                                   // 父结点
        mbrRebateAgentDay.setLoginNameParent(dto.getLoginNameParent());                     // 父结点会员账号
        mbrRebateAgentDay.setMaxDepth(dto.getMaxDepth());                                   // 会员作为子节点深度
        mbrRebateAgentDay.setAgyflag(dto.getAgyflag());                                     // 全民代理标志 0非代理会员 1代理会员
        mbrRebateAgentDay.setAgyLevelId(dto.getAgyLevelId());                               // 代理会员级别id
        mbrRebateAgentDay.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));               // 计算时间 yyyy-MM-dd HH:mm:ss
        mbrRebateAgentDay.setCreateTimeEx(DateUtil.getPastDate(Constants.EVNumber.one,DateUtil.FORMAT_10_DATE));                  // 统计时间 计算时间 yyyy-MM-dd
        return mbrRebateAgentDay;
    }

    private MbrRebateAgentMonth getMbrRebateAgentMonth(MbrRebateAgentQryDto dto,OprActActivity actActivity){
        MbrRebateAgentMonth MbrRebateAgentMonth = new MbrRebateAgentMonth();
        MbrRebateAgentMonth.setAccountId(dto.getChildNodeId());                               // 会员id
        MbrRebateAgentMonth.setLoginName(dto.getLoginName());                                 // 会员名
        MbrRebateAgentMonth.setParentId(dto.getParentId());                                   // 父结点
        MbrRebateAgentMonth.setLoginNameParent(dto.getLoginNameParent());                     // 父结点会员账号
        MbrRebateAgentMonth.setMaxDepth(dto.getMaxDepth());                                   // 会员作为子节点深度
        MbrRebateAgentMonth.setAgyflag(dto.getAgyflag());                                     // 全民代理标志 0非代理会员 1代理会员
        MbrRebateAgentMonth.setAgyLevelId(dto.getAgyLevelId());                               // 代理会员级别id
        MbrRebateAgentMonth.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));               // 计算时间 yyyy-MM-dd HH:mm:ss
        MbrRebateAgentMonth.setCreateTimeEx(DateUtil.getFirstOfMonth(DateUtil.FORMAT_6_DATE,1,0));                   // 统计时间 yyyy-mm

        MbrRebateAgentMonth.setActivityId(actActivity.getId());                               // 活动ID
        MbrRebateAgentMonth.setRuleId(actActivity.getRuleId());                               // 活动规则ID
        return MbrRebateAgentMonth;
    }

    public BigDecimal getAllSubMemAgentRebateChildTotal(List<MbrRebateAgentMonth> listAll){
        BigDecimal allSubMemAgentRebateChildTotal = BigDecimal.ZERO;
        // 比对上级节点，不存在上级节点，则累加
        for(MbrRebateAgentMonth rebate : listAll){
            boolean isExist = false;
            String parentids = rebate.getParentIds();
            String[] parentIdStr = parentids.split(",");
            for (int i = 0; i < parentIdStr.length; i++) {
                Integer parentId = Integer.valueOf(parentIdStr[i]);
                MbrRebateAgentMonth data = listAll.stream().filter(o->o.getAccountId().equals(parentId)).findFirst().orElse(null);
                if(Objects.nonNull(data)){
                    isExist = true;
                    break;
                }
            }
            if(!isExist){
                allSubMemAgentRebateChildTotal = allSubMemAgentRebateChildTotal.add(rebate.getRebateChildTotal());
            }
        }
        return allSubMemAgentRebateChildTotal;
    }

    public static void main(String[] args) {
        List<MbrRebateAgentQryDto> listAll = new ArrayList<>();
        MbrRebateAgentQryDto t1 = new MbrRebateAgentQryDto();
        MbrRebateAgentQryDto t2 = new MbrRebateAgentQryDto();
        MbrRebateAgentQryDto t3 = new MbrRebateAgentQryDto();
        MbrRebateAgentQryDto t4 = new MbrRebateAgentQryDto();
        MbrRebateAgentQryDto t5 = new MbrRebateAgentQryDto();
        MbrRebateAgentQryDto t6 = new MbrRebateAgentQryDto();
        MbrRebateAgentQryDto t7 = new MbrRebateAgentQryDto();

        t1.setMaxDepth(6);
        t2.setMaxDepth(2);
        t3.setMaxDepth(1);
        t4.setMaxDepth(4);
        t5.setMaxDepth(4);
        t6.setMaxDepth(5);
        t7.setMaxDepth(6);

        listAll.add(t1);
        listAll.add(t2);
        listAll.add(t3);
        listAll.add(t4);
        listAll.add(t5);
        listAll.add(t6);
        listAll.add(t7);

        // 先按深度排序，再分组
        listAll = listAll.stream().sorted(Comparator.comparingInt(MbrRebateAgentQryDto::getMaxDepth).reversed()).collect(Collectors.toList());
        LinkedHashMap<Integer, List<MbrRebateAgentQryDto>> groupBy =
                listAll.stream().collect(Collectors.groupingBy(MbrRebateAgentQryDto::getMaxDepth, LinkedHashMap::new,Collectors.toList()));

        listAll.clear();
    }
}
