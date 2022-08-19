package com.wsdy.saasops.saasopsv2;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.wsdy.saasops.aff.dto.AccountAgentDto;
import com.wsdy.saasops.aff.service.SdyDataService;
import com.wsdy.saasops.agapi.modules.dto.*;
import com.wsdy.saasops.agapi.modules.mapper.TeamMapper;
import com.wsdy.saasops.agapi.modules.service.*;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.entity.*;
import com.wsdy.saasops.modules.agent.mapper.AgentMapper;
import com.wsdy.saasops.modules.agent.service.*;
import com.wsdy.saasops.modules.analysis.entity.GameReportQueryModel;
import com.wsdy.saasops.modules.analysis.service.AnalysisService;
import com.wsdy.saasops.modules.member.entity.MbrTree;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AgenrTests {

    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private SdyDataService sdyDataService;

    @Autowired
    private AgentReportService reportService;
    @Autowired
    private AgentNewService agentNewService;

    @Autowired
    private CommReportService commReportService;
    @Autowired
    private AgentAuditService agentAuditService;
    @Autowired
    private AgentSafeyInfoService safeyInfoService;
    @Autowired
    private AgentTeamService teamService;
    @Autowired
    private AgentNewService agentService;
    @Autowired
    private FinancialCenterService financialCenterService;
    @Autowired
    private CommissionCastService commissionCastService;
    @Autowired
    private AnalysisService analysisService;
    @Autowired
    private AgentReportService agentReportService;
    @Autowired
    private AgentWaterCostService waterCostService;

    @Test
    public void findCostReportViewAgent() {
        waterCostService.findCostReportViewAgent(null,1,10);
    }


    @Test
    public void upperScoreRecord() {
        agentReportService.upperScoreRecord(new AgyBillDetail(),
                1, 10);
    }

    @Test
    public void directMember() {
        DirectMemberParamDto paramDto = new DirectMemberParamDto();
        paramDto.setStartTime("2021-07-01 00:00:00");
        paramDto.setEndTime("2021-07-22 23:59:59");
        paramDto.setLoginName("liuzq1245");
        AgentAccount account = agentAccountMapper.selectByPrimaryKey(4248);
        PageUtils  utils = teamService.directMember(account, paramDto, 1, 10);
        System.out.println(JSON.toJSONString(utils));

    }

    @Test
    public void rechargeWalletFlow() {
        WalletFlowParamDto paramDto = new WalletFlowParamDto();
        paramDto.setStartTime("2021-03-01 00:00:00");
        paramDto.setEndTime("2021-03-29 59:59:59");

        AgentAccount account = agentAccountMapper.selectByPrimaryKey(710);
        financialCenterService.rechargeWalletFlow(paramDto, account, 1, 10);
    }

    @Test
    public void getAgentBkRptBetListPage() {
        GameReportQueryModel model = new GameReportQueryModel();
        model.setAgentid(727);
        model.setIsSdyNet(false);
        model.setIsSubtotal(true);
        model.setPayOutStrTime("2021-03-15 00:00:00");
        model.setPayOutEndTime("2021-03-24 59:59:59");
        model.setSiteCode("dcs");
        analysisService.getAgentBkRptBetListPage(1, 10, model);
    }


    @Test
    public void superiorCloneList() {
        DirectMemberParamDto paramDto = new DirectMemberParamDto();
        paramDto.setCagencyId(727);
        teamService.superiorCloneList(paramDto, 1, 10);
    }

    @Test
    public void agentAccountLogin() {
        AgentAccount account = new AgentAccount();
        account.setAgyAccount("tesfafasd");
        account.setAgyPwd("123456");
        agentService.agentAccountLogin(account);
    }

    @Test
    public void rechargeTransfer() {
        AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(710);
        RechargeTransferParamDto paramDto = new RechargeTransferParamDto();
        paramDto.setAmount(new BigDecimal(111));
        paramDto.setLoginName("tests3ss");
        financialCenterService.rechargeTransfer(paramDto, agentAccount);
    }


    @Test
    public void dataTrendList() {
        AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(715);
        DataTrendParamDto dto = new DataTrendParamDto();
        dto.setTime("2021-03");
        dto.setType(4);
        agentService.dataTrendList(agentAccount, dto);
    }


    @Test
    public void auditSave() {
        AgentAudit agentAudit = new AgentAudit();
        agentAudit.setAgyAccount("wwxt01");
        agentAudit.setAmount(new BigDecimal(22));
        agentAudit.setWalletType(1);
        agentAudit.setFinancialCode("GA");
        agentAuditService.auditSave(agentAudit);
    }

    @Test
    public void test2333() {
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setAgyAccount("wwxt02");
        AgentAccount agentAccount1 = agentAccountMapper.selectOne(agentAccount);
        commReportService.finCommission(agentAccount1, 0);
    }

    @Test
    public void test2() {
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setAgyAccount("wwxt02");
        AgentAccount agentAccount1 = agentAccountMapper.selectOne(agentAccount);
        agentNewService.agent0verview("2021-11-01","2021-12-01",null);
    }

    @Test
    public void test1() {
        AgyBillDetail billDetail = new AgyBillDetail();
        reportService.upperScoreRecord(null, 1, 10);
    }

    @Test
    public void test() {

        sdyDataService.findAccountBet(null,1,10);
       /* AccountAgentDto dto = new AccountAgentDto();
        dto.setMembercode("yd0002");
        dto.setParentmembercode("ksyd0001");
        sdyDataService.updateAgentSuperior(dto);*/
        /*List<AgyTree> mbrTreeList = agentMapper.findAgyTreeByparentId(626);

        List<AgyTree> agyTrees = Lists.newArrayList();
        for (AgyTree tree : mbrTreeList) {
            AgyTree agyTree = new AgyTree();
            if (tree.getChildNodeId().equals(626)) {
                agyTree.setParentId(625);
                agyTree.setChildNodeId(626);
            } else {
                AgyTree agyTree1 = agentMapper.findAgyParentId(tree.getChildNodeId());
                agyTree.setParentId(agyTree1.getParentId());
                agyTree.setChildNodeId(tree.getChildNodeId());
            }
            agyTrees.add(agyTree);
        }
        for (AgyTree agyTree : agyTrees) {
            if (agyTree.getChildNodeId().equals(626)) {
                agentMapper.updateParentId(agyTree.getParentId(), agyTree.getChildNodeId());
            }
            agentMapper.deleteAgyTreeByparentId(agyTree.getChildNodeId());
            agentMapper.addAgentNode(agyTree.getParentId(), agyTree.getChildNodeId());

            AgentAccount agentAccount1 = new AgentAccount();
            agentAccount1.setId(agyTree.getChildNodeId());
            AgentAccount tAgent = agentMapper.getTagentByCagent(agentAccount1);

            agentMapper.updateAccountTagencyId(tAgent.getId(), agyTree.getChildNodeId());
        }*/
    }
}
