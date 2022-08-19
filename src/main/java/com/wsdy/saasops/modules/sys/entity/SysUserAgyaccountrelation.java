package com.wsdy.saasops.modules.sys.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;


@Setter
@Getter
@ApiModel(value = "SysUserAgyaccountrelation", description = "")
@Table(name = "sys_user_agyAccountRelation")
public class SysUserAgyaccountrelation implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Long id;

    //用户Id
    @ApiModelProperty(value = "用户Id")
    private Long userId;
    //代理账户Id
    @ApiModelProperty(value = "代理账户Id")
    private Integer agyAccountId;

    //代理类型
    @ApiModelProperty(value = "代理类型 0 总代 1 子代")
    private Integer agyAccountType;
    @ApiModelProperty(value = "是否可选")
    private Boolean disabled;

    public SysUserAgyaccountrelation(Long userId, Integer agyAccountId, Integer agyAccountType) {
        this.userId = userId;
        this.agyAccountId = agyAccountId;
        this.agyAccountType = agyAccountType;
    }
    public SysUserAgyaccountrelation() {}
}