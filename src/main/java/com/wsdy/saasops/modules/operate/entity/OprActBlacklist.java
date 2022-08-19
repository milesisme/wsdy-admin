package com.wsdy.saasops.modules.operate.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@ApiModel(value = "OprActBlacklist", description = "活动黑名单")
@Table(name = "opr_act_blacklist")
public class OprActBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "会员id")
    private Integer accountId;

    @ApiModelProperty(value = "活动标识")
    private String tmplCode;

    @ApiModelProperty(value = "会员名")
    private String loginName;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "是否代理0否,1是")
    private Integer isAgent;

    @ApiModelProperty(value = "是否全部活动0否,1是")
    private Integer allCode;

}
