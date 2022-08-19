package com.wsdy.saasops.api.modules.transfer.service;

import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.constants.ApiConstants.TransferStates;
import com.wsdy.saasops.api.modules.apisys.entity.TGmApi;
import com.wsdy.saasops.api.modules.apisys.service.TGmApiService;
import com.wsdy.saasops.api.modules.transfer.dto.*;
import com.wsdy.saasops.api.modules.transferNew.dto.GatewayResponseDto;
import com.wsdy.saasops.api.modules.transferNew.service.GatewayDepotService;
import com.wsdy.saasops.api.modules.unity.dto.TransferModel;
import com.wsdy.saasops.api.modules.user.service.*;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.BigDecimalMath;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.member.dao.MbrBillManageMapper;
import com.wsdy.saasops.modules.member.dto.AuditBonusDto;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import com.wsdy.saasops.modules.member.entity.MbrBillManage;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.member.service.MbrBillManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.wsdy.saasops.api.modules.transferNew.service.TransferNewService.DEPOT_BALANCE;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

@Slf4j
@Service
@Transactional
public class TransferService {

    @Autowired
    private BBINTransferService bbinTransferService;
    @Autowired
    private TGmApiService gmApiService;
    @Autowired
    private GatewayDepotService gatewayDepotService;
    @Autowired
    private MbrBillManageService mbrBillManageService;
    @Autowired
    private MbrAccountService userService;
    @Autowired
    private DepotWalletService depotWalletService;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;
    @Autowired
    private MbrBillManageMapper billManageMapper;

    private String[] depotCode = new String[]{"XM"}; //需要全部带出金额的平台
    /**
     * 会员信息的操作
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

    /**
     * 转入前的操作
     *
     * @param requestDto
     * @param sitePrefix
     * @return
     */
    public TGmApi gettGmApi(BillRequestDto requestDto, String sitePrefix) {
        MbrAccount user = userService.queryObject(requestDto.getAccountId(), sitePrefix);
        if (user.getAvailable() == MbrAccount.Status.LOCKED) {
            throw new R200Exception("账号余额已冻结,不能转账!");
        }
        TGmApi gmApi = gmApiService.queryApiObject(requestDto.getDepotId(), sitePrefix);
        if (gmApi == null) {
            throw new R200Exception("对不起，暂无此游戏线路");
        }
        return gmApi;
    }


    public BillRequestDto getBillRequestDto(BillRequestDto requestDto) {
        BillRequestDto requestDto1 = new BillRequestDto();
        requestDto1.setAmount(requestDto.getAmount());
        requestDto1.setDepotId(requestDto.getDepotId());
        requestDto1.setDepotBeforeBalance(requestDto.getDepotBeforeBalance());
        requestDto1.setCatId(requestDto.getCatId());
        requestDto1.setOpType(requestDto.getOpType());
        requestDto1.setOrderNo(requestDto.getOrderNo());
        requestDto1.setIp(requestDto.getIp());
        requestDto1.setId(requestDto.getId());
        requestDto1.setAccountId(requestDto.getAccountId());
        requestDto1.setTransferSource(requestDto.getTransferSource());
        requestDto1.setTerminal(requestDto.getTerminal());
        requestDto1.setDepotName(requestDto.getDepotName());
        requestDto1.setBonusId(requestDto.getBonusId());
        requestDto1.setMemo(requestDto.getMemo());
        requestDto1.setGameId(requestDto.getGameId());
        requestDto1.setLoginName(requestDto.getLoginName());
        return requestDto1;
    }

    /**
     * 转出前操作
     *
     * @param requestDto
     * @param siteCode
     * @return
     */
    public TGmApi getAllGmApi(BillRequestDto requestDto, String siteCode) {
        TGmApi gmApi = gmApiService.queryApiObject(requestDto.getDepotId(), siteCode);
        if (gmApi == null) {
            throw new R200Exception("对不起，暂无此游戏线路");
        }
        return gmApi;
    }

