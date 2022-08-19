package com.wsdy.saasops.modules.analysis.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("会员首充日起30日内，每日活跃情况")
@Data
public class RetentionRateDailyActiveReportVo {
    @ApiModelProperty(value = "用户名")
    private String userName;
    @ApiModelProperty(value = "首充日期")
    private String firstChargeDate;
    @ApiModelProperty(value = "自首充日起，未来30天的活跃状态")
    private List<ActiveStateIn30Days> stateList;

    @ApiModel("充值日期-状态")
    @Data
    public static class ActiveStateIn30Days {
        @ApiModelProperty(value = "距离首充日第几天，如：“day9”，则表示首充日后第九天（首充日+9天）")
        private String day;
        @ApiModelProperty(value = "状态：0，未活跃；1，活跃；null，尚未到来的日期")
        private Integer state;
    }
}