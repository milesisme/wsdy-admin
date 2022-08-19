package com.wsdy.saasops.saasopsv2;

import com.wsdy.saasops.modules.log.entity.LogMbrLogin;
import com.wsdy.saasops.modules.log.mapper.LogMapper;
import com.wsdy.saasops.modules.member.service.IpService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Objects;

@RunWith(SpringRunner.class)
@SpringBootTest
public class V2Tests {
    @Autowired
    private LogMapper logMapper;

    @Test
    public void modifyIpArea() {
        List<LogMbrLogin> list =  logMapper.queryLoginAreaIsNull();
        IpService ipService = new IpService();
        if(Objects.nonNull(list)){
            for(LogMbrLogin l: list){
                String loginarea = ipService.getIpArea(l.getLoginIp());
                System.out.println("update log_mbrlogin set loginarea = '" + loginarea + "' where id = " + l.getId() +";");
            }
        }
    }
}
