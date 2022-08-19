package com.wsdy.saasops.modules.member.service;


import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.transfer.dto.BillRequestDto;
import com.wsdy.saasops.api.modules.transfer.dto.DepotFailDtosDto;
import com.wsdy.saasops.api.modules.transferNew.service.DepotService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.member.dao.MbrBillDetailMapper;
import com.wsdy.saasops.modules.member.dao.MbrWalletMapper;
import com.wsdy.saasops.modules.member.dto.BalanceDto;
import com.wsdy.saasops.modules.member.dto.DepotFailDto;
import com.wsdy.saasops.modules.member.entity.*;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail.OpTypeStatus;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.google.common.collect.Lists;
import com.wsdy.saasops.modules.operate.dao.TGmDepotMapper;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_25_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.nonNull;


@Service
@Slf4j
public class MbrWalletService extends BaseService<MbrWalletMapper, MbrWallet> {

    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private DepotService depotService;
    @Autowired
    private MbrBillDetailMapper mbrBillDetailMapper;
    @Autowired
    private MbrDepotWalletService mbrDepotWalletService;
    @Autowired
    private MbrDepotAsyncWalletService mbrDepotAsyncWalletService;
    @Autowired
    private TGmDepotMapper gmDepotMapper;

    public MbrWallet queryById(Integer accountId) {
        MbrWallet wallet = new MbrWallet();
        wallet.setAccountId(accountId);
        return queryObjectCond(wallet);
    }

    public List<MbrAccount> listAccName(Integer[] accountIds) {
        return mbrMapper.listAccName(accountIds);
    }

