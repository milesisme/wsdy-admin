package com.wsdy.saasops.api.modules.pay.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wsdy.saasops.api.constants.PayConstants;
import com.wsdy.saasops.api.modules.pay.dto.*;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.fund.dao.FastDepositWithdrawCertificateMapper;
import com.wsdy.saasops.modules.fund.entity.FastDepositWithdrawCertificate;
import com.wsdy.saasops.modules.fund.service.*;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrDepositCountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrDepositCount;
import com.wsdy.saasops.modules.member.entity.MbrDepositLockLog;
import com.wsdy.saasops.modules.member.service.MbrDepositCondService;
import com.wsdy.saasops.modules.member.service.MbrDepositLockLogService;
import com.wsdy.saasops.modules.member.service.MbrVerifyService;
import com.wsdy.saasops.modules.operate.dto.ActivityRuleDto;
import com.wsdy.saasops.modules.operate.dto.JDepositSentDto;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.TOpActtmpl;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;
import com.wsdy.saasops.modules.system.pay.dao.*;
import com.wsdy.saasops.modules.system.pay.entity.*;
import com.wsdy.saasops.modules.system.pay.mapper.PayMapper;
import com.wsdy.saasops.modules.system.pay.service.SysQrCodeService;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.modules.fund.dao.FundDepositMapper;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.fund.entity.FundDeposit.Mark;
import com.wsdy.saasops.modules.fund.entity.FundDeposit.PaymentStatus;
import com.wsdy.saasops.modules.fund.entity.FundDeposit.Status;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.member.entity.MbrDepositCond;
import org.springframework.transaction.annotation.Transactional;

import static com.wsdy.saasops.common.utils.DateUtil.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@Transactional
public class PayInfoService {

    @Autowired
    private FundDepositMapper fundDepositMapper;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private MbrDepositCondService mbrDepositCondService;
    @Autowired
    private PayMapper payMapper;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private TPayLogoMapper payLogoMapper;
    @Autowired
    private SysDepositMapper depositMapper;
    @Autowired
    private SetBacicFastPayMapper fastPayMapper;
    @Autowired
    private SaasopsPayService saasopsPayService;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private SysQrCodeService sysQrCodeService;
    @Autowired
    private MbrDepositCountMapper mbrDepositCountMapper;
    @Autowired
    private MbrAccountMapper mbrAccountMapper;
    @Autowired
    private MbrVerifyService verifyService;
    @Autowired
    private MbrDepositLockLogService mbrDepositLockLogService;
    @Autowired
    private FastDepositWithdrawCertificateMapper certificateMapper;
    @Autowired
    private OperateActivityMapper operateMapper;

    /**
     * 	???????????????
     * @param accountId
     * @return
     */
    public BankResponseDto findDepositList(Integer accountId) {
        List<SysDeposit> sysDeposits = payMapper.findDepositBankList(accountId);
        if (Collections3.isNotEmpty(sysDeposits)) {
            List<SysDeposit> depositList = Lists.newArrayList();
            Map<Integer, List<SysDeposit>> depositGroupingBy =
                    sysDeposits.stream().collect(
                            Collectors.groupingBy(
                                    SysDeposit::getTier));
            for (Integer tierIdKey : depositGroupingBy.keySet()) {
                List<SysDeposit> deposits = depositGroupingBy.get(tierIdKey);
                SysDeposit deposit = deposits.get(0);
                if(nonNull(deposit) && Constants.EVNumber.zero == deposit.getIsQueue()) {//??????
                    depositList.addAll(deposits);
                }else{
                    depositList.add(deposit);
                }
            }
            TPayLogo payLogo = getTPayLogo();
            return getBankResponseDto(payLogo, depositList, payLogo.getName());
        }
        return null;
    }

    public List<BankResponseDto> findFastWithdrawList(Integer accountId) {
        // ???????????????????????????
        SysSetting fastWithdraw = sysSettingService.getSysSetting(SystemConstants.FAST_WITHDRAW_ENABLE);
        if (fastWithdraw == null || StringUtils.isBlank(fastWithdraw.getSysvalue()) || "0".equals(fastWithdraw.getSysvalue())) {
            return null;
        }

        List<SetBacicFastPay> fastPayListAll = payMapper.findFastDepositWithdrawList(accountId);

        if (Collections3.isNotEmpty(fastPayListAll)) {
            List<BankResponseDto> responseDtos = Lists.newArrayList();
            for(SetBacicFastPay fs : fastPayListAll) {
                BankResponseDto response = new BankResponseDto();
                response.setShowName(fs.getShowName());
                response.setPaymentType(fs.getPaymentType());   // ??????
                response.setFastDWAmount(fs.getFastDWAmount()); // ????????????
                response.setIsHot(fs.getIsHot());
                response.setIsRecommend(fs.getIsRecommend());
                response.setFastDWPayId(fs.getId());

                responseDtos.add(response);
            }
            return responseDtos;
        }
        return null;
    }

