package com.wsdy.saasops.saasopsv2;

import com.wsdy.saasops.agapi.modules.dto.DirectMemberParamDto;
import com.wsdy.saasops.agapi.modules.service.AgentTeamService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class N2Tests {

    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private AgentTeamService teamService;
    @Autowired
    private RedisService redisService;

    @Test
    public void test() {
       DirectMemberParamDto dto = new DirectMemberParamDto();
        AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(4248);
        dto.setSubAgentId(agentAccount.getId());
        dto.setStartTime("2021-07-01 00:00:00");
        dto.setEndTime("2021-07-31 23:59:59");
        teamService.subAgentList(dto,1,10);

      /*  redisService.findLikeRedis("auditAccount_*");
        Boolean isSiteExpired111 = redisService.setRedisExpiredTimeBo("auditAccount_goc9", "2323", 15, TimeUnit.MINUTES);
        Boolean isSiteExpired2222 = redisService.setRedisExpiredTimeBo("auditAccount_goc8888", "9999", 15, TimeUnit.MINUTES);

        redisService.findLikeRedis("auditAccount_*");*/
    }
}
