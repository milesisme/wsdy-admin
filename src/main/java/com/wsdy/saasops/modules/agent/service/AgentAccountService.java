package com.wsdy.saasops.modules.agent.service;

import static com.wsdy.saasops.common.constants.ColumnAuthConstants.AGENT_MOBILE_CONTACT;
import static com.wsdy.saasops.common.constants.ColumnAuthConstants.AGENT_NAME_CONTACT;
import static com.wsdy.saasops.common.constants.ColumnAuthConstants.AGENT_PASSWORD_CONTACT;
import static com.wsdy.saasops.common.constants.ColumnAuthConstants.AGENT_SECUREPWD_CONTACT;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.google.gson.Gson;
import com.wsdy.saasops.aff.service.SdyDataService;
import com.wsdy.saasops.agapi.modules.mapper.AgentMenuMapper;
import com.wsdy.saasops.api.config.ApiConfig;
import com.wsdy.saasops.common.constants.ColumnAuthConstants;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.SysConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.dao.AgentCryptoCurrenciesMapper;
import com.wsdy.saasops.modules.agent.dao.AgentDepartmentMapper;
import com.wsdy.saasops.modules.agent.dao.AgentSubAccountMapper;
import com.wsdy.saasops.modules.agent.dao.AgyBankCardMapper;
import com.wsdy.saasops.modules.agent.dao.AgyWalletMapper;
import com.wsdy.saasops.modules.agent.dto.AgentTree;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgentCryptoCurrencies;
import com.wsdy.saasops.modules.agent.entity.AgentDepartment;
import com.wsdy.saasops.modules.agent.entity.AgentSubAccount;
import com.wsdy.saasops.modules.agent.entity.AgyBankcard;
import com.wsdy.saasops.modules.agent.entity.AgyTree;
import com.wsdy.saasops.modules.agent.entity.AgyWallet;
import com.wsdy.saasops.modules.agent.mapper.AgentMapper;
import com.wsdy.saasops.modules.agent.mapper.AgentNewMapper;
import com.wsdy.saasops.modules.agent.mapper.CommissionCastMapper;
import com.wsdy.saasops.modules.base.entity.BaseBank;
import com.wsdy.saasops.modules.base.service.BaseBankService;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.sys.dto.ColumnAuthTreeDto;
import com.wsdy.saasops.modules.sys.entity.SysMenuExtend;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import com.wsdy.saasops.modules.sys.service.ColumnAuthProviderService;
import com.wsdy.saasops.modules.sys.service.SysRoseMenuExtendService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class AgentAccountService extends BaseService<AgentAccountMapper, AgentAccount> {

	@Autowired
	private AgentAccountMapper agentAccountMapper;
	@Autowired
	private AgentMapper agentMapper;
	@Autowired
	private AgyWalletMapper agyWalletMapper;
	@Autowired
	private AgyBankCardMapper bankCardMapper;
	@Autowired
	private MbrAccountService accountService;
	@Autowired
	private MbrAccountLogService accountLogService;
	@Autowired
	private AgentNewMapper agentNewMapper;
	@Autowired
	private AgentDepartmentMapper departmentMapper;
	@Autowired
	private SdyDataService sdyDataService;
	@Autowired
	private AgentSubAccountMapper subAccountMapper;
	@Autowired
	private AgentMenuMapper agentMenuMapper;
	@Autowired
	private MbrMapper mbrMapper;
	@Autowired
	private ApiConfig apiConfig;
	@Autowired
	private AgentCryptoCurrenciesMapper cryptoCurrenciesMapper;
	@Autowired
	private BaseBankService baseBankService;
	@Autowired
	private SysRoseMenuExtendService sysRoseMenuExtenService;
	@Autowired
	private CommissionCastMapper commissionCastMapper;
	@Autowired
	private ColumnAuthProviderService columnAuthProviderService;

	public List<AgentAccount> totalAgentList(AgentAccount agentAccount) {
		return agentNewMapper.totalAgentList(agentAccount);
	}

	public PageUtils agyAccountList(AgentAccount agentAccount, Integer pageNo, Integer pageSize, String orderBy) {
		PageHelper.startPage(pageNo, pageSize);
		PageHelper.orderBy(orderBy);
		List<AgentAccount> list = agentMapper.findAgyAccountListPage(agentAccount);
		return BeanUtil.toPagedResult(list);
	}

	public PageUtils agyAccountReviewList(Long userId, AgentAccount agentAccount, Integer pageNo, Integer pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		List<AgentAccount> list = agentNewMapper.newfindAgyAccountListPage(agentAccount);
		String moblie = mbrMapper.findAccountContact(userId, AGENT_MOBILE_CONTACT);
		String name = mbrMapper.findAccountContact(userId, AGENT_NAME_CONTACT);
		list.stream().forEach(ls -> mobileAndNameAuthority(ls, moblie, name));
		return BeanUtil.toPagedResult(list);
	}

	public PageUtils newAgyAccountList(Long userId, AgentAccount agentAccount, Integer pageNo, Integer pageSize,
			Integer roleId) {

		boolean rt = checkPerm(agentAccount, userId);
		if (!rt) {
			throw new R200Exception("部门或代理属性权限不够无法查询！");
		}

		if (agentAccount.getAttributes() == null && agentAccount.getAttributesList().size() == 0) {
			return BeanUtil.toPagedResult(new ArrayList());
		}
		List<ColumnAuthTreeDto> menuList = columnAuthProviderService.getRoleColumnAuthByFlag(roleId,
				ColumnAuthConstants.AGENT_LIST_ID);

		PageHelper.startPage(pageNo, pageSize);
		agentAccount.setStatus(Constants.EVNumber.one);
		List<AgentAccount> accounts = agentNewMapper.newfindAgyAccountListPage(agentAccount);
		String moblie = null;
		
		Optional<ColumnAuthTreeDto> findFirst = menuList.stream().filter(t -> ColumnAuthConstants.AGENT_LIST_FULLY_MOBILE.equals(t.getMenuId())).findFirst();
		boolean present = findFirst.isPresent();
		
		if (present) {
			moblie = findFirst.get().getPerms();
		}
		String name = mbrMapper.findAccountContact(userId, AGENT_NAME_CONTACT);
		for (AgentAccount ls : accounts) {
			mobileAndNameAuthority(ls, moblie, name);
			ls.setSkype("");
			ls.setQq("");
			ls.setWeChat("");
			ls.setTelegram("");
			ls.setEmail("");
			ls.setFlyGram("");
		}
		return BeanUtil.toPagedResult(accounts);
	}

	public void mobileAndNameAuthority(AgentAccount agentAccount, String moblie, String name) {
		if (StringUtils.isEmpty(moblie) && StringUtils.isNotEmpty(agentAccount.getMobile())) {
			if (agentAccount.getMobile().length() > 7) {
				StringBuilder sb = new StringBuilder(agentAccount.getMobile());
				agentAccount.setMobile(sb.replace(3, 7, "****").toString());
			}
		}
		if (StringUtils.isEmpty(name) && StringUtils.isNotEmpty(agentAccount.getRealName())) {
			String str = agentAccount.getRealName().substring(0, 1) + "**";
			agentAccount.setRealName(str);
		}
	}

	public AgentAccount agyAccountInfo(Long userId, Integer id) {

		AgentAccount agentAccount = new AgentAccount();
		agentAccount.setId(id);
		List<AgentAccount> agentAccounts = agentNewMapper.newfindAgyAccountListPage(agentAccount);
		if (Collections3.isNotEmpty(agentAccounts)) {
			AgentAccount agentAccount1 = agentAccounts.get(0);
			String moblie = mbrMapper.findAccountContact(userId, AGENT_MOBILE_CONTACT);
			String name = mbrMapper.findAccountContact(userId, AGENT_NAME_CONTACT);
			mobileAndNameAuthority(agentAccount1, moblie, name);
			AgyBankcard bankcard = new AgyBankcard();
			bankcard.setAccountId(agentAccount1.getId());
			bankcard.setIsDel(0);
			int count = bankCardMapper.selectCount(bankcard);
			agentAccount1.setBankNum(count);
			agentAccount1.setAgyPwd(null);
			agentAccount1.setSecurePwd(null);
			agentAccount1.setSalt(null);
			BigDecimal zero = new BigDecimal("0");
			agentAccount1.setWithdrawServicerate(zero);
			agentAccount1.setDepositServicerate(zero);
			agentAccount1.setAdditionalServicerate(zero);
			agentAccount1.setFeeModel(0);
			agentAccount1.setSkype("");
			agentAccount1.setQq("");
			agentAccount1.setWeChat("");
			agentAccount1.setTelegram("");
			agentAccount1.setEmail("");
			agentAccount1.setMobile("");
			agentAccount1.setFlyGram("");
			return agentAccount1;
		}
		return null;
	}

	public AgentAccount viewOther(Integer agyId, Integer roleId) {
		List<ColumnAuthTreeDto> menuList = columnAuthProviderService.getRoleColumnAuthByFlag(roleId,
				ColumnAuthConstants.AGENT_LIST_ID);

		if (CollectionUtils.isEmpty(menuList)) {
			return new AgentAccount();
		}
		Boolean isShowAdditional = false;
		Boolean isShowMobile = false;
		Boolean isShowServicerate = false;
		
		
		
		Set<String> columnSets = new HashSet<String>();
		for (ColumnAuthTreeDto columnAuthTreeDto : menuList) {
			if (ColumnAuthConstants.AGENT_ACCOUNT_UPDATESERVICERATE.equals(columnAuthTreeDto.getPerms())) {
				isShowServicerate = true;
			}
			if (ColumnAuthConstants.AGENT_ACCOUNT_UPDATEADDITIONAL.equals(columnAuthTreeDto.getPerms())) {
				isShowAdditional = true;
			}
			if (ColumnAuthConstants.AGENT_MOBILE_CONTACT.equals(columnAuthTreeDto.getPerms())) {
				isShowMobile = true;
			}
			columnSets.add(columnAuthTreeDto.getColumnName());
		}
		if (columnSets.size() > 0) {
			columnSets.add("feemodel");
		}

		AgentAccount viewOtherAccount = agentNewMapper.viewOtherAccount(columnSets, agyId);
		viewOtherAccount.setIsShowAdditional(isShowAdditional);
		viewOtherAccount.setIsShowMobile(isShowMobile);
		viewOtherAccount.setIsShowServicerate(isShowServicerate);
		return viewOtherAccount;
	}

	/**
	 * 审核代理
	 *
	 * @param agentAccount
	 */
	public void agentReview(AgentAccount agentAccount, String userName) {
		AgentAccount account = agentAccountMapper.selectByPrimaryKey(agentAccount.getId());
		if (account.getStatus() != 2) {
			throw new R200Exception("只能处理待处理的订单");
		}

		account.setStatus(agentAccount.getStatus());
		if (account.getStatus() == Constants.EVNumber.one) {
			account.setAvailable(Constants.EVNumber.one);
		}
		account.setEmail(agentAccount.getEmail());
		account.setQq(agentAccount.getQq());
		account.setWeChat(agentAccount.getWeChat());
		account.setSkype(agentAccount.getSkype());
		account.setFlyGram(agentAccount.getFlyGram());
		account.setTelegram(agentAccount.getTelegram());
		account.setParentId(agentAccount.getParentId());
		account.setContractId(agentAccount.getContractId());
		account.setContractStart(agentAccount.getContractStart());
		account.setContractEnd(agentAccount.getContractEnd());
		account.setDefaultGroupId(agentAccount.getDefaultGroupId());
		account.setMemo(agentAccount.getMemo());
		account.setSetGroupId(agentAccount.getSetGroupId());
		if (agentAccount.getFeeModel() != null) {
			account.setFeeModel(agentAccount.getFeeModel());
		}
		account.setDepositServicerate(agentAccount.getDepositServicerate());
		account.setWithdrawServicerate(agentAccount.getWithdrawServicerate());
		if (nonNull(agentAccount.getMobile()) && !agentAccount.getMobile().contains("*")) {
			account.setMobile(agentAccount.getMobile());
		}
		if (nonNull(agentAccount.getRealName()) && !agentAccount.getRealName().contains("*")) {
			account.setRealName(agentAccount.getRealName());
		}
		account.setModifyUser(userName);
		account.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
		account.setReviewTime(getCurrentDate(FORMAT_18_DATE_TIME));
		
		// 只有直线代理有代理返佣
		if (agentAccount.getAttributes() == Constants.EVNumber.zero) {
			account.setRebateratio(agentAccount.getRebateratio());
			account.setFirstagentratio(agentAccount.getFirstagentratio());
		}
		
		agentAccountMapper.updateByPrimaryKey(account);
		if (account.getStatus() != 0) {
			// 默认钱包类型1
			agentAccount.setWalletType(1);
			addAgentWalletAndTree(agentAccount);
			saveUserRole(agentAccount);
		}
	}

	/**
	 * 新增代理
	 *
	 * @param agentAccount
	 */
	public void agyAccountSave(AgentAccount agentAccount, String url) {
		checkSubAccount(agentAccount.getAgyAccount());
		accountService.checkoutUsername(agentAccount.getAgyAccount());
		String salt = RandomStringUtils.randomAlphanumeric(20);
		String agyPwd = agentAccount.getAgyPwd();

		agentAccount.setDepositServicerate(agentAccount.getDepositServicerate());
		agentAccount.setWithdrawServicerate(agentAccount.getWithdrawServicerate());
		agentAccount.setAdditionalServicerate(agentAccount.getAdditionalServicerate());
		agentAccount.setFeeModel(agentAccount.getFeeModel());
		agentAccount.setAgyPwd(new Sha256Hash(agentAccount.getAgyPwd(), salt).toHex());
		agentAccount.setSecurePwd(new Sha256Hash(agentAccount.getSecurePwd(), salt).toHex());
		agentAccount.setSalt(salt);
		agentAccount.setAvailable(Constants.EVNumber.one);
		agentAccount.setParentId(agentAccount.getParentId());
		agentAccount.setStatus(Constants.EVNumber.one);
		agentAccount.setRegisterSign(Constants.EVNumber.one);
		agentAccount.setAvailable(Constants.EVNumber.one);
		agentAccount.setSpreadCode(getSpreadCode());
		agentAccount.setModifyTime(agentAccount.getCreateTime());
		agentAccount.setModifyUser(agentAccount.getCreateUser());
		if (agentAccount.getAttributes() == 2 || agentAccount.getAttributes() == 3) {
			AgentDepartment department = departmentMapper.selectByPrimaryKey(agentAccount.getDepartmentid());
			agentAccount.setParentId(department.getAgentId());
			agentAccount.setDepartmentid(agentAccount.getDepartmentid());
		}
		if (agentAccount.getAttributes() == Constants.EVNumber.one) {
			agentAccount.setSuperiorCloneId(agentAccount.getParentId());
		}
		// 只有直线代理有代理返佣
		if (agentAccount.getAttributes() == Constants.EVNumber.zero) {
			agentAccount.setRebateratio(agentAccount.getRebateratio());
			agentAccount.setFirstagentratio(agentAccount.getFirstagentratio());
		}
		
		agentAccountMapper.insert(agentAccount);
		if (Constants.SITECODE_GOC.equals(CommonUtil.getSiteCode())
				|| agentAccount.getAttributes() != Constants.EVNumber.one) {
			addAgentWalletAndTree(agentAccount);
		} else {
			insertAgyWallet(agentAccount);
		}
		if (agentAccount.getAttributes() == 0 || agentAccount.getAttributes() == 1) {
			MbrAccount account = new MbrAccount();
			account.setEmail(agentAccount.getEmail());
			account.setLoginName(agentAccount.getAgyAccount());
			account.setRealName(agentAccount.getRealName());
			account.setCagencyId(agentAccount.getParentId());
			account.setMobile(agentAccount.getMobile());
			account.setLoginIp(agentAccount.getIp());
			account.setRegisterUrl(url);
			account.setAddAgent(Boolean.TRUE);
			account.setLoginPwd(agyPwd);
			account.setSecurePwd(agyPwd);
			int count = agentMapper.isTagencyid(agentAccount.getParentId());
			if (count == 0) {
				account.setCagencyId(apiConfig.getRegDeaultCagencyId());
			}

			accountService.adminSave(account, null, getUser().getUsername(), agentAccount.getIp(), Boolean.TRUE,
					Constants.EVNumber.one);
		}
		saveUserRole(agentAccount);
	}

	public void saveUserRole(AgentAccount agentAccount) {
		Integer roleId = 2;
		if (agentAccount.getAttributes() == 3) {
			roleId = 3;
		}
		agentMenuMapper.saveUserRole(agentAccount.getId(), roleId, agentAccount.getAgyAccount());
	}

	/**
	 * 修改代理
	 *
	 * @param agentAccount
	 */
	public void updateAgent(AgentAccount agentAccount, SysUserEntity entity) {
		AgentAccount account = agentAccountMapper.selectByPrimaryKey(agentAccount.getId());
//        Integer parentId = account.getParentId();
		account.setAvailable(agentAccount.getAvailable());
		account.setParentId(agentAccount.getParentId());
		if (StringUtil.isNotEmpty(agentAccount.getAgyPwd())) {
			String passwordAuthority = mbrMapper.findAccountContact(entity.getUserId(), AGENT_PASSWORD_CONTACT);
			if (nonNull(passwordAuthority)) {
				account.setAgyPwd(new Sha256Hash(agentAccount.getAgyPwd(), account.getSalt()).toHex());
			}
		}
		if (StringUtil.isNotEmpty(agentAccount.getSecurePwd())) {
			String securePwdAuthority = mbrMapper.findAccountContact(entity.getUserId(), AGENT_SECUREPWD_CONTACT);
			if (nonNull(securePwdAuthority)) {
				account.setSecurePwd(new Sha256Hash(agentAccount.getSecurePwd(), account.getSalt()).toHex());
			}
		}
		if (agentAccount.getAttributes() == 2 || agentAccount.getAttributes() == 3) {
			AgentDepartment department = departmentMapper.selectByPrimaryKey(agentAccount.getDepartmentid());
			account.setParentId(department.getAgentId());
			account.setDepartmentid(department.getId());
		}
		if (agentAccount.getAttributes() == Constants.EVNumber.one) {
			account.setSuperiorCloneId(agentAccount.getParentId());
		}
		// 如果是分线，默认费率 = 0
		if (account.getAttributes() == 1) {
			// 结算费模式
			account.setFeeModel(1);
			// 服务费存款比例
			account.setDepositServicerate(BigDecimal.ZERO);
			// 服务费取款比例
			account.setWithdrawServicerate(BigDecimal.ZERO);
			// 平台费额外比例
			account.setAdditionalServicerate(BigDecimal.ZERO);
		} else {
			// 判断当前操作人是否有权限，如果有权限set值
			List<ColumnAuthTreeDto> menuList = columnAuthProviderService.getRoleColumnAuthByFlag(entity.getRoleId(),
					ColumnAuthConstants.AGENT_LIST_ID);
			// 服务费权限
			Optional<ColumnAuthTreeDto> updateservicerate = menuList.stream().filter(t -> ColumnAuthConstants.AGENT_ACCOUNT_UPDATESERVICERATE.equals(t.getPerms())).findFirst();
			boolean serviceratePresent = updateservicerate.isPresent();
			
			if (serviceratePresent && agentAccount.getDepositServicerate() != null && agentAccount.getWithdrawServicerate() != null) {
				// 服务费存款比例
				account.setDepositServicerate(agentAccount.getDepositServicerate());
				// 服务费取款比例
				account.setWithdrawServicerate(agentAccount.getWithdrawServicerate());
			}

			// 平台费权限
			Optional<ColumnAuthTreeDto> updateadditional = menuList.stream().filter(t -> ColumnAuthConstants.AGENT_ACCOUNT_UPDATEADDITIONAL.equals(t.getPerms())).findFirst();
			boolean updateadditionalPresent = updateadditional.isPresent();

			if (updateadditionalPresent && agentAccount.getAdditionalServicerate() != null) {
				// 平台费额外比例
				account.setAdditionalServicerate(agentAccount.getAdditionalServicerate());
			}
			if (updateadditionalPresent && serviceratePresent && agentAccount.getFeeModel() != null) {
				// 结算费模式
				account.setFeeModel(agentAccount.getFeeModel());
			}
			// 只有直线代理有代理返佣
			if (agentAccount.getAttributes() == Constants.EVNumber.zero) {
				account.setRebateratio(agentAccount.getRebateratio());
				account.setFirstagentratio(agentAccount.getFirstagentratio());
			}
		}
		if (StringUtil.isNotEmpty(agentAccount.getEmail())) {
			account.setEmail(agentAccount.getEmail());
		}
		if (StringUtil.isNotEmpty(agentAccount.getQq())) {
			account.setQq(agentAccount.getQq());
		}
		if (StringUtil.isNotEmpty(agentAccount.getWeChat())) {
			account.setWeChat(agentAccount.getWeChat());
		}
		if (StringUtil.isNotEmpty(agentAccount.getSkype())) {
			account.setSkype(agentAccount.getSkype());
		}
		if (StringUtil.isNotEmpty(agentAccount.getFlyGram())) {
			account.setFlyGram(agentAccount.getFlyGram());
		}
		if (StringUtil.isNotEmpty(agentAccount.getTelegram())) {
			account.setTelegram(agentAccount.getTelegram());
		}
		account.setContractId(agentAccount.getContractId());
		account.setContractStart(agentAccount.getContractStart());
		account.setContractEnd(agentAccount.getContractEnd());
		account.setMemo(agentAccount.getMemo());
		account.setDefaultGroupId(agentAccount.getDefaultGroupId());
		account.setSetGroupId(agentAccount.getSetGroupId());
		// 不为空，设置下级会员的分组id
		if (agentAccount.getSetGroupId() != null) {
			mbrMapper.updateGroupIdByCagencyid(account.getId(), agentAccount.getSetGroupId());
		}

//        BeanUtils.copyProperties(agentAccount, account);
		if (StringUtil.isNotEmpty(agentAccount.getMobile()) && nonNull(agentAccount.getMobile()) && !agentAccount.getMobile().contains("*")) {
			account.setMobile(agentAccount.getMobile());
		}
		if (StringUtil.isNotEmpty(agentAccount.getRealName()) && nonNull(agentAccount.getRealName()) && !agentAccount.getRealName().contains("*")) {
			account.setRealName(agentAccount.getRealName());
		}
		account.setModifyUser(entity.getUsername());
		account.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
		agentAccountMapper.updateByPrimaryKey(account);
		AgyWallet wallet = new AgyWallet();
		wallet.setAgyAccount(account.getAgyAccount());
		AgyWallet agyWallet = agyWalletMapper.selectOne(wallet);
		agyWallet.setWalletType(agentAccount.getWalletType());
		agyWalletMapper.updateByPrimaryKey(agyWallet);
		if (account.getAttributes() != Constants.EVNumber.one) {
			sdyDataService.updateAgentTree(account.getId(), account.getParentId());
		}
	}

	public void addAgentWalletAndTree(AgentAccount agentAccount) {
		insertAgyWallet(agentAccount);
		agentMapper.addAgentNode(agentAccount.getParentId(), agentAccount.getId());
	}

	private void insertAgyWallet(AgentAccount agentAccount) {
		AgyWallet agyWallet = new AgyWallet();
		agyWallet.setAccountId(agentAccount.getId());
		agyWallet.setAgyAccount(agentAccount.getAgyAccount());
		agyWallet.setBalance(BigDecimal.ZERO);
		agyWallet.setRechargeWallet(BigDecimal.ZERO);
		agyWallet.setPayoffWallet(BigDecimal.ZERO);
		agyWallet.setWalletType(agentAccount.getWalletType());
		agyWalletMapper.insert(agyWallet);
	}

	public void agyAccountAudit(AgentAccount agentAccount, String userName) {
		AgentAccount account = agentAccountMapper.selectByPrimaryKey(agentAccount.getId());
		if (account.getStatus() != Constants.EVNumber.two) {
			return;
		}
		account.setModifyUser(userName);
		account.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
		account.setMemo(agentAccount.getMemo());
		account.setRealName(agentAccount.getRealName());
		account.setMobile(agentAccount.getMobile());
		if (agentAccount.getStatus() == Constants.EVNumber.zero) {
			account.setSpreadCode(StringUtils.EMPTY);
			account.setStatus(Constants.EVNumber.zero);
			account.setAvailable(Constants.EVNumber.zero);
			agentMapper.removeSubAgyTree(agentAccount.getId());
			account.setCommissionId(null);
		}
		if (agentAccount.getStatus() == Constants.EVNumber.one) {
			account.setStatus(Constants.EVNumber.one);
			account.setAvailable(Constants.EVNumber.one);
			if (!account.getSpreadCode().equals(agentAccount.getSpreadCode())) {
				AgentAccount account1 = new AgentAccount();
				account1.setSpreadCode(agentAccount.getSpreadCode());
				int count = agentAccountMapper.selectCount(account1);
				if (count > 0) {
					throw new R200Exception("推广码已经存在");
				}
				agentAccount.setStatus(Constants.EVNumber.one);
			}
		}
		agentAccountMapper.updateByPrimaryKey(account);
	}

	public void agyAccountDelete(Integer id, int flag) {
		AgentAccount agentAccount = new AgentAccount();
		agentAccount.setId(id);
		List<AgentAccount> agentAccounts = agentMapper.findAgyAccountListPage(agentAccount);
		if (Collections3.isNotEmpty(agentAccounts)) {
			AgentAccount agentAccount1 = agentAccounts.get(0);
			if (agentAccount1.getDirectAgentCount() > 0 || agentAccount1.getAccountNum() > 0
					|| agentAccount1.getAvailable() == Constants.EVNumber.one) {
				throw new R200Exception("该代理不可删除");
			}
			agentMapper.removeSubAgyTree(id);
			agentAccountMapper.deleteByPrimaryKey(id);

			// 增加操作日志
			if (flag == Constants.EVNumber.one) {
				accountLogService.agyAccountDeleteLog(agentAccount1);
			}

		}
	}

	public void agyAccountAvailable(Integer id, Integer available, String userName) {
		AgentAccount account = agentAccountMapper.selectByPrimaryKey(id);
		AgentAccount agentAccount = new AgentAccount();
		agentAccount.setId(id);
		if (account.getStatus() == Constants.EVNumber.one) {
			account.setAvailable(available);
			account.setModifyUser(userName);
			account.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
			agentAccountMapper.updateByPrimaryKey(account);

			// 增加操作日志
			accountLogService.agyAccountAvailableLog(account);
		}
	}

	public void agyAccountPassword(AgentAccount agentAccount, String userName) {
		AgentAccount account = agentAccountMapper.selectByPrimaryKey(agentAccount.getId());
		if (nonNull(account)) {
			String salt = account.getSalt();
			if (StringUtils.isNotEmpty(agentAccount.getAgyPwd())) {
				String agyPwd = new Sha256Hash(agentAccount.getAgyPwd(), salt).toHex();
				if (account.getAgyPwd().equals(agyPwd)) {
					throw new R200Exception("不能跟原密码相同");
				}
				if (account.getSecurePwd().equals(agyPwd)) {
					throw new R200Exception("不能跟提款密码相同");
				}
				account.setAgyPwd(agyPwd);
			}
			if (StringUtils.isNotEmpty(agentAccount.getSecurePwd())) {
				String securePwd = new Sha256Hash(agentAccount.getSecurePwd(), salt).toHex();
				if (account.getSecurePwd().equals(securePwd)) {
					throw new R200Exception("不能跟原密码相同");
				}
				if (account.getAgyPwd().equals(securePwd)) {
					throw new R200Exception("不能跟登录密码相同");
				}
				account.setSecurePwd(securePwd);
			}
			account.setModifyUser(userName);
			account.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
			agentAccountMapper.updateByPrimaryKey(account);

			// 增加操作日志
			agentAccount.setAgyAccount(account.getAgyAccount());
			accountLogService.agyAccountPasswordLog(agentAccount);
		}
	}

	public void updateAgentPassword(AgentAccount agentAccount, String userName) {
		AgentAccount account = agentAccountMapper.selectByPrimaryKey(agentAccount.getId());
		if (nonNull(account)) {
			String salt = account.getSalt();
			String oldAgyPwd = new Sha256Hash(agentAccount.getOldAgyPwd(), salt).toHex();
			String agyPwd = new Sha256Hash(agentAccount.getAgyPwd(), salt).toHex();
			if (!account.getAgyPwd().equals(oldAgyPwd)) {
				throw new R200Exception("旧密码错误");
			}
			if (account.getSecurePwd().equals(agyPwd)) {
				throw new R200Exception("新密码不能跟提款密码相同");
			}
			if (oldAgyPwd.equals(agyPwd)) {
				throw new R200Exception("新密码不能跟旧密码相同");
			}
			account.setAgyPwd(agyPwd);

			if (StringUtils.isNotEmpty(agentAccount.getSecurePwd())) {
				String securePwd = new Sha256Hash(agentAccount.getSecurePwd(), salt).toHex();
				if (account.getSecurePwd().equals(securePwd)) {
					throw new R200Exception("不能跟原密码相同");
				}
				if (account.getAgyPwd().equals(securePwd)) {
					throw new R200Exception("不能跟登录密码相同");
				}
				account.setSecurePwd(securePwd);
			}
			account.setModifyUser(userName);
			account.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
			agentAccountMapper.updateByPrimaryKeySelective(account);
		}
	}

	public void agyAccountUpdate(AgentAccount agentAccount, String userName) {
		AgentAccount account = agentAccountMapper.selectByPrimaryKey(agentAccount.getId());
		// 为添加操作日志，需保留变更前的数据
		Gson gson = new Gson();
		AgentAccount oldAccount = gson.fromJson(gson.toJson(account), AgentAccount.class);

		account.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
		account.setModifyUser(userName);
		account.setRealName(agentAccount.getRealName());
		account.setMobile(agentAccount.getMobile());
		account.setMemo(agentAccount.getMemo());
		if (StringUtil.isNotEmpty(agentAccount.getEmail())) {
			account.setEmail(agentAccount.getEmail());
		}
		if (StringUtil.isNotEmpty(agentAccount.getWeChat())) {
			account.setWeChat(agentAccount.getWeChat());
		}
		if (StringUtil.isNotEmpty(agentAccount.getQq())) {
			account.setQq(agentAccount.getQq());
		}
		agentAccountMapper.updateByPrimaryKey(account);

		// 增加操作日志
		accountLogService.agyAccountUpdateLog(account, oldAccount);

	}

	public List<AgyBankcard> agyBankList(Integer id) {
		AgyBankcard agyBankcard = new AgyBankcard();
		agyBankcard.setAccountId(id);
		agyBankcard.setIsDel(Constants.EVNumber.zero);
		List<AgyBankcard> agyBankcardList = bankCardMapper.select(agyBankcard);
		agyBankcardList.forEach(e -> {
			e.setCardNoEncryption(StringUtil.walletAddress(e.getCardNo()));
		});
		return agyBankcardList;
	}

	public List<AgyBankcard> agyAlipayList(Integer id) {
		AgyBankcard agyBankcard = new AgyBankcard();
		agyBankcard.setBankName("支付宝");
		agyBankcard.setAccountId(id);
		agyBankcard.setIsDel(Constants.EVNumber.zero);
		List<AgyBankcard> agyBankcardList = bankCardMapper.select(agyBankcard);
		agyBankcardList.forEach(e -> {
			e.setCardNoEncryption(StringUtil.walletAddress(e.getCardNo()));
		});
		return agyBankcardList;
	}

	public List<AgentCryptoCurrencies> agyCryptocurrenciesList(Integer id) {
		AgentCryptoCurrencies cryptoCurrencies = new AgentCryptoCurrencies();
		cryptoCurrencies.setAccountId(id);
		cryptoCurrencies.setIsDel(Constants.Available.disable);
		List<AgentCryptoCurrencies> agyBankcardList = cryptoCurrenciesMapper.select(cryptoCurrencies);
		return agyBankcardList;
	}

	public void agyBankSave(AgyBankcard bankcard) {
		AgyBankcard agyBankcard = new AgyBankcard();
		agyBankcard.setAccountId(bankcard.getAccountId());
		agyBankcard.setIsDel(Constants.EVNumber.zero);
		int count = bankCardMapper.selectCount(agyBankcard);
		if (count == Constants.EVNumber.three) {
			throw new R200Exception("最多绑定3张银行卡");
		}
		agyBankcard.setCardNo(bankcard.getCardNo());
		int countC = bankCardMapper.selectCount(agyBankcard);
		if (countC > Constants.EVNumber.zero) {
			throw new R200Exception("银行卡号已经存在");
		}
		AgentAccount agyAccount = agentAccountMapper.selectByPrimaryKey(bankcard.getAccountId());
		if (isNull(agyAccount)) {
			throw new R200Exception("代理不存在");
		}
		bankcard.setAvailable(Constants.EVNumber.one);
		bankcard.setAccountId(agyAccount.getId());
		bankcard.setIsDel(Constants.EVNumber.zero);
		bankCardMapper.insert(bankcard);
	}

	public void agyBankUpdate(AgyBankcard bankcard, String name) {
		AgyBankcard agyBankcard = new AgyBankcard();
		agyBankcard.setCardNo(bankcard.getCardNo());
		agyBankcard.setId(bankcard.getId());
		if (StringUtils.isNotEmpty(name)) {
			agyBankcard.setRealName(name);
		}
		int count = agentMapper.findBankExists(agyBankcard);
		if (count > Constants.EVNumber.zero) {
			throw new R200Exception("账号已经存在");
		}
		bankCardMapper.updateByPrimaryKeySelective(bankcard);
	}

	public void agyBankAvailable(AgyBankcard bankcard) {
		AgyBankcard bankcard1 = bankCardMapper.selectByPrimaryKey(bankcard.getId());
		bankcard1.setAvailable(bankcard.getAvailable());
		bankCardMapper.updateByPrimaryKey(bankcard1);
	}

	public void agyBankDelete(Integer id) {
		AgyBankcard bankcard1 = bankCardMapper.selectByPrimaryKey(id);
		bankcard1.setAvailable(Constants.EVNumber.zero);
		bankcard1.setIsDel(Constants.EVNumber.one);
		bankCardMapper.updateByPrimaryKey(bankcard1);
	}

	public String getSpreadCode() {
		String number;
		while (true) {
			number = CommonUtil.genRandom(8, 8);
			AgentAccount agentAccount = new AgentAccount();
			agentAccount.setSpreadCode(number);
			int count = agentAccountMapper.selectCount(agentAccount);
			if (count == 0) {
				break;
			}
		}
		return number;
	}

	public List<Integer> getAllLocalAgentAccount() {
		List<AgentAccount> agentAccounts = findTopAccountAll(null);
		if (Collections3.isNotEmpty(agentAccounts)) {
			return agentAccounts.stream().map(AgentAccount::getId).collect(Collectors.toList());
		}
		return null;
	}

	public int findAccountByName(String agyAccount) {
		AgentAccount account = new AgentAccount();
		account.setAgyAccount(agyAccount);
		return agentAccountMapper.selectCount(account);
	}

	public List<AgentAccount> findTopAccountAll(Integer parentId) {
		parentId = isNull(parentId) ? Constants.EVNumber.zero : parentId;
		AgentAccount agentAccount = new AgentAccount();
		agentAccount.setAvailable(Constants.EVNumber.one);
		agentAccount.setParentId(parentId);
		return agentMapper.findAccountList(agentAccount);
	}

	/**
	 * 获取所有总代
	 *
	 * @return
	 */
	public List<AgentAccount> findTopAccountAll() {
		return agentMapper.findGeneralAgent();
	}

	public List<AgentAccount> findBySubAgentPrentId(Integer parentId) {
		return agentMapper.findSubAgent(parentId);
	}

	public List<AgentAccount> findTopAccountLike(String agyAccount) {

		AgentAccount agentAccount = new AgentAccount();
		agentAccount.setAvailable(Constants.EVNumber.one);
		agentAccount.setAgyAccount(agyAccount);
		return agentMapper.findTopAccountLike(agentAccount);
	}

	public List<AgentAccount> findTopAccountAllIncludeDisable(Integer parentId) {
		parentId = isNull(parentId) ? Constants.EVNumber.zero : parentId;
		AgentAccount agentAccount = new AgentAccount();
		agentAccount.setParentId(parentId);
		return agentMapper.findAccountList(agentAccount);
	}

	public List<AgentAccount> getAllParentAccount(String parentIds, String enable) {
		AgentAccount agentAccount = new AgentAccount();
		agentAccount.setParentIds(parentIds);
		// 此处代理账号为总代,获取所有地区代理上对于的总代
		return agentMapper.getAgentAccountAuth(agentAccount);
	}

	/**
	 * 查询未配置域名的代理账号
	 *
	 * @return
	 */
	public List<Map<String, Object>> queryAgyCountNoUrl() {
		return agentMapper.queryAgyCountNoUrl();
	}

	public List<AgentTree> selectLevelsAgentTree() {
		List<AgentAccount> agentAccounts = agentAccountMapper.selectAll();
		return getAgentTreeTop(agentAccounts);
	}

	private List<AgentTree> getAgentTreeTop(List<AgentAccount> agentAccounts) {
		List<AgentTree> top = new LinkedList<>();
		for (AgentAccount agentAccount : agentAccounts) {
			if (nonNull(agentAccount.getParentId()) && agentAccount.getParentId() == 0) {
				AgentTree agentTree = new AgentTree(agentAccount.getId(), agentAccount.getAgyAccount(),
						agentAccount.getParentId(), null, false);
				getAgentTreeChildren(agentAccounts, agentTree);
				top.add(agentTree);
			}
		}
		return top;
	}

	private AgentTree getAgentTreeChildren(List<AgentAccount> agentAccounts, AgentTree treeTop) {
		List<AgentTree> children = new LinkedList<>();
		for (AgentAccount agentAccount : agentAccounts) {
			if (nonNull(agentAccount.getParentId()) && agentAccount.getParentId().equals(treeTop.getId())) {
				AgentTree child = new AgentTree(agentAccount.getId(), agentAccount.getAgyAccount(),
						agentAccount.getParentId(), null, false);
				getAgentTreeChildren(agentAccounts, child);
				children.add(child);
			}
		}
		treeTop.setChildren(children);
		return treeTop;
	}

	public List<AgentAccount> findAllSubAgency() {
		return agentMapper.findAllSubAgency();
	}

	public List<AgentAccount> findAllSubAgencyIncludeDisable() {
		return agentMapper.findAllSubAgencyIncludeDisable();
	}

	public List<AgentAccount> findSubAgencyByName(Integer agyAccountId) {
		return agentMapper.findSubAgencyByName(agyAccountId);
	}

	public AgentAccount findSubAgency(String agyAccount) {
		return agentMapper.findSubAgency(agyAccount);
	}

	public AgentAccount findAccountInfo(Integer id) {
		return agentAccountMapper.selectByPrimaryKey(id);
	}

	public List<AgentAccount> agentList(String agyAccount, Integer parentId) {
		AgentAccount account = new AgentAccount();
		account.setAgyAccount(agyAccount);
		account.setParentId(parentId);
		return agentMapper.findAgyAccountAndGrade(account);
	}

	public AgentAccount agyAccountAuditInfo(Integer id) {
		AgentAccount account = new AgentAccount();
		account.setId(id);
		List<AgentAccount> agentAccounts = agentMapper.findAgyAccountListPage(account);
		if (Collections3.isNotEmpty(agentAccounts)) {
			return agentAccounts.get(0);
		}
		return null;
	}

	public PageUtils getAgent(AgentAccount agentAccount, Integer pageNo, Integer pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		List<AgentAccount> list = agentMapper.getAgent(agentAccount);
		return BeanUtil.toPagedResult(list);
	}

	public List<AgentAccount> findAgentByloginName(String loginName) {
		return agentMapper.findAgentByloginName(loginName);
	}

	public List<AgentAccount> findAgentByAgyaccount(String agyAccount) {
		return agentMapper.findAgentByAgyaccount(agyAccount);
	}

	public List<AgentAccount> getAgentBanner(AgentAccount agyAccount) {
		return agentMapper.getAgentBanner(agyAccount);
	}

	public List<AgentAccount> getMbrBanner(AgentAccount agyAccount) {
		return agentMapper.getMbrBanner(agyAccount);
	}

	public void checkSubAccount(String loginName) {
		AgentSubAccount subAccount = new AgentSubAccount();
		subAccount.setAgyAccount(loginName);
		int subcount = subAccountMapper.selectCount(subAccount);
		if (subcount > 0) {
			throw new R200Exception("用户已经存在");
		}
	}

	/**
	 * 代理批量修改结算模式
	 * 
	 * @param agyAccount
	 * @param username
	 */
	public void updateFeeModel(AgentAccount agyAccount, String username) {
		agyAccount.getIds().stream().forEach(as -> {
			AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(as);
			if (nonNull(agentAccount)) {
				// 结算费模式
				agentAccount.setFeeModel(agyAccount.getFeeModel());
				// 服务费存款比例
				agentAccount.setDepositServicerate(agyAccount.getDepositServicerate());
				// 服务费取款比例
				agentAccount.setWithdrawServicerate(agyAccount.getWithdrawServicerate());
				// 平台费额外比例
				agentAccount.setAdditionalServicerate(agyAccount.getAdditionalServicerate());
				agentAccount.setModifyUser(username);
				agentAccount.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
				agentAccountMapper.updateByPrimaryKeySelective(agentAccount);
			}
		});
	}

	public void saveBankCard(AgyBankcard agyBankcard) {
		AgyBankcard agyBankcardQuery = new AgyBankcard();
		agyBankcardQuery.setBankName("支付宝");
		agyBankcardQuery.setAccountId(agyBankcard.getAccountId());
		agyBankcardQuery.setIsDel(Constants.EVNumber.zero);

		int count = bankCardMapper.selectCount(agyBankcardQuery);
		if (count == Constants.EVNumber.three) {
			throw new R200Exception("最多绑定3个账号");
		}
		agyBankcardQuery.setCardNo(agyBankcard.getCardNo());
		int countC = bankCardMapper.selectCount(agyBankcardQuery);
		if (countC > Constants.EVNumber.zero) {
			throw new R200Exception("账号号已经存在");
		}
		AgentAccount agyAccount = agentAccountMapper.selectByPrimaryKey(agyBankcard.getAccountId());
		if (isNull(agyAccount)) {
			throw new R200Exception("代理不存在");
		}

		// 支付宝wdenable无法开启，直接通过name和code查询支付宝
		BaseBank zfb = new BaseBank() {
			{
				setBankName("支付宝");
				setBankCode("ZFB");
			}
		};
		zfb = baseBankService.selectOne(zfb);
		agyBankcard.setBankCardId(zfb.getId());
		agyBankcard.setBankName(zfb.getBankName());

		agyBankcard.setAvailable(Constants.EVNumber.one);
		agyBankcard.setIsDel(Constants.EVNumber.zero);
		bankCardMapper.insert(agyBankcard);
	}

	private boolean checkPerm(AgentAccount agentAccount, Long userId) {
		boolean rt = true;
		List<SysMenuExtend> sysRoleMenuExtens = sysRoseMenuExtenService.getSysRosePermExtendByUserId(userId);
		Set<Long> agentAttrites = new HashSet<>();
		Set<Long> departmentIds = new HashSet<>();

		for (SysMenuExtend sysMenuExtend : sysRoleMenuExtens) {
			if (sysMenuExtend.getType() == Constants.EVNumber.six) {
				agentAttrites.add(sysMenuExtend.getRefId());
			} else if (sysMenuExtend.getType() == Constants.EVNumber.seven) {
				departmentIds.add(sysMenuExtend.getRefId());
			}
		}

		if (agentAccount.getAttributes() != null && !agentAttrites.contains(new Long(agentAccount.getAttributes()))) {
			rt = false;
		} else {
			agentAccount.setAttributesList(new ArrayList<>(agentAttrites));
		}

		if (!departmentIds.contains(new Long(0))) {
			if (agentAccount.getDepartmentid() != null
					&& !departmentIds.contains(new Long(agentAccount.getDepartmentid()))) {
				rt = false;
			} else {
				agentAccount.setDepartmentIdList(new ArrayList<>(departmentIds));
			}
		}

		if (agentAttrites.contains(SysConstants.AGENT_TYEP[0]) || agentAttrites.contains(SysConstants.AGENT_TYEP[1])) {
			agentAccount.setDepartmentIdIsNull(true);
		}
		return rt;
	}

	public int updateAgentRate(AgentAccount agentAccount) {
		List<AgyTree> agyTreeList = commissionCastMapper.findAgyTree(agentAccount.getId());
		if (agyTreeList.size() == 1 || agyTreeList.size() == 2) {
			return agentMapper.updateAgentRate(agentAccount.getId(), agentAccount.getRate());
		}
		return Constants.EVNumber.zero;
	}
}