    public List<BankResponseDto> findFastDepositWithdrawList(Integer accountId) {
        List<SetBacicFastPay> fastPayListAll = payMapper.findFastDepositWithdrawList(accountId);

        // ??????????????????????????????????????????????????????
        /*if (true) {
            return null;
        }*/

        // ???????????????????????????????????????
        MbrDepositCond cond = mbrDepositCondService.getMbrDeposit(accountId);
        Integer depositName = Constants.EVNumber.zero;
        if (Objects.nonNull(cond) && cond.getDepositName() != null) {
            depositName = cond.getDepositName();
        }

        if (Collections3.isNotEmpty(fastPayListAll)) {
            List<BankResponseDto> responseDtos = Lists.newArrayList();
            for(SetBacicFastPay fs : fastPayListAll) {
                BankResponseDto response = new BankResponseDto();
                response.setShowName(fs.getShowName());
                response.setPaymentType(fs.getPaymentType());   // ??????
                response.setFastDWAmount(fs.getFastDWAmount()); // ????????????
                response.setIsHot(fs.getIsHot());
                response.setIsRecommend(fs.getIsRecommend());
                response.setFastDWPayId(fs.getId());
                response.setDepositName(Constants.EVNumber.one);

                responseDtos.add(response);
            }
            return responseDtos;
        }
        return null;
    }

    public List<BankResponseDto> findFastPayList(Integer accountId) {
        List<SetBacicFastPay> fastPayListAll = payMapper.findFastPayBankList(accountId);

        // ???????????????????????????????????????
        MbrDepositCond cond = mbrDepositCondService.getMbrDeposit(accountId);
        Integer depositName = Constants.EVNumber.zero;
        if (Objects.nonNull(cond) && cond.getDepositName() != null) {
            depositName = cond.getDepositName();
        }

        // ????????????????????? group
        if (Collections3.isNotEmpty(fastPayListAll)) {
            Map<String, List<SetBacicFastPay>> groupBy =
                    fastPayListAll.stream().collect(
                            Collectors.groupingBy(
                                    SetBacicFastPay::getAlipayFlg));

            List<BankResponseDto> responseDtos = Lists.newArrayList();
            for (String alipayFlg : groupBy.keySet()) {
                List<SetBacicFastPay> fastPayList = groupBy.get(alipayFlg);
                TPayLogo payLogo = getTPayLogo();
                for(SetBacicFastPay fs : fastPayList) {
                    List<SysDeposit> depositList = payMapper.fundFastPayDepositList(fs.getId());
                    BankResponseDto response = getBankResponseDto(payLogo, depositList, fs.getName());
                    response.setShowName(fs.getShowName());
                    response.setPaymentType(fs.getPaymentType());   // ??????
                    response.setAlipayFlg(fs.getAlipayFlg());       // ???????????????
                    response.setAmountType(fs.getAmountType());     // ????????????
        	        response.setIsHot(fs.getIsHot());
        	        response.setIsRecommend(fs.getIsRecommend());
                    // ??????????????????????????????
                    response.setDepositName("LBT".equals(fs.getPayCode()) ? depositName : Constants.EVNumber.zero);

                    if(PayConstants.TONGLUEYUN_CODE.equals(fs.getPlatfromCode())){
                        response.setUrlMethod(0);
                    } else if(PayConstants.DSDFPAY_CODE.equals(fs.getPlatfromCode())){
                        response.setUrlMethod(1);
                    } else if(PayConstants.BTPPAY_CODE.equals(fs.getPlatfromCode())){
                        response.setUrlMethod(0);
                    }
                    responseDtos.add(response);
                    if(Constants.EVNumber.zero == fs.getIsQueue()){//??????
                        continue;
                    }else{// ??????
                        break;
                    }
                }
            }

            return responseDtos;
        }
        return null;
    }

    public SysDeposit getRecentBankPay(Integer accountId){
        FundDeposit param = new FundDeposit();
        param.setMark(Constants.EVNumber.one);
        param.setAccountId(accountId);
        FundDeposit deposit = fundMapper.getRecentDeposit(param);
        if(nonNull(deposit)){
            Integer payId = deposit.getCompanyPayId();
            SysDeposit sysDeposit = depositMapper.selectByPrimaryKey(payId);
            return sysDeposit;
        }
        return null;
    }

