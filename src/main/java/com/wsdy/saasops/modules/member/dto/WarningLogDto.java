package com.wsdy.saasops.modules.member.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WarningLogDto {

    public WarningLogDto(String loginName, String userName, String content, Integer type) {
        this.loginName = loginName;
        this.userName = userName;
        this.content = content;
        this.type = type;
    }

    public WarningLogDto(String userName, String content, Integer type) {
        this.userName = userName;
        this.content = content;
        this.type = type;
    }


    /**
     * 会员账号
     */
    private String loginName;

    /**
     * 运营账号
     */
    private String userName;


    /**
     * 内容
     */
    private String content;


    /**
     * 预警类型
     */
    private Integer type;


}
