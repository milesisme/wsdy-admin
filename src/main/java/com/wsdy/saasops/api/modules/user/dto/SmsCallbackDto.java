package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(value = "SmsCallbackDto", description = "短信回调参数")
public class SmsCallbackDto {
    // 公共参数
    @ApiModelProperty(value = "回调操作码：dr 短信状态报告  mo 回复短信")
    private String op;
    @ApiModelProperty(value = "扩展子号")
    private String da;
    @ApiModelProperty(value = "手机号码")
    private String sa;
    @ApiModelProperty(value = "提交时间(格式：yyyyMMddHHmmss)")
    private String sd;

    // 状态回复单独的参数
    @ApiModelProperty(value = "错误码：0成功，其他错误")
    private Integer rp;
    @ApiModelProperty(value = "发送成功时返回的消息编号")
    private String id;
    @ApiModelProperty(value = "状态说明,成功时为：DELIVRD")
    private String su;
    @ApiModelProperty(value = "完成时间(格式：yyyyMMddHHmmss)")
    private String dd;
    @ApiModelProperty(value = "群发时号码所在的位置")
    private Integer di;

    // 回复时单独的参数
    @ApiModelProperty(value = "编码方式， 8:UCS2，15:GB2312")
    private Integer dc;
    @ApiModelProperty(value = "回复内容（16进制字符串），编码格式8:UCS2，15:GB2312")
    private String sm;
}
