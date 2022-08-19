package com.wsdy.saasops.modules.analysis.dto;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RetentionRateUserResultDto {

    /**
     * 日期
     */
    private String userName;

    /**
     * 时间
     */
    private String date;

    /**
     * 数量
     */
    private Integer num;
}
