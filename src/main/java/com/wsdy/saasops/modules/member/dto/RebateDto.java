package com.wsdy.saasops.modules.member.dto;


import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RebateDto {

    @ApiModelProperty(value ="是否稽核 0 否，1 是")
    private Integer auditType;

    @ApiModelProperty(value ="稽核倍数")
    private Integer auditMultiple;

    @ApiModelProperty(value = "参与最小VIP等级")
    private Integer minVipLevel;

    @ApiModelProperty(value = "参与最大VIP等级")
    private Integer maxVipLevel;

    @ApiModelProperty(value = "活动开始时间，发奖开始时间")
    private String startTime;

    @ApiModelProperty(value = "活动结束时间， 发奖结束时间")
    private String endTime;

    @ApiModelProperty(value ="会员层级配置")
    private List<RebateLevelDto> levelDtoList;

    @JSONField(serialize = false)
    private Map<Integer, RebateLevelDto>  levelDtoMap;

    public void toMap(){
        if(CollectionUtil.isNotEmpty(levelDtoList)){
            levelDtoMap = new HashMap<>();
            for(RebateLevelDto rebateLevelDto: levelDtoList){
                rebateLevelDto.toMap();
                levelDtoMap.put(rebateLevelDto.getLevel(), rebateLevelDto);
            }
        }
    }

}

