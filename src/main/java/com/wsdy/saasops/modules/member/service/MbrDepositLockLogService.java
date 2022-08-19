package com.wsdy.saasops.modules.member.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.google.gson.JsonArray;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.listener.BizEventType;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrDepositLockLogMapper;
import com.wsdy.saasops.modules.member.dao.MbrFriendTransDetailMapper;
import com.wsdy.saasops.modules.member.dto.DepositAutoLockDto;
import com.wsdy.saasops.modules.member.dto.MbrFriendTransDetailDto;
import com.wsdy.saasops.modules.member.dto.MbrFriendTransDto;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import com.wsdy.saasops.modules.member.entity.MbrDepositLockLog;
import com.wsdy.saasops.modules.member.entity.MbrFriendTransDetail;
import com.wsdy.saasops.modules.member.mapper.AuditMapper;
import com.wsdy.saasops.modules.member.mapper.MbrFriendTransMapper;
import com.wsdy.saasops.modules.system.msgtemple.entity.MsgModel;
import com.wsdy.saasops.modules.system.msgtemple.service.MsgModelService;
import com.wsdy.saasops.modules.system.pay.entity.SetBacicOnlinepay;
import com.wsdy.saasops.modules.system.systemsetting.dao.SysSettingMapper;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Objects.nonNull;


@Service
public class MbrDepositLockLogService extends BaseService<MbrFriendTransDetailMapper, MbrFriendTransDetail> {

