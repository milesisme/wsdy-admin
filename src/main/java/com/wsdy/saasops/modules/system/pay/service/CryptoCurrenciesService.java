package com.wsdy.saasops.modules.system.pay.service;


import static com.wsdy.saasops.common.constants.Constants.SYSTEM_USER;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.wsdy.saasops.api.modules.pay.service.PayInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.wsdy.saasops.api.constants.PayConstants;
import com.wsdy.saasops.api.modules.pay.dto.CrPayDto;
import com.wsdy.saasops.api.modules.pay.dto.CrPayLogoDto;
import com.wsdy.saasops.api.modules.pay.dto.PayParams;
import com.wsdy.saasops.api.modules.pay.dto.evellet.CommonEvelletResponse;
import com.wsdy.saasops.api.modules.pay.dto.evellet.EvelletPayPushCallbackDto;
import com.wsdy.saasops.api.modules.pay.dto.evellet.EvelletPayQueryAddressDto;
import com.wsdy.saasops.api.modules.pay.dto.evellet.EvelletPayTradeRequestDto;
import com.wsdy.saasops.api.modules.pay.dto.evellet.EvelletPayTransferRequestDto;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.ASCIIUtils;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.api.utils.MD5;
import com.wsdy.saasops.api.utils.OkHttpUtils;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.modules.base.dao.BaseBankMapper;
import com.wsdy.saasops.modules.base.entity.BaseBank;
import com.wsdy.saasops.modules.fund.dao.FundDepositMapper;
import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.fund.entity.FundMerchantPay;
import com.wsdy.saasops.modules.fund.service.FundDepositService;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrGroupMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrDepositCond;
import com.wsdy.saasops.modules.member.entity.MbrGroup;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.member.service.MbrDepositCondService;
import com.wsdy.saasops.modules.member.service.MbrVerifyService;
import com.wsdy.saasops.modules.system.pay.dao.SetBasicCryptoCurrenciesGroupMapper;
import com.wsdy.saasops.modules.system.pay.dao.SetBasicSysCryptoCurrenciesMapper;
import com.wsdy.saasops.modules.system.pay.dto.AllotDto;
import com.wsdy.saasops.modules.system.pay.entity.SetBasicCryptoCurrenciesBank;
import com.wsdy.saasops.modules.system.pay.entity.SetBasicCryptoCurrenciesGroup;
import com.wsdy.saasops.modules.system.pay.entity.SetBasicSysCryptoCurrencies;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class CryptoCurrenciesService {
    @Autowired
    private BaseBankMapper baseBankMapper;
    @Autowired
    private SetBasicSysCryptoCurrenciesMapper setBasicSysCryptoCurrenciesMapper;
    @Autowired
    private SetBasicCryptoCurrenciesGroupMapper setBasicCryptoCurrenciesGroupMapper;

    @Autowired
    private MbrGroupMapper groupMapper;
    @Autowired
    private MbrAccountMapper mbrAccountMapper;
    @Value("${evellet.url}")
    private String evelletUrl;
    @Value("${panzi.callback.url}")
    private String panziCallbackUrl;
    @Value("${v2.intranet.callback.url}")
    private String intranetUrl;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private MbrDepositCondService mbrDepositCondService;
    @Autowired
    private FundDepositMapper fundDepositMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private FundDepositService fundDepositService;
    @Autowired
    private MbrAccountLogService logService;
    @Autowired
    private MbrVerifyService verifyService;

    public Map<String, List<BaseBank>> crTypeList() {
        BaseBank baseBank = new BaseBank();
        baseBank.setWDEnable((byte) 9);
        List<BaseBank> list = baseBankMapper.select(baseBank);
        if (Collections3.isEmpty(list)) {
            return null;
        }
        Map<String, List<BaseBank>> groupBy = list.stream().collect(
                Collectors.groupingBy(
                        BaseBank::getBankCode));
        return groupBy;
    }

    /**
     * 加密货币支付列表
     *
     * @param setBasicSysCryptoCurrencies
     * @return
     */
    public List<SetBasicSysCryptoCurrencies> crPayList(SetBasicSysCryptoCurrencies setBasicSysCryptoCurrencies) {
        setBasicSysCryptoCurrencies.setIsDelete(Constants.EVNumber.zero);
        // 获取加密货币pay列表
        List<SetBasicSysCryptoCurrencies> crList = setBasicSysCryptoCurrenciesMapper.queryList(setBasicSysCryptoCurrencies);
        // 处理会员组
        if (Collections3.isNotEmpty(crList)) {
            crList.forEach(q -> {
                q.setGroupList(setBasicSysCryptoCurrenciesMapper.findGroupById(q.getId()));
                List<Integer> groupIds = q.getGroupList().stream().map(MbrGroup::getId).collect(Collectors.toList());
                q.setGroupIds(groupIds);
            });
        }
        return crList;
    }

    public void crSave(SetBasicSysCryptoCurrencies setBasicSysCryptoCurrencies, String userName) {
        // 获得币种和协议
        BaseBank bank = baseBankMapper.selectByPrimaryKey(setBasicSysCryptoCurrencies.getBankIds().get(0));
        // 同币种同协议的可用状态的加密货币只能有一个
        SetBasicSysCryptoCurrencies setBasicSysCryptoCurrencies1 = new SetBasicSysCryptoCurrencies();
        setBasicSysCryptoCurrencies1.setCurrencyCode(bank.getBankCode());
        setBasicSysCryptoCurrencies1.setCurrencyProtocol(bank.getCategory());
        setBasicSysCryptoCurrencies1.setIsDelete(Constants.EVNumber.zero);
        List<SetBasicSysCryptoCurrencies> list = setBasicSysCryptoCurrenciesMapper.select(setBasicSysCryptoCurrencies1);
        if (Objects.nonNull(list) && list.size() > 0) {
            throw new R200Exception("同币种同协议仅允许存在一条记录！");
        }

        // 保存 SetBasicSysCryptoCurrencies
        setBasicSysCryptoCurrencies.setIsDelete(Constants.EVNumber.zero);
        setBasicSysCryptoCurrencies.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        setBasicSysCryptoCurrencies.setCreateUser(userName);
        setBasicSysCryptoCurrencies.setDepositAmount(BigDecimal.ZERO);
        setBasicSysCryptoCurrencies.setShowName(bank.getBankCode() + "(" + bank.getCategory() + ")");
        setBasicSysCryptoCurrencies.setName(bank.getBankCode() + "-" + bank.getCategory());
        setBasicSysCryptoCurrencies.setCurrencyCode(bank.getBankCode());
        setBasicSysCryptoCurrencies.setCurrencyProtocol(bank.getCategory());
        setBasicSysCryptoCurrencies.setIsHot(false);
        setBasicSysCryptoCurrencies.setIsRecommend(false);
        setBasicSysCryptoCurrenciesMapper.insert(setBasicSysCryptoCurrencies);
        // 保存 SetBasicCryptoCurrenciesBank
        insertCrBank(setBasicSysCryptoCurrencies);
        // 保存 组关系
        insertCrGroup(setBasicSysCryptoCurrencies);
        // 操作日志
        logService.crSave(setBasicSysCryptoCurrencies);
    }


    private void insertCrBank(SetBasicSysCryptoCurrencies setBasicSysCryptoCurrencies) {
        List<Integer> bankIds = setBasicSysCryptoCurrencies.getBankIds();
        List<SetBasicCryptoCurrenciesBank> crBanks = bankIds.stream().map(id -> {
            SetBasicCryptoCurrenciesBank crBank = new SetBasicCryptoCurrenciesBank();
            crBank.setCurrenciesId(setBasicSysCryptoCurrencies.getId());
            crBank.setBankId(id);
            return crBank;
        }).collect(Collectors.toList());
        setBasicSysCryptoCurrenciesMapper.batchInsertCrBank(crBanks);
    }

    private void insertCrGroup(SetBasicSysCryptoCurrencies setBasicSysCryptoCurrencies) {
        List<Integer> groupIds = setBasicSysCryptoCurrencies.getGroupIds();
        List<SetBasicCryptoCurrenciesGroup> crGroupsIsQueue = setBasicSysCryptoCurrenciesMapper.getCrGroupIsQueue();
        List<SetBasicCryptoCurrenciesGroup> qrCodeGroups = groupIds.stream().map(id -> {
            SetBasicCryptoCurrenciesGroup crGroup = new SetBasicCryptoCurrenciesGroup();
            crGroup.setCurrenciesId(setBasicSysCryptoCurrencies.getId());
            crGroup.setGroupId(id);
            crGroup.setSort(Constants.EVNumber.zero);
            crGroup.setIsQueue(getIsQueue(id, crGroupsIsQueue));
            return crGroup;
        }).collect(Collectors.toList());
        setBasicSysCryptoCurrenciesMapper.batchInsertCrGroup(qrCodeGroups);
    }

    private Integer getIsQueue(Integer groupId, List<SetBasicCryptoCurrenciesGroup> crGroupsIsQueue) {
        //设置排队/平铺
        Optional<SetBasicCryptoCurrenciesGroup> groupRelationOptional = crGroupsIsQueue.stream().filter(groupIsQueue ->
                groupId.equals(groupIsQueue.getGroupId()))
                .findAny();
        if (groupRelationOptional.isPresent()) {
            return groupRelationOptional.get().getIsQueue();
        } else {//默认排队
            return Constants.EVNumber.one;
        }
    }

    public void crUpdate(SetBasicSysCryptoCurrencies setBasicSysCryptoCurrencies, String userName, String ip) {
        SetBasicSysCryptoCurrencies crOld = setBasicSysCryptoCurrenciesMapper.selectByPrimaryKey(setBasicSysCryptoCurrencies.getId());
        // 删除旧的bank关联
        setBasicSysCryptoCurrenciesMapper.deleteCrBank(crOld.getId());
        SetBasicCryptoCurrenciesGroup crGroup = new SetBasicCryptoCurrenciesGroup();
        crGroup.setCurrenciesId(setBasicSysCryptoCurrencies.getId());
        // 删除旧的group关联
        setBasicSysCryptoCurrenciesMapper.deleteCrGroup(crGroup);

        // 插入新的bank关联
        insertCrBank(setBasicSysCryptoCurrencies);
        // 插入新的group关联
        insertCrGroup(setBasicSysCryptoCurrencies);

        setBasicSysCryptoCurrencies.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        setBasicSysCryptoCurrencies.setModifyUser(userName);
        setBasicSysCryptoCurrenciesMapper.updateByPrimaryKeySelective(setBasicSysCryptoCurrencies);

        // 记录操作日志 TODO
        logService.crUpdate(crOld);
    }


    public void crDelete(Integer id, String userName, String ip) {
        SetBasicSysCryptoCurrencies crOld = setBasicSysCryptoCurrenciesMapper.selectByPrimaryKey(id);
        if (Objects.isNull(crOld)) {
            throw new R200Exception("该支付渠道不存在");
        }
        if (crOld.getAvailable() == Constants.EVNumber.one) {
            throw new R200Exception("禁用状态才能删除");
        }

        // 如果不存在入款单，则物理删除, 否则逻辑删除
        FundDeposit deposit = new FundDeposit();
        deposit.setCrId(id);
        List<FundDeposit> list = fundDepositMapper.select(deposit);
        if (Collections3.isEmpty(list)) {
            setBasicSysCryptoCurrenciesMapper.deleteByPrimaryKey(id);
        } else {
            // 更新删除标志
            SetBasicSysCryptoCurrencies crNew = new SetBasicSysCryptoCurrencies();
            crNew.setId(id);
            crNew.setIsDelete(Constants.EVNumber.one);
            crNew.setModifyUser(userName);
            crNew.setModifyTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
            setBasicSysCryptoCurrenciesMapper.updateByPrimaryKeySelective(crNew);
        }

        // 删除旧的group关联
        SetBasicCryptoCurrenciesGroup crGroup = new SetBasicCryptoCurrenciesGroup();
        crGroup.setCurrenciesId(id);
        setBasicSysCryptoCurrenciesMapper.deleteCrGroup(crGroup);

        // 记录操作日志 TODO
        logService.crDelete(crOld);
    }

    public void crUpdateStatus(SetBasicSysCryptoCurrencies setBasicSysCryptoCurrencies, String userName, String ip) {
        // isHot，isRecommend 只有一个为true
        if (setBasicSysCryptoCurrencies.getIsHot() != null && setBasicSysCryptoCurrencies.getIsRecommend() != null
                && setBasicSysCryptoCurrencies.getIsHot() && setBasicSysCryptoCurrencies.getIsRecommend()) {
            throw new R200Exception("操作失败，推荐和热门按钮只允许同时勾选一个！");
        }
        SetBasicSysCryptoCurrencies crOld = setBasicSysCryptoCurrenciesMapper.selectByPrimaryKey(setBasicSysCryptoCurrencies.getId());
        if (crOld == null) {
            throw new R200Exception("当前数据不存在，请重新刷新页面");
        }

        crOld.setIsHot(setBasicSysCryptoCurrencies.getIsHot());
        crOld.setIsRecommend(setBasicSysCryptoCurrencies.getIsRecommend());
        crOld.setAvailable(setBasicSysCryptoCurrencies.getAvailable());
        crOld.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        crOld.setModifyUser(userName);
        setBasicSysCryptoCurrenciesMapper.updateByPrimaryKeySelective(crOld);

        // 记录日志
        logService.crUpdateStatus(crOld);
    }

    public SetBasicSysCryptoCurrencies crPayInfo(Integer id) {
        SetBasicSysCryptoCurrencies cr = setBasicSysCryptoCurrenciesMapper.queryById(id);
        // 获取grouplist
        cr.setGroupList(setBasicSysCryptoCurrenciesMapper.findGroupById(id));
        // 处理groupIds
        List<Integer> groupIds = cr.getGroupList().stream().map(MbrGroup::getId).collect(Collectors.toList());
        cr.setGroupIds(groupIds);
        // 处理bank
        List<Integer> bankIds = setBasicSysCryptoCurrenciesMapper.findBankList(id).stream().map(BaseBank::getId).collect(Collectors.toList());
        cr.setBankIds(bankIds);

        return cr;
    }

    public void updateCrSort(AllotDto allotDto) {
        deleteCrGroup(allotDto);
        if (Collections3.isNotEmpty(allotDto.getCrPayGroups())) {
            setBasicCryptoCurrenciesGroupMapper.insertList(allotDto.getCrPayGroups());
        }
    }


    private void deleteCrGroup(AllotDto allotDto) {
        // 记录日志
        crPayLog(allotDto);

        MbrGroup group = groupMapper.selectByPrimaryKey(allotDto.getGroupId());
        if (Objects.isNull(group)) {
            throw new R200Exception("会员组不存在");
        }
        SetBasicCryptoCurrenciesGroup crGroup = new SetBasicCryptoCurrenciesGroup();
        crGroup.setGroupId(group.getId());
        setBasicSysCryptoCurrenciesMapper.deleteCrGroupEx(crGroup);

    }

    private void crPayLog(AllotDto allotDto) {
        Integer groupId = allotDto.getGroupId();
        MbrGroup group = groupMapper.selectByPrimaryKey(groupId);
        SetBasicCryptoCurrenciesGroup crGroup = new SetBasicCryptoCurrenciesGroup();
        crGroup.setGroupId(groupId);
        List<SetBasicCryptoCurrenciesGroup> crGroups = setBasicSysCryptoCurrenciesMapper.getCrGroup(crGroup);
        List<Integer> crIds = crGroups.stream().map(SetBasicCryptoCurrenciesGroup::getCurrenciesId).collect(Collectors.toList());
        List<Integer> diffList = null;
        Integer addOrDel = Constants.EVNumber.zero;
        if (CollectionUtils.isNotEmpty(allotDto.getCrPayGroups()) &&
                (CollectionUtils.isEmpty(crIds) || crIds.size() < allotDto.getCrPayGroups().size())) {//添加 addOrDel = 1
            List<Integer> newCrIds = allotDto.getCrPayGroups().stream().map(SetBasicCryptoCurrenciesGroup::getCurrenciesId).collect(Collectors.toList());
            diffList = getSubtractId(crIds, newCrIds);
            addOrDel = Constants.EVNumber.one;
        } else if (CollectionUtils.isNotEmpty(crIds) && (CollectionUtils.isEmpty(allotDto.getSysDepMbrs()) ||
                crIds.size() > allotDto.getCrPayGroups().size())) {//删除 addOrDel = 0
            if (CollectionUtils.isEmpty(allotDto.getCrPayGroups())) {
                diffList = crIds;
            } else {
                List<Integer> newDepositList = allotDto.getCrPayGroups().stream().map(SetBasicCryptoCurrenciesGroup::getCurrenciesId).collect(Collectors.toList());
                diffList = getSubtractId(newDepositList, crIds);
            }
            addOrDel = Constants.EVNumber.zero;
        }
        if (Collections3.isNotEmpty(diffList)) {
            SetBasicSysCryptoCurrencies cr = setBasicSysCryptoCurrenciesMapper.selectByPrimaryKey(diffList.get(0));
            // 插入日志 TODO
            logService.updatePaySetLog(addOrDel, group.getGroupName(), cr.getName());
        } else if (CollectionUtils.isNotEmpty(crIds)) {//调整顺序
            // 插入日志 TODO
            logService.updatePaySetLog(Constants.EVNumber.three, group.getGroupName(), null);
        }
    }

    private List<Integer> getSubtractId(List<Integer> smallList, List<Integer> bigList) {
        if (Collections3.isEmpty(smallList)) {
            return bigList;
        }
        return Collections3.subtract(bigList, smallList);
    }

    public List<SetBasicSysCryptoCurrencies> findCrList(Integer accountId) {
        MbrAccount account = mbrAccountMapper.selectByPrimaryKey(accountId);
        if (!Objects.nonNull(account)) {
            return new ArrayList<>();
        }
        List<SetBasicSysCryptoCurrencies> crList = setBasicSysCryptoCurrenciesMapper.findCrList(account.getGroupId());
        if (Objects.isNull(crList) || crList.size() == 0) {
            return null;
        } else if (Constants.EVNumber.zero == crList.get(0).getIsQueue()) {//平铺，获取全部
            return crList;
        } else {//排队，取第一个
            return Lists.newArrayList(crList.get(0));
        }
    }

    public CrPayDto qrCrPay(PayParams params) {
        // 提单校验 校验限额
        SetBasicSysCryptoCurrencies cr = checkoutCrPay(params);
        // 校验完整性 姓名/手机
        MbrAccount account = mbrAccountMapper.selectByPrimaryKey(params.getAccountId());
        sysSettingService.checkPayCondition(account, SystemConstants.DEPOSIT_CONDITION);

        // 下单参数
        EvelletPayTradeRequestDto requestDto = new EvelletPayTradeRequestDto();
        // 金额处理 TODO
        Long longFee = params.getFee().multiply(new BigDecimal(10000)).longValue();
        requestDto.setAmount(longFee);
        requestDto.setApplyDate(DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
        requestDto.setLoginName(account.getLoginName());
        requestDto.setUserType(Constants.TYPE_ACCOUNT);
        // 获得商户号
        requestDto.setMerchantNo(cr.getMerNo());
        // 协议类型
        if (Constants.TYPE_ERC20.equals(cr.getCurrencyProtocol())) {
            requestDto.setType(Constants.TYPE_ERC);
        }
        if (Constants.TYPE_TRC20.equals(cr.getCurrencyProtocol())) {
            requestDto.setType(Constants.TYPE_TRC);
        }

        // 签名
        Map<String, Object> param = jsonUtil.Entity2Map(requestDto);
        param.remove("sign");
        String sign = MD5.getMD5(ASCIIUtils.getFormatUrl(param, cr.getPassword()));
        requestDto.setSign(sign);
        // 下单请求
        String url = evelletUrl + PayConstants.SAASOPS_PAY_EVELLET_PATDO;
        if (!evelletUrl.endsWith("/")) {
            url = evelletUrl + "/" + PayConstants.SAASOPS_PAY_EVELLET_PATDO;
        }

        String jsonMessage;
        try {
            Map<String, String> formMap = jsonUtil.toStringMap(requestDto);
            log.info("qrCrPay==createuser==" + account.getLoginName() + "==requestDto==" + formMap + "==url==" + url);
            jsonMessage = OkHttpUtils.postForm(url, formMap);
            log.info("qrCrPay==createuser==" + account.getLoginName() + "==qrCrPay返回信息==" + jsonMessage);
        } catch (Exception e) {
            log.info("qrCrPay==createuser==" + account.getLoginName() + "==qrCrPay报错==" + e);
            throw new RRException("该支付维护中,请使用其它支付");
        }

        if (Objects.isNull(jsonMessage)) {
            throw new RRException("支付异常！");
        }

        CommonEvelletResponse payResponse = new Gson().fromJson(jsonMessage, CommonEvelletResponse.class);
        if (Objects.isNull(payResponse) || Objects.isNull(payResponse.getCode())) {
            throw new RRException("支付异常！");
        }
        if (payResponse.getCode() != 200) {
            log.info("qrCrPay==createuser==" + account.getLoginName() + "==qrCrPay异常==" + payResponse.getMsg());
            throw new RRException("支付异常！" + payResponse.getMsg());
        }
        // 返回前端数据
        CrPayDto crPayDto = new CrPayDto();
        crPayDto.setCreateTime(requestDto.getApplyDate());
        crPayDto.setDepositAmount(params.getFee()); // 加密货币无手续费下
        BigDecimal depositAmountCNY = CommonUtil.adjustScale(params.getFee().multiply(params.getExchangeRate()));
        crPayDto.setDepositAmountCNY(depositAmountCNY);
        crPayDto.setWalletAddress(payResponse.getAddress());
        crPayDto.setQrCode(payResponse.getQrCode());
        return crPayDto;
    }

    private SetBasicSysCryptoCurrencies checkoutCrPay(PayParams params) {
        SetBasicSysCryptoCurrencies cr = setBasicSysCryptoCurrenciesMapper.selectByPrimaryKey(params.getDepositId());
        if (Objects.isNull(cr) || Constants.EVNumber.zero == cr.getAvailable() || Constants.EVNumber.one == cr.getIsDelete()) {
            throw new R200Exception("此支付方式不接受会员充值");
        }
        if (cr.getMinAmout().compareTo(params.getFee()) == 1) {
            throw new R200Exception("小于单笔最小充值额度");
        }
        return cr;
    }

    public List<CrPayLogoDto> getCrLogo() {
        return setBasicSysCryptoCurrenciesMapper.getCrLogo();
    }

    // 催单
    public void memberReminder(FundDeposit deposit) {
        // 获得USDT加密支付：任一可用
        SetBasicSysCryptoCurrencies cr = new SetBasicSysCryptoCurrencies();
        cr.setAvailable(Constants.EVNumber.one);
        cr.setIsDelete(Constants.EVNumber.zero);
        cr.setCurrencyCode("USDT");
        //cr.setCurrencyProtocol("ERC20");
        List<SetBasicSysCryptoCurrencies> list = setBasicSysCryptoCurrenciesMapper.select(cr);
        if (Objects.isNull(list) || list.size() == 0) {
            throw new R200Exception("未存在可用的USDT入款渠道！");
        }
        // 处理参数
        EvelletPayTransferRequestDto requestDto = new EvelletPayTransferRequestDto();
        requestDto.setLoginName(deposit.getLoginName());    // 会员名
        requestDto.setMerchantNo(list.get(0).getMerNo());   // 商户号

        // 签名
        Map<String, Object> param = jsonUtil.toStringMapObject(requestDto);
        String sign = MD5.getMD5(ASCIIUtils.getFormatUrl(param, list.get(0).getPassword()));
        requestDto.setSign(sign);

        String url = evelletUrl + PayConstants.SAASOPS_PAY_EVELLET_REMINDER;
        if (!evelletUrl.endsWith("/")) {
            url = evelletUrl + "/" + PayConstants.SAASOPS_PAY_EVELLET_REMINDER;
        }
        String jsonMessage;
        try {
            jsonMessage = OkHttpUtils.get(url, jsonUtil.toStringMap(requestDto));
            log.info("memberReminder==【" + jsonMessage + "】");
        } catch (Exception e) {
            log.error("memberReminder==报错【" + e + "】");
            throw new RRException("催单异常！请稍后重试!");
        }

        if (Objects.isNull(jsonMessage)) {
            log.error("memberReminder==jsonMessage为空");
            throw new RRException("催单异常！");
        }

        CommonEvelletResponse payResponse = new Gson().fromJson(jsonMessage, CommonEvelletResponse.class);
        if (Objects.isNull(payResponse) || Objects.isNull(payResponse.getCode())) {
            log.error("memberReminder==返回空！");
            throw new RRException("催单异常！！");
        }
        if (payResponse.getCode() != 200) {
            log.info("memberReminder==异常【" + payResponse.getMsg() + "】");
            throw new RRException("催单异常！" + payResponse.getMsg());
        }
    }

    public List<EvelletPayQueryAddressDto> queryAddress(Integer userId) {
        MbrAccount mbr = mbrAccountMapper.selectByPrimaryKey(userId);
        if (Objects.isNull(mbr)) {
            throw new R200Exception("会员不存在！");
        }
        // 获得USDT加密支付：任一可用
        SetBasicSysCryptoCurrencies cr = new SetBasicSysCryptoCurrencies();
        cr.setAvailable(Constants.EVNumber.one);
        cr.setIsDelete(Constants.EVNumber.zero);
        cr.setCurrencyCode("USDT");
        //cr.setCurrencyProtocol("ERC20");
        List<SetBasicSysCryptoCurrencies> list = setBasicSysCryptoCurrenciesMapper.select(cr);
        if (Objects.isNull(list) || list.size() == 0) {
            throw new R200Exception("未存在可用的USDT入款渠道！");
        }
        // 处理参数
        EvelletPayTransferRequestDto requestDto = new EvelletPayTransferRequestDto();
        requestDto.setLoginName(mbr.getLoginName());        // 会员名
        requestDto.setMerchantNo(list.get(0).getMerNo());   // 商户号

        // 签名
        Map<String, Object> param = jsonUtil.toStringMapObject(requestDto);
        String sign = MD5.getMD5(ASCIIUtils.getFormatUrl(param, list.get(0).getPassword()));
        requestDto.setSign(sign);

        String url = evelletUrl + PayConstants.SAASOPS_PAY_EVELLET_QUERY_ADDRESS;
        if (!evelletUrl.endsWith("/")) {
            url = evelletUrl + "/" + PayConstants.SAASOPS_PAY_EVELLET_QUERY_ADDRESS;
        }
        String jsonMessage;
        try {
            jsonMessage = OkHttpUtils.get(url, jsonUtil.toStringMap(requestDto));
            log.info("queryAddress==【" + jsonMessage + "】");
        } catch (Exception e) {
            log.error("queryAddress==报错【" + e + "】");
            throw new RRException("查询钱包异常");
        }

        if (Objects.isNull(jsonMessage)) {
            log.error("queryAddress==jsonMessage为空");
            throw new RRException("查询钱包异常！");
        }

        Type jsonType = new TypeToken<CommonEvelletResponse<List<EvelletPayQueryAddressDto>>>() {
        }.getType();

        CommonEvelletResponse<List<EvelletPayQueryAddressDto>> payResponse = jsonUtil.fromJson(jsonMessage, jsonType);
        if (Objects.isNull(payResponse) || Objects.isNull(payResponse.getCode())) {
            log.error("queryAddress==返回空！");
            throw new RRException("查询钱包异常！！");
        }
        if (payResponse.getCode() != 200) {
            log.error("queryAddress==异常【" + payResponse.getMsg() + "】");
            throw new RRException("查询钱包异常！" + payResponse.getMsg());
        }
        List<EvelletPayQueryAddressDto> dto = payResponse.getData();
        if (Collections3.isEmpty(dto)) {
            log.error("queryAddress==地址返回为空");
            throw new RRException("地址返回为空");
        }

        return dto;
    }


    /**
     * 获得加密货币汇率
     *
     * @param type deposit or Withdrawal
     * @return
     */
    public String getExchangeRate(String type) {
        String url = evelletUrl + PayConstants.SAASOPS_PAY_EVELLET_EXCHANGE_RATE;
        if (!evelletUrl.endsWith("/")) {
            url = evelletUrl + "/" + PayConstants.SAASOPS_PAY_EVELLET_EXCHANGE_RATE;
        }
        SetBasicSysCryptoCurrencies cr = new SetBasicSysCryptoCurrencies();
        cr.setAvailable(Constants.EVNumber.one);
        cr.setIsDelete(Constants.EVNumber.zero);
        cr.setCurrencyCode("USDT");
        List<SetBasicSysCryptoCurrencies> list = setBasicSysCryptoCurrenciesMapper.select(cr);
        if (list.size() == 0) {
            return null;
        }
        String jsonMessage;
        try {
            Map<String, String> param = new HashMap<>();
            param.put("type", type);
            if (list.size() > 0) {
                param.put("merchantNo", list.get(0).getMerNo());
            }
            jsonMessage = OkHttpUtils.get(url, param);
            log.info("getExchangeRate【" + jsonMessage + "】");
        } catch (Exception e) {
            log.error("getExchangeRate报错【" + e + "】");
            throw new RRException("获取汇率异常！请稍后重试!");
        }

        if (Objects.isNull(jsonMessage)) {
            throw new RRException("获取汇率异常！");
        }

        CommonEvelletResponse payResponse = new Gson().fromJson(jsonMessage, CommonEvelletResponse.class);
        if (Objects.isNull(payResponse) || Objects.isNull(payResponse.getCode())) {
            throw new RRException("获取汇率异常！！");
        }
        if (payResponse.getCode() != 200) {
            log.info("getExchangeRatey异常【" + payResponse.getMsg() + "】");
            throw new RRException("获取汇率异常！" + payResponse.getMsg());
        }
        return payResponse.getRate();
    }

    public String evelletCallback(EvelletPayPushCallbackDto data, String siteCode) {
        // 判断是否存在相同的hash
        FundDeposit tmp = new FundDeposit();
        tmp.setHash(data.getHash());
        int count = fundDepositMapper.selectCount(tmp);
        if (count != 0) {
            log.info("evelletCallback==siteCode==" + siteCode + "==loginName==" + data.getLoginName() + "==hash==" + data.getHash() + "该hash已存在!");
            throw new R200Exception("该hash已存在！");
        }

        // 获得交易渠道pay  TODO 此处写死，需要等加密钱包可以给唯一标志
        data.setCurrencyCode("USDT");
        // 协议类型
        if (Constants.TYPE_ERC.equals(data.getType())) {
            data.setCurrencyProtocol(Constants.TYPE_ERC20);
        }
        if (Constants.TYPE_TRC.equals(data.getType())) {
            data.setCurrencyProtocol(Constants.TYPE_TRC20);
        }
        SetBasicSysCryptoCurrencies cr = setBasicSysCryptoCurrenciesMapper.getCrByCodeAndProtocol(data.getCurrencyCode(), data.getCurrencyProtocol());

        // 验签
        Map<String, Object> param = jsonUtil.Entity2Map(data);
        param.remove("sign");
        param.remove("currencyProtocol");   // TODO
        param.remove("currencyCode");
        String sign = MD5.getMD5(ASCIIUtils.getFormatUrl(param, cr.getPassword()));
        if (!sign.equals(data.getSign())) {
            log.info("evelletCallback==siteCode==" + siteCode + "==loginName==" + data.getLoginName() + "签名错误");
            throw new R200Exception("验签失败！");
        }

        String key = RedisConstants.EVELLET_CALLBACK + siteCode + data.getLoginName() + data.getHash();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, data.getHash(), 360, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isExpired)) {
            log.info("evelletCallback==siteCode==" + siteCode + "==loginName==" + data.getLoginName() + "==重复回调请求");
            throw new R200Exception("重复请求！");
        }
        try {
            // 1. 创建订单
            // 获得会员
            MbrAccount account = new MbrAccount();
            account.setLoginName(data.getLoginName());
            account = mbrAccountMapper.selectOne(account);

            // 获得存款设置
            MbrDepositCond mbrDepositCond = mbrDepositCondService.getMbrDeposit(account.getId());

            // 参数准备
            PayParams params = new PayParams();
            params.setOutTradeNo(new SnowFlake().nextId());
            params.setAccountId(account.getId());
            params.setUserName(account.getRealName());
            params.setIp(null);
            params.setFundSource(null);

            params.setExchangeRate(data.getExchangeRate());
            params.setCreateTime(data.getCreateTime());
            params.setHash(data.getHash());

            params.setCrId(cr.getId());    // 加密货币支付渠道id

            // 处理入款金额(币)
            BigDecimal fee = CommonUtil.adjustScale(BigDecimal.valueOf(data.getAmount()).divide(new BigDecimal(10000)));
            params.setFee(fee);

            // 保存订单
            FundDeposit deposit = saveFundDeposit(params, cr, mbrDepositCond, account, siteCode);
            if (deposit != null) {
                // 2. 入款成功后处理钱包等
                FundDeposit fundDeposit = new FundDeposit();
                fundDeposit.setId(deposit.getId());
                fundDeposit.setStatus(Constants.EVNumber.one);

                fundDeposit = fundDepositService.updateDeposit(fundDeposit, SYSTEM_USER, "", siteCode);
                // 推送入款成功消息
                fundDepositService.accountDepositMsg(fundDeposit, siteCode);
                return "SUCCESS" + "&" + deposit.getOrderNo();
            }
            return "SUCCESS&";
        } finally {
            redisService.del(key);
        }
    }

    private FundDeposit saveFundDeposit(PayParams params, SetBasicSysCryptoCurrencies cr, MbrDepositCond depositCond,
                                        MbrAccount account, String siteCode) {
        BigDecimal depositAmount = CommonUtil.adjustScale(params.getFee().multiply(params.getExchangeRate()));  // 存款金额RNB
        // 截取后等于0不保存
        if (depositAmount.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        FundDeposit deposit = new FundDeposit();
        deposit.setOrderNo(params.getOutTradeNo() + CommonUtil.genRandom(3, 3));
        deposit.setMark(FundDeposit.Mark.crPay);
        deposit.setStatus(FundDeposit.Status.apply);
        deposit.setIsPayment(FundDeposit.PaymentStatus.unPay);
        deposit.setCrId(params.getCrId());
        deposit.setDepositAmount(depositAmount);    // 截断存款2位数
        deposit.setHandlingCharge(new BigDecimal(0));   // 手续费为空
        deposit.setHandingback(Constants.Available.disable);
        deposit.setActualArrival(deposit.getDepositAmount().add(deposit.getHandlingCharge()));

        deposit.setIp(params.getIp());
        deposit.setOrderPrefix(OrderConstants.FUND_ORDER_COMPANYDEPOSIT);
        deposit.setDepositUser(account.getLoginName());
        deposit.setCreateUser(account.getLoginName());
        deposit.setAccountId(params.getAccountId());
        deposit.setCreateTime(params.getCreateTime());
        deposit.setModifyTime(params.getCreateTime());
        deposit.setFundSource(params.getFundSource());
        deposit.setDepositPostscript(CommonUtil.genRandom(6, 6));

        deposit.setDepositAmountCr(params.getFee());        // 存款金额 币
        deposit.setExchangeRate(params.getExchangeRate());  // 汇率
        deposit.setHash(params.getHash());
        deposit.setPayOrderNo(params.getHash());    // hash做第三方订单？
        fundDepositMapper.insert(deposit);

        verifyService.addMbrVerifyDeposit(deposit, siteCode);
        return deposit;
    }

    // 取款申请
    public String evelletPayment(FundMerchantPay merchantPay, String orderNo, BigDecimal actualArrivalCr,
                                 String loginName, String walletAddress, String createUser, String userType) {
        // 提交代付申请: 提单了，不管结果怎样，都是自动出款、自动出款中，等回调或者让人工去干涉
        String siteCode = CommonUtil.getSiteCode();

        // 提单 参数处理
        EvelletPayTransferRequestDto requestDto = new EvelletPayTransferRequestDto();
        // 金额处理 TODO
        Long longFee = actualArrivalCr.multiply(new BigDecimal(10000)).longValue();
        requestDto.setAmount(longFee);
        requestDto.setApplyDate(DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
        requestDto.setLoginName(loginName);
        requestDto.setMerchantNo(merchantPay.getMerchantNo());
        requestDto.setAddress(walletAddress);
        // 处理url
        String notifyUrl = intranetUrl + PayConstants.SAASOPS_PAY_EVELLET_NOTIFY_URL + "/" + siteCode;
        if (!intranetUrl.endsWith("/")) {
            notifyUrl = intranetUrl + "/" + PayConstants.SAASOPS_PAY_EVELLET_NOTIFY_URL + "/" + siteCode;
        }
        requestDto.setNotifyUrl(notifyUrl);
        requestDto.setOutTradeno(orderNo);

        // 协议类型
        if (Constants.TYPE_ERC20.equals(merchantPay.getCurrencyProtocol())) {
            requestDto.setType(Constants.TYPE_ERC);
        }
        if (Constants.TYPE_TRC20.equals(merchantPay.getCurrencyProtocol())) {
            requestDto.setType(Constants.TYPE_TRC);
        }
        requestDto.setUserType(userType);
        // 签名
        Map<String, Object> param = jsonUtil.Entity2Map(requestDto);
        param.remove("sign");
        String sign = MD5.getMD5(ASCIIUtils.getFormatUrl(param, merchantPay.getMerchantKey()));
        requestDto.setSign(sign);

        // 下单请求
        String url = evelletUrl + PayConstants.SAASOPS_PAY_EVELLET_TRANSFER;
        if (!evelletUrl.endsWith("/")) {
            url = evelletUrl + "/" + PayConstants.SAASOPS_PAY_EVELLET_TRANSFER;
        }
        String result;
        try {
            log.info("evelletPayment==createuser==" + createUser + "==order==" + orderNo
                    + "==requestDto==" + jsonUtil.toStringMap(requestDto) + "==url==" + url);
            result = OkHttpUtils.get(url, jsonUtil.toStringMap(requestDto));

            log.info("evelletPayment==createuser==" + createUser + "==order==" + orderNo
                    + "==evelletPayment返回信息==" + result);

            return result;
        } catch (Exception e) {
            log.info("evelletPayment==createuser==" + createUser + "==order==" + orderNo + "==evelletPayment报错==" + e);
            return null;
        }

    }

    // 代付查询
    public String querySubmitSuccess(AccWithdraw withDraw, FundMerchantPay merchantPay) {
        // 提单 参数处理
        EvelletPayTransferRequestDto requestDto = new EvelletPayTransferRequestDto();
        requestDto.setMerchantNo(merchantPay.getMerchantNo());
        requestDto.setOutTradeno(withDraw.getOrderNo());
        requestDto.setUserType(Constants.TYPE_ACCOUNT);
        // 签名
        Map<String, Object> param = jsonUtil.toStringMapObject(requestDto);
        String sign = MD5.getMD5(ASCIIUtils.getFormatUrl(param, merchantPay.getMerchantKey()));
        requestDto.setSign(sign);

        // 下单请求
        // 处理url
        String url = evelletUrl + PayConstants.SAASOPS_PAY_EVELLET_TRANSFER_QUERY;
        if (!evelletUrl.endsWith("/")) {
            url = evelletUrl + "/" + PayConstants.SAASOPS_PAY_EVELLET_TRANSFER_QUERY;
        }
        String result;
        try {
            log.info("querySubmitSuccess==createuser==" + withDraw.getCreateUser() + "==order==" + withDraw.getOrderNo()
                    + "==requestDto==" + jsonUtil.toStringMap(requestDto) + "==url==" + url);
            result = OkHttpUtils.get(url, jsonUtil.toStringMap(requestDto));
            log.info("querySubmitSuccess==createuser==" + withDraw.getCreateUser() + "==order==" + withDraw.getOrderNo()
                    + "==evelletPayment返回信息==" + result);

            return result;
        } catch (Exception e) {
            log.info("querySubmitSuccess==createuser==" + withDraw.getCreateUser() + "==order==" + withDraw.getOrderNo()
                    + "==querySubmitSuccess报错==" + e);
            return null;
        }

    }

    public List<SetBasicSysCryptoCurrencies> qrCrList() {
        return setBasicSysCryptoCurrenciesMapper.selectAll();
    }

}
