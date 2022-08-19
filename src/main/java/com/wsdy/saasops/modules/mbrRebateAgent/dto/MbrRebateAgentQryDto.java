package com.wsdy.saasops.modules.mbrRebateAgent.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "全民代理查询dto")
public class MbrRebateAgentQryDto {
    // 查询条件
    @ApiModelProperty(value = "计算的深度")
    private  Integer rebateCastDepth;

    @ApiModelProperty(value = "子结点")
    private Integer childNodeId;
    @ApiModelProperty(value = "深度")
    private Integer depth;
    @ApiModelProperty(value = "最大深度")
    private Integer maxDepth;
    @ApiModelProperty(value = "代理会员级别id")
    private Integer agyLevelId;
    @ApiModelProperty(value = "全民代理标志 0非代理会员 1代理会员")
    private Integer agyflag;
    @ApiModelProperty(value = "会员账号")
    private String loginName;

    @ApiModelProperty(value = "父结点")
    private Integer parentId;
    @ApiModelProperty(value = "父结点代理会员级别id")
    private Integer agyLevelIdParent;
    @ApiModelProperty(value = "父结点全民代理标志 0非代理会员 1代理会员")
    private Integer agyflagParent;
    @ApiModelProperty(value = "父结点会员账号")
    private String loginNameParent;
    @ApiModelProperty(value = "派彩")
    private BigDecimal payout;
    @ApiModelProperty(value = "有效投注")
    private BigDecimal validbet;

    // 条件
    @ApiModelProperty(value = "开始时间")
    private String startTime;
    @ApiModelProperty(value = "结束时间")
    private String endTime;
    @ApiModelProperty(value = "自身数据 统计时间")
    private String createTime;
    @ApiModelProperty(value = "分页页数")
    private Integer pageNo;
    @ApiModelProperty(value = "分页大小")
    private Integer pageSize;
    @ApiModelProperty(value = "查询排序")
    private String orderBy;
    @ApiModelProperty(value = "会员组id")
    private Integer groupId;
    @ApiModelProperty(value = "审核状态 0 失败 1成功 2待审核 ")
    private Integer status;
    @ApiModelProperty(value = "下级数据 统计时间开始 yyyy-MM-dd")
    private String createTimeStart;
    @ApiModelProperty(value = "下级数据 统计时间结束 yyyy-MM-dd")
    private String createTimeEnd;
}