    /**
     * 资金减少
     *
     * @return
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public boolean walletSubtract(MbrWallet mbrWallet, MbrBillDetail mbrBillDetail) {
        int opRecord = mbrMapper.walletSubtract(mbrWallet);
        if (opRecord > 0) {
            MbrWallet entity = new MbrWallet();
            entity.setAccountId(mbrWallet.getAccountId());
            entity = queryObjectCond(entity);
            mbrBillDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
            mbrBillDetail.setOpType(OpTypeStatus.expenditure);
            mbrBillDetail.setAfterBalance(entity.getBalance());
            mbrBillDetail.setBeforeBalance(
                    BigDecimalMath.round(BigDecimalMath.add(entity.getBalance(), mbrBillDetail.getAmount()), 2));
            opRecord = mbrBillDetailMapper.insert(mbrBillDetail);
        }
        return opRecord > 0 ? true : false;
    }

    /**
     * 资金add
     *
     * @return
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public Boolean walletAdd(MbrWallet mbrWallet, MbrBillDetail mbrBillDetail) {
        int opRecord = mbrMapper.walletAdd(mbrWallet);
        if (opRecord > 0) {
            MbrWallet entity = new MbrWallet();
            entity.setAccountId(mbrWallet.getAccountId());
            entity = queryObjectCond(entity);
            mbrBillDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
            mbrBillDetail.setOpType(OpTypeStatus.income);
            mbrBillDetail.setAfterBalance(entity.getBalance());
            mbrBillDetail.setBeforeBalance(
                    BigDecimalMath.round(BigDecimalMath.sub(entity.getBalance(), mbrBillDetail.getAmount()), 2));
            if (opRecord > 0) {
                mbrBillDetailMapper.insert(mbrBillDetail);
            }
        }
        return opRecord > 0 ? true : false;
    }


    /**
     * 资金减少
     *
     * @return
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public boolean hPWalletSubtract(MbrWallet mbrWallet, MbrBillDetail mbrBillDetail) {
        int opRecord = mbrMapper.hPWalletSubtract(mbrWallet);

        if (opRecord > 0) {
            MbrWallet entity = new MbrWallet();
            entity.setAccountId(mbrWallet.getAccountId());
            entity = queryObjectCond(entity);
            mbrBillDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
            mbrBillDetail.setOpType(OpTypeStatus.income);
            mbrBillDetail.setAfterBalance(entity.getHuPengBalance());
            mbrBillDetail.setBeforeBalance(
                    BigDecimalMath.round(BigDecimalMath.sub(entity.getHuPengBalance(), mbrBillDetail.getAmount()), 2));
            if (opRecord > 0) {
                mbrBillDetailMapper.insert(mbrBillDetail);
            }
        }
        return opRecord > 0 ? true : false;
    }


    /**
     * 资金增加
     *
     * @return
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public boolean hPWalletAdd(MbrWallet mbrWallet, MbrBillDetail mbrBillDetail) {
        int opRecord = mbrMapper.hPWalletAdd(mbrWallet);

        if (opRecord > 0) {
            MbrWallet entity = new MbrWallet();
            entity.setAccountId(mbrWallet.getAccountId());
            entity = queryObjectCond(entity);
            mbrBillDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
            mbrBillDetail.setOpType(OpTypeStatus.income);
            mbrBillDetail.setAfterBalance(entity.getHuPengBalance());
            mbrBillDetail.setBeforeBalance(
                    BigDecimalMath.round(BigDecimalMath.sub(entity.getHuPengBalance(), mbrBillDetail.getAmount()), 2));
            if (opRecord > 0) {
                mbrBillDetailMapper.insert(mbrBillDetail);
            }
        }
        return opRecord > 0 ? true : false;
    }

    /**
     * 钱包转平台
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public Boolean walletArriveDepot(MbrWallet mbrWallet, MbrBillManage mbrBillManage) {
        if (mbrWallet.getBalance().compareTo(BigDecimal.ONE) != -1) {
            int isSucceed = mbrMapper.walletSubtract(mbrWallet);
            if (isSucceed == 0) {
                return Boolean.FALSE;
            }
            mbrWallet.setBalance(null);
            MbrWallet wallet = queryObjectCond(mbrWallet);
            mbrBillManage.setAfterBalance(wallet.getBalance());
            mbrBillManage.setBeforeBalance(
                    BigDecimalMath.round(BigDecimalMath.add(wallet.getBalance(),
                            mbrBillManage.getAmount()), 2));
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 平台转钱包
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public Boolean depotArriveWallet(MbrWallet mbrWallet, MbrBillDetail mbrBillDetail) {
        int isSucceed = mbrMapper.walletAdd(mbrWallet);
        if (isSucceed == 0) {
            return Boolean.FALSE;
        }
        mbrWallet.setBalance(null);
        MbrWallet wallet = queryObjectCond(mbrWallet);
        mbrBillDetail.setAfterBalance(wallet.getBalance());
        mbrBillDetail.setBeforeBalance(
                BigDecimalMath.round(BigDecimalMath.sub(wallet.getBalance(), mbrBillDetail.getAmount()), 2));
        return Boolean.TRUE;
    }

    public MbrDepotWallet queryObjectCond(MbrDepotWallet mbrWallet, String siteCode) {
        return mbrDepotWalletService.queryObjectCond(mbrWallet);
    }

    public MbrDepotWallet noExistInsert(MbrDepotWallet mbrWallet, String siteCode) {
        MbrDepotWallet queryWallet = new MbrDepotWallet();
        queryWallet.setAccountId(mbrWallet.getAccountId());
        queryWallet.setDepotId(mbrWallet.getDepotId());
        MbrDepotWallet depotWallet = mbrDepotWalletService.queryObjectCond(queryWallet);
        if (Objects.isNull(depotWallet)) {
            mbrWallet.setBalance(ApiConstants.DEAULT_ZERO_VALUE);
            mbrWallet.setIsTransfer(MbrDepotWallet.IsTransFer.no);
            mbrWallet.setIsLogin(Constants.Available.enable);
            mbrWallet.setIsBuild(Boolean.TRUE);
            mbrDepotWalletService.save(mbrWallet);
            return mbrWallet;
        }
        return queryWallet;
    }

    public List<MbrDepotWallet> getDepotWallet(MbrDepotWallet walletModel) {
        MbrDepotWallet mbrWallet = new MbrDepotWallet();
        mbrWallet.setAccountId(walletModel.getAccountId());
        mbrWallet.setDepotIds(walletModel.getDepotIds());
        return mbrDepotWalletService.queryCondDepot(mbrWallet);
    }

    // 排除掉t188平台
    public List<Integer> getIntegersDepot() {
        List<Integer> depotList = new ArrayList<>();

        TGmDepot depot = new TGmDepot();
        depot.setDepotCode(ApiConstants.DepotCode.T188);
        List<TGmDepot> list = gmDepotMapper.select(depot);
        if(Objects.nonNull(list) && list.size() > 0){
            depotList.add(list.get(0).getId());
        }

        return depotList;
    }

    /**
     * 封装新老平台批量回收余额
     * 平台到中心钱包
     *
     * @param depotFailDtosDto
     * @return
     */
    public List<DepotFailDto> getDepotFailDtos(DepotFailDtosDto depotFailDtosDto) {
        // 不支持apigateway回收余额的平台id
        List<Integer> getDepotList = getIntegersDepot();

        // 支持apigateway异步余额回收的平台List: 提前获取，下面会setDepotWallets
        List<Integer> newDepotIds = depotFailDtosDto.getDepotWallets()
                .stream().filter(e -> !getDepotList.contains(e.getDepotId()))
                .map(MbrDepotWallet::getDepotId).collect(Collectors.toList());

        // 不支持apigateway回收余额的平台钱包
        List<MbrDepotWallet> depotWallets = depotFailDtosDto.getDepotWallets()
                .stream().filter(e -> getDepotList.contains(e.getDepotId())).collect(Collectors.toList());
        depotFailDtosDto.setDepotWallets(depotWallets);
        // 不支持apigateway回收余额的平台走这逻辑： 平台->中心钱包 --> 目前就188   用废弃的单独平台的接口
        List<DepotFailDto> depotFailDtoList = mbrDepotAsyncWalletService.getAsyncRecoverBalance(depotFailDtosDto);
        depotFailDtosDto.setRecoverBalanceList(depotFailDtoList);

        // 回收余额
        List<DepotFailDto> errDepotFails = Lists.newArrayList();
        if (Collections3.isNotEmpty(newDepotIds)) {
            BillRequestDto requestDto = new BillRequestDto();
            requestDto.setDepotIds(newDepotIds);
            requestDto.setAccountId(depotFailDtosDto.getUserId());
            requestDto.setLoginName(depotFailDtosDto.getLoginName());
            requestDto.setDev(depotFailDtosDto.getDev());
            requestDto.setTransferSource(depotFailDtosDto.getTransferSource());
            // 并发余额回收：转账
            String siteCode = nonNull(depotFailDtosDto.getSiteCode()) ? depotFailDtosDto.getSiteCode() : CommonUtil.getSiteCode();
            List<DepotFailDto> depotFailDtos = depotService.depotTransferBatchFuture(requestDto,siteCode);
            // 筛选转账失败的结果，并返回
            if (Collections3.isNotEmpty(depotFailDtos)) {
                errDepotFails = depotFailDtos.stream().filter(e -> e.getFailError().equals(Boolean.FALSE)).collect(Collectors.toList());
            }
        }
        return errDepotFails;
    }


