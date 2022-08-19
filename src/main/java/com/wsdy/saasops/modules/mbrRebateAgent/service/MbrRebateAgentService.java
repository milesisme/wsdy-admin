package com.wsdy.saasops.modules.mbrRebateAgent.service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.mbrRebateAgent.dto.*;
import com.wsdy.saasops.modules.mbrRebateAgent.entity.MbrRebateAgentBonus;
import com.wsdy.saasops.modules.mbrRebateAgent.entity.MbrRebateAgentMonth;
import com.wsdy.saasops.modules.mbrRebateAgent.mapper.MbrRebateAgentBonusMapper;
import com.wsdy.saasops.modules.mbrRebateAgent.mapper.MbrRebateAgentMapper;
import com.wsdy.saasops.modules.mbrRebateAgent.mapper.MbrRebateAgentMonthMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;


@Slf4j
@Service
public class MbrRebateAgentService {
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private MbrRebateAgentMapper mbrRebateAgentMapper;
    @Autowired
    private MbrAccountMapper mbrAccountMapper;
    @Autowired
    private MbrRebateAgentBonusMapper mbrRebateAgentBonusMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private MbrWalletService mbrWalletService;
    @Autowired
    private MbrRebateAgentMonthMapper mbrRebateAgentMonthMapper;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private MbrRebateAgentCastService mbrRebateAgentCastService;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;

    public MbrRebateAgentRespDto mbrRebateAgentInfo(Integer accountId){
        MbrRebateAgentRespDto ret = new MbrRebateAgentRespDto();

        // 判断是否有全民代理的活动
        // 判断是否有有可用的全民代理活动
        OprActActivity actActivity = mbrRebateAgentCastService.getRebateAct();
        if (isNull(actActivity) || StringUtil.isEmpty(actActivity.getRule())) {
            ret.setIsShowMbrAgent(Boolean.FALSE);
            return ret;
        }
        // 获得规则
        MbrRebateAgentRuleDto rebateDto = jsonUtil.fromJson(actActivity.getRule(), MbrRebateAgentRuleDto.class);
        if (Objects.isNull(rebateDto) || Objects.isNull(rebateDto.getRuleScopeDtos())) {
            ret.setIsShowMbrAgent(Boolean.FALSE);
            return ret;
        }
        // 获得层级规则
        List<MbrRebateAgentRuleLevelDto> ruleScopeDtos = rebateDto.getRuleScopeDtos();
        if(Objects.isNull(ruleScopeDtos) || ruleScopeDtos.size() == Constants.EVNumber.zero){
            ret.setIsShowMbrAgent(Boolean.FALSE);
            return ret;
        }

        // 查询计算的深度
        Integer rebateCastDepth = Integer.valueOf(sysSettingService.getSysSetting(SystemConstants.REBATE_CAST_DEPTH_AGENT).getSysvalue());
        // 查询会员深度
        Integer maxDepth = mbrRebateAgentMapper.qryMbrDepth(accountId);
        // 会员层级大于等于计算深度,不显示全民代理
        if(rebateCastDepth.compareTo(maxDepth) <= 0){
            ret.setIsShowMbrAgent(Boolean.FALSE);
            return ret;
        }
        // 获得代理会员信息
        MbrAccount mbrAccount = mbrAccountMapper.selectByPrimaryKey(accountId);
        // 获取推荐人code
        if(StringUtil.isEmpty(mbrAccount.getDomainCode())){
            mbrAccount.setDomainCode(getDomainCode());
            mbrAccountMapper.updateByPrimaryKeySelective(mbrAccount);
        }

        // 判断代理会员条件
        // 查询自身累积存款
        BigDecimal totalDepositSelf = mbrRebateAgentMapper.qryDepositSum(accountId);
        // 查询下级累积存款
        BigDecimal totalDepositChild = mbrRebateAgentMapper.qryDepositSumFromChild(accountId);
        if(totalDepositSelf.compareTo(new BigDecimal(1000)) >= Constants.EVNumber.zero
                && totalDepositChild.compareTo(new BigDecimal(1000)) >= Constants.EVNumber.zero ){
            ret.setIsApply(Boolean.TRUE);
        }else{
            ret.setIsApply(Boolean.FALSE);
        }

        ret.setAgyflag(mbrAccount.getAgyflag());    // 全民代理标志 0非代理会员 1代理会员
        ret.setIsShowMbrAgent(Boolean.TRUE);        // 是否显示全民代理 true 显示  fale 不显示
        ret.setCodeId(mbrAccount.getDomainCode());  // 推荐人domainCode

        String url = sysSettingService.getPromotionUrl(CommonUtil.getSiteCode());
        if (!StringUtil.isEmpty(url)) {
            ret.setAppDomain(url);                  // APP推广域名
        }

        return ret;
    }

