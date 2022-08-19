package com.wsdy.saasops.modules.member.dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Transient;
import java.math.BigDecimal;
@Getter
@Setter
public class TotalDto {
    /**
     * 总金额
     */
    @Transient
    private BigDecimal totalBalance;
    /**
     * 总存款
     */
    @Transient
    private BigDecimal totalDeposit;
    /**
     * 总取款
     */
    @Transient
    private BigDecimal totalWithdrawal;

    private  Integer accountId;
}
