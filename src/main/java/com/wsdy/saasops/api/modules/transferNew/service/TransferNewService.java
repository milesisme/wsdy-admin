package com.wsdy.saasops.api.modules.transferNew.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.apisys.entity.TGmApi;
import com.wsdy.saasops.api.modules.apisys.service.TGmApiService;
import com.wsdy.saasops.api.modules.transfer.dto.BillRequestDto;
import com.wsdy.saasops.api.modules.transfer.service.TransferService;
import com.wsdy.saasops.api.modules.transferNew.dto.DepotBalanceDto;
import com.wsdy.saasops.api.modules.transferNew.dto.GatewayResponseDto;
import com.wsdy.saasops.api.modules.transferNew.dto.ResponseDto;
import com.wsdy.saasops.api.modules.unity.dto.LoginModel;
import com.wsdy.saasops.api.modules.unity.dto.RegisterModel;
import com.wsdy.saasops.api.modules.unity.dto.TransferModel;
import com.wsdy.saasops.api.modules.user.service.AginService;
import com.wsdy.saasops.api.modules.user.service.DepositCommonService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.utils.BigDecimalMath;
import com.wsdy.saasops.modules.member.dao.MbrBillManageMapper;
import com.wsdy.saasops.modules.member.dao.MbrDepotWalletMapper;
import com.wsdy.saasops.modules.member.dao.MbrWalletMapper;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import com.wsdy.saasops.modules.member.entity.MbrBillManage;
import com.wsdy.saasops.modules.member.entity.MbrDepotWallet;
import com.wsdy.saasops.modules.member.entity.MbrWallet;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import com.wsdy.saasops.modules.operate.service.TGmDepotService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@Slf4j
@Service
public class TransferNewService {

    @Autowired
    private AginService aginService;
    @Autowired
    private TGmApiService gmApiService;
    @Autowired
    private MbrWalletService walletService;
    @Autowired
    private TGmDepotService gmDepotService;
    @Autowired
    private TransferService transferService;
    @Autowired
    private MbrWalletMapper mbrWalletMapper;
    @Autowired
    private DepositCommonService commonService;
    @Autowired
    private MbrBillManageMapper billManageMapper;
    @Autowired
    private MbrDepotWalletMapper depotWalletMapper;
    @Autowired
    private GatewayDepotService gatewayDepotService;

    public static String DEPOT_BALANCE = "??????????????????1";

    public static List<String> noTransferDepotCodes = Lists.newArrayList("XM","SBOD","FBOB"); //?????????????????????????????????

    /**
     * ???????????? -?????????
     *
     * @param requestDto
     * @param siteCode
     * @return
     */
    public ResponseDto accountTransferOut(BillRequestDto requestDto, String siteCode) {
        ResponseDto dto = new ResponseDto();
        TGmApi gmApi = transferService.gettGmApi(requestDto, siteCode);
        dto.setGmApi(gmApi);
        checkoutTransferOut(requestDto, siteCode, dto);
        if (Boolean.FALSE.equals(dto.getIsSucceed())) {
            return dto;
        }

        ResponseDto responseDto = ((TransferNewService) AopContext.currentProxy()).createMember(
                requestDto.getAccountId(), requestDto.getDepotId(), requestDto.getLoginName(), dto.getGmApi());
        if (Boolean.FALSE.equals(responseDto.getIsSucceed())) {
            return responseDto;
        }
        if (noTransferDepotCodes.contains(gmApi.getDepotCode())) {
            return responseDto;
        }
        requestDto.setSumAmount(requestDto.getAmount().add(requestDto.getBonusAmount()));
        DepotBalanceDto balance = setDepotAfterBalance(dto.getGmApi(), requestDto.getLoginName(), requestDto.getSumAmount());
        MbrBillManage billManage = ((TransferNewService) AopContext.currentProxy()).transferSubtractMoney(requestDto, balance);
        if (isNull(billManage)) {
            return dto;
        }

        ResponseDto response = ((TransferNewService) AopContext.currentProxy()).
                transferOutDepot(billManage, responseDto.getDepotWallet(), dto.getGmApi());
        response.setMbrBillManage(billManage);
        return response;
    }

