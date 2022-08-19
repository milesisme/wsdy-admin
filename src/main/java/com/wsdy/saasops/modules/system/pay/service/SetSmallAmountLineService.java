package com.wsdy.saasops.modules.system.pay.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.system.pay.dao.SetSmallAmountLineMapper;
import com.wsdy.saasops.modules.system.pay.entity.SetSmallAmountLine;

/**
 * <p>
 * 小额客服线 服务实现类
 * </p>
 *
 * @author ${author}
 */
@Service
public class SetSmallAmountLineService extends BaseService<SetSmallAmountLineMapper, SetSmallAmountLine> {
	
    @Autowired
    private RedisService redisService;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private SetSmallAmountLineMapper setSmallAmountLineMapper;

	public List<SetSmallAmountLine> list() {
		long todatCount = redisService.getSetSize(RedisConstants.LINE_SERVICE_COUNT + CommonUtil.getSiteCode());
		List<SetSmallAmountLine> selectAll = setSmallAmountLineMapper.selectAll();
		selectAll.forEach(t -> t.setTodatCount(todatCount));
		return selectAll;
	}
	
	public Boolean checkUser(Integer userId) {
		MbrAccount selectByPrimaryKey = accountMapper.selectByPrimaryKey(userId);
		Integer groupId = selectByPrimaryKey.getGroupId();
		SetSmallAmountLine selectByGroupId = setSmallAmountLineMapper.selectByGroupId(groupId);
		return selectByGroupId != null;
	}

	/**
	 * 	小额客服线当日点击次数统计
	 * @param userId
	 */
	public void lineServiceCount(Integer userId) {
		String key = RedisConstants.LINE_SERVICE_COUNT + CommonUtil.getSiteCode();
		
		// 获取当前剩余秒数
		Date currentDate = new Date();
		LocalDateTime midnight = LocalDateTime.ofInstant(currentDate.toInstant(),
                ZoneId.systemDefault()).plusDays(1).withHour(0).withMinute(0)
                .withSecond(0).withNano(0);
        LocalDateTime currentDateTime = LocalDateTime.ofInstant(currentDate.toInstant(),
                ZoneId.systemDefault());
        long seconds = ChronoUnit.SECONDS.between(currentDateTime, midnight);
		redisService.addSet(key, seconds, userId);
	}

}
