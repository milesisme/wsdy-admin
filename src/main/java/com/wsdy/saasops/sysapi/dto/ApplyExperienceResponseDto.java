package com.wsdy.saasops.sysapi.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ApplyExperienceResponseDto {

    /**
     * 父级
     */
   public String userName;


    /**
     * 子状态
     */
   public List<ApplySubUserNameDto> applySubUserNames;

    /**
     * 父级状态  0成功
     */
   public Integer userNameStatus;

}
