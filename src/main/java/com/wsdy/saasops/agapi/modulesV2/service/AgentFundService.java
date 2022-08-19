package com.wsdy.saasops.agapi.modulesV2.service;

import com.wsdy.saasops.agapi.modulesV2.dto.AccountFundDto;
import com.wsdy.saasops.agapi.modulesV2.dto.AgentFundDto;
import com.wsdy.saasops.agapi.modulesV2.dto.BillRecordListDto;
import com.wsdy.saasops.agapi.modulesV2.mapper.AgentFundMapper;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.transfer.dto.BillRequestDto;
import com.wsdy.saasops.api.modules.transferNew.dto.ResponseDto;
import com.wsdy.saasops.api.modules.transferNew.service.DepotService;
import com.wsdy.saasops.api.modules.user.dto.UserBalanceResponseDto;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgyWallet;
import com.wsdy.saasops.modules.agent.service.AgentWalletService;
import com.wsdy.saasops.modules.fund.service.FundReportService;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import com.wsdy.saasops.modules.member.entity.MbrBillManage;
import com.wsdy.saasops.modules.member.entity.MbrWallet;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.modules.operate.dao.TGmDepotMapper;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;


@Slf4j
@Service
@Transactional
public class AgentFundService {

    @Autowired
    private AgentFundMapper agentFundMapper;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private MbrWalletService walletService;
    @Autowired
    private AgentWalletService agentWalletService;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private DepotService depotService;
    @Autowired
    private FundReportService fundReportService;
    @Autowired
    private AgentV2AccountLogService agentV2AccountLogService;
    @Autowired
    private TGmDepotMapper gmDepotMapper;

    public PageUtils agentFundList(AgentFundDto agentFundDto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<AgentFundDto> list = agentFundMapper.agentFundList(agentFundDto);
        return BeanUtil.toPagedResult(list);
    }

    public void updateAgentBalance(AgentAccount agentAccount, AccountFundDto dto) {
        // 查询历史数据
        AgyWallet oldOperatee = agentWalletService.queryAgyWallet(agentAccount.getId());        // 操作者钱包    旧记录
        AgyWallet oldOperator = agentWalletService.queryAgyWallet(dto.getAccountId());          // 被操作者钱包    旧记录

        AgentAccount agentAccount1 = agentAccountMapper.selectByPrimaryKey(dto.getAccountId());
        if (isNull(agentAccount1)) {
            throw new R200Exception("代理不存在");
        }
        String orderNo = String.valueOf(new SnowFlake().nextId());
        // 余额增加
        if (dto.getIsBalance() == Constants.EVNumber.one) {
            if (agentAccount.getParentId() != 0) {
                AgyWallet agyWallet = setAgyWallet(agentAccount, dto.getBalance(),
                        OrderConstants.WQK_EG, dto.getAccountId(),
                        null, orderNo, agentAccount.getAgyAccount());
                agentWalletService.reduceWalletAndBillDetail(agyWallet, Constants.EVNumber.zero);
            }

            AgyWallet agyWallet1 = setAgyWallet(agentAccount1, dto.getBalance(),
                    OrderConstants.WCK_EG, agentAccount.getId(),
                    null, orderNo, agentAccount.getAgyAccount());
            agentWalletService.addWalletAndBillDetail(agyWallet1, Constants.EVNumber.one);
        }
        // 余额减少
        if (dto.getIsBalance() == Constants.EVNumber.zero) {

            AgyWallet agyWallet = setAgyWallet(agentAccount1, dto.getBalance(),
                    OrderConstants.WQK_EG, agentAccount.getId(),
                    null, orderNo, agentAccount.getAgyAccount());
            agentWalletService.reduceWalletAndBillDetail(agyWallet, Constants.EVNumber.zero);

            if (agentAccount.getParentId() != 0) {
                AgyWallet agyWallet1 = setAgyWallet(agentAccount, dto.getBalance(),
                        OrderConstants.WCK_EG, agentAccount1.getId(),
                        null, orderNo, agentAccount.getAgyAccount());
                agentWalletService.addWalletAndBillDetail(agyWallet1, Constants.EVNumber.one);
            }
        }

        // 插入账户记录
        if (agentAccount.getParentId() != 0) {  // 操作者为非公司
            // 操作者日志
            AgyWallet newOperatee = agentWalletService.queryAgyWallet(agentAccount.getId());        // 操作者钱包    新记录
            agentV2AccountLogService.updateAgentBalanceAgy(oldOperatee,newOperatee, agentAccount);
        }
        // 被操作者日志
        AgyWallet newOperator = agentWalletService.queryAgyWallet(dto.getAccountId());        // 被操作者钱包    新记录
        agentV2AccountLogService.updateAgentBalanceAgy(oldOperator,newOperator, agentAccount);
    }

