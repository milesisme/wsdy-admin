package com.wsdy.saasops.modules.system.pay.service;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.modules.member.entity.MbrGroup;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.system.pay.dao.SetBacicFastPayGroupMapper;
import com.wsdy.saasops.modules.system.pay.dao.SetBacicFastPayMapper;
import com.wsdy.saasops.modules.system.pay.dao.SetBasicSysDepMbrMapper;
import com.wsdy.saasops.modules.system.pay.dao.SysDepositMapper;
import com.wsdy.saasops.modules.system.pay.entity.SetBacicFastPay;
import com.wsdy.saasops.modules.system.pay.entity.SetBasicFastPayGroup;
import com.wsdy.saasops.modules.system.pay.entity.SetBasicSysDepMbr;
import com.wsdy.saasops.modules.system.pay.entity.SysDeposit;
import com.wsdy.saasops.modules.system.pay.mapper.PayMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class SysDepositService {

	@Autowired
	private SysDepositMapper sysDepositMapper;
	@Autowired
	private PayMapper payMapper;
	@Autowired
	private SetBasicSysDepMbrMapper sysDepMbrMapper;
	@Autowired
	private SetBacicFastPayMapper fastPayMapper;
	@Autowired
	private MbrAccountLogService mbrAccountLogService;
	@Autowired
	private SetBacicFastPayGroupMapper fastPayGroupMapper;

	/**
	 * 银行卡列表
	 * 
	 * @param deposit
	 * @return
	 */
	public List<SysDeposit> queryList(SysDeposit deposit) {
		deposit.setIsDelete(Constants.EVNumber.zero);
		List<SysDeposit> deposits = payMapper.findDepositList(deposit);
		if (Collections3.isNotEmpty(deposits)) {
			deposits.forEach(d -> d.setGroupList(payMapper.findDepGroupByDepositId(d.getId())));
		}
		return deposits;
	}

	/**
	 *	 更新状态
	 * 
	 * @param deposit
	 * @param userName
	 * @param ip
	 */
	public void updateStatus(SysDeposit deposit, String userName, String ip) {
		SysDeposit sysDeposit = sysDepositMapper.selectByPrimaryKey(deposit.getId());
		if (sysDeposit == null) {
			throw new R200Exception("当前数据不存在，请重新刷新页面");
		}
		// isHot，isRecommend 只有一个为true
		if (deposit.getIsHot() != null && deposit.getIsRecommend() != null && deposit.getIsHot()
				&& deposit.getIsRecommend()) {
			throw new R200Exception("操作失败，推荐和热门按钮只允许同时勾选一个！");
		}
		
		sysDeposit.setIsHot(deposit.getIsHot());
		sysDeposit.setIsRecommend(deposit.getIsRecommend());
		sysDeposit.setAvailable(deposit.getAvailable());
		sysDeposit.setCreateUser(userName);
		sysDeposit.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
		sysDepositMapper.updateByPrimaryKey(sysDeposit);

		// 添加操作日志
		mbrAccountLogService.updateCompanyDepositStatusLog(sysDeposit, userName, ip);
	}

	public void companyDelete(Integer id, String userName, String ip) {
		SysDeposit sysDeposit = sysDepositMapper.selectByPrimaryKey(id);
		if (sysDeposit.getAvailable() == Constants.EVNumber.one) {
			throw new R200Exception("禁用状态才能删除");
		}
		sysDeposit.setIsDelete(Constants.EVNumber.one);
		sysDeposit.setAvailable(Constants.EVNumber.zero);
		sysDeposit.setModifyUser(userName);
		sysDeposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
		sysDepositMapper.updateByPrimaryKeySelective(sysDeposit);
		SetBasicSysDepMbr sysDepMbr = new SetBasicSysDepMbr();
		sysDepMbr.setDepositId(sysDeposit.getId());
		sysDepMbrMapper.delete(sysDepMbr);

		// 添加操作日志
		mbrAccountLogService.deleteCompanyDepositLog(sysDeposit, userName, ip);
	}

	public SysDeposit companyInfo(Integer id) {
		SysDeposit sysDeposit = sysDepositMapper.selectByPrimaryKey(id);
		sysDeposit.setGroupIds(getSetBasicSysDepMbrGroupIds(sysDeposit.getId()));
		return sysDeposit;
	}

	private List<Integer> getSetBasicSysDepMbrGroupIds(Integer depositId) {
		SetBasicSysDepMbr setBasicSysDepMbr = new SetBasicSysDepMbr();
		setBasicSysDepMbr.setDepositId(depositId);
		List<SetBasicSysDepMbr> sysDepMbrList = sysDepMbrMapper.select(setBasicSysDepMbr);
		if (Collections3.isNotEmpty(sysDepMbrList)) {
			return sysDepMbrList.stream().map(st -> st.getGroupId()).collect(Collectors.toList());
		}
		return null;
	}

	/**
	 * 保存银行卡
	 * 
	 * @param deposit
	 * @param userName
	 * @param ip
	 */
	public void companySave(SysDeposit deposit, String userName, String ip) {
		// 参数校验
		checkoutBankAccount(deposit);
		deposit.setIsDelete(Constants.EVNumber.zero);
		deposit.setCreateUser(userName);
		deposit.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
		deposit.setDepositAmount(BigDecimal.ZERO);
		deposit.setType(Constants.EVNumber.zero);
		sysDepositMapper.insert(deposit);

		insertSetBasicSysDepMbrList(deposit);
		// 添加操作日志
		mbrAccountLogService.addCompanyDepositLog(deposit, userName, ip);
	}

	private void insertSetBasicSysDepMbrList(SysDeposit deposit) {
		if (Collections3.isNotEmpty(deposit.getGroupIds())) {
			List<SetBasicSysDepMbr> groupsBankPayIsQueue = payMapper.getBankPayGroupIsQueue();
			List<SetBasicSysDepMbr> sysDepMbrs = Lists.newArrayList();
			for (int i = 0; i < deposit.getGroupIds().size(); i++) {
				SetBasicSysDepMbr sysDepMbr = new SetBasicSysDepMbr();
				Integer groupId = deposit.getGroupIds().get(i);
				sysDepMbr.setTier(Constants.EVNumber.one);
				sysDepMbr.setSort(i);
				sysDepMbr.setGroupId(groupId);
				sysDepMbr.setIsQueue(getBankPayIsQueue(groupId, groupsBankPayIsQueue));
				sysDepMbr.setDepositId(deposit.getId());
				sysDepMbrs.add(sysDepMbr);
			}
			sysDepMbrMapper.insertList(sysDepMbrs);
		}
	}

	/**
	 * 更新银行卡
	 * 
	 * @param deposit
	 * @param userName
	 * @param ip
	 */
	public void companyUpdate(SysDeposit deposit, String userName, String ip) {
		// 参数校验
		checkoutBankAccount(deposit);
		deposit.setModifyUser(userName);
		deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
		sysDepositMapper.updateByPrimaryKeySelective(deposit);

		List<Integer> sysDepMbrGroupIds = getSetBasicSysDepMbrGroupIds(deposit.getId());
		if (Objects.isNull(sysDepMbrGroupIds) || sysDepMbrGroupIds.size() == 0) {
			insertSetBasicSysDepMbrList(deposit);
		} else {
			updateSetBasicSysDepMbrGroupGroup(deposit, sysDepMbrGroupIds);
		}
		// 添加操作日志
		mbrAccountLogService.updateCompanyDepositLog(deposit, userName, ip);
	}

	private void updateSetBasicSysDepMbrGroupGroup(SysDeposit deposit, List<Integer> sysDepMbrGroupIds) {
		if (deposit.getGroupIds().size() == 0 && sysDepMbrGroupIds.size() > 0) {
			sysDepMbrGroupIds.stream().forEach(sy -> deleteSetBasicSysDepMbr(deposit.getId(), sy));
		}
		if (Collections3.isNotEmpty(deposit.getGroupIds())) {
			for (int i = 0; i < sysDepMbrGroupIds.size(); i++) {
				Boolean isDelete = Boolean.TRUE;
				for (int j = 0; j < deposit.getGroupIds().size(); j++) {
					if (sysDepMbrGroupIds.get(i).equals(deposit.getGroupIds().get(j))) {
						isDelete = Boolean.FALSE;
						break;
					}
				}
				if (Boolean.TRUE.equals(isDelete)) {
					deleteSetBasicSysDepMbr(deposit.getId(), sysDepMbrGroupIds.get(i));
				}
			}
			List<SetBasicSysDepMbr> groupsBankPayIsQueue = payMapper.getBankPayGroupIsQueue();
			for (int j = 0; j < deposit.getGroupIds().size(); j++) {
				Boolean isInsert = Boolean.TRUE;
				for (int i = 0; i < sysDepMbrGroupIds.size(); i++) {
					if (deposit.getGroupIds().get(j).equals(sysDepMbrGroupIds.get(i))) {
						isInsert = Boolean.FALSE;
						break;
					}
				}
				if (Boolean.TRUE.equals(isInsert)) {
					SetBasicSysDepMbr sysDepMbr = new SetBasicSysDepMbr();
					Integer groupId = deposit.getGroupIds().get(j);
					sysDepMbr.setGroupId(groupId);
					sysDepMbr.setDepositId(deposit.getId());
					sysDepMbr.setTier(Constants.EVNumber.one);
					sysDepMbr.setSort(payMapper.findSetBasicSysDepMbrMaxSort(sysDepMbr.getGroupId()));
					sysDepMbr.setIsQueue(getBankPayIsQueue(groupId, groupsBankPayIsQueue));
					sysDepMbrMapper.insert(sysDepMbr);
				}
			}
		}
	}

	private Integer getBankPayIsQueue(Integer groupId, List<SetBasicSysDepMbr> groupsBankPayIsQueue) {
		// 设置排队/平铺
		Optional<SetBasicSysDepMbr> sysDepMbrOptional = groupsBankPayIsQueue.stream()
				.filter(groupIsQueue -> groupId.equals(groupIsQueue.getGroupId())).findAny();
		if (sysDepMbrOptional.isPresent()) {
			return sysDepMbrOptional.get().getIsQueue();
		} else {
			return Constants.EVNumber.one;
		}
	}

	private void deleteSetBasicSysDepMbr(Integer depositId, Integer groupId) {
		SetBasicSysDepMbr setBasicSysDepMbr = new SetBasicSysDepMbr();
		setBasicSysDepMbr.setDepositId(depositId);
		setBasicSysDepMbr.setGroupId(groupId);
		sysDepMbrMapper.delete(setBasicSysDepMbr);
	}

	/**
	 * 新增，更新银行卡时的参数校验
	 * 
	 * @param bankAccount
	 * @param id
	 */
	private void checkoutBankAccount(SysDeposit deposit) {
		String bankAccount = deposit.getBankAccount();
		Integer id = deposit.getId();
		SysDeposit sysDeposit = payMapper.findDepositByBank(bankAccount, id);
		if (nonNull(sysDeposit)) {
			throw new R200Exception("操作失败，银行卡号已经存在!");
		}
		// isHot，isRecommend 只有一个为true
		if (deposit.getIsHot() != null && deposit.getIsRecommend() != null && deposit.getIsHot()
				&& deposit.getIsRecommend()) {
			throw new R200Exception("操作失败，推荐和热门按钮只允许同时勾选一个！");
		}
	}

	/**
	 * 全部的银行卡
	 * 
	 * @return
	 */
	public List<SysDeposit> querySysDepositList() {
		return sysDepositMapper.selectAll();
	}

	/**
	 * 	自动入款新增
	 * @param fastPay
	 * @param username
	 * @param ip
	 */
	public void fastPaySave(SetBacicFastPay fastPay, String username, String ip) {
		// isHot，isRecommend 只有一个为true
		if (fastPay.getIsHot() != null && fastPay.getIsRecommend() != null && fastPay.getIsHot()
				&& fastPay.getIsRecommend()) {
			throw new R200Exception("操作失败，推荐和热门按钮只允许同时勾选一个！");
		}
		fastPay.setIsDelete(Constants.EVNumber.zero);
		fastPay.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
		fastPay.setCreateUser(username);
		fastPayMapper.insert(fastPay);
		insertFastBank(fastPay, username);

		insertSetBasicFastPayGroupList(fastPay);
		// 添加操作日志
		mbrAccountLogService.addAutoDepositLog(fastPay, username, ip);
	}

	private void insertSetBasicFastPayGroupList(SetBacicFastPay fastPay) {
		if (Collections3.isNotEmpty(fastPay.getGroupIds())) {
			List<SetBasicFastPayGroup> fastPayGroups = Lists.newArrayList();
			List<SetBasicFastPayGroup> fastPayGroupsIsQueue = payMapper.getFastPayGroupIsQueue();
			for (int i = 0; i < fastPay.getGroupIds().size(); i++) {
				SetBasicFastPayGroup fastPayGroup = new SetBasicFastPayGroup();
				Integer groupId = fastPay.getGroupIds().get(i);
				fastPayGroup.setGroupId(groupId);
				fastPayGroup.setFastPayId(fastPay.getId());
				fastPayGroup.setSort(payMapper.findFastPayGroupMaxSort(groupId));
				fastPayGroup.setIsQueue(getFastPayIsQueue(groupId, fastPayGroupsIsQueue));
				fastPayGroups.add(fastPayGroup);
			}
			fastPayGroupMapper.insertList(fastPayGroups);
		}
	}

	private void insertFastBank(SetBacicFastPay fastPay, String username) {
		if (fastPay.getDeposits() != null) {
			fastPay.getDeposits().stream().forEach(fs -> {
				SysDeposit deposit = new SysDeposit();
				deposit.setIsDelete(Constants.EVNumber.zero);
				deposit.setBankAccount(fs.getBankAccount());
				int count = sysDepositMapper.selectCount(deposit);
				if (count > 0) {
					throw new R200Exception("银行卡号已经存在");
				}
				fs.setFastPayId(fastPay.getId());
				fs.setIsDelete(Constants.EVNumber.zero);
				fs.setAvailable(fastPay.getAvailable());
				fs.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
				fs.setCreateUser(username);
				fs.setDepositAmount(BigDecimal.ZERO);
				fs.setType(Constants.EVNumber.one);
				fs.setIsShow(Constants.EVNumber.one);
				fs.setIsHot(false);
				fs.setIsRecommend(false);
			});
			sysDepositMapper.insertList(fastPay.getDeposits());
		}
	}

	public void fastPayUpdate(SetBacicFastPay fastPay, String username, String ip) {
		SetBacicFastPay bacicFastPay = fastPayMapper.selectByPrimaryKey(fastPay.getId());
		if (nonNull(bacicFastPay)) {
			// isHot，isRecommend 只有一个为true
			if (fastPay.getIsHot() != null && fastPay.getIsRecommend() != null && fastPay.getIsHot()
					&& fastPay.getIsRecommend()) {
				throw new R200Exception("操作失败，推荐和热门按钮只允许同时勾选一个！");
			}
			bacicFastPay.setIsHot(fastPay.getIsHot());
			bacicFastPay.setIsRecommend(fastPay.getIsRecommend());
			bacicFastPay.setPayId(fastPay.getPayId());
			bacicFastPay.setPassword(fastPay.getPassword());
			bacicFastPay.setAvailable(fastPay.getAvailable());
			bacicFastPay.setName(fastPay.getName());
			bacicFastPay.setFeeWay(fastPay.getFeeWay());
			bacicFastPay.setFeeTop(fastPay.getFeeTop());
			bacicFastPay.setFeeScale(fastPay.getFeeScale());
			bacicFastPay.setFeeFixed(fastPay.getFeeFixed());
			bacicFastPay.setAmountType(fastPay.getAmountType());
			bacicFastPay.setModifyUser(username);
			bacicFastPay.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
			bacicFastPay.setShowName(fastPay.getShowName());
			bacicFastPay.setAlipayFlg(fastPay.getAlipayFlg()); // 支付宝转卡标志
			// 全量更新fastpay(null也更新)
			fastPayMapper.updateByPrimaryKey(fastPay);

			// 添加操作日志
			mbrAccountLogService.updateAutoDepositLog(fastPay, username, ip);

			SysDeposit sysDeposit = new SysDeposit();
			sysDeposit.setFastPayId(fastPay.getId());
			List<SysDeposit> deposits = sysDepositMapper.select(sysDeposit);

			// 逻辑删除前端删除的银行卡(depsit)
			updateFastPayDeleteBank(deposits, fastPay);
			// 更新之前已存在的银行卡
			updateFastPayBank(deposits, fastPay);
			// 插入新增的银行卡
			updateFastPayInsertBank(fastPay, bacicFastPay.getId());
			// 更新会员组关联
			updateSetBasicFastPayGroup(fastPay);
		}
	}

	private void updateSetBasicFastPayGroup(SetBacicFastPay fastPay) {
		// 查询自动入款的旧的会员组
		List<MbrGroup> mbrGroups = payMapper.findFastPayGroupById(fastPay.getId());
		// 无关联会员组，直接插入会员组关联关系(新增自动入款渠道时)
		if (mbrGroups.size() == 0) {
			insertSetBasicFastPayGroupList(fastPay);
		} else { // 存在会员组关联(修改自动入款渠道时)
			// 前端上送groupIds为空 ，且存在关联会员组，则删除所有会员组关联
			if (fastPay.getGroupIds().size() == 0 && mbrGroups.size() > 0) {
				mbrGroups.stream().forEach(os -> deleteSetBasicPaymbrGroupRelation(fastPay.getId(), os.getId()));
				log.info("updateSetBasicFastPayGroup==id==" + fastPay.getId());
			}
			// 前端上送groupIds不为空
			if (Collections3.isNotEmpty(fastPay.getGroupIds())) {
				// 1. 找出旧的会员组中不在此次变更的groupIds中的，并删除该会员组关联：先删除旧的排除掉的，再插入新的
				for (int i = 0; i < mbrGroups.size(); i++) {
					Boolean isDelete = Boolean.TRUE;
					for (int j = 0; j < fastPay.getGroupIds().size(); j++) {
						if (mbrGroups.get(i).getId().equals(fastPay.getGroupIds().get(j))) {
							isDelete = Boolean.FALSE;
							break;
						}
					}
					if (Boolean.TRUE.equals(isDelete)) {
						deleteSetBasicPaymbrGroupRelation(fastPay.getId(), mbrGroups.get(i).getId());
					}
				}
				// 查询所有的会员组的支付渠道的排序： 用于设置排序状态，原先有排序的，使用原先，没有则默认为“排序”
				List<SetBasicFastPayGroup> fastPayGroupsIsQueue = payMapper.getFastPayGroupIsQueue();
				// 2. 找出此次新增的会员组，插入会员组关联
				for (int j = 0; j < fastPay.getGroupIds().size(); j++) {
					Boolean isInsert = Boolean.TRUE;
					for (int i = 0; i < mbrGroups.size(); i++) {
						if (fastPay.getGroupIds().get(j).equals(mbrGroups.get(i).getId())) {
							isInsert = Boolean.FALSE;
							break;
						}
					}
					if (Boolean.TRUE.equals(isInsert)) {
						SetBasicFastPayGroup fastPayGroup = new SetBasicFastPayGroup();
						Integer groupId = fastPay.getGroupIds().get(j);
						fastPayGroup.setGroupId(groupId);
						fastPayGroup.setFastPayId(fastPay.getId());
						fastPayGroup.setSort(payMapper.findFastPayGroupMaxSort(fastPayGroup.getGroupId()));
						fastPayGroup.setIsQueue(getFastPayIsQueue(groupId, fastPayGroupsIsQueue)); // 设置平铺/排序
						fastPayGroupMapper.insert(fastPayGroup);
					}
				}
			}
		}
	}

	private Integer getFastPayIsQueue(Integer groupId, List<SetBasicFastPayGroup> fastPayGroupsIsQueue) {
		// 设置排队/平铺
		Optional<SetBasicFastPayGroup> groupRelationOptional = fastPayGroupsIsQueue.stream()
				.filter(groupIsQueue -> groupId.equals(groupIsQueue.getGroupId())).findAny();
		if (groupRelationOptional.isPresent()) {
			return groupRelationOptional.get().getIsQueue();
		} else {// 默认排队
			return Constants.EVNumber.one;
		}
	}

	private void deleteSetBasicPaymbrGroupRelation(Integer fastPayId, Integer groupId) {
		SetBasicFastPayGroup fastPayGroup = new SetBasicFastPayGroup();
		fastPayGroup.setFastPayId(fastPayId);
		fastPayGroup.setGroupId(groupId);
		fastPayGroupMapper.delete(fastPayGroup);
	}

	private void updateFastPayBank(List<SysDeposit> deposits, SetBacicFastPay fastPay) {
		for (SysDeposit deposit : fastPay.getDeposits()) { // 此次更新的银行卡 deposit
			SysDeposit upDeposit = null; // 最后保存的银行卡 upDeposit
			for (SysDeposit updateDeposit : deposits) { // 原来的银行卡 updateDeposit
				if (updateDeposit.getId().equals(deposit.getId())) { // 此次更新的银行卡已存在
					upDeposit = updateDeposit; // 则引用数据库已存在的银行卡
				}
			}
			if (nonNull(upDeposit)) { // 只更新已存在的银行卡
				int count = payMapper.findFastDepositCount(deposit.getId(), upDeposit.getBankAccount()); // 查询除本卡以外未删除的卡
				if (count > 0) {
					throw new R200Exception("银行卡号已经存在");
				}
//                upDeposit.setBankBranch(deposit.getBankBranch());
//                upDeposit.setRealName(deposit.getRealName());
//                upDeposit.setBankId(deposit.getBankId());
//                upDeposit.setBankName(deposit.getBankName());
//                upDeposit.setMinAmout(deposit.getMinAmout());
//                upDeposit.setDayMaxAmout(deposit.getDayMaxAmout());
//                upDeposit.setMaxAmout(deposit.getMaxAmout());
//                upDeposit.setAvailable(fastPay.getAvailable());
//                upDeposit.setModifyUser(fastPay.getModifyUser());
//                upDeposit.setModifyTime(fastPay.getModifyTime());
				deposit.setAvailable(fastPay.getAvailable());
				deposit.setModifyUser(fastPay.getModifyUser());
				deposit.setModifyTime(fastPay.getModifyTime());
				// sysDepositMapper.updateByPrimaryKeySelective(deposit); //
				// 前端原样上传，所以此处可以直接更新这个值
				// 需要删除限额模式的其中一个
				sysDepositMapper.updateByPrimaryKey(deposit); // 前端原样上传，所以此处可以直接更新这个值
			}
		}
	}

	private void updateFastPayInsertBank(SetBacicFastPay fastPay, Integer fastPayId) {
		for (SysDeposit addDeposit : fastPay.getDeposits()) {
			if (isNull(addDeposit.getId())) {
				SysDeposit deposit = new SysDeposit();
				deposit.setIsDelete(Constants.EVNumber.zero);
				deposit.setBankAccount(addDeposit.getBankAccount());
				int count = sysDepositMapper.selectCount(deposit);
				if (count > 0) {
					throw new R200Exception("银行卡号已经存在");
				}
				addDeposit.setFastPayId(fastPayId);
				addDeposit.setIsDelete(Constants.EVNumber.zero);
				addDeposit.setAvailable(fastPay.getAvailable());
				addDeposit.setCreateUser(fastPay.getModifyUser());
				addDeposit.setCreateTime(fastPay.getModifyTime());
				addDeposit.setDepositAmount(BigDecimal.ZERO);
				addDeposit.setType(Constants.EVNumber.one);
				addDeposit.setIsShow(Constants.EVNumber.one);
				sysDepositMapper.insert(addDeposit);
			}
		}
	}

	private void updateFastPayDeleteBank(List<SysDeposit> deposits, SetBacicFastPay fastPay) {
		for (SysDeposit deposit : deposits) {
			Boolean isDelete = Boolean.TRUE;
			for (SysDeposit updateDeposit : fastPay.getDeposits()) {
				if (deposit.getId().equals(updateDeposit.getId())) {
					isDelete = Boolean.FALSE;
				}
			}
			if (Boolean.TRUE.equals(isDelete)) {
				deposit.setIsDelete(Constants.EVNumber.one);
				deposit.setAvailable(Constants.EVNumber.zero);
				deposit.setModifyUser(fastPay.getModifyUser());
				deposit.setModifyTime(fastPay.getModifyTime());
				sysDepositMapper.updateByPrimaryKey(deposit);
			}
		}
	}

	/**
	 * 自动入款列表
	 * 
	 * @param fastPay
	 * @return
	 */
	public List<SetBacicFastPay> fastPayList(SetBacicFastPay fastPay) {
		List<SetBacicFastPay> fastPayList = payMapper.findBasicFastPay(fastPay);
		if (Collections3.isNotEmpty(fastPayList)) {
			fastPayList.forEach(p -> {
				p.setGroupList(payMapper.findFastPayGroupById(p.getId()));
				if (Collections3.isNotEmpty(p.getGroupList())) {
					p.setGroupIds(p.getGroupList().stream().map(st -> st.getId()).collect(Collectors.toList()));
				}
				if (Boolean.TRUE.equals(fastPay.getIsAllocation())) {
					p.setDeposits(findSysDepositByFastPayId(p.getId()));
				} else {
					p.setDeposits(payMapper.findSysDepositByFastPayId(p.getId()));
				}
			});
			return fastPayList;
		}
		return null;
	}

	public SetBacicFastPay fastPayInfo(Integer id) {
		SetBacicFastPay fastPay = new SetBacicFastPay();
		fastPay.setId(id);
		return Optional.ofNullable(fastPayList(fastPay).stream().findAny()).get().orElse(null);
	}

	/**
	 * 	自动入款状态更新
	 * 
	 * @param fastPay
	 * @param username
	 * @param ip
	 */
	public void fastPayAvailable(SetBacicFastPay fastPay, String username, String ip) {
		SetBacicFastPay bacicFastPay = fastPayMapper.selectByPrimaryKey(fastPay.getId());
		if (nonNull(bacicFastPay)) {
			// isHot，isRecommend 只有一个为true
			if (fastPay.getIsHot() != null && fastPay.getIsRecommend() != null && fastPay.getIsHot()
					&& fastPay.getIsRecommend()) {
				throw new R200Exception("操作失败，推荐和热门按钮只允许同时勾选一个！");
			}
			bacicFastPay.setIsHot(fastPay.getIsHot());
			bacicFastPay.setIsRecommend(fastPay.getIsRecommend());
			bacicFastPay.setAvailable(fastPay.getAvailable());
			bacicFastPay.setModifyUser(username);
			bacicFastPay.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
			fastPayMapper.updateByPrimaryKey(bacicFastPay);
			updateSysDepositByFastPayId(bacicFastPay);

			// 添加操作日志
			mbrAccountLogService.updateAutoDepositStatusLog(bacicFastPay, username, ip);
		}
	}

	public void fastPayDelete(Integer id, String username, String ip) {
		SetBacicFastPay bacicFastPay = fastPayMapper.selectByPrimaryKey(id);
		if (nonNull(bacicFastPay)) {
			if (bacicFastPay.getAvailable() == Constants.EVNumber.one) {
				throw new R200Exception("禁用状态才能删除");
			}
			bacicFastPay.setIsDelete(Constants.EVNumber.one);
			bacicFastPay.setAvailable(Constants.EVNumber.zero);
			bacicFastPay.setModifyUser(username);
			bacicFastPay.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
			fastPayMapper.updateByPrimaryKey(bacicFastPay);
			updateSysDepositByFastPayId(bacicFastPay);

			// 添加操作日志
			mbrAccountLogService.deleteAutoDepositLog(bacicFastPay, username, ip);
		}
	}

	private void updateSysDepositByFastPayId(SetBacicFastPay bacicFastPay) {
		List<SysDeposit> sysDepositList = findSysDepositByFastPayId(bacicFastPay.getId());
		if (Collections3.isNotEmpty(sysDepositList)) {
			sysDepositList.stream().forEach(s -> {
				s.setAvailable(bacicFastPay.getAvailable());
				s.setIsDelete(bacicFastPay.getIsDelete());
				s.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
				s.setModifyUser(bacicFastPay.getModifyUser());
				sysDepositMapper.updateByPrimaryKey(s);
			});
		}
	}

	private List<SysDeposit> findSysDepositByFastPayId(Integer fastPayId) {
		SysDeposit deposit = new SysDeposit();
		deposit.setFastPayId(fastPayId);
		deposit.setType(Constants.EVNumber.one);
		deposit.setIsDelete(Constants.EVNumber.zero);
		return sysDepositMapper.select(deposit);
	}
}
