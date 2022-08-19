package com.wsdy.saasops.modules.sdyExcel.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

@Data
public class ValilBetEntityDto {

    //会员名
    @Excel(name = "memberaccount")
    private String memberaccount;

    @Excel(name = "totalvalidbet")
    private String totalvalidbet;

    @Excel(name = "totaldp")
    private String totaldp;
}
