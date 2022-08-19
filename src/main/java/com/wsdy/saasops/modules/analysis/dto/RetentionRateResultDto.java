package com.wsdy.saasops.modules.analysis.dto;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RetentionRateResultDto {

    /**
     * 日期
     */
    private String date;

    /**
     * 数量
     */
    private Integer num;
}