    /**
     * 转出（公）
     *
     * @param requestDto
     * @param siteCode
     * @return
     */
    public AuditBonusDto TransferOut(BillRequestDto requestDto, String siteCode) {
        Boolean isTransfer = false;
        // 校验：有无改平台线路
        TGmApi gmApi = getAllGmApi(requestDto, siteCode);
        AuditBonusDto auditBonusDto = new AuditBonusDto();
        // 转出判断优惠是否通过违规优惠稽核
        if (Boolean.TRUE.equals(requestDto.getIsTransferBouns())) {
            // auditBonusDto 中的默认值为true, 所以以下执行与否无意义；
            auditBonusDto = auditAccountService.outAuditBonus(getMbrAccount(requestDto), requestDto.getDepotId());
        }
        // 没有违规且通过则进行平台->中心钱包转账：必定执行此if，auditBonusDto 中的默认值为true
        if (Boolean.TRUE.equals(auditBonusDto.getIsFraud()) && Boolean.TRUE.equals(auditBonusDto.getIsSucceed())) {
            // 先查询转账前平台原余额
            requestDto.setDepotBeforeBalance(depotWalletService.queryDepotBalance(requestDto.getAccountId(), gmApi).getBalance());
            // 判断原平台余额是否大于等于1
            int isSgin = requestDto.getDepotBeforeBalance().compareTo(BigDecimal.ONE);
            if (isSgin == Constants.EVNumber.one || isSgin == Constants.EVNumber.zero) {
                if (!Arrays.asList(depotCode).contains(gmApi.getDepotCode())){
                    // 向下取整，仅回收整数金额
                    requestDto.setDepotBeforeBalance(BigDecimalMath.formatDownRounding(requestDto.getDepotBeforeBalance()));
                }
                // 转账 ： 旧的转账接口
                isTransfer = getAllDepotTransferOut(requestDto, gmApi);
            } else {
                throw new RRException(DEPOT_BALANCE);   // 错误提示：平台余额小于1
            }
        }
        if (Boolean.TRUE.equals(isTransfer)) {
            auditAccountService.succeedAuditBonus(getMbrAccount(requestDto), requestDto.getDepotId());
        }
        return auditBonusDto;
    }


    public R checkTransfer(Long orderNo, String siteCode) {
        String key = RedisConstants.ACCOUNT_CHECK_TRANSFER + siteCode + orderNo;
        Integer state = TransferStates.progress;
        try {
            Boolean isExpired = redisService.setRedisExpiredTimeBo(key, String.valueOf(orderNo), 200, TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(isExpired)) {
                throw new R200Exception("正在处理中,请稍等");
            }
            MbrBillManage mbrBillManage = mbrBillManageService.queryOrderNo(orderNo);
            if (Objects.isNull(mbrBillManage)) {
                throw new R200Exception("无此订单,请确认订单号是否正确,或此订单已更新!");
            }
            TGmApi gmApi = gmApiService.queryApiObject(mbrBillManage.getDepotId(), siteCode);
            if (Objects.isNull(gmApi)) {
                throw new R200Exception("对不起，暂无此游戏线路");
            }
            TransferModel transferModel = new TransferModel();
            transferModel.setTGmApi(gmApi);
            transferModel.setSiteCode(siteCode);
            transferModel.setUserName(mbrBillManage.getLoginName());
            transferModel.setOrderNo(mbrBillManage.getOrderNo());
            transferModel.setAmount(mbrBillManage.getAmount().doubleValue());
            GatewayResponseDto responseDto = gatewayDepotService.checkTransfer(transferModel);
            if (Objects.isNull(responseDto) || "604".equals(responseDto.getMsgCode())) {
                throw new R200Exception("第三方正在处理中,请稍等", 604);
            }
            if ("200".equals(responseDto.getMsgCode())) {
                if (mbrBillManage.getOpType().equals(Constants.TransferType.out)) {
                    bbinTransferService.updateMbrBillManageStatus(mbrBillManage, Constants.manageStatus.succeed);
                } else if (mbrBillManage.getOpType().equals(Constants.TransferType.into)) {
                    MbrBillDetail mbrBillDetail = bbinTransferService.setMbrBillDetail(mbrBillManage);
                    mbrBillManage.setBeforeBalance(mbrBillDetail.getBeforeBalance());
                    mbrBillManage.setAfterBalance(mbrBillDetail.getAfterBalance());
                    bbinTransferService.transferOutSuc(mbrBillDetail, mbrBillManage, Constants.manageStatus.succeed);
                }
                state = Constants.EVNumber.one;
                mbrAccountLogService.checkTransfer(orderNo, "成功");
            } else if ("201".equals(responseDto.getMsgCode())) {
                if (mbrBillManage.getOpType().equals(Constants.TransferType.out)) {
                    bbinTransferService.transferBust(mbrBillManage);
                } else if (mbrBillManage.getOpType().equals(Constants.TransferType.into)) {
                    bbinTransferService.updateMbrBillManageStatus(mbrBillManage, Constants.manageStatus.defeated);
                }
                state = Constants.EVNumber.two;
                mbrAccountLogService.checkTransfer(orderNo, "失败");
            }
            if (mbrBillManage.getStatus() == Constants.EVNumber.one || mbrBillManage.getStatus() == Constants.EVNumber.two) {
                auditAccountService.accountBonusFreeze(mbrBillManage);
            }
        } finally {
            redisService.del(key);
        }
        return R.ok().put("state", state);
    }

