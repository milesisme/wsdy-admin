package com.wsdy.saasops.modules.member.entity;


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
@ApiModel(value = "mbr_warning_condition", description = "")
@Table(name = "mbr_warning_condition")
public class MbrWarningCondition {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 运营账号1，持续盈利，2，盈利比例高，3盈利金额大，4优惠比例，5同IP投注，6同设备投注
     */
    private Integer  type;

    /**
     * 参数1
     */
    private String param1;

    /**
     * 参数2
     */
    private String param2;


    /**
     * 参数3
     */
    private String param3;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 处理时间
     */
    private String updateTime;

    /**
     * 处理用户
     */
    private String updateUser;



}
