package com.wsdy.saasops.modules.member.dto;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MbrWarningQueryDto {

    /**
     * 开始时间
     */
    public String startTime;

    /**
     * 结束时间
     */
    public String endTime;


    /**
     * 页码
     */
    public Integer pageNo;

    /**
     * 页大小
     */
    public Integer pageSize;


    /**
     * 状态
     */
    public Integer status;  // 0待处理  1 已处理
}
