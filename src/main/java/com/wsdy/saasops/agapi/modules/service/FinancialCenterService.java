package com.wsdy.saasops.agapi.modules.service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.aff.service.SdyDataService;
import com.wsdy.saasops.agapi.modules.dto.RechargeTransferParamDto;
import com.wsdy.saasops.agapi.modules.dto.WalletFlowParamDto;
import com.wsdy.saasops.agapi.modules.dto.WalletFlowResponseDto;
import com.wsdy.saasops.agapi.modules.mapper.AgentMenuMapper;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.dao.AgyWalletMapper;
import com.wsdy.saasops.modules.agent.dao.AgyWithdrawMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgyBillDetail;
import com.wsdy.saasops.modules.agent.entity.AgyWallet;
import com.wsdy.saasops.modules.agent.entity.AgyWithdraw;
import com.wsdy.saasops.modules.agent.mapper.AgentMapper;
import com.wsdy.saasops.modules.agent.mapper.FinanciaCenterMapper;
import com.wsdy.saasops.modules.agent.service.AgentWalletService;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;

@Slf4j
@Service
public class FinancialCenterService {

    @Autowired
    private AgentMenuMapper agentMenuMapper;
    @Autowired
    private AgyWalletMapper agyWalletMapper;
    @Autowired
    private FinanciaCenterMapper financiaCenterMapper;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private AgentWalletService walletService;
    @Autowired
    private AgyWithdrawMapper withdrawMapper;
    @Autowired
    private MbrAccountMapper mbrAccountMapper;
    @Autowired
    private SdyDataService sdyDataService;
    @Autowired
    private AgentMapper agentMapper;

    public AgyWallet agentWallet(AgentAccount agentAccount) {
        AgyWallet agyWallet = new AgyWallet();
        agyWallet.setAccountId(agentAccount.getId());
        if (agentAccount.getAttributes() == Constants.EVNumber.four) {
            agyWallet.setAccountId(agentAccount.getAgentId());
        }
        AgyWallet wallet = agyWalletMapper.selectOne(agyWallet);
        wallet.setWithdramAmount(financiaCenterMapper.sumWithdramAmount(wallet.getAccountId()));
        return wallet;
    }

    public void financialCenterMenu(AgentAccount account) {
        List<Long> menuIdList = agentMenuMapper.querySubAccountAllMenuId(account.getAgyAccount(), 4L);
        if (menuIdList.size() == 0) {
            throw new AuthorizationException("没有权限，请联系上级授权");
        }
    }

    public PageUtils walletFlow(WalletFlowParamDto paramDto, AgentAccount agentAccount, Integer pageNo, Integer pageSize) {
        if (agentAccount.getAttributes() == Constants.EVNumber.four) {
            paramDto.setAgentId(agentAccount.getAgentId());
        } else {
            paramDto.setAgentId(agentAccount.getId());
        }
        PageHelper.startPage(pageNo, pageSize);
        List<WalletFlowResponseDto> responseDtos = financiaCenterMapper.walletFlow(paramDto);
        return BeanUtil.toPagedResult(responseDtos);
    }

    public PageUtils rechargeWalletFlow(WalletFlowParamDto paramDto, AgentAccount agentAccount, Integer pageNo, Integer pageSize) {
        if (agentAccount.getAttributes() == Constants.EVNumber.four) {
            paramDto.setAgentId(agentAccount.getAgentId());
        } else {
            paramDto.setAgentId(agentAccount.getId());
        }
        PageHelper.startPage(pageNo, pageSize);
        List<WalletFlowResponseDto> responseDtos = financiaCenterMapper.rechargeWalletFlow(paramDto);
        return BeanUtil.toPagedResult(responseDtos);
    }
    public PageUtils payoffWalletFlow(WalletFlowParamDto paramDto, AgentAccount agentAccount, Integer pageNo, Integer pageSize) {
        if (agentAccount.getAttributes() == Constants.EVNumber.four) {
            paramDto.setAgentId(agentAccount.getAgentId());
        } else {
            paramDto.setAgentId(agentAccount.getId());
        }
        PageHelper.startPage(pageNo, pageSize);
        List<WalletFlowResponseDto> responseDtos = financiaCenterMapper.payoffWalletFlow(paramDto);
        if (null!=responseDtos&&responseDtos.size()>0){
            for (WalletFlowResponseDto dto:responseDtos) {
                if (dto.getType().equals(OrderConstants.AGENT_MSF)){
                    dto.setLoginName(dto.getMerAccount());
                }else  if (dto.getType().equals(OrderConstants.AGENT_ASF)){
                    dto.setLoginName(dto.getAgyAccount());
                }
            }
        }

        return BeanUtil.toPagedResult(responseDtos);
    }