    public Object getResentPayChannel(Integer accountId){

        FundDeposit param = new FundDeposit();
        param.setAccountId(accountId);
        FundDeposit deposit = fundMapper.getRecentDeposit(param);
        if(nonNull(deposit)){
            Map<String,Object> map = new HashMap<>(1);
            if(deposit.getMark() == Constants.EVNumber.zero){//??????
                Map<String,Object> selectedPay = new HashMap<>(2);
                Integer onlinePayId = deposit.getOnlinePayId();
                SetBacicOnlinepay onlinepay = payMapper.findOnlinePayAndPay(onlinePayId);
                Integer payType = onlinepay.getPaymentType();
                if (payType == Constants.EVNumber.one) {//qq
                    selectedPay.put("onlinePayId",onlinepay.getId());
                    selectedPay.put("name",onlinepay.getName());
                    map.put("qqList",selectedPay);
                }
                if (payType == Constants.EVNumber.two) {//wechat
                    selectedPay.put("onlinePayId",onlinepay.getId());
                    selectedPay.put("name",onlinepay.getName());
                    map.put("weChatList",selectedPay);
                }
                if (payType == Constants.EVNumber.three) {//jd
                    selectedPay.put("onlinePayId",onlinepay.getId());
                    selectedPay.put("name",onlinepay.getName());
                    map.put("jdList",selectedPay);
                }
                if (payType == Constants.EVNumber.four
                        || payType == Constants.EVNumber.seven
                        || payType == Constants.EVNumber.eight) {//wy
                    selectedPay.put("onlinePayId",onlinepay.getId());
                    selectedPay.put("name",onlinepay.getName());
                    map.put("wyList",selectedPay);
                }
                if (payType == Constants.EVNumber.five) {
                    selectedPay.put("onlinePayId",onlinepay.getId());
                    selectedPay.put("name",onlinepay.getName());
                    map.put("alipayList",selectedPay);
                }
            }else{
                Integer comPayId = deposit.getCompanyPayId();
                SysDeposit sysDeposit = depositMapper.selectByPrimaryKey(comPayId);
                Map<String,Object> selectedPay = new HashMap<>(2);
                if(isNull(sysDeposit.getFastPayId())){//????????????
                    selectedPay.put("depositId",sysDeposit.getId());
                    selectedPay.put("bankName",sysDeposit.getBankName());
                    selectedPay.put("userName",deposit.getDepositUser());
                    map.put("bankList",selectedPay);
                }else{
                    selectedPay.put("depositId",sysDeposit.getId());
                    selectedPay.put("bankName",sysDeposit.getBankName());
                    selectedPay.put("userName",deposit.getDepositUser());
                    map.put("fastBankList",selectedPay);
                }
            }
            return map;
        }
        return null;

    }

    private BankResponseDto getBankResponseDto(TPayLogo payLogo, List<SysDeposit> depositList, String bankName) {
        BankResponseDto response = new BankResponseDto();
        // ?????????????????????????????????????????????????????????
        if (depositList.get(0).getFastPayId() == null) {
            response.setAmountType(0);
        } else {
            SetBacicFastPay fastPay = fastPayMapper.selectByPrimaryKey(depositList.get(0).getFastPayId());
            response.setAmountType(fastPay.getAmountType());
        }
        response.setBankCards(depositList);
        response.setBankName(bankName);
        response.setEwmLogo(payLogo.getEwmLogo());
        response.setBankLogo(payLogo.getBankLogo());
        response.setDisableLogo(payLogo.getDisableLogo());
        Optional<SysDeposit> maxDeposit = depositList.stream()
                .max(Comparator.comparing(SysDeposit::getMaxAmout));
        response.setMaxAmout(maxDeposit.isPresent()
                ? maxDeposit.get().getMaxAmout() : BigDecimal.ZERO);
        Optional<SysDeposit> minDeposit = depositList.stream()
                .min(Comparator.comparing(SysDeposit::getMinAmout));
        response.setMinAmout(minDeposit.isPresent()
                ? minDeposit.get().getMinAmout() : BigDecimal.ZERO);
        return response;
    }

    // ????????????????????????
    public DepositPostScript getDepositPostScript(PayParams params) {
        // ????????????
        MbrAccount account = accountMapper.selectByPrimaryKey(params.getAccountId());
        // ???????????????????????????+??????
        sysSettingService.checkPayCondition(account,SystemConstants.DEPOSIT_CONDITION);
        // ??????????????????
        SysDeposit sysDeposit = getSysDeposit(params, account);
        // ??????????????????
        checkoutSysDeposit(sysDeposit, params);
        // ???????????????????????????
        MbrDepositCond mbrDepositCond = mbrDepositCondService.getMbrDeposit(params.getAccountId());

        // ???????????????????????????
        MbrDepositCount count = checkDepositCount(account, Constants.EVNumber.zero);

        // ???????????????????????????????????????????????????
        boolean lock = mbrDepositLockLogService.companyUnpayLock(account);
        if (lock) {
            throw new R200Exception("???????????????????????????????????????.???");
        }

        // ??????deposit??????
        FundDeposit deposit = saveFundDespoit(params, sysDeposit, mbrDepositCond, account);

        // ???????????????
        DepositPostScript script;
        // ???????????????????????????
        if (Objects.isNull(sysDeposit.getFastPayId())) {
            // deposit??????
            fundDepositMapper.insert(deposit);
            verifyService.addMbrVerifyDeposit(deposit, params.getSiteCode());
            // ???????????????
            script = fundMapper.findOfflineDepositInfo(deposit.getId());
            return script;
        }
        // ??????????????????
        script =  fastPay(deposit, sysDeposit, params.getSiteCode(),count);
        return script;
    }

