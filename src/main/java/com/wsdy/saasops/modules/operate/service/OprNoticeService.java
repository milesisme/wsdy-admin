package com.wsdy.saasops.modules.operate.service;

import java.util.List;
import java.util.Objects;

import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wsdy.saasops.common.constants.GroupByConstants;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.operate.dao.OprNoticeMapper;
import com.wsdy.saasops.modules.operate.entity.OprNotice;
import com.wsdy.saasops.modules.operate.mapper.OperateMapper;
import com.github.pagehelper.PageHelper;

@Service
public class OprNoticeService extends BaseService<OprNoticeMapper, OprNotice> {

	@Autowired
	private OperateMapper operateMapper;
	@Autowired
	private OprNoticeMapper oprNoticeMapper;
	@Autowired
	private MbrAccountLogService mbrAccountLogService;

	public PageUtils queryListPage(OprNotice oprNotice, Integer pageNo, Integer pageSize, String orderBy) {
		PageHelper.startPage(pageNo, pageSize);
		orderBy=GroupByConstants.getOrderBy(GroupByConstants.noticeMod, orderBy);
			PageHelper.orderBy(orderBy);
		List<OprNotice> list = operateMapper.selectNoticeList(oprNotice);
		return BeanUtil.toPagedResult(list);
	}

	public List<OprNotice> queryValidListPage() {
		List<OprNotice> list = operateMapper.queryValidListPage();
		return list;
	}

	public Object queryNoticeListPage(String showType,Integer pageNo, Integer pageSize, String orderBy) {
		PageHelper.startPage(pageNo, pageSize);
		List<OprNotice> list = operateMapper.queryNoticeList(showType);
		return BeanUtil.toPagedResult(list);
		

	}

	public void deleteBatch(Integer[] ids, String userName, String ip) {
		if (Objects.nonNull(ids)) {
			String idStr = StringUtil.join(",", ids);
			List<OprNotice> noticeList = oprNoticeMapper.selectByIds(idStr);
			oprNoticeMapper.deleteByIds(idStr);

			for(OprNotice oprNotice:noticeList){
				//操作日志
				mbrAccountLogService.deleteOprNoticelog(oprNotice, userName, ip);
			}
		}
	}

}
