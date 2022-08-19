package com.wsdy.saasops.modules.agent.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.util.StringUtil;
import com.wsdy.saasops.api.modules.apisys.entity.TcpSiteurl;
import com.wsdy.saasops.api.modules.apisys.mapper.ApiSysMapper;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.agent.dao.AgyChannelMapper;
import com.wsdy.saasops.modules.agent.dto.AgyChannelDto;
import com.wsdy.saasops.modules.agent.dto.AgyChannelForApiDto;
import com.wsdy.saasops.modules.agent.entity.AgyChannel;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.system.systemsetting.dto.PromotionSet;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;

import io.jsonwebtoken.lang.Collections;

/**
 * <p>
 * 渠道服务实现类
 * </p>
 *
 * @author ${author}
 * @since 2021-11-25
 */
@Service
public class AgyChannelService extends BaseService<AgyChannelMapper, AgyChannel> {

	@Autowired
	private AgyChannelMapper agyChannelMapper;

	@Autowired
	private ApiSysMapper apiSysMapper;

	@Autowired
	private SysSettingService sysSettingService;

	/**
	 * 后台用分页查询
	 * 
	 * @param agyChannelDto
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public PageUtils list(AgyChannelDto agyChannelDto, Integer pageNo, Integer pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		String channelPromotionUrl = "";
		// 获取当前站点的渠道推广域名id
		PromotionSet queryChannelPromotionSet = sysSettingService.queryChannelPromotionSet();
		if(queryChannelPromotionSet != null) {
			// 获取渠道的推广域名
			TcpSiteurl tcpSiteurl = new TcpSiteurl();
			tcpSiteurl.setSiteCode(CommonUtil.getSiteCode());
			tcpSiteurl.setId(queryChannelPromotionSet.getSiteUrlId());
			List<TcpSiteurl> findCpSiteUrlBySiteCode = apiSysMapper.findCpSiteUrlBySiteCode(tcpSiteurl);
			if (!Collections.isEmpty(findCpSiteUrlBySiteCode)) {
				Optional<TcpSiteurl> findFirst = findCpSiteUrlBySiteCode.stream().filter(t -> t.getId().equals(queryChannelPromotionSet.getSiteUrlId())).findFirst();
				if (findFirst.isPresent()) {
					TcpSiteurl target = findFirst.get();
					channelPromotionUrl = target.getSiteUrl().startsWith("www.") ? "https://" + target.getSiteUrl(): "https://www." + target.getSiteUrl();
					channelPromotionUrl = channelPromotionUrl+ "/?channelCode=";
				}
			}
		}
		List<AgyChannel> list = agyChannelMapper.list(agyChannelDto);
		// 如果存在推广域名，拼接上渠道号
		if (StringUtil.isNotEmpty(channelPromotionUrl)) {
			for (AgyChannel agyChannel : list) {
				agyChannel.setChannelPromotionUrl(channelPromotionUrl + agyChannel.getMasterNum());
			}
		}
		
		return BeanUtil.toPagedResult(list);
	}

	/**
	 * 用户端用查询
	 * 
	 * @param masterNum
	 * @return
	 */
	public AgyChannelForApiDto viceNum(String masterNum) {
		return agyChannelMapper.selectByMasterNum(masterNum);
	}

	/**
	 * 根据号码（主号或者副号 ）返回当前渠道对象
	 * 
	 * @param num
	 * @return
	 */
	public AgyChannel getByNum(String num) {
		return agyChannelMapper.getByNum(num);
	}

	/**
	 * 保存渠道，判断主号副号是否重复
	 * 
	 * @param agyChannel
	 * @return
	 */
	public R saveChannel(AgyChannel agyChannel) {
		if (agyChannel.getMasterNum().equals(agyChannel.getViceNum())) {
			throw new R200Exception("主号副号不可相同！");
		}
		AgyChannel master = getByNum(agyChannel.getMasterNum());
		AgyChannel vice = getByNum(agyChannel.getViceNum());
		if (master != null || vice != null) {
			throw new R200Exception("主号或副号已存在！");
		}

		return R.ok(save(agyChannel));
	}

	public R updateChannel(AgyChannel agyChannel) {
		if (agyChannel.getId() == null) {
			throw new R200Exception("数据id不可为空！");
		}
		AgyChannel selectByPrimaryKey = agyChannelMapper.selectByPrimaryKey(agyChannel.getId());
		if (selectByPrimaryKey == null) {
			throw new R200Exception("渠道id不存在");
		}
		if (agyChannel.getMasterNum().equals(agyChannel.getViceNum())) {
			throw new R200Exception("主号副号不可相同！");
		}

		AgyChannel master = agyChannelMapper.getByNumAndId(agyChannel.getId(), agyChannel.getMasterNum());
		AgyChannel vice = agyChannelMapper.getByNumAndId(agyChannel.getId(), agyChannel.getViceNum());
		if (master != null || vice != null) {
			throw new R200Exception("主号或副号已存在！");
		}
		if (!agyChannel.getIsOpen()) {
			agyChannel.setDeductRate(new BigDecimal("100"));
		}
		return R.ok(update(agyChannel));
	}
}