    // ????????????????????????
    public DepositPostScript getFastDepositPostScript(PayParams params) {
        // ????????????
        MbrAccount account = accountMapper.selectByPrimaryKey(params.getAccountId());
        // ???????????????????????????+??????
        sysSettingService.checkPayCondition(account,SystemConstants.DEPOSIT_CONDITION);
        // ????????????????????????
        List<SetBacicFastPay> tempList = payMapper.findBasicFastPay(new SetBacicFastPay(){{
            setAvailable(Constants.EVNumber.one);
            setId(params.getDepositId());
        }});
        SetBacicFastPay fastDeposit = (tempList != null && tempList.size() > 0) ? tempList.get(0) : null;
        log.info("????????????????????????, ????????????{}", JSON.toJSONString(fastDeposit));
        // ????????????????????????
        checkoutFastDepositPay(fastDeposit, params);
        // ???????????????????????????
        MbrDepositCond mbrDepositCond = mbrDepositCondService.getMbrDeposit(params.getAccountId());

        // ???????????????????????????
        MbrDepositCount count = checkDepositCount(account, Constants.EVNumber.zero);

        // ???????????????????????????????????????????????????
        boolean lock = mbrDepositLockLogService.companyUnpayLock(account);
        if (lock) {
            throw new R200Exception("???????????????????????????????????????.???");
        }

        // ??????deposit??????
        FundDeposit deposit = saveFastDepositPayFundDespoit(params, fastDeposit, mbrDepositCond, account);

        // ???????????????
        DepositPostScript script = new DepositPostScript();
        script.setSucceed(false);
        script.setError("??????????????????????????????????????????");
        // ????????????????????????
        if (fastDeposit != null && fastDeposit.getPaymentType() != null && fastDeposit.getPaymentType().intValue() == 15) {
            script = fastDepositOrder(deposit, fastDeposit, params.getSiteCode(), count);
            return script;
        }
        return script;
    }

    // ??????????????????????????????
    public void uploadFastDepositCertificate(PayParams params) {
        FundDeposit order = fundDepositMapper.selectByPrimaryKey(params.getOrderId());
        FastDepositWithdrawCertificate param = new FastDepositWithdrawCertificate();
        param.setAccountId(params.getAccountId());
        param.setDepositorderno(order.getOrderNo());
        FastDepositWithdrawCertificate certificate = certificateMapper.selectOne(param);
        certificate.setDepositPictures(String.join(",", params.getPictureList()));
        certificateMapper.updateByPrimaryKeySelective(certificate);
    }

    public MbrDepositCount checkDepositCount(MbrAccount account, int source){
        // ????????????????????????
        MbrDepositCount count = new MbrDepositCount();
        count.setAccountId(account.getId());
        count.setStartDay(DateUtil.getCurrentDate(FORMAT_10_DATE));
        count.setIsUpdateDepositLock(false);    // ??????

        // ??????????????????
        if(Integer.valueOf(Constants.EVNumber.one).equals(account.getDepositLock())){
            if(source == Constants.EVNumber.zero){
                throw new R200Exception("??????????????????????????????????????????");
            }
            count.setDepositLock(account.getDepositLock());
            return count;
        }

        // ??????????????????????????????????????????
        FundDeposit isDeposit = new FundDeposit();
        isDeposit.setStatus(Constants.EVNumber.one);
        isDeposit.setAccountId(account.getId());
        List<FundDeposit> list = fundDepositMapper.select(isDeposit);

        if(!Collections3.isEmpty(list) && list.size() > 0){
            // ??????????????????
            count.setIsSuccessDeposit(true);
            count.setDepositLock(Constants.EVNumber.zero);
            // ???????????????????????????
            List<MbrDepositCount> counts = mbrDepositCountMapper.select(count);
            // ???????????????
            if(!Collections3.isEmpty(counts) && counts.size() > 0){
                count.setNum(counts.get(0).getNum());
            }else{  // ???????????????????????????????????????
                count.setNum(Constants.EVNumber.zero);
                mbrDepositCountMapper.insert(count);
            }

            // ????????????????????????3??????????????????????????????
            if(!Collections3.isEmpty(counts) &&  Integer.valueOf(Constants.DEPOSIT_TIPS_NUM_EXT).compareTo(counts.get(0).getNum()) <= 0){
                count.setIsReminder(true);
                return count;
            }
            count.setIsReminder(false);
            return count;
        }
        // ?????????????????????????????????????????????
        else{
            // ??????????????????
            count.setIsSuccessDeposit(false);
            // ???????????????????????????
            List<MbrDepositCount> counts = mbrDepositCountMapper.select(count);
            // ???????????????
            if(!Collections3.isEmpty(counts) && counts.size() > 0){
                count.setNum(counts.get(0).getNum());
            }else{  // ???????????????????????????????????????
                count.setNum(Constants.EVNumber.zero);
                mbrDepositCountMapper.insert(count);
            }

            // ??????????????????4?????????????????????????????????????????????????????????
            // ??????????????????????????????????????????????????????????????????
            /*if(!Collections3.isEmpty(counts) &&  Integer.valueOf(Constants.DEPOSIT_LOCK_NUM_EXT).compareTo(counts.get(0).getNum()) == 0){
                count.setIsUpdateDepositLock(true);
            }

            // ??????????????????5
            if(!Collections3.isEmpty(counts) &&  Integer.valueOf(Constants.DEPOSIT_LOCK_NUM).compareTo(counts.get(0).getNum()) <= 0){
                if(source == Constants.EVNumber.zero){
                    throw new R200Exception("??????????????????????????????????????????");
                }
                count.setDepositLock(Constants.EVNumber.one);
                return count;
            }*/
        }
        count.setDepositLock(Constants.EVNumber.zero);
        count.setNumRest(Constants.DEPOSIT_LOCK_NUM - count.getNum());  // ????????????
        return count;
    }

