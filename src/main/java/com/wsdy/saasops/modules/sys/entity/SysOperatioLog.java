package com.wsdy.saasops.modules.sys.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.Id;
import java.util.Date;

@Setter
@Getter
@ApiModel(value = "SysOperatioLog", description = "SysOperatioLog")
public class SysOperatioLog {

    @Id
    @ApiModelProperty(value = "站点前缀")
    private String sitePrefix;

    @ApiModelProperty(value = "操作人")
    private String userName;

    @ApiModelProperty(value = "操作时间")
    private Date operationTime;

    @ApiModelProperty(value = "操作模块")
    private String operatioTitle;

    @ApiModelProperty(value = "操作描述")
    private String operatioText;

    @ApiModelProperty(value = "操作状态")
    private String status;

    @ApiModelProperty(value = "操作IP")
    private String ip;

    @ApiModelProperty(value = "终端设备")
    private String operatioEquipment;

    @ApiModelProperty(value = "接口处理时间毫秒")
    private Integer time;

    @ApiModelProperty(value = "方法")
    private String method;

    @ApiModelProperty(value = "方法描述")
    private String methodText;

    @ApiModelProperty(value = "Controller")
    private String controllerName;

    @ApiModelProperty(value = "操作时间开始")
    private String operationTimeFrom;

    @ApiModelProperty(value = "操作时间结束")
    private String operationTimeTo;

    @ApiModelProperty(value = "登录来源  dev：(PC、H5、Android、IOS)")
    private String dev;
}
