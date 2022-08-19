package com.wsdy.saasops.modules.member.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MbrInfoExcelModel {

    @Excel(name = "会员名")
    private String loginName;
    @Excel(name = "真实姓名")
    private String realName;
    @Excel(name = "电话")
    private String mobile;
    @Excel(name = "QQ")
    private String qq;
    @Excel(name = "微信")
    private String weChat;
    @Excel(name = "代理")
    private String agyAccount;
    @Excel(name = "会员组")
    private String groupName;
    @Excel(name = "钱包余额")
    private String balance;
    @Excel(name = "注册时间")
    private String registerTime;
    @Excel(name = "注册IP")
    private String registerIp;
    @Excel(name = "最后登录时间")
    private String loginTime;
    @Excel(name = "登录IP")
    private String loginIp;
    @Excel(name = "状态")
    private String available;
    @Excel(name = "首存时间")
    private String depositTime;
    @Excel(name="推荐好友人数")
    private String referralsCount;
    @Excel(name = "线上入款次数")
    private String onlineDepositCount;
    @Excel(name = "线上入款金额")
    private String onlineDepositAmount;
    @Excel(name = "公司入款次数")
    private String conDepositCount;
    @Excel(name = "公司入款金额")
    private String conDepositAmount;
    @Excel(name = "取款次数")
    private String withdrawCount;
    @Excel(name = "取款金额")
    private String withdrawAmount;
    @Excel(name = "红利次数")
    private String bonusCount;
    @Excel(name = "红利金额")
    private String bonusAmount;
    @Excel(name = "人工调整次数")
    private String auditCount;
    @Excel(name = "人工调整金额")
    private String auditAmount;
    @Excel(name = "总投注额")
    private String betAmount;
    @Excel(name = "有效投注额")
    private String validbetAmount;
    @Excel(name = "总派彩")
    private String payoutAmount;
}
