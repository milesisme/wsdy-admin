package com.wsdy.saasops.api.modules.apisys.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Setter
@Getter
@ApiModel(value = "站点信息", description = "站点信息")
@Table(name = "t_schema")
public class Tschema {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "库表SCHEMA")
    private String schemaName;
    @ApiModelProperty(value = "站点前缀code")
    private String siteCode;
    @ApiModelProperty(value = "0:未使用 1:已使用")
    private Byte   isUsed;
    @ApiModelProperty(value = "建立时间")
    private String createTime;
    @ApiModelProperty(value = "修改时间")
    private String modifyTime;
    @ApiModelProperty(value = "站点简称")
    private String simpleName;
}