    public MbrWallet getBalance(Integer userId) {
        MbrWallet mbrWallet = new MbrWallet();
        mbrWallet.setAccountId(userId);
        return super.queryObjectCond(mbrWallet);

    }

    public MbrBillDetail castWalletAndBillDetail(
            String loginName, int accountId, String financialCode,
            BigDecimal amount, String orderNo, Boolean isSign, Integer agentId, String createuser) {
        log.info("castWalletAndBillDetail开始");
        MbrBillDetail billDetail = setMbrBillDetail(loginName, accountId, financialCode, amount, orderNo, agentId);
        log.info("castWalletAndBillDetail -> setMbrBillDetail结束");
        billDetail.setCreateuser(createuser);
        MbrWallet mbrWallet = new MbrWallet();
        mbrWallet.setBalance(billDetail.getAmount());
        mbrWallet.setAccountId(billDetail.getAccountId());
        Boolean isSuccess = null;
        if (Boolean.TRUE.equals(isSign)) {
            isSuccess = walletAdd(mbrWallet, billDetail);
        }
        if (Boolean.FALSE.equals(isSign)) {
            isSuccess = walletSubtract(mbrWallet, billDetail);
        }
        log.info("castWalletAndBillDetail -> setMbrBillDetail -> wallet操作结束");
        if (Boolean.TRUE.equals(isSuccess)) {
            return billDetail;
        }
        return null;
    }

