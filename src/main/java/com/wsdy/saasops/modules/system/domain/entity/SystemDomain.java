package com.wsdy.saasops.modules.system.domain.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Setter
@Getter
@ApiModel(value = "SystemDomain", description = "站点域名")
@Table(name = "set_basic_domain")
public class SystemDomain implements Serializable,Cloneable{
private static final long serialVersionUID=1L;

    //id
    @Id
    @ApiModelProperty(value = "id")
    private Integer id;
    //名称
    @ApiModelProperty(value = "名称")
    private String name;
    //域名类型 1管理后台 2网站主页 3支付域名 4代理后台
    @ApiModelProperty(value = "域名类型 1管理后台 2网站主页 3支付域名 4代理后台")
    private Integer domainType;
    //域名路径
    @ApiModelProperty(value = "域名路径")
    private String domainUrl;
    //是否绑定 1：绑定 2：未绑定
    @ApiModelProperty(value = "是否绑定 1：绑定 2：未绑定")
    private Integer bind;
    //1;审批中 2：绑定 3：解绑
    @ApiModelProperty(value = "1;审批中 2：绑定 3：解绑")
    private Integer state;
    //创建时间
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    //最后一次更新时间
    @ApiModelProperty(value = "最后一次更新时间")
    private Date modifyTime;
    @Transient
    @ApiModelProperty(value = "1;审批中 2：绑定 3：解绑 查询使用")
    private String states;
    @Transient
    @ApiModelProperty(value = "批量删除使用")
    private List<Integer> ids;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}