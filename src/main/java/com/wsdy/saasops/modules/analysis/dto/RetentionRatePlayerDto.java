package com.wsdy.saasops.modules.analysis.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RetentionRatePlayerDto {

    /**
     * accountId
     */
    private Integer accountId;

    /**
     * 首充日期
     */
    private String  firstChargeDate;


    /**
     * 会员名
     */
    private String loginName;
}
