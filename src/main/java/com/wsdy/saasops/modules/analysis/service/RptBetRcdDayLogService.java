package com.wsdy.saasops.modules.analysis.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.analysis.entity.RptBetRcdDayLog;
import com.wsdy.saasops.modules.analysis.mapper.RptBetRcdDayLogMapper;
import com.wsdy.saasops.modules.base.service.BaseService;

@Service
public class RptBetRcdDayLogService extends BaseService<RptBetRcdDayLogMapper, RptBetRcdDayLog> {

	@Autowired
	private RptBetRcdDayLogMapper rptBetRcdDayLogMapper;

	public PageUtils list(RptBetRcdDayLog rptBetRcdDayLog) {
		PageHelper.startPage(rptBetRcdDayLog.getPageNo(), rptBetRcdDayLog.getPageSize());
		List<RptBetRcdDayLog> list = rptBetRcdDayLogMapper.selectList(rptBetRcdDayLog);
		return BeanUtil.toPagedResult(list);
	}

}
