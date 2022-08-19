package com.wsdy.saasops.modules.agent.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.agent.dao.AgyChannelLogMapper;
import com.wsdy.saasops.modules.agent.dto.AgyChannelForRegisterDto;
import com.wsdy.saasops.modules.agent.dto.AgyChannelLogDto;
import com.wsdy.saasops.modules.agent.entity.AgyChannel;
import com.wsdy.saasops.modules.agent.entity.AgyChannelLog;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ${author}
 * @since 2021-11-25
 */
@Slf4j
@Service
public class AgyChannelLogService extends BaseService<AgyChannelLogMapper, AgyChannelLog> {

	@Autowired
	private AgyChannelService agyChannelService;

	@Autowired
	private SysFileExportRecordService sysFileExportRecordService;

	@Autowired
	private AgyChannelLogMapper agyChannelLogMapper;

	@Autowired
	private JsonUtil jsonUtil;

	@Autowired
	private MbrAccountMapper accountMapper;

	/**
	 * 虚拟号
	 */
	private static final String[] vrtualNums = { "1700", "1701", "1702", "162", "1703", "1705", "1706", "165", "1704",
			"1707", "1708", "1709", "171", "167", "1349", "174", "140", "141", "144", "146", "148" };

	/**
	 * 渠道统计查询，多条件
	 * 
	 * @param agyChannelLogDto
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public PageUtils list(AgyChannelLogDto agyChannelLogDto, Integer pageNo, Integer pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		List<AgyChannelLogDto> list = agyChannelLogMapper.list(agyChannelLogDto);
		for (AgyChannelLogDto target : list) {
			target.setAllRegisterTotal(target.getRegisterTotal() + target.getViceRegisterTotal());
		}
		return BeanUtil.toPagedResult(list);
	}

	/**
	 * 
	 * 导出查询结果
	 * 
	 * @param agyChannelLogDto
	 * @param module
	 * @param mbrWinLoseExcelPath
	 * @return
	 */
	public SysFileExportRecord exportChannelLog(AgyChannelLogDto agyChannelLogDto, String module,
			String excelPath) {
		SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(getUser().getUserId(), module);
		if (null != record) {
			List<AgyChannelLogDto> list = agyChannelLogMapper.list(agyChannelLogDto);
			list.forEach(t -> {
				t.setDeductRate(t.getDeductRate() + "%");
				t.setAllRegisterTotal(t.getRegisterTotal() + t.getViceRegisterTotal());
			});
			List<Map<String, Object>> exportList = list.stream().map(e -> {
				setNum2Char(e);
				Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
				return entityMap;
			}).collect(Collectors.toList());
			log.info("exportChannelLog---getSiteCode: {}", CommonUtil.getSiteCode());
			sysFileExportRecordService.exportExcel(excelPath, exportList, getUser().getUserId(), module,
					CommonUtil.getSiteCode());
		}

		return record;
	}

	/**
	 * excel导出字段转换
	 * 
	 * @param agyChannelLogDto
	 */
	private void setNum2Char(AgyChannelLogDto agyChannelLogDto) {
		if (agyChannelLogDto.getIsOpen()) {
			agyChannelLogDto.setIsOpenStr("开启");
		} else {
			agyChannelLogDto.setIsOpenStr("关闭");
		}
	}

	/**
	 * 
	 * 用户端下载后第一次打开时调用，保存设备号以及对应的渠道
	 * 
	 * @param agyChannelForRegisterDto
	 * @return 
	 */
	public R logDeviceuuid(AgyChannelForRegisterDto agyChannelForRegisterDto) {
		// 根据号码（主号或者副号 ）返回当前渠道对象
		AgyChannel byNum = agyChannelService.getByNum(agyChannelForRegisterDto.getNum());
		if (byNum == null) {
			return R.ok("对应渠道号不存在！");
		}
		
		// 当前设备id是否已经注册过
		AgyChannelLog agyChannelLog = new AgyChannelLog();
		agyChannelLog.setDeviceuuid(agyChannelForRegisterDto.getDeviceuuid());
		agyChannelLog.setChannelid(byNum.getId());
		int selectCount = agyChannelLogMapper.selectCount(agyChannelLog);
		if (selectCount > 0) {
			return R.ok("设备号已经注册！");
		}
		
		Boolean isMasterNum = false;
		// 如果主号与当前的号码一致,并且当前渠道开关打开
		if (StringUtils.equalsIgnoreCase(byNum.getMasterNum(), agyChannelForRegisterDto.getNum()) && byNum.getIsOpen()) {
			isMasterNum = true;
		}
		agyChannelLog.setIsMasterNum(isMasterNum);
		agyChannelLog.setChannelid(byNum.getId());
		agyChannelLog.setIsVrtualModif(false);
		agyChannelLog.setIsVrtual(false);
		// 是否模拟器注册
		agyChannelLog.setIsEmulator(agyChannelForRegisterDto.isEmulator());
		agyChannelLog.setRegistertime(new Date());
		agyChannelLogMapper.insert(agyChannelLog);
		return R.ok();
	}

	/**
	 * 用户每次打开登录时调用，根据设备号查询是否在渠道记录里，如果在，判断手机号是否属于虚拟号，进行记录
	 * 
	 * @param mobile
	 * @param registerDevice
	 */
	public void logChannelIsVrtual(Integer accountId, String registerDevice, String channelNum) {
		if (StringUtils.isEmpty(channelNum)) {
			return;
		}
		AgyChannel byNum = agyChannelService.getByNum(channelNum);
		if (byNum == null) {
			return;
		}
		// 查询记录是否存在，存在并且虚拟号没有被验证过
		AgyChannelLog agyChannelLog = new AgyChannelLog();
		agyChannelLog.setDeviceuuid(registerDevice);
		agyChannelLog.setIsVrtualModif(false);
		agyChannelLog.setChannelid(byNum.getId());
		AgyChannelLog selectOne = agyChannelLogMapper.selectOne(agyChannelLog);
		if (selectOne == null) {
			return;
		}
		MbrAccount queryccount = new MbrAccount();
		queryccount.setId(accountId);
		// 根据用户id获取手机号
		MbrAccount mbrAccount = accountMapper.selectOne(queryccount);
		if (StringUtils.isEmpty(mbrAccount.getMobile())) {
			return;
		}
		Boolean isVrtual = false;
		for (String vrtual : vrtualNums) {
			if (mbrAccount.getMobile().startsWith(vrtual)) {
				isVrtual = true;
				break;
			}
		}
		selectOne.setIsVrtual(isVrtual);
		selectOne.setIsVrtualModif(true);
		selectOne.setAccountId(accountId);
		agyChannelLogMapper.updateByPrimaryKey(selectOne);
	}
}
