package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;


@Getter
@Setter
@ApiModel(value = "MbrExperience")
@Table(name = "mbr_experience")
public class MbrExperience  implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "父级用户名")
    private String userName;

    @ApiModelProperty(value = "子级用户名")
    private String subUserName;

    @ApiModelProperty(value = "IP")
    private String ip;

    @ApiModelProperty(value = "申请时间")
    private String applyTime;
}
