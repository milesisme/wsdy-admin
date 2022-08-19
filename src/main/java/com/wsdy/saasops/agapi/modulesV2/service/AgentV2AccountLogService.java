package com.wsdy.saasops.agapi.modulesV2.service;

import com.wsdy.saasops.agapi.modulesV2.dao.AgyAccountLogMapper;
import com.wsdy.saasops.agapi.modulesV2.dto.AgentV2AccountLogDto;
import com.wsdy.saasops.agapi.modulesV2.entity.AgyAccountLog;
import com.wsdy.saasops.agapi.modulesV2.mapper.AgentV2AccountLogMapper;
import com.wsdy.saasops.agapi.modulesV2.mapper.AgentV2Mapper;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgentAccountOther;
import com.wsdy.saasops.modules.agent.entity.AgyWallet;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrAccountOther;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.wsdy.saasops.agapi.modulesV2.entity.AgyAccountLog.*;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

@Service
@Transactional
public class AgentV2AccountLogService extends BaseService<AgyAccountLogMapper, AgyAccountLog> {

    @Autowired
    private AgentV2AccountLogMapper agentV2AccountLogMapper;
    @Autowired
    private AgentV2Mapper agentAccountMapper;

    public PageUtils getAccountLogList(AgentV2AccountLogDto agentV2AccountLogDto){
        PageHelper.startPage(agentV2AccountLogDto.getPageNo(), agentV2AccountLogDto.getPageSize());
        // 查询数据
        List<AgentV2AccountLogDto> list = new ArrayList<>();
        // TODO 判断会员权限

        // 用户（代理）agent 用户（会员）mbr  执行用户 operator
        if("agent".equals(agentV2AccountLogDto.getUserType())){
            list = agentV2AccountLogMapper.getAccountLogListAgent(agentV2AccountLogDto);
        }else if("mbr".equals(agentV2AccountLogDto.getUserType())){
            list = agentV2AccountLogMapper.getAccountLogListMbr(agentV2AccountLogDto);
        }else if("operator".equals(agentV2AccountLogDto.getUserType())){
            list = agentV2AccountLogMapper.getAccountLogListOperator(agentV2AccountLogDto);
        }
        return BeanUtil.toPagedResult(list);
    }



