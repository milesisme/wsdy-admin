package com.wsdy.saasops.modules.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MbrWarningConditionDto {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 运营账号1，持续盈利，2，盈利比例高，3盈利金额大，4优惠比例，5同IP投注，6同设备投注
     */
    private Integer  type;

    /**
     * 参数1
     */

    @JsonIgnore
    private Integer param1;

    /**
     * 参数2
     */
    @JsonIgnore
    private Integer param2;


    /**
     * 参数3
     */
    @JsonIgnore
    private Integer param3;



    /**
     * 参数4
     */
    @JsonIgnore
    private Integer param4;

    /**
     * 模板
     */
    @JsonIgnore
    private String template;


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
    private String updateTime;

    /**
     * 处理用户
     */
    private String updateUser;

}
