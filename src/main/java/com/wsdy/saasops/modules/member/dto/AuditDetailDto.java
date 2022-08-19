package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
@ApiModel(value = "即时稽核明细")
public class AuditDetailDto {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "稽核类型 0存款稽核 1优惠稽核, 2 好友推荐")
    private Integer auditType;

    @ApiModelProperty(value = "时间")
    private String time;

    @ApiModelProperty(value = "存款金额")
    private BigDecimal depositAmount;

    @ApiModelProperty(value = "优惠金额")
    private BigDecimal discountAmount;

    @ApiModelProperty(value = "好友返利金额")
    private BigDecimal friendRebateAmount;

    @ApiModelProperty(value = "流水要求")
    private BigDecimal auditAmount;

    @ApiModelProperty(value = "有效投注")
    private BigDecimal validBet;

    @ApiModelProperty(value = "溢出投注")
    private BigDecimal remainValidBet;

    @ApiModelProperty(value = "派彩")
    private BigDecimal payOut;

    @ApiModelProperty(value = "0未通过,1通过 2输光")
    private Integer status;

    @ApiModelProperty(value = "是否已经人工处理 0否 1是")
    private Integer isDispose;

    @ApiModelProperty(value = "投注额是否有效0无效，违规 1有效 没有")
    private Integer isValid;

    @ApiModelProperty(value = "转账使用存款 方便显示")
    private BigDecimal discardAmount;

    @ApiModelProperty(value = "活动名称")
    private String activityName;

    @ApiModelProperty(value = "平台名称")
    private String depotName;

    @ApiModelProperty(value = "分类名称")
    private String catName;

    @ApiModelProperty(value = "是输光 0否 1是",hidden = true)
    private Integer isOut;

    @ApiModelProperty(value = "人工处理类型 1正常 2增加稽核 3清除稽核 4扣款")
    private Integer disposeType;

    @ApiModelProperty(value = "disposeAmout")
    private BigDecimal disposeAmout;

    @ApiModelProperty(value = "通过时间")
    private String passTime;

    private BigDecimal reduceAuditAmount;
}
