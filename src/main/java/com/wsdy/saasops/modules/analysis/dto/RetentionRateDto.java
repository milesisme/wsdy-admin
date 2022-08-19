package com.wsdy.saasops.modules.analysis.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RetentionRateDto {


    /**
     * 用户名 代理查询传递代理名称，用户查询传递用户名称
     */
    private String userName;

    /**
     * 首充开始时间
     */
    private String startTime;

    /**
     * 首充结束时间
     */
    private String endTime;

    /**
     *
     */
    private Integer pageNo;

    /**
     *
     */
    private Integer pageSize;

}
