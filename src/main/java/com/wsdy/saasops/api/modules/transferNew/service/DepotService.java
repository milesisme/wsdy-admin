package com.wsdy.saasops.api.modules.transferNew.service;

import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.apisys.entity.TGmApi;
import com.wsdy.saasops.api.modules.apisys.service.TGmApiService;
import com.wsdy.saasops.api.modules.transfer.dto.BillManBooleanDto;
import com.wsdy.saasops.api.modules.transfer.dto.BillRequestDto;
import com.wsdy.saasops.api.modules.transfer.dto.TransferAmoutDto;
import com.wsdy.saasops.api.modules.transfer.service.TransferService;
import com.wsdy.saasops.api.modules.transferNew.dto.GatewayResponseDto;
import com.wsdy.saasops.api.modules.transferNew.dto.ResponseDto;
import com.wsdy.saasops.api.modules.unity.dto.TransferModel;
import com.wsdy.saasops.api.modules.user.dto.UserBalanceResponseDto;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.BigDecimalMath;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.member.dao.MbrDepotWalletMapper;
import com.wsdy.saasops.modules.member.dto.AuditBonusDto;
import com.wsdy.saasops.modules.member.dto.DepotFailDto;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrDepotWallet;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import com.wsdy.saasops.modules.operate.dto.BonusListDto;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import com.wsdy.saasops.modules.operate.service.OprActActivityCastService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;


@Slf4j
@Service
public class DepotService {

    @Autowired
    private TGmApiService gmApiService;
    @Autowired
    private MbrAccountService userService;
    @Autowired
    private TransferService transferService;
    @Autowired
    private TransferNewService transferNewService;
    @Autowired
    private MbrDepotWalletMapper depotWalletMapper;
    @Autowired
    private GatewayDepotService gatewayDepotService;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private OprActActivityCastService actActivityCastService;
    @Autowired
    private MbrWalletService walletService;

    /**
     * ????????????(????????????)
     */
    public UserBalanceResponseDto queryDepotBalanceNew(MbrAccount user, TGmApi gmApi) {
        return queryDepotBalance(user.getId(), gmApi.getDepotId(), gmApi.getSiteCode());
    }

    /**
     * ????????????
     */
    public UserBalanceResponseDto queryDepotBalance(Integer accountId, Integer depotId, String siteCode) {
        MbrDepotWallet depotWallet = new MbrDepotWallet();
        depotWallet.setAccountId(accountId);
        depotWallet.setDepotId(depotId);
        MbrDepotWallet wallet = depotWalletMapper.selectOne(depotWallet);
        BigDecimal balance = BigDecimal.ZERO;
        if (Objects.nonNull(wallet) && wallet.getIsTransfer() == Constants.EVNumber.one) {
            TGmApi gmApi = gmApiService.queryApiObject(depotId, siteCode);
            GatewayResponseDto gatewayDto = gatewayDepotService.queryBalance(
                    transferNewService.getLoginModel(gmApi, wallet.getLoginName()));
            if (nonNull(gatewayDto) && Boolean.TRUE.equals(gatewayDto.getStatus())
                    && StringUtils.isNotEmpty(gatewayDto.getMessage())) {
                balance = CommonUtil.adjustScale(new BigDecimal(gatewayDto.getMessage()));
            }
        }
        UserBalanceResponseDto balanceDto = new UserBalanceResponseDto();
        balanceDto.setBalance(balance);
        balanceDto.setCurrency(ApiConstants.CURRENCY_TYPE);
        return balanceDto;
    }

    public TGmApi getAllGmApi(BillRequestDto requestDto, String siteCode) {
        TGmApi gmApi = gmApiService.queryApiObject(requestDto.getDepotId(), siteCode);
        if (gmApi == null) {
            throw new RRException("?????????????????????????????????");
        }
        return gmApi;
    }

