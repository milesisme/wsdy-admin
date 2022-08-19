package com.wsdy.saasops.modules.base.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Table(name="t_bs_area")
@Getter
@Setter
public class BaseArea {
    /**
     * 自增长ID
     * 表 : base_area
     * 对应字段 : id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column
    private Integer cityIndex;
    
    @Column
    private Integer provinceId;
    /**
     * 市
     * 表 : base_area
     * 对应字段 : city
     */
    @Column
    private String city;
    /**
     * 省
     * 表 : base_area
     * 对应字段 : prov
     */
    @Column
    private String prov;
}