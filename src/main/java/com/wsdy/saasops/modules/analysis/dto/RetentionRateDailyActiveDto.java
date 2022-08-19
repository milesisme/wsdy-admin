package com.wsdy.saasops.modules.analysis.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@ApiModel("会员首充日起30日内，每日活跃查询条件对象")
@Data
public class RetentionRateDailyActiveDto {


    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    private String userName;

    /**
     * 代理名
     */
    @ApiModelProperty(value = "代理名")
    private String agentName;

    /**
     * 首充开始时间
     */
    @ApiModelProperty(value = "首充开始时间")
    private String startTime;

    /**
     * 首充结束时间
     */
    @ApiModelProperty(value = "首充结束时间")
    private String endTime;

    /**
     * 留存规则:
     * 留存数据统计规则：1，存款规则；2，投注规则；不传，全部；
     * 1.存款：当天存款99以上=1个单位活跃；
     * 2.投注：当天投注99以上=1个单位活跃；
     * 3.全部：当天存款或者投注99以上=1个单位活跃
     */
    @ApiModelProperty(value = "留存数据统计规则：1，存款规则；2，投注规则；不传，全部；")
    private Integer rule;

    /**
     *
     */
    @ApiModelProperty(value = "页号")
    private Integer pageNo;

    /**
     *
     */
    @ApiModelProperty(value = "每页大小")
    private Integer pageSize;

}
