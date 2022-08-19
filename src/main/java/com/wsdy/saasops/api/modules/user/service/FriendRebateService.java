package com.wsdy.saasops.api.modules.user.service;


import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.api.modules.user.dto.*;
import com.wsdy.saasops.api.modules.user.mapper.ApiPromotionMapper;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.modules.mbrRebateAgent.service.MbrRebateAgentService;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.OprActRule;
import com.wsdy.saasops.modules.operate.entity.TOpActtmpl;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_10_DATE;
import static com.wsdy.saasops.common.utils.DateUtil.getPastDate;

@Slf4j
@Service
@Transactional
public class FriendRebateService {

    @Autowired
    private OperateActivityMapper operateActivityMapper;

    @Autowired
    private ApiPromotionMapper apiPromotionMapper;

    @Autowired
    private MbrMapper mbrMapper;

    @Autowired
    private MbrAccountMapper mbrAccountMapper;

    @Autowired
    private MbrRebateAgentService mbrRebateAgentService;

    public OprActRule findActRuleByCode(String tmplCode){
        return operateActivityMapper.findActRuleByCode(tmplCode);
    }


    public PageUtils getApiFriendRebateDtoList(String firstChargeStartTime, String firstChargeEndTime, String startTime, String endTime, Integer accountId, String subLoginName, Integer showAll, Integer pageNo, Integer pageSize){

        PageHelper.startPage(pageNo, pageSize);
        List<FriendRebateDto>  apiFriendRebateDtoList =  apiPromotionMapper.getApiFriendRebateDtoList( firstChargeStartTime,  firstChargeEndTime,  startTime,  endTime,  accountId,  subLoginName, showAll);
        return BeanUtil.toPagedResult(apiFriendRebateDtoList);
    }

    public FriendRebateDto getApiFriendRebateDtoDetails(String startTime, String endTime, String loginName, String subLoginName){
        FriendRebateDto apiFriendRebateDto =  apiPromotionMapper.getApiFriendRebateDtoDetails( startTime,  endTime,  loginName,  subLoginName);
        return apiFriendRebateDto;
    }

    public PageUtils getFriendRebateRewardReportForDay(String startTime, String endTime, Integer accountId, Integer pageNo, Integer pageSize){
        PageHelper.startPage(pageNo, pageSize);
        if(StringUtil.isNotEmpty(startTime)){
            startTime = startTime + " 00:00:00";
        }
        if(StringUtil.isNotEmpty(endTime)){
            endTime  = endTime + " 23:59:59";
        }
        List<FriendRebateRewardDto> apiFriendRebateRewardDtoList = apiPromotionMapper.getFriendRebateRewardReportForDay( startTime,  endTime,  accountId);
        apiFriendRebateRewardDtoList.removeAll(Collections.singleton(null));
        return BeanUtil.toPagedResult(apiFriendRebateRewardDtoList);
    }

    public PageUtils getFriendRebateRewardReportForMonth(String startTime, String endTime, Integer accountId, Integer pageNo, Integer pageSize){
        PageHelper.startPage(pageNo, pageSize);
        if(StringUtil.isNotEmpty(startTime)){
            startTime = startTime + " 00:00:00";
        }
        if(StringUtil.isNotEmpty(endTime)){
            endTime  = endTime + " 23:59:59";
        }
        List<FriendRebateRewardDto> apiFriendRebateRewardDtoList = apiPromotionMapper.getFriendRebateRewardReportForMonth( startTime,  endTime,  accountId);
        apiFriendRebateRewardDtoList.removeAll(Collections.singleton(null));
        return BeanUtil.toPagedResult(apiFriendRebateRewardDtoList);
    }

    public FriendRebateAccountDto getFriendRebateAccountInfo(String  loginName){
        return apiPromotionMapper.getFriendRebateAccountInfo(loginName);
    }


