package com.wsdy.saasops.modules.system.pay.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Table;

@Setter
@Getter
@ApiModel(value = "SetBasicQrCodeGroup", description = "二维码与会员组关联")
@Table(name = "set_basic_qrcode_group")
public class SetBasicQrCodeGroup {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "个人二维码id")
    private Integer qrCodeId;

    @ApiModelProperty(value = "会员组")
    private Integer groupId;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "是否排队 0否 1是;不排队前端展示根据sort排序取第一个")
    private Integer isQueue;

}
