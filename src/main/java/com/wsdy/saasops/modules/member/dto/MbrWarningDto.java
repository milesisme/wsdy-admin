package com.wsdy.saasops.modules.member.dto;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MbrWarningDto {
    /**
     * ID
     */
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
     * 额外内容
     */
    private String exContent;

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
