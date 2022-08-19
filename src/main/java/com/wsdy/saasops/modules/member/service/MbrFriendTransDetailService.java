package com.wsdy.saasops.modules.member.service;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.listener.BizEventType;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.fund.dao.AccWithdrawMapper;
import com.wsdy.saasops.modules.fund.dao.FundDepositMapper;
import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import com.wsdy.saasops.modules.member.dto.MbrFriendTransDetailDto;
import com.wsdy.saasops.modules.member.dto.MbrFriendTransDto;
import com.wsdy.saasops.modules.member.mapper.AuditMapper;
import com.wsdy.saasops.modules.member.mapper.MbrFriendTransMapper;
import com.wsdy.saasops.modules.system.systemsetting.dao.SysSettingMapper;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wsdy.saasops.modules.member.dao.MbrFriendTransDetailMapper;
import com.wsdy.saasops.modules.member.entity.MbrFriendTransDetail;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class MbrFriendTransDetailService extends BaseService<MbrFriendTransDetailMapper, MbrFriendTransDetail> {

    @Autowired
    private MbrFriendTransMapper mbrFriendTransMapper;
    @Autowired
    private SysSettingMapper sysSettingMapper;
    @Autowired
    private MbrWalletService mbrWalletService;
    @Autowired
    private MbrFriendTransDetailMapper mbrFriendTransDetailMapper;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private AuditMapper auditMapper;
    @Autowired
    private FundDepositMapper fundDepositMapper;
    @Autowired
    private AccWithdrawMapper withdrawMapper;
    @Autowired
    private MbrAccountService mbrAccountService;

    public PageUtils queryListPageForUser(MbrFriendTransDetailDto mbrFreindTransDetail, Integer pageNo, Integer pageSize) {
        log.info("用户{}获取转账记录参数{}", mbrFreindTransDetail.getLoginName(), JSON.toJSONString(mbrFreindTransDetail));
        PageHelper.startPage(pageNo, pageSize);
        if (StringUtils.isEmpty(mbrFreindTransDetail.getStartTime()) || StringUtils.isEmpty(mbrFreindTransDetail.getEndTime())) {
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.FORMAT_18_DATE_TIME);
            Calendar rightNow = Calendar.getInstance();
            rightNow.setTime(new Date());
            rightNow.add(Calendar.DAY_OF_YEAR, -7);
            Date dt1 = rightNow.getTime();
            String startTime = sdf.format(dt1);
            String endTime = sdf.format(new Date());
            mbrFreindTransDetail.setStartTime(startTime);
            mbrFreindTransDetail.setEndTime(endTime);
        }
        PageUtils pageUtils = BeanUtil.toPagedResult(mbrFriendTransMapper.findMbrFriendsTransList(mbrFreindTransDetail));
        List<MbrFriendTransDetail> list = (List<MbrFriendTransDetail>) pageUtils.getList();
        for (MbrFriendTransDetail detail : list) {
            if (detail.getTransLoginName().equalsIgnoreCase(mbrFreindTransDetail.getLoginName())) {
                detail.setTransType(Constants.EVNumber.two);
            }
            if (detail.getReceiptLoginName().equalsIgnoreCase(mbrFreindTransDetail.getLoginName())) {
                detail.setTransType(Constants.EVNumber.one);
            }
            detail.setStatus(Constants.EVNumber.one);
        }
        pageUtils.setList(list);
        return pageUtils;

    }

    public MbrFriendTransDetail queryTodayCount(MbrFriendTransDetailDto mbrFreindTransDetail) {
        mbrFreindTransDetail.setStartTime(DateUtil.getCurrentDate(DateUtil.FORMAT_10_DATE).concat(" 00:00:00"));
        mbrFreindTransDetail.setEndTime(DateUtil.getCurrentDate(DateUtil.FORMAT_10_DATE).concat(" 23:59:59"));

        MbrFriendTransDetail result = mbrFriendTransMapper.findTodayCount(mbrFreindTransDetail);

        return result;

    }

    public PageUtils queryListPage(MbrFriendTransDetailDto mbrFreindTransDetail, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        if (StringUtils.isEmpty(mbrFreindTransDetail.getStartTime()) || StringUtils.isEmpty(mbrFreindTransDetail.getEndTime())) {
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.FORMAT_18_DATE_TIME);
            Calendar rightNow = Calendar.getInstance();
            rightNow.setTime(new Date());
            rightNow.add(Calendar.DAY_OF_YEAR, -7);
            Date dt1 = rightNow.getTime();
            String startTime = sdf.format(dt1);
            String endTime = sdf.format(new Date());
            mbrFreindTransDetail.setStartTime(startTime);
            mbrFreindTransDetail.setEndTime(endTime);
        }
        PageUtils pageUtils = BeanUtil.toPagedResult(mbrFriendTransMapper.findMbrFriendsTransList(mbrFreindTransDetail));
        List<MbrFriendTransDetail> list = (List<MbrFriendTransDetail>) pageUtils.getList();
        List<MbrFriendTransDetail> transList = new ArrayList<>();
        if (Objects.nonNull(mbrFreindTransDetail.getType()) && mbrFreindTransDetail.getType() == 1) {
            for (MbrFriendTransDetail mftd : list) {
                mftd.setAmount("+" + mftd.getTransAmount());
                transList.add(mftd);
            }
            pageUtils.setList(transList);
        } else if (Objects.nonNull(mbrFreindTransDetail.getType()) && mbrFreindTransDetail.getType() == 2) {
            for (MbrFriendTransDetail mftd : list) {
                mftd.setAmount("-" + mftd.getTransAmount());
                transList.add(mftd);
            }
            pageUtils.setList(transList);
        }
        return pageUtils;

    }

    public PageUtils findMbrFriendsList(Integer accoutId, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        return BeanUtil.toPagedResult(mbrFriendTransMapper.findMbrFriendsList(accoutId));
    }

    public boolean checkFriendsTransInfo(MbrFriendTransDto mbrFriendTransDto) {
        List<SysSetting> settingList = sysSettingMapper.selectAll();
        if (CollectionUtils.isNotEmpty(settingList)) {
            settingList.stream().forEach(sysSetting -> {
                if (SystemConstants.FRIEND_TRANS_AUTOMATIC.equals(sysSetting.getSyskey())) {
                    String value = sysSetting.getSysvalue();
                    if (!"1".equals(value)) {
                        throw new R200Exception("系统好友转账功能已经禁用!");
                    }
                }

            });
        }

        int count = findAuditAccountBounsCount(mbrFriendTransDto.getTransAccountId());
        if (count > 0) {
            throw new R200Exception("请先满足优惠流水，方可转账");
        }

        MbrAccount account = mbrAccountService.getAccountInfo(mbrFriendTransDto.getTransLoginName());
        if (account.getAvailable() == Constants.EVNumber.two) {
            throw new R200Exception("您的账号余额被冻结，暂无法转账");
        }

        boolean dAndw = checkDepositAndWithdraw(mbrFriendTransDto);
        if (!dAndw) {
            throw new R200Exception("必须进行一次充值和取款后，方可转账");
        }

        return true;
    }

    public boolean checkDepositAndWithdraw(MbrFriendTransDto mbrFriendTransDto) {
        FundDeposit deposit = new FundDeposit();
        deposit.setAccountId(mbrFriendTransDto.getTransAccountId());
        deposit.setStatus(Constants.EVNumber.one);
        int depositCount = fundDepositMapper.selectCount(deposit);
        if (depositCount <= 0) {
            return false;
        }
        AccWithdraw withdraw = new AccWithdraw();
        withdraw.setAccountId(mbrFriendTransDto.getTransAccountId());
        withdraw.setStatus(Constants.EVNumber.one);
        int withdrawCount = withdrawMapper.selectCount(withdraw);
        if (withdrawCount <= 0) {
            return false;
        }
        return true;
    }

    @Transactional
    public boolean saveFriendsTransInfo(MbrFriendTransDto mbrFriendTransDto, String siteCode, Byte pcSource) {
        List<SysSetting> settingList = sysSettingMapper.selectAll();
        if (CollectionUtils.isNotEmpty(settingList)) {
            settingList.stream().forEach(sysSetting -> {
                if (SystemConstants.FRIEND_TRANS_AUTOMATIC.equals(sysSetting.getSyskey())) {
                    String value = sysSetting.getSysvalue();
                    if (!"1".equals(value)) {
                        throw new R200Exception("系统好友转账功能已经禁用!");
                    }
                }
                /*if (SystemConstants.FRIEND_TRANS_MAX_AMOUNT.equals(sysSetting.getSyskey())) {
                    BigDecimal bd = new BigDecimal(sysSetting.getSysvalue());
                    if (mbrFriendTransDto.getTransAmount() != null && bd != null && !bd.equals(BigDecimal.ZERO)) {
                        BigDecimal decimal = mbrFriendTransDto.getTransAmount();
                        int flag = bd.compareTo(decimal);
                        if (flag != 1) {
                            throw new R200Exception("转账金额大于系统设置最大限定额!");
                        }
                    }
                }*/
            });
        }

        int count = findAuditAccountBounsCount(mbrFriendTransDto.getTransAccountId());
        if (count > 0) {
            throw new R200Exception("请先满足优惠流水，方可转账");
        }
        MbrAccount account = mbrAccountService.getAccountInfo(mbrFriendTransDto.getTransLoginName());
        if (account.getAvailable() == Constants.EVNumber.two) {
            throw new R200Exception("您的账号余额被冻结，暂无法转账");
        }
        MbrAccount receiptAccount = mbrAccountService.getAccountInfo(mbrFriendTransDto.getReceiptLoginName());
        if (receiptAccount == null) {
            throw new R200Exception("您输入的转账账号不正确，无法转账");
        }
        boolean dAndw = checkDepositAndWithdraw(mbrFriendTransDto);
        if (!dAndw) {
            throw new R200Exception("必须进行一次充值和取款后，方可转账");
        }

        auditAccountService.updateAuditAccount(mbrFriendTransDto.getTransAccountId(), mbrFriendTransDto.getTransAmount(), siteCode);
        auditAccountService.insertAccountAudit(
                mbrFriendTransDto.getReceiptAccountId(),
                mbrFriendTransDto.getTransAmount(),
                null, null, null,
                null, null, Constants.EVNumber.one);

        MbrBillDetail transMbrBillDetail = mbrWalletService.castWalletAndBillDetail(
                mbrFriendTransDto.getTransLoginName(),
                mbrFriendTransDto.getTransAccountId(), OrderConstants.FRIENDTRANS_FT,
                mbrFriendTransDto.getTransAmount(), null, false,null,null);

        if (transMbrBillDetail == null) {
            throw new R200Exception("好友转账异常,请确认账户余额!");
        }
        MbrBillDetail receiptMbrBillDetail = mbrWalletService.castWalletAndBillDetail(
                mbrFriendTransDto.getReceiptLoginName(),
                mbrFriendTransDto.getReceiptAccountId(), OrderConstants.FRIENDTRANS_FT,
                mbrFriendTransDto.getTransAmount(), null, true,null,null);

        MbrFriendTransDetail mbrFreindTransDetail = new MbrFriendTransDetail();
        mbrFreindTransDetail.setReceiptAccountId(mbrFriendTransDto.getReceiptAccountId());
        mbrFreindTransDetail.setReceiptLoginName(receiptAccount.getLoginName());
        mbrFreindTransDetail.setTransAccountId(mbrFriendTransDto.getTransAccountId());
        mbrFreindTransDetail.setTransLoginName(mbrFriendTransDto.getTransLoginName());
        mbrFreindTransDetail.setTransAmount(mbrFriendTransDto.getTransAmount());
        mbrFreindTransDetail.setTransBeforeBalance(transMbrBillDetail.getBeforeBalance());
        mbrFreindTransDetail.setMbrBillDetailTransId(transMbrBillDetail.getId());
        mbrFreindTransDetail.setMbrBillDetailReceipId(receiptMbrBillDetail.getId());
        mbrFreindTransDetail.setTransAfterBalance(transMbrBillDetail.getAfterBalance());
        mbrFreindTransDetail.setReceiptBeforeBalance(receiptMbrBillDetail.getBeforeBalance());
        mbrFreindTransDetail.setTransferSource(pcSource);
        mbrFreindTransDetail.setReceiptAfterBalance(receiptMbrBillDetail.getAfterBalance());
        mbrFreindTransDetail.setCreateTime(DateUtil.format(new Date(), DateUtil.FORMAT_18_DATE_TIME));
        mbrFriendTransDetailMapper.insert(mbrFreindTransDetail);
        friendsTransMsg(mbrFriendTransDto, siteCode);
        return true;
    }

    public MbrFriendTransDetail findFriendsTransOneInfo(Integer id) {
        return mbrFriendTransMapper.findFriendsTransOneInfo(id);
    }

    @Async
    public void friendsTransMsg(MbrFriendTransDto mbrFriendTransDto, String siteCode) {
        BizEvent bizEvent = new BizEvent(this, siteCode, mbrFriendTransDto.getReceiptAccountId(),
                BizEventType.FRIEND_TRANS_FT);
        bizEvent.setLoginName(mbrFriendTransDto.getTransLoginName());
        bizEvent.setTransAmount(mbrFriendTransDto.getTransAmount());
        applicationEventPublisher.publishEvent(bizEvent);
    }

    public int findAuditAccountBounsCount(Integer accountId) {
        return auditMapper.findAuditAccountBounsCount(accountId);
    }
}
