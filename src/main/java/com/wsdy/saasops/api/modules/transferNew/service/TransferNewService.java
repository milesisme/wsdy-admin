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

    public static String DEPOT_BALANCE = "平台余额小于1";

    public static List<String> noTransferDepotCodes = Lists.newArrayList("XM","SBOD","FBOB"); //单一钱包平台不需要转账

    /**
     * 中心钱包 -》平台
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
            String memo = "转入平台，返回信息未知";
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
            String memo = "转入平台失败，返回信息:" + JSON.toJSONString(responseDto);
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
            dto.setError("线路不存在");
        }
        if (isNull(requestDto.getAmount()) && !noTransferDepotCodes.contains(gmApi.getDepotCode())) {
            requestDto.setAmount(accountQueryBalance(requestDto.getAccountId()));
            if (requestDto.getAmount().compareTo(BigDecimal.ZERO) != 1) {
                dto.setIsSucceed(Boolean.FALSE);
                dto.setError("余额不足");
            }
        }
    }

    /**
     * 查询主账户余额
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
     * 查询主账户余额 全部包括小数不取整
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
     * 平台 -》中心钱包 服务
     */
    public ResponseDto accountTransferIn(BillRequestDto requestDto, String siteCode) {
        // 校验：余额是否冻结，是否有优先线路
        TGmApi gmApiDto = transferService.gettGmApi(requestDto, siteCode);

        ResponseDto dto = new ResponseDto();
        dto.setGmApi(gmApiDto);
        // 线路是否存在：此处逻辑与上面的校验重复，若不存在线路，上方校验已直接抛出异常
        checkoutTransferIn(requestDto, siteCode, dto);
        if (Boolean.FALSE.equals(dto.getIsSucceed())) {
            return dto;
        }
        // 此处创建钱包逻辑：没有创建钱包的，创建钱包；看调用：查询余额前提就是已创建钱包；
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
        // 先查询余额
        if (isNull(requestDto.getAmount())) {
            gatewayDto = gatewayDepotService.queryBalance(getLoginModel(gmApi, requestDto.getLoginName()));
            if (isNull(gatewayDto) || Boolean.FALSE.equals(gatewayDto.getCode()) || StringUtils.isEmpty(gatewayDto.getMessage())) {
                dto.setIsSucceed(Boolean.FALSE);
                dto.setError("查询余额失败：" + JSON.toJSONString(gatewayDto));
                return dto;
            }
        } else {
            gatewayDto.setCode(Boolean.TRUE);
            gatewayDto.setMessage(requestDto.getAmount().toString());
        }

        // 判断原平台余额是否大于等于1
        BigDecimal depotAmount = new BigDecimal(gatewayDto.getMessage());
        int isSgin = depotAmount.compareTo(BigDecimal.ONE);
        if (isSgin == Constants.EVNumber.one || isSgin == Constants.EVNumber.zero) {
            // 向下取整，仅回收整数金额
            requestDto.setAmount(BigDecimalMath.formatDownRounding(depotAmount));
            // 插入转账订单 mbr_bill_manage ： 状态冻结
            MbrBillManage billManage = ((TransferNewService) AopContext.currentProxy()).
                    addTransferInBillManage(requestDto, depotAmount);
            // 转账：平台->中心钱包
            ((TransferNewService) AopContext.currentProxy()).transferInDepot(billManage, gmApi);
        } else {
            dto.setError(DEPOT_BALANCE);        // 小于0不予回收
            dto.setIsSucceed(Boolean.FALSE);
        }
        return dto;
    }

    private void checkoutTransferIn(BillRequestDto requestDto, String siteCode, ResponseDto dto) {
        TGmApi gmApi = gmApiService.queryApiObject(requestDto.getDepotId(), siteCode);
        if (isNull(gmApi)) {
            dto.setIsSucceed(Boolean.FALSE);
            dto.setError("线路不存在");
        }
        dto.setGmApi(gmApi);
    }

    @Transactional
    public ResponseDto transferInDepot(MbrBillManage billManage, TGmApi gmApi) {
        ResponseDto response = new ResponseDto();
        GatewayResponseDto responseDto = gatewayDepotService.withdrawal(getTransferModel(billManage, gmApi));
        if (nonNull(responseDto) && !"604".equals(responseDto.getMsgCode())) {
            // 转账成功
            if (Boolean.TRUE.equals(responseDto.getStatus()) && "200".equals(responseDto.getMsgCode())) {
                // 上分，记明细
                MbrBillDetail mbrBillDetail = walletService.castWalletAndBillDetail(billManage.getLoginName(),
                        billManage.getAccountId(), OrderConstants.FUND_ORDER_TROUT, billManage.getAmount(),
                        billManage.getOrderNo(), Boolean.TRUE, null, null);

                billManage.setStatus(Constants.EVNumber.one);
                billManage.setAfterBalance(mbrBillDetail.getAfterBalance());
                billManage.setBeforeBalance(mbrBillDetail.getBeforeBalance());
            } else {
                String memo = "转出平台失败，返回信息:" + JSON.toJSONString(responseDto);
                billManage.setMemo(memo);
                billManage.setStatus(Constants.EVNumber.two);
            }
        } else {    // 604错误：未知错误
            billManage.setMemo("转出平台，返回信息未知");
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
                String memo = "创建用户失败，返回信息:" + JSON.toJSONString(responseDto);
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