    @Transactional
    public MbrBillManage transferSubtractMoney(BillRequestDto requestDto, DepotBalanceDto balance) {
        requestDto.setPrefix("D");
        MbrBillManage mbrBillManage = commonService.setMbrBillManage(requestDto);
        MbrBillDetail mbrBillDetail = walletService.castTransferWalletSubtract(requestDto);
        if (nonNull(mbrBillDetail)) {
            mbrBillManage.setMemo(requestDto.getMemo());
            mbrBillManage.setBonusId(requestDto.getBonusId());
            mbrBillManage.setCatId(requestDto.getCatId());
            mbrBillManage.setAmount(requestDto.getSumAmount());
            mbrBillManage.setOpType(Constants.TransferType.out);
            mbrBillManage.setAfterBalance(mbrBillDetail.getAfterBalance());
            mbrBillManage.setBeforeBalance(mbrBillDetail.getBeforeBalance());
            mbrBillManage.setTransferSource(requestDto.getTransferSource());
            mbrBillManage.setDepotAfterBalance(balance.getDepotAfterBalance());
            mbrBillManage.setDepotBeforeBalance(balance.getDepotBeforeBalance());
            billManageMapper.insert(mbrBillManage);
            return mbrBillManage;
        }
        return null;
    }

    @Transactional
    public ResponseDto transferOutDepot(MbrBillManage billManage, MbrDepotWallet depotWallet, TGmApi gmApi) {
        ResponseDto response = new ResponseDto();
        GatewayResponseDto responseDto = gatewayDepotService.deposit(getTransferModel(billManage, gmApi));
        if (isNull(responseDto) || "604".equals(responseDto.getMsgCode())) {
            String memo = "?????????????????????????????????";
            if (nonNull(responseDto)) {
                memo = memo + responseDto.getMessage();
            }
            billManage.setMemo(memo);
            billManageMapper.updateByPrimaryKey(billManage);
            response.setIsSucceed(Boolean.FALSE);
            response.setError(memo);
            return response;
        }
        if (Boolean.TRUE.equals(responseDto.getStatus()) && "200".equals(responseDto.getMsgCode())) {
            if (depotWallet.getIsTransfer() == Constants.Available.disable) {
                depotWallet.setIsTransfer(Constants.Available.enable);
                depotWalletMapper.updateByPrimaryKey(depotWallet);
            }
            billManage.setStatus(Constants.EVNumber.one);
            billManageMapper.updateByPrimaryKey(billManage);
        } else {
            String memo = "?????????????????????????????????:" + JSON.toJSONString(responseDto);
            transferAddWalletMoney(billManage, memo);
            response.setIsSucceed(Boolean.FALSE);
            response.setError(memo);
        }
        return response;
    }

    @Transactional
    public void transferAddWalletMoney(MbrBillManage mbrBillManage, String memo) {
        walletService.castWalletAndBillDetail(mbrBillManage.getLoginName(),
                mbrBillManage.getAccountId(), OrderConstants.FUND_ORDER_FHQB, mbrBillManage.getAmount(),
                mbrBillManage.getOrderNo(), Boolean.TRUE, null, null);
        mbrBillManage.setStatus(Constants.EVNumber.two);
        mbrBillManage.setMemo(memo);
        billManageMapper.updateByPrimaryKey(mbrBillManage);
    }

    private DepotBalanceDto setDepotAfterBalance(TGmApi gmApi, String loginName, BigDecimal sumAmount) {
        GatewayResponseDto gatewayDto = gatewayDepotService.queryBalance(getLoginModel(gmApi, loginName));
        BigDecimal amount = BigDecimal.ZERO;
        if (nonNull(gatewayDto) && Boolean.TRUE.equals(gatewayDto.getCode()) && StringUtils.isNotEmpty(gatewayDto.getMessage())) {
            amount = new BigDecimal(gatewayDto.getMessage());
        }
        DepotBalanceDto balanceDto = new DepotBalanceDto();
        balanceDto.setDepotAfterBalance(amount.add(sumAmount));
        balanceDto.setDepotBeforeBalance(amount);
        return balanceDto;
    }

    private void checkoutTransferOut(BillRequestDto requestDto, String siteCode, ResponseDto dto) {
        TGmApi gmApi = gmApiService.queryApiObject(requestDto.getDepotId(), siteCode);
        if (isNull(gmApi)) {
            dto.setIsSucceed(Boolean.FALSE);
            dto.setError("???????????????");
        }
        if (isNull(requestDto.getAmount()) && !noTransferDepotCodes.contains(gmApi.getDepotCode())) {
            requestDto.setAmount(accountQueryBalance(requestDto.getAccountId()));
            if (requestDto.getAmount().compareTo(BigDecimal.ZERO) != 1) {
                dto.setIsSucceed(Boolean.FALSE);
                dto.setError("????????????");
            }
        }
    }

    /**
     * ?????????????????????
     *
     * @param accountId
     * @return
     */
    public BigDecimal accountQueryBalance(Integer accountId) {
        MbrWallet wallet = new MbrWallet();
        wallet.setAccountId(accountId);
        MbrWallet mbrWallet = mbrWalletMapper.selectOne(wallet);
        return BigDecimalMath.formatDownRounding(mbrWallet.getBalance());
    }

