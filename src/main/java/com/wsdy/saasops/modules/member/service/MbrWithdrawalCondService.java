package com.wsdy.saasops.modules.member.service;

import java.util.List;

import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.member.dao.MbrWithdrawalCondMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrWithdrawalCond;
import com.github.pagehelper.PageHelper;

@Service
public class MbrWithdrawalCondService extends BaseService<MbrWithdrawalCondMapper, MbrWithdrawalCond> {
	@Autowired
	MbrAccountService mbrAccountService;
	@Autowired
	private FundMapper fundMapper;

	public PageUtils queryListPage(MbrWithdrawalCond mbrWithdrawalCond, Integer pageNo, Integer pageSize,
			String orderBy) {
		PageHelper.startPage(pageNo, pageSize);
		if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
		List<MbrWithdrawalCond> list = queryListCond(mbrWithdrawalCond);
		return BeanUtil.toPagedResult(list);
	}

	public int selectCountNo(Integer groupId) {
		MbrWithdrawalCond mbrWithdrawalCond = new MbrWithdrawalCond();
		mbrWithdrawalCond.setGroupId(groupId);
		return super.selectCount(mbrWithdrawalCond);
	}

	/**
	 * 	根据会员id查询当前会员的会员组，根据会员组查询取款手续费比例
	 * @param accountId
	 * @return
	 */
	public MbrWithdrawalCond getMbrWithDrawal(Integer accountId) {
		MbrAccount mbrAccount = mbrAccountService.getAccountInfo(accountId);
		MbrWithdrawalCond mbrDepositCond = new MbrWithdrawalCond();
		mbrDepositCond.setGroupId(mbrAccount.getGroupId());
		return super.queryObjectCond(mbrDepositCond);
	}

	public AccWithdraw sumWithDraw(Integer accountId) {
		String startTime = DateUtil.getTodayStart(DateUtil.FORMAT_10_DATE);
		String endTime = DateUtil.getTodayEnd(DateUtil.FORMAT_10_DATE);
		return fundMapper.sumWithDraw(startTime, endTime, accountId);
	}
}
