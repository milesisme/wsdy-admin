package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel(value = "JbirthdayDto", description = "生日礼金")
public class JbirthdayDto {

    @ApiModelProperty(value = "已填写真实姓名 true是 false 否")
    private Boolean isName;

    @ApiModelProperty(value = "已绑定银行卡 true是 false 否")
    private Boolean isBank;

    @ApiModelProperty(value = "已验证手机 true是 false 否")
    private Boolean isMobile;

    @ApiModelProperty(value = "已验证邮箱 true是 false 否")
    private Boolean isMail;

    @ApiModelProperty(value = "注册多少天不可以领取")
    private Integer day;

    @ApiModelProperty(value = "层级 活动规则")
    private List<JbirthdayInfoDto> ruleScopeDtos;

}
