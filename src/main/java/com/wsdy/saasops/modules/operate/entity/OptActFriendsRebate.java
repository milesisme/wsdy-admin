package com.wsdy.saasops.modules.operate.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Setter
@Getter
@ApiModel(value = "OptActFriendsRebate", description = "OptActFriendsRebate")
@Table(name = "opr_act_friends_rebate")
public class OptActFriendsRebate  implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "会员ID")
    private Integer accountId;

    @ApiModelProperty(value = "首存时间")
    private String firstChargeTime;

    @ApiModelProperty(value = "首存金额")
    private BigDecimal firstChargeAmount;

    @ApiModelProperty(value = "累计存款")
    private BigDecimal totalDeposit;


    @ApiModelProperty(value = "有效投注")
    private BigDecimal validBet;

    @Transient
    @ApiModelProperty(value = "会员账号")
    private String loginName;


    @Transient
    @ApiModelProperty(value = "会员组id")
    private Integer groupId;



}
