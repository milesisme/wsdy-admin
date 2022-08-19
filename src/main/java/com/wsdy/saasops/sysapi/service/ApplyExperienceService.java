package com.wsdy.saasops.sysapi.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.fund.service.FundDepositService;
import com.wsdy.saasops.modules.member.dao.MbrExperienceMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrBankcard;
import com.wsdy.saasops.modules.member.entity.MbrExperience;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.member.service.MbrBankcardService;
import com.wsdy.saasops.sysapi.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ApplyExperienceService {

    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private MbrBankcardService mbrBankcardService;
    @Autowired
    private FundDepositService  fundDepositService;
    @Autowired
    private MbrExperienceMapper mbrExperienceMapper;

    public ResponseDto applyExperience(ApplyExperienceRequestDto applyExperienceRequestDto){
        MbrAccount mbrAccount =  mbrMapper.findAccountByLoginName(applyExperienceRequestDto.getUserName());
        ResponseDto responseDto = new ResponseDto();
        ApplyExperienceResponseDto applyExperienceResponseDto = new ApplyExperienceResponseDto();
        applyExperienceResponseDto.setUserName(applyExperienceRequestDto.getUserName());
        if(mbrAccount == null){
            applyExperienceResponseDto.setUserNameStatus(Constants.EVNumber.one); // 父级不存在
            Map<String, Object>  data = JSON.parseObject(JSON.toJSONString(applyExperienceResponseDto), Map.class);
            responseDto.setData(data);
            return  responseDto;
        }

        FundDeposit fundDeposit = new FundDeposit();
        fundDeposit.setAccountId(mbrAccount.getId());
        fundDeposit.setStatus(Constants.EVNumber.one);
        List<FundDeposit> fundDeposits = fundDepositService.selectList(fundDeposit);
        if(fundDeposits == null || fundDeposits.size() <= 0){
            applyExperienceResponseDto.setUserNameStatus(Constants.EVNumber.two); // 没充值
            Map<String, Object>  data = JSON.parseObject(JSON.toJSONString(applyExperienceResponseDto), Map.class);
            responseDto.setData(data);
            return  responseDto;
        }

        String startDay=  mbrMapper.getFirstBetDay(mbrAccount.getLoginName());
        if(startDay == null || DateUtil.daysBetween(startDay, DateUtil.getCurrentDate(DateUtil.FORMAT_10_DATE)) < 5){
            applyExperienceResponseDto.setUserNameStatus(Constants.EVNumber.three); // 下注没有超过5天
            Map<String, Object>  data = JSON.parseObject(JSON.toJSONString(applyExperienceResponseDto), Map.class);
            responseDto.setData(data);
            return  responseDto;
        }

        if(applyExperienceRequestDto.getSubUserNames()==null || applyExperienceRequestDto.getSubUserNames().size() == 0){
            applyExperienceResponseDto.setUserNameStatus(Constants.EVNumber.zero);
            Map<String, Object>  data = JSON.parseObject(JSON.toJSONString(applyExperienceResponseDto), Map.class);
            responseDto.setData(data);
            return  responseDto;
        }

        List<RelationDto> relationDtoList =  mbrMapper.checkRelation(applyExperienceRequestDto.getUserName(), applyExperienceRequestDto.getSubUserNames());
        Map<String, ApplySubUserNameDto>  applySubUserNameDtoMap = new HashMap<>();


        for (RelationDto  relationDto :relationDtoList) {
            ApplySubUserNameDto applySubUserNameDto = new ApplySubUserNameDto();
            applySubUserNameDto.setSubUserName(relationDto.getSubUserName());
            MbrAccount subMbrAccount =  mbrMapper.findAccountByLoginName(relationDto.getSubUserName());
            List<MbrBankcard> mbrBankcards =  mbrBankcardService.listCondBankCard(subMbrAccount.getId());
            MbrExperience  mbrExperience = new MbrExperience();
            mbrExperience.setUserName(relationDto.getUserName());
            mbrExperience.setSubUserName(relationDto.getSubUserName());
            List<MbrExperience> mbrExperiences =  mbrExperienceMapper.select(mbrExperience);

            String lastIp =  mbrMapper.getLoginLastIp(subMbrAccount.getId());
            List<MbrExperience> mbrExperienceIPs = new ArrayList<>();

            if(lastIp != null && lastIp.length() > 0){
                MbrExperience  mbrExperienceIP = new MbrExperience();
                mbrExperienceIP.setIp(lastIp);
                mbrExperienceIPs = mbrExperienceMapper.select(mbrExperienceIP);
            }

            // 检查是否完成真实名称
            if(subMbrAccount.getRealName() == null || subMbrAccount.getRealName().length() == 0){
                applySubUserNameDto.setStatus(Constants.EVNumber.one); // 没有绑定名称
                // 是否验证手机
            }else if(subMbrAccount.getIsVerifyMoblie() == null || subMbrAccount.getIsVerifyMoblie() != 1){
                applySubUserNameDto.setStatus(Constants.EVNumber.two); // 没有绑定手机
                // 是否是否绑定银行卡
            }else if(mbrBankcards == null || mbrBankcards.size() == 0){
                applySubUserNameDto.setStatus(Constants.EVNumber.three); // 没有银行卡
            }else if(mbrExperiences.size() > 0){
                applySubUserNameDto.setStatus(Constants.EVNumber.four); // 已经体验过
            }else if(  mbrExperienceIPs.size() > 0) {
                applySubUserNameDto.setStatus(Constants.EVNumber.five); // 已经体验过IP
            }else {
                applySubUserNameDto.setStatus(Constants.EVNumber.zero);// 体验
            }
            applySubUserNameDtoMap.put(applySubUserNameDto.getSubUserName(), applySubUserNameDto);
        }

        // 补全不存在上下级关系
        for ( String subUserName : applyExperienceRequestDto.getSubUserNames()){
            ApplySubUserNameDto applySubUserNameDto = applySubUserNameDtoMap.get(subUserName);
            if(applySubUserNameDto == null){
                applySubUserNameDto = new ApplySubUserNameDto();
                applySubUserNameDto.setSubUserName(subUserName);
                applySubUserNameDto.setStatus(Constants.EVNumber.six); // 不存在上下级关系
                applySubUserNameDtoMap.put(subUserName,applySubUserNameDto);
            }
        }
        List<ApplySubUserNameDto > applySubUserNames  =  new ArrayList<>();
        applySubUserNames.addAll(applySubUserNameDtoMap.values());
        applyExperienceResponseDto.setApplySubUserNames(applySubUserNames);
        applyExperienceResponseDto.setUserNameStatus(Constants.EVNumber.zero);


        Map<String, Object>  data = JSON.parseObject(JSON.toJSONString(applyExperienceResponseDto), Map.class);
        responseDto.setData(data);
        return responseDto;
    }

    public  ResponseDto findSubUser(FindSubUserRequestDto findSubUserRequestDto){
        MbrAccount mbrAccount =  mbrMapper.findAccountByLoginName(findSubUserRequestDto.getUserName());
        ResponseDto responseDto = new ResponseDto();
        if(mbrAccount == null){
            responseDto.setCode(String.valueOf(Constants.EVNumber.one));
            responseDto.setMsg("父级不存在");
            return responseDto;
        }
        PageHelper.startPage(findSubUserRequestDto.getPageNo(), findSubUserRequestDto.getPageSize());
        List<SubUserDto> subUserDtos =  mbrMapper.findSubUserByParentId(mbrAccount.getId(),findSubUserRequestDto.getStartTime(), findSubUserRequestDto.getEndTime(), findSubUserRequestDto.getSubUserName() );
        responseDto.setCode(String.valueOf(Constants.EVNumber.zero));
        responseDto.setMsg("成功");
        PageUtils pageUtils = BeanUtil.toPagedResult(subUserDtos);
        Map<String, Object> data = new HashMap<>();
        data.put("subUserDtos",pageUtils.getList() );
        data.put("currPage", pageUtils.getCurrPage());
        data.put("totalPage", pageUtils.getTotalPage());
        data.put("pageSize", pageUtils.getPageSize());
        data.put("totalCount", pageUtils.getTotalCount());
        responseDto.setData(data);
        return responseDto;
    }

    public  ResponseDto startExperience(StartExperienceRequestDto startExperienceRequestDto){

        ResponseDto responseDto = new ResponseDto();
        List<String> subUserNames =  startExperienceRequestDto.getSubUserNames();
        List<MbrExperience> mbrExperienceList = new ArrayList<>();

        StartExperienceResponseDto  startExperienceResponseDto = new StartExperienceResponseDto();
        startExperienceResponseDto.setUserName(startExperienceRequestDto.getUserName());

        List<ApplySubUserNameDto> applySubUserNames = new ArrayList<>();
        for(String subUserName: subUserNames){
            ApplySubUserNameDto  applySubUserNameDto = new ApplySubUserNameDto();
            applySubUserNameDto.setSubUserName(subUserName);
            MbrAccount subMbrAccount =  mbrMapper.findAccountByLoginName(subUserName);

            MbrExperience  mbrExperience = new MbrExperience();
            mbrExperience.setUserName(startExperienceRequestDto.getUserName());
            mbrExperience.setSubUserName(subUserName);
            List<MbrExperience> mbrExperiences =  mbrExperienceMapper.select(mbrExperience);
            if(subMbrAccount == null){
                applySubUserNameDto.setStatus(Constants.EVNumber.one); //账号不存在
            }
            else if(mbrExperiences.size() > 0){
                applySubUserNameDto.setStatus(Constants.EVNumber.two); //账号已体验
            }else{
                applySubUserNameDto.setStatus(Constants.EVNumber.zero); // 体验
                String lastIp =  mbrMapper.getLoginLastIp(subMbrAccount.getId());
                MbrExperience m = new MbrExperience();
                m.setApplyTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
                m.setUserName(startExperienceRequestDto.getUserName());
                m.setSubUserName(subUserName);
                m.setIp(lastIp);
                mbrExperienceList.add(m);
            }
            applySubUserNames.add(applySubUserNameDto);
        }
        startExperienceResponseDto.setApplySubUserNames(applySubUserNames);
        if(mbrExperienceList.size() > 0){
            mbrExperienceMapper.insertList(mbrExperienceList);
        }
        Map<String, Object>  data = JSON.parseObject(JSON.toJSONString(startExperienceResponseDto), Map.class);
        responseDto.setData(data);
    return  responseDto;
    }
}
