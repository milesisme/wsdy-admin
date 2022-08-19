package com.wsdy.saasops.modules.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.member.dao.MbrBillDetailMapper;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import com.github.pagehelper.PageHelper;

@Service
public class MbrBillDetailService extends BaseService<MbrBillDetailMapper, MbrBillDetail> {

	public PageUtils queryListPage(MbrBillDetail mbrBillDetail, Integer pageNo, Integer pageSize, String orderBy) {
		PageHelper.startPage(pageNo, pageSize);
		if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
		List<MbrBillDetail> list = queryListCond(mbrBillDetail);
		return BeanUtil.toPagedResult(list);
	}
}
