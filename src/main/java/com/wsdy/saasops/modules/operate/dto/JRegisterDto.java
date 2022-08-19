package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel(value = "RegisterDto", description = "注册送")
public class JRegisterDto {

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

    @ApiModelProperty(value = "流水范围")
    private List<AuditCat> auditCats;

    @ApiModelProperty(value = "活动规则")
    private RegisterRuleDto ruleDto;

    @ApiModelProperty(value = "注册开始时间")
    private String registerStartTime;

    @ApiModelProperty(value = "注册结束时间")
    private String registerEndTime;
}
