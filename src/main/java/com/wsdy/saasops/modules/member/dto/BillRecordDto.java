package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BillRecordDto {

    @ApiModelProperty(value = "会员ID")
    private Integer accountId;

    @ApiModelProperty(value = "会员名")
    private String accountName;
    @ApiModelProperty(value = "好友转账记录Id 废弃")
    private Integer mbdId;
    @ApiModelProperty(value = "总代")
    private String parentAgyAccount;
    @ApiModelProperty(value = "代理")
    private String agyAccount;
    @ApiModelProperty(value = "总代ID")
    private String parentAgyAccountId;
    @ApiModelProperty(value = "代理ID")
    private String agyAccountId;
    @ApiModelProperty(value = "操作方式")
    private String type;
    @ApiModelProperty(value = "金额")
    private String amount;
    @ApiModelProperty(value = "主账户原额度")
    private String beforeBalance;
    @ApiModelProperty(value = "主账户现额度")
    private String afterBalance;
    @ApiModelProperty(value = "客户端")
    private String devSource;
    @ApiModelProperty(value = "操作时间")
    private String time;
    @ApiModelProperty(value = "开始时间")
    private String startTime;
    @ApiModelProperty(value = "结束时间")
    private String endTime;
    @ApiModelProperty(value = "操作类型，0 支出1 收入")
    private String opType;
    @ApiModelProperty(value = "操作类型 提款--0  入款--1 转出--2 转入--3 人工增加--4 人工减少--5 红利--6 好友转账--8 全民代理--9 单一钱包--10 好友返利 11 反波胆下注 12 反波胆派彩 13 反波胆撤单 14 充值返上级 15")
    private String opTypeName;
    @ApiModelProperty(value = "订单号")
    private String orderNo;
    @ApiModelProperty(value = "订单号前缀")
    private String financialCode;
    @ApiModelProperty(value = "优惠金额")
    private String bonusAmount;
    @ApiModelProperty(value = "账户名")
    private String account;
    @ApiModelProperty(value = "转账手续费")
    private String handlingCharge;
    @ApiModelProperty(value = "实际金额")
    private String actualArrival;
    @ApiModelProperty(value = "审核人")
    private String auditUser;
    @ApiModelProperty(value = "审核时间")
    private String auditTime;
    @ApiModelProperty(value = "备注")
    private String memo;
    @ApiModelProperty(value = "真实姓名")
    private String realName;
    @ApiModelProperty(value = "银行账号")
    private String cardNo;
    @ApiModelProperty(value = "银行名称")
    private String bankName;
    @ApiModelProperty(value = "支行名称")
    private String address;
    @ApiModelProperty(value = "行政扣款")
    private String cutAmount;
    @ApiModelProperty(value = "扣除优惠")
    private String discountAmount;
    @ApiModelProperty(value = "0 免稽核 1存款稽核 2优惠稽核")
    private String auditType;
    @ApiModelProperty(value = "稽核倍数")
    private String auditMultiple;
    @ApiModelProperty(value = " 1 清除稽核点 0 不清除稽核点")
    private String isClear;
    @ApiModelProperty(value = "会员分组名")
    private String groupName;
    @ApiModelProperty(value = "0冻结,1成功,2失败")
    private String status;
    @ApiModelProperty(value = "平台原额度")
    private String depotBeforeBalance;
    @ApiModelProperty(value = "平台现额度")
    private String depotAfterBalance;
    @ApiModelProperty(value = "红利类别")
    private String tmplName;
    @ApiModelProperty(value = "总代 top 冗余字段")
    private Integer tagencyId;
    private String orderBy;
}