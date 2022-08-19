package com.wsdy.saasops.modules.analysis.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RptBetModel implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
     * 注单唯一编号，平台方获取
     **/
    private String id;
    /**
     * API前缀
     **/
    private String apiPrefix;
    /**
     * 前缀
     **/
    private String sitePrefix;
    /**
     * 站点名称
     **/
    private String website;
    /**
     * 游戏名称
     **/
    private String gameName;
    /**
     * 游戏类型：真人、老虎机
     **/
    private String gameType;
    /**
     * 游戏平台：PT、AG、BBIN
     **/
    private String platform;
    /**
     * 三方的游戏平台code
     **/
    private String platformCode;
    /**
     * 玩家用户名
     **/
    private String userName;
    /**
     * 投注
     **/
    private BigDecimal bet = BigDecimal.ZERO;
    /**
     * 投注类型
     **/
    private String betType;
    /**
     * 场次
     **/
    private String roundNo;
    /***桌号**/
    private String tableNo;
    /**
     * 局号
     **/
    private String serialId;
    /**
     * 有效投注
     **/
    private BigDecimal validBet = BigDecimal.ZERO;
    /**
     * 派彩
     **/
    private BigDecimal payout = BigDecimal.ZERO;
    /**
     * 奖池投注
     **/
    private BigDecimal jackpotBet = BigDecimal.ZERO;
    /**
     * 奖池赢得
     **/
    private BigDecimal jackpotPayout = BigDecimal.ZERO;
    /**
     * 结果：输、赢
     **/
    private String result;
    /**
     * 状态：已结算、未结算
     **/
    private String status;
    /**
     * 小费
     **/
    private BigDecimal tip = BigDecimal.ZERO;
    /**
     * 投注时间
     **/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT")
    private Date betTime;
    /**
     * 开赛时间
     **/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT")
    private Date startTime;
    /**
     * 开赛时间
     **/
    private String matchTime;
    /**
     * 派彩时间
     **/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT")
    private Date payoutTime;
    /**
     * 下载时间
     **/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT")
    private Date downloadTime;
    /**
     * 账务时间
     **/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT")
    private Date orderDate;
    /**
     * '1.行动装置下单：M 1‐1.ios手机：MI1 1‐2.ios平板：MI2 1‐3.Android手机：MA1 1‐4.Android平板：MA2 2.计算机下单：P'
     **/
    private String origin;
    /**
     * 币别
     **/
    private String currency;
    /**
     * 下注前余额 有些平台无此值
     **/
    private BigDecimal balanceBefore = BigDecimal.ZERO;
    /**
     * 派彩后余额 有些平台无此值
     **/
    private BigDecimal balanceAfter = BigDecimal.ZERO;
    ;
    /**
     * 游戏详情JSON结果
     */
    private String openResultDetail;
    /**
     * 游戏详情结果封装
     */
    private OpenResultModel openResultModel;
    //总代id
    private Integer tagencyId;
    /**
     * 游戏分类
     **/
    private String gameCategory;

    @ApiModelProperty(value = "eg注单详情")
    private String egDetails;

    @ApiModelProperty(value = "eg注单牌闲")
    private String xOpenResult;

    @ApiModelProperty(value = "eg注单牌庄")
    private String zOpenResult;

    @ApiModelProperty(value = "代理名称")
    private String agyAccount;

    // 投注记录新增字段
    @ApiModelProperty(value = "赔率")
    private String odds;
    @ApiModelProperty(value = "玩法类型")
    private String playType;
    @ApiModelProperty(value = "联赛名称")
    private String leagueName;
    @ApiModelProperty(value = "比赛队伍")
    private String team;
    @ApiModelProperty(value = "下注时比分")
    private String betScore;
    @ApiModelProperty(value = "开奖结果")
    private String resultOpen;

    @ApiModelProperty(value = "catid：游戏类型")
    private String catid;

    @ApiModelProperty(value = "是否包赔 包赔注单，type值为8")
    private String type;

    @ApiModelProperty(value = "最高可赢额 原生体育专用")
    private String estimatedPayout;
    
    @ApiModelProperty(value = "赔率类型")
    private String oddsType;
    
    @ApiModelProperty(value = "全场赛事比分")
    private String resultScore;
    
    @ApiModelProperty(value = "半场赛事比分")
    private String halfResultScore;

    @ApiModelProperty(value = "下注类型 1=余额下注，2=体验金下注")
    private Integer userBetType;
    
    @ApiModelProperty(value = "盘口状态 0滚球  1非滚球")
    private Integer matchType;
    
    @ApiModelProperty(value = "是否串关")
    private Boolean isCombination;

//    /**游戏详情结果封装(导出用)*/
//    private String openResultModelStr;
}