    public FriendRebateSummaryDto getFriendRebateSummary(Integer accountId){
        List<FriendRebateSumDto> friendRebateSummaryDtoList =  apiPromotionMapper.getFriendRebateSummary(accountId);
        FriendRebateSummaryDto friendRebateSummaryDto  = toFriendRebateSummaryDto(friendRebateSummaryDtoList);
        Integer num = mbrMapper.findPromotionCountByAccountId(accountId);
        friendRebateSummaryDto.setNum(num);
        return friendRebateSummaryDto;
    }


    public FriendRebatePersonalRewardSummaryDto getFriendRebatePersonalRewardSummary(Integer accountId, String startTime, String endTime){
        return apiPromotionMapper.getFriendRebatePersonalRewardSummary(accountId, startTime, endTime);
    }

    public  FriendRebateFriendRewardSummaryDto getFriendRebateFriendsRewardSummary(String firstChargeStartTime, String firstChargeEndTime, Integer accountId, String startTime, String endTime, String subLoginName){
        return apiPromotionMapper.getFriendRebateFriendsRewardSummary(firstChargeStartTime, firstChargeEndTime, accountId, startTime, endTime, subLoginName);
    }

    public FriendRebateInfoDto getFriendsRebateActInfo(Integer accountId) {
        FriendRebateInfoDto friendRebateInfoDto = new FriendRebateInfoDto();
        friendRebateInfoDto.setIsShowFriendRebate(Boolean.FALSE);
        OprActRule  oprActRule =  findActRuleByCode( TOpActtmpl.mbrRebateCode);

        if(oprActRule!= null && oprActRule.getAvailable() == 1){
            friendRebateInfoDto.setIsShowFriendRebate(Boolean.TRUE);
        }

        if(accountId == null){
            friendRebateInfoDto.setCodeId("");
            return friendRebateInfoDto;
        }
        // 获得代理会员信息
        MbrAccount mbrAccount = mbrAccountMapper.selectByPrimaryKey(accountId);
        // 获取推荐人code

        if(mbrAccount == null){
            friendRebateInfoDto.setCodeId("");
            return friendRebateInfoDto;
        }
        if(StringUtil.isEmpty(mbrAccount.getDomainCode())){
            mbrAccount.setDomainCode(mbrRebateAgentService.getDomainCode());
            mbrAccountMapper.updateByPrimaryKeySelective(mbrAccount);
        }
        friendRebateInfoDto.setCodeId(mbrAccount.getDomainCode());
        return friendRebateInfoDto;
    }

    public  PageUtils rewardList(String startTime, String endTime, Integer accountId, Integer pageNo, Integer pageSize){
        PageHelper.startPage(pageNo, pageSize);
        List<FriendRebateRewardListDto>  friendRebateRewardListDtoList=  apiPromotionMapper.rewardList( startTime,  endTime,  accountId);
        return BeanUtil.toPagedResult(friendRebateRewardListDtoList) ;
    }


    private FriendRebateSummaryDto toFriendRebateSummaryDto(List<FriendRebateSumDto> friendRebateSummaryDtoList){
        FriendRebateSummaryDto friendRebateSummaryDto = new FriendRebateSummaryDto();
        for(FriendRebateSumDto friendRebateSumDto: friendRebateSummaryDtoList){
            if(friendRebateSumDto.getType() == 1){
                friendRebateSummaryDto.setSumActualReward(friendRebateSumDto.getActualReward());
                friendRebateSummaryDto.setSumFirstChargeReward(friendRebateSumDto.getFirstChargeReward());
                friendRebateSummaryDto.setSumValidBetReward(friendRebateSumDto.getValidBetReward());
            } else if(friendRebateSumDto.getType() == 2){
                friendRebateSummaryDto.setYdActualReward(friendRebateSumDto.getActualReward());
                friendRebateSummaryDto.setYdFirstChargeReward(friendRebateSumDto.getFirstChargeReward());
                friendRebateSummaryDto.setYdValidBetReward(friendRebateSumDto.getValidBetReward());
            }
        }
        return friendRebateSummaryDto;
    }

}
