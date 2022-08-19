package com.wsdy.saasops.modules.sys.dto;


import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

@Data
public class SysUserExportDto {

    /**
     * 用户名
     */
    @Excel(name = "用户名", width = 20, orderNum = "1")
    private String userName;

    /**
     * 用户角色
     */
    @Excel(name = "用户角色", width = 20, orderNum = "2")
    private String userRole;

    @Excel(name = "真实姓名", width = 20, orderNum = "3")
    private String realName;

    @Excel(name = "是否启用", width = 20, orderNum = "4")
    private String available;
    
    @Excel(name = "最后登录时间", width = 20, orderNum = "4")
    private String lastLoginTime;

}
