package com.wsdy.saasops.modules.operate.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.operate.dao.HelpGuessAskMapper;
import com.wsdy.saasops.modules.operate.entity.HelpGuessAsk;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ${author}
 * @since 2022-05-21
 */
@Service
public class HelpGuessAskService extends BaseService<HelpGuessAskMapper, HelpGuessAsk> {
	
	@Autowired
    private HelpGuessAskMapper helpGuessAskMapper;

	public PageUtils queryListPage(Integer pageNo, Integer pageSize, Boolean isOpen) {
		PageHelper.startPage(pageNo, pageSize);
		List<HelpGuessAsk> result = helpGuessAskMapper.queryListPage(isOpen);
		return BeanUtil.toPagedResult(result);
	}

}
