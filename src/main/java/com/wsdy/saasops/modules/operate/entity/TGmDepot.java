package com.wsdy.saasops.modules.operate.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;


@Setter
@Getter
@ApiModel(value = "TGmDepot", description = "平台类别名称")
@Table(name = "t_gm_depot")
public class TGmDepot implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "自增长字段")
    private String depotCode;

    @ApiModelProperty(value = "平台名称")
    private String depotName;

    @ApiModelProperty(value = "1开启，0禁用")
    private Byte available;

    @ApiModelProperty(value = "")
    private String memo;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "最后一次修改人的账号")
    private String modifyUser;

    @ApiModelProperty(value = "最后一次修改时间")
    private String modifyTime;

    @ApiModelProperty(value = "游戏开始时间")
    private String startDate;

    @ApiModelProperty(value = "游戏结束时间")
    private String endDate;

    @ApiModelProperty(value = "排序号")
    private Integer sortId;

    @Transient
    private String logo;

    @Transient
    private String mbPicUrl;

    @Transient
    @ApiModelProperty(value = "平台分类")
    private String catNames;

    @ApiModelProperty(value = "是否有第三方账号")
    @Transient
    private Byte hasAccount;

    @ApiModelProperty(value = "第三方是否转账过 0 否 1是")
    @Transient
    private Byte hasTransfer;

    @Transient
    @ApiModelProperty(value = " 0代表PC端,1代表手机")
    private Byte terminal;

    @Transient
    @ApiModelProperty(value = "平台分类")
    private String catName;

    @ApiModelProperty(value = "平台分类ID")
    @Transient
    private Integer catId;

    @Transient
    private Integer depotId;

    @Transient
    @ApiModelProperty(value = "1开启，0禁用,2维护")
    private Byte availableWh;

    @Transient
    @ApiModelProperty(value = "是否支持试玩  0 否  1是  默认0")
    private Integer isTry;


    @Transient
    @ApiModelProperty(value = "是否是单一i")
    private Integer isSingleOne;
}