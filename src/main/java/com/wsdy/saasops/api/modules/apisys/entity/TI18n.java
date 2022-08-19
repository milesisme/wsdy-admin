package com.wsdy.saasops.api.modules.apisys.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Setter
@Getter
@ApiModel(value = "多语言信息", description = "多语言信息")
@Table(name = "t_i18n")
public class TI18n implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "翻译源")
    private String source;
    @ApiModelProperty(value = "翻译目标值")
    private String translate;
    @ApiModelProperty(value = "多语言标志")
    private String i18nflag;
    @ApiModelProperty(value = "语种标识 en_us英文 vi越南语 ID印尼语")
    private String language;
}