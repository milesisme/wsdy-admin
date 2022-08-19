package com.wsdy.saasops.sysapi.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class StartExperienceResponseDto {

    /**
     * 上级用户
     */
    private String userName;
    /**
     * 子状态
     */
    public List<ApplySubUserNameDto> applySubUserNames;
}
