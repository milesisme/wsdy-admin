package com.wsdy.saasops.modules.member.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.google.common.base.Splitter;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.member.dao.MbrLabelMapper;
import com.wsdy.saasops.modules.member.entity.MbrLabel;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;

/**
 * <p>
 * 	会员标签表 服务实现类
 * </p>
 *
 * @author ${author}
 * @since 2022-05-06
 */
@Service
public class MbrLabelService extends BaseService<MbrLabelMapper, MbrLabel> {
	
	@Autowired
	private MbrMapper mbrMapper;
	
    @Autowired
    private MbrLabelMapper mbrLabelMapper;

	public PageUtils listPage(MbrLabel mbrLabel, Integer pageNo, Integer pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		List<MbrLabel> list = mbrLabelMapper.listPage(mbrLabel);
        return BeanUtil.toPagedResult(list);
	}

	public Integer updateAvailable(Integer id, Boolean isAvailable) {
		return mbrLabelMapper.updateAvailable(id, isAvailable);
	}

	public int checkNameCount(String mbrLabelName, Integer id) {
		return mbrLabelMapper.checkNameCount(mbrLabelName, id);
	}

	public int setMbrLabel(Integer labelid, String userNames) {
		 Iterable<String> userList = Splitter.on(",").trimResults().omitEmptyStrings().split(userNames);
		 return mbrLabelMapper.setMbrLabel(labelid, userList);
	}

	@Transactional
	public int deleteOne(MbrLabel mbrLabel) {
		// 更新所有当前标签的用户为1
		mbrMapper.updateMbrLabel(mbrLabel.getId(), 1);
		// 删除当前标签
		return this.deleteById(mbrLabel.getId());
	}

}
