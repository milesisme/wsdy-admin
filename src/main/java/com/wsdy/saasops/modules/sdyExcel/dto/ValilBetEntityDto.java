package com.wsdy.saasops.modules.sdyExcel.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

@Data
public class ValilBetEntityDto {

    //δΌεε
    @Excel(name = "memberaccount")
    private String memberaccount;

    @Excel(name = "totalvalidbet")
    private String totalvalidbet;

    @Excel(name = "totaldp")
    private String totaldp;
}
