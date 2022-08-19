package com.wsdy.saasops.modules.analysis.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;

@Setter
@Getter
public class AccountWinLostReportExcelDto {

    @Excel(name = "账号", width = 20, orderNum = "1")
    private String username;

    @Excel(name = "级别", width = 20, orderNum = "2")
    private String levelName;

    @Excel(name = "部门", width = 20, orderNum = "3")
    private String deName = StringUtils.EMPTY;


    @Excel(name = "首存时间", width = 20, orderNum = "4")
    private String  depositTime = StringUtils.EMPTY;

    @Excel(name = "投注人数", width = 20, orderNum = "5")
    @ApiModelProperty(value = "投注人数")
    private Long total;

    @Excel(name = "注单数", width = 20, orderNum = "6")
    @ApiModelProperty(value = "投注数")
    private Integer quantity;

    @Excel(name = "投注金额", width = 20, orderNum = "7")
    @ApiModelProperty(value = "投注金额")
    private BigDecimal betTotal;

    @Excel(name = "有效投注", width = 20, orderNum = "8")
    @ApiModelProperty(value = "有效投注额")
    private BigDecimal validbetTotal;

    @Excel(name = "输赢", width = 20, orderNum = "9")
    @ApiModelProperty(value = "派彩")
    private BigDecimal payoutTotal;

    @Excel(name = "盈利比", width = 20, orderNum = "10")
    private String rate;
}
