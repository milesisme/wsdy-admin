package com.wsdy.saasops.agapi.modulesV2.service;


import com.wsdy.saasops.agapi.modulesV2.dto.AgentV2ListDto;
import com.wsdy.saasops.agapi.modulesV2.mapper.AgentV2Mapper;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.modules.agent.dao.AgentAccountOtherMapper;
import com.wsdy.saasops.modules.agent.dao.AgyWalletMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgentAccountOther;
import com.wsdy.saasops.modules.agent.entity.AgyTree;
import com.wsdy.saasops.modules.agent.entity.AgyWallet;
import com.wsdy.saasops.modules.agent.mapper.AgentMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountOtherMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrAccountOther;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;


@Slf4j
@Service
@Transactional
public class AgentV2AccountManageService {
    @Autowired
    private AgentV2Mapper agentAccountMapper;
    @Autowired
    private MbrAccountService accountService;
    @Autowired
    private AgyWalletMapper agyWalletMapper;
    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private AgentAccountOtherMapper agentAccountOtherMapper;
    @Autowired
    private MbrAccountOtherMapper mbrAccountOtherMapper;
    @Autowired
    private MbrAccountMapper mbrAccountMapper;
    @Autowired
    private MbrAccountService mbrAccountService;
    @Autowired
    private AgentV2AccountLogService agentV2AccountLogService;

