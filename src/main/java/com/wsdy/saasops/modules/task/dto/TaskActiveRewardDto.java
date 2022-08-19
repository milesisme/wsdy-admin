package com.wsdy.saasops.modules.task.dto;

import com.wsdy.saasops.modules.task.entity.TaskBonus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ApiModel(value="活跃奖励规则及当下校验结果数据")
public class TaskActiveRewardDto {

  /*@ApiModelProperty(value = "领取要求 当日存款不小于")
  private BigDecimal depositAmount;

  @ApiModelProperty(value = "当日投注不小于")
  private BigDecimal validBet;*/

  // 规则部分
  @ApiModelProperty(value = "已填写真实姓名 true是 false 否")
  private Boolean isName;
  @ApiModelProperty(value = "已绑定银行卡 true是 false 否")
  private Boolean isBank;
  @ApiModelProperty(value = "已验证手机 true是 false 否")
  private Boolean isMobile;
  @ApiModelProperty(value = "已验证邮箱 true是 false 否")
  private Boolean isMail;
  @ApiModelProperty(value = "流水倍数")
  private Integer multipleWater;
  @ApiModelProperty(value = "是否可以循环领取 1是 0否")
  private Integer cycle;
  @ApiModelProperty(value = " 奖励规则")
  private List<TaskActiveFilesDto> filesDtos;
  @ApiModelProperty(value = "规则备注说明")
  private String memo;

  // 当下数据：最近一次的签到信息  和 校验结果
  @ApiModelProperty(value = "最后一次领取时间，天")
  private String day;
  @ApiModelProperty(value = "最后一次领取是第几天")
  private Integer num;
  @ApiModelProperty(value = "当天存款")
  private BigDecimal dayDepositAmount;
  @ApiModelProperty(value = "当天投注")
  private BigDecimal dayValidbet;
  @ApiModelProperty(value = "资料信息校验结果")
  private TaskActiveInfoDto accountInfoDto;
  @ApiModelProperty(value = "签到历史红利")
  private List<TaskBonus> taskBonuses;
}