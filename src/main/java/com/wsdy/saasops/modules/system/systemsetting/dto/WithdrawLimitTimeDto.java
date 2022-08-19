package com.wsdy.saasops.modules.system.systemsetting.dto;


import lombok.Getter;
import lombok.Setter;



@Setter
@Getter
public class WithdrawLimitTimeDto {

    private String startTime;


    private String endTime;

    @Override
    public String toString() {
        return "{" +
                "startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                '}';
    }
}
