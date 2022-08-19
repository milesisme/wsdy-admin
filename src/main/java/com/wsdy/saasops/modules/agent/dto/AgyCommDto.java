package com.wsdy.saasops.modules.agent.dto;

import lombok.Data;

import java.util.List;

@Data
public class AgyCommDto {

    private List<CommissionDto> commissionDtos;

    private Integer accountId;
}
