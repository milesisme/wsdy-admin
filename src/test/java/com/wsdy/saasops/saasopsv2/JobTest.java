package com.wsdy.saasops.saasopsv2;

import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.agent.dto.AgentComReportDto;
import com.wsdy.saasops.modules.agent.service.AgentComReportService;
import com.wsdy.saasops.modules.agent.service.CommissionCastService;
import com.wsdy.saasops.modules.member.service.AccountAutoCastService;
import com.wsdy.saasops.modules.member.service.MbrFundsReportService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.wsdy.saasops.modules.member.service.MbrGroupAutoUpdateService;
import sun.management.Agent;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JobTest {

	
	@Autowired
	private MbrGroupAutoUpdateService mbrGroupAutoUpdateService;
	@Autowired
	private MbrFundsReportService mbrFundsReportService;
	@Autowired
	private AgentComReportService agentComReportService;
	@Autowired
	private CommissionCastService commissionCastService;
	@Autowired
	private AccountAutoCastService accountAutoCastService;

	
	@Test
	public void isCastAccountAuto() {
		accountAutoCastService.isCastAccountAuto("dev");
	}
	
	@Test
	public void calculateCommission() {
		commissionCastService.calculateCommission("dev", "02");
	}
	
	@Test
	public void mbrGroupAutoUpdate() {
		mbrGroupAutoUpdateService.mbrGroupAutoUpdate("haha", true);
		
	}

	@Test
	public void mbrFundsReportTest() {
		mbrFundsReportService.countDailyMbrFundsReport("fundsreport", "2021-11-29");

	}

	@Test
	public void agentReportTest() {
		AgentComReportDto agentComReportDto = new AgentComReportDto();
		agentComReportDto.setPageSize(10);
		agentComReportDto.setPageNo(1);
		agentComReportDto.setStartTime("2021-11-01 00:00:00");
		agentComReportDto.setEndTime("2021-11-16 00:00:00");
		ThreadLocalCache.setSiteCodeAsny("dcs");
		agentComReportService.totalListByDay(agentComReportDto, 1l);
	}
}