    /**
     * ????????????????????? ???????????????????????????
     *
     * @param accountId
     * @return
     */
    public BigDecimal accountQueryBalanceAll(Integer accountId) {
        MbrWallet wallet = new MbrWallet();
        wallet.setAccountId(accountId);
        MbrWallet mbrWallet = mbrWalletMapper.selectOne(wallet);
        return mbrWallet.getBalance();
    }

    /**
     * ?????? -??????????????? ??????
     */
    public ResponseDto accountTransferIn(BillRequestDto requestDto, String siteCode) {
        // ???????????????????????????????????????????????????
        TGmApi gmApiDto = transferService.gettGmApi(requestDto, siteCode);

        ResponseDto dto = new ResponseDto();
        dto.setGmApi(gmApiDto);
        // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        checkoutTransferIn(requestDto, siteCode, dto);
        if (Boolean.FALSE.equals(dto.getIsSucceed())) {
            return dto;
        }
        // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        TGmApi gmApi = dto.getGmApi();
        ResponseDto responseDto = ((TransferNewService) AopContext.currentProxy()).createMember(
                requestDto.getAccountId(), requestDto.getDepotId(), requestDto.getLoginName(), gmApi);
        if (Boolean.FALSE.equals(responseDto.getIsSucceed())) {
            return responseDto;
        }
        if (noTransferDepotCodes.contains(gmApi.getDepotCode())) {
            return responseDto;
        }

        GatewayResponseDto gatewayDto = new GatewayResponseDto();
        // ???????????????
        if (isNull(requestDto.getAmount())) {
            gatewayDto = gatewayDepotService.queryBalance(getLoginModel(gmApi, requestDto.getLoginName()));
            if (isNull(gatewayDto) || Boolean.FALSE.equals(gatewayDto.getCode()) || StringUtils.isEmpty(gatewayDto.getMessage())) {
                dto.setIsSucceed(Boolean.FALSE);
                dto.setError("?????????????????????" + JSON.toJSONString(gatewayDto));
                return dto;
            }
        } else {
            gatewayDto.setCode(Boolean.TRUE);
            gatewayDto.setMessage(requestDto.getAmount().toString());
        }

        // ???????????????????????????????????????1
        BigDecimal depotAmount = new BigDecimal(gatewayDto.getMessage());
        int isSgin = depotAmount.compareTo(BigDecimal.ONE);
        if (isSgin == Constants.EVNumber.one || isSgin == Constants.EVNumber.zero) {
            // ????????????????????????????????????
            requestDto.setAmount(BigDecimalMath.formatDownRounding(depotAmount));
            // ?????????????????? mbr_bill_manage ??? ????????????
            MbrBillManage billManage = ((TransferNewService) AopContext.currentProxy()).
                    addTransferInBillManage(requestDto, depotAmount);
            // ???????????????->????????????
            ((TransferNewService) AopContext.currentProxy()).transferInDepot(billManage, gmApi);
        } else {
            dto.setError(DEPOT_BALANCE);        // ??????0????????????
            dto.setIsSucceed(Boolean.FALSE);
        }
        return dto;
    }

    private void checkoutTransferIn(BillRequestDto requestDto, String siteCode, ResponseDto dto) {
        TGmApi gmApi = gmApiService.queryApiObject(requestDto.getDepotId(), siteCode);
        if (isNull(gmApi)) {
            dto.setIsSucceed(Boolean.FALSE);
            dto.setError("???????????????");
        }
        dto.setGmApi(gmApi);
    }

    @Transactional
    public ResponseDto transferInDepot(MbrBillManage billManage, TGmApi gmApi) {
        ResponseDto response = new ResponseDto();
        GatewayResponseDto responseDto = gatewayDepotService.withdrawal(getTransferModel(billManage, gmApi));
        if (nonNull(responseDto) && !"604".equals(responseDto.getMsgCode())) {
            // ????????????
            if (Boolean.TRUE.equals(responseDto.getStatus()) && "200".equals(responseDto.getMsgCode())) {
                // ??????????????????
                MbrBillDetail mbrBillDetail = walletService.castWalletAndBillDetail(billManage.getLoginName(),
                        billManage.getAccountId(), OrderConstants.FUND_ORDER_TROUT, billManage.getAmount(),
                        billManage.getOrderNo(), Boolean.TRUE, null, null);

                billManage.setStatus(Constants.EVNumber.one);
                billManage.setAfterBalance(mbrBillDetail.getAfterBalance());
                billManage.setBeforeBalance(mbrBillDetail.getBeforeBalance());
            } else {
                String memo = "?????????????????????????????????:" + JSON.toJSONString(responseDto);
                billManage.setMemo(memo);
                billManage.setStatus(Constants.EVNumber.two);
            }
        } else {    // 604?????????????????????
            billManage.setMemo("?????????????????????????????????");
            if (nonNull(responseDto)) {
                billManage.setMemo(billManage.getMemo() + responseDto.getMessage());
            }
        }
        billManageMapper.updateByPrimaryKey(billManage);
        return response;
    }

