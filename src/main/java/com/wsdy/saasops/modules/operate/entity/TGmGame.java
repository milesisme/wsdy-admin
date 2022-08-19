package com.wsdy.saasops.modules.operate.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@ApiModel(value = "TGmGame", description = "平台游戏列表")
@Table(name = "t_gm_game")
public class TGmGame implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "游戏类别代码")
    private Integer catId;

    @ApiModelProperty(value = "游戏子类别代码")
    private Integer subCatId;

    @ApiModelProperty(value = "平台代码")
    private Integer depotId;

    @ApiModelProperty(value = "游戏代码")
    private String gameCode;

    @ApiModelProperty(value = "手机游戏代码")
    private String mbGameCode;

    @ApiModelProperty(value = "安卓游戏代码")
    private String adGameCode;

    @ApiModelProperty(value = "桌面应用游戏代码")
    private String downGameCode;

    @ApiModelProperty(value = "游戏名称")
    private String gameName;

    @ApiModelProperty(value = "游戏简单描述")
    private String gameTag;

    @ApiModelProperty(value = "游戏链接URL")
    private String gameParam;

    @ApiModelProperty(value = "游戏链接URL")
    private String mbGameParam;

    @ApiModelProperty(value = "1开启，0禁用")
    private Byte available;

    @ApiModelProperty(value = "个性化图片")
    private String logo;

    @ApiModelProperty(value = "开启PC端 1->开启，0－>禁用")
    private Byte enablePc;

    @ApiModelProperty(value = "开启移动端 1->开启，0－>禁用")
    private Byte enableMb;

    @ApiModelProperty(value = "开启测试账号 1->开启，0－>禁用")
    private Byte enableTest;

    @ApiModelProperty(value = "开启App 1->开启，0－>禁用")
    private Byte enableApp;

    @ApiModelProperty(value = "热门游戏")
    private Byte enableHot;

    @ApiModelProperty(value = "最新游戏")
    private Byte ebableNew;

    @ApiModelProperty(value = "开启奖金池 1->开启，0－>禁用")
    private Byte enablePool;

    @ApiModelProperty(value = "详细备注")
    private String memo;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "最后一次修改人的账号")
    private String modifyUser;

    @ApiModelProperty(value = "最后一次修改时间")
    private String modifyTime;

    @ApiModelProperty(value = "赔付线数")
    private Integer compensateNum;

    @ApiModelProperty(value = "游戏名称(英文)")
    private String gameNameEn;

    @ApiModelProperty(value = "游戏ID")
    private String gameId;

    @ApiModelProperty(value = "URL")
    private String url;

    @ApiModelProperty(value = "PC端链接标识")
    private String pcUrlTag;

    @ApiModelProperty(value = "HTML5链接标识")
    private String htmlTag;

    @ApiModelProperty(value = "推荐指数")
    private Integer recRating;

    @ApiModelProperty(value = "奖池类型")
    private String poolCat;

    @ApiModelProperty(value = "奖金池参数")
    private String poolParam;

    @ApiModelProperty(value = "非电子 显示标识")
    private Byte topLink;

    @ApiModelProperty(value = "排序")
    private Integer sortId;

    @ApiModelProperty(value = "点击量")
    private Integer clickNum;

    @ApiModelProperty(value = "好评度")
    private Integer goodNum;

    @ApiModelProperty(value = "过去一个月这个游戏玩家数")
    private Integer monthPer;

    @ApiModelProperty(value = "昨天该游戏玩家数")
    private Integer lastdayPer;
    
    @ApiModelProperty(value = "桌号，真人游戏独有")
    private String tableCode;

    @Transient
    @ApiModelProperty(value = "游戏类别名称")
    private String catName;

    @Transient
    @ApiModelProperty(value = "游戏平台名称")
    private String depotName;

    @Transient
    @ApiModelProperty(value = "分类统计游戏总数")
    private Integer gameCount;

    @Transient
    @ApiModelProperty(value = "分类统计玩家数量（30天）")
    private Integer tMonthPer;

    @Transient
    @ApiModelProperty(value = "分类统计玩家数量（昨天）")
    private Integer tLastDayPer;

    @Transient
    @ApiModelProperty(value = "开启PC端 1->开启，0－>禁用")
    private Integer enableGmaePc;

    @Transient
    @ApiModelProperty(value = "开启移动端 1->开启，0－>禁用")
    private Integer enableGmaeMb;

    @Transient
    @ApiModelProperty(value = "开启App 1->开启，0－>禁用")
    private Integer enableGmaeApp;

    @Transient
    @ApiModelProperty(value = "平台代码")
    private String depotIds;

    @Transient
    @ApiModelProperty(value = "人气值")
    private Integer popularityGame;

    @Transient
    @ApiModelProperty(value = "详细备注")
    private String memoGmae;

    @Transient
    @ApiModelProperty(value = "临时变量")
    private Byte enablePcTem;

    @Transient
    @ApiModelProperty(value = "临时变量")
    private Byte enableMbTem;

    @Transient
    @ApiModelProperty(value = "临时变量")
    private Byte enableAppTem;
    @Transient
    private Byte terminal;

    @ApiModelProperty(value = "是否支持试玩  0 否  1是  默认0")
    private Integer isTry;
    @ApiModelProperty(value = "是否计算有效投注 1不计算有效投注 0计算有效投注")
    private Integer validBetType;

    @ApiModelProperty(value = "横竖屏标志： 0是不限制  1=竖屏  2=横屏")
    private Byte orientation;

    @Transient
    private BigDecimal waterrate;

    @Transient
    @ApiModelProperty(value = "平台流水费率")
    private String strWaterrate;
    
    @Transient
    @ApiModelProperty(value = "子分类名")
    private String subCatName;
}