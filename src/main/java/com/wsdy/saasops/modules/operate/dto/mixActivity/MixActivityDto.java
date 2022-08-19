package com.wsdy.saasops.modules.operate.dto.mixActivity;

import java.util.HashMap;

import com.wsdy.saasops.modules.operate.dto.ActRescueRuleDto;
import com.wsdy.saasops.modules.operate.dto.ActRuleBaseDto;
import com.wsdy.saasops.modules.operate.dto.BettingGiftDto;
import com.wsdy.saasops.modules.operate.dto.JDepositSentDto;
import com.wsdy.saasops.modules.operate.dto.JOtherDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "MixActivityDto", description = "混合活动规则dto")
public class MixActivityDto {

    @ApiModelProperty(value = "统计开始时间")
    private String startTime;
    @ApiModelProperty(value = "活动顺序map key: 活动code 存就送AQ0000003 投就送AQ0000012 救援金AQ0000004 其他AQ0000015  value:排序，从1开始从小到大 ")
    private HashMap<String,Integer> activityLinkedList;
    @ApiModelProperty(value = "领取顺序 0 按顺序 1条件满足领取")
    private Integer applyType;

    @ApiModelProperty(value = "存就送规则")
    private JDepositSentDto jDepositSentDto;
    @ApiModelProperty(value = "投就送规则")
    private BettingGiftDto bettingGiftDto;
    @ApiModelProperty(value = "救援金规则")
    private ActRuleBaseDto<ActRescueRuleDto> actRescueRuleDto;
    @ApiModelProperty(value = "其他规则")
    private JOtherDto jOtherDto;

//    public static void main(String[] args) {
//        MixActivityDto rule = new MixActivityDto();
//        rule.setStartTime("2021-06-05 16:00:00");
//        HashMap<String,Integer> activityLinkedList = new HashMap<>();
//        activityLinkedList.put("CS",2);
//        activityLinkedList.put("TS",4);
//        activityLinkedList.put("RS",1);
//        activityLinkedList.put("JY",3);
//
//        JsonUtil jsonUtil = new JsonUtil();
//        Map<String,Integer> sortMap = CommonUtil.mapSortByValue(activityLinkedList,1);
//        System.out.println(jsonUtil.toJson(sortMap));
//        for (Map.Entry<String, Integer> entry : sortMap.entrySet()) {
//            System.out.println("Item : " + entry.getKey() + " Count : " + entry.getValue());
//        }
//
//        rule.setActivityLinkedList(activityLinkedList);
//        rule.setApplyType(0);
//
//        // 存就送规则
//        JDepositSentDto jDepositSentDto = new JDepositSentDto();
//        // 投就送规则
//        BettingGiftDto bettingGiftDto = new BettingGiftDto();
//        // 救援金规则
//        ActRuleBaseDto<ActRescueRuleDto> actRescueRuleDto = new ActRuleBaseDto<ActRescueRuleDto>();
//        // 其他规则
//        JOtherDto jOtherDto = new JOtherDto();
//
//
//        String json = jsonUtil.toJson(rule);
//        System.out.println(json);
//    }
}