    /**
     * ??????????????????????????????
     * @param deposit       ????????????
     * @param fastPay    ????????????-??????????????????
     * @param siteCode      siteCode
     * @param count         ????????????????????????
     * @return
     */
    private DepositPostScript fastDepositOrder(FundDeposit deposit, SetBacicFastPay fastPay, String siteCode, MbrDepositCount count) {
        // ??????????????????code
        String platFormCode = payMapper.findPayId(fastPay.getId());
        // ?????????
        DepositPostScript resultObj = new DepositPostScript();

        // ????????????
        if(PayConstants.SAASOPS_PAY_CODE.equals(platFormCode)){
            // ??????
            resultObj = saasopsPayService.fastDepositPlaceOrder(deposit, fastPay, siteCode);
            // deposit??????
            deposit.setPayOrderNo(deposit.getOrderNo());     // ??????????????????
            fundDepositMapper.insert(deposit);
            verifyService.addMbrVerifyDeposit(deposit, siteCode);
            mbrDepositCountMapper.updateCount(count);

            resultObj.setOrderId(deposit.getId());
            resultObj.setDepositPostscript(deposit.getDepositPostscript());
            // ??????????????????????????????
            if(count.getIsUpdateDepositLock()){
                MbrAccount mbr = new MbrAccount();
                mbr.setId(count.getAccountId());
                mbr.setDepositLock(Constants.EVNumber.one);
                mbrAccountMapper.updateByPrimaryKeySelective(mbr);
            }
        }

        return resultObj;
    }


    /**
     *  ????????????????????????
     * @param deposit       ????????????
     * @param sysDeposit    ????????????-??????????????????
     * @param siteCode      siteCode
     * @param count         ????????????????????????
     * @return
     */
    private DepositPostScript fastPay(FundDeposit deposit, SysDeposit sysDeposit, String siteCode, MbrDepositCount count) {
        // ??????????????????code
        String platFormCode = payMapper.findPayId(sysDeposit.getFastPayId());
        // ?????????
        DepositPostScript resultObj = new DepositPostScript();

        // ????????????
        if(PayConstants.SAASOPS_PAY_CODE.equals(platFormCode)){
            // ??????
            resultObj = saasopsPayService.placeOrder(deposit, sysDeposit, siteCode);
            // deposit??????
            deposit.setPayOrderNo(deposit.getOrderNo());     // ??????????????????
            fundDepositMapper.insert(deposit);
            verifyService.addMbrVerifyDeposit(deposit, siteCode);
            // ??????????????????????????????????????????
//            if(!count.getIsSuccessDeposit()){
                mbrDepositCountMapper.updateCount(count);
                // ??????????????????????????????
                if(count.getIsUpdateDepositLock()){
                    MbrAccount mbr = new MbrAccount();
                    mbr.setId(count.getAccountId());
                    mbr.setDepositLock(Constants.EVNumber.one);
                    mbrAccountMapper.updateByPrimaryKeySelective(mbr);
//                }
            }
        }

        return resultObj;
    }

    private SysDeposit getSysDeposit(PayParams params, MbrAccount account) {
        SysDeposit deposit = depositMapper.selectByPrimaryKey(params.getDepositId());
        SysDeposit sysDeposit = null;
        // ???????????????
        if (isNull(deposit.getFastPayId())) {
            sysDeposit = payMapper.findDepositByGroupIdAndDepositId(account.getGroupId(), params.getDepositId());
        }
        // ???????????????????????????????????????bankCode
        if (nonNull(deposit.getFastPayId())) {
            sysDeposit = payMapper.findFastPayDepositByGroupId(account.getGroupId(), params.getDepositId());
        }
        return sysDeposit;
    }

