package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@Setter
@Getter
@ApiModel(value = "mbr_deposit_count", description = "会员待处理存款次数统计报")
@Table(name = "mbr_deposit_count")
@ToString
public class MbrDepositCount implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "会员id")
    private Integer accountId;
    @ApiModelProperty(value = "统计时间")
    private String startDay;
    @ApiModelProperty(value = "天次数")
    private Integer num;

    @Transient
    @ApiModelProperty(value = "是否更新存款锁定状态 true 修改为锁定状态")
    private Boolean isUpdateDepositLock;

    // 查询返回
    @Transient
    @ApiModelProperty(value = "存款锁定状态  0正常 1冻结")
    private Integer depositLock;
    @Transient
    @ApiModelProperty(value = "是否存在成功存款")
    private Boolean isSuccessDeposit;

    @Transient
    @ApiModelProperty(value = "剩余天次数")
    private Integer numRest;

    @Transient
    @ApiModelProperty(value = "是否出现客服提醒  true是 false否")
    private Boolean isReminder ;
}