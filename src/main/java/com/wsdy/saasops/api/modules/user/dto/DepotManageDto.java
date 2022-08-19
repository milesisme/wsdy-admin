package com.wsdy.saasops.api.modules.user.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepotManageDto implements Serializable {


    private static final long serialVersionUID = -3830368859448481362L;
    /** 平台 */
    @ApiModelProperty(value = "平台")
    private String platform;
    /** 平台id */
    @ApiModelProperty(value = "平台id")
    private Integer platformId;
    /** 线路id */
    @ApiModelProperty(value = "线路id")
    private Integer apiId;
    /** 余额 */
    @ApiModelProperty(value = "余额")
    private BigDecimal balance;
    /** 用户名 */
    @ApiModelProperty(value = "用户名")
    private String player;
    /** 可以转账 */
    @ApiModelProperty(value = "可以转账")
    private boolean enableTransfer = true;
    /** 操作权限 */
    @ApiModelProperty(value = "操作权限")
    private DepotManageOperation opterate = new DepotManageOperation();

    @ApiModelProperty(value = "平台编码")
    private String depotCode;

}