    private void checkoutSysDeposit(SysDeposit sysDeposit, PayParams params) {
        if (Objects.isNull(sysDeposit)) {
            throw new R200Exception("?????????????????????????????????");
        }
        if (sysDeposit.getMinAmout() != null && sysDeposit.getMaxAmout() != null) {
            if (sysDeposit.getMinAmout().compareTo(params.getFee()) == 1) {
                throw new R200Exception("??????????????????????????????");
            }
            if (params.getFee().compareTo(sysDeposit.getMaxAmout()) == 1) {
                throw new R200Exception("??????????????????????????????");
            }
        }
        if (StringUtils.isNotBlank(sysDeposit.getFixedAmount())) {
            if (!sysDeposit.getFixedAmount().contains(String.valueOf(params.getFee().intValue()))) {
                throw new R200Exception("?????????????????????????????????");
            }
        }
        if (sysDeposit.getDepositAmount().compareTo(sysDeposit.getDayMaxAmout()) == 1) {
            throw new R200Exception("????????????????????????????????????????????????????????????????????????");
        }
    }

    private void checkoutFastDepositPay(SetBacicFastPay fastPay, PayParams params) {
        if (Objects.isNull(fastPay)) {
            throw new R200Exception("?????????????????????????????????");
        }
        if (StringUtils.isNotBlank(fastPay.getFastDWAmount())) {
            if (!fastPay.getFastDWAmount().contains(String.valueOf(params.getFee().intValue()))) {
                throw new R200Exception("?????????????????????????????????");
            }
        }
        // ????????????????????????????????????
        BigDecimal todayMaxAmount = new BigDecimal(0);
        if (todayMaxAmount.compareTo(fastPay.getFastDWDayMaxAmout()) == 1) {
            throw new R200Exception("????????????????????????????????????????????????????????????????????????");
        }
    }