    public void applyMbrAgent(Integer accountId){
        // 先查询
        MbrAccount dto = mbrAccountMapper.selectByPrimaryKey(accountId);
        if(Objects.isNull(dto)){
            throw new R200Exception("该会员不存在");
        }

        if(Integer.valueOf(Constants.EVNumber.one).equals(dto.getAgyflag())){
            throw new R200Exception("该会员已是代理会员！");
        }

        // 查询自身累积存款
        BigDecimal totalDepositSelf = mbrRebateAgentMapper.qryDepositSum(accountId);
        // 查询下级累积存款
        BigDecimal totalDepositChild = mbrRebateAgentMapper.qryDepositSumFromChild(accountId);
        // 符合条件更新代理标志
        if(totalDepositSelf.compareTo(new BigDecimal(1000)) >= Constants.EVNumber.zero
                && totalDepositChild.compareTo(new BigDecimal(1000)) >= Constants.EVNumber.zero ){
            MbrAccount mbrAccount = new MbrAccount();
            mbrAccount.setId(accountId);
            mbrAccount.setAgyflag(Constants.EVNumber.one);   // 全民代理标志 0非代理会员 1代理会员
            mbrAccount.setAgyTime(getCurrentDate(FORMAT_18_DATE_TIME));
            mbrAccount.setAgyLevelId(Constants.EVNumber.one);  // 默认会员代理等级 0,数据库id为1
            mbrAccountMapper.updateByPrimaryKeySelective(mbrAccount);
            return;
        }
        throw new R200Exception("未满足条件！");
    }

    public MbrRebateAgentRespDto qryRebateInfo(MbrRebateAgentQryDto dto){
        // 查询自身数据
        MbrRebateAgentMonth qryMonth = new MbrRebateAgentMonth();
        qryMonth.setAccountId(dto.getChildNodeId());
        qryMonth.setCreateTimeEx(dto.getCreateTime());

        qryMonth = mbrRebateAgentMonthMapper.selectOne(qryMonth);
        MbrRebateAgentRespDto ret = new MbrRebateAgentRespDto();
        if(Objects.isNull(qryMonth)){
            return ret;
        }
        ret.setValidPayoutForSelf(qryMonth.getValidPayoutForSelf());        // 自身的有效派彩
        ret.setValidPayoutFromChild(qryMonth.getValidPayoutFromChild());    // 下级贡献的有效派彩
        ret.setCommissionRatio(qryMonth.getCommissionRatio());              // 自身返利比例
        ret.setCommissionRatioSub(qryMonth.getCommissionRatioSub());        // 下级提成比例
        ret.setRebate(qryMonth.getRebate());                                // 自身返利
        ret.setBonusAmountExfromChildTotal(qryMonth.getBonusAmountExfromChildTotal());  // 自身获得下级的奖金总计
        ret.setRebateFromChildActual(qryMonth.getRebateFromChildActual());  // 自身获得的实际下级佣金实际
        ret.setRebateTotal(qryMonth.getRebateTotal());                      // 自身实发

        // 查询下级数据 分页
        PageHelper.startPage(dto.getPageNo(), dto.getPageSize());
        List<MbrRebateAgentRespChildListDto> childList = mbrRebateAgentMapper.getChildList(dto);
        PageUtils p = BeanUtil.toPagedResult(childList);
        ret.setChildList(p);
        return ret;
    }

