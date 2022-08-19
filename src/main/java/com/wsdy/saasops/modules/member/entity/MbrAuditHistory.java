package com.wsdy.saasops.modules.member.entity;

import com.wsdy.saasops.modules.member.dto.AuditInfoDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;


@Setter
@Getter
@ApiModel(value = "MbrAuditHistory", description = "历史稽核")
@Table(name = "mbr_audit_history")
public class MbrAuditHistory implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @ApiModelProperty(value = "结束时间")
    private String endTime;

    @ApiModelProperty(value = "会员名字")
    private String loginName;

    @ApiModelProperty(value = "会员ID")
    private Integer accountId;

    @ApiModelProperty(value = "0 没有同步提款时间 1同步提款时间", hidden = true)
    private Integer isSign;

    @Transient
    @ApiModelProperty(value = "历史稽核明细")
    private AuditInfoDto auditInfoDto;
}