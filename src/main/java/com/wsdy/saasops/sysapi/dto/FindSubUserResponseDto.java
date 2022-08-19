package com.wsdy.saasops.sysapi.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FindSubUserResponseDto {

    /**
     * 子用户列表
     */
    private List<SubUserDto> subUserDtos;


    /**
     * 代码
     */
    private Integer code;

    /**
     * 总记录数
     */
    private long totalCount;
    /**
     *  每页记录数
     */

    private int pageSize;

    /**
     * 总页
     */
    private int totalPage;

    /**
     * 当前页
     */
    private int currPage;
}
