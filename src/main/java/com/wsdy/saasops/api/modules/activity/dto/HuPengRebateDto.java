package com.wsdy.saasops.api.modules.activity.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class HuPengRebateDto {

    @ApiModelProperty(value = "参与最小VIP等级")
    private Integer minVipLevel;

    @ApiModelProperty(value = "参与最大VIP等级")
    private Integer maxVipLevel;

    @ApiModelProperty(value = "活动开始时间，发奖开始时间")
    private String startTime;

    @ApiModelProperty(value = "活动结束时间， 发奖结束时间")
    private String endTime;

    @ApiModelProperty(value = "返佣等级配置")
    private List<HuPengLevelRewardDto>  huPengLevelRewardDtoList;


    @JsonIgnore
    @ApiModelProperty(value = "返佣等级配置")
    private Map<Integer, HuPengLevelRewardDto> huPengLevelRewardDtoMap;



    public void toMap(){
        huPengLevelRewardDtoMap = new HashMap<>();
        if(huPengLevelRewardDtoList!=null){
            for (int i = 0; i < huPengLevelRewardDtoList.size(); i++){
                HuPengLevelRewardDto huPengLevelRewardDto = huPengLevelRewardDtoList.get(i);
                huPengLevelRewardDtoMap.put(huPengLevelRewardDto.getLevel(), huPengLevelRewardDto);
            }
        }
    }


}
