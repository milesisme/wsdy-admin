package com.wsdy.saasops.agapi.modules.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ReportParamsDto {

    @ApiModelProperty(value = "开始时间开始")
    private String startTime;

    @ApiModelProperty(value = "开始时间结束")
    private String endTime;

    @ApiModelProperty(value = "直属代理 direct")
    private Integer cagencyId;

    @ApiModelProperty(value = "分线代理id")
    private Integer subCagencyId;

    @ApiModelProperty(value = "排序")
    private String orderBy;

    @ApiModelProperty(value = "倒序排序：true,false")
    private Boolean desc;
    
    @ApiModelProperty(value = "代理名")
    private String agyAccount;
    
    @ApiModelProperty(value = "游戏注单用户名")
    private String username;
    
    @ApiModelProperty(value = "代理id")
    private Integer agyId;
    
    @ApiModelProperty(value = "是否是子代 1是 0不是(总代)")
    private Integer isCagency;
    
    @ApiModelProperty(value = "代理等级")
    private Integer agentLevel;
    
    @ApiModelProperty(value = "是否是测试代理 true是 false不是")
    private Boolean isTest;
    
    @ApiModelProperty(value = "代理查询平台费， 服务费时，不包含自身代理")
    private Boolean isNotIncludeSelf;
    
    @ApiModelProperty(value = "部门id")
    private Integer departmentid;
}
