package com.wsdy.saasops.modules.fund.dto;

import com.wsdy.saasops.api.modules.pay.dto.DPaySearchResponseDto;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class CommonDPayQueryRespDto {

    private Integer code;
    private String msg;
    private DPaySearchResponseDto data;
}
