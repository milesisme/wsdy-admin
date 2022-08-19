package com.wsdy.saasops.modules.member.entity;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@Table(name = "mbr_token")
public class MbrToken implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "会员ID")
    private Integer accountId;
    
    @ApiModelProperty(value = "会员账号")
    private String loginName;
        
    @ApiModelProperty(value="会员token")
    private String token;
}