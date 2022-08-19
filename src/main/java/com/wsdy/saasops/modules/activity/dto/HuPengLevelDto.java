package com.wsdy.saasops.modules.activity.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HuPengLevelDto {


    /**
     * 父级登录名
     */
    private String loginName;

    /**
     * 父级账号ID
     */
    private Integer accountId;

    /**
     * 深度
     */
    private Integer depth;


    /**
     * 子账号ID
     */
    private Integer subAccountId;

    /**
     * 子账号
     */
    private String subAccount;
}
