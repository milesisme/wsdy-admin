package com.wsdy.saasops.modules.sys.dao;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SysI18nDto {
    @ApiModelProperty(value = "翻译源")
    @Excel(name = "source")
    private String source;
    @ApiModelProperty(value = "翻译目标值")
    @Excel(name = "translate")
    private String translate;
    @ApiModelProperty(value = "多语言标志")
    @Excel(name = "i18nflag")
    private String i18nflag;
}
