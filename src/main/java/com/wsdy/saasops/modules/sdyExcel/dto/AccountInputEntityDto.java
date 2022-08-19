package com.wsdy.saasops.modules.sdyExcel.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

@Data
public class AccountInputEntityDto {

    //会员名
    @Excel(name = "membercode")
    private String membercode;

    //id
    @Excel(name = "memberid")
    private String memberid;

    //姓名
    @Excel(name = "fullname")
    private String fullname;

    //email
    @Excel(name = "email")
    private String email;

    //电话
    @Excel(name = "contact")
    private String contact;

    //生日
    @Excel(name = "dob")
    private java.util.Date dob;

    //会员状态
    @Excel(name = "status")
    private String status;

    //密码
    @Excel(name = "pass")
    private String pass;

    //会员组
    @Excel(name = "groupname")
    private String groupname;

    //vip
    @Excel(name = "vip")
    private String vip;

    //余额
    @Excel(name = "balance")
    private String balance;

    //注册时间
    @Excel(name = "joined_date")
    private java.util.Date joined_date;

    //最后登录时间
    @Excel(name = "lastlogin_date")
    private java.util.Date lastlogin_date;

    //注册ip
    @Excel(name = "register_ip")
    private String register_ip;

    //推荐人 即代理
    @Excel(name = "parentmembercode")
    private String parentmembercode;

    //代理推广码
    @Excel(name = "affiliatecode")
    private String affiliatecode;

    //affilaitedomain affiliatedomain
    @Excel(name = "affiliatedomain")
    private String affilaitedomain;

    //affiliatestatus 代理线状态
    @Excel(name = "affiliatestatus")
    private String affiliatestatus;
}
