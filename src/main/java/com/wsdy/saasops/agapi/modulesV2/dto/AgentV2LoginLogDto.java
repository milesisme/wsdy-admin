package com.wsdy.saasops.agapi.modulesV2.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AgentV2LoginLogDto {
    // 查询参数
    @ApiModelProperty(value = "开始时间")
    private String startTime;
    @ApiModelProperty(value = "结束时间")
    private String endTime;
    @ApiModelProperty(value = "用户类型 mbr会员 agent 代理")
    private String userType;
    @ApiModelProperty(value = "本次登陆IP")
    private String loginIp;
    @ApiModelProperty(value = "搜索用户名")
    private String searchName;
    @ApiModelProperty(value = "分页：页码")
    private Integer pageNo;
    @ApiModelProperty(value = "分页：分页大小")
    private Integer pageSize;

    // 其他字段
    @ApiModelProperty(value = "登录代理名")
    private String agyAccount;
    @ApiModelProperty(value = "登录代理Id")
    private Integer agyId;

    // 返回字段
    @ApiModelProperty(value = "上级名")
    private String parentName;
    @ApiModelProperty(value = "log id")
    private Integer id;
    @ApiModelProperty(value = "用户ID")
    private Integer accountId;
    @ApiModelProperty(value = "用户")
    private String loginName;
    @ApiModelProperty(value = "别名")
    private String realName;
    @ApiModelProperty(value = "登陆时间")
    private String loginTime;

    @ApiModelProperty(value = "赢输金额")
    private BigDecimal payoutTotal;
}

