package com.wsdy.saasops.modules.base.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Table(name="t_bs_financialcode")
public class BaseFinancialCode {
    /**
     * 
     * 表 : t_bs_financialcode
     * 对应字段 : id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 
     * 表 : t_bs_financialcode
     * 对应字段 : code
     */
    @Column
    private String code;

    /**
     * 
     * 表 : t_bs_financialcode
     * 对应字段 : codeName
     */
    @Column
    private String codeName;

    /**
     * 操作类型，0 支出1 收入
     * 表 : t_bs_financialcode
     * 对应字段 : opType
     */
    @Column
    private Byte opType;

 
}