    /**
     * ?????????????????? ?????? -???????????????
     *
     * @param requestDto
     * @param siteCode
     * @return
     */
    public ResponseDto accountTransferIn(BillRequestDto requestDto, String siteCode) {
        ResponseDto responseDto = new ResponseDto();
        TGmApi gmApi = getAllGmApi(requestDto, siteCode);
        AuditBonusDto auditBonusDto = new AuditBonusDto();
        MbrAccount account = new MbrAccount();
        account.setId(requestDto.getAccountId());
        account.setLoginName(requestDto.getLoginName());
        if (Boolean.TRUE.equals(requestDto.getIsTransferBouns())) {
            auditBonusDto = auditAccountService.outAuditBonus(account, requestDto.getDepotId());
        }
        if (Boolean.TRUE.equals(auditBonusDto.getIsFraud()) && Boolean.TRUE.equals(auditBonusDto.getIsSucceed())) {
            List<Integer> depotIds = walletService.getIntegersDepot();
            Boolean isSuceed = Boolean.FALSE;
            if (depotIds.contains(requestDto.getDepotId())) {
                isSuceed = transferService.getAllDepotTransferOut(requestDto, gmApi);
                responseDto.setIsSucceed(isSuceed);
                responseDto.setError(Boolean.FALSE.equals(isSuceed) ? "????????????" : null);
            } else {
                responseDto = transferNewService.accountTransferIn(requestDto, siteCode);
            }
            if (Boolean.TRUE.equals(isSuceed) || Boolean.TRUE.equals(responseDto.getIsSucceed())) {
                auditAccountService.succeedAuditBonus(getMbrAccount(requestDto), requestDto.getDepotId());
            }
        } else {
            responseDto.setIsSucceed(Boolean.FALSE);
            responseDto.setError("???????????????????????????");
        }
        return responseDto;
    }
    public ResponseDto accountTransferOutPlatform(BillRequestDto requestDto, String siteCode){
        ResponseDto responseDto = new ResponseDto();
        TGmApi gmApi = getAllGmApi(requestDto, siteCode);
        MbrAccount account = new MbrAccount();
        account.setId(requestDto.getAccountId());
        account.setLoginName(requestDto.getLoginName());

        List<Integer> depotIds = walletService.getIntegersDepot();
        Boolean isSuceed = Boolean.FALSE;
        if (depotIds.contains(requestDto.getDepotId())) {
            isSuceed = transferService.getAllDepotTransferOut(requestDto, gmApi);
            responseDto.setIsSucceed(isSuceed);
            responseDto.setError(Boolean.FALSE.equals(isSuceed) ? "????????????" : null);
        } else {
            responseDto = transferNewService.accountTransferIn(requestDto, siteCode);
        }
        return responseDto;
    }



    /**
     * ?????? ??????-???????????????
     *
     * @param requestDto ???????????????depotIds?????????????????????????????????
     * @param siteCode   ??????siteCode
     * @return
     */
    public List<DepotFailDto> depotTransferBatchFuture(BillRequestDto requestDto, String siteCode) {
        // ??????????????????
        List<CompletableFuture<DepotFailDto>> depotFindingFutureList =
                requestDto.getDepotIds().stream().map(depotId ->
                        depotTransfeFuture(requestDto, depotId, siteCode)).collect(Collectors.toList());

        CompletableFuture<Void> allFutures =
                CompletableFuture
                        .allOf(depotFindingFutureList.toArray(
                                new CompletableFuture[depotFindingFutureList.size()]));

        CompletableFuture<List<DepotFailDto>> depotFailResults = allFutures.thenApply(v -> {
            return depotFindingFutureList.stream().map(
                    depotFailDtoCompletableFuture -> depotFailDtoCompletableFuture.join())
                    .collect(Collectors.toList());
        });
        try {
            return depotFailResults.get();
        } catch (Exception e) {
            return null;
        }
    }