    public PageUtils qryBonusList(MbrRebateAgentQryDto dto){
        PageHelper.startPage(dto.getPageNo(), dto.getPageSize());
        List<MbrRebateAgentRespBonusListDto> list = mbrRebateAgentMapper.qryBonusList(dto);
        PageUtils p = BeanUtil.toPagedResult(list);
        return p;
    }

    public PageUtils getChildBonusList(MbrRebateAgentQryDto dto){
        PageHelper.startPage(dto.getPageNo(), dto.getPageSize());
        List<MbrRebateAgentRespBonusListDto> list = mbrRebateAgentMapper.getChildBonusList(dto);
        PageUtils p = BeanUtil.toPagedResult(list);
        return p;
    }
    public PageUtils getMbrRebateAgentDayList(MbrRebateAgentQryDto dto){
        PageHelper.startPage(dto.getPageNo(), dto.getPageSize());
        List<MbrRebateAgentRespHistoryListDto> list = mbrRebateAgentMapper.getMbrRebateAgentDayList(dto);
        PageUtils p = BeanUtil.toPagedResult(list);
        return p;
    }


    @Transactional
    public void bonusAuditBatch( MbrRebateAgentAuditDto dto, String siteCode){
        String key = RedisConstants.MBR_REBATE_AGENT_AUDIT_BATCH + dto.getCreateTime() + siteCode;
        try {
            // 查询当月所有待处理单
            MbrRebateAgentBonus qry = new MbrRebateAgentBonus();
            qry.setStatus(Constants.EVNumber.two);
            qry.setCreateTimeEx(dto.getCreateTime());
            List<MbrRebateAgentBonus> list = mbrRebateAgentBonusMapper.select(qry);
            if (Collections3.isEmpty(list)) {
                return;
            }

            list.stream().forEach(bs -> {
                String key2 = RedisConstants.MBR_REBATE_AGENT_AUDIT + bs.getId() + CommonUtil.getSiteCode();
                Boolean isExpired2 = redisService.setRedisExpiredTimeBo(key2, bs.getId(), 10, TimeUnit.SECONDS);
                if (isExpired2) {
                    try{
                        // 更新dto
                        MbrRebateAgentBonus updateDto = new MbrRebateAgentBonus();
                        updateDto.setId(bs.getId());
                        updateDto.setAuditTime(dto.getAuditTime());
                        updateDto.setAuditUser(dto.getUserName());
                        updateDto.setStatus(dto.getStatus());

                        // 如果拒绝
                        if (dto.getStatus().equals(Constants.EVNumber.zero)) {
                            updateDto.setMemo("批量拒绝");
                            mbrRebateAgentBonusMapper.updateByPrimaryKeySelective(updateDto);
                        }

                        // 如果成功
                        if (dto.getStatus().equals(Constants.EVNumber.one)) {
                            // 处理帐变，钱包等
                            if(bs.getRebateTotal().compareTo(new BigDecimal(Constants.EVNumber.zero)) > 0){
                                MbrBillDetail billDetail = mbrWalletService.castWalletAndBillDetail(bs.getLoginName(),
                                        bs.getAccountId(), OrderConstants.ACTIVITY_QMDL,
                                        bs.getRebateTotal(), bs.getOrderNo().toString(), Boolean.TRUE, null, null);

                                // billdetailId
                                updateDto.setBillDetailId(billDetail.getId());
                            }

                            // 更新MbrRebateAgentBonus
                            updateDto.setId(bs.getId());
                            updateDto.setMemo("批量通过");
                            mbrRebateAgentBonusMapper.updateByPrimaryKeySelective(updateDto);
                            // 发送通知 TODO
                        }
                    } finally {
                        redisService.del(key2);
                    }
                }
            });

            // 日志
            mbrAccountLogService.bonusAuditBatch(dto);

        } finally {
            // 删除外层key
            redisService.del(key);
        }
    }

