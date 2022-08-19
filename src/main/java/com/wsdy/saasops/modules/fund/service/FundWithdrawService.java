package com.wsdy.saasops.modules.fund.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wsdy.saasops.api.modules.pay.dto.DPaySearchResponseDto;
import com.wsdy.saasops.api.modules.pay.dto.evellet.CommonEvelletResponse;
import com.wsdy.saasops.api.modules.pay.dto.evellet.EvelletPayTransferCallbackDto;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.ASCIIUtils;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.api.utils.MD5;
import com.wsdy.saasops.common.constants.*;
import com.wsdy.saasops.common.constants.Constants.Available;
import com.wsdy.saasops.common.constants.MerchantPayConstants.paymentStatusCode;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.config.MessagesConfig;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.listener.BizEventType;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.fund.dao.AccWithdrawMapper;
import com.wsdy.saasops.modules.fund.dao.FundMerchantDetailMapper;
import com.wsdy.saasops.modules.fund.dao.FundMerchantPayMapper;
import com.wsdy.saasops.modules.fund.dao.TChannelPayMapper;
import com.wsdy.saasops.modules.fund.dto.*;
import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.wsdy.saasops.modules.fund.entity.FundMerchantDetail;
import com.wsdy.saasops.modules.fund.entity.FundMerchantPay;
import com.wsdy.saasops.modules.fund.entity.TChannelPay;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.member.dao.*;
import com.wsdy.saasops.modules.member.entity.*;
import com.wsdy.saasops.modules.member.entity.MbrWithdrawalCond.FeeWayVal;
import com.wsdy.saasops.modules.member.service.*;
import com.wsdy.saasops.modules.sys.dto.SysWarningDto;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import com.wsdy.saasops.modules.sys.service.SysWarningService;
import com.wsdy.saasops.modules.system.pay.service.CryptoCurrenciesService;
import com.wsdy.saasops.modules.system.systemsetting.dto.WithdrawLimitTimeDto;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.utils.DateUtil.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@Transactional
public class FundWithdrawService extends BaseService<AccWithdrawMapper, AccWithdraw> {

    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private MbrBankcardMapper mbrBankcardMapper;
    @Autowired
    private AccWithdrawMapper accWithdrawMapper;
    @Autowired
    private MessagesConfig messagesConfig;
    @Autowired
    private MbrBankcardService mbrBankcardService;
    @Value("${fund.accWithdraw.excel.path}")
    private String accExcelPath;
    @Value("${fund.agyWithdraw.excel.path}")
    private String agyExcelPath;
    @Autowired
    private MbrAccountService mbrAccountService;
    @Autowired
    private MbrWalletService mbrWalletService;
    @Autowired
    private MbrBillDetailMapper mbrBillDetailMapper;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private MbrAuditAccountMapper accountAuditMapper;
    @Autowired
    private MbrWithdrawalCondService withdrawalCond;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private PanZiPayService panZiPayService;
    @Autowired
    private FundMerchantDetailMapper merchantDetailMapper;
    @Autowired
    private FundMerchantPayMapper merchantPayMapper;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private TChannelPayMapper channelPayMapper;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private OnePayService onePayService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    @Autowired
    private PaymentPayService paymentPayService;
    @Autowired
    private LBTPayService lbtPayService;
    @Autowired
    private MbrCryptoCurrenciesService mbrCryptoCurrenciesService;
    @Autowired
    private MbrCryptoCurrenciesMapper mbrCryptoCurrenciesMapper;
    @Autowired
    private CryptoCurrenciesService cryptoCurrenciesService;
    @Autowired
    private MbrFundsReportMapper mbrFundsReportMapper;
    
    @Autowired
    private PayCenterService payCenterService;
    @Autowired
    private MbrLabelService mbrLabelService;
    @Autowired
    private SysWarningService sysWarningService;
    @Autowired
    private MbrWarningService mbrWarningService;
    
    public PageUtils queryAccListPage(AccWithdraw accWithdraw, Integer pageNo, Integer pageSize, int flag) {
        //accWithdraw.setBaseAuth(getRowAuth());
        PageHelper.startPage(pageNo, pageSize);
        List<Integer> statuss = accWithdraw.getStatuss();
        //判断statuss是否为空
        if (Collections3.isNotEmpty(statuss)) {
            int forFlag = statuss.size();
            for (int i = 0; i < statuss.size(); i++) {
                if (2 == statuss.get(i) || 4 == statuss.get(i)) {
                    statuss.add(4);
                    statuss.add(2);
                } else if (3 == statuss.get(i) || 5 == statuss.get(i)) {
                    statuss.add(3);
                    statuss.add(5);
                } else if (6 == statuss.get(i)) {
                    statuss.add(6);
                }
                if (i + 1 == forFlag) {
                    break;
                }
            }
        }

        List<AccWithdraw> list = fundMapper.findAccWithdrawList(accWithdraw);

        list.stream().filter(p -> "支付宝".equals(p.getBankName()) && StringUtil.isEmpty(p.getRelatedOrderno())).forEach(e -> e.setMethodType(Constants.EVNumber.two));
        return BeanUtil.toPagedResult(list);
    }

    public AccWithdraw accSumStatistics(AccWithdraw accWithdraw) {
        accWithdraw.setStatuss(Lists.newArrayList(1));
        AccWithdraw allTotal = fundMapper.findAccWithdrawListSum(accWithdraw);
        allTotal.setTodayWithdraw(fundMapper.findTodayWithdraw());
        return allTotal;
    }

    public PageUtils queryAccListPage(String startTime, String endTime, Integer accountId, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<AccWithdraw> list = fundMapper.findFixateAccWithdraw(startTime, endTime, accountId, null, null);
        return BeanUtil.toPagedResult(list);
    }

    public double totalActualArrival(String startTime, String endTime, Integer accountId) {
        return fundMapper.totalFixateAccWithdraw(startTime, endTime, accountId);
    }

    public Double accSumDrawingAmount(String loginSysUserName) {
        AccWithdraw accWithdraw = new AccWithdraw();
        accWithdraw.setStatus(Constants.IsStatus.succeed);
        accWithdraw.setPassTime(getCurrentDate(FORMAT_10_DATE));
        accWithdraw.setLoginSysUserName(loginSysUserName);
        return fundMapper.accSumDrawingAmount(accWithdraw);
    }

    public AccWithdraw queryAccObject(Integer id, String orderNo, String userName) {
        AccWithdraw accWithdraw = new AccWithdraw();
        accWithdraw.setId(id);
        accWithdraw.setOrderNo(orderNo);
        Optional<AccWithdraw> optional = Optional.ofNullable(
                fundMapper.findAccWithdrawList(accWithdraw)
                        .stream().findAny()).get();
        if (optional.isPresent()) {
            AccWithdraw accWithdraw1 = optional.get();
            accWithdraw1.setMbrBankcard(mbrBankcardMapper.
                    selectByPrimaryKey(accWithdraw1.getBankCardId()));
            AccWithdraw accCount = new AccWithdraw();
            accCount.setNotStatus(Constants.IsStatus.defeated);
            accCount.setAccountId(accWithdraw1.getAccountId());
            accCount.setCreateTimeTo(accWithdraw1.getCreateTime());
            accWithdraw1.setWithdrawCount(fundMapper.findAccWithdrawCount(accCount));

            if (accWithdraw1.getStatus().intValue() == Constants.EVNumber.one) {
                // 当成功订单审核时间不超过72小时，可以修改订单状态
                Date after24 = DateUtil.addHours(accWithdraw1.getCreateTime(), 72);
                if (after24.after(new Date())) {
                    accWithdraw1.setCanChangeStatus(Constants.EVNumber.one);
                }
            }

            // 判断是否锁定
            if (Integer.valueOf(Constants.EVNumber.one).equals(accWithdraw1.getLockStatus())) {    // 锁定状态
                if (userName.equals(accWithdraw1.getLockOperator())) {     // 被当前用户锁定
                    accWithdraw1.setIsCurrentUserLock(Constants.EVNumber.one);
                } else {  // 非当前用户锁定
                    accWithdraw1.setIsCurrentUserLock(Constants.EVNumber.two);
                }
            } else {      // 未锁定状态
                accWithdraw1.setIsCurrentUserLock(Constants.EVNumber.zero);
            }

            return accWithdraw1;
        }
        return null;
    }

    public void checkoutStatusByTwo(Integer id) {
        AccWithdraw withdraw = accWithdrawMapper.selectByPrimaryKey(id);
        if (!withdraw.getStatus().equals(Constants.IsStatus.pending)
                && !withdraw.getStatus().equals(Constants.IsStatus.four)) {
            throw new R200Exception("请刷新数据");
        }
        // 未锁定不允许操作
        if (Integer.valueOf(Constants.EVNumber.zero).equals(withdraw.getLockStatus())) {    // 锁定状态
            throw new R200Exception("未锁定的订单不允许操作");
        }
    }

    public void checkoutStatusBySucToFail(Integer id) {
        AccWithdraw withdraw = accWithdrawMapper.selectByPrimaryKey(id);
        if (!withdraw.getStatus().equals(Constants.IsStatus.succeed)) {
            throw new R200Exception("订单不是成功状态，无法修改");
        }
        // 当成功订单审核时间不超过72小时，可以修改订单状态
        Date after24 = DateUtil.addHours(withdraw.getCreateTime(), 72);
        if (after24.before(new Date())) {
            throw new R200Exception("订单已超过申请时间24小时，无法修改");
        }
        // 涉及到报表统计，10分钟内的订单无法标记为失败
        Date after10min = DateUtil.addDate(withdraw.getPassTime(), 0);
        if (after10min.after(new Date())) {
            throw new R200Exception("出款完成10分钟内的订单无法修改，请超过10分钟后处理");
        }
    }

