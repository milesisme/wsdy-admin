package com.wsdy.saasops.modules.member.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import javax.persistence.*;


@Setter
@Getter
@ApiModel(value = "MbrAuditBonus", description = "优惠稽核")
@Table(name = "mbr_audit_bonus")
public class MbrAuditBonus implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "领取优惠金额时间")
    private String time;

    //TODO 等会要把计算稽核哪里加上这个时间判断逻辑
    @ApiModelProperty(value = "人工把稽核变更正常时间")
    private String updateAuditTime;

    @ApiModelProperty(value = "有效投注额")
    private BigDecimal validBet;

    @ApiModelProperty(value = "输光余额")
    private BigDecimal outBalance;

    @ApiModelProperty(value = "优惠金额")
    private BigDecimal discountAmount;

    @ApiModelProperty(value = "优惠稽核流水要求")
    private BigDecimal auditAmount;

    @ApiModelProperty(value = "累计剩余有效投注额")
    private BigDecimal remainValidBet;

    @ApiModelProperty(value = "有效投注流水范围 0输 1赢 2全部 null 全部")
    private Integer scope;

    @ApiModelProperty(value = "会员登陆名称")
    private String loginName;

    @ApiModelProperty(value = "会员ID")
    private Integer accountId;

    @ApiModelProperty(value = "备注")
    private String memo;

    @ApiModelProperty(value = "")
    private String modifyUser;

    @ApiModelProperty(value = "")
    private String modifyTime;

    @ApiModelProperty(value = "优惠剩余余额 -派彩")
    private BigDecimal discountBalance;

    @ApiModelProperty(value = "存款金额即转入金额")
    private BigDecimal depositAmount;

    @ApiModelProperty(value = "活动ID")
    private Integer activityId;

    @ApiModelProperty(value = "稽核规则 预留")
    private String auditRule;

    @ApiModelProperty(value = "转账ID")
    private Integer billManageId;

    @ApiModelProperty(value = "平台ID号 平台号ID为0是本平台")
    private Integer depotId;

    @ApiModelProperty(value = "")
    private String depotName;

    @ApiModelProperty(value = "")
    private String depotCode;

    @ApiModelProperty(value = "分类ID")
    private Integer catId;

    @ApiModelProperty(value = "派彩")
    private BigDecimal payOut;

    @ApiModelProperty(value = "是否转出 0否 1是")
    private Integer isDrawings;

    @ApiModelProperty(value = "0未解锁,未通过,1解锁 通过")
    private Integer status;

    @ApiModelProperty(value = "投注额是否有效0无效，违规 1有效 没有")
    private Integer isValid;

    @ApiModelProperty(value = "是否已经人工处理违规过 0否 1是")
    private Integer isDispose;

    @ApiModelProperty(value = "是输光 0否 1是")
    private Integer isOut;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "产生交易记录order")
    private Long orderNo;

    @ApiModelProperty(value = "是否人工清除(弹窗里面的清除 0否 1是)")
    private Integer isClean;

    @ApiModelProperty(value = "优惠稽核变更历史时间")
    private String transferTime;

    @ApiModelProperty(value = "人工处理类型 1正常 2增加稽核 3清除稽核 4扣款")
    private Integer disposeType;

    @ApiModelProperty(value = "disposeAmout")
    private BigDecimal disposeAmout;

    @Transient
    @ApiModelProperty(value = "时间")
    private String auditTime;

    @Transient
    private Boolean sort;

    @Transient
    private String startTime;

    @Transient
    private String endTime;

    @Transient
    private List<MbrAuditFraud> auditFrauds;
}