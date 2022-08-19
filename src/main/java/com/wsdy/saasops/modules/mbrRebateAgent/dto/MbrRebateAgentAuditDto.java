package com.wsdy.saasops.modules.mbrRebateAgent.dto;

import com.wsdy.saasops.modules.mbrRebateAgent.entity.MbrRebateAgentBonus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel(value = "全民代理审核dto")
public class MbrRebateAgentAuditDto {
    @ApiModelProperty(value = "mbr_rebate_agent_bonus 表id")
    private Integer id;

    @ApiModelProperty(value = "审核结果 0 拒绝 1 通过 ")
    private Integer status;
    @ApiModelProperty(value = "创建时间 yyyy-mm")
    private String createTime;

    @ApiModelProperty(value = "用户ID")
    private Long userId;
    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "审核时间")
    private String auditTime;
    @ApiModelProperty(value ="批量更新")
    private List<MbrRebateAgentBonus> groups;

    @ApiModelProperty(value = "备注")
    private String memo;
}