    public void withdrawal(AgyWithdraw withdraw, AgentAccount account, String ip) {
        Integer agentId = account.getId();
        if (account.getAttributes() == Constants.EVNumber.four) {
            agentId = account.getAgentId();
        }
        AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(agentId);
        AgyWallet wallet = new AgyWallet();
        wallet.setAccountId(agentAccount.getId());
        AgyWallet agyWallet = agyWalletMapper.selectOne(wallet);
        if (agyWallet.getBalance().compareTo(withdraw.getDrawingAmount()) == -1) {
            throw new R200Exception("佣金钱包余额不足");
        }
        String orderNo = String.valueOf(new SnowFlake().nextId());
        withdraw.setOrderNo(orderNo);
        withdraw.setAccountId(agentAccount.getId());
        withdraw.setStatus(Constants.EVNumber.two);
        withdraw.setType(Constants.EVNumber.zero);
        withdraw.setHandlingCharge(BigDecimal.ZERO);
        withdraw.setCutAmount(BigDecimal.ZERO);
        withdraw.setDiscountAmount(BigDecimal.ZERO);
        withdraw.setActualArrival(withdraw.getDrawingAmount());
        withdraw.setIp(ip);
        withdraw.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        withdraw.setCreateUser(agentAccount.getAgyAccount());
        withdraw.setMethodType(withdraw.getMethodType());

        if (Constants.EVNumber.one == withdraw.getMethodType().intValue()) {  // 加密货币 计算
            BigDecimal actualArrivalCryptoCurrencies = CommonUtil.adjustScale(withdraw.getDrawingAmount()
                    .divide(withdraw.getExchangeRate(), 4, RoundingMode.DOWN), 4);
            withdraw.setActualArrivalCr(actualArrivalCryptoCurrencies);
        }
        AgyWallet agyWallet1 = walletService.setAgyWallet(agentAccount,
                withdraw.getDrawingAmount(), OrderConstants.AGENT_ORDER_CODE_ATK,
                null, null, orderNo, agentAccount.getAgyAccount(),
                Constants.EVNumber.zero);
        AgyBillDetail billDetail = walletService.reduceWalletAndBillDetail(
                agyWallet1, Constants.EVNumber.zero);
        if (isNull(billDetail)) {
            throw new R200Exception("余额不足");
        }
        withdraw.setBillDetailId(billDetail.getId());
        withdrawMapper.insert(withdraw);
    }

    public void rechargeTransfer(RechargeTransferParamDto paramDto, AgentAccount agentAccount) {
        MbrAccount account1 = new MbrAccount();
        account1.setLoginName(paramDto.getLoginName());
        MbrAccount mbrAccount = mbrAccountMapper.selectOne(account1);
        if (isNull(mbrAccount)) {
            throw new R200Exception("请输入正确的转账账号");
        }
        if (!mbrAccount.getCagencyId().equals(agentAccount.getId())
                && !agentAccount.getId().equals(mbrAccount.getSubCagencyId())) {
            throw new R200Exception("请输入正确的转账账号");
        }
        Integer agentId = agentAccount.getId();
        if (agentAccount.getAttributes() == Constants.EVNumber.four) {
            agentId = agentAccount.getAgentId();
        }
        AgyWallet wallet = new AgyWallet();
        wallet.setAccountId(agentId);
        AgyWallet agyWallet = agyWalletMapper.selectOne(wallet);
        if (agyWallet.getRechargeWallet().compareTo(paramDto.getAmount()) == -1) {
            throw new R200Exception("代充钱包余额不足");
        }
        String financialCode = OrderConstants.AGENT_ADC;
        if (agentAccount.getAttributes() == Constants.EVNumber.two
                || agentAccount.getAttributes() == Constants.EVNumber.three) {
            financialCode = OrderConstants.AGENT_ASF;
        }
        String orderNo = String.valueOf(new SnowFlake().nextId());
        AgyWallet agyWallet1 = walletService.setAgyWallet(agentAccount,
                paramDto.getAmount(), financialCode,
                null, mbrAccount.getId(), orderNo, agyWallet.getAgyAccount(),
                Constants.EVNumber.one);
        AgyBillDetail billDetail = walletService.reduceWalletAndBillDetail(
                agyWallet1, Constants.EVNumber.zero);
        if (isNull(billDetail)) {
            throw new R200Exception("余额不足");
        }
        sdyDataService.addBalanceDeposit(paramDto.getAmount(), mbrAccount, orderNo);
    }

