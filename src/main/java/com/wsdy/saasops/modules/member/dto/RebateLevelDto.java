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
public class RebateLevelDto {

    @ApiModelProperty(value = "会员等级")
    private Integer level;

    @ApiModelProperty(value = "首充返利规则")
    private RebateFirstChargeDto rebateFirstChargeDto;

    @ApiModelProperty(value = "VIP等级奖励")
    private List<RebateVipDto>  rebateVipDtoList;

    @ApiModelProperty(value = "游戏类别规则")
    private List<RebateCatDto> catDtoList;

    @ApiModelProperty(value = "好友充值奖励")
    private List<RebateChargeDto> rebateChargeDtoList;

    @JSONField(serialize = false)
    private Map<Integer, RebateVipDto> rebateVipDtoMap;

    @JSONField(serialize = false)
    private Map<Integer, RebateCatDto> catDtoMap;

    @JSONField(serialize = false)
    private Map<Integer, RebateChargeDto> rebateChargeDtoMap;

    public void toMap(){
            if(CollectionUtil.isNotEmpty(rebateVipDtoList)){
                rebateVipDtoMap = new HashMap<>();
                for (RebateVipDto rebateVipDto : rebateVipDtoList) {
                    rebateVipDtoMap.put(rebateVipDto.getLevel(), rebateVipDto);
                }
            }
            if(CollectionUtil.isNotEmpty(catDtoList)){
                catDtoMap = new HashMap<>();
                for (RebateCatDto rebateCatDto: catDtoList){
                    catDtoMap.put(rebateCatDto.getCatId(), rebateCatDto);
                }
            }

            if(CollectionUtil.isNotEmpty(rebateChargeDtoList)){
                rebateChargeDtoMap = new HashMap<>();
                for (RebateChargeDto rebateChargeDto: rebateChargeDtoList){
                    rebateChargeDto.setName(rebateChargeDto.getMinCharge().toString());
                    rebateChargeDtoMap.put(rebateChargeDto.getNum(), rebateChargeDto);
                }
            }
    }
}
