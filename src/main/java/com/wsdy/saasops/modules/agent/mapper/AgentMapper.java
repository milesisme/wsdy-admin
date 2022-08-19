package com.wsdy.saasops.modules.agent.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.wsdy.saasops.modules.agent.dto.AgentTree;
import com.wsdy.saasops.modules.agent.dto.AgentTypeDto;
import com.wsdy.saasops.modules.agent.dto.ParentAgentDto;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgyBankcard;
import com.wsdy.saasops.modules.agent.entity.AgyDomain;
import com.wsdy.saasops.modules.agent.entity.AgyTree;
import com.wsdy.saasops.modules.agent.entity.AgyWallet;
import com.wsdy.saasops.modules.agent.entity.AgyWithdraw;
import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.fund.entity.FundAudit;
import com.wsdy.saasops.modules.operate.dto.AgyAccDto;

@Mapper
public interface AgentMapper extends MyMapper<AgentAccount> {

    List<AgentAccount> findAgyAccountListPage(AgentAccount agentAccount);

    int addAgentNode(
            @Param("parentId") Integer parentId,
            @Param("childNodeId") Integer childNodeId);

    int removeSubAgyTree(@Param("parentId") Integer parentId);

    List<AgentAccount> findAccountList(AgentAccount agentAccount);
    List<AgentAccount> findTopAccountLike(AgentAccount agentAccount);

    List<Map<String, Object>> queryAgyCountNoUrl();

    List<AgentAccount> getAgentAccountAuth(AgentAccount agentAccount);

    List<AgentAccount> queryAgentList(AgyAccDto agyAccDto);

    List<AgentTree> selectAgentTree();

    List<AgentAccount> findAllSubAgency();

    List<AgentAccount> findAllSubAgencyIncludeDisable();

    List<AgentAccount> findSubAgencyByName(@Param("agyAccountId") Integer agyAccountId);

    AgentAccount findSubAgency(String agyAccount);

    int findParentIdByChildNodeId(@Param("childNodeId") Integer childNodeId);

    int updateAddAgyWallet(AgyWallet agyWallet);

    int updateReduceAgyWallet(AgyWallet agyWallet);

    int updateAddRechargeWallet(AgyWallet agyWallet);
    int updateAddPayoffWallet(AgyWallet agyWallet);
    int updateReduceRechargeWallet(AgyWallet agyWallet);
    int updateReducePayoffWallet(AgyWallet agyWallet);


    List<AgentAccount> findAgyAccountAndGrade(AgentAccount account);

    List<AgyWithdraw> findWithdrawList(AgyWithdraw withdraw);

    AgyWithdraw findWithdrawById(AgyWithdraw withdraw);

    int findWithdrawCount(AgyWithdraw withdraw);

    BigDecimal sumWithdraw(@Param("passTime") String passTime);

    int findBankExists(AgyBankcard agyBankcard);

    List<AgyDomain> queryAllDomains();

    List<AgyDomain> findAgyDomainListPage(AgyDomain agyDomain);

    int selectCountByDomainUrl(@Param("domainUrl") String domainUrl);

    String findAgyDomain(@Param("status") Integer status);

    String findAgyCommitDomain();

    int findAgyCommitDomainCount(@Param("domainurl") String domainurl);

    AgentAccount getAgentAndMemberCount(@Param("id") Integer id, @Param("available") Integer available);

    List<AgentAccount> getAgent(AgentAccount agentAccount);

    List<FundAudit> accountAuditDetail(
            @Param("loginName") String loginName,
            @Param("auditTime") String auditTime);

    List<AgentAccount> findAgentByloginName(
            @Param("loginName") String loginName);

    List<AgentAccount> findAgentByAgyaccount(
            @Param("agyAccount") String agyAccount);

    List<AgentAccount> getAgentBanner(AgentAccount account);

    List<AgentAccount> getMbrBanner(AgentAccount account);

    AgentAccount getTagentByCagent(AgentAccount account);

    List<AgyTree> findAgyTreeByparentId(
            @Param("parentid") Integer parentid);

    AgyTree findAgyParentId(
            @Param("childnodeid") Integer childnodeid);

    int deleteAgyTreeByparentId(
            @Param("parentid") Integer parentid);

    int updateParentId(
            @Param("parentid") Integer parentid,
            @Param("id") Integer id);

    int updateAccountTagencyId(
            @Param("tagencyId") Integer tagencyId,
            @Param("cagencyId") Integer cagencyId);

    int modifyAgentCateGory(AgentAccount account);

    List<String> domainList(
            @Param("siteCode") String siteCode);
    List<String> domainSubList(
            @Param("siteCode") String siteCode);

    int isTagencyid(
            @Param("id") Integer id);

    int isParent(
            @Param("childNodeId") Integer childNodeId, @Param("parentId") Integer parentId);

    AgentAccount getAgentByAccount(String agyAccount);

    AgentAccount getAgentAccountById(@Param("id") Integer id);

	/**
	 * 
	 * 查询所有总代
	 * @param agentAccount
	 * @return
	 */
	List<AgentAccount> findGeneralAgent();


    List<AgentAccount> findSubAgent(@Param("parentId")Integer parentId);

    List<AgentTypeDto> getAllAgentType();
    
    List<Integer> getAllLowerLevel(@Param("agyAccount") String agyAccount);


    List<ParentAgentDto> findParentAgent(@Param("agtAccounts")List<String> agtAccounts);

    int updateAgentRate(Integer agentId, Integer upRate);
}
