package com.wsdy.saasops.modules.fund.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;


@Setter
@Getter
@ApiModel(value = "FundMerchantPay", description = "代付")
@Table(name = "fund_merchant_pay")
public class FundMerchantPay implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @ApiModelProperty(value = "商户号")
    private String merchantNo;

    @ApiModelProperty(value = "商户KEY")
    private String merchantKey;

    @ApiModelProperty(value = "排序号")
    private Integer sort;

    @ApiModelProperty(value = "接口地址")
    private String url;

    @ApiModelProperty(value = "状态1开启，0禁用,")
    private Integer available;

    @ApiModelProperty(value = "")
    private String createUser;

    @ApiModelProperty(value = "")
    private String createTime;

    @ApiModelProperty(value = "")
    private String modifyUser;

    @ApiModelProperty(value = "")
    private String modifyTime;

    @ApiModelProperty(value = "渠道ID")
    private Integer channelId;

    @ApiModelProperty(value = "盘子收款账号 id")
    private String bankId;

    @ApiModelProperty(value = "设备来源：PC:0 H5:3")
    private String devSource;

    @Transient
    @ApiModelProperty(value = "适用会员组IDS")
    private List<Integer> ids;

    @Transient
    @ApiModelProperty(value = "状态1开启，0禁用(后台展示),2禁用(前端展示),3不符合会员组(前端展示)查询使用")
    private List<Integer> availables;

    // 加密货币新增
    @ApiModelProperty(value = "提现方式：0银行卡， 1加密货币钱包, 4C2C")
    private Integer methodType;
    @ApiModelProperty(value = "货币类型 USDT")
    private String currencyCode;
    @ApiModelProperty(value = "协议类型 ERC20 TRC20")
    private String currencyProtocol;
}