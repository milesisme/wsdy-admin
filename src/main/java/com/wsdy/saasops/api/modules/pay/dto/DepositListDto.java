package com.wsdy.saasops.api.modules.pay.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepositListDto {

    @ApiModelProperty(value = "0 入款 （原线上入款） ,1 公司入款（停用），2 人工增加（原人工充值） 3全部 4返利增加（停用）")
    private Integer mark;

    @ApiModelProperty(value = "开始时间 yyyy-MM-dd HH:mm:ss")
    private String createTimeFrom;

    @ApiModelProperty(value = "结束时间 yyyy-MM-dd HH:mm:ss")
    private String createTimeTo;

    @ApiModelProperty(value = "会员ID(mbr_account）")
    @JsonIgnore
    private Integer accountId;

    @ApiModelProperty(value = "0 失败 1 成功 2待处理")
    private Integer status;

    private String orderBy;

    private String financialCode;
}
