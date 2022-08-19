package com.wsdy.saasops.modules.member.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.api.modules.transfer.dto.BillRequestDto;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.dto.SettingAgentDto;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgyDomain;
import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.wsdy.saasops.modules.fund.entity.FundAudit;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.fund.entity.FundMerchantPay;
import com.wsdy.saasops.modules.mbrRebateAgent.dto.MbrRebateAgentAuditDto;
import com.wsdy.saasops.modules.mbrRebateAgent.entity.MbrRebateAgentBonus;
import com.wsdy.saasops.modules.member.dao.MbrAccountLogMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrActivityLevelMapper;
import com.wsdy.saasops.modules.member.dao.MbrGroupMapper;
import com.wsdy.saasops.modules.member.dto.AccountLogDto;
import com.wsdy.saasops.modules.member.dto.WarningLogDto;
import com.wsdy.saasops.modules.member.entity.*;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.operate.dto.AddFriendRebateRewardDto;
import com.wsdy.saasops.modules.operate.dto.ReduceFriendRebateRewardDto;
import com.wsdy.saasops.modules.operate.entity.*;
import com.wsdy.saasops.modules.sys.dao.SysWarningMapper;
import com.wsdy.saasops.modules.sys.entity.SysRoleEntity;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import com.wsdy.saasops.modules.sys.entity.SysUserMbrgrouprelation;
import com.wsdy.saasops.modules.sys.entity.SysWarning;
import com.wsdy.saasops.modules.system.msgtemple.entity.MsgModel;
import com.wsdy.saasops.modules.system.pay.entity.*;
import com.wsdy.saasops.modules.system.systemsetting.dto.*;
import com.wsdy.saasops.modules.system.systemsetting.entity.SmsConfig;
import com.wsdy.saasops.modules.task.entity.TaskConfig;
import com.wsdy.saasops.modules.task.mapper.TaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

import static com.wsdy.saasops.common.constants.Constants.SYSTEM_USER;
import static com.wsdy.saasops.common.constants.OrderConstants.HR_TASK_ACTIVITY;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static com.wsdy.saasops.modules.member.entity.MbrAccountLog.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.math.BigDecimal;
import java.util.ArrayList;

@Slf4j
@Service
@Transactional
public class MbrAccountLogService {

    @Autowired
    private MbrAccountLogMapper accountLogMapper;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private MbrGroupMapper groupMapper;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private MbrActivityLevelMapper activityLevelMapper;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private MbrGroupMapper mbrGroupMapper;
    @Autowired
    private SysWarningMapper sysWarningMapper;

    public void updateAccountInfo(MbrAccount account, MbrAccount mbrAccount, String userName, String ip) {
        // TIPS: 此处的格式不能随便变更，若要变更，请一并变更**号处理的逻辑
        if (null != mbrAccount.getRealName() && !mbrAccount.getRealName().equals(account.getRealName())) {
            String afterChange = "真实姓名:" + account.getRealName() + " > " + mbrAccount.getRealName();
            addMbrAccountLog(mbrAccount.getId(), userName, ACCOUNT_INFO_ONE + account.getLoginName(),
                    null, afterChange, account.getLoginName(), Constants.EVNumber.two, MEMBER_LIST, ip);
        }
        if ( null != mbrAccount.getMobile() && !mbrAccount.getMobile().equals(account.getMobile())) {
            String newMobile ="";
            if (StringUtil.isEmpty(mbrAccount.getMobile())||mbrAccount.getMobile().trim()==""){
                newMobile = "空";
            }else {
                newMobile = mbrAccount.getMobile();
            }
            String afterChange = "电话:" + account.getMobile() + " > " + newMobile;
            addMbrAccountLog(mbrAccount.getId(), userName, ACCOUNT_INFO_ONE + account.getLoginName(),
                    null, afterChange, account.getLoginName(), Constants.EVNumber.two, MEMBER_LIST, ip);
        }
        if (null != mbrAccount.getEmail() && !mbrAccount.getEmail().equals(account.getEmail())) {
            String afterChange = "邮箱:" + account.getEmail() + " > " + mbrAccount.getEmail();
            addMbrAccountLog(mbrAccount.getId(), userName, ACCOUNT_INFO_ONE + account.getLoginName(),
                    null, afterChange, account.getLoginName(), Constants.EVNumber.two, MEMBER_LIST, ip);
        }
        if (null != mbrAccount.getWeChat() && !mbrAccount.getWeChat().equals(account.getWeChat())) {
            String afterChange = "微信:" + account.getWeChat() + " > " + mbrAccount.getWeChat();
            addMbrAccountLog(mbrAccount.getId(), userName, ACCOUNT_INFO_ONE + account.getLoginName(),
                    null, afterChange, account.getLoginName(), Constants.EVNumber.two, MEMBER_LIST, ip);
        }
        if (null != mbrAccount.getQq() && !mbrAccount.getQq().equals(account.getQq())) {
            String afterChange = "QQ:" + account.getQq() + " > " + mbrAccount.getQq();
            addMbrAccountLog(mbrAccount.getId(), userName, ACCOUNT_INFO_ONE + account.getLoginName(),
                    null, afterChange, account.getLoginName(), Constants.EVNumber.two, MEMBER_LIST, ip);
        }
        if (null != mbrAccount.getGender() && !mbrAccount.getGender().equals(account.getGender())) {
            String afterChange = "性别:" + account.getGender() + " > " + mbrAccount.getGender();
            addMbrAccountLog(mbrAccount.getId(), userName, ACCOUNT_INFO_ONE + account.getLoginName(),
                    null, afterChange, account.getLoginName(), Constants.EVNumber.two, MEMBER_LIST, ip);
        }
        if (null != mbrAccount.getBirthday() && !mbrAccount.getBirthday().equals(account.getBirthday())) {
            String afterChange = "生日:" + account.getBirthday() + " > " + mbrAccount.getBirthday();
            addMbrAccountLog(mbrAccount.getId(), userName, ACCOUNT_INFO_ONE + account.getLoginName(),
                    null, afterChange, account.getLoginName(), Constants.EVNumber.two, MEMBER_LIST, ip);
        }
    }

    public void updateAccountAvailable(Integer accountId, MbrAccount account, String userName, String ip) {
        MbrAccount account1 = accountMapper.selectByPrimaryKey(accountId);
        String beforeChange = account1.getAvailable() == 0 ? "禁用"
                : account1.getAvailable() == 1 ? "开启" : "余额冻结";
        String afterChange = account.getAvailable() == 0 ? "禁用"
                : account.getAvailable() == 1 ? "开启" : "余额冻结";
        addMbrAccountLog(accountId, userName, ACCOUNT_STATUS_ONE + account1.getLoginName() + ACCOUNT_STATUS_TWO,
                beforeChange, afterChange, account1.getLoginName(), Constants.EVNumber.two, MEMBER_LIST, ip);
    }

    public void addAccountMemo(String userName, MbrMemo mbrMemo, String ip) {
        MbrAccount account = accountMapper.selectByPrimaryKey(mbrMemo.getAccountId());
        addMbrAccountLog(mbrMemo.getAccountId(), userName,
                ACCOUNT_MEMO_ONE + account.getLoginName() + ACCOUNT_MEMO_TWO, null, mbrMemo.getMemo(), account.getLoginName(), Constants.EVNumber.two, MEMBER_LIST, ip);
    }

    //新增会员
    public void addAccountLog(MbrAccount mbrAccount, String userName, String ip) {
        String afterChange = mbrAccount.getLoginName();
        addMbrAccountLog(mbrAccount.getId(), userName,
                "新增会员", null, afterChange, mbrAccount.getLoginName(), Constants.EVNumber.two, MEMBER_LIST, ip);
    }

    //新增会员组
    public void addMbrGroupLog(SysUserMbrgrouprelation sysUserMbrgrouprelation, String groupName, String userName, String ip) {
        addMbrAccountLog(null, userName,
                "新增会员组", null, groupName, "", Constants.EVNumber.two, MEMBER_GROUP, ip);
    }

    //批量切换会员组
    public void changeMbrGroupLog(MbrAccount mbrAccount, String groupName, String userName, String ip) {
        addMbrAccountLog(mbrAccount.getId(), userName,
                "更换会员" + mbrAccount.getLoginName() + "的会员组为", null, groupName, "", Constants.EVNumber.two, MEMBER_GROUP, ip);
    }

    //会员组存款设置
    public void updateGroupDepositConfig(MbrDepositCond mbrDepositCond, String userName, String ip) {
        MbrGroup group1 = mbrMapper.selectMbrGroupById(Long.parseLong(mbrDepositCond.getGroupId().toString()));
        addMbrAccountLog(null, userName,
                "修改会员组" + group1.getGroupName() + "存款设置", null, null, "", Constants.EVNumber.two, MEMBER_GROUP, ip);
    }

    //会员组取款设置
    public void updateGroupWisdrawConfig(MbrWithdrawalCond mbrWithdrawalCond, String userName, String ip) {
        MbrGroup group1 = mbrMapper.selectMbrGroupById(Long.parseLong(mbrWithdrawalCond.getGroupId().toString()));
        addMbrAccountLog(null, userName,
                "修改会员组" + group1.getGroupName() + "取款设置", null, null, "", Constants.EVNumber.two, MEMBER_GROUP, ip);
    }

    //会员组返点设置
    public void updateAccountGroupRebate(MbrRebate rebate, String userName, String ip) {
        MbrGroup group1 = mbrMapper.selectMbrGroupById(Long.parseLong(rebate.getGroupId().toString()));
        addMbrAccountLog(null, userName,
                "修改会员组" + group1.getGroupName() + "返点设置", null, null, "", Constants.EVNumber.two, MEMBER_GROUP, ip);
    }

    //删除会员组
    public void deleteAccountGroup(MbrGroup mbrGroup, String userName, String ip) {
        addMbrAccountLog(null, userName,
                "删除会员组" + mbrGroup.getGroupName(), null, null, "", Constants.EVNumber.two, MEMBER_GROUP, ip);
    }

    //修改会员组状态
    public void updateGroupAvailable(MbrGroup mbrGroup, String userName, String ip) {
        String afterChange = mbrGroup.getAvailable() == 1 ? "开启" : "禁用";
        addMbrAccountLog(null, userName,
                "修改会员组" + mbrGroup.getGroupName() + "的状态", null, afterChange, "", Constants.EVNumber.two, MEMBER_GROUP, ip);
    }

    //编辑会员组
    public void updateAcccountGroup(MbrGroup mbrGroup, String userName, String ip) {
        String afterChange = "名称:" + mbrGroup.getGroupName() + "备注:" + mbrGroup.getMemo();
        addMbrAccountLog(null, userName,
                "编辑会员组" + mbrGroup.getGroupName(), null, afterChange, "", Constants.EVNumber.two, MEMBER_GROUP, ip);
    }

    //修改会员其他资料  account：old，mbrAccount：new
    public void updateAccountRest(MbrAccount account, MbrAccount mbrAccount, String userName, String ip) {
        MbrGroup group = groupMapper.selectByPrimaryKey(account.getGroupId());
//        AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(account.getCagencyId());
        MbrGroup group1 = groupMapper.selectByPrimaryKey(mbrAccount.getGroupId());
//        AgentAccount agentAccount1 = agentAccountMapper.selectByPrimaryKey(mbrAccount.getCagencyId());
        // 由levelId获得星级名称
        MbrActivityLevel levelOld = activityLevelMapper.selectByPrimaryKey(account.getActLevelId());
        MbrActivityLevel levelNew = activityLevelMapper.selectByPrimaryKey(mbrAccount.getActLevelId());

        // 处理，避免前端不送值导致空异常
        String afterChange = "会员组:" + group.getGroupName() + " > " + (nonNull(group1) ? group1.getGroupName() : group.getGroupName())
                + ",状态:" + (account.getAvailable() == 0 ? "禁用" : (account.getAvailable() == 1 ? "开启" : "余额冻结")) + " > " + ((nonNull(mbrAccount.getAvailable()) ? mbrAccount.getAvailable() : account.getAvailable()) == 0 ? "禁用" : ((nonNull(mbrAccount.getAvailable()) ? mbrAccount.getAvailable() : account.getAvailable()) == 1 ? "开启" : "余额冻结"))
//                + ",代理:" + (nonNull(agentAccount) ? agentAccount.getAgyAccount() : "") + " > " + (nonNull(agentAccount1) ? agentAccount1.getAgyAccount() : (nonNull(agentAccount) ? agentAccount.getAgyAccount() : ""))
                + ",星级:" + levelOld.getTierName() + " > " + (nonNull(levelNew) ? levelNew.getTierName() : levelOld.getTierName());
        addMbrAccountLog(account.getId(), userName, ACCOUNT_INFO_OT_ONE + account.getLoginName(),
                null, afterChange, userName, Constants.EVNumber.two, MEMBER_LIST, ip);
    }

