package com.wsdy.saasops.modules.sdyExcel.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

@Data
public class BankInputEntityDto {

    //会员名
    @Excel(name = "membercode")
    private String membercode;

    //银行卡号
    @Excel(name = "bankacc")
    private String bankacc;

    //姓名
    @Excel(name = "bankaccname")
    private String bankaccname;

    //银行名称
    @Excel(name = "bankname")
    private String bankname;

    //province
    @Excel(name = "province")
    private String province;

    //city
    @Excel(name = "city")
    private String city;

    //city
    @Excel(name = "district")
    private String district;
}
