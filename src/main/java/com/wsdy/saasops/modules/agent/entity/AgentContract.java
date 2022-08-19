package com.wsdy.saasops.modules.agent.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Setter
@Getter
@Table(name = "agy_contract")
public class AgentContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "契约名称")
    private String contractName;

    @ApiModelProperty(value = "佣金上限")
    private BigDecimal commissionCap;

    @ApiModelProperty(value = "周期内累计投注额")
    private BigDecimal validbetmax;

    @ApiModelProperty(value = "规则字符串")
    private String rule;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "最后一次修改人的账号")
    private String modifyUser;

    @ApiModelProperty(value = "最后一次修改时间")
    private String modifyTime;

    @Transient
    @ApiModelProperty(value = "签约人数")
    private Integer contractCount;

    @Transient
    @ApiModelProperty(value = "活跃用户设置")
    private String validbetmaxStr;
}