    public PageUtils getSubAgentList(AgentAccount agentAccount, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<AgentV2ListDto> list = agentAccountMapper.getSubAgentList(agentAccount);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils getSubAccountList(AgentAccount agentAccount, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<AgentV2ListDto> list = agentAccountMapper.getSubAccountList(agentAccount);
        return BeanUtil.toPagedResult(list);
    }

    public void agyAccountSave(AgentAccount agentAccount,AgentAccount loginAccount) {
        // ????????????
        accountService.checkoutUsername(agentAccount.getAgyAccount());
        String salt = RandomStringUtils.randomAlphanumeric(20);
        agentAccount.setAgyPwd(new Sha256Hash(agentAccount.getAgyPwd(), salt).toHex());
        agentAccount.setSalt(salt);
        agentAccount.setAvailable(Constants.EVNumber.one);
        agentAccount.setParentId(isNull(agentAccount.getParentId())
                ? Constants.EVNumber.zero : agentAccount.getParentId());
        agentAccount.setStatus(Constants.EVNumber.one);
        agentAccount.setRegisterSign(Constants.EVNumber.two);
        agentAccountMapper.insert(agentAccount);

        // ????????????????????????
        AgentAccountOther aentAccountOther = new AgentAccountOther();
        aentAccountOther.setAgentId(agentAccount.getId());
        aentAccountOther.setAgyAccount(agentAccount.getAgyAccount());
        aentAccountOther.setBettingStatus(Constants.EVNumber.one);
        aentAccountOther.setElectronic(agentAccount.getElectronic());
        aentAccountOther.setElectronicwash(agentAccount.getElectronicwash());
        aentAccountOther.setRealpeople(agentAccount.getRealpeople());
        aentAccountOther.setRealpeoplewash(agentAccount.getRealpeoplewash());
        agentAccountOtherMapper.insert(aentAccountOther);

        // ????????????????????????
        addAgentWalletAndTree(agentAccount);

        // ??????????????????
        agentV2AccountLogService.agyAccountSave(agentAccount,loginAccount);
    }

    private void addAgentWalletAndTree(AgentAccount agentAccount) {
        AgyWallet agyWallet = new AgyWallet();
        agyWallet.setAccountId(agentAccount.getId());
        agyWallet.setAgyAccount(agentAccount.getAgyAccount());
        agyWallet.setBalance(BigDecimal.ZERO);
        agyWalletMapper.insert(agyWallet);

        agentMapper.addAgentNode(agentAccount.getParentId(), agentAccount.getId());
    }

    public void addMbrAccount(MbrAccount mbrAccount, AgentAccount agentAccount) {
        // ????????????
        mbrAccountService.adminSave(mbrAccount, agentAccount, null, null, Boolean.FALSE, Constants.EVNumber.five);
        // ????????????????????????
        MbrAccountOther mbrAccountOther = new MbrAccountOther();
        mbrAccountOther.setAccountId(mbrAccount.getId());
        mbrAccountOther.setLoginName(mbrAccount.getLoginName());
        mbrAccountOther.setBettingStatus(Constants.EVNumber.one);
        mbrAccountOther.setRealpeoplewash(mbrAccount.getRealpeoplewash());
        mbrAccountOther.setElectronicwash(mbrAccount.getElectronicwash());

        mbrAccountOtherMapper.insert(mbrAccountOther);

        // ??????????????????
        agentV2AccountLogService.addMbrAccount(mbrAccount,agentAccount);
    }

    public AgyTree getAgentByDepth(Integer childNodeId, Integer depth ){
        return agentAccountMapper.getAgentByDepth(childNodeId,depth);
    }

    public void updateAgentAvailable(AgentAccount agentAccount,  AgentAccount loginAccount) {
        // ???????????????
        AgentAccount oldAgyAccount = agentAccountMapper.selectByPrimaryKey(agentAccount.getId());
        agentAccountMapper.updateByPrimaryKeySelective(agentAccount);
        // ??????????????????
        agentV2AccountLogService.updateAgentAvailable(oldAgyAccount,agentAccount,loginAccount);
    }

    public void agyAccountUpdate(AgentAccount agentAccount,  AgentAccount loginAccount) {
        AgentAccount agyAccount = agentAccountMapper.selectByPrimaryKey(agentAccount.getId());
        // ????????????
        if(StringUtil.isNotEmpty(agentAccount.getAgyPwd()) && Objects.nonNull(agyAccount)){
            String salt = agyAccount.getSalt();
            agentAccount.setAgyPwd(new Sha256Hash(agentAccount.getAgyPwd(), salt).toHex());
        }
        // ????????????
        agentAccountMapper.updateByPrimaryKeySelective(agentAccount);

        // ????????????????????????
        AgentAccountOther agentOther = agentAccountMapper.selectByAgent(agentAccount);
        AgentAccountOther agentAccountOther = new AgentAccountOther();
        agentAccountOther.setId(agentOther.getId());
        agentAccountOther.setElectronic(agentAccount.getElectronic());
        agentAccountOther.setElectronicwash(agentAccount.getElectronicwash());
        agentAccountOther.setRealpeople(agentAccount.getRealpeople());
        agentAccountOther.setRealpeoplewash(agentAccount.getRealpeoplewash());
        agentAccountOtherMapper.updateByPrimaryKeySelective(agentAccountOther);

        // ??????????????????
        agentV2AccountLogService.agyAccountUpdate(agyAccount,agentOther,agentAccount,agentAccountOther,loginAccount);
    }

    public void updateMbrAccount(MbrAccount account,  AgentAccount loginAccount) {
        MbrAccount mbrAccount = mbrAccountMapper.selectByPrimaryKey(account.getId());
        // ????????????
        if(StringUtil.isNotEmpty(account.getLoginPwd()) && Objects.nonNull(mbrAccount)){
            String salt = mbrAccount.getSalt();
            account.setLoginPwd(new Sha256Hash(account.getLoginPwd(), salt).toHex());
        }
        // ????????????
        mbrAccountMapper.updateByPrimaryKeySelective(account);

        // ????????????????????????
        MbrAccountOther mbrOther = agentAccountMapper.selectByMbr(account);
        MbrAccountOther mbrAccountOther = new MbrAccountOther();
        mbrAccountOther.setId(mbrOther.getId());
        mbrAccountOther.setRealpeoplewash(account.getRealpeoplewash());
        mbrAccountOther.setElectronicwash(account.getElectronicwash());

        mbrAccountOtherMapper.updateByPrimaryKeySelective(mbrAccountOther);

        // ??????????????????
        agentV2AccountLogService.updateMbrAccount(mbrAccount,mbrOther,account,mbrAccountOther,loginAccount);
    }

    public void updateMbrAvailable(MbrAccount account,AgentAccount loginAccount) {
        // ???????????????
        MbrAccount oldAgyAccount = mbrAccountMapper.selectByPrimaryKey(account.getId());
        mbrAccountMapper.updateByPrimaryKeySelective(account);
        // ??????????????????
        agentV2AccountLogService.updateMbrAvailable(oldAgyAccount,account,loginAccount);
    }

    public void updateMbrBettingStatus(MbrAccountOther account, AgentAccount loginAccount) {
        // ???????????????
        MbrAccountOther oldAgyAccount = new MbrAccountOther();
        oldAgyAccount.setAccountId(account.getId());
        oldAgyAccount = mbrAccountOtherMapper.selectOne(oldAgyAccount);
        agentAccountMapper.updateMbrBettingStatus(account);

        // ??????????????????
        agentV2AccountLogService.updateMbrBettingStatus(oldAgyAccount,account,loginAccount);

    }
    public void updateAgentBettingStatus(AgentAccount agentAccount,AgentAccount loginAccount ) {
        // ?????????????????????
        List<AgentAccountOther>  agyList = agentAccountMapper.getAgentBettingStatusList(agentAccount);
        List<MbrAccountOther>  mbrList = new ArrayList<>();

        // ??????????????????
        agentAccountMapper.updateAgentBettingStatus(agentAccount);
        // ?????????????????? ???????????????????????????????????? ???????????????????????????????????????????????????????????????
        if(agentAccount.getBettingStatus().equals(Constants.EVNumber.zero)){
            // ?????????????????????
            mbrList = agentAccountMapper.getMbrBettingStatusByAgentList(agentAccount);
            // ??????
            agentAccountMapper.updateMbrBettingStatusByAgent(agentAccount);
        }

        // ??????????????????
        agentV2AccountLogService.updateAgentBettingStatus(agyList,mbrList, agentAccount,loginAccount);
    }
}
