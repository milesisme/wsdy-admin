package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@ApiModel(value = "MbrPromotion", description = "")
@Table(name = "mbr_promotion")
public class MbrPromotion implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "会员账号")
    private String loginName;

    @ApiModelProperty(value = "会员id")
    private Integer accountId;

    @ApiModelProperty(value = "点击次数")
    private Integer number;

    @ApiModelProperty(value = "是否点击 0否 1是")
    private Integer isClick;
}