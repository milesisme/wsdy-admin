package com.wsdy.saasops.modules.mbrRebateAgent.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "全民代理-返利列表-历史day数据dto")
public class MbrRebateAgentRespHistoryListDto {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "mbr_rebate_agent_day表id")
    private Integer id;
    @ApiModelProperty(value = "会员ID")
    private Integer accountId;
    @ApiModelProperty(value = "会员账号")
    private String loginName;
    @ApiModelProperty(value = "代理会员级别id")
    private Integer agyLevelId;
    @ApiModelProperty(value = "计算时间 精确到时分秒")
    private String createTime;
    @ApiModelProperty(value = "统计时间 yyyy-MM-dd")
    private String createTimeEx;
    @ApiModelProperty(value = "昨日下级贡献的有效派彩=validpayoutfromchildmember+validpayoutfromchildmemberagent ")
    private BigDecimal validPayoutFromChild;
    @ApiModelProperty(value = "月累计下级贡献的有效派彩=validPayoutFromChildMemberMonth+validPayoutFromChildMemberAgentMonth ")
    private BigDecimal validPayoutFromChildMonth;

    // 其他
    @ApiModelProperty(value = "会员组名称")
    private String groupName;
}
