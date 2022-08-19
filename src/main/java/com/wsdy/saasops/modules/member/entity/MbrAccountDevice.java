package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@Setter
@Getter
@ApiModel(value = "MbrAccountDevice", description = "会员设备表")
@Table(name = "mbr_account_device")
@ToString
public class MbrAccountDevice implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "`会员id`")
    private Integer accountId;
    @ApiModelProperty(value = "会员账号")
    private String loginName;
    @ApiModelProperty(value = "设备唯一标识")
    private String deviceUuid;
    @ApiModelProperty(value = "设备类型：android,ios,H5,PC")
    private String deviceType;
    @ApiModelProperty(value = "浏览器类型，如 Mozilla")
    private String browserType;
    @ApiModelProperty(value = "创建时间")
    private String createTime;
    @ApiModelProperty(value = "是否拉黑操作过 0 否 1是")
    private Byte isBlackOpr;

    @Transient
    @ApiModelProperty(value = "同设备数量")
    private Integer count;
    
    @Transient
    @ApiModelProperty(value = "渠道num")
    private String channelNum;
}