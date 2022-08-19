package com.wsdy.saasops.modules.agent.service;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.BigDecimalMath;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.modules.agent.dao.AgyBillDetailMapper;
import com.wsdy.saasops.modules.agent.dao.AgyWalletMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgyBillDetail;
import com.wsdy.saasops.modules.agent.entity.AgyWallet;
import com.wsdy.saasops.modules.agent.mapper.AgentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_25_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;


@Slf4j
@Service
public class AgentWalletService {

    @Autowired
    public AgyBillDetailMapper billDetailMapper;
    @Autowired
    public AgyWalletMapper agyWalletMapper;
    @Autowired
    public AgentMapper agentMapper;

    public AgyWallet findAgyWallet(Integer accountId) {
        AgyWallet agyWallet = new AgyWallet();
        agyWallet.setAccountId(accountId);
        return agyWalletMapper.selectOne(agyWallet);
    }


    //佣金钱包
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public Boolean updateAddAgyWallet(AgyWallet agyWallet, AgyBillDetail agyBillDetail) {
        int isSuccess;
        if (agyWallet.getWalletType().equals(Constants.EVNumber.zero)) {
            isSuccess = agentMapper.updateAddAgyWallet(agyWallet);
        } else if (agyWallet.getWalletType().equals(Constants.EVNumber.two)){
            isSuccess = agentMapper.updateAddPayoffWallet(agyWallet);
        }
        else {
            isSuccess = agentMapper.updateAddRechargeWallet(agyWallet);
        }
        if (isSuccess == 1) {
            AgyWallet agyWallet1 = getAgyWallet(agyBillDetail.getAccountId());
            if (agyWallet.getWalletType().equals(Constants.EVNumber.zero)) {
                agyBillDetail.setAfterBalance(agyWallet1.getBalance());
                agyBillDetail.setBeforeBalance(
                        BigDecimalMath.round(BigDecimalMath.sub(agyWallet1.getBalance(),
                                agyBillDetail.getAmount()), 2));
            } else if (agyWallet.getWalletType().equals(Constants.EVNumber.two)){
                agyBillDetail.setAfterBalance(agyWallet1.getPayoffWallet());
                agyBillDetail.setBeforeBalance(
                        BigDecimalMath.round(BigDecimalMath.sub(agyWallet1.getPayoffWallet(),
                                agyBillDetail.getAmount()), 2));
            }
            else {
                agyBillDetail.setAfterBalance(agyWallet1.getRechargeWallet());
                agyBillDetail.setBeforeBalance(
                        BigDecimalMath.round(BigDecimalMath.sub(agyWallet1.getRechargeWallet(),
                                agyBillDetail.getAmount()), 2));
            }
            agyBillDetail.setBalance(agyWallet1.getBalance());
            agyBillDetail.setRechargeWallet(agyWallet1.getRechargeWallet());
            agyBillDetail.setPayoffWallet(agyWallet1.getPayoffWallet());
            billDetailMapper.insert(agyBillDetail);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    //佣金钱包
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public Boolean updateReduceAgyWallet(AgyWallet agyWallet, AgyBillDetail agyBillDetail) {
        int isSuccess;
        if (agyWallet.getWalletType().equals(Constants.EVNumber.zero)) {
            isSuccess = agentMapper.updateReduceAgyWallet(agyWallet);
        } else if (agyWallet.getWalletType().equals(Constants.EVNumber.two)){
            isSuccess = agentMapper.updateReducePayoffWallet(agyWallet);
        }
        else {
            isSuccess = agentMapper.updateReduceRechargeWallet(agyWallet);
        }
        if (isSuccess == 1) {
            AgyWallet agyWallet1 = getAgyWallet(agyBillDetail.getAccountId());
            if (agyWallet.getWalletType().equals(Constants.EVNumber.zero)) {
                agyBillDetail.setAfterBalance(agyWallet1.getBalance());
                agyBillDetail.setBeforeBalance(
                        BigDecimalMath.round(BigDecimalMath.add(agyWallet1.getBalance(),
                                agyBillDetail.getAmount()), 2));
            } else if (agyWallet.getWalletType().equals(Constants.EVNumber.two)){
                agyBillDetail.setAfterBalance(agyWallet1.getPayoffWallet());
                agyBillDetail.setBeforeBalance(
                        BigDecimalMath.round(BigDecimalMath.add(agyWallet1.getPayoffWallet(),
                                agyBillDetail.getAmount()), 2));
            }
            else {
                agyBillDetail.setAfterBalance(agyWallet1.getRechargeWallet());
                agyBillDetail.setBeforeBalance(
                        BigDecimalMath.round(BigDecimalMath.add(agyWallet1.getRechargeWallet(),
                                agyBillDetail.getAmount()), 2));
            }
            agyBillDetail.setBalance(agyWallet1.getBalance());
            agyBillDetail.setRechargeWallet(agyWallet1.getRechargeWallet());
            agyBillDetail.setPayoffWallet(agyWallet1.getPayoffWallet());
            billDetailMapper.insert(agyBillDetail);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private AgyWallet getAgyWallet(Integer accountId) {
        AgyWallet wallet = new AgyWallet();
        wallet.setAccountId(accountId);
        return agyWalletMapper.selectOne(wallet);
    }

    public AgyWallet queryAgyWallet(Integer accountId) {
        AgyWallet wallet = new AgyWallet();
        wallet.setAccountId(accountId);
        return agyWalletMapper.selectOne(wallet);
    }

    public AgyBillDetail addWalletAndBillDetail(AgyWallet agyWallet, int opType) {
        AgyBillDetail agyBillDetail = setAgyBillDetail(agyWallet, opType);
        Boolean isSuccess = updateAddAgyWallet(agyWallet, agyBillDetail);
        if (Boolean.FALSE.equals(isSuccess)) {
            throw new R200Exception("钱包操作失败");
        }
        return agyBillDetail;
    }

    public AgyBillDetail reduceWalletAndBillDetail(AgyWallet agyWallet, int opType) {
        AgyBillDetail agyBillDetail = setAgyBillDetail(agyWallet, opType);
        Boolean isSuccess = updateReduceAgyWallet(agyWallet, agyBillDetail);
        if (Boolean.FALSE.equals(isSuccess)) {
            throw new R200Exception("钱包操作失败");
        }
        return agyBillDetail;
    }

    private AgyBillDetail setAgyBillDetail(AgyWallet agyWallet, int opType) {
        AgyBillDetail agyBillDetail = new AgyBillDetail();
        agyBillDetail.setOrderNo(StringUtils.isEmpty(agyWallet.getOrderNo())
                ? new SnowFlake().nextId() + "" : agyWallet.getOrderNo());
        agyBillDetail.setAgyAccount(agyWallet.getAgyAccount());
        agyBillDetail.setAccountId(agyWallet.getAccountId());
        agyBillDetail.setFinancialCode(agyWallet.getFinancialCode());
        agyBillDetail.setAmount(agyWallet.getBalance());
        agyBillDetail.setOpType(opType);
        agyBillDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
        agyBillDetail.setMemo(agyWallet.getMemo());
        agyBillDetail.setAgentId(agyWallet.getAgentId());
        agyBillDetail.setMerAccountid(agyWallet.getMerAccountId());
        agyBillDetail.setCreateuser(agyWallet.getCreateuser());
        agyBillDetail.setWalletType(agyWallet.getWalletType());
        return agyBillDetail;
    }

    public AgyWallet setAgyWallet(AgentAccount agentAccount, BigDecimal amount, String financialCode,
                                  Integer agentId, Integer merAccountId, String orderNo, String createuser,
                                  Integer walletType) {
        AgyWallet agyWallet = new AgyWallet();
        agyWallet.setAccountId(agentAccount.getId());
        agyWallet.setAgyAccount(agentAccount.getAgyAccount());
        agyWallet.setBalance(amount);
        agyWallet.setFinancialCode(financialCode);
        agyWallet.setAgentId(agentId);
        agyWallet.setMerAccountId(merAccountId);
        agyWallet.setOrderNo(orderNo);
        agyWallet.setCreateuser(createuser);
        agyWallet.setWalletType(walletType);
        return agyWallet;
    }
}

