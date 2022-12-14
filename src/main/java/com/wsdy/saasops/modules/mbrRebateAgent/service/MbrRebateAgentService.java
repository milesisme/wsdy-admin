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

        // ????????????????????????????????????
        // ?????????????????????????????????????????????
        OprActActivity actActivity = mbrRebateAgentCastService.getRebateAct();
        if (isNull(actActivity) || StringUtil.isEmpty(actActivity.getRule())) {
            ret.setIsShowMbrAgent(Boolean.FALSE);
            return ret;
        }
        // ????????????
        MbrRebateAgentRuleDto rebateDto = jsonUtil.fromJson(actActivity.getRule(), MbrRebateAgentRuleDto.class);
        if (Objects.isNull(rebateDto) || Objects.isNull(rebateDto.getRuleScopeDtos())) {
            ret.setIsShowMbrAgent(Boolean.FALSE);
            return ret;
        }
        // ??????????????????
        List<MbrRebateAgentRuleLevelDto> ruleScopeDtos = rebateDto.getRuleScopeDtos();
        if(Objects.isNull(ruleScopeDtos) || ruleScopeDtos.size() == Constants.EVNumber.zero){
            ret.setIsShowMbrAgent(Boolean.FALSE);
            return ret;
        }

        // ?????????????????????
        Integer rebateCastDepth = Integer.valueOf(sysSettingService.getSysSetting(SystemConstants.REBATE_CAST_DEPTH_AGENT).getSysvalue());
        // ??????????????????
        Integer maxDepth = mbrRebateAgentMapper.qryMbrDepth(accountId);
        // ????????????????????????????????????,?????????????????????
        if(rebateCastDepth.compareTo(maxDepth) <= 0){
            ret.setIsShowMbrAgent(Boolean.FALSE);
            return ret;
        }
        // ????????????????????????
        MbrAccount mbrAccount = mbrAccountMapper.selectByPrimaryKey(accountId);
        // ???????????????code
        if(StringUtil.isEmpty(mbrAccount.getDomainCode())){
            mbrAccount.setDomainCode(getDomainCode());
            mbrAccountMapper.updateByPrimaryKeySelective(mbrAccount);
        }

        // ????????????????????????
        // ????????????????????????
        BigDecimal totalDepositSelf = mbrRebateAgentMapper.qryDepositSum(accountId);
        // ????????????????????????
        BigDecimal totalDepositChild = mbrRebateAgentMapper.qryDepositSumFromChild(accountId);
        if(totalDepositSelf.compareTo(new BigDecimal(1000)) >= Constants.EVNumber.zero
                && totalDepositChild.compareTo(new BigDecimal(1000)) >= Constants.EVNumber.zero ){
            ret.setIsApply(Boolean.TRUE);
        }else{
            ret.setIsApply(Boolean.FALSE);
        }

        ret.setAgyflag(mbrAccount.getAgyflag());    // ?????????????????? 0??????????????? 1????????????
        ret.setIsShowMbrAgent(Boolean.TRUE);        // ???????????????????????? true ??????  fale ?????????
        ret.setCodeId(mbrAccount.getDomainCode());  // ?????????domainCode

        String url = sysSettingService.getPromotionUrl(CommonUtil.getSiteCode());
        if (!StringUtil.isEmpty(url)) {
            ret.setAppDomain(url);                  // APP????????????
        }

        return ret;
    }

    public void applyMbrAgent(Integer accountId){
        // ?????????
        MbrAccount dto = mbrAccountMapper.selectByPrimaryKey(accountId);
        if(Objects.isNull(dto)){
            throw new R200Exception("??????????????????");
        }

        if(Integer.valueOf(Constants.EVNumber.one).equals(dto.getAgyflag())){
            throw new R200Exception("??????????????????????????????");
        }

        // ????????????????????????
        BigDecimal totalDepositSelf = mbrRebateAgentMapper.qryDepositSum(accountId);
        // ????????????????????????
        BigDecimal totalDepositChild = mbrRebateAgentMapper.qryDepositSumFromChild(accountId);
        // ??????????????????????????????
        if(totalDepositSelf.compareTo(new BigDecimal(1000)) >= Constants.EVNumber.zero
                && totalDepositChild.compareTo(new BigDecimal(1000)) >= Constants.EVNumber.zero ){
            MbrAccount mbrAccount = new MbrAccount();
            mbrAccount.setId(accountId);
            mbrAccount.setAgyflag(Constants.EVNumber.one);   // ?????????????????? 0??????????????? 1????????????
            mbrAccount.setAgyTime(getCurrentDate(FORMAT_18_DATE_TIME));
            mbrAccount.setAgyLevelId(Constants.EVNumber.one);  // ???????????????????????? 0,?????????id???1
            mbrAccountMapper.updateByPrimaryKeySelective(mbrAccount);
            return;
        }
        throw new R200Exception("??????????????????");
    }

    public MbrRebateAgentRespDto qryRebateInfo(MbrRebateAgentQryDto dto){
        // ??????????????????
        MbrRebateAgentMonth qryMonth = new MbrRebateAgentMonth();
        qryMonth.setAccountId(dto.getChildNodeId());
        qryMonth.setCreateTimeEx(dto.getCreateTime());

        qryMonth = mbrRebateAgentMonthMapper.selectOne(qryMonth);
        MbrRebateAgentRespDto ret = new MbrRebateAgentRespDto();
        if(Objects.isNull(qryMonth)){
            return ret;
        }
        ret.setValidPayoutForSelf(qryMonth.getValidPayoutForSelf());        // ?????????????????????
        ret.setValidPayoutFromChild(qryMonth.getValidPayoutFromChild());    // ???????????????????????????
        ret.setCommissionRatio(qryMonth.getCommissionRatio());              // ??????????????????
        ret.setCommissionRatioSub(qryMonth.getCommissionRatioSub());        // ??????????????????
        ret.setRebate(qryMonth.getRebate());                                // ????????????
        ret.setBonusAmountExfromChildTotal(qryMonth.getBonusAmountExfromChildTotal());  // ?????????????????????????????????
        ret.setRebateFromChildActual(qryMonth.getRebateFromChildActual());  // ???????????????????????????????????????
        ret.setRebateTotal(qryMonth.getRebateTotal());                      // ????????????

        // ?????????????????? ??????
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
            // ??????????????????????????????
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
                        // ??????dto
                        MbrRebateAgentBonus updateDto = new MbrRebateAgentBonus();
                        updateDto.setId(bs.getId());
                        updateDto.setAuditTime(dto.getAuditTime());
                        updateDto.setAuditUser(dto.getUserName());
                        updateDto.setStatus(dto.getStatus());

                        // ????????????
                        if (dto.getStatus().equals(Constants.EVNumber.zero)) {
                            updateDto.setMemo("????????????");
                            mbrRebateAgentBonusMapper.updateByPrimaryKeySelective(updateDto);
                        }

                        // ????????????
                        if (dto.getStatus().equals(Constants.EVNumber.one)) {
                            // ????????????????????????
                            if(bs.getRebateTotal().compareTo(new BigDecimal(Constants.EVNumber.zero)) > 0){
                                MbrBillDetail billDetail = mbrWalletService.castWalletAndBillDetail(bs.getLoginName(),
                                        bs.getAccountId(), OrderConstants.ACTIVITY_QMDL,
                                        bs.getRebateTotal(), bs.getOrderNo().toString(), Boolean.TRUE, null, null);

                                // billdetailId
                                updateDto.setBillDetailId(billDetail.getId());
                            }

                            // ??????MbrRebateAgentBonus
                            updateDto.setId(bs.getId());
                            updateDto.setMemo("????????????");
                            mbrRebateAgentBonusMapper.updateByPrimaryKeySelective(updateDto);
                            // ???????????? TODO
                        }
                    } finally {
                        redisService.del(key2);
                    }
                }
            });

            // ??????
            mbrAccountLogService.bonusAuditBatch(dto);

        } finally {
            // ????????????key
            redisService.del(key);
        }
    }

    @Transactional
    public void bonusAudit( MbrRebateAgentAuditDto dto){
        MbrRebateAgentBonus bs = mbrRebateAgentBonusMapper.selectByPrimaryKey(dto.getId());
        if (Objects.isNull(bs)) {
            throw new R200Exception("???????????????");
        }
        if (!bs.getStatus().equals(Constants.EVNumber.two)) {
            throw new R200Exception("????????????????????????????????????");
        }
        // ???????????????
        MbrRebateAgentBonus updateDto = new MbrRebateAgentBonus();
        updateDto.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        updateDto.setAuditUser(dto.getUserName());
        updateDto.setStatus(dto.getStatus());
        updateDto.setId(bs.getId());

        // ??????
        mbrAccountLogService.bonusAudit(dto,bs);


        // ????????????
        if (dto.getStatus().equals(Constants.EVNumber.zero)) {
            // ??????MbrRebateAgentBonus
            mbrRebateAgentBonusMapper.updateByPrimaryKeySelective(updateDto);
            return;
        }

        // ????????????
        if (dto.getStatus().equals(Constants.EVNumber.one)) {
            // ????????????????????????
            MbrBillDetail billDetail = mbrWalletService.castWalletAndBillDetail(bs.getLoginName(),
                    bs.getAccountId(), OrderConstants.ACTIVITY_QMDL,
                    bs.getRebateTotal(), bs.getOrderNo().toString(), Boolean.TRUE,null,null);

            // ??????MbrRebateAgentBonus
            updateDto.setId(bs.getId());
            updateDto.setBillDetailId(billDetail.getId());
            mbrRebateAgentBonusMapper.updateByPrimaryKeySelective(updateDto);

            // ???????????? TODO
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
        // ???????????? 0 ?????? 1?????? 2?????????
        switch (dto.getStatus()) {
            case 0:
                dto.setStatusStr("??????");
                break;
            case 1:
                dto.setStatusStr("??????");
                break;
            case 2:
                dto.setStatusStr("?????????");
                break;
            default:
                dto.setStatusStr("");
        }

        dto.setCommissionRatioStr(dto.getCommissionRatio().toString() + "%");
        dto.setCommissionRatioSubStr(dto.getCommissionRatioSub().toString() + "%");
    }

}
