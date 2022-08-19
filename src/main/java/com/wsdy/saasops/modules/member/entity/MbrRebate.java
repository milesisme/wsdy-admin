package com.wsdy.saasops.modules.member.entity;

import com.wsdy.saasops.modules.member.dto.RebateCatDto;
import com.wsdy.saasops.modules.member.dto.RebateMbrDepthDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@ApiModel(value = "MbrRebate", description = "")
@Table(name = "mbr_rebate")
public class MbrRebate implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "比例")
    private String rebateText;

    @ApiModelProperty(value = "0 免稽核 1稽核")
    private Integer auditType;

    @ApiModelProperty(value = "稽核倍数")
    private Integer auditMultiple;

    @ApiModelProperty(value = "会员组id")
    private Integer groupId;

    @ApiModelProperty(value = "")
    private String createUser;

    @ApiModelProperty(value = "")
    private String createTime;

    @ApiModelProperty(value = "0 前端不显示台显示 1前端显示")
    private Integer isShow;

    @ApiModelProperty(value = "0 不计算 1计算")
    private Integer isCast;

    @Transient
    @ApiModelProperty(value = "比例")
    private List<RebateCatDto> rebateCatDtos;

    @Transient
    @ApiModelProperty(value = "比例")
    private List<RebateMbrDepthDto> rebateDepthDtos;

    @Transient
    private Integer accountId;

    @Transient
    private String loginName;
}