    // 存款锁定修改日志
    public void updateAccountRestDepositLock(MbrAccount mbrAccount) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = MEMBER_LIST_DETAIL;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = "修改会员" + mbrAccount.getLoginName() + "入款防刷：" + (mbrAccount.getDepositLock().equals(Constants.EVNumber.zero) ? "冻结" : "正常") + " > "
                + (mbrAccount.getDepositLock().equals(Constants.EVNumber.zero) ? "正常" : "冻结");
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }


    //新增银行卡信息
    public void addAccountBank(MbrBankcard mbrBankcard, MbrAccount mbrAccount, String userName, int operatorType, String ip) {
        addMbrAccountLog(mbrAccount.getId(), userName,
                ACCOUNT_BANK_ADD_ONE + mbrAccount.getLoginName() + ACCOUNT_BANK_TWO, null, mbrBankcard.getBankName(),
                isNull(userName) ? mbrAccount.getLoginName() : userName, operatorType, MEMBER_LIST, ip);
    }

    //修改银行卡信息
    public void updateAccountBank(MbrBankcard mbrBankcard, String userName, int operatorType, String ip) {
        MbrAccount account = accountMapper.selectByPrimaryKey(mbrBankcard.getAccountId());
        addMbrAccountLog(mbrBankcard.getAccountId(), userName,
                ACCOUNT_BANK_ONE + account.getLoginName() + ACCOUNT_BANK_TWO, null, mbrBankcard.getBankName(),
                isNull(userName) ? account.getLoginName() : userName, operatorType, MEMBER_LIST, ip);
    }

    //修改银行卡状态
    public void updateAccountBankStatus(MbrBankcard mbrBankcard, String userName, int operatorType, String ip) {
        MbrAccount account = accountMapper.selectByPrimaryKey(mbrBankcard.getAccountId());
        String afterChange = mbrBankcard.getAvailable() == 1 ? "开启" : "禁用";
        addMbrAccountLog(mbrBankcard.getAccountId(), userName,
                ACCOUNT_BANK_ONE + account.getLoginName() + ACCOUNT_BANK_STATUS, null, afterChange,
                isNull(userName) ? account.getLoginName() : userName, operatorType, MEMBER_LIST, ip);
    }

    //删除银行卡
    public void deleteAccountBank(MbrBankcard mbrBankcard, String userName, int operatorType, String ip) {
        String afterChange = mbrBankcard.getBankName() + "卡号：" + mbrBankcard.getCardNo();
        addMbrAccountLog(mbrBankcard.getAccountId(), userName,
                ACCOUNT_BANK_DELETE + mbrBankcard.getLoginName() + ACCOUNT_BANK_TWO, null, afterChange,
                isNull(userName) ? mbrBankcard.getLoginName() : userName, operatorType, MEMBER_LIST, ip);
    }

    //删除站内信
    public void deleteOprRecLog(OprRecMbr opr, String title, String userName, String ip) {
        String afterChange = title;
        addMbrAccountLog(null, userName,
                "删除站内信", null, afterChange,
                userName, 2, OPR_REC_MSG, ip);
    }

    //发送站内信
    public void sendRecMbrMail(OprRecMbr oprRecMbr, String userName, int operatorType, String ip) {
        //站内信发送会员
        if (null != oprRecMbr.getMbrList()) {
            for (MbrAccount mbrAccount : oprRecMbr.getMbrList()) {
                MbrAccount newAccount = accountMapper.selectByPrimaryKey(mbrAccount.getId());
                addMbrAccountLog(mbrAccount.getId(), userName,
                        ACCOUNT_TO_ACCOUNT + newAccount.getLoginName() + ACCOUNT_TO_SENDMAIL, null, "",
                        newAccount.getLoginName(), operatorType, OPR_REC_MSG, ip);
            }
        }
        // 站内信发送代理
        if (null != oprRecMbr.getAgyList()) {
            for (AgentAccount agentAccount : oprRecMbr.getAgyList()) {
                addMbrAccountLog(null, userName,
                        ACCOUNT_TO_AGENT + agentAccount.getAgyAccount() + ACCOUNT_TO_SENDMAIL, null, "",
                        agentAccount.getAgyAccount(), operatorType, OPR_REC_MSG, ip);
            }
        }
    }

    //修改会员真实姓名
    public void updateApiAccountName(MbrAccount mbrAccount, String realName, String ip) {
        addMbrAccountLog(mbrAccount.getId(), mbrAccount.getLoginName(),
                ACCOUNT_NAME_ONE + mbrAccount.getLoginName() + ACCOUNT_NAME_TWO, mbrAccount.getRealName(), realName,
                mbrAccount.getLoginName(), Constants.EVNumber.one, MEMBER_LIST, ip);
    }

    //修改会员邮箱
    public void updateApiAccountMail(MbrAccount account, String email, String ip) {
        MbrAccount mbrAccount = accountMapper.selectByPrimaryKey(account.getId());
        addMbrAccountLog(mbrAccount.getId(), mbrAccount.getLoginName(),
                ACCOUNT_EMAIL_ONE + mbrAccount.getLoginName() + ACCOUNT_EMAIL_TWO, account.getEmail(), email,
                mbrAccount.getLoginName(), Constants.EVNumber.one, MEMBER_LIST, ip);
    }

    //修改会员电话
    public void updateApiAccountMobile(MbrAccount account, String mobile, String ip) {
        MbrAccount mbrAccount = accountMapper.selectByPrimaryKey(account.getId());
        addMbrAccountLog(account.getId(), mbrAccount.getLoginName(),
                ACCOUNT_MOBILE_ONE + mbrAccount.getLoginName() + ACCOUNT_MOBILE_TWO, account.getMobile(), mobile,
                mbrAccount.getLoginName(), Constants.EVNumber.one, MEMBER_LIST, ip);
    }

    //修改会员登录密码
    public void updateAccountLoginPwd(MbrAccount account, String userName, String ip) {
        MbrAccount mbrAccount = accountMapper.selectByPrimaryKey(account.getId());
        addMbrAccountLog(account.getId(), userName,
                ACCOUNT_MOBILE_ONE + mbrAccount.getLoginName() + ACCOUNT_LOGIN_PWD, null, null,
                userName, Constants.EVNumber.two, MEMBER_LIST, ip);
    }

    //修改会员提款密码
    public void updateAccountDrawPwd(MbrAccount account, String userName, String ip) {
        MbrAccount mbrAccount = accountMapper.selectByPrimaryKey(account.getId());
        addMbrAccountLog(account.getId(), mbrAccount.getLoginName(),
                ACCOUNT_MOBILE_ONE + mbrAccount.getLoginName() + ACCOUNT_DRAW_PWD, null, null,
                userName, Constants.EVNumber.two, MEMBER_LIST, ip);
    }

    //人工增加调整
    public void addAuditSave(FundAudit fundAudit, String userName, String ip) {
        String afterChange = fundAudit.getAmount() + "";
        addMbrAccountLog(fundAudit.getAccountId(), userName,
                ACCOUNT_AUDIT_ADD + fundAudit.getLoginName() + ACCOUNT_LIMIT, null, afterChange,
                userName, Constants.EVNumber.two, MEMBER_LIST, ip);
    }
    
    // 账户核对 资金调整
    public void addUpdateAdjustment(MbrWallet mbrWallet, String userName, String ip, BigDecimal beforeChange) {
    	MbrAccount selectByPrimaryKey = accountMapper.selectByPrimaryKey(mbrWallet.getAccountId());
    	String afterChange = mbrWallet.getAdjustment() + "";
    	String afterChangeStr = "";
    	if (beforeChange != null) {
    		afterChangeStr = String.valueOf(beforeChange);
    	}
    	addMbrAccountLog(mbrWallet.getAccountId(), userName,
    			UPDATE_ADJUSTMENT + selectByPrimaryKey.getLoginName() + ACCOUNT_LIMIT, afterChangeStr, afterChange,
    			userName, Constants.EVNumber.two, MEMBER_LIST, ip);
    }

    //d 人工减少调整
    public void reduceAuditSave(FundAudit fundAudit, String userName, String ip) {
        String afterChange = fundAudit.getAmount() + "";
        addMbrAccountLog(fundAudit.getAccountId(), userName,
                ACCOUNT_AUDIT_REDUCE + fundAudit.getLoginName() + ACCOUNT_LIMIT, null, afterChange,
                userName, Constants.EVNumber.two, MEMBER_LIST, ip);
    }

    //会员踢线
    public void accountKickLine(MbrAccount account, String userName, String ip) {
        MbrAccount mbrAccount = accountMapper.selectByPrimaryKey(account.getId());
        addMbrAccountLog(mbrAccount.getId(), userName,
                "把" + mbrAccount.getLoginName() + "踢下了线", null, null,
                userName, Constants.EVNumber.two, MEMBER_LIST, ip);
    }

    //会员被拉到黑名单会员组
    public void accountBackListGroup(Integer id, String afterGroupName) {
        MbrAccount mbrAccount = accountMapper.selectByPrimaryKey(id);
        MbrGroup beforeGroup = mbrGroupMapper.selectByPrimaryKey(mbrAccount.getGroupId());
        addMbrAccountLog(mbrAccount.getId(), mbrAccount.getLoginName(),
                "会员组", beforeGroup.getGroupName(), afterGroupName,
                SYSTEM_USER, Constants.EVNumber.two, MEMBER_GROUP, null);
    }

    private void addMbrAccountLog(Integer accountId, String loginName, String item, String beforeChange,
                                  String afterChange, String userName, int operatorType, String moduleName, String ip) {
        AccountLogDto accountLogDto = new AccountLogDto();
        accountLogDto.setStatus(ACCOUNT_SUCCEED);
        accountLogDto.setItem(item);
        accountLogDto.setBeforeChange(beforeChange);
        accountLogDto.setAfterChange(afterChange);
        accountLogDto.setOperatorUser(userName);
        accountLogDto.setOperatorType(operatorType);

        MbrAccountLog mbrAccountLog = new MbrAccountLog();
        if (nonNull(accountId)) {
            mbrAccountLog.setAccountId(accountId);
        }
        if (nonNull(loginName)) {
            mbrAccountLog.setLoginName(loginName);
        }
        mbrAccountLog.setOrderNo(String.valueOf(new SnowFlake().nextId()));
        mbrAccountLog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        mbrAccountLog.setContent(JSON.toJSONString(accountLogDto));
        mbrAccountLog.setModuleName(moduleName);
        mbrAccountLog.setIp(ip);
        accountLogMapper.insert(mbrAccountLog);
    }
    
    /**
     * The batch update member group log
     * @param mbrAccountList
     * @param groupName
     */
    public void addMbrAccountLogBatch(List<MbrAccount> mbrAccountList, String groupName) {
    	List<MbrAccountLog> insertList = new ArrayList<>(mbrAccountList.size());
    	for (MbrAccount mbrAccount : mbrAccountList) {
	    	AccountLogDto accountLogDto = new AccountLogDto();
	    	accountLogDto.setStatus(ACCOUNT_SUCCEED);
	    	accountLogDto.setItem("更换会员" + mbrAccount.getLoginName() + "的会员组为");
	    	accountLogDto.setBeforeChange(null);
	    	accountLogDto.setAfterChange(groupName);
	    	accountLogDto.setOperatorUser("");
	    	accountLogDto.setOperatorType(Constants.EVNumber.two);
	    	
	    	MbrAccountLog mbrAccountLog = new MbrAccountLog();
    		mbrAccountLog.setAccountId(mbrAccount.getId());
    		mbrAccountLog.setLoginName(mbrAccount.getLoginName());
	    	mbrAccountLog.setOrderNo(String.valueOf(new SnowFlake().nextId()));
	    	mbrAccountLog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
	    	mbrAccountLog.setContent(JSON.toJSONString(accountLogDto));
	    	mbrAccountLog.setModuleName(MEMBER_GROUP_AUTO);
	    	insertList.add(mbrAccountLog);
    	}
    	
		// d一次oneTimeCount条，分updateTimes次插入
		int updateTimes = (insertList.size() / Constants.BATCH_ONCE_COUNT) + 1;
		// d最后一次插入lastCount条，取余数
		int lastCount = insertList.size() % Constants.BATCH_ONCE_COUNT;
		// d插入，会循环
		for (int i = 0; i < updateTimes; i++) {
			int start = i * Constants.BATCH_ONCE_COUNT;
			int end = (i + 1) * Constants.BATCH_ONCE_COUNT;
			// d声明一个list集合,用于存放每次批量插入的数据
			List<MbrAccountLog> targetList = new ArrayList<>(Constants.BATCH_ONCE_COUNT);
			if (i == updateTimes - 1) {
				// subList(index, end)，list集合的下标从0开始,[start,end) subList包含start,不包含end。
				// d最后一次插入 subList(end, end+lastNumber)
				targetList = insertList.subList(start, start + lastCount);
			} else {
				// d非最后一次插入 subList(start, end)
				targetList = insertList.subList(start, end);
			}
			accountLogMapper.insertList(targetList);
		}
    }

    public PageUtils accountLogList(Integer accountId, Integer pageNo, Integer pageSize, Long userId) {
        PageHelper.startPage(pageNo, pageSize);
        PageHelper.orderBy("createTime DESC");
        MbrAccountLog accountLog = new MbrAccountLog();
        accountLog.setAccountId(accountId);
        List<MbrAccountLog> accountLogs = accountLogMapper.select(accountLog);
        if (Collections3.isNotEmpty(accountLogs)) {
            String perms = mbrMapper.findAccountContact(userId, Constants.ACCOUNT_CONTACT);
            String crPerms = mbrMapper.findAccountContact(userId, Constants.ACCOUNT_CURRENCY);  // 加密货币钱包
            accountLogs.stream().forEach(as -> {
                as.setLogDto(jsonUtil.fromJson(as.getContent(), AccountLogDto.class));
//                if (nonNull(as.getLogDto())) {
//                    if (ACCOUNT_INFO.equals(as.getLogDto().getItem())) {
//                        as.getLogDto().setBeforeChange(getAccountStr(as.getLogDto().getBeforeChange(), perms));
//                        as.getLogDto().setAfterChange(getAccountStr(as.getLogDto().getAfterChange(), perms));
//                    }
//                    if (ACCOUNT_MOBILE.equals(as.getLogDto().getItem())) {
//                        as.getLogDto().setBeforeChange(perms.contains("mobile")?as.getLogDto().getBeforeChange():StringUtil.phone(as.getLogDto().getBeforeChange()));
//                        as.getLogDto().setAfterChange(perms.contains("mobile")?as.getLogDto().getAfterChange():StringUtil.phone(as.getLogDto().getAfterChange()));
//                    }
//                    if (ACCOUNT_EMAIL.equals(as.getLogDto().getItem())){
//                        as.getLogDto().setBeforeChange(perms.contains("email")?as.getLogDto().getBeforeChange():StringUtil.mail(as.getLogDto().getBeforeChange()));
//                        as.getLogDto().setAfterChange(perms.contains("email")?as.getLogDto().getAfterChange():StringUtil.mail(as.getLogDto().getAfterChange()));
//                    }
//                }
                if (nonNull(as.getLogDto()) && nonNull(as.getLogDto().getItem())) {
                    if (as.getLogDto().getItem().contains(ACCOUNT_INFO_ONE)) {    // 修改会员
                        // 电话
                        if (nonNull(as.getLogDto().getAfterChange()) && as.getLogDto().getAfterChange().contains(ACCOUNT_MOBILE_EX)) {
                            // 获取电话
                            String[] arr = as.getLogDto().getAfterChange().replace(">", ":").replace(" ", "").split(":");
                            String phoneOld = "";
                            String phoneNew = "";
                            if (arr.length == Constants.EVNumber.three) {
                                phoneOld = StringUtil.phone(arr[1]);
                                phoneNew = StringUtil.phone(arr[2]);
                            }
                            if (arr.length == Constants.EVNumber.two) {
                                phoneOld = StringUtil.phone(arr[1]);
                            }

                            phoneNew = ACCOUNT_MOBILE_EX + phoneOld + ">" + phoneNew;
                            if (nonNull(perms)) {
                                as.getLogDto().setAfterChange(perms.contains("mobile") ? as.getLogDto().getAfterChange() : phoneNew);
                            } else {
                                as.getLogDto().setAfterChange(phoneNew);
                            }
                        }
                        // 邮箱
                        if (nonNull(as.getLogDto().getAfterChange()) && as.getLogDto().getAfterChange().contains(ACCOUNT_EMAIL_EX)) {
                            // 获取邮箱
                            String[] arr = as.getLogDto().getAfterChange().replace(">", ":").replace(" ", "").split(":");
                            String emailOld = "";
                            String emailNew = "";
                            if (arr.length == Constants.EVNumber.three) {
                                emailOld = StringUtil.mail(arr[1]);
                                emailNew = StringUtil.mail(arr[2]);
                            }
                            if (arr.length == Constants.EVNumber.two) {
                                emailOld = StringUtil.mail(arr[1]);
                            }
                            emailNew = ACCOUNT_EMAIL_EX + emailOld + ">" + emailNew;
                            if (nonNull(perms)) {
                                as.getLogDto().setAfterChange(perms.contains("email") ? as.getLogDto().getAfterChange() : emailNew);
                            } else {
                                as.getLogDto().setAfterChange(emailNew);
                            }
                        }
                        // qq
                        if (nonNull(as.getLogDto().getAfterChange()) && as.getLogDto().getAfterChange().contains(ACCOUNT_QQ_EX)) {
                            // qq
                            String[] arr = as.getLogDto().getAfterChange().replace(">", ":").replace(" ", "").split(":");
                            String qqOld = "";
                            String qqNew = "";
                            if (arr.length == Constants.EVNumber.three) {
                                qqOld = StringUtil.QQ(arr[1]);
                                qqNew = StringUtil.QQ(arr[2]);
                            }
                            if (arr.length == Constants.EVNumber.two) {
                                qqOld = StringUtil.QQ(arr[1]);
                            }
                            qqNew = ACCOUNT_QQ_EX + qqOld + ">" + qqNew;
                            if (nonNull(perms)) {
                                as.getLogDto().setAfterChange(perms.contains("qq") ? as.getLogDto().getAfterChange() : qqNew);
                            } else {
                                as.getLogDto().setAfterChange(qqNew);
                            }
                        }
                        // wechat
                        if (nonNull(as.getLogDto().getAfterChange()) && as.getLogDto().getAfterChange().contains(ACCOUNT_WECHAT_EX)) {
                            // wechat
                            String[] arr = as.getLogDto().getAfterChange().replace(">", ":").replace(" ", "").split(":");
                            String wechatOld = "";
                            String wechatNew = "";
                            if (arr.length == Constants.EVNumber.three) {
                                wechatOld = StringUtil.QQ(arr[1]);
                                wechatNew = StringUtil.QQ(arr[2]);
                            }
                            if (arr.length == Constants.EVNumber.two) {
                                wechatOld = StringUtil.QQ(arr[1]);
                            }
                            wechatNew = ACCOUNT_WECHAT_EX + wechatOld + ">" + wechatNew;
                            if (nonNull(perms)) {
                                as.getLogDto().setAfterChange(perms.contains("wechat") ? as.getLogDto().getAfterChange() : wechatNew);
                            } else {
                                as.getLogDto().setAfterChange(wechatNew);
                            }
                        }
                        // 真实姓名
                        if (nonNull(as.getLogDto().getAfterChange()) && as.getLogDto().getAfterChange().contains(ACCOUNT_NAME_TREE)) {
                            // 真实姓名
                            String[] arr = as.getLogDto().getAfterChange().replace(">", ":").replace(" ", "").split(":");
                            String realNameOld = "";
                            String realNameNew = "";
                            if (arr.length == Constants.EVNumber.three) {
                                realNameOld = StringUtil.realName(arr[1]);
                                realNameNew = StringUtil.realName(arr[2]);
                            }
                            if (arr.length == Constants.EVNumber.two) {
                                realNameOld = StringUtil.realName(arr[1]);
                            }
                            realNameNew = ACCOUNT_NAME_TREE + realNameOld + ">" + realNameNew;
                            if (nonNull(perms)) {
                                as.getLogDto().setAfterChange(perms.contains("realname") ? as.getLogDto().getAfterChange() : realNameNew);
                            } else {
                                as.getLogDto().setAfterChange(realNameNew);
                            }
                        }

                    }
                    if (as.getLogDto().getItem().contains(ACCOUNT_INFO_WALLET)) {  // 钱包
                        if (as.getLogDto().getItem().contains(ACCOUNT_STATUS_FIVE)) {  // 删除
                            as.getLogDto().setAfterChange(null);
                        }
                    }

                }
                as.setContent(null);
            });
        }
        return BeanUtil.toPagedResult(accountLogs);
    }

    private String getAccountStr(String str, String perms) {
        if (StringUtil.isNotEmpty(str)) {
            String[] arr = str.replace(",", ":").split(":");

            String mobile = StringUtil.phone(arr[3]);
            String email = StringUtil.mail(arr[5]);
            String qq = StringUtil.QQ(arr[7]);
            String wechat = (arr.length == 9 ? "" : StringUtil.QQ(arr[9]));
            if (null == perms) {
                return arr[0] + ":" + arr[1] + "," + arr[2] + ":"
                        + mobile + "," + arr[4] + ":" + email + ","
                        + arr[6] + ":" + qq + "," + arr[8] + ":" + wechat;
            }

            if (perms.contains("mobile")) {
                mobile = arr[3];
            }
            if (perms.contains("email")) {
                email = arr[5];
            }
            if (perms.contains("qq")) {
                qq = arr[7];
            }
            if (perms.contains("wechat")) {
                wechat = (arr.length == 9 ? "" : arr[9]);
            }

            return arr[0] + ":" + arr[1] + "," + arr[2] + ":"
                    + mobile + "," + arr[4] + ":" + email + ","
                    + arr[6] + ":" + qq + "," + arr[8] + ":" + wechat;
        }
        return StringUtils.EMPTY;
    }

    //会员入款操作日志
    public void updateMemberDepositInfo(FundDeposit deposit, FundDeposit oldDeposit, String userName, String ip) {
        String afterChange = deposit.getStatus() == 1 ? "成功" : "失败";
        String beforeChange;
        if (oldDeposit.getStatus() == 2) {
            beforeChange = "待处理";
        } else if (oldDeposit.getStatus() == 1) {
            beforeChange = "成功";
        } else {
            beforeChange = "";
        }
        addFundAccountLog(deposit.getAccountId(), userName, ACCOUNT_DEPOSIT_INFO_ONE + deposit.getOrderPrefix() + deposit.getOrderNo() + ACCOUNT_DEPOSIT_INFO_TWO,
                beforeChange, afterChange, userName, Constants.EVNumber.two, deposit.getOrderNo(), MEMBER_DEPOSIT, ip);
    }

    //会员提款操作日志
    public void updateMemberWithdrawInfo(AccWithdraw withdraw, AccWithdraw oldWithdraw, String userName, String ip) {
        String afterChange = "";

        switch (withdraw.getStatus()) {
            case 1:
                afterChange = "通过";
                break;
            case 2:
                afterChange = "初审";
                break;
            case 3:
                afterChange = "复审";
                break;
            case 6:
                afterChange = "初审待定";
                break;
            case 7: //失败订单
                afterChange = "失败";
                break;
            case 5: //自动出款的逻辑
                afterChange = "自动出款中";
                break;

            case 4: //自动出款的逻辑
                afterChange = "自动出款人工审核";
                break;
            default:
                afterChange = "拒绝";
        }

        //String beforeChange = oldWithdraw.getStatus() == 2 ? "初审" : "复审";
        String beforeChange = "";
        switch (oldWithdraw.getStatus()) {
            case 1:
                beforeChange = "通过";
                break;
            case 2:
                beforeChange = "初审";
                break;
            case 3:
                beforeChange = "复审";
                break;
            case 6:
                beforeChange = "初审待定";
                break;
            case 7: //失败订单
                afterChange = "失败";
                break;
            case 5: //自动出款的逻辑
                afterChange = "自动出款中";
                break;
            case 4: //自动出款的逻辑
                afterChange = "自动出款人工审核";
                break;
            default:
                beforeChange = "复审";
        }
        addFundAccountLog(withdraw.getAccountId(), userName, ACCOUNT_WITHDRAW_INFO_ONE + withdraw.getOrderPrefix() + withdraw.getOrderNo() + beforeChange + ACCOUNT_WITHDRAW_INFO_TWO,
                beforeChange, afterChange, userName, Constants.EVNumber.two, withdraw.getOrderNo(), MEMBER_WITHDRAW, ip);
    }

    //资金调整操作日志
    public void updateFundAuditInfo(FundAudit fundAudit, FundAudit oldAudit, String userName, String ip) {
        String afterChange = fundAudit.getStatus() == 1 ? "通过" : "拒绝";
        String beforeChange = oldAudit.getStatus() == 2 ? "待处理" : "";
        addFundAccountLog(fundAudit.getAccountId(), userName, ACCOUNT_AUDIT_INFO_ONE + fundAudit.getFinancialCode() + fundAudit.getOrderNo() + ACCOUNT_AUDIT_INFO_TWO,
                beforeChange, afterChange, "", Constants.EVNumber.two, fundAudit.getOrderNo(), FUND_ADJUST, ip);
    }

    //红利列表审核操作日志
    public void updateBonusInfo(OprActBonus actBonus, OprActBonus oldBonus, String userName, String ip, String activityName) {
        String afterChange = actBonus.getStatus() == 3 ? "可使用" : "拒绝";
        String beforeChange = oldBonus.getStatus() == 2 ? "待处理" : "";
        addFundAccountLog(actBonus.getAccountId(), userName, ACCOUNT_BONUS_INFO_ONW + actBonus.getLoginName() + "的" + activityName,
                beforeChange, afterChange, userName, Constants.EVNumber.two, actBonus.getActivityId() + "", BONUS_LIST, ip);
    }

    //增加活动的操作日志
    public void bonusAddInfo(OprActActivity activity, String userName, Long userId, String ip) {
        addFundAccountLog(null, userName, ACCOUNT_BONUS_ADD_INFO_ONW,
                "", activity.getActivityName(), userName, Constants.EVNumber.two, activity.getId() + "", ACTIVITY_CONFIG, ip);
    }

    //编辑活动操作日志
    public void updateBonusEditInfo(OprActActivity activity, OprActActivity oldActivity, String userName, Long userId, String ip) {
        //String afterChange = activity.toString();
        //String beforeChange = oldActivity.toString();
        addFundAccountLog(null, userName, ACCOUNT_BONUS_EDIT_INFO_ONE,
                "", activity.getActivityName(), userName, Constants.EVNumber.two, activity.getId() + "", ACTIVITY_CONFIG, ip);
    }

    //活动状态编辑操作日志
    public void updateBonusStatusInfo(OprActActivity activity, OprActActivity oldActivity, String userName, Long userId, String ip) {
        String afterChange = activity.getAvailable() == 1 ? "开启" : "禁用";
        String beforeChange = oldActivity.getAvailable() == 1 ? "开启" : "禁用";
        addFundAccountLog(null, userName, ACCOUNT_BONUS_STATUS_INFO_ONE + activity.getActivityName() + ACCOUNT_BONUS_STATUS_INFO_TWO,
                beforeChange, afterChange, userName, Constants.EVNumber.two, activity.getId() + "", ACTIVITY_CONFIG, ip);
    }

    //修改稽核1
    public void updateAccountAuditInfo(MbrAuditAccount accountAudit, String userName, String ip) {
        String afterChange = accountAudit.getAuditAmount() + "";
        addFundAccountLog(accountAudit.getAccountId(), userName, ACCOUNT_AUDIT_UPDATE + accountAudit.getLoginName() + ACCOUNT_AUDIT,
                "", afterChange, userName, Constants.EVNumber.two, accountAudit.getId() + "", AUDIT_REPORT, ip);
    }

    //修改稽核2
    public void updateAccountAuditBonusInfo(MbrAuditBonus auditBonus, String userName, String ip) {
        //String afterChange = auditBonus.getAuditAmount()+"";
        addFundAccountLog(auditBonus.getAccountId(), userName, ACCOUNT_AUDIT_UPDATE + auditBonus.getLoginName() + ACCOUNT_ILLEGAL_AUDIT_THREE,
                "", "", userName, Constants.EVNumber.two, auditBonus.getActivityId() + "", AUDIT_REPORT, ip);
    }

    //清除稽核2
    public void cleanAccountAuditInfo(MbrAuditAccount accountAudit, String userName, String ip) {
        //String afterChange = accountAudit.getAuditAmount()+"";
        addFundAccountLog(accountAudit.getAccountId(), userName, ACCOUNT_AUDIT_UPDATE + accountAudit.getLoginName() + ACCOUNT_AUDIT,
                "", "", accountAudit.getLoginName(), Constants.EVNumber.two, accountAudit.getId() + "", AUDIT_REPORT, ip);
    }

    //清除稽核2
    public void cleanAccountAuditBonusInfo(MbrAuditBonus auditBonus, String userName, String ip) {
        //String afterChange = auditBonus.getAuditAmount()+"";
        addFundAccountLog(auditBonus.getAccountId(), userName, ACCOUNT_AUDIT_UPDATE + auditBonus.getLoginName() + ACCOUNT_ILLEGAL_AUDIT,
                "", "", auditBonus.getLoginName(), Constants.EVNumber.two, auditBonus.getActivityId() + "", AUDIT_REPORT, ip);
    }

    //增加稽核
    public void addAuditBonus(MbrAuditBonus auditBonus, String userName, String ip) {
        String afterChange = ACCOUNT_ADD_AUDIT + "  " + auditBonus.getAuditAmount();
        addFundAccountLog(auditBonus.getAccountId(), userName, ACCOUNT_UPDATE_ACCOUNT + auditBonus.getLoginName() + ACCOUNT_ILLEGAL_AUDIT_TWO,
                "", afterChange, userName, Constants.EVNumber.two, auditBonus.getActivityId() + "", AUDIT_REPORT, ip);
    }

    //清除违规稽核
    public void cleanIllegalAudit(MbrAuditBonus auditBonus, String userName, String ip) {
        addFundAccountLog(auditBonus.getAccountId(), userName, ACCOUNT_CLEAN_AUDIT + auditBonus.getLoginName() + ACCOUNT_ILLEGAL_AUDIT_TWO,
                "", "", userName, Constants.EVNumber.two, auditBonus.getActivityId() + "", AUDIT_REPORT, ip);
    }

    //稽核扣款
    public void auditCharge(MbrAuditBonus auditBonus, String userName, String ip) {
        String afterChange = auditBonus.getAuditAmount() + "";
        addFundAccountLog(auditBonus.getAccountId(), userName, ACCOUNT_CHARGE_AUDIT + auditBonus.getLoginName() + ACCOUNT_ILLEGAL_AMOUNT,
                "", afterChange, userName, Constants.EVNumber.two, auditBonus.getActivityId() + "", AUDIT_REPORT, ip);
    }

    //新增户内转账
    public void addAccountTransferLog(BillRequestDto requestDto, String userName, String ip) {
        String afterChange = requestDto.getOrderNo() + "";
        if (0 == requestDto.getOpType()) {
            addFundAccountLog(requestDto.getAccountId(), userName, ACCOUNT_TRANSFER_ADD + requestDto.getLoginName() + ACCOUNT_TRANSFER_DEPOSIT,
                    "", afterChange, userName, Constants.EVNumber.two, requestDto.getOrderNo() + "", ACCOUNT_TRANSFER, ip);
        } else {
            addFundAccountLog(requestDto.getAccountId(), userName, ACCOUNT_TRANSFER_ADD + requestDto.getLoginName() + ACCOUNT_TRANSFER_WISDRAW,
                    "", afterChange, userName, Constants.EVNumber.two, requestDto.getOrderNo() + "", ACCOUNT_TRANSFER, ip);
        }
    }

    //游戏列表PC状态log
    public void addDepotPCAvailable(TGameLogo tGameLogo, String userName, String ip) {
        addFundAccountLog(null, userName, GAME_LIST_UPDATE + tGameLogo.getDepotName() + "基本信息",
                "", "", userName, Constants.EVNumber.two, "", GAME_LIST, ip);
    }

    //新增信息模板
    public void addMsgModelLog(MsgModel msgModel, String userName, String ip) {
        String afterChange = msgModel.getName();
        addFundAccountLog(null, userName, "新增" + MESSAGE_TEMPLATE,
                "", afterChange, userName, Constants.EVNumber.two, "", MESSAGE_TEMPLATE, ip);
    }

    //修改信息模板
    public void editMsgModelLog(MsgModel msgModel, String userName, String ip) {
        String afterChange = msgModel.getName();
        addFundAccountLog(null, userName, "修改" + MESSAGE_TEMPLATE,
                "", afterChange, userName, Constants.EVNumber.two, "", MESSAGE_TEMPLATE, ip);
    }

    //删除信息模板
    public void deleteMsgModelLog(MsgModel msgModel, String userName, String ip) {
        String afterChange = msgModel.getName();
        addFundAccountLog(null, userName, "删除" + MESSAGE_TEMPLATE,
                "", afterChange, userName, Constants.EVNumber.two, "", MESSAGE_TEMPLATE, ip);
    }

    //修改信息模板状态
    public void updateMsgModelStatusLog(MsgModel msgModel, String userName, String ip) {
        String afterChange = msgModel.getState() == 1 ? "开启" : "禁用";
        addFundAccountLog(null, userName, "修改" + MESSAGE_TEMPLATE + msgModel.getName() + GAME_LIST_STATUS,
                "", afterChange, userName, Constants.EVNumber.two, "", MESSAGE_TEMPLATE, ip);
    }

    //新增公告
    public void addOprNoticelog(OprNotice oprNotice, String userName, String ip) {
        String afterChange = oprNotice.getNoticeTitle();
        addFundAccountLog(null, userName, "新增" + NOTICE_MESSAGE,
                "", afterChange, userName, Constants.EVNumber.two, "", NOTICE_MESSAGE, ip);
    }

    //修改公告通知
    public void editOprNoticelog(OprNotice oprNotice, String userName, String ip) {
        String afterChange = oprNotice.getNoticeTitle();
        addFundAccountLog(null, userName, "修改" + NOTICE_MESSAGE,
                "", afterChange, userName, Constants.EVNumber.two, "", NOTICE_MESSAGE, ip);
    }

    //删除公告通知
    public void deleteOprNoticelog(OprNotice oprNotice, String userName, String ip) {
        String afterChange = oprNotice.getNoticeTitle();
        addFundAccountLog(null, userName, "删除" + NOTICE_MESSAGE,
                "", afterChange, userName, Constants.EVNumber.two, "", NOTICE_MESSAGE, ip);
    }

    //修改公告通知状态
    public void updateNoticeStatus(OprNotice oprNotice, String userName, String ip) {
        String afterChange = oprNotice.getAvailable() == 1 ? "开启" : "禁用";
        addFundAccountLog(null, userName, "修改" + NOTICE_MESSAGE + oprNotice.getNoticeTitle() + GAME_LIST_STATUS,
                "", afterChange, userName, Constants.EVNumber.two, "", NOTICE_MESSAGE, ip);
    }

    //新增广告
    public void addOprAdvLog(OprAdv oprAdv, String userName, String ip) {
        String afterChange = oprAdv.getTitle();
        addFundAccountLog(null, userName, "新增" + OPR_ADV,
                "", afterChange, userName, Constants.EVNumber.two, "", OPR_ADV_MANAGER, ip);
    }

    //新增帮助中心分类
    public void addHelpCategoryLog(OprHelpCategory oprHelpCategory, String userName, String ip) {
        String afterChange = oprHelpCategory.getHelpCategoryName();
        addFundAccountLog(null, userName, "新增" + OPR_HELP_CATEGORY,
                "", afterChange, userName, Constants.EVNumber.two, "", OPR_HELP_MANAGER, ip);
    }

    //新增帮助中心标题
    public void addHelpTitleLog(OprHelpTitle oprHelpTitle, String userName, String ip) {
        String afterChange = oprHelpTitle.getTitleName();
        addFundAccountLog(null, userName, "新增" + OPR_HELP_TITLE,
                "", afterChange, userName, Constants.EVNumber.two, "", OPR_HELP_MANAGER, ip);
    }

    //新增帮助中心内容
    public void addHelpContentLog(OprHelpContent oprHelpContent, String userName, String ip) {
        String afterChange = oprHelpContent.getContentTitle();
        addFundAccountLog(null, userName, "新增" + OPR_HELP_CONTENT,
                "", afterChange, userName, Constants.EVNumber.two, "", OPR_HELP_MANAGER, ip);
    }

    //修改广告
    public void editOprAdvLog(OprAdv oprAdv, String userName, String ip) {
        String afterChange = oprAdv.getTitle();
        addFundAccountLog(null, userName, "修改" + OPR_ADV,
                "", afterChange, userName, Constants.EVNumber.two, "", OPR_ADV_MANAGER, ip);
    }

    //修改帮助中心分类
    public void editHelpCategoryLog(OprHelpCategory oprHelpCategory, String userName, String ip) {
        String afterChange = oprHelpCategory.getHelpCategoryName();
        addFundAccountLog(null, userName, "修改" + OPR_HELP_CATEGORY,
                "", afterChange, userName, Constants.EVNumber.two, "", OPR_HELP_MANAGER, ip);
    }

    //修改帮助中心标题
    public void editHelpTitleLog(OprHelpTitle oprHelpTitle, String userName, String ip) {
        String afterChange = oprHelpTitle.getTitleName();
        addFundAccountLog(null, userName, "修改" + OPR_HELP_TITLE,
                "", afterChange, userName, Constants.EVNumber.two, "", OPR_HELP_MANAGER, ip);
    }

    //修改帮助中心内容
    public void editHelpContentLog(OprHelpContent oprHelpContent, String userName, String ip) {
        String afterChange = oprHelpContent.getContentTitle();
        addFundAccountLog(null, userName, "修改" + OPR_HELP_CONTENT,
                "", afterChange, userName, Constants.EVNumber.two, "", OPR_HELP_MANAGER, ip);
    }

    //删除广告
    public void deleteOprAdvLog(OprAdv oprAdv, String userName, String ip) {
        String afterChange = oprAdv.getTitle();
        addFundAccountLog(null, userName, "删除" + OPR_ADV,
                "", afterChange, userName, Constants.EVNumber.two, "", OPR_ADV_MANAGER, ip);
    }

    //删除帮助中心分类
    public void deleteCategoryLog(OprHelpCategory oprHelpCategory, String userName, String ip) {
        String afterChange = oprHelpCategory.getHelpCategoryName();
        addFundAccountLog(null, userName, "删除" + OPR_HELP_CATEGORY,
                "", afterChange, userName, Constants.EVNumber.two, "", OPR_HELP_MANAGER, ip);
    }

    //删除帮助中心标题
    public void deleteTitleLog(OprHelpTitle oprHelpTitle, String userName, String ip) {
        String afterChange = oprHelpTitle.getTitleName();
        addFundAccountLog(null, userName, "删除" + OPR_HELP_TITLE,
                "", afterChange, userName, Constants.EVNumber.two, "", OPR_HELP_MANAGER, ip);
    }

    //删除帮助中心标题
    public void deleteContentLog(OprHelpContent oprHelpContent, String userName, String ip) {
        String afterChange = oprHelpContent.getContentTitle();
        addFundAccountLog(null, userName, "删除" + OPR_HELP_CONTENT,
                "", afterChange, userName, Constants.EVNumber.two, "", OPR_HELP_MANAGER, ip);
    }

    //广告禁用启用
    public void updateOprAdvStatusLog(OprAdv oprAdv, String userName, String ip) {
        String afterChange = oprAdv.getAvailable() == 1 ? "开启" : "禁用";
        addFundAccountLog(null, userName, "修改" + OPR_ADV + oprAdv.getTitle() + GAME_LIST_STATUS,
                "", afterChange, userName, Constants.EVNumber.two, "", OPR_ADV_MANAGER, ip);
    }

    //帮助中心分类启用禁用
    public void updateCategoryStatusLog(OprHelpCategory oprHelpCategory, String userName, String ip) {
        String afterChange = oprHelpCategory.getAvailable() == 0 ? "开启" : "禁用";
        addFundAccountLog(null, userName, "修改" + OPR_HELP_CATEGORY + oprHelpCategory.getHelpCategoryName() + GAME_LIST_STATUS,
                "", afterChange, userName, Constants.EVNumber.two, "", OPR_HELP_MANAGER, ip);
    }


    //支付配置新增银行卡
    public void addCompanyDepositLog(SysDeposit deposit, String userName, String ip) {
        String afterChange = deposit.getBankName();
        addFundAccountLog(null, userName, "新增" + ACCOUNT_BANK_TWO,
                "", afterChange, userName, Constants.EVNumber.two, "", PAY_LIST, ip);
    }

    //支付配置修改银行状态
    public void updateCompanyDepositStatusLog(SysDeposit deposit, String userName, String ip) {
        String afterChange = deposit.getAvailable() == 1 ? "启用" : "禁用";
        addFundAccountLog(null, userName, "修改" + ACCOUNT_BANK_STATUS,
                "", afterChange, userName, Constants.EVNumber.two, "", PAY_LIST, ip);
    }

    //支付配置修改银行
    public void updateCompanyDepositLog(SysDeposit deposit, String userName, String ip) {
        String afterChange = deposit.getBankName();
        addFundAccountLog(null, userName, "修改银行卡",
                "", afterChange, userName, Constants.EVNumber.two, "", PAY_LIST, ip);
    }

    //支付配置修改银行--支付分配
    public void updateCompanyDepositLog2(SysDeposit deposit, String userName, String ip) {
        String afterChange = deposit.getBankName();
        addFundAccountLog(null, userName, "修改银行卡",
                "", afterChange, userName, Constants.EVNumber.two, "", UPDATE_PAY_SET, ip);
    }

    //支付配置删除银行
    public void deleteCompanyDepositLog(SysDeposit deposit, String userName, String ip) {
        String afterChange = deposit.getBankName();
        addFundAccountLog(null, userName, "删除" + ACCOUNT_BANK_TWO,
                "", afterChange, userName, Constants.EVNumber.two, "", PAY_LIST, ip);
    }

    //新增自动入款支付方式
    public void addAutoDepositLog(SetBacicFastPay fastPay, String userName, String ip) {
        String afterChange = fastPay.getName();
        addFundAccountLog(null, userName, "新增自动入款支付",
                "", afterChange, userName, Constants.EVNumber.two, "", PAY_LIST, ip);
    }

    //修改自动入款支付方式状态
    public void updateAutoDepositStatusLog(SetBacicFastPay fastPay, String userName, String ip) {
        String afterChange = fastPay.getAvailable() == 1 ? "启用" : "禁用";
        addFundAccountLog(null, userName, "修改自动入款支付" + fastPay.getName() + GAME_LIST_STATUS,
                "", afterChange, userName, Constants.EVNumber.two, "", PAY_LIST, ip);
    }

    //修改自动入款支付方式
    public void updateAutoDepositLog(SetBacicFastPay fastPay, String userName, String ip) {
        String afterChange = fastPay.getName();
        addFundAccountLog(null, userName, "修改自动入款支付",
                "", afterChange, userName, Constants.EVNumber.two, "", PAY_LIST, ip);
    }

    //删除自动入款支付方式
    public void deleteAutoDepositLog(SetBacicFastPay fastPay, String userName, String ip) {
        String afterChange = fastPay.getName();
        addFundAccountLog(null, userName, "删除自动入款支付",
                "", afterChange, userName, Constants.EVNumber.two, "", PAY_LIST, ip);
    }

    //新增线上支付
    public void addOnlinePayLog(SetBacicOnlinepay onlinepay, String userName, String ip) {
        String afterChange = onlinepay.getName();
        addFundAccountLog(null, userName, "新增线上支付",
                "", afterChange, userName, Constants.EVNumber.two, "", PAY_LIST, ip);
    }

    //修改线上支付状态
    public void updateOnlinePayStatusLog(SetBacicOnlinepay onlinepay, String userName, String ip) {
        String afterChange = onlinepay.getAvailable() == 1 ? "启用" : "禁用";
        addFundAccountLog(null, userName, "修改线上支付" + onlinepay.getName() + GAME_LIST_STATUS,
                "", afterChange, userName, Constants.EVNumber.two, "", PAY_LIST, ip);
    }

    //修改线上支付跳转方式
    public void updateOnlinePayJumpLog(SetBacicOnlinepay onlinepay, String userName, String ip) {
        String afterChange = onlinepay.getIsJump() == 1 ? "外跳" : "内跳";
        addFundAccountLog(null, userName, "修改线上支付" + onlinepay.getName() + GAME_LIST_JUMP,
                "", afterChange, userName, Constants.EVNumber.two, "", PAY_LIST, ip);
    }

    //修改线上支付
    public void updateOnlinePayLog(SetBacicOnlinepay onlinepay, String userName, String ip) {
        String afterChange = onlinepay.getName();
        addFundAccountLog(null, userName, "修改了线上支付",
                "", afterChange, userName, Constants.EVNumber.two, "", PAY_LIST, ip);
    }

    //修改线上支付--支付分配
    public void updateOnlinePayLog2(SetBacicOnlinepay onlinepay, String userName, String ip) {
        String afterChange = onlinepay.getName();
        addFundAccountLog(null, userName, "修改了线上支付",
                "", afterChange, userName, Constants.EVNumber.two, "", UPDATE_PAY_SET, ip);
    }

    //删除线上支付
    public void deleteOnlinePayLog(SetBacicOnlinepay onlinepay, String userName, String ip) {
        String afterChange = onlinepay.getName();
        addFundAccountLog(null, userName, "删除线上支付",
                "", afterChange, userName, Constants.EVNumber.two, "", PAY_LIST, ip);
    }

    //新增线上支付
    public void addSysQrCodeLog(SysQrCode sysQrCode, String userName, String ip) {
        String afterChange = sysQrCode.getName();
        addFundAccountLog(null, userName, "新增个人二维码支付",
                "", afterChange, userName, Constants.EVNumber.two, "", PAY_LIST, ip);
    }

    //修改线上支付状态
    public void updateSysQrCodeStatusLog(SysQrCode sysQrCode, String userName, String ip) {
        String afterChange = sysQrCode.getAvailable() == 1 ? "启用" : "禁用";
        addFundAccountLog(null, userName, "修改个人二维码支付" + sysQrCode.getName() + GAME_LIST_STATUS,
                "", afterChange, userName, Constants.EVNumber.two, "", PAY_LIST, ip);
    }

    //修改个人二维码
    public void updateSysQrCodeLog(SysQrCode sysQrCode, String userName, String ip) {
        String afterChange = sysQrCode.getName();
        addFundAccountLog(null, userName, "修改个人二维码支付",
                "", afterChange, userName, Constants.EVNumber.two, "", PAY_LIST, ip);
    }

    //修改个人二维码--支付分配
    public void updateSysQrCodeLog2(SysQrCode sysQrCode, String userName, String ip) {
        String afterChange = sysQrCode.getName();
        addFundAccountLog(null, userName, "修改个人二维码支付",
                "", afterChange, userName, Constants.EVNumber.two, "", UPDATE_PAY_SET, ip);
    }


    //删除线上支付
    public void deleteSysQrCodeLog(SysQrCode sysQrCode, String userName, String ip) {
        String afterChange = sysQrCode.getName();
        addFundAccountLog(null, userName, "删除个人二维码支付",
                "", afterChange, userName, Constants.EVNumber.two, "", PAY_LIST, ip);
    }

    //系统设置站点设置
    public void updateSysSiteSetLog(StationSet stationSet, String userName, String ip) {
        addFundAccountLog(null, userName, "编辑站点设置",
                "", "", userName, Constants.EVNumber.two, "", SYSTEM_COMFIG, ip);
    }

    //系统设置邮件设置
    public void updateSysMailSetLog(MailSet mailSet, String userName, String ip) {
        addFundAccountLog(null, userName, "编辑邮件设置",
                "", "", userName, Constants.EVNumber.two, "", SYSTEM_COMFIG, ip);
    }


    //系统设置注册设置
    public void updateSysRegisterSetLog(RegisterSet registerSet, String userName, String ip) {
        addFundAccountLog(null, userName, "编辑注册设置",
                "", "", userName, Constants.EVNumber.two, "", SYSTEM_COMFIG, ip);
    }
    //系统设置注ai推荐设置
    public void updateAiRecommendSetLog(AiRecommend aiRecommend, String userName, String ip) {
        String afterChange = aiRecommend.getIsEnble() == 0 ? "启用" : "禁用";
        addFundAccountLog(null, userName, "AI推荐设置",
                "", afterChange, userName, Constants.EVNumber.two, "", SYSTEM_COMFIG, ip);
    }
    //系统设置注册设置_是否允许前台注册控制
    public void saveAccWebRegSetLog(RegisterSet registerSet, String userName, String ip) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = SYSTEM_COMFIG_REG;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = SYSTEM_COMFIG;
        Integer oldStatus = registerSet.getAccWebRegister().equals(1) ? 0 : 1;
        // 业务逻辑日志
        afterChange = ":" + SYSTEM_COMFIG_REG_WEBREGSET + ":" + Constants.accWebRegister.get(oldStatus) + ">" + Constants.accWebRegister.get(registerSet.getAccWebRegister());
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    //系统设置推广设置
    public void updateSysPromotionSetLog(PromotionSet promotionSet, String userName, String ip) {
        addFundAccountLog(null, userName, "编辑推广设置",
                "", "", userName, Constants.EVNumber.two, "", SYSTEM_COMFIG, ip);
    }

    //系统设置其他设置
    public void updateSysOtherSetLog(PaySet domain, String userName, String ip) {
        addFundAccountLog(null, userName, "编辑其他设置",
                "", "", userName, Constants.EVNumber.two, "", SYSTEM_COMFIG, ip);
    }

    //系统设置出款设置
    public void updateSysPayAutomaticSetLog(PaySet domain, String userName, String ip) {
        addFundAccountLog(null, userName, "编辑出款设置",
                "", "", userName, Constants.EVNumber.two, "", SYSTEM_COMFIG, ip);
    }
    
    /** 系统设置合营计划图 */
    public void updateSysVenturePlanSetSetLog(String userName, String ip) {
    	addFundAccountLog(null, userName, "编辑合营计划图",
    			"", "", userName, Constants.EVNumber.two, "", SYSTEM_COMFIG, ip);
    }

    //系统设置好友转账设置
    public void updateSysFriendTransSetLog(PaySet domain, String userName, String ip) {
        addFundAccountLog(null, userName, "编辑好友转账设置",
                "", "", userName, Constants.EVNumber.two, "", SYSTEM_COMFIG, ip);
    }

    //系统设置佣金设置
    public void updateDomainAvailableLog(AgyDomain paySet, String userName, String ip) {
        addFundAccountLog(null, userName, "编辑佣金设置",
                "", "", userName, Constants.EVNumber.two, "", SYSTEM_COMFIG, ip);
    }

    //系统设置代理注册设置
    public void updateAgentRegisterSetLog(SettingAgentDto settingAgentDto, String userName, String ip) {
        addFundAccountLog(null, userName, "编辑代理注册设置",
                "", "", userName, Constants.EVNumber.two, "", SYSTEM_COMFIG, ip);
    }

    //系统设置代理注册设置
    public void updateAgentCommissionLog(SettingAgentDto settingAgentDto, String userName, String ip) {
        addFundAccountLog(null, userName, "编辑代理佣金设置",
                "", "", userName, Constants.EVNumber.two, "", SYSTEM_COMFIG, ip);
    }

    //新增系统角色
    public void addSysRoleLog(SysRoleEntity sysRoleEntity, String userName, String ip) {
        String afterChange = sysRoleEntity.getRoleName();
        addFundAccountLog(null, userName, "新增系统角色",
                "", afterChange, userName, Constants.EVNumber.two, "", ROLE_LIST, ip);
    }

    //修改系统角色
    public void updateSysRoleLog(SysRoleEntity sysRoleEntity, String userName, String ip) {
        String afterChange = sysRoleEntity.getRoleName();
        addFundAccountLog(null, userName, "修改系统角色",
                "", afterChange, userName, Constants.EVNumber.two, "", ROLE_LIST, ip);
    }

    //修改角色状态开启禁用
    public void updateSysRoleStatusLog(SysRoleEntity sysRoleEntity, String userName, String ip) {
        String afterChange = sysRoleEntity.getIsEnable() == 1 ? "启用" : "禁用";
        addFundAccountLog(null, userName, "修改系统角色" + sysRoleEntity.getRoleName() + GAME_LIST_STATUS,
                "", afterChange, userName, Constants.EVNumber.two, "", ROLE_LIST, ip);
    }

    //删除系统角色
    public void deleteSysRoleLog(SysRoleEntity sysRoleEntity, String userName, String ip) {
        String afterChange = sysRoleEntity.getRoleName();
        addFundAccountLog(null, userName, "删除系统角色",
                "", afterChange, userName, Constants.EVNumber.two, "", ROLE_LIST, ip);
    }

    //新增用户日志
    public void addSysUserLog(SysUserEntity user, String userName, String ip) {
        String afterChange = user.getUsername();
        addFundAccountLog(null, userName, "新增系统用户",
                "", afterChange, userName, Constants.EVNumber.two, "", USER_MANAGER, ip);
    }

    //修改系统用户登录密码
    public void updateSysUserPwdLog(SysUserEntity user, String userName, String ip) {
        String afterChange = user.getUsername() + "登录密码";
        addFundAccountLog(null, userName, "修改系统用户",
                "", afterChange, userName, Constants.EVNumber.two, "", USER_MANAGER, ip);
    }

    //修改系统用户安全密码
    public void updateSysUserSafePwdLog(SysUserEntity user, String userName, String ip) {
        String afterChange = user.getUsername() + "安全密码";
        addFundAccountLog(null, userName, "修改系统用户",
                "", afterChange, userName, Constants.EVNumber.two, "", USER_MANAGER, ip);
    }

    //修改系统用户信息
    public void updateSysUserInfoLog(SysUserEntity user, String userName, String ip) {
        String afterChange = user.getUsername() + "的基本信息";
        addFundAccountLog(null, userName, "修改系统用户",
                "", afterChange, userName, Constants.EVNumber.two, "", USER_MANAGER, ip);
    }

    //修改系统用户状态
    public void updateSysUserStatusLog(SysUserEntity user, String userName, String ip) {
        String afterChange = user.getStatus() == 1 ? "开启" : "禁用";
        addFundAccountLog(null, userName, "修改系统用户" + user.getUsername() + ACCOUNT_BONUS_STATUS_INFO_TWO,
                "", afterChange, userName, Constants.EVNumber.two, "", USER_MANAGER, ip);
    }

    //删除系统用户状态
    public void deleteSysUserLog(SysUserEntity user, String userName, String ip) {
        String afterChange = user.getUsername();
        addFundAccountLog(null, userName, "删除系统用户",
                "", afterChange, userName, Constants.EVNumber.two, "", USER_MANAGER, ip);
    }

    // 登录
    public void authenticatorLoginLog(String userName, String ip) {
        addMbrAccountLog(null, userName, "登录系统成功",
                "", "", userName, Constants.EVNumber.one, USER_LOGIN, ip);
    }

    // 登出
    public void logoutLog(String userName, String ip) {
        addMbrAccountLog(null, userName, "登出系统成功",
                "", "", userName, Constants.EVNumber.one, USER_LOGOUT, ip);
    }

    private void addFundAccountLog(Integer accountId, String loginName, String item, String beforeChange,
                                   String afterChange, String userName, int operatorType, String orderNo, String moduleName, String ip) {
        AccountLogDto accountLogDto = new AccountLogDto();
        accountLogDto.setStatus(ACCOUNT_SUCCEED);
        accountLogDto.setItem(item);
        accountLogDto.setBeforeChange(beforeChange);
        accountLogDto.setAfterChange(afterChange);
        accountLogDto.setOperatorUser(userName);
        accountLogDto.setOperatorType(operatorType);

        MbrAccountLog mbrAccountLog = new MbrAccountLog();
        if (nonNull(accountId)) {
            mbrAccountLog.setAccountId(accountId);
        }
        mbrAccountLog.setLoginName(loginName);
        if (!"".equals(orderNo) && !"null".equals(orderNo)) {
            mbrAccountLog.setOrderNo(orderNo);
        } else {
            mbrAccountLog.setOrderNo(String.valueOf(new SnowFlake().nextId()));
        }
        mbrAccountLog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        mbrAccountLog.setContent(JSON.toJSONString(accountLogDto));
        mbrAccountLog.setModuleName(moduleName);
        mbrAccountLog.setIp(ip);
        accountLogMapper.insert(mbrAccountLog);
    }

    // 会员列表--锁定会员星级
    public void updateActivityLevel(MbrAccount mbrAccount, MbrAccount mbrAccountOld) {
        // 参数准备
        Integer accountId = mbrAccount.getId();
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = ACCOUNT_INFO_OT_ONE + mbrAccountOld.getLoginName();  // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = mbrAccount.getLoginName();
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = MEMBER_LIST;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = ACCOUNT_LEVEL_LOCK_STATUS + ":" + (mbrAccount.getIsActivityLock() == 0 ? "锁定" : "解锁") + " > " + (mbrAccountOld.getIsActivityLock() == 0 ? "锁定" : "解锁");
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 会员列表--批量调级
    public void batchUpdateActLevel(MbrActivityLevel newLevel, List<MbrAccount> oldMbrList, String memo) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = ACCOUNT_LEVEL_UPDATE_BATCH;   // item+afterChange为前端“操作描述”
        String afterChange = ": ";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = MEMBER_LIST;
        String ip = getUserIp();
        // 业务逻辑日志
        for (MbrAccount fs : oldMbrList) {
            afterChange += fs.getLoginName() + " " + fs.getAccountLevel() + "级"
                    + ">" + newLevel.getAccountLevel() + "级" + ";";
        }
        if (StringUtil.isNotEmpty(memo)) {
            afterChange += "备注：" + memo;
        }
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 活动等级--周期统计规则
    public void setActLevelStaticsRule(String sysValueNew, String sysValueOld, String newDes, String oldDes) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = ACCOUNT_LEVEL_STATICS_RULE;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = ACCOUNT_LEVEL;
        String ip = getUserIp();
        // 业务逻辑日志
        // 活动等级周期统计规则 0无限期，1按日，2按周，3按月
        afterChange = ":" + Constants.activityStaticsRule.get(sysValueOld) + " > " + Constants.activityStaticsRule.get(sysValueNew);
        // 活动等级周期统计规则 前端显示描述
        afterChange += ";前台说明：" + oldDes + " > " + newDes;

        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 活动等级--开启/关闭自动晋升
    public void setAutomatic(String sysValueNew, String sysValueOld) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = ACCOUNT_LEVEL_AUTOMATI;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = ACCOUNT_LEVEL;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = ":" + ("1".equals(sysValueOld) ? "开启" : "关闭") + " > " + ("1".equals(sysValueNew) ? "开启" : "关闭");
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 活动等级--编辑
    public void updateMbrActivityLevel(MbrActivityLevel activityLevelNew, MbrActivityLevel activityLevelOld) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = ACCOUNT_LEVEL_EDIT;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = ACCOUNT_LEVEL;
        String ip = getUserIp();
        // 业务逻辑日志 TODO

        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 活动规则--新增
    public void saveActivityRule(OprActRule actRule) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = ACTIVITY_RULE_SAVE;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = ACTIVITY_RULE;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = ":" + actRule.getRuleName();
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 活动规则--编辑
    public void updateActivityRule(OprActRule actRule) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = ACTIVITY_RULE_UPDATE;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = ACTIVITY_RULE;
        String ip = getUserIp();
        // 业务逻辑日志 TODO
        afterChange = ":" + actRule.getRuleName();
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 活动规则--删除
    public void deleteDisableRule(OprActRule actRule) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = ACTIVITY_RULE_DELETE;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = ACTIVITY_RULE;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = ":" + actRule.getRuleName();
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 活动规则--状态变更
    public void updateAvailableActivityRule(OprActRule actRule, OprActRule actRuleOld) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = ACTIVITY_RULE_UPDATE_STATUS;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = ACTIVITY_RULE;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = ":" + actRuleOld.getRuleName();
        afterChange += ":" + (actRuleOld.getAvailable() == 0 ? "禁用" : "开启") + " > " + (actRule.getAvailable() == 0 ? "禁用" : "开启");
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 活动类别--新增
    public void saveOprActCat(OprActCat oprActCat) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = ACTIVITY_CAT_SAVE;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = ACTIVITY_CAT;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = ":" + oprActCat.getCatName();

        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 活动类别--编辑
    public void updateOprActCat(OprActCat oprActCat) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = ACTIVITY_CAT_UPDATE;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = ACTIVITY_CAT;
        String ip = getUserIp();
        // 业务逻辑日志 TODO
        afterChange = ":" + oprActCat.getCatName();

        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 返水列表--审核
    public void waterActivityAudit(OprActBonus actBonus, OprActBonus oldBonus, String activityName) {
        // 参数准备
        Integer accountId = actBonus.getAccountId();
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = OPR_BONUS_WATER_AUDIT;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = Constants.bonusActivityAudit.get(oldBonus.getStatus());
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = OPR_BONUS_WATER;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = ":" + "会员" + actBonus.getLoginName() + "的" + activityName
                + Constants.bonusActivityAudit.get(actBonus.getStatus());

        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 红利列表--新增
    public void saveBonus(OprActBonus bonus) {
        // 参数准备
        Integer accountId = bonus.getAccountId();
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = OPR_BONUS_SAVE;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = OPR_BONUS;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = ":" + "会员" + bonus.getLoginName();

        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 红利列表--审核
    public void bonusActivityAudit(OprActBonus actBonus, OprActBonus oldBonus, String activityName) {
        // 参数准备
        Integer accountId = actBonus.getAccountId();
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = OPR_BONUS_AUDIT;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = Constants.bonusActivityAudit.get(oldBonus.getStatus());
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = OPR_BONUS;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = ":" + "会员" + actBonus.getLoginName() + "的" + activityName
                + Constants.bonusActivityAudit.get(actBonus.getStatus());

        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 红利列表--调整金额
    public void activityModifyAmount(OprActBonus actBonus, OprActBonus oldBonus) {
        // 参数准备
        Integer accountId = actBonus.getAccountId();
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = OPR_BONUS_MODIFY_AMOUNT;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = Constants.bonusActivityAudit.get(oldBonus.getStatus());
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = OPR_BONUS;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = ":" + "会员" + actBonus.getLoginName() + "的红利调整:赠送金额" + oldBonus.getBonusAmount() + ">" + actBonus.getBonusAmount()
                + "; 流水金额" + oldBonus.getAuditAmount() + ">" + actBonus.getAuditAmount();

        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    public void updatePaySetLog(Integer addOrDel, String groupName, String payName) {
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = UPDATE_PAY_SET;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = UPDATE_PAY_SET;
        String ip = getUserIp();
        // 业务逻辑日志
        if (Constants.EVNumber.zero == addOrDel) {
            afterChange = ":" + "删除" + groupName + payName;
        } else if (Constants.EVNumber.one == addOrDel) {
            afterChange = ":" + "添加" + groupName + payName;
        } else {
            afterChange = ":" + "修改" + groupName + "支付顺序";
        }

        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);

    }

    // 代理列表--新增总代/子代理
    public void agyAccountSaveLog(AgentAccount agentAccount) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = agentAccount.getParentId().equals(Constants.EVNumber.zero) ? AGENT_LIST_SAVE_PARENT : AGENT_LIST_SAVE_CHILD;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = AGENT_LIST;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = ":" + agentAccount.getAgyAccount();

        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 代理列表--总代/子代理 状态变更
    public void agyAccountAvailableLog(AgentAccount agentAccount) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = agentAccount.getParentId().equals(Constants.EVNumber.zero) ? AGENT_LIST_UPDATE_STATUS_PARENT : AGENT_LIST_UPDATE_STATUS_CHILD;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = AGENT_LIST;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = ":" + agentAccount.getAgyAccount() + ":状态 " + (agentAccount.getAvailable().equals(Constants.EVNumber.zero) ? "开启" : "禁用") + " > "
                + (agentAccount.getAvailable().equals(Constants.EVNumber.zero) ? "禁用" : "开启");

        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 代理列表--编辑代理
    public void agyAccountUpdateLog(AgentAccount newAgentAccount, AgentAccount oldAgentAccount) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = AGENT_LIST_UPDATE;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = AGENT_LIST;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = ":" + newAgentAccount.getAgyAccount()
                + ": 真实姓名 " + oldAgentAccount.getRealName() + " > " + newAgentAccount.getRealName()
                + "；联系方式 " + oldAgentAccount.getMobile() + " > " + newAgentAccount.getMobile()
                + "；备注 " + oldAgentAccount.getMemo() + " > " + newAgentAccount.getMemo();

        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 代理列表--重置代理登录密码/资金密码
    public void agyAccountPasswordLog(AgentAccount agentAccount) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = AGENT_LIST_CHANGE;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = AGENT_LIST;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = agentAccount.getAgyAccount() + (StringUtils.isNotEmpty(agentAccount.getAgyPwd()) ? "登录密码" : "资金密码");
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 代理列表/代理后台--删除代理
    public void agyAccountDeleteLog(AgentAccount agentAccount) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = AGENT_LIST_DELETE;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = AGENT_LIST;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = ":" + agentAccount.getAgyAccount();
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 代理域名--新增域名
    public void agyDomainSaveLog(AgyDomain agyDomain) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "代理" + agyDomain.getAgyAccount() + AGENT_DOMAIN_ADD;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = AGENT_DOMAIN;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = ":" + agyDomain.getDomainUrl();
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 代理域名--审核推广域名
    public void agyDomainAuditLog(AgyDomain agyDomain) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = AGENT_DOMAIN_AUDIT;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = AGENT_DOMAIN;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = ":" + agyDomain.getDomainUrl() + ":" + (agyDomain.getStatus().equals(Constants.EVNumber.zero) ? "拒绝" : "通过");
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 活动规则--编辑
    public void updateWaterSelf(OprActRule actRule) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = ACTIVITY_RULE_UPDATE;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = ACTIVITY_RULE;
        String ip = getUserIp();
        StringBuffer buffer = new StringBuffer();
        buffer.append(":");
        buffer.append(actRule.getRuleName());
        buffer.append(";洗码状态->");
        buffer.append(actRule.getIsSelfHelp() == 1 ? "开启" : "关闭");
        buffer.append(";自助申请状态->");
        buffer.append(actRule.getIsSelfHelpShow() == 1 ? "开启" : "关闭");
        buffer.append(";是否限制金额->");
        buffer.append(actRule.getIsLimit() == 1 ? "开启" : "关闭");
        buffer.append(";限制金额->");
        buffer.append(actRule.getMinAmount());

        afterChange = buffer.toString();
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }
    
    
    public void addBlackListLog(Integer accountType, String account){
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = ACTIVITY_RULE_UPDATE;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = ACTIVITY_RULE;
        String ip = getUserIp();
        StringBuffer buffer = new StringBuffer();
        buffer.append("新增黑名单->");

        if(accountType == Constants.EVNumber.one){
            buffer.append("代理账号:");
        } else if(accountType == Constants.EVNumber.zero){
            buffer.append("玩家账号:");
        }
        buffer.append(account);
        afterChange = buffer.toString();
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }


    public void delBlackListLog(Integer accountType, String account){
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = ACTIVITY_RULE_UPDATE;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = ACTIVITY_RULE;
        String ip = getUserIp();
        StringBuffer buffer = new StringBuffer();
        buffer.append("删除黑名单->");

        if(accountType == Constants.EVNumber.one){
            buffer.append("代理账号:");
        } else if(accountType == Constants.EVNumber.zero){
            buffer.append("玩家账号:");
        }
        buffer.append(account);
        afterChange = buffer.toString();
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 导出日志
    public void downloadFileLog(String fileName) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = DOWNLOAD_FILE;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = DOWNLOAD_FILE;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = ":" + fileName;

        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 修改语音平台
    public void outCallSetLog(String outCallPlatform) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = SYSTEM_OUT_CALLPLATE_SET;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = SYSTEM_COMFIG;
        String ip = getUserIp();
        // 业务逻辑日志
        if ("1".equals(outCallPlatform)) {
            afterChange = ":blink";
        } else {
            afterChange = ":rowave";
        }

        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 获得user实体
    protected SysUserEntity getUser() {
        if (Objects.nonNull(SecurityUtils.getSubject().getPrincipal())) {
            return (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
        }
        return new SysUserEntity();

    }

    // 获得ip地址
    protected String getUserIp() {
        String ip = "";
        if (RequestContextHolder.getRequestAttributes() != null) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            if (request != null) {
                ip = CommonUtil.getIpAddress(request);
            }
        }
        return ip;
    }

    public void accountMassTextingLog(List<String> loginNames, String content) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = ACCOUNT_MASSTEXTING;   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = ACCOUNT_MASSTEXTING;
        String ip = getUserIp();
        // 业务逻辑日志
//            afterChange =  "发送内容:" + content + " >>> 发送人员+ " + JSON.toJSON(loginNames);
        afterChange = "发送内容:" + content;
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    public void updateAccountAgent(MbrAccount account, String agyAccount, String username, String ip) {
        AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(account.getCagencyId());
        String str = account.getLoginName() + "原代理:" + agentAccount.getAgyAccount() + " >>> 修改为" + agyAccount;
        addMbrAccountLog(null, username, ACCOUNT_AGRNT,
                "", str, username, Constants.EVNumber.two, ACCOUNT_AGRNT, ip);
    }

    public void updateSupLoginName(MbrAccount account, String supLoginName, String username, String ip, Integer parentid) {
        String strname = "";
        if (nonNull(parentid)) {
            MbrAccount account1 = accountMapper.selectByPrimaryKey(parentid);
            strname = nonNull(account1) ? account1.getLoginName() : "";
        }
        strname = StringUtils.isEmpty(strname) ? "无" : strname;
        String str = account.getLoginName() + "原推荐人:" + strname + " >>> 修改为" + supLoginName;
        addMbrAccountLog(null, username, ACCOUNT_SUPLOGNANAME,
                "", str, username, Constants.EVNumber.two, ACCOUNT_SUPLOGNANAME, ip);
    }

    // 任务中心相关日志
    // 任务开启/关闭
    public void taskUpdateAvailableLog(TaskConfig taskConfig) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = TASK_ACCOUNT;
        String ip = getUserIp();
        String financialCodeName = taskMapper.financialCodeName(taskConfig.getFinancialCode());
        // 业务逻辑日志
        afterChange = (Integer.valueOf(Constants.EVNumber.one).equals(taskConfig.getAvailable()) ? "开启" : "关闭")
                + financialCodeName;
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 任务编辑
    public void updateTaskRuleLog(TaskConfig taskConfig) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = TASK_ACCOUNT;
        String ip = getUserIp();

        String financialCodeName = taskMapper.financialCodeName(taskConfig.getFinancialCode());
        // 业务逻辑日志
        afterChange = "编辑" + financialCodeName;
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 任务中心--删除黑名单
    public void deletTaskBlackListLog(OprActBlacklist black) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = TASK_ACCOUNT;
        String ip = getUserIp();

        String financialCodeName = "";
        if ("QD".equals(black.getTmplCode())) {
            financialCodeName = "签到";
        } else if ("XS".equals(black.getTmplCode())) {
            financialCodeName = "限时活动";
        } else if ("TJ".equals(black.getTmplCode())) {
            financialCodeName = "好友推荐";
        } else if ("SJ".equals(black.getTmplCode())) {
            financialCodeName = "限时活动";
        } else if ("ZL".equals(black.getTmplCode())) {
            financialCodeName = "完善资料";
        } else if ("DS".equals(black.getTmplCode())) {
            financialCodeName = "定时奖励";
        } else if (HR_TASK_ACTIVITY.equals(black.getTmplCode())) {
            financialCodeName = "活跃奖励";
        }
        // 业务逻辑日志
        afterChange = "从" + financialCodeName + "黑名单删除" + black.getLoginName();
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 任务中心--添加黑名单
    public void addTaskBlackListLog(OprActBlacklist black) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = TASK_ACCOUNT;
        String ip = getUserIp();
        String financialCodeName = "";
        if ("QD".equals(black.getTmplCode())) {
            financialCodeName = "签到";
        } else if ("XS".equals(black.getTmplCode())) {
            financialCodeName = "限时活动";
        } else if ("TJ".equals(black.getTmplCode())) {
            financialCodeName = "好友推荐";
        } else if ("SJ".equals(black.getTmplCode())) {
            financialCodeName = "限时活动";
        } else if ("ZL".equals(black.getTmplCode())) {
            financialCodeName = "完善资料";
        } else if ("DS".equals(black.getTmplCode())) {
            financialCodeName = "定时奖励";
        } else if (HR_TASK_ACTIVITY.equals(black.getTmplCode())) {
            financialCodeName = "活跃奖励";
        }
        // 业务逻辑日志
        afterChange = "添加" + black.getLoginName() + "至" + financialCodeName + "黑名单";
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    public void checkUserInfo(MbrAccount account, String accLoginName, String userName, String ip) {
        StringBuffer sb = new StringBuffer();
        sb.append("验证");
        sb.append(accLoginName);
        sb.append(":");
        if (StringUtil.isNotEmpty(account.getRealName())) {
            sb.append("真实姓名;");
        }
        if (StringUtil.isNotEmpty(account.getMobile())) {
            sb.append("电话号码;");
        }
        if (StringUtil.isNotEmpty(account.getWeChat())) {
            sb.append("微信号;");
        }
        if (StringUtil.isNotEmpty(account.getQq())) {
            sb.append("QQ号;");
        }
        if (StringUtil.isNotEmpty(account.getCardNo())) {
            sb.append("银行卡号;");
        }
        String log = sb.toString();
        addMbrAccountLog(null, userName, "",
                "", log, userName, Constants.EVNumber.two, MEMBER_LIST, ip);
    }


    // 自动升级日志
    public void accountAutoLog(Integer accountId, String loginName, String btierName, String atierName, boolean isRecover) {
        String afterChange = loginName + ":" + btierName + ">>" + atierName;
        if (isRecover) {
        	addMbrAccountLog(accountId, SYSTEM_USER, ACCOUNT_AUTO_RECOVER,
        			"", afterChange, SYSTEM_USER, Constants.EVNumber.two, ACCOUNT_AUTO_RECOVER, null);
        } else {
        	addMbrAccountLog(accountId, SYSTEM_USER, ACCOUNT_AUTO,
        			"", afterChange, SYSTEM_USER, Constants.EVNumber.two, ACCOUNT_AUTO, null);
        }
    }

    // 自动降级日志
    public void accountAutoDowngradeLog(Integer accountId, String loginName, String btierName, String atierName) {
        String afterChange = loginName + ":" + btierName + ">>" + atierName;
        addMbrAccountLog(accountId, SYSTEM_USER, ACCOUNT_AUTO_DOWNGRADE,
                "", afterChange, SYSTEM_USER, Constants.EVNumber.two, ACCOUNT_AUTO_DOWNGRADE, null);
    }

    // 支付列表
    // 新增加密货币支付
    public void crSave(SetBasicSysCryptoCurrencies setBasicSysCryptoCurrencies) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = PAY_LIST;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = "新增加密货币[" + setBasicSysCryptoCurrencies.getName() + "]";
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 修改加密货币支付
    public void crUpdate(SetBasicSysCryptoCurrencies setBasicSysCryptoCurrencies) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = PAY_LIST;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = "修改了加密货币[" + setBasicSysCryptoCurrencies.getName() + "]";
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 修改加密货币支付状态
    public void crUpdateStatus(SetBasicSysCryptoCurrencies setBasicSysCryptoCurrencies) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = PAY_LIST;
        String ip = getUserIp();
        // 业务逻辑日志
        String status = setBasicSysCryptoCurrencies.getAvailable().intValue() == 1 ? "开启" : "禁用";
        afterChange = "修改了加密货币[" + setBasicSysCryptoCurrencies.getName() + "]状态为" + status;
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 删除加密货币支付
    public void crDelete(SetBasicSysCryptoCurrencies setBasicSysCryptoCurrencies) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = PAY_LIST;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = "删除加密货币[" + setBasicSysCryptoCurrencies.getName() + "]";
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 支付分配
    //修改加密货币--支付分配限额
    public void updateSysQrCodeLog3(SetBasicSysCryptoCurrencies setBasicSysCryptoCurrencies) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = UPDATE_PAY_SET;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = "修改加密货币[" + setBasicSysCryptoCurrencies.getName() + "]：单笔最低限额" + setBasicSysCryptoCurrencies.getMinAmout();
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 出款管理
    // 新增出款--加密货币
    public void addFundMerchantPay(FundMerchantPay merchantPay) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = DEPOSIT_MANAGER;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = "新增加密货币出款[" + merchantPay.getCurrencyCode() + "-" + merchantPay.getCurrencyProtocol() + "]";
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 修改出款-加密货币
    public void updateFundMerchantPay(FundMerchantPay merchantPay) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = DEPOSIT_MANAGER;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = "修改了加密货币出款[" + merchantPay.getCurrencyCode() + "-" + merchantPay.getCurrencyProtocol() + "]";
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 修改出款状态-加密货币
    public void updateFundMerchantPayAvailable(FundMerchantPay newMerchantPay, FundMerchantPay oldMerchantPay) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = DEPOSIT_MANAGER;
        String ip = getUserIp();
        // 业务逻辑日志
        String status = newMerchantPay.getAvailable().intValue() == 1 ? "开启" : "禁用";
        afterChange = "修改了加密货币出款[" + oldMerchantPay.getCurrencyCode() + "-" + oldMerchantPay.getCurrencyProtocol() + "]状态为" + status;
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 修改出款状态-人民币
    public void updateFundMerchantPayAvailableRMB(FundMerchantPay newMerchantPay, FundMerchantPay oldMerchantPay) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = DEPOSIT_MANAGER;
        String ip = getUserIp();
        // 业务逻辑日志
        String status = newMerchantPay.getAvailable().intValue() == 1 ? "开启" : "禁用";
        afterChange = "修改了人民币出款[商户：" + oldMerchantPay.getMerchantName() + "  商户号：" + oldMerchantPay.getMerchantNo() + "]状态为" + status;
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 修改出款-人民币
    public void updateFundMerchantPayRMB(FundMerchantPay merchantPay) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = DEPOSIT_MANAGER;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = "修改了人民币出款[商户：" + merchantPay.getMerchantName() + "  商户号：" + merchantPay.getMerchantNo() + "]";
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 新增出款-人民币
    public void addFundMerchantPayRMB(FundMerchantPay merchantPay) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = DEPOSIT_MANAGER;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = "新增人民币出款[商户：" + merchantPay.getMerchantName() + "  商户号：" + merchantPay.getMerchantNo() + "]";
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 会员列表-会员详情-加密货币钱包状态修改
    public void updateAccountCryptoCurrenciesStatus(MbrCryptoCurrencies newMbrCryptoCurrencies, MbrCryptoCurrencies oldMbrCryptoCurrencies, MbrAccount mbr) {
        // 参数准备
        Integer accountId = oldMbrCryptoCurrencies.getAccountId();
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = ACCOUNT_INFO_ONE + mbr.getLoginName() + "的" + oldMbrCryptoCurrencies.getCurrencyCode() + " 钱包[" + oldMbrCryptoCurrencies.getWalletName() + "]状态为";   // item+afterChange为前端“操作描述”
        String status = newMbrCryptoCurrencies.getAvailable().intValue() == 1 ? "开启" : "禁用";
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = MEMBER_LIST;
        String ip = getUserIp();
        // 业务逻辑日志

        afterChange += status;
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    public void deleteAccountCr(MbrCryptoCurrencies newMbrCryptoCurrencies, MbrAccount mbr) {
        // 参数准备
        Integer accountId = newMbrCryptoCurrencies.getAccountId();
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = ACCOUNT_INFO_ONE_DELETE + mbr.getLoginName() + "的" + newMbrCryptoCurrencies.getCurrencyCode() + " 钱包[" + newMbrCryptoCurrencies.getWalletName() + "]";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "[" + newMbrCryptoCurrencies.getWalletAddress() + "]";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = MEMBER_LIST;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange += "[" + newMbrCryptoCurrencies.getWalletAddress() + "]";
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    //系统设置短信设置
    public void updateSysSmsConfigLog(SmsConfig smsConfig) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "编辑短信设置";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = SYSTEM_COMFIG;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = "";
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 会员详情-账户资料-修改登录锁定状态
    public void loginLockUpdate(MbrAccount mbrAccount) {
        // 参数准备
        Integer accountId = mbrAccount.getId();
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = ACCOUNT_INFO_OT_ONE + mbrAccount.getLoginName();  // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = mbrAccount.getLoginName();
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = MEMBER_LIST;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = ACCOUNT_LOGIN_LOCK + ":" + (mbrAccount.getLoginLock() == 0 ? "锁定" : "解锁") + " > " + (mbrAccount.getLoginLock() == 0 ? "解锁" : "锁定");
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    //系统设置--app下载设置
    public void appDownloadSet(AppDownloadSet appDownloadSet) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "编辑APP下载设置";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = SYSTEM_COMFIG;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = "";
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }
    
    //系统设置--原生投注设置
    public void updateNativeSports(NativeSports nativeSports) {
    	// 参数准备
    	Integer accountId = null;
    	String loginName = getUser().getUsername(); // 前端的管理员
    	String item = "编辑原生投注设置";   // item+afterChange为前端“操作描述”
    	String afterChange = "";
    	String beforeChange = "";
    	String userName = "";
    	int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
    	String moduleName = SYSTEM_COMFIG;
    	String ip = getUserIp();
    	// 业务逻辑日志
    	afterChange = "";
    	// 插入日志调用
    	addMbrAccountLog(accountId, loginName, item, beforeChange,
    			afterChange, userName, operatorType, moduleName, ip);
    }

    // 户内转账-刷新
    public void updateManageStatusLog(MbrBillManage mbrBillManage, String flag) {
        // 参数准备
        Integer accountId = null;
        String loginName = mbrBillManage.getUsername(); // 前端的管理员
        String item = "户内转编辑状态";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = ACCOUNT_UPDATETRANSFER;
        String ip = getUserIp();
        String opr = mbrBillManage.getOpType() == 0 ? "转出" : "转入";
        // 业务逻辑日志
        afterChange = "编辑" + mbrBillManage.getLoginName() + opr + "订单：" + mbrBillManage.getOrderNo() + "; 状态变更为：" + flag;
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 户内转账-编辑状态
    public void checkTransfer(Long orderNo, String flag) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "刷新";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = ACCOUNT_TRANSFER;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = "订单：" + String.valueOf(orderNo.longValue()) + "; 状态：" + flag;
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 会员详情-会员代理级别修改
    public void updateMbrAgentLevel(MbrAccount newMa, MbrAccount oldMa) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = MEMBER_LIST_DETAIL;
        String ip = getUserIp();
        // 业务逻辑日志
        afterChange = "修改会员：" + oldMa.getLoginName() + "的会员代理等级";
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 全民代理-批量审核
    public void bonusAuditBatch(MbrRebateAgentAuditDto dt) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = ACCOUNT_REBATE_AGENT;
        String ip = getUserIp();
        // 业务逻辑日志
        String status = dt.getStatus() == 1 ? "通过" : "拒绝";
        afterChange = "批量审核" + status + ",日期" + dt.getCreateTime();
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    // 全民代理-单条审核
    public void bonusAudit(MbrRebateAgentAuditDto newDto, MbrRebateAgentBonus oldDto) {
        // 参数准备
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        String userName = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = ACCOUNT_REBATE_AGENT;
        String ip = getUserIp();
        // 业务逻辑日志
        String status = newDto.getStatus() == 1 ? "通过" : "拒绝";
        afterChange = "审核会员:" + oldDto.getLoginName() + ",日期" + oldDto.getCreateTimeEx() + "返利" + status;
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }

    public void addFriendRebateReward(AddFriendRebateRewardDto addFriendRebateRewardDto, String userName){
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = ACCOUNT_REBATE_FRIEND;
        String ip = getUserIp();

        // 业务逻辑日志

        String  type = "";
        if(addFriendRebateRewardDto.getRewardType() ==4){
            type = "有效下注";
        }else if(addFriendRebateRewardDto.getRewardType() ==3){
            type = "首充";
        }else if(addFriendRebateRewardDto.getRewardType() ==5){
            type = "VIP";
        }else if(addFriendRebateRewardDto.getRewardType() ==6){
            type = "充值";
        }

        beforeChange = String.format("增加%s->%s奖励:金额%.2f绑定玩家%s稽核倍数%d", addFriendRebateRewardDto.getLoginName(),type, addFriendRebateRewardDto.getAmount().doubleValue(), addFriendRebateRewardDto.getSubLoginName(), addFriendRebateRewardDto.getAuditMultiple() == null ?0 : addFriendRebateRewardDto.getAuditMultiple().intValue()  );
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }


    public void reduceFriendRebateReward(ReduceFriendRebateRewardDto reduceFriendRebateRewardDto, String userName){
        Integer accountId = null;
        String loginName = getUser().getUsername(); // 前端的管理员
        String item = "";   // item+afterChange为前端“操作描述”
        String afterChange = "";
        String beforeChange = "";
        int operatorType = 2;                       // 操作人类型 1本人 2后台操作"
        String moduleName = ACCOUNT_REBATE_FRIEND;
        String ip = getUserIp();

        // 业务逻辑日志

        String  type = "";
        if(reduceFriendRebateRewardDto.getRewardType() ==4){
            type = "有效下注";
        }else if(reduceFriendRebateRewardDto.getRewardType() ==3){
            type = "首充";
        }else if(reduceFriendRebateRewardDto.getRewardType() ==5){
            type = "VIP";
        }else if(reduceFriendRebateRewardDto.getRewardType() ==6){
            type = "充值";
        }

        String clear = "";

        if(reduceFriendRebateRewardDto.getAudit() == null || reduceFriendRebateRewardDto.getAudit() == 0 ){
            clear = "不清除";
        }else if(reduceFriendRebateRewardDto.getAudit() == 1){
            clear = "清除";
        }

        beforeChange = String.format("减少%s->%s奖励:金额%.2f 是否除稽核:%s", reduceFriendRebateRewardDto.getLoginName(),type, reduceFriendRebateRewardDto.getAmount().doubleValue(), clear);
        // 插入日志调用
        addMbrAccountLog(accountId, loginName, item, beforeChange,
                afterChange, userName, operatorType, moduleName, ip);
    }


    /**
     * 添加预警日志
     * @param warningLogDto
     */
    public void addWarningLog(WarningLogDto warningLogDto){
        SysWarning sysWarning = new SysWarning();
        sysWarning.setUserName(warningLogDto.getUserName());
        sysWarning.setLoginName(warningLogDto.getLoginName());
        sysWarning.setContent(warningLogDto.getContent());
        sysWarning.setCreateTime(DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
        sysWarning.setStatus(Constants.EVNumber.zero);
        sysWarning.setType(warningLogDto.getType());
        sysWarningMapper.insert(sysWarning);
    }


    /**
     * 添加预警日志
     * @param
     */
    public void addWarningLog(List<Integer> ids, String userName, String content, Integer type){
        List<String> loginNameList = mbrMapper.getAccountNames(ids);
        for(String loginName: loginNameList){
            SysWarning sysWarning = new SysWarning();
            sysWarning.setLoginName(loginName);
            sysWarning.setUserName(userName);
            sysWarning.setContent(content);
            sysWarning.setStatus(Constants.EVNumber.zero);
            sysWarning.setCreateTime(DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
            sysWarning.setType(type);
            sysWarningMapper.insert(sysWarning);
        }
    }

}
