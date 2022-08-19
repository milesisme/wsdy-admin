package com.wsdy.saasops.modules.agent.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Table(name = "agy_account_memo")
@ApiModel(value = "AgentAccountMemo", description = "代理备注会员表")
public class AgentAccountMemo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "会员id")
    private Integer accountId;

    @ApiModelProperty(value = "会员账号")
    private String logiNname;

    @ApiModelProperty(value = "编号")
    private String numbering;

    @ApiModelProperty(value = "备注类型  1大客户 2套利客户 3潜力客户 4一般客户 5体育会员 6电竞会员")
    private Integer memoType;
}