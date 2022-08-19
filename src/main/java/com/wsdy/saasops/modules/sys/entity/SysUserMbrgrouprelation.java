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
@ApiModel(value = "SysUserMbrgrouprelation", description = "")
@Table(name = "sys_user_mbrGroupRelation")
public class SysUserMbrgrouprelation implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Long id;

    //会员组Id
    @ApiModelProperty(value = "会员组Id")
    private Integer mbrGroupId;
    //用户权限Id
    @ApiModelProperty(value = "用户权限Id")
    private Long userId;

    public SysUserMbrgrouprelation() {}

    public SysUserMbrgrouprelation(Integer mbrGroupId, Long userId) {
        this.mbrGroupId = mbrGroupId;
        this.userId = userId;
    }
}