package com.wsdy.saasops.modules.analysis.dto;


import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExportRetentionRateDto {

    /**
     * 时间
     */
    @Excel(name = "首存日期", width = 20, orderNum = "1")
    private String time;


    /**
     * 首充数量
     */
    @Excel(name = "首存人数", width = 20, orderNum = "2")
    private Integer firstChargeTimeNum;


    /**
     * 第1天
     */
    @Excel(name = "1", width = 10, orderNum = "3")
    private String d1;

    /**
     * 第2天
     */
    @Excel(name = "2", width = 10, orderNum = "4")
    private String d2;

    /**
     * 第3天
     */
    @Excel(name = "3", width = 10, orderNum = "5")
    private String d3;


    /**
     * 第4天
     */
    @Excel(name = "4", width = 10, orderNum = "6")
    private String d4;


    /**
     * 第5天
     */
    @Excel(name = "5", width = 10, orderNum = "7")
    private String d5;


    /**
     * 第6天
     */
    @Excel(name = "6", width = 10, orderNum = "8")
    private String d6;


    /**
     * 第7天
     */
    @Excel(name = "7", width = 10, orderNum = "9")
    private String d7;


    /**
     * 第8天
     */
    @Excel(name = "8", width = 10, orderNum = "10")
    private String d8;


    /**
     * 第9天
     */
    @Excel(name = "9", width = 10, orderNum = "11")
    private String d9;

    /**
     * 第10天
     */
    @Excel(name = "10", width = 10, orderNum = "12")
    private String d10;

    /**
     * 第11天
     */
    @Excel(name = "11", width = 10, orderNum = "13")
    private String d11;

    /**
     * 第12天
     */
    @Excel(name = "12", width = 10, orderNum = "14")
    private String d12;

    /**
     * 第13天
     */
    @Excel(name = "13", width = 10, orderNum = "15")
    private String d13;

    /**
     * 第14天
     */
    @Excel(name = "14", width = 10, orderNum = "16")
    private String d14;

    /**
     * 第15天
     */
    @Excel(name = "15", width = 10, orderNum = "17")
    private String d15;

    /**
     * 第16天
     */
    @Excel(name = "16", width = 10, orderNum = "18")
    private String d16;

    /**
     * 第17天
     */
    @Excel(name = "17", width = 10, orderNum = "19")
    private String d17;

    /**
     * 第18天
     */
    @Excel(name = "18", width = 10, orderNum = "20")
    private String d18;

    @Excel(name = "19", width = 10, orderNum = "21")
    private String d19;

    @Excel(name = "20", width = 10, orderNum = "22")
    private String d20;

    @Excel(name = "21", width = 10, orderNum = "23")
    private String d21;

    @Excel(name = "22", width = 10, orderNum = "24")
    private String d22;

    @Excel(name = "23", width = 10, orderNum = "25")
    private String d23;

    @Excel(name = "24", width = 10, orderNum = "26")
    private String d24;

    @Excel(name = "25", width = 10, orderNum = "27")
    private String d25;

    @Excel(name = "26", width = 10, orderNum = "28")
    private String d26;

    @Excel(name = "27", width = 10, orderNum = "29")
    private String d27;

    @Excel(name = "28", width = 10, orderNum = "30")
    private String d28;

    @Excel(name = "29", width = 10, orderNum = "31")
    private String d29;

    @Excel(name = "30", width = 10, orderNum = "32")
    private String d30;










}
