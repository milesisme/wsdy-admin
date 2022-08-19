package com.wsdy.saasops.agapi.modulesV2.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AgentV2AccountLogDto {
    // 查询参数
    @ApiModelProperty(value = "开始时间")
    private String startTime;
    @ApiModelProperty(value = "结束时间")
    private String endTime;
    @ApiModelProperty(value = "用户类型 用户（代理）agent 用户（会员）mbr  执行用户 operator")
    private String userType;
    @ApiModelProperty(value = "更改项目")
    private String moduleName;
    @ApiModelProperty(value = "搜索用户名")
    private String searchName;
    @ApiModelProperty(value = "分页：页码")
    private Integer pageNo;
    @ApiModelProperty(value = "分页：分页大小")
    private Integer pageSize;

    // 其他字段
    @ApiModelProperty(value = "查询：登录代理名/ 返回：执行代理名")
    private String agyAccount;
    @ApiModelProperty(value = "查询：登录代理Id / 返回：执行代理id")
    private Integer agyId;

    // 返回字段
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "被执行用户名")
    private String operatorUser;
    @ApiModelProperty(value = "createTime")
    private String createTime;
    @ApiModelProperty(value = "变更前")
    private String beforeChange;
    @ApiModelProperty(value = "变更后")
    private String afterChange;
    @ApiModelProperty(value = "操作ip")
    private String ip;
    @ApiModelProperty(value = "备注")
    private String memo;
}

