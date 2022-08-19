package com.wsdy.saasops.modules.agent.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.agent.dao.AgyChannelGroupMapper;
import com.wsdy.saasops.modules.agent.dao.AgyChannelMapper;
import com.wsdy.saasops.modules.agent.dto.AgyChannelGroupDto;
import com.wsdy.saasops.modules.agent.entity.AgyChannelGroup;
import com.wsdy.saasops.modules.base.service.BaseService;

/**
 * <p>
 * 渠道分組服务实现类
 * </p>
 *
 * @author ${author}
 * @since 2021-11-25
 */
@Service
public class AgyChannelGroupService extends BaseService<AgyChannelGroupMapper, AgyChannelGroup> {

	@Autowired
	private AgyChannelMapper agyChannelMapper;
	
    @Autowired
    private AgyChannelGroupMapper agyChannelGroupMapper;
    
    public PageUtils list(AgyChannelGroupDto agyChannelGroup, Integer pageNo, Integer pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		return BeanUtil.toPagedResult(agyChannelGroupMapper.list(agyChannelGroup));
	}

	/**
	 * 查询渠道组下是否有渠道，如果有不可删除
	 * 
	 * @param id
	 * @return
	 */
	public R delete(Integer id) {
		int count = agyChannelMapper.getCountByGroupId(id);
		if (count > 0) {
			throw new R200Exception("该分组下有渠道无法删除该分组");
		}
		return R.ok(deleteById(id));
	}

}
