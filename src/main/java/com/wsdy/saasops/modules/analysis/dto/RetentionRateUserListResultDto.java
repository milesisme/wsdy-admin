package com.wsdy.saasops.modules.analysis.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Setter
@Getter
public class RetentionRateUserListResultDto {

    /**
     * 时间
     */
    private String time;


    /**
     * 用户名称
     */
    private String userName;

    /**
     * 留存数据
     */
    private List<RetentionRateResultDto> retentionRateResultDtoList;
}
