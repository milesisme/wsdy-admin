package com.wsdy.saasops.sysapi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

@Data
@ApiModel(value = "登陆信息", description = "登陆信息")
public class TradeResponseDto {

    private static final long serialVersionUID = 1L;

    public static final String ERROR_CODE_2000 = "500"; //500 系统异常
    public static final String ERROR_CODE_00 = "00"; //成功
    public static final String ERROR_CODE_11 = "11"; //会员不存在
    public static final String ERROR_CODE_22 = "22"; //sitecode不能为空
    public static final String ERROR_CODE_33 = "33"; //会员名不能为空
    public static final String ERROR_CODE_44 = "44"; //交易号不能为空
    public static final String ERROR_CODE_55 = "55"; //操作类型不能为空
    public static final String ERROR_CODE_66 = "66"; //金额不正确
    public static final String ERROR_CODE_77 = "77"; //平台CODE不能为空
    public static final String ERROR_CODE_88 = "88"; //类别代码不能为空
    public static final String ERROR_CODE_99 = "99"; //正在处理，请勿频繁提交
    public static final String ERROR_CODE_98 = "98"; //余额不足，扣款失败
    public static final String ERROR_CODE_97 = "97"; //订单不存在
    public static final String ERROR_CODE_96 = "96"; //交易号已存在

    @ApiModelProperty(value = "错误明细")
    private String msg;

    @ApiModelProperty(value = "数据信息")
    private Map<String, Object> data;

    @ApiModelProperty(value = "00 成功，11 会员不存在,22 sitecode 不能为空，33 会员名不能为空，2000 系统异常" +
            "44 交易号不能为空，55 操作类型不能为空，66 金额不能为空,77 平台CODE不能为空，88 类别代码不能为空")
    private String code = "00";


}