    CompletableFuture<DepotFailDto> depotTransfeFuture(BillRequestDto requestDto, Integer depotId, String siteCode) {

        return CompletableFuture.supplyAsync(() -> {
            DepotFailDto depotFailDto = new DepotFailDto();
            depotFailDto.setDepotId(depotId);
            try {
                ThreadLocalCache.setSiteCodeAsny(siteCode);
                // ???????????????????????????????????????????????????auditBonusDto ??????true, ???????????????????????????
                MbrAccount account = new MbrAccount();
                account.setId(requestDto.getAccountId());
                AuditBonusDto auditBonusDto = auditAccountService.outAuditBonus(account, depotId);
                // ???if?????????????????????
                if (Boolean.FALSE.equals(auditBonusDto.getIsSucceed()) || Boolean.FALSE.equals(auditBonusDto.getIsFraud())) {
                    depotFailDto.setIsSign(Constants.EVNumber.one);
                    depotFailDto.setError("????????????");
                    depotFailDto.setFailError(Boolean.FALSE);
                    return depotFailDto;
                }

                // ???????????????->????????????
                BillRequestDto billRequestDto = new BillRequestDto();
                billRequestDto.setAccountId(requestDto.getAccountId());
                billRequestDto.setLoginName(requestDto.getLoginName());
                billRequestDto.setDev(requestDto.getLoginName());
                billRequestDto.setDepotId(depotId);
                billRequestDto.setTransferSource(requestDto.getTransferSource());
                ResponseDto responseDto = transferNewService.accountTransferIn(billRequestDto, siteCode);

                // ????????????????????????
                if (Boolean.TRUE.equals(responseDto.getIsSucceed())) {  // ??????
                    depotFailDto.setFailError(Boolean.TRUE);
                } else {    // ??????
                    log.info(depotId + "??????,??????-????????????????????????" + responseDto.getError() + "???");
                    depotFailDto.setError(responseDto.getError());
                    depotFailDto.setIsSign(Constants.EVNumber.zero);
                    depotFailDto.setFailError(Boolean.FALSE);
                }
                return depotFailDto;
            } catch (Exception e) {
                log.error(depotId + "??????,??????-?????????????????????", e);
                depotFailDto.setError("??????????????????");
                depotFailDto.setFailError(Boolean.FALSE);
                depotFailDto.setIsSign(Constants.EVNumber.zero);
                return depotFailDto;
            }
        });
    }

    /**
     * ??????????????????
     *
     * @param requestDto
     * @param sitePrefix
     * @return
     */
    public TGmApi gettGmApi(BillRequestDto requestDto, String sitePrefix) {
        MbrAccount user = userService.queryObject(requestDto.getAccountId(), sitePrefix);
        if (user.getAvailable() == MbrAccount.Status.LOCKED) {
            throw new RRException("?????????????????????,????????????!");
        }
        TGmApi gmApi = gmApiService.queryApiObject(requestDto.getDepotId(), sitePrefix);
        if (gmApi == null) {
            throw new RRException("?????????????????????????????????");
        }
        return gmApi;
    }

    /**
     * ???????????????????????????????????????
     *
     * @param requestDto
     * @return
     */
    public OprActBonus getOprActBonus(BillRequestDto requestDto) {
      /*  //TODO ????????????????????????ID
        if (!Objects.isNull(requestDto.getBonusId())) {
            return auditAccountService.getAccountBonus(requestDto.getDepotId(), requestDto.getCatId(),
                    requestDto.getBonusId(), requestDto.getAmount(), requestDto.getAccountId());
        }*/
        return null;
    }


