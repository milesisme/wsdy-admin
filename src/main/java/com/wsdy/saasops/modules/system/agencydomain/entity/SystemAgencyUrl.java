package com.wsdy.saasops.modules.system.agencydomain.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Setter
@Getter
@ApiModel(value = "SystemAgencyUrl", description = "")
@Table(name = "set_basic_agencyurl")
public class SystemAgencyUrl implements Serializable,Cloneable{
private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "代理机构Id")
    private Integer agencyId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    //名称
    @ApiModelProperty(value = "名称")
    private String name;
    //状态
    @ApiModelProperty(value = "状态 1;审批中 2：绑定 3：解绑")
    private Integer state;
    //域名
    @ApiModelProperty(value = "域名")
    private String url;
    @ApiModelProperty(value = "是否绑定  1：绑定 2：未绑定")
    private Integer bind;
    //创建时间
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    //最后一次更新时间
    @ApiModelProperty(value = "最后一次更新时间")
    private Date modifyTime;

    @ApiModelProperty(value = "会员数量")
    @Transient
    private Integer sum ;

    @ApiModelProperty(value = "状态 查询使用")
    @Transient
    private String states;

    @ApiModelProperty(value = "代理账号名称")
    @Transient
    private String agyAccount ;
    @ApiModelProperty(value = "代理账号名称")
    @Transient
    private List<Integer> agencyIds ;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}