    private AgyWallet setAgyWallet(AgentAccount agentAccount, BigDecimal amount, String financialCode,
                                   Integer agentId, Integer merAccountId, String orderNo, String createuser) {
        AgyWallet agyWallet = new AgyWallet();
        agyWallet.setAccountId(agentAccount.getId());
        agyWallet.setAgyAccount(agentAccount.getAgyAccount());
        agyWallet.setBalance(amount);
        agyWallet.setFinancialCode(financialCode);
        agyWallet.setAgentId(agentId);
        agyWallet.setMerAccountId(merAccountId);
        agyWallet.setOrderNo(orderNo);
        agyWallet.setCreateuser(createuser);
        return agyWallet;
    }

    public PageUtils accountFundList(AgentFundDto agentFundDto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<AgentFundDto> list = agentFundMapper.accountFundList(agentFundDto);
        return BeanUtil.toPagedResult(list);
    }

    public BigDecimal accountDepotBalance(Integer accountId) {
        UserBalanceResponseDto responseDto = depotService.queryDepotBalance(accountId, 27, CommonUtil.getSiteCode());
        return responseDto.getBalance();
    }

    public void updateAccountBalance(AgentAccount agentAccount, AccountFundDto dto) {
        // 查询历史数据
        AgyWallet oldOperatee = agentWalletService.queryAgyWallet(agentAccount.getId());
        BigDecimal oldOperatorBalance = getTotalBalance(dto);                               // 被操作者总余额    旧记录

        dto.setBalance(new BigDecimal(dto.getBalance().intValue()));
        MbrAccount account = accountMapper.selectByPrimaryKey(dto.getAccountId());
        if (isNull(account)) {
            throw new R200Exception("会员不存在");
        }
        String orderNo = String.valueOf(new SnowFlake().nextId());
        // 余额增加
        if (dto.getIsBalance() == Constants.EVNumber.one) {
            if (agentAccount.getParentId() != 0) {
                AgyWallet agyWallet = setAgyWallet(agentAccount, dto.getBalance(),
                        OrderConstants.WQK_EG,
                        null, account.getId(), orderNo, agentAccount.getAgyAccount());
                agentWalletService.reduceWalletAndBillDetail(agyWallet, Constants.EVNumber.zero);
            }
            MbrBillDetail billDetail = walletService.castWalletAndBillDetail(account.getLoginName(), account.getId(),
                    OrderConstants.WCK_EG, dto.getBalance(), orderNo, Boolean.TRUE, agentAccount.getId(), agentAccount.getAgyAccount());
            if (isNull(billDetail)) {
                throw new R200Exception("操作钱包失败");
            }
            egAccountTransferOut(account);
        }
        // 余额减少
        if (dto.getIsBalance() == Constants.EVNumber.zero) {
            MbrWallet mbrWallet = fundReportService.queryAccountBalance(account.getLoginName());
            if (mbrWallet.getBalance().compareTo(dto.getBalance()) != -1) {
                accountMbrWalletIn(agentAccount, account, dto, orderNo);
            }
            if (mbrWallet.getBalance().compareTo(dto.getBalance()) == -1) {
                egAccountTransferIn(account, dto, mbrWallet);
                accountMbrWalletIn(agentAccount, account, dto, orderNo);
            }
        }

        // 插入账户记录
        if (agentAccount.getParentId() != 0) {  // 操作者为非公司
            // 操作者日志
            AgyWallet newOperatee = agentWalletService.queryAgyWallet(agentAccount.getId());        // 操作者钱包    新记录
            agentV2AccountLogService.updateAgentBalanceAgy(oldOperatee,newOperatee, agentAccount);
        }
        // 被操作者日志
        BigDecimal newOperatorBalance = getTotalBalance(dto);       // 被操作者总余额    新记录
        agentV2AccountLogService.updateAccountBalance(account.getLoginName(),oldOperatorBalance,newOperatorBalance, agentAccount);
    }

