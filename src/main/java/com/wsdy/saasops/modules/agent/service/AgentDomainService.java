package com.wsdy.saasops.modules.agent.service;

import com.wsdy.saasops.api.modules.apisys.dao.TcpSiteurlMapper;
import com.wsdy.saasops.api.modules.apisys.entity.TCpSite;
import com.wsdy.saasops.api.modules.apisys.entity.TcpSiteurl;
import com.wsdy.saasops.api.modules.apisys.mapper.ApiSysMapper;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.agent.dao.*;
import com.wsdy.saasops.modules.agent.entity.*;
import com.wsdy.saasops.modules.agent.mapper.AgentMapper;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

@Slf4j
@Service
public class AgentDomainService extends BaseService<AgentAccountMapper, AgentAccount> {

    @Autowired
    private AgyDomainMapper agyDomainMapper;
    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    ApiSysMapper apiSysMapper;
    @Autowired
    TcpSiteurlMapper tcpSiteurlMapper;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private MbrAccountLogService accountLogService;

    public List<String> domainList(String siteCode) {
        return agentMapper.domainList(siteCode);
    }

    public List<String> domainSubList(String siteCode) {
        return agentMapper.domainSubList(siteCode);
    }

    public PageUtils agyDomainList(AgyDomain agyDomain, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<AgyDomain> list = agentMapper.findAgyDomainListPage(agyDomain);
        return BeanUtil.toPagedResult(list);
    }

    @Transactional
    public void agyDomainSave(AgyDomain agyDomain1, String userName, int flag) {
        for (String domainUrl : agyDomain1.getDomainUrlList()) {
            AgyDomain agyDomain = new AgyDomain();
            agyDomain.setDomainUrl(domainUrl);
            AgentAccount accountParam = new AgentAccount();
            accountParam.setAgyAccount(agyDomain1.getAgyAccount());
            List<AgentAccount> list = agentAccountMapper.select(accountParam);
            agyDomain.setAccountId(list.get(0).getId());
            agyDomain.setAgyAccount(list.get(0).getAgyAccount());
            agyDomain.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
            agyDomain.setCreateUser(userName);
            agyDomain.setStatus(Constants.EVNumber.one); //?????????
            agyDomain.setIsDel(1);  //?????????????????? 0??? 1???
            agyDomain.setAvailable(1); //??????
            agyDomain.setExpireDate(agyDomain1.getExpireDate());
            String agyDomains = agentMapper.findAgyCommitDomain();
            // ???????????????????????????????????????domainUrl
            String oldDomainUrl = agyDomain.getDomainUrl();
            if (-1 == agyDomain.getDomainUrl().indexOf(",")) {
                verifyDomain(agyDomain.getDomainUrl(), agyDomains);
                agyDomainMapper.insertSelective(agyDomain);
            } else {
                String[] domains = agyDomain.getDomainUrl().split(",");
                for (String domain : domains) {
                    verifyDomain(domain, agyDomains);
                    agyDomain.setDomainUrl(domain);
                    agyDomainMapper.insertSelective(agyDomain);
                    agyDomain.setId(null);
                }
            }
            // ??????????????????
            if (flag == Constants.EVNumber.one) {    // ??????????????????
                agyDomain.setDomainUrl(oldDomainUrl);
                accountLogService.agyDomainSaveLog(agyDomain);
            }
        }
    }

    public void verifyDomain(String domain, String agyDomains) {
        if (StringUtils.isEmpty(domain)) {
            throw new R200Exception("?????????????????????!");
        }
        int count = agentMapper.findAgyCommitDomainCount(domain);
        if (count > 0) {
            throw new R200Exception(domain + "????????????!");
        }
        if ("www.".equals(domain.substring(0, 4).toLowerCase())) {
            throw new R200Exception("????????????www.???????????????!");
        }
        if ("m.".equals(domain.substring(0, 2).toLowerCase())) {
            throw new R200Exception("????????????m.???????????????!");
        }
    }


    public void agyDomainAudit(AgyDomain agyDomain, String userName, String siteCode) {
        agyDomain.setModifyUser(userName);
        agyDomain.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));

        //??????????????????????????????
        AgyDomain agyDomainTemp = agyDomainMapper.selectByPrimaryKey(agyDomain.getId());    // ??????if??????????????????????????????
        if (1 == agyDomain.getStatus()) {
            String mainDomainUrl = CommonUtil.getDomainForUrl(agyDomainTemp.getDomainUrl());
            String agyDomains = agentMapper.findAgyDomain(1);
            if (StringUtils.isNotEmpty(agyDomains)) {
                if (agyDomains.contains(agyDomainTemp.getDomainUrl())) {
                    throw new RRException(agyDomainTemp.getDomainUrl() + "????????????!");
                }
            }
            TcpSiteurl siteUrl = new TcpSiteurl();
            siteUrl.setSiteUrl(mainDomainUrl);
            siteUrl.setAvailable(1);
            siteUrl.setSiteCode(siteCode);
            List<TcpSiteurl> list = tcpSiteurlMapper.select(siteUrl);
            if (Collections3.isEmpty(list)) {
                TCpSite site = apiSysMapper.getCpSiteBySiteCode(siteCode);
                siteUrl.setSiteId(site.getId());
                siteUrl.setSiteCode(site.getSiteCode());
                siteUrl.setSiteUrl(mainDomainUrl);
                siteUrl.setClientType(3);  //pc??????
                siteUrl.setAvailable(1);
                apiSysMapper.insertCpSiteUrlInfo(siteUrl);
            }
        }
        agyDomainMapper.updateByPrimaryKeySelective(agyDomain);

        // ??????????????????
        agyDomain.setAgyAccount(agyDomainTemp.getAgyAccount());
        agyDomain.setDomainUrl(agyDomainTemp.getDomainUrl());
        accountLogService.agyDomainAuditLog(agyDomain);
    }

    @Transactional
    public void agyDomainDelete(AgyDomain agyDomain, String siteCode) {
        for (Integer id : agyDomain.getIds()) {
            // AgyDomain agyDomain = agyDomainMapper.selectByPrimaryKey(id);
            agyDomainMapper.deleteByPrimaryKey(id);
            //String mainDomainUrl = CommonUtil.getDomainForUrl(agyDomain.getDomainUrl());
       /* int count = agentMapper.selectCountByDomainUrl(mainDomainUrl);
        if (0 == count) {
            TCpSite site = apiSysMapper.getCpSiteBySiteCode(siteCode);
            TcpSiteurl siteUrl = new TcpSiteurl();
            siteUrl.setSiteId(site.getId());
            siteUrl.setSiteCode(site.getSiteCode());
            siteUrl.setSiteUrl(mainDomainUrl);
            siteUrl.setClientType(1);  // ?????????????????????????????????
            apiSysMapper.updateCpSiteUrlClientType(siteUrl);
        }*/
        }
    }

    public void updateAvailable(AgyDomain agyDomain){
        AgyDomain domain = agyDomainMapper.selectByPrimaryKey(agyDomain.getId());
        domain.setAvailable(agyDomain.getAvailable());
        agyDomainMapper.updateByPrimaryKeySelective(domain);
    }
}

