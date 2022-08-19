package com.wsdy.saasops.modules.member.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrGroup;

import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MbrGroupAutoUpdateService {

	private static final String MBR_GROUP_JOB_LOCK_KEY = "MBR_GROUP_JOB_LOCK_KEY";

	@Autowired
	private MbrAccountService mbrAccountService;
	
	@Autowired
	private RedisService redisService;
	
	@Autowired
	private MbrGroupService mbrGroupService;
	
    @Autowired
    private MbrAccountLogService accountLogService;

	/**
     * 会员组升级流程
     * a.获取所有mbr_group.isLockUpgrade = 0的数据
     * b.循环mbr_group数组,调用sql，当前mbr_group对象A以及下一级对象B的条件获取用户id
     * sql查询逻辑
     *   b.1 根据rpt_bet_rcd_day获得用户累计公司输赢  累计有效投注
     *	 b.2  根据fund_deposit获得用户累计存款  累计存款次数  
     *	 b.3 根据mbr_group传入的两个对象进行范围比较，当大于 A 的条件，并且 小于B的条件的用户id
     * c.循环分批更新用户groupid
     * 	
     */
    @Transactional
	public void mbrGroupAutoUpdate(String siteCode, Boolean queryRecent) {
		boolean flag = redisService.setRedisExpiredTimeBo(MBR_GROUP_JOB_LOCK_KEY + siteCode, MBR_GROUP_JOB_LOCK_KEY + siteCode, 100,
				TimeUnit.SECONDS);
		if (!flag) {
			log.info("{} 会员组升级流程重复调用，本次调用结束！ ", siteCode);
			return;
		}
		try {
			long start = System.currentTimeMillis();
			log.info("siteCode: {} 会员组升级开始", siteCode);
			MbrGroup mbrGroupQuery = new MbrGroup();
			mbrGroupQuery.setIsLockUpgrade(0);
			mbrGroupQuery.setAvailable((byte) 1);
			// 1.获取所有mbr_group.isLockUpgrade = 0 并且状态是开启的会员组数据
			List<MbrGroup> mbrGroupList = mbrGroupService.queryListCond(mbrGroupQuery);
			if (Collections3.isEmpty(mbrGroupList)) {
				log.info("siteCode: {} 会员组升级结束，用户组都为锁定升级", siteCode);
				return;
			}
			// 根据充值金额排序：规定deposit 由小到大
			mbrGroupList.sort(Comparator.comparing(MbrGroup::getDeposit));
			int updateCount = 0;
			for (int i = 0; i < mbrGroupList.size(); i++) {
				MbrGroup nextGroup = null;
				// 下一个还有
				if (i + 1 < mbrGroupList.size()) {
					for (int j = i + 1; j < mbrGroupList.size(); j++) {
						MbrGroup newNextGroup = mbrGroupList.get(j);
						// 如果下一个的配置是空
						if(newNextGroup.getDeposit() == null || newNextGroup.getDeposit().compareTo(BigDecimal.ZERO) == 0) {
							continue;
						}
						nextGroup = newNextGroup;
						break;
					}
				}

				List<MbrAccount> mbrAccountList = mbrAccountService.selectAccountIdsForGroupJob(mbrGroupList.get(i),
						nextGroup, queryRecent);
				log.info("siteCode: {} 会员组升级 会员组对象:{} ,查询到需要升级的用户: {}", siteCode, mbrGroupList.get(i).getGroupName(),
							mbrAccountList.stream().map(MbrAccount :: getLoginName).collect(Collectors.toSet()));
				if (!Collections.isEmpty(mbrAccountList)) {
					int batchUpdateManyGroupidForJob = this.batchUpdateManyGroupidForJob(mbrAccountList, mbrGroupList.get(i).getId(), siteCode, mbrGroupList.get(i).getGroupName());
					updateCount = updateCount + batchUpdateManyGroupidForJob;
				}
	
			}
			log.info("siteCode: {} 会员组升级结束,更新用户条数: {} ，耗时: {} ", siteCode, updateCount, System.currentTimeMillis() - start);
		} catch (Exception e) {
			log.info("会员组升级失败", e);
			throw e;
		} finally {
			redisService.del(MBR_GROUP_JOB_LOCK_KEY + siteCode);
		}
		
	}

	/**
	 * 分批处理
	 * 
	 * @param mbrAccountIdList
	 * @param groupId
	 * @return
	 */
	public int batchUpdateManyGroupidForJob(List<MbrAccount> mbrAccountList, Integer groupId, String siteCode, String groupName) {
		
		List<Integer> mbrAccountIdList = mbrAccountList.stream().map(MbrAccount :: getId).collect(Collectors.toList());
		int oneGroupIdUpdateCount = 0;
		// d一次oneTimeCount条，分updateTimes次插入
		int updateTimes = (mbrAccountIdList.size() / Constants.BATCH_ONCE_COUNT) + 1;
		// d最后一次插入lastCount条，取余数
		int lastCount = mbrAccountIdList.size() % Constants.BATCH_ONCE_COUNT;
		// d插入，会循环
		for (int i = 0; i < updateTimes; i++) {
			int start = i * Constants.BATCH_ONCE_COUNT;
			int end = (i + 1) * Constants.BATCH_ONCE_COUNT;
			// d声明一个list集合,用于存放每次批量插入的数据
			List<Integer> queryListMbrAccount = new ArrayList<>(Constants.BATCH_ONCE_COUNT);
			if (i == updateTimes - 1) {
				// subList(index, end)，list集合的下标从0开始,[start,end) subList包含start,不包含end。
				// d最后一次插入 subList(end, end+lastNumber)
				queryListMbrAccount = mbrAccountIdList.subList(start, start + lastCount);
			} else {
				// d非最后一次插入 subList(start, end)
				queryListMbrAccount = mbrAccountIdList.subList(start, end);
			}
			int count = mbrAccountService.updateManyGroupidForJob(queryListMbrAccount, groupId);
			oneGroupIdUpdateCount = count + oneGroupIdUpdateCount;
		}
		accountLogService.addMbrAccountLogBatch(mbrAccountList, groupName);
		return oneGroupIdUpdateCount;
	}
	
	

}