    private BigDecimal getTotalBalance(AccountFundDto dto){
        MbrWallet Operator = walletService.getBalance(dto.getAccountId());                   // 被操作者钱包-平台
        BigDecimal OperatorBalance = accountDepotBalance(dto.getAccountId());                // 被操作者钱包-中心
        OperatorBalance = OperatorBalance.add(Operator.getBalance()) ;
        return OperatorBalance;
    }

    private void accountMbrWalletIn(AgentAccount agentAccount, MbrAccount account, AccountFundDto dto, String orderNo) {
        MbrBillDetail billDetail = walletService.castWalletAndBillDetail(account.getLoginName(), account.getId(),
                OrderConstants.WQK_EG, dto.getBalance(), orderNo, Boolean.FALSE, agentAccount.getId(), agentAccount.getAgyAccount());
        if (isNull(billDetail)) {
            throw new R200Exception("操作钱包失败");
        }
        if (agentAccount.getParentId() != 0) {
            AgyWallet agyWallet1 = setAgyWallet(agentAccount, dto.getBalance(),
                    OrderConstants.WCK_EG, null, account.getId(), orderNo, agentAccount.getAgyAccount());
            agentWalletService.addWalletAndBillDetail(agyWallet1, Constants.EVNumber.one);
        }
    }

    public void egAccountTransferIn(MbrAccount account, AccountFundDto dto, MbrWallet mbrWallet) {
        BigDecimal wBalance = BigDecimalMath.formatDownRounding(mbrWallet.getBalance());
        BigDecimal bigDecimal = BigDecimalMath.formatDownRounding(dto.getBalance().subtract(wBalance));
        BigDecimal depotAmout = accountDepotBalance(account.getId());
        if (depotAmout.compareTo(bigDecimal) == -1) {
            throw new R200Exception("余额不足");
        }
        BillRequestDto billRequestDto = new BillRequestDto();
        billRequestDto.setAccountId(account.getId());
        billRequestDto.setLoginName(account.getLoginName());

        TGmDepot depot = new TGmDepot();
        depot.setDepotCode(ApiConstants.DepotCode.EG);
        List<TGmDepot> list = gmDepotMapper.select(depot);
        if(Objects.nonNull(list) && list.size() > 0){
            billRequestDto.setDepotId(list.get(0).getId());
        }

        billRequestDto.setTransferSource((byte) 0);
        billRequestDto.setAmount(bigDecimal);
        ResponseDto responseDto = depotService.accountTransferIn(billRequestDto, CommonUtil.getSiteCode());
        if (Boolean.FALSE.equals(responseDto.getIsSucceed())) {
            throw new R200Exception(responseDto.getError());
        }
    }

    public void egAccountTransferOut(MbrAccount account) {
        MbrWallet mbrWallet = fundReportService.queryAccountBalance(account.getLoginName());
        if (mbrWallet.getBalance().compareTo(BigDecimal.ZERO) == 1) {
            BillRequestDto requestDto = new BillRequestDto();
            requestDto.setAccountId(mbrWallet.getId());

            TGmDepot depot = new TGmDepot();
            depot.setDepotCode(ApiConstants.DepotCode.EG);
            List<TGmDepot> list = gmDepotMapper.select(depot);
            if(Objects.nonNull(list) && list.size() > 0){
                requestDto.setDepotId(list.get(0).getId());
            }
            requestDto.setLoginName(account.getLoginName());
            requestDto.setTransferSource((byte) 0);
            BigDecimal bd = new BigDecimal(mbrWallet.getBalance().toString());
            requestDto.setAmount(bd.setScale(0, BigDecimal.ROUND_DOWN));
            depotService.accountTransferOut(requestDto, CommonUtil.getSiteCode());
        }
    }


    public PageUtils billListPage(MbrBillManage mbrBillManage, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<MbrBillManage> list = agentFundMapper.findMbrBillManageList(mbrBillManage);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils billRecordList(BillRecordListDto recordListDto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<BillRecordListDto> list = agentFundMapper.billRecordList(recordListDto);
        return BeanUtil.toPagedResult(list);
    }
}
