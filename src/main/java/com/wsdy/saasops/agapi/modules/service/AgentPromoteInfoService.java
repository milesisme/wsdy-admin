package com.wsdy.saasops.agapi.modules.service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.api.utils.JwtUtils;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.agent.dao.AgyDomainMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgyDomain;
import com.wsdy.saasops.modules.agent.service.AgentMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class AgentPromoteInfoService {

    @Autowired
    private AgyDomainMapper agyDomainMapper;
    @Autowired
    private AgentMaterialService materialService;

    public PageUtils sponsoredLinks(Integer pageNo, Integer pageSize, AgentAccount agentAccount) {
        PageHelper.startPage(pageNo, pageSize);
        AgyDomain agyDomain = new AgyDomain();
        agyDomain.setAccountId(agentAccount.getId());
        agyDomain.setAvailable(Constants.EVNumber.one);
        List<AgyDomain> agyDomains = agyDomainMapper.select(agyDomain);
        return BeanUtil.toPagedResult(agyDomains);
    }

    public PageUtils promotionMaterials(Integer pageNo, Integer pageSize) {
        return materialService.materialDetailList(null, pageNo, pageSize);
    }
}
