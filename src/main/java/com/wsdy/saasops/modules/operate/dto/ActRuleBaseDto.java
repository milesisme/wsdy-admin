package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@ApiModel(value = "ActRuleBaseDto", description = "活动规则基础字段")
public class ActRuleBaseDto<T> {

    @ApiModelProperty(value = "流水范围")
    private List<AuditCat> auditCats;

    @ApiModelProperty(value = "已填写真实姓名 true是 false 否")
    private Boolean isName;

    @ApiModelProperty(value = "已绑定银行卡 true是 false 否")
    private Boolean isBank;

    @ApiModelProperty(value = "已验证手机 true是 false 否")
    private Boolean isMobile;

    @ApiModelProperty(value = "App注册 true是 false 否")
    private Boolean isApp;

    @ApiModelProperty(value = "可领取类型 0昨日 1上周 2上月")
    private Integer drawType;

    @ApiModelProperty(value = "活动规则明细")
    private List<T> ruleDtos;

}
