package com.wsdy.saasops.modules.member.service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.api.modules.user.service.DepotOperatService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.analysis.entity.GameReportQueryModel;
import com.wsdy.saasops.modules.analysis.service.AnalysisService;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.wsdy.saasops.modules.fund.entity.FundAudit;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.fund.service.FundDepositService;
import com.wsdy.saasops.modules.fund.service.FundReportService;
import com.wsdy.saasops.modules.fund.service.FundWithdrawService;
import com.wsdy.saasops.modules.log.entity.LogMbrLogin;
import com.wsdy.saasops.modules.log.service.LogMbrloginService;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrWalletMapper;
import com.wsdy.saasops.modules.member.dto.MbrWindDto;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrBillManage;
import com.wsdy.saasops.modules.member.entity.MbrMemo;
import com.wsdy.saasops.modules.member.entity.MbrWallet;
import com.wsdy.saasops.modules.member.mapper.MbrWindMapper;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class MbrWindService extends BaseService<MbrAccountMapper, MbrAccount> {

    @Autowired
    private MbrWindMapper mbrWindMapper;
    @Autowired
    private MbrWalletMapper mbrWalletMapper;
    @Autowired
    private MbrAccountService mbrAccountService;
    @Autowired
    DepotOperatService depotOperatService;
    @Autowired
    private FundReportService fundReportService;
    @Autowired
    private AnalysisService analysisService;
    @Autowired
    private LogMbrloginService logMbrloginService;
    @Autowired
    private MbrMemoService mbrMemoService;
    @Autowired
    private FundDepositService depositService;
    @Autowired
    private FundWithdrawService withdrawService;
    @Autowired
    private OperateActivityMapper operateMapper;

    public PageUtils mbrList(MbrAccount mbrAccount, Integer pageNo, Integer pageSize){
        PageHelper.startPage(pageNo, pageSize);
        List<MbrAccount> list = mbrWindMapper.getMbrAccountList(mbrAccount);
        return BeanUtil.toPagedResult(list);
    }

    /**
     * 	风控会员信息
     * 
     * @param accountId
     * @return
     */
    public MbrAccount mbrInfoByAccount(Integer accountId){
        MbrAccount  mbrDto=mbrWindMapper.mbrInfoByAccount(accountId);
        return mbrDto;
    }

    public PageUtils memoList(MbrMemo mbrMemo, Integer pageNo, Integer pageSize) {
        return mbrMemoService.queryListPageAll(mbrMemo,pageNo,pageSize);
    }

    public void mbrNewMemo(MbrMemo mbrMemo, SysUserEntity user, String ip){
        mbrMemoService.saveMbrMemo(mbrMemo,user, ip);
    }

    public R depotBalance(Integer pageNo, Integer pageSize, Integer accountId) {
        return depotOperatService.getDepotList(pageNo, pageSize, accountId);
    }
    public BigDecimal balance(Integer accountId) {
        MbrWallet mbrWallet = new MbrWallet();
        mbrWallet.setAccountId(accountId);
        mbrWallet = mbrWalletMapper.selectOne(mbrWallet);
        if(Objects.isNull(mbrWallet)){
            throw new R200Exception("查询主账户余额异常");
        }
        return mbrWallet.getBalance();
    }
    public BigDecimal flushBalance(Integer accountId, Integer platformId) {
        return depotOperatService.flushBalance(accountId, platformId, CommonUtil.getSiteCode());
    }



    public PageUtils mbrdepositList(FundDeposit deposit, Integer pageNo, Integer pageSize) {
        return depositService.queryListPage(deposit, pageNo, pageSize, Constants.EVNumber.zero);
    }

    public PageUtils mbrwithdrawList(AccWithdraw accWithdraw, Integer pageNo, Integer pageSize) {
        return withdrawService.queryAccListPage(accWithdraw, pageNo, pageSize, Constants.EVNumber.zero);
    }

    public Map<String,Integer> mbrDeviceIpTop(Integer accountId) {
        Map<String,Integer> map = new HashMap<>();
        map.put("mbrPreferIP",mbrWindMapper.queryAccountIPNum(accountId));              // 同ip用户数
        map.put("mbrwithIPPrefNum",mbrWindMapper.queryAccountIPProfNum(accountId));     // 同ip优惠数： 相同ip的用户有优惠的用户个数
        map.put("mbrwithIPPrefNum",mbrWindMapper.queryAccountIPProfNum(accountId));     // 同设备用户数
        map.put("mbrPreferDevice",mbrWindMapper.queryAccountDeviceNum(accountId));      // 同设备优惠数
        map.put("mbrwithDevicePrefNum",mbrWindMapper.queryAccountDeviceProfNum(accountId));
        return map;
    }

    public PageUtils activitymbrIP(Integer accountId,Integer pageNo,Integer pageSize){
        PageHelper.startPage(pageNo, pageSize);
        List<MbrWindDto> list = mbrWindMapper.activitymbrIP(accountId);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils activitymbrDevice(Integer accountId,Integer pageNo,Integer pageSize){
        PageHelper.startPage(pageNo, pageSize);
        List<MbrWindDto> list = mbrWindMapper.activitymbrDevice(accountId);
        return BeanUtil.toPagedResult(list);
    }


    public PageUtils mbrwithIPPrefNum(Integer accountId,Integer pageNo,Integer pageSize){
        PageHelper.startPage(pageNo, pageSize);
        List<MbrWindDto> list = mbrWindMapper.mbrwithIPPrefNum(accountId);
        return BeanUtil.toPagedResult(list);
    }


    public PageUtils mbrwithDevicePrefNum(Integer accountId,Integer pageNo,Integer pageSize){
        PageHelper.startPage(pageNo, pageSize);
        List<MbrWindDto> list = mbrWindMapper.mbrwithDevicePrefNum(accountId);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils getBonusList(Integer accountId,Integer pageNo,Integer pageSize){
        PageHelper.startPage(pageNo, pageSize);
        List<MbrWindDto> list = mbrWindMapper.getBonusList(accountId);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils mbrbillList(MbrBillManage mbrBillManage, Integer pageNo, Integer pageSize) {
        return fundReportService.queryListPage(mbrBillManage, pageNo, pageSize);
    }

    public R mbrbetDetailsData(Integer pageNo, Integer pageSize, GameReportQueryModel model) {
        model.setSiteCode(CommonUtil.getSiteCode());
        return R.ok().putPage(analysisService.getBkRptBetListPage(pageNo, pageSize, model)).put("total", analysisService.getRptBetListReport(model));
    }

    public PageUtils mbrauditList(FundAudit audit, Integer pageNo, Integer pageSize) {
        return fundReportService.queryAuditListPage(audit, pageNo, pageSize);
    }

    public PageUtils bonusList(OprActBonus bonus, Integer pageNo, Integer pageSize) {
        bonus.setStatus(Constants.EVNumber.one);
        bonus.setFinancialCode(OrderConstants.FUND_ORDER_CODE_AA);  // 人工增加 里的 优惠类别--> 属于优惠（线下优惠活动）
        PageHelper.startPage(pageNo, pageSize);
        List<OprActBonus> bonuses = operateMapper.bonusAndTaskList(bonus);
        return BeanUtil.toPagedResult(bonuses);
    }

    public PageUtils mbrLoginlist(LogMbrLogin logMbrlogin, Integer pageNo, Integer pageSize) {
        return logMbrloginService.queryListPage(logMbrlogin, pageNo, pageSize);
    }
}