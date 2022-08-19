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
@Table(name = "i18n_collect")
public class TI18nCollect implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "翻译源")
    private String source;
    @ApiModelProperty(value = "0已处理 1未处理")
    private Integer translateFlag;
    @ApiModelProperty(value = "创建时间")
    private String createTime;
    @ApiModelProperty(value = "创建时间")
    private String modifyTime;
}