    @Transactional
    public void bonusAudit( MbrRebateAgentAuditDto dto){
        MbrRebateAgentBonus bs = mbrRebateAgentBonusMapper.selectByPrimaryKey(dto.getId());
        if (Objects.isNull(bs)) {
            throw new R200Exception("返利不存在");
        }
        if (!bs.getStatus().equals(Constants.EVNumber.two)) {
            throw new R200Exception("仅可审核待处理状态的返利");
        }
        // 初始化数据
        MbrRebateAgentBonus updateDto = new MbrRebateAgentBonus();
        updateDto.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        updateDto.setAuditUser(dto.getUserName());
        updateDto.setStatus(dto.getStatus());
        updateDto.setId(bs.getId());

        // 日志
        mbrAccountLogService.bonusAudit(dto,bs);


        // 如果拒绝
        if (dto.getStatus().equals(Constants.EVNumber.zero)) {
            // 更新MbrRebateAgentBonus
            mbrRebateAgentBonusMapper.updateByPrimaryKeySelective(updateDto);
            return;
        }

        // 如果成功
        if (dto.getStatus().equals(Constants.EVNumber.one)) {
            // 处理帐变，钱包等
            MbrBillDetail billDetail = mbrWalletService.castWalletAndBillDetail(bs.getLoginName(),
                    bs.getAccountId(), OrderConstants.ACTIVITY_QMDL,
                    bs.getRebateTotal(), bs.getOrderNo().toString(), Boolean.TRUE,null,null);

            // 更新MbrRebateAgentBonus
            updateDto.setId(bs.getId());
            updateDto.setBillDetailId(billDetail.getId());
            mbrRebateAgentBonusMapper.updateByPrimaryKeySelective(updateDto);

            // 发送通知 TODO
        }
    }

    public void bonusAuditEdit( MbrRebateAgentAuditDto dto){
        MbrRebateAgentBonus bs = mbrRebateAgentBonusMapper.selectByPrimaryKey(dto.getId());

        MbrRebateAgentBonus updateDto = new MbrRebateAgentBonus();
        updateDto.setId(bs.getId());
        updateDto.setMemo(dto.getMemo());
        mbrRebateAgentBonusMapper.updateByPrimaryKeySelective(updateDto);
    }


    public String getDomainCode() {
        Boolean flag = Boolean.TRUE;
        MbrAccount tempAcc;
        while (flag){
            long numbers = (long)(Math.random()*9*Math.pow(10,8-1)) + (long)Math.pow(10,8-1);
            tempAcc = new MbrAccount();
            tempAcc.setDomainCode(String.valueOf(numbers));
            List<MbrAccount> listAcc = mbrAccountMapper.select(tempAcc);
            if(Collections3.isNotEmpty(listAcc)){
                continue;
            }
            return numbers+"";
        }
        return "";
    }

    public SysFileExportRecord exportBonusAudit(MbrRebateAgentQryDto dto, SysUserEntity user, String module, String excelTempPath) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(user.getUserId(), module);
        if (null != record) {
            List<MbrRebateAgentRespBonusListDto> dtoList = mbrRebateAgentMapper.qryBonusList(dto);
            List<Map<String, Object>> list = dtoList.stream().map(e -> {
                dealAndTransData(e);
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());

            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(excelTempPath, list, user.getUserId(), module, siteCode);
        }
        return record;
    }

    private void dealAndTransData(MbrRebateAgentRespBonusListDto dto){
        // 审核状态 0 失败 1成功 2待审核
        switch (dto.getStatus()) {
            case 0:
                dto.setStatusStr("失败");
                break;
            case 1:
                dto.setStatusStr("成功");
                break;
            case 2:
                dto.setStatusStr("待审核");
                break;
            default:
                dto.setStatusStr("");
        }

        dto.setCommissionRatioStr(dto.getCommissionRatio().toString() + "%");
        dto.setCommissionRatioSubStr(dto.getCommissionRatioSub().toString() + "%");
    }

}
