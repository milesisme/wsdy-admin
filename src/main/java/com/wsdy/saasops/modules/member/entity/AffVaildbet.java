package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Setter
@Getter
@ApiModel(value = "AffVaildbet", description = "")
@Table(name = "aff_vaildbet")
@ToString
public class AffVaildbet implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "会员账号")
    private String loginName;

    @ApiModelProperty(value = "有效投注")
    private BigDecimal totalvalidbet;

    @ApiModelProperty(value = "存取款")
    private BigDecimal totaldp;

}