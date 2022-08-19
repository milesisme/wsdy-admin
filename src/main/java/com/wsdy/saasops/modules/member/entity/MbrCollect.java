package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@ApiModel(value = "MbrCollect", description = "")
@Table(name = "mbr_collect")
@ToString
public class MbrCollect implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "菜单ID")
    private Long menuId;

    @Transient
    private String name;

    @Transient
    private String url;

    @Transient
    private List<Long> menuIds;
}