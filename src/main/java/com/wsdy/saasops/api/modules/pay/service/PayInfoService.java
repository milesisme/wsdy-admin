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
     * 	银行卡列表
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
                if(nonNull(deposit) && Constants.EVNumber.zero == deposit.getIsQueue()) {//平铺
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
        // 判断大开关是否开启
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
                response.setPaymentType(fs.getPaymentType());   // 类型
                response.setFastDWAmount(fs.getFastDWAmount()); // 固定金额
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

        // 判断当前极速取款当日是否达到最大金额
        /*if (true) {
            return null;
        }*/

        // 查询会员组是否开启存款姓名
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
                response.setPaymentType(fs.getPaymentType());   // 类型
                response.setFastDWAmount(fs.getFastDWAmount()); // 固定金额
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

        // 查询会员组是否开启存款姓名
        MbrDepositCond cond = mbrDepositCondService.getMbrDeposit(accountId);
        Integer depositName = Constants.EVNumber.zero;
        if (Objects.nonNull(cond) && cond.getDepositName() != null) {
            depositName = cond.getDepositName();
        }

        // 支付宝转卡标志 group
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
                    response.setPaymentType(fs.getPaymentType());   // 类型
                    response.setAlipayFlg(fs.getAlipayFlg());       // 宝转卡标志
                    response.setAmountType(fs.getAmountType());     // 限额模式
        	        response.setIsHot(fs.getIsHot());
        	        response.setIsRecommend(fs.getIsRecommend());
                    // 设置是否开启存款姓名
                    response.setDepositName("LBT".equals(fs.getPayCode()) ? depositName : Constants.EVNumber.zero);

                    if(PayConstants.TONGLUEYUN_CODE.equals(fs.getPlatfromCode())){
                        response.setUrlMethod(0);
                    } else if(PayConstants.DSDFPAY_CODE.equals(fs.getPlatfromCode())){
                        response.setUrlMethod(1);
                    } else if(PayConstants.BTPPAY_CODE.equals(fs.getPlatfromCode())){
                        response.setUrlMethod(0);
                    }
                    responseDtos.add(response);
                    if(Constants.EVNumber.zero == fs.getIsQueue()){//平铺
                        continue;
                    }else{// 排队
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
            if(deposit.getMark() == Constants.EVNumber.zero){//线上
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
                if(isNull(sysDeposit.getFastPayId())){//普通支付
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
        // 不是自动入款平台的银行卡，只有区间限额
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

    // 公司入款支付申请
    public DepositPostScript getDepositPostScript(PayParams params) {
        // 获得会员
        MbrAccount account = accountMapper.selectByPrimaryKey(params.getAccountId());
        // 入款完整校验：姓名+电话
        sysSettingService.checkPayCondition(account,SystemConstants.DEPOSIT_CONDITION);
        // 获得支付配置
        SysDeposit sysDeposit = getSysDeposit(params, account);
        // 支付配置校验
        checkoutSysDeposit(sysDeposit, params);
        // 获得会员组入款设置
        MbrDepositCond mbrDepositCond = mbrDepositCondService.getMbrDeposit(params.getAccountId());

        // 校验存款锁定和计数
        MbrDepositCount count = checkDepositCount(account, Constants.EVNumber.zero);

        // 效验存款锁定规则，是否应该锁定会员
        boolean lock = mbrDepositLockLogService.companyUnpayLock(account);
        if (lock) {
            throw new R200Exception("您已被限制存款，请联系客服.！");
        }

        // 创建deposit对象
        FundDeposit deposit = saveFundDespoit(params, sysDeposit, mbrDepositCond, account);

        // 获取返回值
        DepositPostScript script;
        // 普通银行卡入款类型
        if (Objects.isNull(sysDeposit.getFastPayId())) {
            // deposit存库
            fundDepositMapper.insert(deposit);
            verifyService.addMbrVerifyDeposit(deposit, params.getSiteCode());
            // 查询返回值
            script = fundMapper.findOfflineDepositInfo(deposit.getId());
            return script;
        }
        // 自动入款类型
        script =  fastPay(deposit, sysDeposit, params.getSiteCode(),count);
        return script;
    }

    // 极速存款支付申请
    public DepositPostScript getFastDepositPostScript(PayParams params) {
        // 获得会员
        MbrAccount account = accountMapper.selectByPrimaryKey(params.getAccountId());
        // 入款完整校验：姓名+电话
        sysSettingService.checkPayCondition(account,SystemConstants.DEPOSIT_CONDITION);
        // 获得快捷支付配置
        List<SetBacicFastPay> tempList = payMapper.findBasicFastPay(new SetBacicFastPay(){{
            setAvailable(Constants.EVNumber.one);
            setId(params.getDepositId());
        }});
        SetBacicFastPay fastDeposit = (tempList != null && tempList.size() > 0) ? tempList.get(0) : null;
        log.info("极速存款支付申请, 渠道配置{}", JSON.toJSONString(fastDeposit));
        // 快捷支付配置校验
        checkoutFastDepositPay(fastDeposit, params);
        // 获得会员组入款设置
        MbrDepositCond mbrDepositCond = mbrDepositCondService.getMbrDeposit(params.getAccountId());

        // 校验存款锁定和计数
        MbrDepositCount count = checkDepositCount(account, Constants.EVNumber.zero);

        // 效验存款锁定规则，是否应该锁定会员
        boolean lock = mbrDepositLockLogService.companyUnpayLock(account);
        if (lock) {
            throw new R200Exception("您已被限制存款，请联系客服.！");
        }

        // 创建deposit对象
        FundDeposit deposit = saveFastDepositPayFundDespoit(params, fastDeposit, mbrDepositCond, account);

        // 获取返回值
        DepositPostScript script = new DepositPostScript();
        script.setSucceed(false);
        script.setError("渠道错误，该渠道并非极速存款");
        // 判断是否极速存款
        if (fastDeposit != null && fastDeposit.getPaymentType() != null && fastDeposit.getPaymentType().intValue() == 15) {
            script = fastDepositOrder(deposit, fastDeposit, params.getSiteCode(), count);
            return script;
        }
        return script;
    }

    // 极速存款支付上传凭证
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
        // 会员今日计数对象
        MbrDepositCount count = new MbrDepositCount();
        count.setAccountId(account.getId());
        count.setStartDay(DateUtil.getCurrentDate(FORMAT_10_DATE));
        count.setIsUpdateDepositLock(false);    // 默认

        // 校验锁定状态
        if(Integer.valueOf(Constants.EVNumber.one).equals(account.getDepositLock())){
            if(source == Constants.EVNumber.zero){
                throw new R200Exception("您已被限制存款，请联系客服！");
            }
            count.setDepositLock(account.getDepositLock());
            return count;
        }

        // 查询会员是否存在成功的入款单
        FundDeposit isDeposit = new FundDeposit();
        isDeposit.setStatus(Constants.EVNumber.one);
        isDeposit.setAccountId(account.getId());
        List<FundDeposit> list = fundDepositMapper.select(isDeposit);

        if(!Collections3.isEmpty(list) && list.size() > 0){
            // 用于外层判断
            count.setIsSuccessDeposit(true);
            count.setDepositLock(Constants.EVNumber.zero);
            // 查询当日的入款计数
            List<MbrDepositCount> counts = mbrDepositCountMapper.select(count);
            // 有存款计数
            if(!Collections3.isEmpty(counts) && counts.size() > 0){
                count.setNum(counts.get(0).getNum());
            }else{  // 当日无计数，则插入一条记录
                count.setNum(Constants.EVNumber.zero);
                mbrDepositCountMapper.insert(count);
            }

            // 已有计数大于等于3，则有弹窗提示找客服
            if(!Collections3.isEmpty(counts) &&  Integer.valueOf(Constants.DEPOSIT_TIPS_NUM_EXT).compareTo(counts.get(0).getNum()) <= 0){
                count.setIsReminder(true);
                return count;
            }
            count.setIsReminder(false);
            return count;
        }
        // 不存在成功入款单，校验入款计数
        else{
            // 用于外层判断
            count.setIsSuccessDeposit(false);
            // 查询当日的入款计数
            List<MbrDepositCount> counts = mbrDepositCountMapper.select(count);
            // 有存款计数
            if(!Collections3.isEmpty(counts) && counts.size() > 0){
                count.setNum(counts.get(0).getNum());
            }else{  // 当日无计数，则插入一条记录
                count.setNum(Constants.EVNumber.zero);
                mbrDepositCountMapper.insert(count);
            }

            // 当下计数等于4，则本次提单成功后，需更新会员锁定状态
            // 注释原有的存款防刷，直接使用新的存款锁定规则
            /*if(!Collections3.isEmpty(counts) &&  Integer.valueOf(Constants.DEPOSIT_LOCK_NUM_EXT).compareTo(counts.get(0).getNum()) == 0){
                count.setIsUpdateDepositLock(true);
            }

            // 计数大于等于5
            if(!Collections3.isEmpty(counts) &&  Integer.valueOf(Constants.DEPOSIT_LOCK_NUM).compareTo(counts.get(0).getNum()) <= 0){
                if(source == Constants.EVNumber.zero){
                    throw new R200Exception("您已被限制存款，请联系客服！");
                }
                count.setDepositLock(Constants.EVNumber.one);
                return count;
            }*/
        }
        count.setDepositLock(Constants.EVNumber.zero);
        count.setNumRest(Constants.DEPOSIT_LOCK_NUM - count.getNum());  // 剩余次数
        return count;
    }

    /**
     * 极速存款发起订单请求
     * @param deposit       存款信息
     * @param fastPay    支付配置-支付平台信息
     * @param siteCode      siteCode
     * @param count         存款计数校验对象
     * @return
     */
    private DepositPostScript fastDepositOrder(FundDeposit deposit, SetBacicFastPay fastPay, String siteCode, MbrDepositCount count) {
        // 获取支付平台code
        String platFormCode = payMapper.findPayId(fastPay.getId());
        // 返回值
        DepositPostScript resultObj = new DepositPostScript();

        // 支付网关
        if(PayConstants.SAASOPS_PAY_CODE.equals(platFormCode)){
            // 提单
            resultObj = saasopsPayService.fastDepositPlaceOrder(deposit, fastPay, siteCode);
            // deposit存库
            deposit.setPayOrderNo(deposit.getOrderNo());     // 第三方订单号
            fundDepositMapper.insert(deposit);
            verifyService.addMbrVerifyDeposit(deposit, siteCode);
            mbrDepositCountMapper.updateCount(count);

            resultObj.setOrderId(deposit.getId());
            resultObj.setDepositPostscript(deposit.getDepositPostscript());
            // 更新会员存款锁定状态
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
     *  自动入款类型提单
     * @param deposit       存款信息
     * @param sysDeposit    支付配置-支付平台信息
     * @param siteCode      siteCode
     * @param count         存款计数校验对象
     * @return
     */
    private DepositPostScript fastPay(FundDeposit deposit, SysDeposit sysDeposit, String siteCode, MbrDepositCount count) {
        // 获取支付平台code
        String platFormCode = payMapper.findPayId(sysDeposit.getFastPayId());
        // 返回值
        DepositPostScript resultObj = new DepositPostScript();

        // 支付网关
        if(PayConstants.SAASOPS_PAY_CODE.equals(platFormCode)){
            // 提单
            resultObj = saasopsPayService.placeOrder(deposit, sysDeposit, siteCode);
            // deposit存库
            deposit.setPayOrderNo(deposit.getOrderNo());     // 第三方订单号
            fundDepositMapper.insert(deposit);
            verifyService.addMbrVerifyDeposit(deposit, siteCode);
            // 不存在成功入款单，则更新计数
//            if(!count.getIsSuccessDeposit()){
                mbrDepositCountMapper.updateCount(count);
                // 更新会员存款锁定状态
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
        // 普通银行卡
        if (isNull(deposit.getFastPayId())) {
            sysDeposit = payMapper.findDepositByGroupIdAndDepositId(account.getGroupId(), params.getDepositId());
        }
        // 快捷银行卡：含关联银卡信息bankCode
        if (nonNull(deposit.getFastPayId())) {
            sysDeposit = payMapper.findFastPayDepositByGroupId(account.getGroupId(), params.getDepositId());
        }
        return sysDeposit;
    }

    private void checkoutSysDeposit(SysDeposit sysDeposit, PayParams params) {
        if (Objects.isNull(sysDeposit)) {
            throw new R200Exception("此银行卡不接受会员充值");
        }
        if (sysDeposit.getMinAmout() != null && sysDeposit.getMaxAmout() != null) {
            if (sysDeposit.getMinAmout().compareTo(params.getFee()) == 1) {
                throw new R200Exception("小于单笔最小充值额度");
            }
            if (params.getFee().compareTo(sysDeposit.getMaxAmout()) == 1) {
                throw new R200Exception("大于单笔最大充值额度");
            }
        }
        if (StringUtils.isNotBlank(sysDeposit.getFixedAmount())) {
            if (!sysDeposit.getFixedAmount().contains(String.valueOf(params.getFee().intValue()))) {
                throw new R200Exception("存款金额不在可选金额中");
            }
        }
        if (sysDeposit.getDepositAmount().compareTo(sysDeposit.getDayMaxAmout()) == 1) {
            throw new R200Exception("此银行卡已经达到单日最大限额，请选择其他银行支付");
        }
    }

    private void checkoutFastDepositPay(SetBacicFastPay fastPay, PayParams params) {
        if (Objects.isNull(fastPay)) {
            throw new R200Exception("此银行卡不接受会员充值");
        }
        if (StringUtils.isNotBlank(fastPay.getFastDWAmount())) {
            if (!fastPay.getFastDWAmount().contains(String.valueOf(params.getFee().intValue()))) {
                throw new R200Exception("存款金额不在可选金额中");
            }
        }
        // 判断是否打到每日最大限额
        BigDecimal todayMaxAmount = new BigDecimal(0);
        if (todayMaxAmount.compareTo(fastPay.getFastDWDayMaxAmout()) == 1) {
            throw new R200Exception("此银行卡已经达到单日最大限额，请选择其他银行支付");
        }
    }

    private FundDeposit saveFundDespoit(PayParams params, SysDeposit sysDeposit, MbrDepositCond depositCond, MbrAccount account) {
        FundDeposit deposit = new FundDeposit();
        params.setOutTradeNo(new SnowFlake().nextId());
        deposit.setOrderNo(params.getOutTradeNo().toString());      // 订单号
        deposit.setCompanyPayId(params.getDepositId());             // 支付渠道ID
        deposit.setDepositUser(params.getUserName());               // 存款人姓名
//        deposit.setDepositUserAcc(params.getDepositUserAcc());      // 存款人卡号
        deposit.setAccountId(params.getAccountId());                // 会员id
        deposit.setFundSource(params.getFundSource());              // 存款来源： 0 PC，3 H5
        deposit.setIp(params.getIp());                              // 客户请求IP

        deposit.setDepositAmount(params.getFee());                  // 存款金额
        Byte feeEnable = nonNull(depositCond) && nonNull(depositCond.getFeeEnable()) ? depositCond.getFeeEnable() : 0;  // 是否返还手续费
        // 返还手续费计算
        BigDecimal handlingCharge = getActualArrival(params.getFee(), sysDeposit, feeEnable);
        deposit.setHandlingCharge(handlingCharge);                  // 手续费
        deposit.setActualArrival(deposit.getDepositAmount().add(deposit.getHandlingCharge()));  // 实际到账

        deposit.setCreateUser(account.getLoginName());              // 下单会员
        deposit.setLoginName(account.getLoginName());               // 会员名
        deposit.setGroupId(account.getGroupId());                   // 会员组

        deposit.setMark(Mark.offlinePay);                           // 存款类型：1 公司入款
        deposit.setStatus(Status.apply);                            // 状态：2 待处理
        deposit.setIsPayment(PaymentStatus.unPay);                  // 付款状态 false 未支付
        deposit.setHandingback(Constants.Available.disable);        // 手续费还返默认(为1 扣（减少） ，为0 手续费已处理（增加）)"
        deposit.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME)); // 创建时间
        deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME)); // 修改时间
        deposit.setOrderPrefix(OrderConstants.FUND_ORDER_COMPANYDEPOSIT);   // 订单前缀： CP 公司入款
        deposit.setDepositPostscript(CommonUtil.genRandom(6, 6));   // 存款附言：6位随机
        deposit.setVerifyCode(params.getVerifyCode());              // 多语言站点使用code
        deposit.setSpreadCode(params.getSpreadCode());              // 代理推广码
        return deposit;
    }

    private FundDeposit saveFastDepositPayFundDespoit(PayParams params, SetBacicFastPay fastPay, MbrDepositCond depositCond, MbrAccount account) {
        FundDeposit deposit = new FundDeposit();
        params.setOutTradeNo(new SnowFlake().nextId());
        deposit.setOrderNo(params.getOutTradeNo().toString());      // 订单号
        deposit.setCompanyPayId(params.getDepositId());             // 支付渠道ID
        deposit.setDepositUser(params.getUserName());               // 存款人姓名
        deposit.setAccountId(params.getAccountId());                // 会员id
        deposit.setFundSource(params.getFundSource());              // 存款来源： 0 PC，3 H5
        deposit.setIp(params.getIp());                              // 客户请求IP

        deposit.setDepositAmount(params.getFee());                  // 存款金额
        Byte feeEnable = nonNull(depositCond) && nonNull(depositCond.getFeeEnable()) ? depositCond.getFeeEnable() : 0;  // 是否返还手续费
        // 返还手续费计算，极速存款目前没有返还手续费
        //BigDecimal handlingCharge = getActualArrival(params.getFee(), sysDeposit, feeEnable);
        BigDecimal handlingCharge = new BigDecimal(0);
        deposit.setHandlingCharge(handlingCharge);                  // 手续费
        deposit.setActualArrival(deposit.getDepositAmount().add(deposit.getHandlingCharge()));  // 实际到账

        deposit.setCreateUser(account.getLoginName());              // 下单会员
        deposit.setLoginName(account.getLoginName());               // 会员名
        deposit.setGroupId(account.getGroupId());                   // 会员组

        deposit.setMark(Mark.offlinePay);                           // 存款类型：1 公司入款
        deposit.setStatus(Status.apply);                            // 状态：2 待处理
        deposit.setIsPayment(PaymentStatus.unPay);                  // 付款状态 false 未支付
        deposit.setHandingback(Constants.Available.disable);        // 手续费还返默认(为1 扣（减少） ，为0 手续费已处理（增加）)"
        deposit.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME)); // 创建时间
        deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME)); // 修改时间
        deposit.setOrderPrefix(OrderConstants.FUND_ORDER_COMPANYDEPOSIT);   // 订单前缀： CP 公司入款
        deposit.setDepositPostscript(CommonUtil.genRandom(6, 6));   // 存款附言：6位随机

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
     * 	获取会员能参与的首个存就送活动规则
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
     * 	获取支付类型相对的支付方式及图片路径 
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
            // 1 QQ 2微信 3京东 4网银 5支付宝 6同略云（网关银行卡） 7快捷支付 8银联扫码 (9风云聚合 10BTP)  11 银行卡(跳转)  12个人二维码 13 LBT 14卡转卡 17EBPAY 19 极速存款
            for (Integer payTypeKey : onlinePayGroupingBys.keySet()) {
                // 获取支付渠道，并根据平铺/排序取数据
                OnlinePayPicture onlinePayPicture = onlinePayGroupingBys.get(payTypeKey).get(0);
                List<OnlinePayPicture> onlinePayPictures;
                if(nonNull(onlinePayPicture) && Constants.EVNumber.zero == onlinePayPicture.getIsQueue() ){//平铺，获取全部
                    onlinePayPictures = onlinePayGroupingBys.get(payTypeKey);
                }else{//排队，取第一个
                    onlinePayPictures = Lists.newArrayList(onlinePayGroupingBys.get(payTypeKey).get(0));
                }

                // qq
                if (payTypeKey == Constants.EVNumber.one) {
                    choiceListDto.setQqList(onlinePayPictures);

                }
                // 微信
                if (payTypeKey == Constants.EVNumber.two) {
                    choiceListDto.setWeChatList(onlinePayPictures);
                }
                // 京东
                if (payTypeKey == Constants.EVNumber.three) {
                    choiceListDto.setJdList(onlinePayPictures);
                }
                // 前端网银： 4网银  7快捷支付 8银联扫码
                if (payTypeKey == Constants.EVNumber.four
                        || payTypeKey == Constants.EVNumber.seven
                        || payTypeKey == Constants.EVNumber.eight) {
                    onlinePayPictures.forEach(os -> os.setPayData(payMapper.findBankListByPayId(os.getPayId(), Integer.parseInt("1".equals(terminal)?"2":"1"))));
                    wyList.addAll(onlinePayPictures);
                    choiceListDto.setWyList(wyList);
                }
                // 支付宝
                if (payTypeKey == Constants.EVNumber.five) {
                    choiceListDto.setAlipayList(onlinePayPictures);
                }
                // 银行卡跳转
                if (payTypeKey ==  Constants.EVNumber.eleven) {
                    onlinePayPictures.forEach(os -> os.setPayData(payMapper.findBankListByPayId(os.getPayId(), Integer.parseInt("1".equals(terminal)?"2":"1"))));
                    choiceListDto.setBankList(onlinePayPictures);
                }

                // 卡转卡
                if (payTypeKey ==  Constants.EVNumber.fourteen) {
                    choiceListDto.setBankTransferList(onlinePayPictures);
                }

                // 聚合支付
                if (payTypeKey ==  Constants.EVNumber.sixteen) {
                    choiceListDto.setAggregationPayList(onlinePayPictures);
                }

                // ebpay支付
                if (payTypeKey == Constants.EVNumber.seventeen) {
                    choiceListDto.setEbpayList(onlinePayPictures);
                }

                // topay支付
                if (payTypeKey == Constants.EVNumber.eighteen) {
                    choiceListDto.setTopayList(onlinePayPictures);
                }

                // 极速支付
                if (payTypeKey ==  Constants.EVNumber.nineteen) {
                    choiceListDto.setJscPayList(onlinePayPictures);
                }

            }

        }
        // 个人二维码
        List<SysQrCode>  arList = sysQrCodeService.findQrCodeList(accountId);
        if(Objects.isNull(arList) || arList.size() == 0){
            choiceListDto.setQrCodeList(null);
        }else if( Constants.EVNumber.zero == arList.get(0).getIsQueue() ){//平铺，获取全部
            choiceListDto.setQrCodeList(arList);
        }else{//排队，取第一个
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
            throw new R200Exception("此支付不接受会员充值");
        }
        OnlinePayPicture picture = pictures.get(0);
        if (picture.getAmountType() == 0) {
            if (picture.getMinAmout().compareTo(params.getFee()) == 1) {
                throw new R200Exception("小于单笔最小充值额度");
            }
            if (params.getFee().compareTo(picture.getMaxAmout()) == 1) {
                throw new R200Exception("大于单笔最大充值额度");
            }
        }
        if (picture.getAmountType() == 1) {
            if (!picture.getFixedAmount().contains(String.valueOf(params.getFee().intValue()))) {
                throw new R200Exception("存款金额不在可选金额中");
            }
        }
        if (picture.getDepositAmount().compareTo(picture.getDayMaxAmout()) == 1) {
            throw new R200Exception("该支付已达到单日充值最大限额,请选择其它支付");
        }
    }

    private TPayLogo getTPayLogo() {
        TPayLogo logo = new TPayLogo();
        logo.setPaymentType(6);
        return payLogoMapper.selectOne(logo);
    }
}
