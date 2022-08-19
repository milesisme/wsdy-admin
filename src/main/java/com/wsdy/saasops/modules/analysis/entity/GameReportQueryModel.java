package com.wsdy.saasops.modules.analysis.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GameReportQueryModel {

    /**
     * 站点前缀
     */
    private String siteCode;
    /**
     * 总代理
     */
    private Integer parentAgentid;
    /**
     * 代理账号
     */
    private Integer agentid;
    /**
     * 会员组
     */
    private Integer groupid;
    /**
     * 会员名
     */
    private String loginName;
    /**
     * 游戏平台
     */
    private String platform;
    /**
     * 游戏分类ID
     */
    private Integer gameCatId;
    /**
     * 一级游戏分类
     */
    private String gametype;
    /**
     * 子级游戏分类
     */
    private List<String> subGametype;
    /**
     * 游戏名称
     */
    private String gamename;
    /**
     * 设备
     */
    private String origin;
    /**
     * 注单ID
     */
    private String betid;
    /**
     * 状态
     */
    private String status;
    /**
     * 结果：输、赢
     **/
    private String result;
    /**
     * 大于下注
     */
    private BigDecimal gtBet;
    private BigDecimal ltBet;
    /**
     * 大于有效投注
     */
    private BigDecimal gtValidBet;
    private BigDecimal ltValidBet;
    /**
     * 大于派彩
     */
    private BigDecimal gtReward;
    private BigDecimal ltReward;
    /**
     * 查询彩金下注，奖池下注大于0
     */
    private BigDecimal gtJpBet;
    private BigDecimal ltJpBet;
    /**
     * 查询彩金中奖，奖池派彩大于0
     */
    private BigDecimal gtJpReward;
    private BigDecimal ltJpReward;
    /**
     * 投注开始时间
     */
    private String betStrTime;
    /**
     * 投注结束时间
     */
    private String betEndTime;
    /**
     * 游戏类型下拉框
     */
    private String gameValue;
    /**
     * 桌号
     */
    private String tableNo;
    /**
     * 局号
     */
    private String serialId;


    //查询使用
    /**
     * 总代理
     */
    @Transient
    private List<Integer> parentAgentidList;
    /**
     * 代理账号
     */
    @Transient
    private List<Integer> agentidList;
    /**
     * 会员组
     */
    @Transient
    private List<Integer> groupidList;
    /**
     * 游戏平台
     */
    @Transient
    private List<String> platformList;
    /**
     * 设备
     */
    @Transient
    private List<String> originList;
    /**
     * 一级游戏分类
     */
    @Transient
    private List<String> gametypeList;
    /*派彩开始时间*/
    private String payOutStrTime;
    /*派彩结束时间*/
    private String payOutEndTime;
    /*推荐人*/
    private String supLoginName;

    @Transient
    @ApiModelProperty(value = "总代查询")
    private String tagencyIdList;
    @Transient
    @ApiModelProperty(value = "代理查询")
    private String cagencyIdList;

    @ApiModelProperty(value = "代理名称")
    private String agyAccount;

    @ApiModelProperty(value = "eg导出")
    private Boolean isEgBetDetauls;

    @ApiModelProperty(value = "是否查询小计")
    private Boolean isSubtotal = Boolean.TRUE;

    @ApiModelProperty(value = "sdyNet接口特殊处理")
    private Boolean isSdyNet = Boolean.FALSE;

    @ApiModelProperty(value = "注单类型： 1 打赏小费  0 正常注单")
    private Integer isTip;

    private String downloadStrTime;
    private String downloadEndTime;

    @ApiModelProperty(value = "分线代理id")
    private Integer subCagencyId;
}
