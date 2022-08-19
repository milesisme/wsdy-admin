package com.wsdy.saasops.agapi.modules.service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.dao.AgentSubAccountMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgentSubAccount;
import com.wsdy.saasops.modules.agent.entity.AgySubMenu;
import com.wsdy.saasops.modules.agent.mapper.SafeyInfoMapper;
import com.wsdy.saasops.modules.sys.entity.SysMenuEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Objects.isNull;

@Slf4j
@Service
@Transactional
public class AgentSafeyInfoService {

    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private SafeyInfoMapper safeyInfoMapper;
    @Autowired
    private AgentSubAccountMapper subAccountMapper;


    public AgentAccount agentInfo(AgentAccount account) {
        AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(account.getId());
        agentAccount.setIsSecurePwd(Constants.EVNumber.one);
        if (StringUtils.isEmpty(agentAccount.getSecurePwd())) {
            agentAccount.setIsSecurePwd(Constants.EVNumber.zero);
        }
        agentAccount.setSecurePwd(null);
        agentAccount.setAgyPwd(null);
        agentAccount.setSalt(null);
        return agentAccount;
    }

    public void updateContact(AgentAccount account, AgentAccount updateAccount) {
        AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(account.getId());
        if (StringUtils.isNotEmpty(updateAccount.getEmail())) {
            agentAccount.setEmail(updateAccount.getEmail());
        }
        if (StringUtils.isNotEmpty(updateAccount.getQq())) {
            agentAccount.setQq(updateAccount.getQq());
        }
        if (StringUtils.isNotEmpty(updateAccount.getWeChat())) {
            agentAccount.setWeChat(updateAccount.getWeChat());
        }
        if (StringUtils.isNotEmpty(updateAccount.getSkype())) {
            agentAccount.setSkype(updateAccount.getSkype());
        }
        if (StringUtils.isNotEmpty(updateAccount.getFlyGram())) {
            agentAccount.setFlyGram(updateAccount.getFlyGram());
        }
        if (StringUtils.isNotEmpty(updateAccount.getTelegram())) {
            agentAccount.setTelegram(updateAccount.getTelegram());
        }
        if (StringUtils.isNotEmpty(updateAccount.getRealName())) {
            agentAccount.setRealName(updateAccount.getRealName());
        }
        agentAccountMapper.updateByPrimaryKeySelective(agentAccount);
    }

    public void checkoutAgentMobile(Integer agentId, String mobile) {
        AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(agentId);
        if (!mobile.equals(agentAccount.getMobile())) {
            throw new R200Exception("手机号码跟注册号码不一致");
        }
    }

    public void updateSecurePwd(AgentAccount account, AgentAccount updateAccount) {
        AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(account.getId());
        String securePwd = new Sha256Hash(updateAccount.getSecurePwd(), agentAccount.getSalt()).toHex();
        agentAccount.setSecurePwd(securePwd);
        agentAccountMapper.updateByPrimaryKeySelective(agentAccount);
    }

    public void updateAgyPwd(AgentAccount account, AgentAccount updateAccount) {
        AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(account.getId());
        String salt = agentAccount.getSalt();
        String csPassword = new Sha256Hash(updateAccount.getAgyPwd(), salt).toHex();
        String newPassword = new Sha256Hash(updateAccount.getNewAgyPwd(), salt).toHex();
        if (!csPassword.equals(agentAccount.getAgyPwd())) {
            throw new R200Exception("旧密码错误");
        }
        agentAccount.setAgyPwd(newPassword);
        agentAccountMapper.updateByPrimaryKeySelective(agentAccount);
    }

    public List<AgentSubAccount> fundSubAccountList(AgentAccount agentAccount) {
        List<AgentSubAccount> subAccounts = safeyInfoMapper.fundSubAccountList(agentAccount.getId());
        if (subAccounts.size() > 0) {
            subAccounts.stream().forEach(st -> {
                st.setSubMenus(safeyInfoMapper.fundAgySubMenu(st.getId()));
            });
        }
        return subAccounts;
    }

    public List<SysMenuEntity> fundSubAccountMenu() {
        return safeyInfoMapper.fundSubAccountMenu();
    }

    public void addSubAccount(AgentSubAccount subAccount) {
        Integer subAccountId = subAccount.getId();
        if (isNull(subAccount.getId())) {
            AgentSubAccount agentSubAccount = new AgentSubAccount();
            agentSubAccount.setAgyAccount(subAccount.getAgyAccount());
            int count = subAccountMapper.selectCount(agentSubAccount);
            if (count > 0) {
                throw new R200Exception("账号已经存在");
            }
            subAccount.setSalt(RandomStringUtils.randomAlphanumeric(20));
            subAccount.setAgyPwd(new Sha256Hash(subAccount.getAgyPwd(), subAccount.getSalt()).toHex());
            subAccount.setCreateTime(subAccount.getModifyTime());
            subAccount.setCreateUser(subAccount.getModifyUser());
            subAccount.setModifyTime(subAccount.getModifyTime());
            subAccount.setModifyUser(subAccount.getModifyUser());
            subAccountMapper.insert(subAccount);
            subAccountId = subAccount.getId();
        } else {
            AgentSubAccount subAccount1 = subAccountMapper.selectByPrimaryKey(subAccount.getId());
            if (StringUtils.isNotEmpty(subAccount.getAgyPwd())) {
                String agyPwd = new Sha256Hash(subAccount.getAgyPwd(), subAccount1.getSalt()).toHex();
                subAccount1.setAgyPwd(agyPwd);
            }
            subAccount1.setModifyTime(subAccount.getModifyTime());
            subAccount1.setModifyUser(subAccount.getModifyUser());
            subAccount1.setMemo(subAccount.getMemo());
            subAccountMapper.updateByPrimaryKeySelective(subAccount1);
        }
        if (Collections3.isNotEmpty(subAccount.getSubMenus())) {
            safeyInfoMapper.deleteSubMenu(subAccount.getAgyAccount());
            for (AgySubMenu agySubMenu : subAccount.getSubMenus()) {
                safeyInfoMapper.addSubMenu(subAccount.getAgyAccount(), subAccountId, agySubMenu.getMenu_id());
            }
        }
    }

}
