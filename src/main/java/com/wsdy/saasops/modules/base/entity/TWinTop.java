package com.wsdy.saasops.modules.base.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;


@Setter
@Getter
@ApiModel(value = "TWinTop", description = "")
@Table(name = "t_win_top")
public class TWinTop implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "中奖时间")
    private String winDate;

    @ApiModelProperty(value = "中奖金额")
    private BigDecimal winAmount;

    @ApiModelProperty(value = "登陆名")
    private String loginName;

    @ApiModelProperty(value = "游戏代码")
    @JsonIgnore
    private String gameCode;

    @ApiModelProperty(value = "游戏平台")
    @JsonIgnore
    private Integer depotId;

    @Transient
    @ApiModelProperty(value = "LOGO")
    private String logo;

    @Transient
    @ApiModelProperty(value = "当前LOGO游戏gameId")
    private Integer gameId;
}