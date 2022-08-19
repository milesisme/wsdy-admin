package com.wsdy.saasops.modules.member.service;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.modules.agent.service.AgyChannelLogService;
import com.wsdy.saasops.modules.log.service.LogMbrloginService;
import com.wsdy.saasops.modules.member.dao.MbrAccountDeviceMapper;
import com.wsdy.saasops.modules.member.dao.MbrGroupMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccountDevice;
import com.wsdy.saasops.modules.member.entity.MbrGroup;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@Transactional
public class MbrAccountDeviceService {
    @Autowired
    private MbrAccountDeviceMapper mbrAccountDeviceMapper;
	@Autowired
	private LogMbrloginService logMbrloginService;
	@Autowired
	private MbrAccountLogService mbrAccountLogService;
	@Autowired
	private MbrGroupMapper mbrGroupMapper;
    @Autowired
	private AgyChannelLogService agyChannelLogService;

    public void deviceBind(MbrAccountDevice deviceDto){
		// 查询该会员是否已绑定该设备
		MbrAccountDevice qryDto = new MbrAccountDevice();
		qryDto.setAccountId(deviceDto.getAccountId());
		qryDto.setLoginName(deviceDto.getLoginName());
		qryDto.setDeviceUuid(deviceDto.getDeviceUuid());

		List<MbrAccountDevice> qryList = mbrAccountDeviceMapper.select(qryDto);
		log.info("deviceBind------AccountId:{}, DeviceUuid：{}", deviceDto.getAccountId(), deviceDto.getDeviceUuid());
		// 若该会员未绑定该设备，则新增会员设备表
		if(Objects.isNull(qryList) || qryList.size() == 0){
			qryDto.setDeviceType(deviceDto.getDeviceType());
			qryDto.setBrowserType(deviceDto.getBrowserType());
			qryDto.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
			qryDto.setIsBlackOpr(Integer.valueOf(Constants.EVNumber.zero).byteValue());		// 是否拉黑操作过 0 否 1是
			int insert = mbrAccountDeviceMapper.insert(qryDto);
			if(insert == 1) {
				log.info("deviceBind------保存成功");
			}
		}else{	// 有绑定，由于历史送的deviceType定义只有PC和H5，此处增加一个逻辑更新deviceType
			for(MbrAccountDevice dto : qryList){
				dto.setDeviceType(deviceDto.getDeviceType());	// 设置设备类型
				mbrAccountDeviceMapper.updateByPrimaryKey(dto);
			}
		}

		// 更新该会员最近一次的登录记录的设备uuid，前端登录后及注册成功后会调用该接口
		logMbrloginService.updateLastLoginDeviceUuid(qryDto);

		// 渠道统计
		agyChannelLogService.logChannelIsVrtual(deviceDto.getAccountId(), deviceDto.getDeviceUuid(), deviceDto.getChannelNum());
		
	}

	// 查询同设备大于4的会员数据
	public List<MbrAccountDevice> getSameDeviceMbrList(int num){
		List<MbrAccountDevice> list = mbrAccountDeviceMapper.getSameDeviceMbrList(num);
		return list;
	}

	public void batchUpdateMbrGroup(List<Integer> list,Integer groupId){
		MbrGroup afterGroup =  mbrGroupMapper.selectByPrimaryKey(groupId);
    	for (int i =0;i<list.size();i++){
    		//在会员资料变更日志中增加操作记录
			mbrAccountLogService.accountBackListGroup(list.get(i),afterGroup.getGroupName());
		}
		mbrAccountDeviceMapper.batchUpdateMbrGroup(list,groupId);
	}
	public void batchUpdateMbrDevice(List<Integer> list){
		mbrAccountDeviceMapper.batchUpdateMbrDevice(list);
	}

	//用户在这设备上登录的次数
	public Integer getCountByLoginNameAndDevice(String loginName,String device){
		return  mbrAccountDeviceMapper.getCountByLoginNameAndDevice( loginName, device);
	}

}
