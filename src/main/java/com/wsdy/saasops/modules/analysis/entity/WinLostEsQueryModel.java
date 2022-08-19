package com.wsdy.saasops.modules.analysis.entity;

import lombok.Data;

@Data
public class WinLostEsQueryModel {
    /**站点前缀*/
    private String siteCode;
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
    /**
     * 会员Id*/
    private String accountId;

}
