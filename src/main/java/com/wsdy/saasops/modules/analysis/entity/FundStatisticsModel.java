package com.wsdy.saasops.modules.analysis.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: Miracle
 * @Description:
 * @Date: 10:51 2017/12/7
 **/
@Data
public class FundStatisticsModel {

    private String date;//日期
    private BigDecimal payout;//派彩
    private BigDecimal deposit;//存款
    private BigDecimal withdraw;//提款
    private BigDecimal profit;//优惠
}
