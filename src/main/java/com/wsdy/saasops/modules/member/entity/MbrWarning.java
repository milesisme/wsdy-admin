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
@ApiModel(value = "mbr_warning", description = "")
@Table(name = "mbr_warning")
public class MbrWarning {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Long id;


    /**
     * 创建时间
     */
    private String  createTime;


    /**
     * 预警日期
     */
    private String  warningDate;

    /**
     * 会员账号
     */
    private String loginName;


    /**
     * 扩展内容
     */
    private String exContent;

    /**
     * 内容
     */
    private String content;


    /**
     *
     */
    private Integer type;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 处理时间
     */
    private String dealTime;

    /**
     * 处理用户
     */
    private String dealUser;

    /**
     * 备注
     */
    private String memo;

}
