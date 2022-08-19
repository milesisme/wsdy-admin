package com.wsdy.saasops.modules.operate.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel(value = "JSignInDto", description = "签到")
public class JSignInDto {

    @ApiModelProperty(value = "活动范围")
    private ActivityScopeDto scopeDto;

    @ApiModelProperty(value = "是否审核 true是 false 否")
    private Boolean isAudit;

    @ApiModelProperty(value = "已填写真实姓名 true是 false 否")
    private Boolean isName;

    @ApiModelProperty(value = "已绑定银行卡 true是 false 否")
    private Boolean isBank;

    @ApiModelProperty(value = "已验证手机 true是 false 否")
    private Boolean isMobile;

    @ApiModelProperty(value = "已验证邮箱 true是 false 否")
    private Boolean isMail;

    @ApiModelProperty(value = "签到机制 0多次循环 1单次循环")
    private Integer signType;

    @ApiModelProperty(value = "签到基准 0充值金额 1有效投注")
    private Integer signBenchmark;

    @ApiModelProperty(value = "签到规则")
    private List<SignInRuleDto> ruleDtos;
}
