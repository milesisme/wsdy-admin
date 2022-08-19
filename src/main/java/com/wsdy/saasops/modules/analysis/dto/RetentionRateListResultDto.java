package com.wsdy.saasops.modules.analysis.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.models.auth.In;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class RetentionRateListResultDto {

    /**
     * 时间
     */
    private String time;


    /**
     * 首充数量
     */
    private Integer firstChargeTimeNum;

    /**
     * 留存数据
     */
    private List<RetentionRateResultDto>  retentionRateResultDtoList;

    /**
     * 留存玩家ID
     */
    @JsonIgnore
    private List<Integer>  retentionRateAccountIdList;

}