    public void payoffTransfer(RechargeTransferParamDto paramDto, AgentAccount agentAccount) {
        MbrAccount account1 = new MbrAccount();
        account1.setLoginName(paramDto.getLoginName());
        MbrAccount mbrAccount = mbrAccountMapper.selectOne(account1);
        if (isNull(mbrAccount)) {
            throw new R200Exception("请输入正确的转账账号");
        }
        // 如果当前会员的直属代理，分线代理不等于当前代理的id
        if (!mbrAccount.getCagencyId().equals(agentAccount.getId())
                && !agentAccount.getId().equals(mbrAccount.getSubCagencyId())) {
        	// 查询当前代理下的所有代理id
        	List<Integer> allLowerLevel = agentMapper.getAllLowerLevel(agentAccount.getAgyAccount());
        	// 所有的代理id都不包含当前会员的直属代理id，分线代理id
        	if (!allLowerLevel.contains(mbrAccount.getCagencyId()) && !allLowerLevel.contains(mbrAccount.getSubCagencyId())) {
        		throw new R200Exception("请输入正确的转账账号");
        	}
        }
        Integer agentId = agentAccount.getId();
        if (agentAccount.getAttributes() == Constants.EVNumber.four) {
            agentId = agentAccount.getAgentId();
        }
        AgyWallet wallet = new AgyWallet();
        wallet.setAccountId(agentId);
        AgyWallet agyWallet = agyWalletMapper.selectOne(wallet);
        if (agyWallet.getPayoffWallet().compareTo(paramDto.getAmount()) == -1) {
            throw new R200Exception("彩金钱包余额不足");
        }
        String financialCode = OrderConstants.AGENT_MSF;
        String orderNo = String.valueOf(new SnowFlake().nextId());
        AgyWallet agyWallet1 = walletService.setAgyWallet(agentAccount,
                paramDto.getAmount(), financialCode,
                null, mbrAccount.getId(), orderNo, agyWallet.getAgyAccount(),
                Constants.EVNumber.two);
        agyWallet1.setMemo(paramDto.getRemarks());
        AgyBillDetail billDetail = walletService.reduceWalletAndBillDetail(
                agyWallet1, Constants.EVNumber.zero);
        if (isNull(billDetail)) {
            throw new R200Exception("余额不足");
        }
        sdyDataService.addBalanceBounsByPayOffWallet(paramDto.getAmount(), mbrAccount, financialCode,paramDto.getAuditMultiple(),paramDto.getRemarks(),paramDto.getMoneyMultiple(),paramDto.getMoney());
    }

    public void commissionTransfer(RechargeTransferParamDto paramDto, AgentAccount account) {
        Integer agentId = account.getId();
        if (account.getAttributes() == Constants.EVNumber.four) {
            agentId = account.getAgentId();
        }
        AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(agentId);
        String orderNo = String.valueOf(new SnowFlake().nextId());

        AgyWallet wallet = new AgyWallet();
        wallet.setAccountId(agentAccount.getId());
        AgyWallet agyWallet = agyWalletMapper.selectOne(wallet);
        if (agyWallet.getBalance().compareTo(paramDto.getAmount()) == -1) {
            throw new R200Exception("佣金钱包余额不足");
        }

        if (paramDto.getType() == 1) {
            MbrAccount account1 = new MbrAccount();
            account1.setLoginName(agentAccount.getAgyAccount());
            MbrAccount mbrAccount = mbrAccountMapper.selectOne(account1);
            AgyWallet agyWallet1 = walletService.setAgyWallet(agentAccount,
                    paramDto.getAmount(), OrderConstants.AGENT_ORDER_CODE_AYS,
                    null, mbrAccount.getId(), orderNo, agyWallet.getAgyAccount(),
                    Constants.EVNumber.zero);
            AgyBillDetail billDetail = walletService.reduceWalletAndBillDetail(
                    agyWallet1, Constants.EVNumber.zero);
            if (isNull(billDetail)) {
                throw new R200Exception("余额不足");
            }
            sdyDataService.addBalanceDeposit(paramDto.getAmount(), mbrAccount, orderNo);
        } else {
            AgyWallet agyWallet1 = walletService.setAgyWallet(agentAccount,
                    paramDto.getAmount(), OrderConstants.AGENT_ORDER_CODE_ADC,
                    null, null, orderNo, agyWallet.getAgyAccount(),
                    Constants.EVNumber.zero);
            AgyBillDetail billDetail = walletService.reduceWalletAndBillDetail(
                    agyWallet1, Constants.EVNumber.zero);
            if (isNull(billDetail)) {
                throw new R200Exception("余额不足");
            }
            AgyWallet agyWallet3 = walletService.setAgyWallet(agentAccount,
                    paramDto.getAmount(), OrderConstants.AGENT_ORDER_CODE_AYJ,
                    null, null, orderNo, agyWallet.getAgyAccount(),
                    Constants.EVNumber.one);
            walletService.addWalletAndBillDetail(agyWallet3, Constants.EVNumber.one);
        }
    }

    public void checkoutSecurePwd(AgentAccount agentAccount, String securePwd) {
        String cjsecurePwd = new Sha256Hash(securePwd, agentAccount.getSalt()).toHex();
        if (!cjsecurePwd.equals(agentAccount.getSecurePwd())) {
            throw new R200Exception("支付密码错误");
        }
    }
}
