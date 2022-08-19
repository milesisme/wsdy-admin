package com.wsdy.saasops.modules.member.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.member.dao.MbrDepositCondMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrDepositCond;


@Service
public class MbrDepositCondService extends BaseService<MbrDepositCondMapper, MbrDepositCond> {

	@Autowired
	MbrAccountService mbrAccountService;

	public int selectCountNo(Integer groupId) {
		MbrDepositCond mbrDepositCond = new MbrDepositCond();
		mbrDepositCond.setGroupId(groupId);
		return super.selectCount(mbrDepositCond);
	}

	public MbrDepositCond getMbrDeposit(Integer accountId) {
		MbrAccount mbrAccount = mbrAccountService.getAccountInfo(accountId);
		MbrDepositCond mbrDepositCond = new MbrDepositCond();
		mbrDepositCond.setGroupId(mbrAccount.getGroupId());
		return super.queryObjectCond(mbrDepositCond);
	}
}
