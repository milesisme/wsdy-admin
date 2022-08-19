package com.wsdy.saasops.modules.sys.entity;


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
@ApiModel(value = "sys_warning", description = "")
@Table(name = "sys_warning")
public class SysWarning {
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
     * 会员账号
     */
    private String loginName;

    /**
     * 运营账号
     */
    private String userName;

    /**
     * 预警类型  1IP异常, 2资金调整, 3存款, 4 彩金, 5 存款手动通过, 6 绑定钱包和卡
     */
    private Integer type;


    /**
     * 内容
     */
    private String content;

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
