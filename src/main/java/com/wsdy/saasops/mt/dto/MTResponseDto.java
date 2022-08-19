package com.wsdy.saasops.mt.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@ApiModel(value = "MT响应信息", description = "MT响应信息")
public class MTResponseDto {

    private static final long serialVersionUID = 1L;

    public static final String ERROR_CODE_2000 = "500"; //500 系统异常
    public static final String ERROR_CODE_00 = "00"; //成功
    public static final String ERROR_CODE_11 = "11"; //手机号码不能为空
    public static final String ERROR_CODE_22 = "22"; //sitecode不能为空
    public static final String ERROR_CODE_33 = "33"; //上级代理未开通

    public static final String ERROR_CODE_44 = "44"; //账号不存在

    public static final String ERROR_CODE_55 = "55"; //账号已存在
    public static final String ERROR_CODE_66 = "66"; //手机号码格式不对
    @ApiModelProperty(value = "错误明细")
    private String msg;

    @ApiModelProperty(value = "数据信息")
    private Map<String, Object> data;

    @ApiModelProperty(value = "00 成功，11 会员不存在,22 sitecode 不能为空，33 会员名不能为空，2000 系统异常" +
            "44 交易号不能为空，55 操作类型不能为空，66 金额不能为空,77 平台CODE不能为空，88 类别代码不能为空")
    private String code = "00";


    public static MTResponseDto response(String code, String msg){
        MTResponseDto mtResponseDto = new MTResponseDto();
        mtResponseDto.setCode(code);
        mtResponseDto.setMsg(msg);
        mtResponseDto.setData(new HashMap<>());
        return mtResponseDto;
    }

    public static MTResponseDto response(String code, String msg, Map<String, Object> data){
        MTResponseDto mtResponseDto = new MTResponseDto();
        mtResponseDto.setCode(code);
        mtResponseDto.setMsg(msg);
        mtResponseDto.setData(data);
        return mtResponseDto;
    }


}