    /**
     * ?????????????????? ???????????? -?????????
     */
    public ResponseDto accountBonusTransferOut(BillRequestDto requestDto, String siteCode) {
        TGmApi gmApi = gettGmApi(requestDto, siteCode);
        BillRequestDto requestDto_zr = transferService.getBillRequestDto(requestDto);
        OprActBonus oprActBonus = getOprActBonus(requestDto);
        TransferAmoutDto amoutDto = new TransferAmoutDto();
        ResponseDto responseDto = new ResponseDto();
        if (nonNull(oprActBonus)) {
            Assert.isNull(requestDto.getCatId(), "??????ID????????????");
            requestDto.setBonusAmount(oprActBonus.getBonusAmount());
            requestDto.setAmount(requestDto.getAmount().add(oprActBonus.getBonusAmount()));
            MbrAccount mbrAccount = new MbrAccount();
            mbrAccount.setId(requestDto.getAccountId());
            requestDto.setDepotBeforeBalance(queryDepotBalance(
                    requestDto.getAccountId(), requestDto.getDepotId(), siteCode).getBalance());
            if (requestDto.getDepotBeforeBalance().compareTo(BigDecimal.ONE) == Constants.EVNumber.one ||
                    requestDto.getDepotBeforeBalance().compareTo(BigDecimal.ONE) == Constants.EVNumber.zero) {
                //TODO ????????????????????????????????????
                ResponseDto isTransferOut = accountTransferIn(requestDto_zr, siteCode);
                if (Boolean.FALSE.equals(isTransferOut)) {
                    throw new RRException(isTransferOut.getError());
                }
                amoutDto.setIsShow(Boolean.TRUE);
                amoutDto.setDepotName(gmApi.getDepotCode());
                amoutDto.setBounsAmount(oprActBonus.getBonusAmount());
                amoutDto.setTransferAmount(requestDto.getAmount());
                amoutDto.setAmount(BigDecimalMath.formatDownRounding(requestDto.getDepotBeforeBalance()));
            }
            //TODO ????????????????????????????????????
            BonusListDto bonusListDto = actActivityCastService.accountBonusOne(requestDto.getAccountId(), requestDto.getBonusId());
            if (bonusListDto.getWalletBalance().compareTo(requestDto.getAmount()
                    .subtract(oprActBonus.getBonusAmount())) == Constants.EVNumber.one
                    || bonusListDto.getWalletBalance().compareTo(requestDto.getAmount()
                    .subtract(oprActBonus.getBonusAmount())) == Constants.EVNumber.zero) {
                //TODO ??????????????????????????????
                BillManBooleanDto billManBooleanDto = new BillManBooleanDto();
                List<Integer> depotIds = walletService.getIntegersDepot();
                BigDecimal depositAmount = BigDecimal.ZERO;
                if (depotIds.contains(requestDto.getDepotId())) {
                    billManBooleanDto = transferService.getAllDepotTransferIn(requestDto, gmApi);
                    depositAmount = requestDto.getAmount().subtract(oprActBonus.getBonusAmount());
                } else {
                    requestDto.setAmount(requestDto.getAmount().subtract(oprActBonus.getBonusAmount()));
                    responseDto = transferNewService.accountTransferOut(requestDto, siteCode);
                    if (Boolean.FALSE.equals(responseDto.getIsSucceed())) {
                        throw new RRException("?????????????????????" + responseDto.getError());
                    }
                    billManBooleanDto.setIsTransfer(responseDto.getIsSucceed());
                    billManBooleanDto.setMbrBillManage(responseDto.getMbrBillManage());
                    depositAmount = requestDto.getAmount();
                }
                //TODO ????????????????????????
                if (Boolean.TRUE.equals(billManBooleanDto.getIsTransfer())) {
                    auditAccountService.accountUseBonus(oprActBonus, getMbrAccount(requestDto),
                            depositAmount, billManBooleanDto.getMbrBillManage().getId(),
                            requestDto.getDepotId(), requestDto.getCatId());
                }
                if ((Boolean.FALSE.equals(billManBooleanDto.getIsTransfer())
                        && billManBooleanDto.getMbrBillManage().getStatus() == Constants.EVNumber.zero)) {
                    auditAccountService.accountBonusFreeze(oprActBonus,
                            billManBooleanDto.getMbrBillManage().getId(), depositAmount);
                }
            } else {
                throw new RRException("?????????????????????");
            }
        } else {
            throw new RRException("?????????????????????????????????");
        }
        return responseDto;
    }

    /**
     * ????????????-????????? ???????????????
     */
    public BillManBooleanDto accountTransferOut(BillRequestDto requestDto, String siteCode) {
        BillManBooleanDto billManBooleanDto = new BillManBooleanDto();
        ResponseDto responseDto = transferNewService.accountTransferOut(requestDto, siteCode);
        billManBooleanDto.setMbrBillManage(responseDto.getMbrBillManage());
        billManBooleanDto.setIsTransfer(responseDto.getIsSucceed());
        return billManBooleanDto;
    }

    public GatewayResponseDto LoginOutGateway(TGmApi gmApi,String loginName){
        TransferModel transferModel = new TransferModel();
        transferModel.setTGmApi(gmApi);
        transferModel.setUserName(loginName);
        transferModel.setSiteCode(gmApi.getSiteCode());
        transferModel.setDepotName(gmApi.getDepotCode());
        MbrDepotWallet mbrDepotWallet = new MbrDepotWallet();
        mbrDepotWallet.setLoginName(loginName);
        mbrDepotWallet.setDepotId(gmApi.getDepotId());
        transferModel.setPassword(depotWalletMapper.selectOne(mbrDepotWallet).getPwd());

        GatewayResponseDto responseDto = gatewayDepotService.LoginOutGateway(transferModel);
        return responseDto;
    }

    /**
     * ?????????????????????
     *
     * @param requestDto
     * @return
     */
    private MbrAccount getMbrAccount(BillRequestDto requestDto) {
        MbrAccount account = new MbrAccount();
        account.setId(requestDto.getAccountId());
        account.setLoginName(requestDto.getLoginName());
        return account;
    }
}
