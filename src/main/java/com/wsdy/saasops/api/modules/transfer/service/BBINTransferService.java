package com.wsdy.saasops.api.modules.transfer.service;

import com.wsdy.saasops.api.modules.transfer.dto.*;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.config.MessagesConfig;
import com.wsdy.saasops.modules.member.dao.MbrBillDetailMapper;
import com.wsdy.saasops.modules.member.dao.MbrBillManageMapper;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import com.wsdy.saasops.modules.member.entity.MbrBillManage;
import com.wsdy.saasops.modules.member.entity.MbrWallet;
import com.wsdy.saasops.modules.member.service.MbrBillManageService;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import static com.wsdy.saasops.common.utils.DateUtil.*;

@Service
@Slf4j
public class BBINTransferService {

    @Autowired
    private MessagesConfig messagesConfig;
    @Autowired
    private MbrWalletService mbrWalletService;
    @Autowired
    private MbrBillManageMapper mbrBillManageMapper;
    @Autowired
    private MbrBillDetailMapper mbrBillDetailMapper;
    @Autowired
    private MbrBillManageService mbrBillManageService;

    public void transferBust(MbrBillManage manage) {
        //TODO 判断修改转帐预处理表(中间表)是否成功
        if (updateMbrBillManageStatus(manage, Constants.manageStatus.defeated) > 0) {
            MbrBillDetail mbrBillDetail = setMbrBillDetail(manage, MbrBillDetail.OpTypeStatus.income, Boolean.TRUE);
            walletIncome(mbrBillDetail);
        }
    }

    /**
     * @param mbrBillDetail
     * @param mbrBillManage 这个参数目前只使用到了 id
     * @param status
     */
    public void transferOutSuc(MbrBillDetail mbrBillDetail, MbrBillManage mbrBillManage, Integer status) {
        if (updateMbrBillManageStatus(mbrBillManage, status) > 0) {
            if (Constants.manageStatus.succeed.equals(status)) {
                walletIncome(mbrBillDetail);
                MbrBillManage billManage = new MbrBillManage();
                billManage.setId(mbrBillManage.getId());
                billManage.setBeforeBalance(mbrBillDetail.getBeforeBalance());
                billManage.setAfterBalance(mbrBillDetail.getAfterBalance());
                mbrBillManageMapper.updateByPrimaryKeySelective(billManage);
            }
        }
    }


    public int updateMbrBillManageStatus(MbrBillManage manage, Integer status) {
        MbrBillManage billManage = new MbrBillManage();
        billManage.setId(manage.getId());
        billManage.setStatus(status);
        billManage.setDepotAfterBalance(manage.getDepotAfterBalance());
        return mbrBillManageService.updateStatus(billManage);
    }

    public MbrBillManage setMbrBillManage(BillRequestDto requestDto) {
        MbrBillManage mbrBillManage = new MbrBillManage();
        mbrBillManage.setAccountId(requestDto.getAccountId());
        mbrBillManage.setDepotId(requestDto.getDepotId());
        mbrBillManage.setLoginName(requestDto.getLoginName());
        mbrBillManage.setAmount(requestDto.getAmount());
        mbrBillManage.setOrderNo(new SnowFlake().nextId() + "");
        mbrBillManage.setStatus(Constants.manageStatus.freeze);
        mbrBillManage.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        mbrBillManage.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        mbrBillManage.setModifyUser(requestDto.getLoginName());
        mbrBillManage.setLogId(requestDto.getId());
        return mbrBillManage;
    }

    private MbrBillDetail setMbrBillDetail(MbrBillManage manage, Byte opType, Boolean isMemo) {
        MbrBillDetail mbrBillDetail = new MbrBillDetail();
        mbrBillDetail.setOrderNo(manage.getOrderNo());
        mbrBillDetail.setLoginName(manage.getLoginName());
        mbrBillDetail.setAccountId(manage.getAccountId());
        mbrBillDetail.setAmount(manage.getAmount());
        mbrBillDetail.setAfterBalance(manage.getAfterBalance());
        mbrBillDetail.setBeforeBalance(manage.getBeforeBalance());
        mbrBillDetail.setBeforeBalance(manage.getBeforeBalance());
        mbrBillDetail.setOpType(new Byte(manage.getOpType().toString()));
        mbrBillDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
        mbrBillDetail.setDepotId(manage.getDepotId());
        mbrBillDetail.setFinancialCode(OrderConstants.FUND_ORDER_FHQB);
        mbrBillDetail.setOpType(opType);
        mbrBillDetail.setOrderPrefix(opType == 1 ? OrderConstants.FUND_ORDER_TRIN : OrderConstants.FUND_ORDER_TROUT);
        if (isMemo) {
            mbrBillDetail.setMemo(manage.getOrderNo().toString());
            mbrBillDetail.setOrderNo(new SnowFlake().nextId() + "");
        }
        return mbrBillDetail;
    }


    private void walletIncome(MbrBillDetail mbrBillDetail) {
        MbrWallet mbrWallet = new MbrWallet();
        mbrWallet.setBalance(mbrBillDetail.getAmount());
        mbrWallet.setAccountId(mbrBillDetail.getAccountId());
        //TODO 平台转钱包
        Boolean isSucceed = mbrWalletService.depotArriveWallet(mbrWallet, mbrBillDetail);
        if (isSucceed == false) {
            throw new R200Exception(messagesConfig.getValue("saasops.illegal.request"));
        }
        //TODO 添加流水详情
        mbrBillDetailMapper.insert(mbrBillDetail);
    }

    public MbrBillDetail setMbrBillDetail(MbrBillManage mbrBillManage) {
        MbrBillDetail mbrBillDetail = new MbrBillDetail();
        mbrBillDetail.setAmount(mbrBillManage.getAmount());
        mbrBillDetail.setOpType(MbrBillDetail.OpTypeStatus.income);
        mbrBillDetail.setLoginName(mbrBillManage.getLoginName());
        mbrBillDetail.setFinancialCode(OrderConstants.FUND_ORDER_FHQB);
        mbrBillDetail.setAccountId(mbrBillManage.getAccountId());
        mbrBillDetail.setDepotId(mbrBillManage.getDepotId());
        mbrBillDetail.setOrderPrefix(OrderConstants.FUND_ORDER_TRIN);
        mbrBillDetail.setOrderNo(mbrBillManage.getOrderNo());
        mbrBillDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
        return mbrBillDetail;
    }
}