    @Transactional
    public MbrBillManage addTransferInBillManage(BillRequestDto requestDto, BigDecimal depotAmount) {
        requestDto.setPrefix("W");
        MbrBillManage mbrBillManage = commonService.setMbrBillManage(requestDto);
        mbrBillManage.setMemo(requestDto.getMemo());
        mbrBillManage.setOpType(Constants.TransferType.into);
        mbrBillManage.setTransferSource(requestDto.getTransferSource());
        mbrBillManage.setDepotBeforeBalance(depotAmount);
        mbrBillManage.setDepotAfterBalance(depotAmount.subtract(requestDto.getAmount()));
        billManageMapper.insert(mbrBillManage);
        return mbrBillManage;
    }


    @Transactional
    public ResponseDto createMember(Integer accountId, Integer depotId, String loginName, TGmApi gmApi) {
        ResponseDto response = new ResponseDto();
        MbrDepotWallet depotWallet = aginService.getDepotWallet(accountId, depotId, gmApi.getSiteCode(), loginName);
        if (Boolean.FALSE.equals(depotWallet.getIsBuild())) {
            RegisterModel registerModel = new RegisterModel();
            registerModel.setTGmApi(gmApi);
            registerModel.setSiteCode(gmApi.getSiteCode());
            registerModel.setDepotName(gmApi.getDepotCode());
            registerModel.setPassword(depotWallet.getPwd());
            registerModel.setDepotId(depotWallet.getDepotId());
            registerModel.setUserName(loginName);
            GatewayResponseDto responseDto = gatewayDepotService.createMember(registerModel);
            if (nonNull(responseDto) && Boolean.TRUE.equals(responseDto.getStatus())) {
                TGmDepot tGmDepot = gmDepotService.queryObject(depotId);
                depotWallet.setDepotName(tGmDepot.getDepotCode());
                depotWallet.setBalance(ApiConstants.DEAULT_ZERO_VALUE);
                depotWallet.setIsTransfer(MbrDepotWallet.IsTransFer.no);
                depotWallet.setIsLogin(Constants.Available.enable);
                depotWallet.setIsBuild(Boolean.TRUE);
                depotWalletMapper.insert(depotWallet);
                response.setDepotWallet(depotWallet);
            } else {
                String memo = "?????????????????????????????????:" + JSON.toJSONString(responseDto);
                response.setIsSucceed(Boolean.FALSE);
                response.setError(memo);
            }
        } else {
            response.setDepotWallet(depotWallet);
        }
        return response;
    }

    private TransferModel getTransferModel(MbrBillManage billManage, TGmApi gmApi) {
        TransferModel transferModel = new TransferModel();
        transferModel.setAmount(Double.valueOf(String.valueOf(billManage.getAmount())));
        transferModel.setDepotId(billManage.getDepotId());
        transferModel.setDepotName(gmApi.getDepotCode());
        transferModel.setOrderNo(billManage.getOrderNo());
        transferModel.setSiteCode(gmApi.getSiteCode());
        transferModel.setUserName(billManage.getLoginName());

        MbrDepotWallet mbrDepotWallet = new MbrDepotWallet();
        mbrDepotWallet.setLoginName(billManage.getLoginName());
        mbrDepotWallet.setDepotId(billManage.getDepotId());
        transferModel.setPassword(depotWalletMapper.selectOne(mbrDepotWallet).getPwd());
        transferModel.setTGmApi(gmApi);
        return transferModel;
    }

    public LoginModel getLoginModel(TGmApi gmApi, String loginName) {
        LoginModel loginModel = new LoginModel();
        loginModel.setSiteCode(gmApi.getSiteCode());
        loginModel.setUserName(loginName);
        loginModel.setTGmApi(gmApi);

        MbrDepotWallet mbrDepotWallet = new MbrDepotWallet();
        mbrDepotWallet.setLoginName(loginName);
        mbrDepotWallet.setDepotId(gmApi.getDepotId());
        loginModel.setPassword(depotWalletMapper.selectOne(mbrDepotWallet).getPwd());
        return loginModel;
    }
}
