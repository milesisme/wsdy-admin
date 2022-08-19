package com.wsdy.saasops.modules.analysis.entity;

import lombok.Data;

import java.math.BigDecimal;
/**
 * 输赢报表数据返回封装与查询传参*/
@Data
public class WinLostReport {

    /**
     * 查询*/
    //页码
    private Integer pageNo;
    /**
     * 查询*/
    //页面显示行数
    private Integer pageSize;
    /**
     * 查询*/
    //开始时间
    private String startTime;
    /**
     * 查询*/
    //结束时间
    private String endTime;
    /**
     * 查询*/
    //平台ID
    private String depotId;
    /**
     * 查询*/
    //类型ID
    private String catId;
    /**
     * 查询*/
    //子类ID
    private String subCatId;


    //投注人数
    private Long total;
    //投注金额
    private BigDecimal betTotal;
    //有效投注额
    private BigDecimal validbetTotal;
    //派彩金额
    private BigDecimal payoutTotal;
    //盈亏比例
    private BigDecimal winLostRatio;
    //游戏类别分类
    private String categoryTotal;
    //级别
    private String level;
    //总代ID
    private Integer tagencyId;
    //代理ID
    private Integer cagencyId;
    //用户名称
    private String userName;
    //会员ID
    private String accountId;
    //会员登录名
    private String loginName;
    //游戏平台
    private String depositName;
    //游戏类型
    private String catName;
    //游戏名称
    private String gameName;
    //游戏局数
    private String gameTimes;


}
