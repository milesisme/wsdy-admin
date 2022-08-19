package com.wsdy.saasops.modules.system.pay.entity;

import com.wsdy.saasops.modules.base.entity.BaseBank;
import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;


@Setter
@Getter
@ApiModel(value = "TPay", description = "支付平台")
@Table(name = "t_pay")
public class TPay implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "是否启用 1启用 0禁言")
    private Integer available;

    @ApiModelProperty(value = "平台名称(XX支付)")
    private String platfromName;

    @ApiModelProperty(value = "支付平台CODE 自定义")
    private String platfromCode;

    @ApiModelProperty(value = "支持终端 1：pc ，2：移动，3：全部")
    private Integer terminalType;

    @ApiModelProperty(value = "支付所属:1 QQ 2微信 3京东 4网银 5支付宝 6同略云 7快捷支付 8银联扫码 9风云聚合 10BTP 11银行卡跳转 12个人二维码 13LBT 14卡转卡 15极速存取款")
    private Integer paymentType;

    @Transient
    @ApiModelProperty(value = "移动端logo")
    private String mBankLog;

    @ApiModelProperty(value = "支付域名")
    private String payUrl;

    @ApiModelProperty(value = "回调URL")
    private String callbackUrl;

    @ApiModelProperty(value = "是否二维码 1是 0否")
    private Integer urlMethod;

    @ApiModelProperty(value = "支付code")
    private String code;

    @ApiModelProperty(value = "是否删除 逻辑删除 0否 1是")
    private Integer isDelete;

    @ApiModelProperty(value = "")
    private String modifyUser;

    @ApiModelProperty(value = "")
    private String modifyTime;

    @ApiModelProperty(value = "支付名称：微信")
    private String payName;

    @Transient
    @ApiModelProperty(value = "支持银行")
    private List<BaseBank> baseBanks;
}