    private FundDeposit saveFundDespoit(PayParams params, SysDeposit sysDeposit, MbrDepositCond depositCond, MbrAccount account) {
        FundDeposit deposit = new FundDeposit();
        params.setOutTradeNo(new SnowFlake().nextId());
        deposit.setOrderNo(params.getOutTradeNo().toString());      // ?????????
        deposit.setCompanyPayId(params.getDepositId());             // ????????????ID
        deposit.setDepositUser(params.getUserName());               // ???????????????
//        deposit.setDepositUserAcc(params.getDepositUserAcc());      // ???????????????
        deposit.setAccountId(params.getAccountId());                // ??????id
        deposit.setFundSource(params.getFundSource());              // ??????????????? 0 PC???3 H5
        deposit.setIp(params.getIp());                              // ????????????IP

        deposit.setDepositAmount(params.getFee());                  // ????????????
        Byte feeEnable = nonNull(depositCond) && nonNull(depositCond.getFeeEnable()) ? depositCond.getFeeEnable() : 0;  // ?????????????????????
        // ?????????????????????
        BigDecimal handlingCharge = getActualArrival(params.getFee(), sysDeposit, feeEnable);
        deposit.setHandlingCharge(handlingCharge);                  // ?????????
        deposit.setActualArrival(deposit.getDepositAmount().add(deposit.getHandlingCharge()));  // ????????????

        deposit.setCreateUser(account.getLoginName());              // ????????????
        deposit.setLoginName(account.getLoginName());               // ?????????
        deposit.setGroupId(account.getGroupId());                   // ?????????

        deposit.setMark(Mark.offlinePay);                           // ???????????????1 ????????????
        deposit.setStatus(Status.apply);                            // ?????????2 ?????????
        deposit.setIsPayment(PaymentStatus.unPay);                  // ???????????? false ?????????
        deposit.setHandingback(Constants.Available.disable);        // ?????????????????????(???1 ??????????????? ??????0 ??????????????????????????????)"
        deposit.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME)); // ????????????
        deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME)); // ????????????
        deposit.setOrderPrefix(OrderConstants.FUND_ORDER_COMPANYDEPOSIT);   // ??????????????? CP ????????????
        deposit.setDepositPostscript(CommonUtil.genRandom(6, 6));   // ???????????????6?????????
        deposit.setVerifyCode(params.getVerifyCode());              // ?????????????????????code
        deposit.setSpreadCode(params.getSpreadCode());              // ???????????????
        return deposit;
    }

    private FundDeposit saveFastDepositPayFundDespoit(PayParams params, SetBacicFastPay fastPay, MbrDepositCond depositCond, MbrAccount account) {
        FundDeposit deposit = new FundDeposit();
        params.setOutTradeNo(new SnowFlake().nextId());
        deposit.setOrderNo(params.getOutTradeNo().toString());      // ?????????
        deposit.setCompanyPayId(params.getDepositId());             // ????????????ID
        deposit.setDepositUser(params.getUserName());               // ???????????????
        deposit.setAccountId(params.getAccountId());                // ??????id
        deposit.setFundSource(params.getFundSource());              // ??????????????? 0 PC???3 H5
        deposit.setIp(params.getIp());                              // ????????????IP

        deposit.setDepositAmount(params.getFee());                  // ????????????
        Byte feeEnable = nonNull(depositCond) && nonNull(depositCond.getFeeEnable()) ? depositCond.getFeeEnable() : 0;  // ?????????????????????
        // ???????????????????????????????????????????????????????????????
        //BigDecimal handlingCharge = getActualArrival(params.getFee(), sysDeposit, feeEnable);
        BigDecimal handlingCharge = new BigDecimal(0);
        deposit.setHandlingCharge(handlingCharge);                  // ?????????
        deposit.setActualArrival(deposit.getDepositAmount().add(deposit.getHandlingCharge()));  // ????????????

        deposit.setCreateUser(account.getLoginName());              // ????????????
        deposit.setLoginName(account.getLoginName());               // ?????????
        deposit.setGroupId(account.getGroupId());                   // ?????????

        deposit.setMark(Mark.offlinePay);                           // ???????????????1 ????????????
        deposit.setStatus(Status.apply);                            // ?????????2 ?????????
        deposit.setIsPayment(PaymentStatus.unPay);                  // ???????????? false ?????????
        deposit.setHandingback(Constants.Available.disable);        // ?????????????????????(???1 ??????????????? ??????0 ??????????????????????????????)"
        deposit.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME)); // ????????????
        deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME)); // ????????????
        deposit.setOrderPrefix(OrderConstants.FUND_ORDER_COMPANYDEPOSIT);   // ??????????????? CP ????????????
        deposit.setDepositPostscript(CommonUtil.genRandom(6, 6));   // ???????????????6?????????

        return deposit;
    }

    public BigDecimal getActualArrival(BigDecimal fee, SysDeposit sysDeposit, Byte feeEnable) {
        if (feeEnable == Constants.EVNumber.zero) {
            return BigDecimal.ZERO;
        }
        BigDecimal bigDecimal = BigDecimal.ZERO;
        if (nonNull(sysDeposit.getFastPayId())) {
            SetBacicFastPay fastPay = fastPayMapper.selectByPrimaryKey(sysDeposit.getFastPayId());
            sysDeposit.setFeeWay(fastPay.getFeeWay());
            sysDeposit.setFeeFixed(fastPay.getFeeFixed());
            sysDeposit.setFeeTop(fastPay.getFeeTop());
            sysDeposit.setFeeScale(fastPay.getFeeScale());
        }
        if (sysDeposit.getFeeWay() == Constants.EVNumber.one) {
            bigDecimal = sysDeposit.getFeeFixed();
        }
        if (sysDeposit.getFeeWay() == Constants.EVNumber.zero) {
            bigDecimal = CommonUtil.adjustScale(sysDeposit.getFeeScale().divide(
                    new BigDecimal(Constants.ONE_HUNDRED)).multiply(fee));
            if (nonNull(sysDeposit.getFeeTop()) && bigDecimal.compareTo(sysDeposit.getFeeTop()) == 1) {
                bigDecimal = sysDeposit.getFeeTop();
            }
        }
        return bigDecimal;
    }

    /**
     * 	???????????????????????????????????????????????????
     */
    public List<ActivityRuleDto> getJDepositActivityRules(Integer accountId, String terminal) {
        List<OprActActivity> activityList = operateMapper.findWebActList(
                null, accountId, StringUtil.isEmpty(terminal)?Constants.EVNumber.one:Byte.parseByte(terminal),
                Constants.EVNumber.zero, null, getCurrentDate(FORMAT_10_DATE), TOpActtmpl.depositSentCode, Constants.EVNumber.one);
        if (activityList != null && activityList.size() > 0) {
            JDepositSentDto jdepositRule = JSON.parseObject(activityList.get(0).getRule(), JDepositSentDto.class);
           return jdepositRule.getRuleScopeDtos().get(0).getActivityRuleDtos();
        }
        return null;
    }

    /**
     * 	?????????????????????????????????????????????????????? 
     * 
     * @param terminal
     * @param accountId
     * @return
     */
    public PayChoiceListDto getPzpayPictureUrl(String terminal, Integer accountId) {
        PayChoiceListDto choiceListDto = new PayChoiceListDto();
        
        List<OnlinePayPicture> pictures = payMapper.findOnlinePayListByAccountId(
                accountId, getTerminalStr(terminal), null, Boolean.TRUE);
        if (Collections3.isNotEmpty(pictures)) {

            Map<Integer, List<OnlinePayPicture>> onlinePayGroupingBys =
                    pictures.stream().collect(
                            Collectors.groupingBy(
                                    OnlinePayPicture::getPaymentType));
            List<OnlinePayPicture> wyList = Lists.newArrayList();
            // 1 QQ 2?????? 3?????? 4?????? 5????????? 6?????????????????????????????? 7???????????? 8???????????? (9???????????? 10BTP)  11 ?????????(??????)  12??????????????? 13 LBT 14????????? 17EBPAY 19 ????????????
            for (Integer payTypeKey : onlinePayGroupingBys.keySet()) {
                // ????????????????????????????????????/???????????????
                OnlinePayPicture onlinePayPicture = onlinePayGroupingBys.get(payTypeKey).get(0);
                List<OnlinePayPicture> onlinePayPictures;
                if(nonNull(onlinePayPicture) && Constants.EVNumber.zero == onlinePayPicture.getIsQueue() ){//?????????????????????
                    onlinePayPictures = onlinePayGroupingBys.get(payTypeKey);
                }else{//?????????????????????
                    onlinePayPictures = Lists.newArrayList(onlinePayGroupingBys.get(payTypeKey).get(0));
                }

                // qq
                if (payTypeKey == Constants.EVNumber.one) {
                    choiceListDto.setQqList(onlinePayPictures);

                }
                // ??????
                if (payTypeKey == Constants.EVNumber.two) {
                    choiceListDto.setWeChatList(onlinePayPictures);
                }
                // ??????
                if (payTypeKey == Constants.EVNumber.three) {
                    choiceListDto.setJdList(onlinePayPictures);
                }
                // ??????????????? 4??????  7???????????? 8????????????
                if (payTypeKey == Constants.EVNumber.four
                        || payTypeKey == Constants.EVNumber.seven
                        || payTypeKey == Constants.EVNumber.eight) {
                    onlinePayPictures.forEach(os -> os.setPayData(payMapper.findBankListByPayId(os.getPayId(), Integer.parseInt("1".equals(terminal)?"2":"1"))));
                    wyList.addAll(onlinePayPictures);
                    choiceListDto.setWyList(wyList);
                }
                // ?????????
                if (payTypeKey == Constants.EVNumber.five) {
                    choiceListDto.setAlipayList(onlinePayPictures);
                }
                // ???????????????
                if (payTypeKey ==  Constants.EVNumber.eleven) {
                    onlinePayPictures.forEach(os -> os.setPayData(payMapper.findBankListByPayId(os.getPayId(), Integer.parseInt("1".equals(terminal)?"2":"1"))));
                    choiceListDto.setBankList(onlinePayPictures);
                }

                // ?????????
                if (payTypeKey ==  Constants.EVNumber.fourteen) {
                    choiceListDto.setBankTransferList(onlinePayPictures);
                }

                // ????????????
                if (payTypeKey ==  Constants.EVNumber.sixteen) {
                    choiceListDto.setAggregationPayList(onlinePayPictures);
                }

                // ebpay??????
                if (payTypeKey == Constants.EVNumber.seventeen) {
                    choiceListDto.setEbpayList(onlinePayPictures);
                }

                // topay??????
                if (payTypeKey == Constants.EVNumber.eighteen) {
                    choiceListDto.setTopayList(onlinePayPictures);
                }

                // ????????????
                if (payTypeKey ==  Constants.EVNumber.nineteen) {
                    choiceListDto.setJscPayList(onlinePayPictures);
                }

            }

        }
        // ???????????????
        List<SysQrCode>  arList = sysQrCodeService.findQrCodeList(accountId);
        if(Objects.isNull(arList) || arList.size() == 0){
            choiceListDto.setQrCodeList(null);
        }else if( Constants.EVNumber.zero == arList.get(0).getIsQueue() ){//?????????????????????
            choiceListDto.setQrCodeList(arList);
        }else{//?????????????????????
            choiceListDto.setQrCodeList( Lists.newArrayList(arList.get(0)));
        }

        return choiceListDto;
    }

    private String getTerminalStr(String terminal) {
        return "1".equals(terminal) ? "3" : "0";
    }


    public void checkoutOnlinePay(PayParams params) {
        MbrAccount account = accountMapper.selectByPrimaryKey(params.getAccountId());
        sysSettingService.checkPayCondition(account,SystemConstants.DEPOSIT_CONDITION);

        Integer terminal = params.getTerminal();
        List<OnlinePayPicture> pictures = payMapper.findOnlinePayListByAccountId(params.getAccountId(),
                getTerminalStr(nonNull(terminal) ? terminal.toString() : org.apache.commons.lang.StringUtils.EMPTY),
                params.getOnlinePayId(), Boolean.FALSE);
        if (pictures.size() == 0) {
            throw new R200Exception("??????????????????????????????");
        }
        OnlinePayPicture picture = pictures.get(0);
        if (picture.getAmountType() == 0) {
            if (picture.getMinAmout().compareTo(params.getFee()) == 1) {
                throw new R200Exception("??????????????????????????????");
            }
            if (params.getFee().compareTo(picture.getMaxAmout()) == 1) {
                throw new R200Exception("??????????????????????????????");
            }
        }
        if (picture.getAmountType() == 1) {
            if (!picture.getFixedAmount().contains(String.valueOf(params.getFee().intValue()))) {
                throw new R200Exception("?????????????????????????????????");
            }
        }
        if (picture.getDepositAmount().compareTo(picture.getDayMaxAmout()) == 1) {
            throw new R200Exception("??????????????????????????????????????????,?????????????????????");
        }
    }

    private TPayLogo getTPayLogo() {
        TPayLogo logo = new TPayLogo();
        logo.setPaymentType(6);
        return payLogoMapper.selectOne(logo);
    }
}
