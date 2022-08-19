package com.wsdy.saasops.saasopsv2;

import com.wsdy.saasops.agapi.modules.dto.DataTrendDto;
import com.wsdy.saasops.agapi.modules.dto.DataTrendParamDto;
import com.wsdy.saasops.agapi.modules.dto.ReportParamsDto;
import com.wsdy.saasops.agapi.modules.service.AgentFinaceReportService;
import com.wsdy.saasops.agapi.modules.service.AgentNewService;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.dto.AgentComReportDto;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.service.AgentComReportService;
import com.wsdy.saasops.modules.agent.service.CommissionCastService;
import com.wsdy.saasops.modules.agent.service.CommissionReportService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(false)
public class ComTests {

    @Autowired
    private CommissionReportService commissionReportService;
    @Autowired
    private CommissionCastService commissionCastService;
    @Autowired
    private AgentNewService agentNewService;
    @Autowired
    private AgentComReportService agentComReportService;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private AgentFinaceReportService reportService;


    @Test
    @Rollback(true)
    public void calculateCommission() {
        ReportParamsDto dto = new ReportParamsDto();
        dto.setStartTime("2021-08-04");
        dto.setEndTime("2021-08-04");
        dto.setCagencyId(2);
        reportService.agentFinanceReportList(dto,1,10);
    }

    @Test
    @Rollback(true)
    public void accountSignInfo() {
        AgentComReportDto model = new AgentComReportDto();
        model.setStartTime("2021-07-01 00:00:00");
        model.setEndTime("2021-07-31 23:59:59");
        model.setIsCagency(1);
        model.setAgyId(728);
        agentComReportService.subAgentTotalViewList(model);
    }


    @Test
    @Rollback(true)
    public void tagencyList() {
        AgentComReportDto model = new AgentComReportDto();
        model.setStartTime("2021-07-01 00:00:00");
        model.setEndTime("2021-07-31 23:59:59");
        model.setIsCagency(1);
        model.setAgyId(232);
        model.setPageNo(1);
        model.setPageSize(10);
        agentComReportService.tagencyList(model);
    }


    @Test
    @Rollback(true)
    public void agent0verview() {
        agentNewService.agent0verview("2021-11-01","2021-12-01",null);
    }

    @Test
    @Rollback(true)
    public void dataTrendList() {
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setId(1219);
        agentAccount.setAttributes(0);

        DataTrendParamDto dto = new DataTrendParamDto();
        dto.setType(1);
        dto.setTime("2021-06");
        List<DataTrendDto> dataTrendDtos = agentNewService.dataTrendList(agentAccount, dto);
        System.out.println(dataTrendDtos);
    }
}
