package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel(value = "MemDayGiftDto", description = "会员日")
public class MemDayGiftDto {

    @ApiModelProperty(value = "流水范围")
    private List<AuditCat> auditCats;

    /**
     * 会员日
     */
    @ApiModelProperty(value = "会员日类型 0周 1月 2每月2次")
    private Integer memDayType;
    @ApiModelProperty(value = "会员日日期： 按周1-7 按日1-31 ,如1,5,7  周一周五周日 ")
    private List<Integer> validDates;
    /**
     * 申请条件
     */
    @ApiModelProperty(value = "已填写真实姓名 true是 false 否")
    private Boolean isName;
    @ApiModelProperty(value = "已绑定银行卡 true是 false 否")
    private Boolean isBank;
    @ApiModelProperty(value = "已验证手机 true是 false 否")
    private Boolean isMobile;

    /**
     * 会员范围/层级
     */
    @ApiModelProperty(value = "会员范围 0全部会员 1层级会员")
    private Integer scope;
    @ApiModelProperty(value = "层级 活动规则")
    private List<MemDayRuleScopeDto> ruleScopeDtos;

}
