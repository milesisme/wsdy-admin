package com.wsdy.saasops.agapi.modules.service;

import com.wsdy.saasops.api.modules.pay.dto.PayParams;
import com.wsdy.saasops.api.modules.pay.dto.QrCodePayDto;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.dao.AgentDepositMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgentDeposit;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.system.pay.dao.SysQrCodeMapper;
import com.wsdy.saasops.modules.system.pay.entity.SysQrCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;

@Slf4j
@Service
public class AgentQrCodePayService {

    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private MbrAccountMapper mbrAccountMapper;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private SysQrCodeMapper sysQrCodeMapper;
    @Autowired
    private AgentDepositMapper agentDepositMapper;


    public Integer getAccountId(AgentAccount agyAccount) {
        Integer agentId = agyAccount.getId();
        if (agyAccount.getAttributes() == Constants.EVNumber.four) {
            agentId = agyAccount.getAgentId();
        }
        AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(agentId);
        MbrAccount mbrAccount = mbrMapper.getMbrByLoginName(agentAccount.getAgyAccount());
        if (isNull(mbrAccount)){
            return null;
        }
        return mbrAccount.getId();
    }

    public AgentAccount getAgyAccountByAccountId(Integer accountId) {
        MbrAccount mbrAccount = mbrAccountMapper.selectByPrimaryKey(accountId);
        AgentAccount account = new AgentAccount();
        account.setAgyAccount(mbrAccount.getLoginName());
        AgentAccount agentAccount = agentAccountMapper.selectOne(account);
        return agentAccount;
    }

    public QrCodePayDto qrCodePay(PayParams params) {
        params.setOutTradeNo(new SnowFlake().nextId());
        MbrAccount account = mbrAccountMapper.selectByPrimaryKey(params.getAccountId());
        params.setUserName(account.getRealName());
        SysQrCode qrCode = checkoutQrcodePay(params);

        AgentDeposit deposit = saveQrCodeFundDeposit(params, account);

        QrCodePayDto qrCodePayDto = new QrCodePayDto();
        qrCodePayDto.setCreateTime(deposit.getCreateTime());
        qrCodePayDto.setDepositPostscript(deposit.getDepositPostscript());
        qrCodePayDto.setOrderNo(deposit.getOrderNo());
        qrCodePayDto.setUrl(qrCode.getQrImgUrl());
        qrCodePayDto.setUrlMethod(Constants.EVNumber.two);
        qrCodePayDto.setDepositAmount(deposit.getDepositAmount());
        return qrCodePayDto;
    }

    private AgentDeposit saveQrCodeFundDeposit(PayParams params, MbrAccount account) {
        AgentAccount agentAccount = getAgyAccountByAccountId(account.getId());

        AgentDeposit deposit = new AgentDeposit();
        deposit.setOrderNo(params.getOutTradeNo().toString());
        deposit.setMark(FundDeposit.Mark.qrCodePay);
        deposit.setStatus(FundDeposit.Status.apply);
        deposit.setIsPayment(FundDeposit.PaymentStatus.unPay);
        deposit.setQrCodeId(params.getDepositId());
        Random random = new Random();
        //设置金额随机两位小数
        Integer d = random.nextInt(50);
        BigDecimal ran = new BigDecimal(d / 100f);
        deposit.setDepositAmount(params.getFee().add(ran.setScale(2, BigDecimal.ROUND_DOWN)));

        deposit.setHandlingCharge(BigDecimal.ZERO);
        deposit.setHandingback(Constants.Available.disable);
        deposit.setActualArrival(deposit.getDepositAmount().add(deposit.getHandlingCharge()));
        deposit.setIp(params.getIp());
        deposit.setOrderPrefix(OrderConstants.FUND_ORDER_COMPANYDEPOSIT);
        deposit.setDepositUser(params.getUserName());
        deposit.setCreateUser(agentAccount.getAgyAccount());
        deposit.setAccountId(agentAccount.getId());
        deposit.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        deposit.setFundSource(params.getFundSource());
        deposit.setDepositPostscript(CommonUtil.genRandom(6, 6));
        deposit.setLoginName(agentAccount.getAgyAccount());   // 增加会员名
        agentDepositMapper.insert(deposit);
        return deposit;
    }

    private SysQrCode checkoutQrcodePay(PayParams params) {
        SysQrCode qrCode = sysQrCodeMapper.selectByPrimaryKey(params.getDepositId());
        if (isNull(qrCode) || Constants.EVNumber.zero == qrCode.getAvailable() || Constants.EVNumber.one == qrCode.getIsDelete()) {
            throw new R200Exception("此支付方式不接受会员充值");
        }
        if (qrCode.getAmountType() == 0) {
            if (qrCode.getMinAmout().compareTo(params.getFee()) == 1) {
                throw new R200Exception("小于单笔最小充值额度");
            }
            if (params.getFee().compareTo(qrCode.getMaxAmout()) == 1) {
                throw new R200Exception("大于单笔最大充值额度");
            }
        }
        if (qrCode.getAmountType() == 1) {
            if (!qrCode.getFixedAmount().contains(String.valueOf(params.getFee().intValue()))) {
                throw new R200Exception("存款金额不在可选金额中");
            }
        }
        if (qrCode.getDepositAmount().compareTo(qrCode.getDayMaxAmout()) == 1) {
            throw new R200Exception("此支付方式已经达到单日最大限额，请选择其他银行支付");
        }
        return qrCode;
    }
}