    public MbrBillDetail castWalletAndBillDetailSucToFail(
            String loginName, int accountId, String financialCode, BigDecimal amount, String orderNo,
            Boolean isSign, Integer agentId, String createuser, String memo) {
        MbrBillDetail billDetail = setMbrBillDetailWithMemo(loginName, accountId, financialCode, amount, orderNo, agentId, memo);
        billDetail.setCreateuser(createuser);
        MbrWallet mbrWallet = new MbrWallet();
        mbrWallet.setBalance(billDetail.getAmount());
        mbrWallet.setAccountId(billDetail.getAccountId());
        Boolean isSuccess = null;
        if (Boolean.TRUE.equals(isSign)) {
            isSuccess = walletAdd(mbrWallet, billDetail);
        }
        if (Boolean.FALSE.equals(isSign)) {
            isSuccess = walletSubtract(mbrWallet, billDetail);
        }
        if (Boolean.TRUE.equals(isSuccess)) {
            return billDetail;
        }
        return null;
    }

    public MbrBillDetail castTransferWalletSubtract(BillRequestDto requestDto) {
        MbrBillDetail billDetail = setMbrBillDetail(requestDto.getLoginName(), requestDto.getAccountId(),
                OrderConstants.FUND_ORDER_TRIN, requestDto.getSumAmount(), requestDto.getOrderNo(), null);
        MbrWallet mbrWallet = new MbrWallet();
        mbrWallet.setBalance(billDetail.getAmount());
        mbrWallet.setBonusAmount(requestDto.getBonusAmount());
        mbrWallet.setAccountId(billDetail.getAccountId());
        Boolean isSuccess = walletSubtract(mbrWallet, billDetail);
        if (Boolean.TRUE.equals(isSuccess)) {
            return billDetail;
        }
        return null;
    }

    public MbrBillDetail setMbrBillDetail(String loginName, int accountId, String financialCode, BigDecimal amount, String orderNo, Integer agentId) {
        MbrBillDetail billDetail = new MbrBillDetail();
        billDetail.setLoginName(loginName);
        billDetail.setAccountId(accountId);
        billDetail.setFinancialCode(financialCode);
        billDetail.setOrderNo(!StringUtils.isEmpty(orderNo) ? orderNo : String.valueOf(new SnowFlake().nextId()));
        billDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
        billDetail.setDepotId(Constants.SYS_DEPOT_ID);
        billDetail.setAmount(amount);
        billDetail.setAgentId(agentId);
        return billDetail;
    }

    public MbrBillDetail setMbrBillDetailWithMemo(String loginName, int accountId, String financialCode, BigDecimal amount, String orderNo, Integer agentId,
                                                  String memo) {
        MbrBillDetail billDetail = new MbrBillDetail();
        billDetail.setLoginName(loginName);
        billDetail.setAccountId(accountId);
        billDetail.setFinancialCode(financialCode);
        billDetail.setOrderNo(!StringUtils.isEmpty(orderNo) ? orderNo : String.valueOf(new SnowFlake().nextId()));
        billDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
        billDetail.setDepotId(Constants.SYS_DEPOT_ID);
        billDetail.setAmount(amount);
        billDetail.setAgentId(agentId);
        billDetail.setMemo(memo);
        return billDetail;
    }


    public List<BalanceDto> balancelist(Integer accountId) {
        MbrWallet wallet = getBalance(accountId);
        ArrayList<BalanceDto> list = new ArrayList<BalanceDto>();
        BalanceDto balanceDto = new BalanceDto();
        balanceDto.setBalance(wallet.getBalance());
        balanceDto.setDepotName(Constants.SYSTEM_DEPOT_NAME);
        list.add(balanceDto);
        MbrDepotWallet mbrDepotWallet = new MbrDepotWallet();
        mbrDepotWallet.setAccountId(accountId);
        PageUtils utils = mbrDepotWalletService.queryListPage(mbrDepotWallet, 1, 100, null);
        List<MbrDepotWallet> mbrDepotWallets = (List<MbrDepotWallet>) utils.getList();
        mbrDepotWallets.forEach(e -> {
            if (e.getBalance().compareTo(BigDecimal.ZERO) == 1) {
                BalanceDto dto = new BalanceDto();
                dto.setBalance(e.getBalance());
                dto.setDepotName(e.getDepotName());
                list.add(dto);
            }
        });
        return list;
    }

    @Transactional
	public void updateAdjustment(MbrWallet mbrWallet) {
    	mbrMapper.updateAdjustment(mbrWallet);
	}
}
