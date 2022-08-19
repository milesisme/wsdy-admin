package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@ApiModel(value = "好友转账查询", description = "")
public class MbrFriendTransDetailDto implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "会员登陆名称")
    private String loginName;
    @ApiModelProperty(value = "转账会员名称")
    private String transLoginName;
    @ApiModelProperty(value = "收款账户名称")
    private String receiptLoginName;
    @ApiModelProperty(value = "类型 1表示转入,2表示转出")
    private Integer type;
    @ApiModelProperty(value = "转账开始时间 yyyy-MM-dd HH:mm:ss")
    private String startTime;
    @ApiModelProperty(value = "转账结束时间 yyyy-MM-dd HH:mm:ss")
    private String endTime;
}