    public BillManBooleanDto getAllDepotTransferIn(BillRequestDto requestDto, TGmApi gmApi) {
        BillManBooleanDto billManBooleanDto = new BillManBooleanDto();
        switch (gmApi.getDepotCode()) {
            case ApiConstants.DepotCode.BBIN:
            default:
                return null;
        }
    }

    public Boolean getAllDepotTransferOut(BillRequestDto requestDto, TGmApi gmApi) {
        Boolean isTransfer;
        switch (gmApi.getDepotCode()) {
            default:
                isTransfer = null;
                break;
        }
        return isTransfer;
    }

    public void updateManageStatus(MbrBillManage billManage, String username) {
        MbrBillManage mbrBillManage = billManageMapper.selectByPrimaryKey(billManage.getId());
        if (Objects.isNull(mbrBillManage)) {
            throw new R200Exception("无此订单");
        }
        if (mbrBillManage.getStatus() != 0) {
            throw new R200Exception("订单状态已经更新");
        }
        String key = RedisConstants.ACCOUNT_UPDATE_TRANSFER + CommonUtil.getSiteCode() + mbrBillManage.getId();
        try {
            Boolean isExpired = redisService.setRedisExpiredTimeBo(key, mbrBillManage.getAccountId(), 200, TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(isExpired)) {
                throw new R200Exception("正在处理中,请稍等");
            }
            mbrBillManage.setUsername(username);
            mbrBillManage.setOperatingTime(getCurrentDate(FORMAT_18_DATE_TIME));
            billManageMapper.updateByPrimaryKeySelective(mbrBillManage);
            if (billManage.getStatus() == 1) {
                if (mbrBillManage.getOpType().equals(Constants.TransferType.out)) {
                    bbinTransferService.updateMbrBillManageStatus(mbrBillManage, Constants.manageStatus.succeed);
                } else if (mbrBillManage.getOpType().equals(Constants.TransferType.into)) {
                    MbrBillDetail mbrBillDetail = bbinTransferService.setMbrBillDetail(mbrBillManage);
                    mbrBillManage.setBeforeBalance(mbrBillDetail.getBeforeBalance());
                    mbrBillManage.setAfterBalance(mbrBillDetail.getAfterBalance());
                    bbinTransferService.transferOutSuc(mbrBillDetail, mbrBillManage, Constants.manageStatus.succeed);
                }
                mbrAccountLogService.updateManageStatusLog(mbrBillManage, "成功");
            }
            if (billManage.getStatus() == 2) {
                if (mbrBillManage.getOpType().equals(Constants.TransferType.out)) {
                    bbinTransferService.transferBust(mbrBillManage);
                } else if (mbrBillManage.getOpType().equals(Constants.TransferType.into)) {
                    bbinTransferService.updateMbrBillManageStatus(mbrBillManage, Constants.manageStatus.defeated);
                }
                mbrAccountLogService.updateManageStatusLog(mbrBillManage, "失败");
            }
        } finally {
            redisService.del(key);
        }
    }

}