    public void updateAccStatus(AccWithdraw accWithdraw, String loginName, BizEvent bizEvent, String ip) {
        String siteCode =  CommonUtil.getSiteCode();
        String key = RedisConstants.ACCOUNT_WITHDRAW_AUDIT + siteCode + accWithdraw.getId();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, accWithdraw.getId(), 200, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isExpired)) {
            throw new R200Exception("任务处理中，请勿重复点击！");
        }
        try {
            // 获得当前记录
            AccWithdraw withdraw = checkoutFund(accWithdraw.getId());
            // 判断是否锁定，未锁定则无法操作
            if (Integer.valueOf(Constants.EVNumber.zero).equals(withdraw.getLockStatus())) {    // 锁定状态
                throw new R200Exception("未锁定的订单不允许操作");
            }
            
            // 为免审订单
            if (withdraw.getIsExempt()) {
            	withdraw.setExemptUser(loginName);
            	withdraw.setExemptTime(new Date());
            	withdraw.setExemptMemo(accWithdraw.getExemptMemo());
            }

            withdraw.setSiteCode(siteCode);
            // 设置消息对象
            setBizEvent(withdraw, bizEvent, accWithdraw, loginName);
            // 增加处理初审/复审的逻辑
            // 1. 初审通过/拒绝 更新初审备注  (初审和初审待定共用审核备注)
            if (withdraw.getStatus() == Constants.EVNumber.two || withdraw.getStatus() == Constants.EVNumber.six) {
                // 允许备注为空
                if (!StringUtil.isEmpty(accWithdraw.getMemo())) {
                    withdraw.setMemo(accWithdraw.getMemo());
                }
            }
            // 2. 复审通过/拒绝 更新复审备注
            if (withdraw.getStatus() == Constants.EVNumber.three) {
                // 允许备注为空
                if (!StringUtil.isEmpty(accWithdraw.getMemoWithdraw())) {
                    withdraw.setMemoWithdraw(accWithdraw.getMemoWithdraw());
                }
            }

            withdraw.setModifyUser(loginName);
            withdraw.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
            // 复审通过
            if (accWithdraw.getStatus() == Constants.EVNumber.one
                    && withdraw.getStatus() == Constants.EVNumber.three) {
                withdraw.setPassTime(getCurrentDate(FORMAT_18_DATE_TIME));
                withdraw.setPassUser(loginName);
                withdraw.setStatus(accWithdraw.getStatus());
                updateIsDrawings(withdraw.getAccountId(), Constants.EVNumber.one);
                // 如果是人工调整的金额，扣除用户余额
                if (StringUtil.isNotEmpty(withdraw.getRelatedOrderno())) {
                	// 保存帐变和减少钱包余额
                	MbrBillDetail mbrBillDetail = new MbrBillDetail();
                	mbrBillDetail.setLoginName(withdraw.getLoginName());
                	mbrBillDetail.setAccountId(withdraw.getAccountId());
                	mbrBillDetail.setFinancialCode(OrderConstants.FUND_ORDER_ACCWITHDRAW);
                	mbrBillDetail.setOrderNo(new SnowFlake().nextId() + "");
                	mbrBillDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
                	mbrBillDetail.setDepotId(Constants.SYS_DEPOT_ID);
                	mbrBillDetail.setOpType(MbrBillDetail.OpTypeStatus.expenditure);
                	mbrBillDetail.setAmount(withdraw.getActualArrival());
                	// handlingCharge 手续费
                	MbrWallet mbrWallet = new MbrWallet();
                	mbrWallet.setAccountId(mbrBillDetail.getAccountId());
                	mbrWallet.setBalance(mbrBillDetail.getAmount());
                	boolean flag = mbrWalletService.walletSubtract(mbrWallet, mbrBillDetail);   // 保存帐变和减少钱包余额
                	if (Boolean.FALSE.equals(flag)) {
                		throw new R200Exception("人工取款失败");
                	}
                	withdraw.setBillDetailId(mbrBillDetail.getId());
                }
            }
            // 初审通过/初审待定通过 都是进入复审
            if (accWithdraw.getStatus() == Constants.EVNumber.one
                    && (withdraw.getStatus() == Constants.EVNumber.two || withdraw.getStatus() == Constants.EVNumber.six)) {
                AccWithdraw oldWithdraw = accWithdrawMapper.selectByPrimaryKey(withdraw.getId());
                AccWithdraw old = new AccWithdraw();
                old.setStatus(oldWithdraw.getStatus());
                log.info("==初审通过/初审待定通过 都是进入复审==withdraw:{}",JSON.toJSONString(withdraw));
                withdraw.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
                withdraw.setAuditUser(loginName);
                withdraw.setStatus(Constants.EVNumber.three);

                // 更新操作锁定：解锁
                withdraw.setLockStatus(Constants.EVNumber.zero);
                withdraw.setLockOperator(null);
                withdraw.setLastLockTime(null);
                
                beginMerchantPayment(withdraw);
                //添加操作日志
                mbrAccountLogService.updateMemberWithdrawInfo(withdraw, old, loginName, ip);
                return;
            }

            // 初审进入初审待定
            if (accWithdraw.getStatus() == Constants.EVNumber.six
                    && withdraw.getStatus() == Constants.EVNumber.two) {
                AccWithdraw oldWithdraw = accWithdrawMapper.selectByPrimaryKey(withdraw.getId());
                AccWithdraw old = new AccWithdraw();
                old.setStatus(oldWithdraw.getStatus());
                withdraw.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
                withdraw.setAuditUser(loginName);
                withdraw.setStatus(Constants.EVNumber.six);

                // 更新操作锁定：解锁
                withdraw.setLockStatus(Constants.EVNumber.zero);
                withdraw.setLockOperator(null);
                withdraw.setLastLockTime(null);

                accWithdrawMapper.updateByPrimaryKey(withdraw);
                //添加操作日志
                mbrAccountLogService.updateMemberWithdrawInfo(withdraw, old, loginName, ip);

                //beginMerchantPayment(withdraw);
                return;
            }
            // 初审/复审/初审待定 拒绝
            if (accWithdraw.getStatus() == Constants.EVNumber.zero
                    && (withdraw.getStatus() == Constants.EVNumber.two
                    || withdraw.getStatus() == Constants.EVNumber.three || withdraw.getStatus() == Constants.EVNumber.six)) {
                withdraw.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
                withdraw.setAuditUser(loginName);
                withdraw.setStatus(accWithdraw.getStatus());
                // 如果不是人工提款的订单，才做处理
                if (StringUtil.isEmpty(withdraw.getRelatedOrderno())) {
                	setAccWithdrawZreo(withdraw);
                	updateIsDrawings(withdraw.getAccountId(), Constants.EVNumber.zero);
                	// 添加拒绝时的稽核: 本金和流水不为空
                	if (Objects.nonNull(accWithdraw.getDepositAmount()) && Objects.nonNull(accWithdraw.getAuditAmount())) {
                		auditAccountService.insertAccountAudit(
                				withdraw.getAccountId(), accWithdraw.getDepositAmount(),
                				null, null, accWithdraw.getAuditAmount(),
                				null, null, Constants.EVNumber.five);
                	}
                }
            }

            AccWithdraw oldWithdraw = accWithdrawMapper.selectByPrimaryKey(withdraw);
            // 更新操作锁定：解锁 <-- 初审/复审拒绝 + 复审通过
            withdraw.setLockStatus(Constants.EVNumber.zero);
            withdraw.setLockOperator(null);
            withdraw.setLastLockTime(null);

            accWithdrawMapper.updateByPrimaryKey(withdraw);
            //添加操作日志
            mbrAccountLogService.updateMemberWithdrawInfo(withdraw, oldWithdraw, loginName, ip);
        } finally {
            redisService.del(key);
        }
    }

    public void updateAccStatusSucToFail(AccWithdraw accWithdraw, String loginName, BizEvent bizEvent, String ip) {

        String siteCode = CommonUtil.getSiteCode();
        String key = RedisConstants.ACCOUNT_WITHDRAW_AUDIT + siteCode + accWithdraw.getId();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, accWithdraw.getId(), 200, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isExpired)) {
            throw new R200Exception("任务处理中，请勿重复点击！");
        }
        try {
            // 获得当前记录
            AccWithdraw withdraw = checkoutFundSucToFail(accWithdraw.getId());
            // 设置消息对象
            setBizEvent(withdraw, bizEvent, accWithdraw, loginName);
            withdraw.setSiteCode(siteCode);
            // 增加处理初审/复审的逻辑
            // 1. 初审通过/拒绝 更新初审备注  (初审和初审待定共用审核备注)
            if (withdraw.getStatus() == Constants.EVNumber.two || withdraw.getStatus() == Constants.EVNumber.six) {
                // 允许备注为空
                if (!StringUtil.isEmpty(accWithdraw.getMemo())) {
                    withdraw.setMemo(accWithdraw.getMemo());
                }
            }
            // 2. 复审通过/拒绝 更新复审备注
            if (withdraw.getStatus() == Constants.EVNumber.three) {
                // 允许备注为空
                if (!StringUtil.isEmpty(accWithdraw.getMemoWithdraw())) {
                    withdraw.setMemoWithdraw(accWithdraw.getMemoWithdraw());
                }
            }

            withdraw.setModifyUser(loginName);
            withdraw.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));

            // 24小时内成功订单修改为失败
            if (accWithdraw.getStatus() == Constants.EVNumber.seven && withdraw.getStatus() == Constants.EVNumber.one) {
                log.info("updateAccStatusSucToFail具体操作订单");
                // 当成功订单审核时间不超过72小时，可以修改订单状态
                Date after24 = DateUtil.addHours(withdraw.getCreateTime(), 72);
                if (after24.before(new Date())) {
                    throw new R200Exception("订单已超过申请时间24小时，无法修改");
                }
                withdraw.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
                withdraw.setModifyUser(loginName);
                withdraw.setStatus(accWithdraw.getStatus());
                setAccWithdrawSucToFail(withdraw);
                // 暂不处理稽核
                // updateIsDrawings(withdraw.getAccountId(), Constants.EVNumber.zero);
                // 添加失败时的稽核: 本金和流水不为空
                if (Objects.nonNull(accWithdraw.getDepositAmount()) && Objects.nonNull(accWithdraw.getAuditAmount())) {
                    auditAccountService.insertAccountAudit(
                            withdraw.getAccountId(), accWithdraw.getDepositAmount(),
                            null, null, accWithdraw.getAuditAmount(),
                            null, null, Constants.EVNumber.five);
                }
                // 修改会员取款统计
                MbrFundsReport upmfr = new MbrFundsReport();
                upmfr.setReportDate(withdraw.getPassTime());
                upmfr.setWithdraw(withdraw.getDepositAmount());
                upmfr.setAccountId(withdraw.getAccountId());
                mbrFundsReportMapper.updateReportDepositByUserDate(upmfr);
            }
            AccWithdraw oldWithdraw = accWithdrawMapper.selectByPrimaryKey(withdraw);
            // 更新操作锁定：解锁 <-- 初审/复审拒绝 + 复审通过
            withdraw.setLockStatus(Constants.EVNumber.zero);
            withdraw.setLockOperator(null);
            withdraw.setLastLockTime(null);
            withdraw.setChangeMemo(accWithdraw.getChangeMemo());
            accWithdrawMapper.updateByPrimaryKey(withdraw);
            //添加操作日志
            mbrAccountLogService.updateMemberWithdrawInfo(withdraw, oldWithdraw, loginName, ip);

        } finally {
            redisService.del(key);
        }
    }

    // 设置取款相关消息
    private void setBizEvent(AccWithdraw withdraw, BizEvent bizEvent, AccWithdraw accWithdraw, String userName) {
        bizEvent.setUserId(withdraw.getAccountId());                // 会员ID
        bizEvent.setWithdrawMoney(withdraw.getDrawingAmount());     // 提款金额
        bizEvent.setUserName(userName);                             // 会员名
        // 2待处理->0拒绝： 初审拒绝 or 初审待定拒绝
        if (accWithdraw.getStatus() == Constants.EVNumber.zero
                && (withdraw.getStatus() == Constants.EVNumber.two || withdraw.getStatus() == Constants.EVNumber.six)) {
            if (Integer.valueOf(Constants.EVNumber.one).equals(accWithdraw.getInMailType())) {          // 会员提款初审拒绝(流水不足)
                bizEvent.setEventType(BizEventType.MEMBER_WITHDRAWAL_PRIMARY_VERIFY_FAILED_1);
            } else if (Integer.valueOf(Constants.EVNumber.two).equals(accWithdraw.getInMailType())) {    // 会员提款初审拒绝(违规下注)
                bizEvent.setEventType(BizEventType.MEMBER_WITHDRAWAL_PRIMARY_VERIFY_FAILED_2);
            } else if (Integer.valueOf(Constants.EVNumber.three).equals(accWithdraw.getInMailType())) {    // 会员提款初审拒绝(优惠套利)
                bizEvent.setEventType(BizEventType.MEMBER_WITHDRAWAL_PRIMARY_VERIFY_FAILED_3);
            } else if (Integer.valueOf(Constants.EVNumber.four).equals(accWithdraw.getInMailType())) {    // 会员提款初审拒绝(注单审核)
                bizEvent.setEventType(BizEventType.MEMBER_WITHDRAWAL_PRIMARY_VERIFY_FAILED_4);
            } else {                                                                                  // 默认
                bizEvent.setEventType(BizEventType.MEMBER_WITHDRAWAL_PRIMARY_VERIFY_FAILED);
            }
        }
        // 3出款中->0拒绝： 复审拒绝
        if (accWithdraw.getStatus() == Constants.EVNumber.zero
                && withdraw.getStatus() == Constants.EVNumber.three) {
            bizEvent.setEventType(BizEventType.MEMBER_WITHDRAWAL_REVIEW_VERIFY_FAILED);
        }
        // 3出款中->1成功：复审成功
        if (accWithdraw.getStatus() == Constants.EVNumber.one
                && withdraw.getStatus() == Constants.EVNumber.three) {
            bizEvent.setEventType(BizEventType.MEMBER_WITHDRAWAL_REVIEW_VERIFY_SUCCESS);
        }
    }


    private void setAccWithdrawZreo(AccWithdraw accWithdraw) {
        MbrBillDetail billDetail = mbrBillDetailMapper.selectByPrimaryKey(accWithdraw.getBillDetailId());
        //log.info("setAccWithdrawZreo空指针调试billDetail{} accwithdraw{}", JSON.toJSONString(billDetail), JSON.toJSONString(accWithdraw));
        //log.info("setAccWithdrawZreo空指针调试mbrWalletService{} ", mbrWalletService);
        if (Objects.nonNull(billDetail)) {

            if(Objects.nonNull(accWithdraw.getSource()) && accWithdraw.getSource() == Constants.EVNumber.one){
                MbrWallet subWallet = new MbrWallet();
                subWallet.setAccountId(billDetail.getAccountId());
                subWallet.setHuPengBalance(billDetail.getAmount());
                MbrBillDetail mbrBillDetail = new MbrBillDetail();
                mbrBillDetail.setLoginName(accWithdraw.getLoginName());
                mbrBillDetail.setAccountId(accWithdraw.getAccountId());
                mbrBillDetail.setFinancialCode(OrderConstants.FUND_ORDER_HUPENG_ADD);
                mbrBillDetail.setOrderNo(new SnowFlake().nextId() + "");
                mbrBillDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
                mbrBillDetail.setDepotId(Constants.SYS_DEPOT_ID);
                mbrBillDetail.setAmount(accWithdraw.getDrawingAmount());

                mbrWalletService.hPWalletAdd(subWallet,mbrBillDetail );
            }else{
                mbrWalletService.castWalletAndBillDetail(billDetail.getLoginName(), billDetail.getAccountId(),
                        billDetail.getFinancialCode(), billDetail.getAmount(), accWithdraw.getOrderNo(), Boolean.TRUE, null, null);
            }

        }
    }

    private void setAccWithdrawSucToFail(AccWithdraw accWithdraw) {
        MbrBillDetail billDetail = mbrBillDetailMapper.selectByPrimaryKey(accWithdraw.getBillDetailId());
        if (Objects.nonNull(billDetail)) {
            if (accWithdraw.getMemoWithdraw() != null && accWithdraw.getMemoWithdraw().contains("LBT实际出款")) {
                mbrWalletService.castWalletAndBillDetailSucToFail(billDetail.getLoginName(), billDetail.getAccountId(),
                        OrderConstants.FUND_ORDER_ACCWITHDRAW_CG, accWithdraw.getActualArrival(), accWithdraw.getOrderNo(), Boolean.TRUE,
                        null, null, accWithdraw.getModifyUser()+"变更订单状态");
            } else {
                mbrWalletService.castWalletAndBillDetailSucToFail(billDetail.getLoginName(), billDetail.getAccountId(),
                        OrderConstants.FUND_ORDER_ACCWITHDRAW_CG, billDetail.getAmount(), accWithdraw.getOrderNo(), Boolean.TRUE,
                        null, null, accWithdraw.getModifyUser()+"变更订单状态");
            }
        }
    }


    /**
     * 	会员提款申请
     * @param withDraw
     * @param pwd
     * @param siteCode
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public void saveApply(AccWithdraw withDraw, String pwd, String siteCode, HttpServletRequest request) {

        String key = RedisConstants.ACCOUNT_WITHDRAW + siteCode + withDraw.getAccountId();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, withDraw.getAccountId(), 200, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isExpired)) {
            throw new R200Exception("任务处理中，请勿重复点击！");
        }
        try {
            // 真实姓名/状态校验
            log.info("withdrawal=={}==会员{}==取款效验开始", siteCode, withDraw.getLoginName());
            boolean check = mbrAccountService.updateOrCheckScPwd(withDraw.getAccountId(), pwd);
            if (check) {
            	// 不满足稽核条件，判断是否未通过稽核出款，是：扣除会员组出款手续费，否：抛出异常
                // 校验存款条件
                checkoutAccWithdraw(withDraw);
                
                // 是否需要手续费
                boolean isNeedFee = true;
                // 取款稽核处理
                boolean withdrawAudit = withdrawAudit(withDraw);
                // 获取是否未通过稽核出款开关
                Integer isMultipleOpen = sysSettingService.queryPaySet().getIsMultipleOpen();
                if (isMultipleOpen != null && isMultipleOpen == 1) {
                	// 稽核通过，不需要手续费
                	if (withdrawAudit) {
                		isNeedFee = false;
                	}
                }
                // 如果开关关闭，未通过稽核，直接提示无法提款
                else {
                	if (!withdrawAudit) {
                		throw new R200Exception("不满足稽核条件,无法提款");
                	}
                }
                
                // 其他费用处理(扣款金额)
                checkoutOther(withDraw);

                // 保存帐变和减少钱包余额
                MbrBillDetail mbrBillDetail = new MbrBillDetail();
                mbrBillDetail.setLoginName(withDraw.getLoginName());
                mbrBillDetail.setAccountId(withDraw.getAccountId());
                mbrBillDetail.setFinancialCode(OrderConstants.FUND_ORDER_ACCWITHDRAW);
                mbrBillDetail.setOrderNo(new SnowFlake().nextId() + "");
                mbrBillDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
                mbrBillDetail.setDepotId(Constants.SYS_DEPOT_ID);
                mbrBillDetail.setOpType(MbrBillDetail.OpTypeStatus.expenditure);
                mbrBillDetail.setAmount(withDraw.getDrawingAmount());
                
                // 提款手续费
                BigDecimal fee = new BigDecimal("0.0");
                // 取款手续费计算
                if (isNeedFee) {
                	fee = calculateFee(withDraw.getDrawingAmount(), withDraw.getAccountId()); 
                }
                mbrBillDetail.setMemo("提款金额:" + withDraw.getDrawingAmount() + ",手续费:" + CommonUtil.adjustScale(fee));

                log.info("withdrawal=={}==会员{}==取款账变开始", siteCode, withDraw.getLoginName());
                MbrWallet mbrWallet = new MbrWallet();
                mbrWallet.setAccountId(mbrBillDetail.getAccountId());
                mbrWallet.setBalance(mbrBillDetail.getAmount());
                boolean flag = mbrWalletService.walletSubtract(mbrWallet, mbrBillDetail);   // 保存帐变和减少钱包余额
                if (Boolean.FALSE.equals(flag)) {
                    throw new R200Exception("取款失败");
                }

                Integer methodType = withDraw.getMethodType();
                if (Objects.nonNull(methodType) && Constants.EVNumber.two == methodType) {
                    // 如果是支付宝提款，同样属于人民币，之前methodType只为验证区分
                    withDraw.setMethodType(Constants.EVNumber.zero);
                }
                MbrAccount account = accountMapper.selectByPrimaryKey(withDraw.getAccountId());
                // 是否免审
                Boolean isExempt = false;
                List<Integer> exemptList = Arrays.asList(0, 2);
                if (exemptList.contains(methodType)) {
                	// 获取用户的标签，获取标签配置，如果开启免审，并且提款金额小于等于配置，即：当前提款为免审
                	MbrLabel mbrLabelQuery = new MbrLabel();
                	mbrLabelQuery.setId(account.getLabelid());
                	mbrLabelQuery.setIsAvailable(true);
                	MbrLabel mbrLabel = mbrLabelService.queryObjectCond(mbrLabelQuery);
                	if (mbrLabel != null) {
                		BigDecimal exemptAmount = BigDecimal.ZERO;
                		if (methodType == 0 && mbrLabel.getIsExemptBank() != null && mbrLabel.getIsExemptBank()) {
                			exemptAmount = mbrLabel.getBankWithdrawal();
                    	}
                    	if (methodType == 2 && mbrLabel.getIsExemptAliPay() != null && mbrLabel.getIsExemptAliPay()) {
                    		exemptAmount = mbrLabel.getAliPayWithdrawal();
                    	}
                    	// 如果配置的金额大于等于提款金额，免审核
                    	// Aaron：免审核就是直接跳过2  变成3 出款中（复审）
                    	if(exemptAmount.compareTo(withDraw.getDrawingAmount()) >= 0) {
                    		isExempt = true;
                    		withDraw.setAuditUser("system");
                    		withDraw.setAuditTime(DateUtil.format(new Date()));
                    		withDraw.setMemo("免初审");
                    	}
                	}
                }

                log.info("withdrawal=={}==会员{}==保存记录开始", siteCode, withDraw.getLoginName());
                withDraw.setIsExempt(isExempt);
                // 保存取款单
                withDraw.setOrderNo(mbrBillDetail.getOrderNo());
                withDraw.setOrderPrefix(mbrBillDetail.getFinancialCode());
                withDraw.setBillDetailId(mbrBillDetail.getId());
                withDraw.setType(Constants.EVNumber.zero);      // 提款第一步都是显示手动出款
                withDraw.setCuiCount(Constants.EVNumber.zero);
                // 提款状态(0 拒绝 1 通过 2待处理 3 出款中 4自动出款人工审核 5自动出款中 6初审待定 7失败订单)
                withDraw.setStatus(Constants.EVNumber.two);
                withDraw.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
                BigDecimal actualArrival = CommonUtil.adjustScale(withDraw.getDrawingAmount().subtract(fee));   // 取款人民币
                withDraw.setActualArrival(actualArrival);
                if (Constants.EVNumber.one == withDraw.getMethodType().intValue()) {  // 加密货币 计算
                    BigDecimal actualArrivalCryptoCurrencies = CommonUtil.adjustScale(actualArrival.divide(withDraw.getExchangeRate(), 4, RoundingMode.DOWN), 4);
                    withDraw.setActualArrivalCr(actualArrivalCryptoCurrencies);
                }

                withDraw.setHandlingCharge(CommonUtil.adjustScale(fee));


                // 系统预警
                SysWarningDto sysWarningDto =  sysWarningService.getSysWarningByLoginNameAndType(account.getLoginName(), Constants.EVNumber.six);
                AccWithdraw accWithdraw = fundMapper.findLastAccWithdraw(account.getId());
                int rt = 1;
                if(sysWarningDto!=null &&  accWithdraw!=null){
                    rt = DateUtil.timeCompare(accWithdraw.getAuditTime(), sysWarningDto.getCreateTime(), FORMAT_18_DATE_TIME);
                }

                if(sysWarningDto != null && accWithdraw == null){
                    rt = -1;
                }
                // 会员预警 没有处理完
                if(rt == 1){
                    Integer count =  mbrWarningService.getMbrWarningCount(account.getLoginName());
                    if(count > 0){
                        rt = -1;
                    }
                }

                if(rt==-1){
                    withDraw.setMemo("");
                }
                super.save(withDraw);

                if (isExempt && rt >= 0) {
                	// 初始化事件
                	BizEvent bizEvent = new BizEvent(this, CommonUtil.getSiteCode(), null, null);
                	withDraw.setStatus(Constants.EVNumber.one);
                	updateAccStatus(withDraw, account.getLoginName(), bizEvent, CommonUtil.getIpAddress(request));
                }
            }
        } finally {
            redisService.del(key);
        }
    }


    /**
     * 	会员提款申请
     * @param withDraw
     * @param pwd
     * @param siteCode
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public void saveHuPengApply(AccWithdraw withDraw, String pwd, String siteCode) {

        String key = RedisConstants.ACCOUNT_HUPENG_WITHDRAW + siteCode + withDraw.getAccountId();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, withDraw.getAccountId(), 200, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isExpired)) {
            throw new R200Exception("任务处理中，请勿重复点击！");
        }
        try {
            // 真实姓名/状态校验
            boolean check = mbrAccountService.updateOrCheckScPwd(withDraw.getAccountId(), pwd);
            if (check) {

                if (fundMapper.sumApplyRec(withDraw.getAccountId()) > 0) {
                    throw new R200Exception("你最近有一笔取款正在处理中，请等待处理完成之后再申请取款");
                }

                // 保存帐变和减少钱包余额
                MbrBillDetail mbrBillDetail = new MbrBillDetail();
                mbrBillDetail.setLoginName(withDraw.getLoginName());
                mbrBillDetail.setAccountId(withDraw.getAccountId());
                mbrBillDetail.setFinancialCode(OrderConstants.FUND_ORDER_HUPENG_ACCWITHDRAW_BANK);
                mbrBillDetail.setOrderNo(new SnowFlake().nextId() + "");
                mbrBillDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
                mbrBillDetail.setDepotId(Constants.SYS_DEPOT_ID);
                mbrBillDetail.setOpType(MbrBillDetail.OpTypeStatus.expenditure);
                mbrBillDetail.setAmount(withDraw.getDrawingAmount());

                // 提款手续费
                BigDecimal fee = new BigDecimal("0.0");
                mbrBillDetail.setMemo("提款金额:" + withDraw.getDrawingAmount() + ",手续费:" + CommonUtil.adjustScale(fee));

                MbrWallet subWallet = new MbrWallet();
                subWallet.setAccountId(withDraw.getAccountId());
                subWallet.setHuPengBalance(withDraw.getDrawingAmount());
                boolean rt = mbrWalletService.hPWalletSubtract(subWallet, mbrBillDetail);
                if(!rt){
                    throw new R200Exception("取款失败");
                }

                if (Objects.nonNull(withDraw.getMethodType()) && Constants.EVNumber.two == withDraw.getMethodType()) {
                    // 如果是支付宝提款，同样属于人民币，之前methodType只为验证区分
                    withDraw.setMethodType(Constants.EVNumber.zero);
                }
                // 保存取款单
                withDraw.setOrderNo(mbrBillDetail.getOrderNo());
                withDraw.setOrderPrefix(mbrBillDetail.getFinancialCode());
                withDraw.setBillDetailId(mbrBillDetail.getId());
                withDraw.setType(Constants.EVNumber.zero);      // 提款第一步都是显示手动出款
                withDraw.setStatus(Constants.EVNumber.two);
                withDraw.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
                BigDecimal actualArrival = CommonUtil.adjustScale(withDraw.getDrawingAmount().subtract(fee));   // 取款人民币
                withDraw.setActualArrival(actualArrival);
                withDraw.setSource(Constants.EVNumber.one);

                withDraw.setHandlingCharge(CommonUtil.adjustScale(fee));
                super.save(withDraw);
            }
        } finally {
            redisService.del(key);
        }
    }


    private void checkoutOther(AccWithdraw withDraw) {
        // 其他费用处理(扣款金额)
        withDraw.setDiscountAmount(BigDecimal.ZERO);
        withDraw.setCutAmount(BigDecimal.ZERO);
        withDraw.setHandlingCharge(nonNull(withDraw.getHandlingCharge()) ?
                withDraw.getHandlingCharge() : BigDecimal.ZERO);
        BigDecimal bigDecimal1 = withDraw.getCutAmount().add(nonNull(withDraw.getDiscountAmount())
                ? withDraw.getDiscountAmount() : BigDecimal.ZERO);
        withDraw.setActualArrival(withDraw.getDrawingAmount().subtract(bigDecimal1));

        BigDecimal bigDecimal = withDraw.getCutAmount().add(withDraw.getDiscountAmount());
        if (bigDecimal.compareTo(withDraw.getDrawingAmount()) == 1) {
            throw new R200Exception("提款金额必须大于需要扣款的金额:" + bigDecimal + "元");
        }
    }

    private void checkoutAccWithdraw(AccWithdraw withDraw) {
        MbrAccount account = accountMapper.selectByPrimaryKey(withDraw.getAccountId());
        sysSettingService.checkPayCondition(account, SystemConstants.WITHDRAW_CONDITION);
        if (account.getAvailable() == Constants.EVNumber.two) {
            throw new R200Exception("余额冻结，不可以提款");
        }
        if (fundMapper.sumApplyRec(withDraw.getAccountId()) > 0) {
            throw new R200Exception("你最近有一笔取款正在处理中，请等待处理完成之后再申请取款");
        }

        if (Constants.EVNumber.one == withDraw.getMethodType().intValue()) {  // 加密货币
            MbrCryptoCurrencies mbrCryptoCurrencies = new MbrCryptoCurrencies();
            mbrCryptoCurrencies.setId(withDraw.getCryptoCurrenciesId());
            mbrCryptoCurrencies.setAccountId(withDraw.getAccountId());
            mbrCryptoCurrencies.setIsDel(Available.disable);
            mbrCryptoCurrencies = mbrCryptoCurrenciesService.queryObjectCond(mbrCryptoCurrencies);
            if (Objects.isNull(mbrCryptoCurrencies)) {
                throw new R200Exception("无此钱包,不能取款");
            }
        	// 查询fund_merchant_pay.currencyprotocol，判断是否包含用户的mbr_cryptocurrencies.currencyprotocol ，如果不包含，即；当前通道关闭
            String currencyProtocol = mbrCryptoCurrencies.getCurrencyProtocol();
            FundMerchantPay fundMerchantPay = new FundMerchantPay();
            fundMerchantPay.setAvailable(1);
            fundMerchantPay.setMethodType(1);
            List<FundMerchantPay> list = fundMapper.findFundMerchantPayList(fundMerchantPay);
            Boolean channlIsOpen = false;
            for (FundMerchantPay target : list) {
            	if (target.getCurrencyProtocol().equalsIgnoreCase(currencyProtocol)) {
            		channlIsOpen = true;
            		break;
            	}
			}
            if (!channlIsOpen) {
            	throw new R200Exception("当前取款通道已关闭，请重新选择");
            }
            
        } else {
            MbrBankcard mbrBankcard = new MbrBankcard();
            mbrBankcard.setId(withDraw.getBankCardId());
            mbrBankcard.setAccountId(withDraw.getAccountId());
            mbrBankcard.setIsDel(Available.disable);
            mbrBankcard = mbrBankcardService.queryObjectCond(mbrBankcard);
            if (Objects.isNull(mbrBankcard)) {
                throw new R200Exception("无此银行卡,不能取款");
            }
        }

        MbrWithdrawalCond cond = withdrawalCond.getMbrWithDrawal(withDraw.getAccountId());

        if (nonNull(cond)) {
            if (nonNull(withDraw.getExchangeRate()) && nonNull(cond.getLowUsdt()) && nonNull(cond.getTopUsdt())) { //设置了加密货币限额，则用加密货币的 针对历史数据处理
                if (!StringUtils.isEmpty(cond.getLowQuota()) && withDraw.getDrawingAmount().compareTo(cond.getLowUsdt()) == -1) {
                    throw new R200Exception("加密货币取款金额小于最低取款金额");
                }
                if (!StringUtils.isEmpty(cond.getTopQuota()) && withDraw.getDrawingAmount().compareTo(cond.getTopUsdt()) == 1) {
                    throw new R200Exception("加密货币取款金额大于最高取款金额");
                }
            } else if (withDraw.getMethodType() == Constants.EVNumber.two && nonNull(cond.getLowAlipayQuota()) && nonNull(cond.getTopAlipayQuota())) { // 如果是支付宝取款则判断支付宝限额
                if (!StringUtils.isEmpty(cond.getLowAlipayQuota())
                        && withDraw.getDrawingAmount().compareTo(cond.getLowAlipayQuota()) == -1) {
                    throw new R200Exception("取款金额小于最低取款金额");
                }
                if (!StringUtils.isEmpty(cond.getTopAlipayQuota())
                        && withDraw.getDrawingAmount().compareTo(cond.getTopAlipayQuota()) == 1) {
                    throw new R200Exception("取款金额大于最高取款金额");
                }
            } else {
                if (!StringUtils.isEmpty(cond.getLowQuota())
                        && withDraw.getDrawingAmount().compareTo(cond.getLowQuota()) == -1) {
                    throw new R200Exception("取款金额小于最低取款金额");
                }
                if (!StringUtils.isEmpty(cond.getTopQuota())
                        && withDraw.getDrawingAmount().compareTo(cond.getTopQuota()) == 1) {
                    throw new R200Exception("取款金额大于最高取款金额");
                }
            }

            // 校验取款次数校验，取款金额
	        if (cond.getFeeAvailable()!= null && cond.getFeeAvailable() == 1) {
	        	// 获取会员已取款次数和金额
	        	AccWithdraw withdraw = this.sumWithDraw(withDraw.getAccountId());
	        	
	        	// 已提款的金额
	        	BigDecimal drawingAmount = withdraw.getDrawingAmount();
	        	// 总可提款金额 - 已提款金额 = 还可以提款的金额
	        	BigDecimal withDrawalQuota = cond.getWithDrawalQuota().subtract(drawingAmount);
	        	// 如果本次提款金额大于还可以提款的金额
	        	if (withDraw.getDrawingAmount().compareTo(withDrawalQuota) == 1) {
	        		// 如果可提现余额大于0
	        		if (BigDecimal.ZERO.compareTo(withDrawalQuota) == 1) {
	        			throw new R200Exception("今日提款金额大于会员组的提现金额，今日还可以提款"+ withDrawalQuota + "元，提现失败");
	        		} else {
	        			throw new R200Exception("今日提款金额大于会员组的提现金额，提现失败");
	        		}
	        	}
	        	// 总可提现次数
	        	Integer withdrawCount = withdraw.getWithdrawCount();
	        	// 已提现次数
	        	if (withdrawCount >= cond.getWithDrawalTimes()) {
	        		throw new R200Exception("今日提款次数大于会员组的提现次数，提现失败");
	        	}
	        }
        }
    }

    private FundMerchantPay getFundMerchantPay(AccWithdraw withDraw, MbrBankcard card) {
        FundMerchantPay merchantPay = new FundMerchantPay();
        if (nonNull(card) && nonNull(card.getBankName()) && "支付宝".equals(card.getBankName())) {
            merchantPay.setMethodType(Constants.EVNumber.two);
        } else {
            merchantPay.setMethodType(withDraw.getMethodType());
        }
        merchantPay.setAvailable(Constants.EVNumber.one);

        if (Constants.EVNumber.one == withDraw.getMethodType().intValue()) {    // 加密货币
            MbrCryptoCurrencies mbrCryptoCurrencies = mbrCryptoCurrenciesMapper.selectByPrimaryKey(withDraw.getCryptoCurrenciesId());
            merchantPay.setCurrencyCode(mbrCryptoCurrencies.getCurrencyCode());
            merchantPay.setCurrencyProtocol(mbrCryptoCurrencies.getCurrencyProtocol());
        }
        if (Constants.EVNumber.three == withDraw.getMethodType().intValue()) {
            MbrBankcard bankcard = mbrBankcardMapper.selectByPrimaryKey(withDraw.getBankCardId());
            merchantPay.setCurrencyCode(bankcard.getBankName());
        }
        return merchantPayMapper.selectOne(merchantPay);
    }

    private void beginMerchantPayment(AccWithdraw withDraw) {
        // 添加了支付宝出款渠道，通过银行名称设置methodType=2获取支付宝出款渠道
        MbrBankcard bankcard = mbrBankcardMapper.selectByPrimaryKey(withDraw.getBankCardId());
        log.info("进入beginMerchantPayment==withDraw：" + JSON.toJSONString(withDraw));

        // 判断该会员是否符合代付配置
        Boolean isPayment = checkoutMerchantPayment(withDraw, bankcard);
        if (Boolean.FALSE.equals(isPayment)) {
            withDraw.setType(Constants.EVNumber.zero);
            accWithdrawMapper.updateByPrimaryKey(withDraw);
            return;
        }
        // 获取代付渠道配置
        FundMerchantPay merchantPay = getFundMerchantPay(withDraw, bankcard);
        log.info("获取代付渠道配置==结果==" + JSON.toJSONString(merchantPay));
        TChannelPay platform = channelPayMapper.selectByPrimaryKey(merchantPay.getChannelId());

        if (Constants.EVNumber.one == withDraw.getMethodType().intValue()) {    // 加密货币
            evelletPayment(withDraw, merchantPay);
        } else {  // 银行卡
            // 盘子
            if (MerchantPayConstants.PAY_PANZI.equals(platform.getPlatformCode())) {
                panZiPayment(withDraw, merchantPay, bankcard);
            }
            // ONEPAY
            if (MerchantPayConstants.ONE_PAY.equals(platform.getPlatformCode())) {
                onePayPayment(withDraw, merchantPay, bankcard);
            }
            // Payment代付
            if (MerchantPayConstants.PAYMENT_PAY.equals(platform.getPlatformCode())) {
                paymentPayment(withDraw, merchantPay, bankcard);
            }
            // LBT
            if (MerchantPayConstants.LBT_PAY.equals(platform.getPlatformCode())) {
                lbtPayment(withDraw, merchantPay, bankcard);
            }

            if(MerchantPayConstants.PAY_CENTER.equals(platform.getPlatformCode())){
                centerPayment(withDraw, merchantPay, bankcard);
            }
        }

    }

    private void evelletPayment(AccWithdraw withDraw, FundMerchantPay merchantPay) {
        log.info("evelletPayment==createuser==" + withDraw.getCreateUser() + "==order==" + withDraw.getOrderNo());
        MbrAccount account = accountMapper.selectByPrimaryKey(withDraw.getAccountId());
        MbrCryptoCurrencies mbrCryptoCurrencies = mbrCryptoCurrenciesMapper.selectByPrimaryKey(withDraw.getCryptoCurrenciesId());

        String result = cryptoCurrenciesService.evelletPayment(merchantPay,
                withDraw.getOrderNo(), withDraw.getActualArrivalCr(), account.getLoginName(),
                mbrCryptoCurrencies.getWalletAddress(), withDraw.getCreateUser(), Constants.TYPE_ACCOUNT);
        if (StringUtils.isEmpty(result)) {
            // 更新为无法判断状态：自动出款、自动出款中
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交evellet代付返回null,程序无法判断是否成功");
            return;
        }

        CommonEvelletResponse response = new Gson().fromJson(result, CommonEvelletResponse.class);
        if (isNull(response) || isNull(response.getCode())) {
            // 更新为无法判断状态：自动出款、自动出款中
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交evellet代付返回数据错误,程序无法判断是否成功");
            return;
        }
        // 提单成功
        if (Integer.valueOf("200").equals(response.getCode())) {
            log.info("evelletPayment==createuser==" + withDraw.getCreateUser() + "==order==" + withDraw.getOrderNo()
                    + "提单成功");
            succeedMerchantPay(merchantPay, withDraw, "1", null, withDraw.getOrderNo());
        } else {  // 提单失败
            // 变更为手动出款
            log.info("evelletPayment==createuser==" + withDraw.getCreateUser() + "==order==" + withDraw.getOrderNo()
                    + "提单失败");
            withDraw.setMemo("提交Payment代付失败,状态描述：" + response.getMsg());
            updateMerchantPaymentWithdraw(withDraw);
            return;
        }

    }

    private void panZiPayment(AccWithdraw withDraw, FundMerchantPay merchantPay, MbrBankcard bankcard) {
        String bankCode = MerchantPayConstants.pzMerchantPayMap.get(bankcard.getBankName());
        if (StringUtil.isEmpty(bankCode) || StringUtil.isEmpty(bankcard.getRealName())
                || StringUtil.isEmpty(bankcard.getCardNo())) {
            updateMerchantPaymentWithdraw(withDraw);
            return;
        }
        PZQueryResponseDto responseDto = panZiPayService.debitPayment(withDraw.getOrderNo(),
                bankCode, bankcard, withDraw.getActualArrival(), merchantPay);
        if (isNull(responseDto)) {
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交代付返回null,程序无法判断是否成功");
            return;
        }
        if (Boolean.TRUE.equals(responseDto.getSuccess())) {
            PZPaymentResponseDto content = jsonUtil.fromJson(jsonUtil.toJson(responseDto.getContent()), PZPaymentResponseDto.class);
            succeedMerchantPay(merchantPay, withDraw, "1", null, content.getOut_trade_no());
        } else {
            updateMerchantPaymentWithdraw(withDraw);
        }
    }

    private void onePayPayment(AccWithdraw withDraw, FundMerchantPay merchantPay, MbrBankcard bankcard) {
        String siteCode = CommonUtil.getSiteCode();
        String orderNo = withDraw.getOrderNo();
        OnePayResponseDto responseDto = onePayService.debitPayment(orderNo, bankcard, withDraw.getActualArrival(), merchantPay, siteCode);
        if (isNull(responseDto)) {
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交onePay代付返回null,程序无法判断是否成功");
        } else if ("0".equals(responseDto.getStatus())) {//提交成功
            succeedMerchantPay(merchantPay, withDraw, "1", responseDto.getOrdersid(), withDraw.getOrderNo());
        } else {//提交失败
            if (StringUtil.isEmpty(withDraw.getMemo())) {
                withDraw.setMemo("提交代付失败,失败原因：" + responseDto.getMsg());
            } else {
                withDraw.setMemo(withDraw.getMemo() + ">>提交代付失败,失败原因：" + responseDto.getMsg());
            }
            updateMerchantPaymentWithdraw(withDraw);
            log.info(withDraw.getOrderNo() + "提交代付失败,失败原因：" + responseDto.getMsg());
        }
    }

    // Payment 代付
    private void paymentPayment(AccWithdraw withDraw, FundMerchantPay merchantPay, MbrBankcard bankcard) {
        // 校验：不支持中国邮政
        String bankCode = MerchantPayConstants.paymentMerchantPayMap.get(bankcard.getBankName());
        if (StringUtil.isNotEmpty(bankCode)) {
            withDraw.setMemo("Payment代付：不支持邮政银行！");
            // 变更为手动出款
            updateMerchantPaymentWithdraw(withDraw);
            return;
        }
        // 校验：银行账号信息不完整
        if (StringUtil.isEmpty(bankcard.getRealName()) || StringUtil.isEmpty(bankcard.getCardNo())) {
            withDraw.setMemo("Payment代付：银行账号信息不完整！");
            updateMerchantPaymentWithdraw(withDraw);
            return;
        }
        // 校验：代付余额   不需要
//        String result = paymentPayService.balance(merchantPay);
//        if (StringUtils.isEmpty(result)) {
//            withDraw.setMemo("payment代付：余额查询查询返回空！");
//            updateMerchantPaymentWithdraw(withDraw);
//            return;
//        }
//        PaymentPayBaseResponseDto response =  jsonUtil.fromJson(result, PaymentPayBaseResponseDto.class);
//        if (isNull(response) || isNull(response.getStatus()) ) {
//            withDraw.setMemo("payment代付：余额查询返回数据错误！");
//            updateMerchantPaymentWithdraw(withDraw);
//            return;
//        }
//        if (!Integer.valueOf(Constants.EVNumber.one).equals(response.getStatus())) {
//            withDraw.setMemo("payment代付：" + response.getMsg());
//            updateMerchantPaymentWithdraw(withDraw);
//            return;
//        }
//        PaymentPayBalanceResponseDto paymentPayBalanceResponseDto = jsonUtil.fromJson(jsonUtil.toJson(response.getData()), PaymentPayBalanceResponseDto.class);
//        if (isNull(paymentPayBalanceResponseDto) || isNull(paymentPayBalanceResponseDto.getBalance())) {
//            withDraw.setMemo("payment代付：余额查询查询返回数据格式错误！");
//            updateMerchantPaymentWithdraw(withDraw);
//            return;
//        }
//        if ( withDraw.getActualArrival().compareTo(paymentPayBalanceResponseDto.getBalance()) == 1) {
//            withDraw.setMemo("payment代付：余额不足！");
//            updateMerchantPaymentWithdraw(withDraw);
//            return;
//        }
//
        // 提交代付申请: 提单了，不管结果怎样，都是自动出款、自动出款中，等回调或者让人工去干涉
        String siteCode = CommonUtil.getSiteCode();
        String orderNo = withDraw.getOrderNo();
        String result = paymentPayService.debitPayment(orderNo, bankcard, withDraw.getActualArrival(), merchantPay, siteCode);
        if (StringUtils.isEmpty(result)) {
            // 更新为无法判断状态：自动出款、自动出款中
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交Payment代付返回null,程序无法判断是否成功");
            return;
        }
        PaymentPayBaseResponseDto response = jsonUtil.fromJson(result, PaymentPayBaseResponseDto.class);
        if (isNull(response) || isNull(response.getStatus())) {
            // 更新为无法判断状态：自动出款、自动出款中
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交Payment代付返回数据错误,程序无法判断是否成功");
            return;
        }
        // 错误状态处理
        if (!Integer.valueOf(Constants.EVNumber.one).equals(response.getStatus())) {
            // 判断不成功的：参数错误2/订单号已存在4/验签失败5/支付失败7/余额不足10/11/12/14/15/16/17/19/21
            if (Integer.valueOf(paymentStatusCode.two).equals(response.getStatus())
                    || Integer.valueOf(paymentStatusCode.four).equals(response.getStatus()) || Integer.valueOf(paymentStatusCode.five).equals(response.getStatus())
                    || Integer.valueOf(paymentStatusCode.seven).equals(response.getStatus()) || Integer.valueOf(paymentStatusCode.ten).equals(response.getStatus())
                    || Integer.valueOf(paymentStatusCode.eleven).equals(response.getStatus()) || Integer.valueOf(paymentStatusCode.twentyone).equals(response.getStatus())
                    || Integer.valueOf(paymentStatusCode.twelve).equals(response.getStatus()) || Integer.valueOf(paymentStatusCode.fourteen).equals(response.getStatus())
                    || Integer.valueOf(paymentStatusCode.fiveteen).equals(response.getStatus()) || Integer.valueOf(paymentStatusCode.sixteen).equals(response.getStatus())
                    || Integer.valueOf(paymentStatusCode.seventeen).equals(response.getStatus()) || Integer.valueOf(paymentStatusCode.nineteen).equals(response.getStatus())) {

                // 变更为手动出款
                withDraw.setMemo("提交Payment代付失败,状态描述：" + response.getMsg() + "=状态码：" + response.getStatus());
                updateMerchantPaymentWithdraw(withDraw);
                return;
            }

            // 无法判断是否成功的  其他的：  // 未知错误/订单未处理/处理中/触发风控/未知异常/订单号不存在....
            // 更新为无法判断状态：自动出款、自动出款中
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交Payment代付返回失败,程序无法判断是否成功，状态描述：" + response.getMsg() + "=状态码：" + response.getStatus());
            return;

        } else {  // 提单成功
            PaymentPayExecuteResponseDto paymentPayExecuteResponseDto = jsonUtil.fromJson(jsonUtil.toJson(response.getData()), PaymentPayExecuteResponseDto.class);
            String orderid = "";
            if (Objects.isNull(paymentPayExecuteResponseDto) || StringUtils.isEmpty(paymentPayExecuteResponseDto.getEventNumber())) {
                orderid = "Payment代付事件单号返回空";
            } else {
                orderid = paymentPayExecuteResponseDto.getEventNumber();
            }
            succeedMerchantPay(merchantPay, withDraw, "1", orderid, withDraw.getOrderNo());
        }
    }

    // LBT代付

    private void lbtPayment(AccWithdraw withDraw, FundMerchantPay merchantPay, MbrBankcard bankcard) {
        if ("深发/平安银行".equals(bankcard.getBankName())) {
            bankcard.setBankName("平安银行");
        }
        // 校验：银行账号信息不完整
        if (StringUtil.isEmpty(bankcard.getRealName()) || StringUtil.isEmpty(bankcard.getCardNo())) {
            withDraw.setMemo("Payment代付：银行账号信息不完整！");
            // 变更为手动出款
            updateMerchantPaymentWithdraw(withDraw);
            return;
        }

        // 提交代付申请: 提单了，不管结果怎样，都是自动出款、自动出款中，等回调或者让人工去干涉
        String result = lbtPayService.debitPayment(bankcard, withDraw, merchantPay);
        if (StringUtils.isEmpty(result)) {
            // 更新为无法判断状态：自动出款、自动出款中
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交LBT代付返回null,程序无法判断是否成功");
            return;
        }

        LBTReponseDto response = jsonUtil.fromJson(result, LBTReponseDto.class);
        if (isNull(response)) {
            // 更新为无法判断状态：自动出款、自动出款中
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交LBT代付返回数据错误,程序无法判断是否成功!");
            return;
        }
        if (Objects.isNull(response.getCode())) {
            // 更新为无法判断状态：自动出款、自动出款中
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交LBT代付返回数据错误,程序无法判断是否成功!!");
            return;
        }
        if (Integer.valueOf(200).equals(response.getCode())) {    // 成功
            // 成功
            succeedMerchantPay(merchantPay, withDraw, "1", null, withDraw.getOrderNo());
            log.info("LBT代付==outTradeNo==" + withDraw.getOrderNo() + "==成功");
        } else {
            // 变更为手动出款
            withDraw.setMemo("提交LBT代付失败,状态描述：" + response.getMsg() + "=状态码：" + response.getCode() + "=msg=" + response.getMsg());
            updateMerchantPaymentWithdraw(withDraw);
            return;
        }
    }

    private void centerPayment(AccWithdraw withDraw, FundMerchantPay merchantPay, MbrBankcard bankcard) {
        String result = payCenterService.debitPayment(bankcard, withDraw, merchantPay);
        if (StringUtils.isEmpty(result)) {
            // 更新为无法判断状态：自动出款、自动出款中
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交支付中心代付返回null,程序无法判断是否成功");
            return;
        }

        Type jsonType = new TypeToken<CommonDPayResponseDto<DPayResponseDto>>() {
        }.getType();

        CommonDPayResponseDto<DPayResponseDto> payCenterResponseDto = jsonUtil.fromJson(result, jsonType);
        if (isNull(payCenterResponseDto)) {
            // 更新为无法判断状态：自动出款、自动出款中
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交支付中心代付返回数据错误,程序无法判断是否成功!");
            return;
        }

        if (Objects.isNull(payCenterResponseDto.getCode()) || Objects.isNull(payCenterResponseDto.getData())) {
            // 更新为无法判断状态：自动出款、自动出款中
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交支付中心代付返回数据错误,程序无法判断是否成功!!");
            return;
        }

       int code =  payCenterResponseDto.getCode();
        boolean flag = false;
        if (Integer.valueOf(200) == code ) {    // 成功
            // 成功
            DPayResponseDto dPayResponseDto = payCenterResponseDto.getData();
            if(dPayResponseDto.getSucceed()){
                flag = true;
            }
        }
        if(flag){
            succeedMerchantPay(merchantPay, withDraw, "1", null, withDraw.getOrderNo());
            log.info("支付中心代付==outTradeNo==" + withDraw.getOrderNo() + "==成功");
        }
        else {
            // 变更为手动出款
            withDraw.setMemo("提交支付中心代付失败,状态描述：" + payCenterResponseDto.getMsg()  + "=状态码：" + payCenterResponseDto.getCode()  + "=msg=" + payCenterResponseDto.getMsg());
            //updateMerchantPaymentWithdraw(withDraw);

        }
    }
    /**
     * 提交出款代付成功处理
     *
     * @param merchantPay
     * @param withDraw
     * @param bankStatus
     * @param orderId
     * @param transId
     */
    private void succeedMerchantPay(FundMerchantPay merchantPay, AccWithdraw withDraw, String bankStatus, String orderId, String transId) {
        FundMerchantDetail merchantDetail = new FundMerchantDetail();
        merchantDetail.setMerchantId(merchantPay.getId());
        merchantDetail.setMerchantName(merchantPay.getMerchantName());
        merchantDetail.setMerchantNo(merchantPay.getMerchantNo());
        merchantDetail.setBankStatus(bankStatus);
        merchantDetail.setOrderId(orderId);
        merchantDetail.setTransId(transId);
        merchantDetail.setAccWithdrawId(withDraw.getId());
        withDraw.setStatus("2".equals(bankStatus) || "SUCCESS".equals(bankStatus) ?
                Constants.EVNumber.one : Constants.EVNumber.five);
        if ("2".equals(bankStatus) || "SUCCESS".equals(bankStatus)) {
            withDraw.setPassTime(getCurrentDate(FORMAT_18_DATE_TIME));
            withDraw.setPassUser(Constants.SYSTEM_PASSUSER);
        }
        withDraw.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        withDraw.setModifyUser(Constants.SYSTEM_PASSUSER);
        withDraw.setType(Constants.EVNumber.one);
        accWithdrawMapper.updateByPrimaryKey(withDraw);
        merchantDetailMapper.insert(merchantDetail);
    }

    private void updateMerchantPaymentWithdraw(AccWithdraw withDraw) {
        withDraw.setStatus(Constants.EVNumber.three);
        withDraw.setType(Constants.EVNumber.zero);
        accWithdrawMapper.updateByPrimaryKey(withDraw);
    }

    private void updateWithdrawErroMsg(AccWithdraw withDraw, Integer type, String memo) {
        withDraw.setStatus(Constants.EVNumber.five);
        withDraw.setType(type);
        if (StringUtil.isEmpty(withDraw.getMemo())) {
            withDraw.setMemo(memo);
        } else {
            withDraw.setMemo(withDraw.getMemo() + ">>" + memo);
        }
        accWithdrawMapper.updateByPrimaryKey(withDraw);
    }

    private Boolean checkoutMerchantPayment(AccWithdraw withDraw, MbrBankcard card) {
        SysSetting setting = sysSettingService.getSysSetting(SystemConstants.PAY_AUTOMATIC);
        SysSetting moneySetting = sysSettingService.getSysSetting(SystemConstants.PAY_MONEY);
        if (isNull(setting) || "0".equals(setting.getSysvalue())) {
            return Boolean.FALSE;
        }
        if (nonNull(moneySetting) && withDraw.getDrawingAmount()
                .compareTo(new BigDecimal(moneySetting.getSysvalue())) == 1) {
            return Boolean.FALSE;
        }
        int count = fundMapper.findMerchantPayCount(withDraw.getAccountId());
        if (count == 0) {
            return Boolean.FALSE;
        }
        FundMerchantPay merchantPay = getFundMerchantPay(withDraw, card);
        log.info("判断该会员是否符合代付配置==结果==" + JSON.toJSONString(merchantPay));
        if (isNull(merchantPay) || isNull(withDraw.getWithdrawSource()) || !merchantPay.getDevSource().contains(withDraw.getWithdrawSource().toString())) {
            return Boolean.FALSE;
        }
        TChannelPay channelPay = channelPayMapper.selectByPrimaryKey(merchantPay.getChannelId());
        if (isNull(channelPay) || channelPay.getAvailable() == Constants.EVNumber.zero) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Async("getPayResultExecutor")
    public void updateMerchantPayment(AccWithdraw withdraw, String siteCode) {
        ThreadLocalCache.setSiteCodeAsny(siteCode);
        String redisKey = RedisConstants.UPDATE_WITHDRAW + siteCode + withdraw.getId();
        boolean flag = redisService.setRedisExpiredTimeBo(redisKey, withdraw.getId(), 200, TimeUnit.SECONDS);
        if (flag) {
            try {
                AccWithdraw accWithdraw = accWithdrawMapper.selectByPrimaryKey(withdraw.getId());
                if (accWithdraw.getStatus() == Constants.EVNumber.five) {   // 5自动出款中 的状态才去查询，此处不会查询未代付下单成功的，因为未成功的没有detail.
                    // 通过订单号获取代付配置
                    FundMerchantPay merchantPay = fundMapper.getMerchantPayByOrderno(withdraw.getOrderNo());
                    if (Objects.isNull(merchantPay)) {  // 非代付的取款单
                        return;
                    }
                    TChannelPay platform = channelPayMapper.selectByPrimaryKey(merchantPay.getChannelId());
                    if (MerchantPayConstants.PAY_PANZI.equals(platform.getPlatformCode())) {
                        updatePanZiMerchantPayment(merchantPay, withdraw, siteCode);
                    }
                    if (MerchantPayConstants.ONE_PAY.equals(platform.getPlatformCode())) {
                        updateOnePayMerchantPayment(merchantPay, withdraw, siteCode);
                    }
                    // Payment代付
                    if (MerchantPayConstants.PAYMENT_PAY.equals(platform.getPlatformCode())) {
                        updatePaymentPayMerchantPayment(merchantPay, withdraw, siteCode);
                    }
                    // EVELLET
                    if (MerchantPayConstants.EVELLET_PAY.equals(platform.getPlatformCode())) {
                        updateEvelltetPayMerchantPayment(merchantPay, withdraw, siteCode);
                    }
                    // LBT
                    if (MerchantPayConstants.LBT_PAY.equals(platform.getPlatformCode())) {
                        updateLBTPayMerchantPayment(merchantPay, withdraw, siteCode);
                    }
                    // 支付中心
                    if(MerchantPayConstants.PAY_CENTER.equals(platform.getPlatformCode())){
                        updateCenterPayMerchantPayment(merchantPay, withdraw, siteCode);
                    }
                }
            } finally {
                redisService.del(redisKey);
            }
        }
    }

    private void updatePanZiMerchantPayment(FundMerchantPay merchantPay, AccWithdraw as, String siteCode) {
        PZQueryContentDto responseDto = panZiPayService.debitQuery(as, merchantPay);
        if (nonNull(responseDto) && StringUtil.isNotEmpty(responseDto.getTrade_status())) {
            if ("SUCCESS".equals(responseDto.getTrade_status())) {
                updateMerchantPaymentStatus(Constants.EVNumber.one, as.getId(), as.getMerchantDetailId(), "2", as.getAccountId(), null, null);
                sendWithdrawMsg(siteCode, as.getAccountId());
            }
            if ("FAIL".equals(responseDto.getTrade_status()) || "REFUND".equals(responseDto.getTrade_status())) {
                updateMerchantPaymentStatus(Constants.EVNumber.three, as.getId(), as.getMerchantDetailId(), "3", as.getAccountId(), null, null);
            }
        }
    }

    /**
     * onePay定时异步查询
     *
     * @param merchantPay
     * @param as
     * @param siteCode
     */
    private void updateOnePayMerchantPayment(FundMerchantPay merchantPay, AccWithdraw as, String siteCode) {
        String orderNo = as.getOrderNo();
        OnePayResponseDto responseDto = onePayService.querySubmitSuccess(orderNo, merchantPay);
        if (nonNull(responseDto) && "0".equals(responseDto.getStatus())) {
            String orderStatus = responseDto.getOrder_status();
            if ("3".equals(orderStatus)) {//出款成功
                updateMerchantPaymentStatus(Constants.EVNumber.one, as.getId(), as.getMerchantDetailId(), "2", as.getAccountId(), null, null);
                sendWithdrawSuccessMsg(siteCode, as.getAccountId(), new BigDecimal(responseDto.getMoney()));
            } else if ("4".equals(orderStatus)) {//出款失败
                updateMerchantPaymentStatus(Constants.EVNumber.three, as.getId(), as.getMerchantDetailId(), "3", as.getAccountId(), null, null);
            }
        }
    }

    // Payment代付查询订单状态
    private void updatePaymentPayMerchantPayment(FundMerchantPay merchantPay, AccWithdraw as, String siteCode) {
        String orderNo = as.getOrderNo();
        String result = paymentPayService.querySubmitSuccess(orderNo, merchantPay);
        if (StringUtil.isNotEmpty(result)) {
            PaymentPayBaseResponseDto response = jsonUtil.fromJson(result, PaymentPayBaseResponseDto.class);
            if (Objects.nonNull(response) && Objects.nonNull(response.getData())) {
                PaymentPayResponseDto paymentPayResponseDto = jsonUtil.fromJson(jsonUtil.toJson(response.getData()), PaymentPayResponseDto.class);
                // 出款成功
                if (Objects.nonNull(paymentPayResponseDto) && Integer.valueOf(Constants.EVNumber.one).equals(paymentPayResponseDto.getPayStatus())) {
                    log.info("Payment代付==outTradeNo==" + orderNo + "==订单查询==成功");
                    updateMerchantPaymentStatus(Constants.EVNumber.one, as.getId(), as.getMerchantDetailId(), "2", as.getAccountId(), null, null);
                    sendWithdrawSuccessMsg(siteCode, as.getAccountId(), paymentPayResponseDto.getAmount());
                }
                // 出款失败
                if (Objects.nonNull(paymentPayResponseDto) && Integer.valueOf(Constants.EVNumber.zero).equals(paymentPayResponseDto.getPayStatus())) {
                    log.info("Payment代付==outTradeNo==" + orderNo + "==订单查询==失败");
                    updateMerchantPaymentStatus(Constants.EVNumber.three, as.getId(), as.getMerchantDetailId(), "3", as.getAccountId(), null, null);
                }
            }
        }
    }

    // LBT代付查询订单状态
    private void updateLBTPayMerchantPayment(FundMerchantPay merchantPay, AccWithdraw as, String siteCode) {
        String result = lbtPayService.querySubmitSuccess(as, merchantPay);
        if (StringUtil.isNotEmpty(result)) {
            LBTQueryReponseDto response = jsonUtil.fromJson(result, LBTQueryReponseDto.class);
            if (Objects.nonNull(response) && Objects.nonNull(response.getStatus())) {
                // 出款成功
                if ("APPROVED".equals(response.getStatus())) {
                    log.info("LBT代付==outTradeNo==" + as.getOrderNo() + "==订单查询==成功");
                    String remarks = response.getRemarks();
                    if (StringUtil.isEmpty(remarks) || remarks.length() > 400) {
                        remarks = "LBT代付：审核通过";
                    }
                    // 如果出款金额比订单金额小，补回会员金额，加入备注
                    BigDecimal realAmount = BigDecimal.valueOf(Double.valueOf(response.getAmount()));
                    if (as.getActualArrival().compareTo(realAmount) == 1) {
                        BigDecimal returnAmount = as.getActualArrival().subtract(realAmount);
                        // 退回金额，加入账变记录
                        remarks = remarks.concat("；提款金额").concat(as.getActualArrival().toString()).concat(" LBT实际出款").concat(realAmount.toString())
                                .concat(" 退回").concat(returnAmount.toString());
                        mbrWalletService.castWalletAndBillDetailSucToFail(as.getLoginName(), as.getAccountId(), OrderConstants.FUND_ORDER_CODE_AA,
                                returnAmount, as.getOrderNo(), Boolean.TRUE, null, null, remarks);
                        updateMerchantPaymentStatusForNewAmount(Constants.EVNumber.one, as.getId(), as.getMerchantDetailId(), "2",
                                as.getAccountId(), realAmount, remarks, null);
                    } else {
                        updateMerchantPaymentStatus(Constants.EVNumber.one, as.getId(), as.getMerchantDetailId(), "2", as.getAccountId(), remarks, null);
                    }
                    //sendWithdrawSuccessMsg(siteCode, as.getAccountId(), as.getActualArrival());
                    sendWithdrawSuccessMsg(siteCode, as.getAccountId(), realAmount);
                }
                // 出款失败
//                if("REJECTED".equals(response.getStatus())){
//                    log.info("Payment代付==outTradeNo==" + as.getOrderNo() + "==订单查询==失败");
//                    updateMerchantPaymentStatus(Constants.EVNumber.three, as.getId(), as.getMerchantDetailId(), "3", as.getAccountId(),null);
//                }
                if ("REJECTED".equals(response.getStatus())) {    // LBT出款拒绝直接上分
                    log.info("LBT代付==outTradeNo==" + as.getOrderNo() + "==订单查询==失败");
                    AccWithdraw withdraw = accWithdrawMapper.selectByPrimaryKey(as.getId());
                    // 处理失败
                    withdraw.setModifyUser(Constants.SYSTEM_USER);
                    withdraw.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    withdraw.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    withdraw.setAuditUser(Constants.SYSTEM_USER);
                    withdraw.setStatus(Constants.EVNumber.zero);    // 拒绝状态

                    String remarks = response.getRemarks();
                    if (StringUtil.isEmpty(remarks) || remarks.length() > 400) {
                        remarks = "LBT代付：审核拒绝";
                    }
                    withdraw.setMemoWithdraw(remarks); // 复审备注
                    // 重新上分
                    setAccWithdrawZreo(withdraw);
                    // 更新稽核和提款状态
                    updateIsDrawings(withdraw.getAccountId(), Constants.EVNumber.zero);
                    // 更新存款单
                    accWithdrawMapper.updateByPrimaryKey(withdraw);
                    // 发送取款拒绝通知
                    sendWithdrawRefuseMsg(siteCode, as.getAccountId(), as.getActualArrival());
                    // 记录操作日志？
                }
            }
        }
    }

    // EVELLET代付查询订单状态
    private void updateEvelltetPayMerchantPayment(FundMerchantPay merchantPay, AccWithdraw as, String siteCode) {
//        String orderNo = as.getOrderNo();
        String result = cryptoCurrenciesService.querySubmitSuccess(as, merchantPay);
        if (StringUtil.isNotEmpty(result)) {
            Type jsonType = new TypeToken<CommonEvelletResponse<EvelletPayTransferCallbackDto>>() {
            }.getType();
            CommonEvelletResponse<EvelletPayTransferCallbackDto> payResponse = jsonUtil.fromJson(result, jsonType);
            if (Objects.isNull(payResponse) || Objects.isNull(payResponse.getData())) {
                return;
            }
            EvelletPayTransferCallbackDto response = payResponse.getData();
            if (Objects.nonNull(response) && Objects.nonNull(response.getStatus())) {
                // 出款成功
                if (Integer.valueOf(Constants.EVNumber.two).equals(response.getStatus())) {
                    log.info("querySubmitSuccess==createuser==" + as.getCreateUser() + "==order==" + as.getOrderNo()
                            + "==订单查询==成功");
                    updateMerchantPaymentStatus(Constants.EVNumber.one, as.getId(), as.getMerchantDetailId(), "2", as.getAccountId(), null, response.getHash());
                    sendWithdrawSuccessMsg(siteCode, as.getAccountId(), as.getActualArrival());
                }
                // 出款失败
                if (Integer.valueOf(Constants.EVNumber.three).equals(response.getStatus())) {
                    log.info("querySubmitSuccess==createuser==" + as.getCreateUser() + "==order==" + as.getOrderNo()
                            + "==订单查询==失败");
                    updateMerchantPaymentStatus(Constants.EVNumber.three, as.getId(), as.getMerchantDetailId(), "3", as.getAccountId(), null, null);
                }
            }
        }
    }



    public void updateCenterPayMerchantPayment(FundMerchantPay merchantPay, AccWithdraw as, String siteCode){
        String result = payCenterService.querySubmitSuccess(as, merchantPay);
        if (StringUtil.isNotEmpty(result)) {
            CommonDPayQueryRespDto response = jsonUtil.fromJson(result, CommonDPayQueryRespDto.class);
            if (Objects.nonNull(response) && Objects.nonNull(response.getCode()) && Objects.nonNull(response.getData())) {

                DPaySearchResponseDto dPaySearchResponseDto = response.getData();
                // 出款成功
                if (dPaySearchResponseDto.getStatus() == Constants.EVNumber.one) {
                    log.info("支付中心代付==outTradeNo==" + as.getOrderNo() + "==订单查询==成功");
                    String remarks = dPaySearchResponseDto.getMemo();
                    updateMerchantPaymentStatus(Constants.EVNumber.one, as.getId(), as.getMerchantDetailId(), "2", as.getAccountId(), remarks, null);
                    sendWithdrawSuccessMsg(siteCode, as.getAccountId(), as.getActualArrival());
                }

                if (dPaySearchResponseDto.getStatus() == Constants.EVNumber.zero) {    // LBT出款拒绝直接上分
                    log.info("LBT代付==outTradeNo==" + as.getOrderNo() + "==订单查询==失败");
                    AccWithdraw withdraw = accWithdrawMapper.selectByPrimaryKey(as.getId());
                    // 处理失败
                    withdraw.setModifyUser(Constants.SYSTEM_USER);
                    withdraw.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    withdraw.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    withdraw.setAuditUser(Constants.SYSTEM_USER);
                    withdraw.setStatus(Constants.EVNumber.zero);    // 拒绝状态

                    String remarks = dPaySearchResponseDto.getMemo();
                    if (StringUtil.isEmpty(remarks) || remarks.length() > 400) {
                        remarks = "LBT代付：审核拒绝";
                    }
                    withdraw.setMemoWithdraw(remarks); // 复审备注
                    // 重新上分
                    setAccWithdrawZreo(withdraw);
                    // 更新稽核和提款状态
                    updateIsDrawings(withdraw.getAccountId(), Constants.EVNumber.zero);
                    // 更新存款单
                    accWithdrawMapper.updateByPrimaryKey(withdraw);
                    // 发送取款拒绝通知
                    sendWithdrawRefuseMsg(siteCode, as.getAccountId(), as.getActualArrival());
                    // 记录操作日志？
                }
            }
        }
    }

    public void dealOnePayCallback(OnePayResponseDto data, String siteCode) {
        FundMerchantDetail detail = fundMapper.findFundMerchantDetailByTransId(data.getMer_ordersid());
        if (null == detail) {
            log.info(data.getMer_ordersid() + "订单明细不存在。");
            return;
        }
        Integer withDrawId = detail.getAccWithdrawId();
        AccWithdraw withdraw = accWithdrawMapper.selectByPrimaryKey(withDrawId);
        if (Constants.EVNumber.one == withdraw.getStatus()) {
            return;
        }
        String redisKey = RedisConstants.UPDATE_WITHDRAW + siteCode + withdraw.getId();
        boolean flag = redisService.setRedisExpiredTimeBo(redisKey, data.getMer_ordersid(), 200, TimeUnit.SECONDS);
        if (flag) {
            if ("3".equals(data.getStatus())) {
                updateMerchantPaymentStatus(Constants.EVNumber.one, withdraw.getId(), detail.getId(), "2", withdraw.getAccountId(), null, null);
                sendWithdrawSuccessMsg(siteCode, withdraw.getAccountId(), new BigDecimal(data.getMoney()));
            } else if ("4".equals(data.getStatus())) {
                updateMerchantPaymentStatus(Constants.EVNumber.three, withdraw.getId(), detail.getId(), "3", withdraw.getAccountId(), null, null);
            }
            redisService.del(redisKey);
        }
    }

    public void dealPaymentPayCallback(PaymentPayResponseDto data, String siteCode) {
//        // 获得代付配置
//        FundMerchantPay queryParam = new FundMerchantPay();
//        queryParam.setMerchantNo(data.getMerchantNumber());
//        queryParam.setAvailable(Constants.EVNumber.one);
//        FundMerchantPay fundMerchantPay = merchantPayService.getMerchantPay(queryParam).get(0); // TODO
//        // 解密校验 TODO
//        try{
//            //私钥解密
//            String verifyResultPri = PaymentPaySignUtil.privateDecrypt(fundMerchantPay.getMerchantKey().getBytes("UTF-8"), data.getSign());
//            log.info("Payment代付==outTradeNo==" + data.getMerchantOrderNumber() + "==回调数据信息verifyResultPri==" + verifyResultPri);
//        }catch (Exception e){
//            log.error("Payment代付==outTradeNo==" + data.getMerchantOrderNumber() + "==调数据信息verifyResultPri==异常" + e);
//        }

        // 获得代付明细
        FundMerchantDetail detail = fundMapper.findFundMerchantDetailByTransId(data.getMerchantOrderNumber());
        if (null == detail) {
            log.info("Payment代付==outTradeNo==" + data.getMerchantOrderNumber() + "==回调==订单明细不存在");
            return;
        }
        // 获得存款
        Integer withDrawId = detail.getAccWithdrawId();
        AccWithdraw withdraw = accWithdrawMapper.selectByPrimaryKey(withDrawId);
        if (Constants.EVNumber.one == withdraw.getStatus() || Constants.EVNumber.zero == withdraw.getStatus()) {   // TODO
            log.info("Payment代付==outTradeNo==" + data.getMerchantOrderNumber() + "==回调==已处理==status==" + withdraw.getStatus());
            return;
        }
        String redisKey = RedisConstants.UPDATE_WITHDRAW + siteCode + withdraw.getId();
        boolean flag = redisService.setRedisExpiredTimeBo(redisKey, data.getMerchantOrderNumber(), 200, TimeUnit.SECONDS);
        if (flag) {
            if (Integer.valueOf(Constants.EVNumber.one).equals(data.getPayStatus())) {           // 代付成功
                log.info("Payment代付==outTradeNo==" + data.getMerchantOrderNumber() + "==回调==代付成功");
                updateMerchantPaymentStatus(Constants.EVNumber.one, withdraw.getId(), detail.getId(), "2", withdraw.getAccountId(), null, null);
                sendWithdrawSuccessMsg(siteCode, withdraw.getAccountId(), data.getAmount());
            } else if (Integer.valueOf(Constants.EVNumber.zero).equals(data.getPayStatus())) {   // 代付失败后，改为手动出款
                log.info("Payment代付==outTradeNo==" + data.getMerchantOrderNumber() + "==回调==代付失败");
                updateMerchantPaymentStatus(Constants.EVNumber.three, withdraw.getId(), detail.getId(), "3", withdraw.getAccountId(), null, null);
            }
            redisService.del(redisKey);
        }

    }

    public void dealLBTCallback(LBTCallbackReqDto data, String siteCode) {
        // 获得代付明细
        FundMerchantDetail detail = fundMapper.findFundMerchantDetailByTransId(data.getOrderno());
        if (null == detail) {
            log.info("LBT代付==outTradeNo==" + data.getOrderno() + "==回调==订单明细不存在");
            return;
        }
        // 获得存款
        Integer withDrawId = detail.getAccWithdrawId();
        AccWithdraw withdraw = accWithdrawMapper.selectByPrimaryKey(withDrawId);
        if (Constants.EVNumber.one == withdraw.getStatus() || Constants.EVNumber.zero == withdraw.getStatus()) {   // TODO
            log.info("LBT代付==outTradeNo==" + data.getOrderno() + "==回调==已处理==status==" + withdraw.getStatus());
            return;
        }
        // 通过订单号获取代付配置
        FundMerchantPay merchantPay = fundMapper.getMerchantPayByOrderno(data.getOrderno());
        if (Objects.isNull(merchantPay)) {
            log.info("LBT代付==outTradeNo==" + data.getOrderno() + "==回调==代付配置不存在！");
            return;
        }
        // 验签
        Boolean isSign = lbtPayService.checkSign(data, merchantPay);
        if (!isSign) {
            log.info("LBT代付==outTradeNo==" + data.getOrderno() + "==回调==验签失败！");
            return;
        }

        // 处理回调
        String redisKey = RedisConstants.UPDATE_WITHDRAW + siteCode + withdraw.getId();
        boolean flag = redisService.setRedisExpiredTimeBo(redisKey, data.getOrderno(), 200, TimeUnit.SECONDS);
        if (flag) {
            if (("APPROVED".equals(data.getStatus()))) {           // 代付成功
                log.info("LBT代付==outTradeNo==" + data.getOrderno() + "==回调==代付成功");
                String remarks = data.getRemarks();
                if (StringUtil.isEmpty(remarks)) {
                    remarks = "LBT代付：审核通过";
                }
                // 如果出款金额比订单金额小，补回会员金额，加入备注
                BigDecimal realAmount = BigDecimal.valueOf(Double.valueOf(data.getAmount()));
                if (withdraw.getActualArrival().compareTo(realAmount) == 1) {
                    BigDecimal returnAmount = withdraw.getActualArrival().subtract(realAmount);
                    // 退回金额，加入账变记录
                    remarks = remarks.concat("；提款金额").concat(withdraw.getActualArrival().toString()).concat(" LBT实际出款").concat(realAmount.toString())
                            .concat(" 退回").concat(returnAmount.toString());
                    mbrWalletService.castWalletAndBillDetailSucToFail(withdraw.getLoginName(), withdraw.getAccountId(), OrderConstants.FUND_ORDER_CODE_AA,
                            returnAmount, withdraw.getOrderNo(), Boolean.TRUE, null, null, remarks);
                    updateMerchantPaymentStatusForNewAmount(Constants.EVNumber.one, withdraw.getId(), detail.getId(), "2",
                            withdraw.getAccountId(), realAmount, remarks, null);
                } else {
                    updateMerchantPaymentStatus(Constants.EVNumber.one, withdraw.getId(), detail.getId(), "2", withdraw.getAccountId(), remarks, null);
                }

                // 发送取款成功消息
                //sendWithdrawSuccessMsg(siteCode, withdraw.getAccountId(), withdraw.getActualArrival());
                sendWithdrawSuccessMsg(siteCode, withdraw.getAccountId(), realAmount);
            }
//            else if (("REJECTED".equals(data.getStatus()))) {   // 代付失败后，改为手动出款
//                log.info("LBT代付==outTradeNo==" + data.getOrderno() + "==回调==代付失败" );
//                updateMerchantPaymentStatus(Constants.EVNumber.three, withdraw.getId(), detail.getId(), "3", withdraw.getAccountId(),null);
//            }
            else if (("REJECTED".equals(data.getStatus()))) {   // lbt代付失败后，直接失败
                log.info("LBT代付==outTradeNo==" + data.getOrderno() + "==回调==代付失败");
                // 处理失败
                withdraw.setModifyUser(Constants.SYSTEM_USER);
                withdraw.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
                withdraw.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
                withdraw.setAuditUser(Constants.SYSTEM_USER);
                withdraw.setStatus(Constants.EVNumber.zero);    // 拒绝状态
                String remarks = data.getRemarks();
                if (StringUtil.isEmpty(remarks)) {
                    remarks = "LBT代付：审核拒绝";
                }
                withdraw.setMemoWithdraw(remarks); // 复审备注
                // 重新上分
                setAccWithdrawZreo(withdraw);
                // 更新稽核和提款状态
                updateIsDrawings(withdraw.getAccountId(), Constants.EVNumber.zero);
                // 更新存款单
                accWithdrawMapper.updateByPrimaryKey(withdraw);

                // 发送取款拒绝通知
                sendWithdrawRefuseMsg(siteCode, withdraw.getAccountId(), withdraw.getActualArrival());
                // 记录操作日志？
            }
            redisService.del(redisKey);
        }
    }


    public void dealEvelletTransferCallback(EvelletPayTransferCallbackDto data, String siteCode) {
        // 获得代付明细
        FundMerchantDetail detail = fundMapper.findFundMerchantDetailByTransId(data.getOutTradeno());
        if (null == detail) {
            log.info("evelletTransferCallback==siteCode==" + siteCode + "==loginName==" + data.getLoginName() + "==回调==订单明细不存在");
            return;
        }

        // 获得秘钥
        FundMerchantPay pay = merchantPayMapper.selectByPrimaryKey(detail.getMerchantId());
        if (null == detail) {
            log.info("evelletTransferCallback==siteCode==" + siteCode + "==loginName==" + data.getLoginName() + "==FundMerchantPay不存在");
            return;
        }
        // 验签
        Map<String, Object> param = jsonUtil.Entity2Map(data);
        param.remove("sign");
        param.remove("memo");
        String sign = MD5.getMD5(ASCIIUtils.getFormatUrl(param, pay.getMerchantKey()));
        if (!sign.equals(data.getSign())) {
            log.info("evelletTransferCallback==siteCode==" + siteCode + "==loginName==" + data.getLoginName() + "签名错误");
            return;
        }

        // 获得取款
        Integer withDrawId = detail.getAccWithdrawId();
        AccWithdraw withdraw = accWithdrawMapper.selectByPrimaryKey(withDrawId);
        if (Constants.EVNumber.one == withdraw.getStatus() || Constants.EVNumber.zero == withdraw.getStatus()) {   // TODO
            log.info("evelletTransferCallback==siteCode==" + siteCode + "==loginName==" + data.getLoginName() + "==outTradeNo==" + data.getOutTradeno() + "==回调==已处理==status==" + withdraw.getStatus());
            return;
        }
        String redisKey = RedisConstants.UPDATE_WITHDRAW + siteCode + withdraw.getId();
        boolean flag = redisService.setRedisExpiredTimeBo(redisKey, data.getOutTradeno(), 200, TimeUnit.SECONDS);
        if (flag) {
            try {
                if (Integer.valueOf(Constants.EVNumber.two).equals(data.getStatus())) {           // 代付成功
                    log.info("evelletTransferCallback==siteCode==" + siteCode + "==loginName==" + data.getLoginName() + "==outTradeNo==" + data.getOutTradeno() + "==回调==代付成功");
                    updateMerchantPaymentStatus(Constants.EVNumber.one, withdraw.getId(), detail.getId(), "2", withdraw.getAccountId(), null, data.getHash());
                    sendWithdrawSuccessMsg(siteCode, withdraw.getAccountId(), withdraw.getActualArrival());
                } else if (Integer.valueOf(Constants.EVNumber.three).equals(data.getStatus())) {   // 代付失败后，改为手动出款
                    log.info("evelletTransferCallback==siteCode==" + siteCode + "==loginName==" + data.getLoginName() + "==outTradeNo==" + data.getOutTradeno() + "==回调==代付失败");
                    if (StringUtils.isEmpty(data.getMemo())) {
                        data.setMemo("evellet代付失败！");
                    }
                    updateMerchantPaymentStatus(Constants.EVNumber.three, withdraw.getId(), detail.getId(), "3", withdraw.getAccountId(), data.getMemo(), null);
                }
            } finally {
                redisService.del(redisKey);
            }

        }
    }
    
    
    // 处理支付中心回调
    public void dealPayCenterCallback(DPaySearchResponseDto  data, String siteCode) {
        // 获得代付明细
        FundMerchantDetail detail = fundMapper.findFundMerchantDetailByTransId(data.getOutTradeNo());
        if (null == detail) {
            log.info("saasopsDPayCallback==支付中心代付==outTradeNo==" + data.getOutTradeNo() + "==回调==订单明细不存在");
            return;
        }
        // 获得存款
        Integer withDrawId = detail.getAccWithdrawId();
        AccWithdraw withdraw = accWithdrawMapper.selectByPrimaryKey(withDrawId);
        if (Constants.EVNumber.one == withdraw.getStatus() || Constants.EVNumber.zero == withdraw.getStatus()) {   // TODO
            log.info("saasopsDPayCallback==支付中心代付==outTradeNo==" + data.getOutTradeNo() + "==回调==已处理==status==" + withdraw.getStatus());
            return;
        }
        // 通过订单号获取代付配置
        FundMerchantPay merchantPay = fundMapper.getMerchantPayByOrderno(data.getOutTradeNo());
        if (Objects.isNull(merchantPay)) {
            log.info("saasopsDPayCallback==支付中心代付==outTradeNo==" + data.getOutTradeNo() + "==回调==代付配置不存在！");
            return;
        }
        // 验签
        Boolean isSign = payCenterService.checkSign(data, merchantPay.getMerchantKey());
        if (!isSign) {
            log.info("saasopsDPayCallback==支付中心代付==outTradeNo==" + data.getOutTradeNo() + "==回调==验签失败！");
            return;
        }

        // 处理回调
        String redisKey = RedisConstants.UPDATE_WITHDRAW + siteCode + withdraw.getId();
        boolean flag = redisService.setRedisExpiredTimeBo(redisKey, data.getOutTradeNo(), 200, TimeUnit.SECONDS);
        if (flag) {
            if (data.getStatus() == 1) {           // 代付成功
                log.info("saasopsDPayCallback==支付中心代付==outTradeNo==" + data.getOutTradeNo() + "==回调==代付成功");
                updateMerchantPaymentStatus(Constants.EVNumber.one, withdraw.getId(), detail.getId(), "2", withdraw.getAccountId(), data.getMemo(), null);
                // 发送取款成功消息
                sendWithdrawSuccessMsg(siteCode, withdraw.getAccountId(), withdraw.getActualArrival());
            }

            else if (data.getStatus() == 0) {   // 代付失败
                log.info("saasopsDPayCallback==支付中心代付==outTradeNo==" + data.getOutTradeNo() + "==回调==代付失败");
                // 处理失败
                withdraw.setModifyUser(Constants.SYSTEM_USER);
                withdraw.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
                withdraw.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
                withdraw.setAuditUser(Constants.SYSTEM_USER);
                withdraw.setStatus(Constants.EVNumber.zero);    // 拒绝状态
                String remarks = data.getMemo();
                withdraw.setMemoWithdraw(remarks); // 复审备注
                // 重新上分
                setAccWithdrawZreo(withdraw);
                // 更新稽核和提款状态
                updateIsDrawings(withdraw.getAccountId(), Constants.EVNumber.zero);
                // 更新存款单
                accWithdrawMapper.updateByPrimaryKey(withdraw);

                // 发送取款拒绝通知
                sendWithdrawRefuseMsg(siteCode, withdraw.getAccountId(), withdraw.getActualArrival());
                // 记录操作日志？
            }
            redisService.del(redisKey);
        }
    }

    private void sendWithdrawMsg(String siteCode, Integer accountId) {
        BizEvent bizEvent = new BizEvent(this, siteCode, accountId, BizEventType.MEMBER_WITHDRAWAL_REVIEW_VERIFY_SUCCESS);
        applicationEventPublisher.publishEvent(bizEvent);
    }

    private void sendWithdrawSuccessMsg(String siteCode, Integer accountId, BigDecimal withdrawMoney) {
        BizEvent bizEvent = new BizEvent(this, siteCode, accountId, BizEventType.MEMBER_WITHDRAWAL_REVIEW_VERIFY_SUCCESS);
        bizEvent.setWithdrawMoney(withdrawMoney);
        applicationEventPublisher.publishEvent(bizEvent);
    }

    private void sendWithdrawRefuseMsg(String siteCode, Integer accountId, BigDecimal withdrawMoney) {
        BizEvent bizEvent = new BizEvent(this, siteCode, accountId, BizEventType.MEMBER_WITHDRAWAL_REVIEW_VERIFY_FAILED);
        bizEvent.setWithdrawMoney(withdrawMoney);
        applicationEventPublisher.publishEvent(bizEvent);
    }

    /**
     * 有代付结果，但实际出款金额不一致；更新出款信息
     *
     * @param status
     * @param accWithdrawId
     * @param merchantDetailId
     * @param bankStatus
     * @param accountId
     */
    private void updateMerchantPaymentStatusForNewAmount(int status, int accWithdrawId, int merchantDetailId, String
            bankStatus, Integer accountId, BigDecimal newAmount, String memo, String hash) {
        AccWithdraw accWithdraw = new AccWithdraw();
        accWithdraw.setStatus(status);
        accWithdraw.setId(accWithdrawId);
        accWithdraw.setHash(hash);
        if (StringUtil.isNotEmpty(memo)) {
            accWithdraw.setMemoWithdraw(memo);  // 出款备注
        }
        if (status == Constants.EVNumber.three) {
            accWithdraw.setType(Constants.EVNumber.zero);
        }
        if (status == Constants.EVNumber.one) {
            accWithdraw.setPassTime(getCurrentDate(FORMAT_18_DATE_TIME));
            accWithdraw.setPassUser(Constants.SYSTEM_PASSUSER);
        }
        accWithdraw.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        accWithdraw.setModifyUser(Constants.SYSTEM_PASSUSER);
        accWithdraw.setActualArrival(newAmount);
        accWithdrawMapper.updateByPrimaryKeySelective(accWithdraw);
        FundMerchantDetail merchantDetail = new FundMerchantDetail();
        merchantDetail.setId(merchantDetailId);
        merchantDetail.setBankStatus(bankStatus);
        merchantDetailMapper.updateByPrimaryKeySelective(merchantDetail);
        if (status == Constants.EVNumber.one) {
            // 处理历史稽核
            updateIsDrawings(accountId, status);
        }
    }

    /**
     * 有代付结果后更新出款信息
     *
     * @param status
     * @param accWithdrawId
     * @param merchantDetailId
     * @param bankStatus
     * @param accountId
     */
    private void updateMerchantPaymentStatus(int status, int accWithdrawId, int merchantDetailId, String
            bankStatus, Integer accountId, String memo, String hash) {
        AccWithdraw accWithdraw = new AccWithdraw();
        accWithdraw.setStatus(status);
        accWithdraw.setId(accWithdrawId);
        accWithdraw.setHash(hash);
        if (StringUtil.isNotEmpty(memo)) {
            accWithdraw.setMemoWithdraw(memo);  // 出款备注
        }
        if (status == Constants.EVNumber.three) {
            accWithdraw.setType(Constants.EVNumber.zero);
        }
        if (status == Constants.EVNumber.one) {
            accWithdraw.setPassTime(getCurrentDate(FORMAT_18_DATE_TIME));
            accWithdraw.setPassUser(Constants.SYSTEM_PASSUSER);
        }
        accWithdraw.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        accWithdraw.setModifyUser(Constants.SYSTEM_PASSUSER);
        accWithdrawMapper.updateByPrimaryKeySelective(accWithdraw);
        FundMerchantDetail merchantDetail = new FundMerchantDetail();
        merchantDetail.setId(merchantDetailId);
        merchantDetail.setBankStatus(bankStatus);
        merchantDetailMapper.updateByPrimaryKeySelective(merchantDetail);
        if (status == Constants.EVNumber.one) {
            // 处理历史稽核
            updateIsDrawings(accountId, status);
        }
    }

    private void updateIsDrawings(Integer accountId, Integer isDrawings) {
        MbrAuditAccount accountAudit = new MbrAuditAccount();
        accountAudit.setAccountId(accountId);
        accountAudit.setIsDrawings(Constants.EVNumber.two);
        List<MbrAuditAccount> auditAccounts = accountAuditMapper.select(accountAudit);
        if (Collections3.isNotEmpty(auditAccounts)) {
            auditAccounts.stream().forEach(as -> {
                MbrAuditAccount mbrAccountAudit = new MbrAuditAccount();
                mbrAccountAudit.setIsDrawings(isDrawings);
                mbrAccountAudit.setId(as.getId());
                if (isDrawings == Constants.EVNumber.one) {
                    mbrAccountAudit.setStatus(Constants.EVNumber.one);
                }
                accountAuditMapper.updateByPrimaryKeySelective(mbrAccountAudit);
            });
            if (isDrawings == Constants.EVNumber.one) {
                MbrAuditAccount auditAccount = auditAccounts.get(auditAccounts.size() - 1);
                auditAccountService.addOrUpdateHistoryByDeposit(auditAccounts.get(0), auditAccount.getTime());
            }
        }
    }

    /**	
     * 	取款时校验稽核列表
     * @param withDraw
     */
    private boolean withdrawAudit(AccWithdraw withDraw) {
        // 稽核列表
        List<MbrAuditAccount> audits = auditAccountService
                .getMbrAuditAccounts(withDraw.getAccountId());
        // 稽核处理
        boolean checkoutAudit = checkoutAudit(audits);
        // 所有稽核通过
        if (checkoutAudit) {
        	audits.stream().forEach(as -> {
        		as.setIsDrawings(Constants.EVNumber.two);
        		accountAuditMapper.updateByPrimaryKey(as);
        	});
        }
        return checkoutAudit;
    }

    /**
     * 	取款稽核校验
     * @param audits 集合列表
     */
    private boolean checkoutAudit(List<MbrAuditAccount> audits) {
    	// 有存款金额大于0，并且状态为不通过
        long depositCountNot = audits.stream().filter(p -> nonNull(p.getDepositAmount())
                && p.getDepositAmount().compareTo(BigDecimal.ZERO) == 1
                && Constants.EVNumber.zero == p.getStatus()).map(MbrAuditAccount::getId).count();
        if (depositCountNot > 0) {
        	return false;
        }
        return true;
    }
    
    /**
     * 	取款时是否全部未稽核完成
     * @param accountId 用户id
     */
    public boolean checkoutAudit(Integer accountId) {
    	 // 稽核列表
        List<MbrAuditAccount> audits = auditAccountService
                .getMbrAuditAccounts(accountId);
    	// 有存款金额大于0，并且状态为不通过
    	long depositCountNot = audits.stream().filter(p -> nonNull(p.getDepositAmount())
    			&& p.getDepositAmount().compareTo(BigDecimal.ZERO) == 1
    			&& Constants.EVNumber.zero == p.getStatus()).map(MbrAuditAccount::getId).count();
    	if (depositCountNot > 0) {
    		return false;
    	}
    	return true;
    }

    private AccWithdraw checkoutFund(Integer id) {
        AccWithdraw withdraw = accWithdrawMapper.selectByPrimaryKey(id);
        if (withdraw.getStatus().equals(Constants.IsStatus.succeed)
                || withdraw.getStatus().equals(Constants.IsStatus.defeated)) {
            throw new R200Exception(messagesConfig.getValue("saasops.illegal.request"));
        }
        if (nonNull(withdraw.getType())) {
            if (withdraw.getStatus() == Constants.EVNumber.two
                    && withdraw.getType() == Constants.EVNumber.three) {
                throw new R200Exception("该订单正在进行代付处理");
            }
            if (withdraw.getStatus() == Constants.EVNumber.five) {
                throw new R200Exception("该订单已经由代付处理，请勿手工处理");
            }
        }
        return withdraw;
    }

    private AccWithdraw checkoutFundSucToFail(Integer id) {
        AccWithdraw withdraw = accWithdrawMapper.selectByPrimaryKey(id);
        if (nonNull(withdraw.getType())) {
            if (withdraw.getStatus() == Constants.EVNumber.two
                    && withdraw.getType() == Constants.EVNumber.three) {
                throw new R200Exception("该订单正在进行代付处理");
            }
            if (withdraw.getStatus() == Constants.EVNumber.five) {
                throw new R200Exception("该订单已经由代付处理，请勿手工处理");
            }
        }
        return withdraw;
    }

    public void updateAccMemo(Integer id, String memo, String loginName) {
        AccWithdraw withdraw = accWithdrawMapper.selectByPrimaryKey(id);
        if (withdraw.getStatus() == Constants.EVNumber.one) {
            throw new R200Exception("已经完成出款，不可以修改备注");
        }
        withdraw.setMemo(memo);
        withdraw.setModifyUser(loginName);
        withdraw.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        accWithdrawMapper.updateByPrimaryKey(withdraw);
    }
    
    public void updateExemptMemo(Integer id, String exemptMemo, String loginName) {
    	AccWithdraw withdraw = accWithdrawMapper.selectByPrimaryKey(id);
    	if (withdraw.getStatus() != Constants.EVNumber.one) {
    		throw new R200Exception("未完成出款，不可以修改免审备注");
    	}
    	if (!StringUtil.isEmpty(withdraw.getExemptMemo())) {
    		throw new R200Exception("免审备注已修改，不可再次修改");
    	}
    	withdraw.setExemptMemo(exemptMemo);
    	withdraw.setExemptUser(loginName);
    	withdraw.setExemptTime(new Date());
    	accWithdrawMapper.updateByPrimaryKey(withdraw);
    }

    public void accExportExecl(AccWithdraw accWithdraw, HttpServletResponse response) {
        String fileName = "会员提款" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xls";
        List<AccWithdraw> fundDeposits = fundMapper.findAccWithdrawList(accWithdraw);
        List<Map<String, Object>> list = Lists.newArrayList();
        fundDeposits.stream().forEach(deposit -> {
            Map<String, Object> paramr = new HashMap<>();
            paramr.put("orderNo", deposit.getOrderPrefix() + deposit.getOrderNo());
            paramr.put("loginName", deposit.getLoginName());
            paramr.put("groupName", deposit.getGroupName());
            paramr.put("drawingAmount", deposit.getDrawingAmount());
            paramr.put("auditUser", deposit.getAuditUser());
            paramr.put("status", deposit.getStatus() == 0
                    ? Constants.ChineseStatus.defeated : deposit.getStatus() == 1
                    ? Constants.ChineseStatus.succeed : Constants.ChineseStatus.pending);
            list.add(paramr);
        });
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList", agyExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            throw new RRException(e.getMessage());
        }
    }

    /**
     * 出款导出
     *
     * @param accWithdraw
     * @param userId
     * @return
     */
    public SysFileExportRecord accWithdrawExportExcel(AccWithdraw accWithdraw, Long userId, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            String siteCode = CommonUtil.getSiteCode();
            CompletableFuture.runAsync(() -> {
                resetStatus(accWithdraw);
                ThreadLocalCache.setSiteCodeAsny(siteCode);
                List<AccWithdraw> accWithdraws = fundMapper.findAccWithdrawList(accWithdraw);
                if (accWithdraws != null && accWithdraws.size() > 0) {
                    for (int i = 0; i < accWithdraws.size(); i++) {
                        if (accWithdraws.get(i).getCurrencyProtocol() != null && "" != accWithdraws.get(i).getCurrencyProtocol()) {
                            if (accWithdraws.get(i).getCurrencyProtocol().contains("ERC20")) {
                                accWithdraws.get(i).setCurrencyProtocol("以太坊(ERC20)");
                            }
                            if (accWithdraws.get(i).getCurrencyProtocol().contains("TRC20")) {
                                accWithdraws.get(i).setCurrencyProtocol("波场(TRC20)");
                            }
                        }

                    }
                }
                List<Map<String, Object>> list = accWithdraws.stream().map(e -> {
                    setNum2Char(e);
                    Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                    return entityMap;
                }).collect(Collectors.toList());
                sysFileExportRecordService.exportExcel(accExcelPath, list, userId, module, siteCode);//异步执行
            });
        }
        return record;
    }

    private void setNum2Char(AccWithdraw accWithdraw) {
        switch (accWithdraw.getStatus()) {
            //0 拒绝 1 通过 2 初审 3 复审
            case 0:
                accWithdraw.setStatusStr("拒绝");
                break;
            case 1:
                accWithdraw.setStatusStr("通过");
                break;
            case 2:
                accWithdraw.setStatusStr("初审");
                break;
            case 3:
                accWithdraw.setStatusStr("复审");
                break;
            case 7:
                accWithdraw.setStatusStr("失败");
                break;
            default:
                accWithdraw.setStatusStr("");
        }
        if (StringUtil.isEmpty(accWithdraw.getWithdrawSource())) {
            accWithdraw.setWithdrawSourceList("");
        } else if (accWithdraw.getWithdrawSource() == 0) {
            accWithdraw.setWithdrawSourceList("PC");
        } else if (accWithdraw.getWithdrawSource() == 3) {
            accWithdraw.setWithdrawSourceList("H5");
        }
        //0 手动出款 1自动出款 3处理中
        switch (accWithdraw.getType()) {
            case 0:
                accWithdraw.setTypeStr("手动出款");
                break;
            case 1:
                accWithdraw.setTypeStr("自动出款");
                break;
            case 3:
                accWithdraw.setTypeStr("处理中");
                break;
            default:
                accWithdraw.setTypeStr("");
        }

        // 提现方式：0银行卡， 1加密货币钱包
        switch (accWithdraw.getMethodType()) {
            //0 拒绝 1 通过 2 初审 3 复审
            case 0:
                accWithdraw.setMethodTypeStr("人民币");
                break;
            case 1:
                accWithdraw.setMethodTypeStr("加密货币");
                break;
            default:
                accWithdraw.setMethodTypeStr("");
        }

        if ("支付宝".equals(accWithdraw.getBankName())) {
            accWithdraw.setMethodTypeStr("支付宝");
        }
    }

    private void resetStatus(AccWithdraw accWithdraw) {
        List<Integer> statuss = accWithdraw.getStatuss();
        //判断statuss是否为空
        if (Collections3.isNotEmpty(statuss)) {
            int forFlag = statuss.size();
            for (int i = 0; i < statuss.size(); i++) {
                if (2 == statuss.get(i) || 4 == statuss.get(i)) {
                    statuss.add(4);
                    statuss.add(2);
                } else if (3 == statuss.get(i) || 5 == statuss.get(i)) {
                    statuss.add(3);
                    statuss.add(5);
                }
                if (i + 1 == forFlag) {
                    break;
                }
            }
        }
    }

    public AccWithdraw sumWithDraw(Integer accountId) {
        String startTime = getTodayStart(FORMAT_10_DATE);
        String endTime = getTodayEnd(FORMAT_10_DATE);
        return fundMapper.sumWithDraw(startTime, endTime, accountId);
    }

    /**
     * 	本次提款是否免费
     * 
     * @param feeTimes 提款次数
     * @param feeHours 	手续费-时限 1：自然日，2：自然周，3：自然月
     * @param accountId 用户id
     * @return
     */
    public Byte isFreeFee(Integer feeTimes, Integer feeHours, Integer accountId) {
    	String startTime = getStartTime(feeHours);
        return fundMapper.isFreeFee(feeTimes, startTime, accountId);
    }

    
    /**
     * 	计算会员提款手续费
     * 
     * @param actualArrival	提款金额
     * @param cond	
     * @param accountId
     * @return
     */
    public BigDecimal calculateFee(BigDecimal actualArrival, MbrWithdrawalCond cond, Integer accountId) {

        // 是否需要计算手续费
        if (!StringUtils.isEmpty(cond.getChargeFeeAvailable()) && cond.getChargeFeeAvailable() == Available.enable) {
            String startTime = getStartTime(cond.getFeeHours());
            // 已经取款次数，是否大于免提款费的次数
            Byte freeFee = fundMapper.isFreeFee(cond.getFeeTimes(), startTime, accountId);
            if (freeFee == Available.enable) {
                return BigDecimal.ZERO;
            } else {
            	// 如果手续费类型不等于null  并且 是固定金额类型，返回固定的手续费金额
                if (!StringUtils.isEmpty(cond.getFeeWay()) && cond.getFeeWay() == FeeWayVal.fixed) {
                    return cond.getFeeFixed();
                } else {
                    BigDecimal fee = BigDecimalMath.div(BigDecimalMath.mul(actualArrival, cond.getFeeScale()),
                            new BigDecimal("100"), 2);
                    if (fee.compareTo(cond.getFeeTop()) == 1) {
                        return cond.getFeeTop();
                    } else {
                        return fee;
                    }
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private String getStartTime(Integer rule) {
        String startTime = DateUtil.getTodayStart(DateUtil.FORMAT_10_DATE);
        if (Constants.EVNumber.one == rule) {
            startTime = DateUtil.getTodayStart(DateUtil.FORMAT_10_DATE);
        } else if (Constants.EVNumber.two == rule) {
            startTime = DateUtil.getMonday(DateUtil.FORMAT_18_DATE_TIME, 0, 0);//本周第一天
        } else if (Constants.EVNumber.three == rule) {
            startTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_18_DATE_TIME, 0, 0);//本月第一天
        }
        return startTime;
    }


    /**
     * 	取款手续费
     * 
     * @param actualArrival  取款金额
     * @param accountId  会员id
     * @return
     */
    public BigDecimal calculateFee(BigDecimal actualArrival, Integer accountId) {
        MbrWithdrawalCond cond = withdrawalCond.getMbrWithDrawal(accountId);
        return calculateFee(actualArrival, cond, accountId);
    }

    /**
     * 会员提款统计
     *
     * @param accWithdraw
     * @return
     */
    public List<CountEntity> withdrawCountByStatus(AccWithdraw accWithdraw) {
        List<CountEntity> list = fundMapper.withdrawCountByStatus(accWithdraw);
        return list;
    }

    public List<AccWithdraw> queryAccListAll(AccWithdraw accWithdraw) {
        List<AccWithdraw> list = fundMapper.findAccWithdrawListAll(accWithdraw);
        return list;
    }

    public List<AccWithdraw> fundAccWithdrawMerchant(Integer accountId) {
        return fundMapper.fundAccWithdrawMerchant(accountId);
    }

    public AccWithdraw lockstatus(AccWithdraw accWithdraw, String userName) {
        // 先更新所有操作锁定
        updateAllLockStatus();
        // 再查询锁定状态
        AccWithdraw acc = this.queryObject(accWithdraw.getId());
        // 判断是否锁定
        if (Integer.valueOf(Constants.EVNumber.one).equals(acc.getLockStatus())) {    // 锁定状态
            if (userName.equals(acc.getLockOperator())) {     // 被当前用户锁定
                acc.setIsCurrentUserLock(Constants.EVNumber.one);
            } else {  // 非当前用户锁定
                acc.setIsCurrentUserLock(Constants.EVNumber.two);
            }
        } else {      // 未锁定状态
            acc.setIsCurrentUserLock(Constants.EVNumber.zero);
        }

        return acc;
    }

    public void lock(AccWithdraw accWithdraw, String userName) {
        AccWithdraw acc = new AccWithdraw();
        acc.setId(accWithdraw.getId());
        acc.setLockStatus(accWithdraw.getLockStatus());
        acc.setLockOperator(userName);
        acc.setLastLockTime(getCurrentDate(FORMAT_18_DATE_TIME));
        this.update(acc);
    }

    public void unLock(AccWithdraw accWithdraw, String userName) {
        AccWithdraw acc = new AccWithdraw();
        acc.setId(accWithdraw.getId());
        acc.setLockStatus(accWithdraw.getLockStatus());
        acc.setLockOperator(null);
        acc.setLastLockTime(null);
        this.update(acc);
    }

    public void updateAllLockStatus() {
        fundMapper.updateAllLockStatus();
    }

	/**
	 * 	创建一个与关联订单一样金额的提款订单，初审直接通过，直接由财务审核
	 * @param accWithdraw
	 */
	public void artificialWithdrawal(AccWithdraw accWithdraw) {
		String relatedOrderno = accWithdraw.getRelatedOrderno().substring(2);
		 // 查询原有订单
		 AccWithdraw accWithdrawQuery = new AccWithdraw();
		 accWithdrawQuery.setOrderNo(relatedOrderno);
		 AccWithdraw queryObjectCond = this.queryObjectCond(accWithdrawQuery);
		 if (queryObjectCond == null) {
			 throw new R200Exception("关联订单不存在");
		 }
		 if (queryObjectCond.getStatus() != Constants.EVNumber.seven && queryObjectCond.getStatus() != Constants.EVNumber.one) {
			 throw new R200Exception("请关联成功或失败的订单");
		 }
		 // 设置实际出款
		 if (accWithdraw.getActualArrival() != null && accWithdraw.getActualArrival().compareTo(BigDecimal.ZERO) > 0) {
			 queryObjectCond.setActualArrival(accWithdraw.getActualArrival());
		 }
		 // 设置参数，新增数据
		 queryObjectCond.setMemo("关联单号" + accWithdraw.getRelatedOrderno());
		 queryObjectCond.setMethodType(Constants.EVNumber.six);
		 queryObjectCond.setRelatedOrderno(accWithdraw.getRelatedOrderno());
		 queryObjectCond.setId(null);
		 queryObjectCond.setCreateTime(DateUtil.format(new Date()));
		 queryObjectCond.setOrderNo(new SnowFlake().nextId() + "");
		 queryObjectCond.setStatus(Constants.EVNumber.three);
		 this.save(queryObjectCond);
	}


    public void  checkWithdwarwLimitTime(){
        SysSetting sysSetting = sysSettingService.getSysSetting(SystemConstants.IS_WITHDRAW_LIMIT_TIME_OPEN);
        // 检查提款时间校验
        if(sysSetting!= null && sysSetting.getSysvalue()!=null && sysSetting.getSysvalue().length()>0 && Integer.parseInt(sysSetting.getSysvalue()) == Constants.EVNumber.one){
            SysSetting sysSetting2 = sysSettingService.getSysSetting(SystemConstants.WITHDRAW_LIMIT_TIME_LIST);
            if(sysSetting2!= null && sysSetting2.getSysvalue()!= null &&  sysSetting2.getSysvalue().length() > 0){
                Type jsonType = new com.google.common.reflect.TypeToken<List<WithdrawLimitTimeDto>>() {}.getType();
                List<WithdrawLimitTimeDto> withdrawLimitTimeDtoList = jsonUtil.fromJson(sysSetting2.getSysvalue(), jsonType);
                for (WithdrawLimitTimeDto withdrawLimitTimeDto : withdrawLimitTimeDtoList){
                    boolean rt = DateUtil.isBelong(withdrawLimitTimeDto.getStartTime(), withdrawLimitTimeDto.getEndTime());
                    if(rt){
                        throw  new R200Exception(withdrawLimitTimeDto.getStartTime() + "-" + withdrawLimitTimeDto.getEndTime()  + "该时间段不允许提款!");
                    }
                }
            }
        }
    }

    public AccWithdraw findCuiDanAccWithdraw(Integer accountId, Integer orderId){
        return fundMapper.findCuiDanAccWithdraw(accountId, orderId);
    }


    public int updateCuiDanAccWithdraw(Integer orderId){
        return fundMapper.updateCuiDanAccWithdraw( orderId);
    }


    public List<CuiDanDto> getNewCuiDan(int second){
        return fundMapper.getNewCuiDan(second);
    }

}