    public void agyAccountSave(AgentAccount agentAccount,AgentAccount loginAccount){
        AgyAccountLog agyAccountLog = new AgyAccountLog();
        agyAccountLog.setAgyId(loginAccount.getId());
        agyAccountLog.setAgyAccount(loginAccount.getAgyAccount());
        agyAccountLog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));

        agyAccountLog.setOperatorUser(agentAccount.getAgyAccount());
        agyAccountLog.setBeforeChange("");
        agyAccountLog.setAfterChange("");
        agyAccountLog.setModuleName(AGENT_V2_AGYACCOUNTSAVE);
        save(agyAccountLog);
    }

    public void addMbrAccount(MbrAccount mbrAccount, AgentAccount loginAccount){
        AgyAccountLog agyAccountLog = new AgyAccountLog();
        agyAccountLog.setAgyId(loginAccount.getId());
        agyAccountLog.setAgyAccount(loginAccount.getAgyAccount());
        agyAccountLog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));

        agyAccountLog.setOperatorUser(mbrAccount.getLoginName());
        agyAccountLog.setBeforeChange("");
        agyAccountLog.setAfterChange("");
        agyAccountLog.setModuleName(AGENT_V2_AGYACCOUNTSAVE);
        save(agyAccountLog);
    }

    public void updateAgentPassword(AgentAccount agentAccount,AgentAccount loginAccount){
        AgyAccountLog agyAccountLog = new AgyAccountLog();
        agyAccountLog.setAgyId(loginAccount.getId());
        agyAccountLog.setAgyAccount(loginAccount.getAgyAccount());
        agyAccountLog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));

        agyAccountLog.setOperatorUser(agentAccount.getAgyAccount());
        agyAccountLog.setBeforeChange("");
        agyAccountLog.setAfterChange("");
        agyAccountLog.setModuleName(AGENT_V2_UPDATEAGENTPASSOWRD);
        save(agyAccountLog);
    }

    public void updateAgentAvailable(AgentAccount oldAgyAccount,AgentAccount newAgyAccount,AgentAccount loginAccount){
        AgyAccountLog agyAccountLog = new AgyAccountLog();
        agyAccountLog.setAgyId(loginAccount.getId());
        agyAccountLog.setAgyAccount(loginAccount.getAgyAccount());
        agyAccountLog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));

        agyAccountLog.setOperatorUser(oldAgyAccount.getAgyAccount());
        agyAccountLog.setBeforeChange((Integer.valueOf(Constants.EVNumber.one).equals(oldAgyAccount.getAvailable()) ? "开启" : "关闭"));
        agyAccountLog.setAfterChange((Integer.valueOf(Constants.EVNumber.one).equals(newAgyAccount.getAvailable()) ? "开启" : "关闭"));
        agyAccountLog.setModuleName(AGENT_V2_UPDATEAVAILABLE);
        save(agyAccountLog);
    }

    public void updateMbrAvailable(MbrAccount oldAccount,MbrAccount newAccount,AgentAccount loginAccount){
        AgyAccountLog agyAccountLog = new AgyAccountLog();
        agyAccountLog.setAgyId(loginAccount.getId());
        agyAccountLog.setAgyAccount(loginAccount.getAgyAccount());
        agyAccountLog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));

        agyAccountLog.setOperatorUser(oldAccount.getLoginName());
        agyAccountLog.setBeforeChange((Integer.valueOf(Constants.EVNumber.one).equals(oldAccount.getAvailable()) ? "开启" : "关闭"));
        agyAccountLog.setAfterChange((Integer.valueOf(Constants.EVNumber.one).equals(newAccount.getAvailable()) ? "开启" : "关闭"));
        agyAccountLog.setModuleName(AGENT_V2_UPDATEAVAILABLE);
        save(agyAccountLog);
    }

    public void updateMbrBettingStatus(MbrAccountOther oldAccount, MbrAccountOther newAccount, AgentAccount loginAccount){
        AgyAccountLog agyAccountLog = new AgyAccountLog();
        agyAccountLog.setAgyId(loginAccount.getId());
        agyAccountLog.setAgyAccount(loginAccount.getAgyAccount());
        agyAccountLog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));

        agyAccountLog.setOperatorUser(oldAccount.getLoginName());
        agyAccountLog.setBeforeChange((Integer.valueOf(Constants.EVNumber.one).equals(oldAccount.getBettingStatus()) ? "开启" : "关闭"));
        agyAccountLog.setAfterChange((Integer.valueOf(Constants.EVNumber.one).equals(newAccount.getBettingStatus()) ? "开启" : "关闭"));
        agyAccountLog.setModuleName(AGENT_V2_UPDATEBETTINGSTATUS);
        save(agyAccountLog);
    }

    public void updateAgentBettingStatus(List<AgentAccountOther>  agyList , List<MbrAccountOther> mbrList, AgentAccount newAccount, AgentAccount loginAccount){
        List<AgyAccountLog> list = new ArrayList<>();

        for(AgentAccountOther dto: agyList ){
            AgyAccountLog agyAccountLog = new AgyAccountLog();
            agyAccountLog.setAgyId(loginAccount.getId());
            agyAccountLog.setAgyAccount(loginAccount.getAgyAccount());
            agyAccountLog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
            agyAccountLog.setModuleName(AGENT_V2_UPDATEBETTINGSTATUS);
            agyAccountLog.setIp("");

            agyAccountLog.setOperatorUser(dto.getAgyAccount());
            agyAccountLog.setBeforeChange((Integer.valueOf(Constants.EVNumber.one).equals(dto.getBettingStatus()) ? "开启" : "关闭"));
            agyAccountLog.setAfterChange((Integer.valueOf(Constants.EVNumber.one).equals(newAccount.getBettingStatus()) ? "开启" : "关闭"));
            agyAccountLog.setModuleName(AGENT_V2_UPDATEBETTINGSTATUS);
            list.add(agyAccountLog);
        }

        for(MbrAccountOther dto: mbrList ){
            AgyAccountLog agyAccountLog = new AgyAccountLog();
            agyAccountLog.setAgyId(loginAccount.getId());
            agyAccountLog.setAgyAccount(loginAccount.getAgyAccount());
            agyAccountLog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
            agyAccountLog.setModuleName(AGENT_V2_UPDATEBETTINGSTATUS);
            agyAccountLog.setIp("");

            agyAccountLog.setOperatorUser(dto.getLoginName());
            agyAccountLog.setBeforeChange((Integer.valueOf(Constants.EVNumber.one).equals(dto.getBettingStatus()) ? "开启" : "关闭"));
            agyAccountLog.setAfterChange((Integer.valueOf(Constants.EVNumber.one).equals(newAccount.getBettingStatus()) ? "开启" : "关闭"));
            agyAccountLog.setModuleName(AGENT_V2_UPDATEBETTINGSTATUS);

            list.add(agyAccountLog);
        }

        // 批量插入
        agentV2AccountLogMapper.batchInsertAgyAccountLog(list);
    }

    public void bindAgentMobile(AgentAccount agentAccount){
        AgyAccountLog agyAccountLog = new AgyAccountLog();
        agyAccountLog.setAgyId(agentAccount.getId());
        agyAccountLog.setAgyAccount(agentAccount.getAgyAccount());
        agyAccountLog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));

        agyAccountLog.setOperatorUser(agentAccount.getAgyAccount());
        agyAccountLog.setBeforeChange("");
        agyAccountLog.setAfterChange(agentAccount.getMobile());
        agyAccountLog.setModuleName(AGENT_V2_BINDAGENTMOBILE);
        save(agyAccountLog);
    }

    public void agyAccountUpdate(AgentAccount oldAgent,AgentAccountOther oldOther,AgentAccount newAgent,AgentAccountOther newOther,AgentAccount loginAccount){
        // 获得上级
        AgentAccount parentAgentAccount = new AgentAccount();
        parentAgentAccount.setId(oldAgent.getParentId());
        AgentAccountOther parentAgentOther = agentAccountMapper.selectByAgent(parentAgentAccount);
        String createTime = getCurrentDate(FORMAT_18_DATE_TIME);
        String memo = "由上级代理设置";
        String memo1 = "对应下级：";

        // 修改了真人占成
        if(oldOther.getRealpeople().compareTo(newOther.getRealpeople()) != 0){
            // 保存真人占成
            AgyAccountLog agyAccountLog = new AgyAccountLog();
            agyAccountLog.setAgyId(loginAccount.getId());
            agyAccountLog.setAgyAccount(loginAccount.getAgyAccount());
            agyAccountLog.setCreateTime(createTime);
            agyAccountLog.setMemo(memo);

            agyAccountLog.setOperatorUser(oldOther.getAgyAccount());
            agyAccountLog.setBeforeChange(oldOther.getRealpeople().toString());
            agyAccountLog.setAfterChange(CommonUtil.adjustScale(newOther.getRealpeople()).toString());
            agyAccountLog.setModuleName(AGENT_V2_REALPEOPLE);
            save(agyAccountLog);

            // 保存真人占成比
            AgyAccountLog agyAccountLog1 = new AgyAccountLog();
            agyAccountLog1.setAgyId(loginAccount.getId());
            agyAccountLog1.setAgyAccount(loginAccount.getAgyAccount());
            agyAccountLog1.setCreateTime(createTime);
            agyAccountLog1.setMemo(memo1 + oldOther.getAgyAccount());

            agyAccountLog1.setOperatorUser(parentAgentOther.getAgyAccount());
            String before = parentAgentOther.getRealpeople().subtract(oldOther.getRealpeople()).toString();
            String after = parentAgentOther.getRealpeople().subtract(newOther.getRealpeople()).toString();
            agyAccountLog1.setBeforeChange(before);
            agyAccountLog1.setAfterChange(after);
            agyAccountLog1.setModuleName(AGENT_V2_REALPEOPLEPARENT);
            save(agyAccountLog1);
        }

        // 修改了电子占成
        if(oldOther.getElectronic().compareTo(newOther.getElectronic())!= 0){
            // 保存电子占成
            AgyAccountLog agyAccountLog = new AgyAccountLog();
            agyAccountLog.setAgyId(loginAccount.getId());
            agyAccountLog.setAgyAccount(loginAccount.getAgyAccount());
            agyAccountLog.setCreateTime(createTime);
            agyAccountLog.setMemo(memo);

            agyAccountLog.setOperatorUser(oldOther.getAgyAccount());
            agyAccountLog.setBeforeChange(oldOther.getElectronic().toString());
            agyAccountLog.setAfterChange(CommonUtil.adjustScale(newOther.getElectronic()).toString());
            agyAccountLog.setModuleName(AGENT_V2_ELECTRONIC);
            save(agyAccountLog);

            // 保存电子占成比
            AgyAccountLog agyAccountLog1 = new AgyAccountLog();
            agyAccountLog1.setAgyId(loginAccount.getId());
            agyAccountLog1.setAgyAccount(loginAccount.getAgyAccount());
            agyAccountLog1.setCreateTime(createTime);
            agyAccountLog1.setMemo(memo1 + oldOther.getAgyAccount());

            agyAccountLog1.setOperatorUser(parentAgentOther.getAgyAccount());
            String before = parentAgentOther.getElectronic().subtract(oldOther.getElectronic()).toString();
            String after = parentAgentOther.getElectronic().subtract(newOther.getElectronic()).toString();
            agyAccountLog1.setBeforeChange(before);
            agyAccountLog1.setAfterChange(after);
            agyAccountLog1.setModuleName(AGENT_V2_ELECTRONICPARENT);
            save(agyAccountLog1);
        }

        // 修改了真人佣金
        if(oldOther.getRealpeoplewash().compareTo(newOther.getRealpeoplewash()) != 0){
            // 保存真人佣金
            AgyAccountLog agyAccountLog = new AgyAccountLog();
            agyAccountLog.setAgyId(loginAccount.getId());
            agyAccountLog.setAgyAccount(loginAccount.getAgyAccount());
            agyAccountLog.setCreateTime(createTime);
            agyAccountLog.setMemo(memo);

            agyAccountLog.setOperatorUser(oldOther.getAgyAccount());
            agyAccountLog.setBeforeChange(oldOther.getRealpeoplewash().toString());
            agyAccountLog.setAfterChange(CommonUtil.adjustScale(newOther.getRealpeoplewash()).toString());
            agyAccountLog.setModuleName(AGENT_V2_REALPEOPLEWASH);
            save(agyAccountLog);
        }

        // 修改了电子佣金
        if(oldOther.getElectronicwash().compareTo(newOther.getElectronicwash()) != 0){
            // 保存电子佣金
            AgyAccountLog agyAccountLog = new AgyAccountLog();
            agyAccountLog.setAgyId(loginAccount.getId());
            agyAccountLog.setAgyAccount(loginAccount.getAgyAccount());
            agyAccountLog.setCreateTime(createTime);
            agyAccountLog.setMemo(memo);

            agyAccountLog.setOperatorUser(oldOther.getAgyAccount());
            agyAccountLog.setBeforeChange(oldOther.getElectronicwash().toString());
            agyAccountLog.setAfterChange(CommonUtil.adjustScale(newOther.getElectronicwash()).toString());
            agyAccountLog.setModuleName(AGENT_V2_ELECTRONICWASH);
            save(agyAccountLog);
        }
    }

    public void updateMbrAccount(MbrAccount oldMbr,MbrAccountOther oldOther,MbrAccount newMbr,MbrAccountOther newOther,AgentAccount loginAccount){
        String createTime = getCurrentDate(FORMAT_18_DATE_TIME);
        String memo = "由上级代理设置";

        // 修改了真人佣金
        if(oldOther.getRealpeoplewash().compareTo(newOther.getRealpeoplewash()) != 0){
            // 保存真人佣金
            AgyAccountLog agyAccountLog = new AgyAccountLog();
            agyAccountLog.setAgyId(loginAccount.getId());
            agyAccountLog.setAgyAccount(loginAccount.getAgyAccount());
            agyAccountLog.setCreateTime(createTime);
            agyAccountLog.setMemo(memo);

            agyAccountLog.setOperatorUser(oldOther.getLoginName());
            agyAccountLog.setBeforeChange(oldOther.getRealpeoplewash().toString());
            agyAccountLog.setAfterChange(newOther.getRealpeoplewash().toString());
            agyAccountLog.setModuleName(AGENT_V2_REALPEOPLEWASH);
            save(agyAccountLog);
        }

        // 修改了电子佣金
        if(oldOther.getElectronicwash().compareTo(newOther.getElectronicwash()) != 0){
            // 保存电子佣金
            AgyAccountLog agyAccountLog = new AgyAccountLog();
            agyAccountLog.setAgyId(loginAccount.getId());
            agyAccountLog.setAgyAccount(loginAccount.getAgyAccount());
            agyAccountLog.setCreateTime(createTime);
            agyAccountLog.setMemo(memo);

            agyAccountLog.setOperatorUser(oldOther.getLoginName());
            agyAccountLog.setBeforeChange(oldOther.getElectronicwash().toString());
            agyAccountLog.setAfterChange(newOther.getElectronicwash().toString());
            agyAccountLog.setModuleName(AGENT_V2_ELECTRONICWASH);
            save(agyAccountLog);
        }
    }

    public void updateAgentBalanceAgy(AgyWallet oldOperatee,AgyWallet newOperatee, AgentAccount loginAccount){
        AgyAccountLog agyAccountLog = new AgyAccountLog();
        agyAccountLog.setAgyId(loginAccount.getId());
        agyAccountLog.setAgyAccount(loginAccount.getAgyAccount());
        agyAccountLog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));

        agyAccountLog.setOperatorUser(oldOperatee.getAgyAccount());
        agyAccountLog.setBeforeChange(oldOperatee.getBalance().toString());
        agyAccountLog.setAfterChange(newOperatee.getBalance().toString());
        agyAccountLog.setModuleName(AGENT_V2_BALANCE);
        save(agyAccountLog);
    }

    public void updateAccountBalance(String oldOperatee, BigDecimal oldOperatorBalance,BigDecimal newOperatorBalance, AgentAccount loginAccount){
        AgyAccountLog agyAccountLog = new AgyAccountLog();
        agyAccountLog.setAgyId(loginAccount.getId());
        agyAccountLog.setAgyAccount(loginAccount.getAgyAccount());
        agyAccountLog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));

        agyAccountLog.setOperatorUser(oldOperatee);
        agyAccountLog.setBeforeChange(oldOperatorBalance.toString());
        agyAccountLog.setAfterChange(newOperatorBalance.toString());
        agyAccountLog.setModuleName(AGENT_V2_BALANCE);
        save(agyAccountLog);
    }

}