    @Autowired
    private MbrDepositLockLogMapper mbrDepositLockLogMapper;
    @Autowired
    private SysSettingService sysSettingService;
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
    private MbrAccountMapper accountMapper;
    @Autowired
    private MbrAccountService mbrAccountService;
    @Autowired
    private MbrAccountLogService accountLogService;
    @Autowired
    private MsgModelService msgModelService;

    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
    public boolean companyUnpayLock(MbrAccount account) {
        finishLockLog(account);
        // 如果配置的是全部或银行卡转卡，则判断是否在有效期内有达到数量的未支付的订单
        if (account.getDepositLock() == Constants.EVNumber.zero) {
            SysSetting sysSetting = sysSettingService.getSysSetting(SystemConstants.DEPOSIT_AUTO_LOCK);
            if (sysSetting != null) {
                List<DepositAutoLockDto> settingList = JSON.parseArray(sysSetting.getSysvalue(), DepositAutoLockDto.class);
                if (settingList == null || settingList.size() <= 0) {
                    return false;
                }
                for (DepositAutoLockDto setting : settingList) {
                    Integer unpayCount = 0;
                    String endTime = DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME);
                    String startTime = DateUtil.format(DateUtil.addHours(endTime, -1 * setting.getValidHours()),
                            DateUtil.FORMAT_18_DATE_TIME);
                    // 获取用户最后的一条锁定记录
                    MbrDepositLockLog lastLock = mbrDepositLockLogMapper.getLastLock(new MbrDepositLockLog(){{setAccountId(account.getId());}});
                    if (lastLock != null && StringUtils.isNotBlank(lastLock.getUnlockuser()) && StringUtils.isNotBlank(lastLock.getUnlocktime())
                            && DateUtil.parse(lastLock.getUnlocktime()).after(DateUtil.parse(startTime))) {
                        // 如果最后一笔已解锁并且时间在开始时间之后
                        startTime = lastLock.getUnlocktime();
                    }
                    if (setting.getDepositType() == 999) {
                        unpayCount = mbrDepositLockLogMapper.getAllUnpayOrder(account.getId(), startTime, endTime);
                    } else if (setting.getDepositType() == 14) {
                        unpayCount = mbrDepositLockLogMapper.getUnpayCompanyidOrder(account.getId(), startTime, endTime);
                    }
                    if (unpayCount >= setting.getNotPayTimes()) {
                        // 插入锁定记录
                        MbrDepositLockLog lockLog = new MbrDepositLockLog();
                        String lockTime = DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME);
                        Date unlockTime = DateUtil.addTime(setting.getLockTime() * 60);
                        lockLog.setLocktime(lockTime);
                        lockLog.setUnlocktime(DateUtil.format(unlockTime, DateUtil.FORMAT_18_DATE_TIME));
                        lockLog.setLockMinute(setting.getLockTime());
                        lockLog.setAccountId(account.getId());
                        lockLog.setLockType(Constants.EVNumber.zero);
                        lockLog.setLockUser("admin");
                        lockLog.setLockmemo("自动锁定");
                        mbrDepositLockLogMapper.insert(lockLog);

                        MbrAccount newAccount = new MbrAccount();
                        newAccount.setId(lockLog.getAccountId());
                        // 修改会员存款锁定状态
                        newAccount.setDepositLock(Constants.EVNumber.one);
                        accountMapper.updateByPrimaryKeySelective(newAccount);

                        // 插入日志
                        newAccount.setLoginName(account.getLoginName());
                        accountLogService.updateAccountRestDepositLock(newAccount);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
    public boolean onlinepayUnpayLock(MbrAccount account, Integer payId) {
        finishLockLog(account);
        // 如果配置的是全部或某种在线支付，则判断是否在有效期内有达到数量的未支付的订单
        if (account.getDepositLock() == Constants.EVNumber.zero) {
            SysSetting sysSetting = sysSettingService.getSysSetting(SystemConstants.DEPOSIT_AUTO_LOCK);
            if (sysSetting != null) {
                List<DepositAutoLockDto> settingList = JSON.parseArray(sysSetting.getSysvalue(), DepositAutoLockDto.class);
                if (settingList == null || settingList.size() <= 0) {
                    return false;
                }
                for (DepositAutoLockDto setting : settingList) {
                    Integer unpayCount = 0;
                    String endTime = DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME);
                    String startTime = DateUtil.format(DateUtil.addHours(endTime, -1 * setting.getValidHours()),
                            DateUtil.FORMAT_18_DATE_TIME);
                    // 获取用户最后的一条锁定记录
                    MbrDepositLockLog lastLock = mbrDepositLockLogMapper.getLastLock(new MbrDepositLockLog(){{setAccountId(account.getId());}});
                    if (lastLock != null && StringUtils.isNotBlank(lastLock.getUnlockuser()) && StringUtils.isNotBlank(lastLock.getUnlocktime())
                            && DateUtil.parse(lastLock.getUnlocktime()).after(DateUtil.parse(startTime))) {
                        // 如果最后一笔已解锁并且时间在开始时间之后
                        startTime = lastLock.getUnlocktime();
                    }
                    if (setting.getDepositType() == 999) {
                        unpayCount = mbrDepositLockLogMapper.getAllUnpayOrder(account.getId(), startTime, endTime);
                    } else if (setting.getDepositType() == payId) {
                        unpayCount = mbrDepositLockLogMapper.getUnpayOnlinepayidOrder(account.getId(),
                                startTime, endTime, Arrays.asList(payId));
                    }
                    if (unpayCount >= setting.getNotPayTimes()) {

                        // 插入锁定记录
                        MbrDepositLockLog lockLog = new MbrDepositLockLog();
                        String lockTime = DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME);
                        Date unlockTime = DateUtil.addTime(setting.getLockTime() * 60);
                        lockLog.setLocktime(lockTime);
                        lockLog.setUnlocktime(DateUtil.format(unlockTime, DateUtil.FORMAT_18_DATE_TIME));
                        lockLog.setLockMinute(setting.getLockTime());
                        lockLog.setAccountId(account.getId());
                        lockLog.setLockType(Constants.EVNumber.zero);
                        lockLog.setLockUser("admin");
                        lockLog.setLockmemo("自动锁定");
                        mbrDepositLockLogMapper.insert(lockLog);

                        MbrAccount newAccount = new MbrAccount();
                        newAccount.setId(lockLog.getAccountId());
                        // 修改会员存款锁定状态
                        newAccount.setDepositLock(Constants.EVNumber.one);
                        accountMapper.updateByPrimaryKeySelective(newAccount);

                        // 插入日志
                        newAccount.setLoginName(account.getLoginName());
                        accountLogService.updateAccountRestDepositLock(newAccount);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public PageUtils listPage(MbrDepositLockLog lockLog, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<MbrDepositLockLog> list = mbrDepositLockLogMapper.listPage(lockLog);
        PageUtils pageUtils = BeanUtil.toPagedResult(list);
        return pageUtils;
    }

    public PageUtils listDepositLockLog(Integer accountId, String startTime, String endTime,
                                        Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<MbrDepositLockLog> list = mbrDepositLockLogMapper.listDepositLockLog(accountId, startTime, endTime);
        PageUtils pageUtils = BeanUtil.toPagedResult(list);
        return pageUtils;
    }

    public List<MbrDepositLockLog> getLock(Integer accountId) {
        return mbrDepositLockLogMapper.getLock(accountId);
    }

    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public void finishLockLog(MbrAccount account) {
        // 判断当前是否有在锁定中，如没有则解锁会员
        List<MbrDepositLockLog> lockList = getLock(account.getId());
        if (lockList == null || lockList.size() <= 0) {
            MbrAccount newAccount = new MbrAccount();
            newAccount.setId(account.getId());
            newAccount.setDepositLock(Constants.EVNumber.zero);
            accountMapper.updateByPrimaryKeySelective(newAccount);

            account.setDepositLock(Constants.EVNumber.zero);

            List<MbrDepositLockLog> unfinish = mbrDepositLockLogMapper.getUnfinishLockLog(account.getId());
            for (MbrDepositLockLog lockLog : unfinish) {
                lockLog.setUnlockuser("admin");
                lockLog.setUnlockmemo("自动解锁");
                mbrDepositLockLogMapper.updateByPrimaryKeySelective(lockLog);
            }
        }

    }

    public MbrDepositLockLog getLastLock(MbrDepositLockLog lockLog) {
        return mbrDepositLockLogMapper.getLastLock(lockLog);
    }

    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public void depositUnlock(MbrDepositLockLog lockLog, String adminUser) {
        MbrAccount newAccount = new MbrAccount();
        newAccount.setId(lockLog.getAccountId());

        // 修改会员存款锁定状态
        if (nonNull(lockLog.getDepositLock())) {
            if (Integer.valueOf(Constants.EVNumber.zero).equals(lockLog.getDepositLock())) {   // 解锁
                // 锁定次数重置
                mbrAccountService.resetDepositLockNum(lockLog.getAccountId());
            }
            // 修改会员存款锁定状态
            newAccount.setDepositLock(lockLog.getDepositLock());
        }
        accountMapper.updateByPrimaryKeySelective(newAccount);

        // 插入日志
        MbrAccount account = mbrAccountService.getAccountInfo(lockLog.getAccountId());
        newAccount.setLoginName(account.getLoginName());
        accountLogService.updateAccountRestDepositLock(newAccount);

        // 记录解锁原因
        MbrDepositLockLog lastLock = mbrDepositLockLogMapper.getLastLock(lockLog);
        lastLock.setUnlockuser(adminUser);
        lastLock.setUnlocktime(DateUtil.format(new Date(), DateUtil.FORMAT_18_DATE_TIME));
        lastLock.setUnlockmemo(lockLog.getUnlockmemo());
        mbrDepositLockLogMapper.updateByPrimaryKeySelective(lastLock);
    }

    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public void depositLock(MbrDepositLockLog lockLog, String adminUser) {
        MbrAccount accountInfo = mbrAccountService.getAccountInfo(lockLog.getLoginName());
        if (accountInfo == null) {
            throw new R200Exception("您输入的用户名不存在...");
        }

        // 插入锁定记录
        String lockTime = DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME);
        Date unlockTime = DateUtil.addTime(lockLog.getLockMinute() * 60);
        lockLog.setLocktime(lockTime);
        lockLog.setUnlocktime(DateUtil.format(unlockTime, DateUtil.FORMAT_18_DATE_TIME));
        lockLog.setAccountId(accountInfo.getId());
        lockLog.setLockType(Constants.EVNumber.one);
        lockLog.setLockUser(adminUser);
        mbrDepositLockLogMapper.insert(lockLog);

        MbrAccount newAccount = new MbrAccount();
        newAccount.setId(lockLog.getAccountId());
        // 修改会员存款锁定状态
        newAccount.setDepositLock(Constants.EVNumber.one);
        accountMapper.updateByPrimaryKeySelective(newAccount);

        // 插入日志
        MbrAccount account = mbrAccountService.getAccountInfo(lockLog.getAccountId());
        newAccount.setLoginName(account.getLoginName());
        accountLogService.updateAccountRestDepositLock(newAccount);

        // 发送站内信
        if (lockLog.getSend() != null && lockLog.getSend() == Constants.EVNumber.one) {
            // 强制提出使用的是非模板信息，可以在此处使用
            BizEvent bizEvent = new BizEvent(this, CommonUtil.getSiteCode(), null, null);
            bizEvent.setEventType(BizEventType.FORCE_LOGOUT);
            MsgModel model = new MsgModel();
            model.setInMail(lockLog.getMessage());
            msgModelService.sendInMailForDepositLock(account, model, bizEvent);
        }
    }

}
