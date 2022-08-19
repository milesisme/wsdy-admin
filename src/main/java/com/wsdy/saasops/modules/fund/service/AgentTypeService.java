package com.wsdy.saasops.modules.fund.service;

import com.wsdy.saasops.modules.agent.dto.AgentTypeDto;
import com.wsdy.saasops.modules.agent.mapper.AgentMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AgentTypeService {

    @Autowired
    private AgentMapper agentMapper;


    public String  checkAgentType(String agentIds){
        if(agentIds == null || agentIds.length() <= 0){
            return agentIds;
        }
       String[] arrayAgentIds  = agentIds.split(",");
       List<Integer> list = new ArrayList<>();
       for(String agentId : arrayAgentIds){
           list.add(Integer.valueOf(agentId));
       }
       List<Integer> newList = checkAgentType(list);
        return StringUtils.join(newList.toArray(), ",");
    }

    public List<Integer> checkAgentType(List<Integer> agentIdList){
        List<Integer> newAgentIdList = new ArrayList<>();
        if(agentIdList!= null && agentIdList.size() > 1){
            List<AgentTypeDto> agentTypeDtoList =  agentMapper.getAllAgentType();
            Map<Integer, AgentTypeDto> agentTypeDtoMap = new HashMap<>();
            Map<Integer, Integer> agentIdMap = new HashMap<>();

            for(AgentTypeDto agentTypeDto : agentTypeDtoList){
                agentTypeDtoMap.put(agentTypeDto.getAgentId(), agentTypeDto);
            }
            for(Integer agentId: agentIdList){
                agentIdMap.put(agentId, agentId);
            }
            for(Map.Entry<Integer, Integer> entry :agentIdMap.entrySet()){
                AgentTypeDto agentTypeDto = agentTypeDtoMap.get(entry.getValue());
                if(agentTypeDto.getAgentType() == 3){
                    if(!agentIdMap.containsKey(agentTypeDto.getParentId())){
                        newAgentIdList.add(entry.getValue());
                    }
                }else if(agentTypeDto.getAgentType() == 2){
                    newAgentIdList.add(entry.getValue());
                }
            }
        }else{
            return agentIdList;
        }
        return newAgentIdList;
    }

}
