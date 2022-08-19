package com.wsdy.saasops.modules.sys.dto;

import com.wsdy.saasops.common.utils.StringUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Setter
@Getter
public class SysPushDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "secret")
    private String secret = StringUtils.EMPTY;

    @ApiModelProperty(value = "pushKey")
    private String pushKey = StringUtils.EMPTY;

    @ApiModelProperty(value = "type")
    private Integer type;

    @ApiModelProperty(value = "创建时间")
    private  String  createTime;

    @ApiModelProperty(value = "创建者")
    private  String  creator;

    @ApiModelProperty(value = "更新时间")
    private  String  updateTime;

    @ApiModelProperty(value = "更新者")
    private String updater;
}
