package com.wsdy.saasops.modules.agent.dto;

import com.wsdy.saasops.common.constants.Constants;
import io.netty.util.internal.StringUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SettingAgentDto {

    @ApiModelProperty(value = "代理注册审核 0 禁用 1启用")
    private Integer agentRgister = Constants.EVNumber.zero;

    @ApiModelProperty(value = "代理后台注册下线 0 禁用 1启用")
    private Integer agentSysRgister = Constants.EVNumber.zero;

    @ApiModelProperty(value = "代理后台新增会员 0 禁用 1启用")
    private Integer agentAddAccount = Constants.EVNumber.zero;

    @ApiModelProperty(value = "代理后台新增子代理 0 禁用 1启用")
    private Integer agentAddSub = Constants.EVNumber.zero;


    @ApiModelProperty(value = "佣金统计周期 5 每日日/期 1 7日/期 2 10日/期 3 15日/期 4 月初至月末最后一日/期")
    private Integer commissionPeriod = Constants.EVNumber.zero;

    @ApiModelProperty(value = "佣金生成时间 单期统计完成后 第N天")
    private Integer commissionDay = Constants.EVNumber.zero;

    @ApiModelProperty(value = "佣金生成时间 单期统计完成后 第N天 开始时间")
    private String commissionStartTime = StringUtil.EMPTY_STRING;

    @ApiModelProperty(value = "佣金生成时间 单期统计完成后 第N天 结束时间")
    private String commissionEndTime = StringUtil.EMPTY_STRING;

    @ApiModelProperty(value = "自动结算负盈利 0 禁用 1启用")
    private Integer commissionClose = Constants.EVNumber.zero;

    @ApiModelProperty(value = "净盈利费率")
    private BigDecimal netProfitRate = BigDecimal.ZERO;

    @ApiModelProperty(value = "0 否 1是")
    private Integer isBeginner;
}
