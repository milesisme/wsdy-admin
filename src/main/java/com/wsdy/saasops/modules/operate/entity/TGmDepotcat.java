package com.wsdy.saasops.modules.operate.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;


@Setter
@Getter
@ApiModel(value = "根据游戏类别找出对应那些平台有这个类别的游戏", description = "")
@Table(name = "t_gm_depotcat")
public class TGmDepotcat implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "平台Id")
    private Integer depotId;

    @ApiModelProperty(value = "类别Id")
    private Integer catId;

    @ApiModelProperty(value = "类别名称")
    @Transient
    private String catName;

    @ApiModelProperty(value = "平台名称")
    @Transient
    private String depotName;

    @ApiModelProperty(value = "游戏表logo")
    @Transient
    private String logo2;

    @ApiModelProperty(value = "游戏表个性图")
    @Transient
    private String logo;

    @ApiModelProperty(value = "PC个性图")
    @Transient
    private String picUrl;

    @ApiModelProperty(value = "PC logo")
    @Transient
    private String logoPc;

    @ApiModelProperty(value = "手机个性图")
    @Transient
    private String mbPicUrl;

    @ApiModelProperty(value = "手机 logo")
    @Transient
    private String logoMb;

    @ApiModelProperty(value = "APP个性图")
    @Transient
    private String appPicUrl;

    @ApiModelProperty(value = "APP logo")
    @Transient
    private String logoApp;

    @ApiModelProperty(value = "APP logo")
    @Transient
    private String depotBackgroundImage;

    @ApiModelProperty(value = "游戏文字说明")
    @Transient
    private String gameTag;

    @ApiModelProperty(value = "游戏文字说明")
    @Transient
    private String titleTag;

    @ApiModelProperty(value = "排序号")
    @Transient
    private String sortId;

    @ApiModelProperty(value = "体育未有登陆 URL链接")
    @Transient
    private String pcUrlTag;

    @Transient
    @ApiModelProperty(value = "1开启，0禁用,2维护")
    private Byte availableWh;

    @Transient
    @ApiModelProperty(value = "是否支持试玩  0 否  1是  默认0")
    private Integer isTry;

    @Transient
    @ApiModelProperty(value = "是否有大厅  1 是 0否")
    private Integer isHall;
    @Transient
    @ApiModelProperty(value = "平台code")
    private String depotCode;
    @Transient
    @ApiModelProperty(value = "二级游戏列表")
    private List<OprGame> gameList;
    @Transient
    @ApiModelProperty(value = "查询用，终端")
    private Byte terminal;
    @Transient
    @ApiModelProperty(value = "查询用，站点code")
    private String siteCode;
    @Transient
    @ApiModelProperty(value = "横竖屏标志：  0是不限制  1=竖屏  2=横屏")
    private Byte orientation;
    @Transient
    @ApiModelProperty(value = "memo")
    private String memo;
}