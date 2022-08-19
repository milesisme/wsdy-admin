package com.wsdy.saasops.modules.system.domain.service;

import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.ExcelUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.system.domain.dao.SystemDomainMapper;
import com.wsdy.saasops.modules.system.domain.entity.DomainType;
import com.wsdy.saasops.modules.system.domain.entity.StateType;
import com.wsdy.saasops.modules.system.domain.entity.SystemDomain;
import com.wsdy.saasops.modules.system.domain.mapper.DomainMapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Slf4j
@Service("systemDomainService")
public class SystemDomainService {

    @Autowired
    private SystemDomainMapper systemDomainMapper;
    @Autowired
    private DomainMapper domainMapper;
    @Value("${domain.agency.excel.path}")
    private String domainExcelPath;

    public SystemDomain queryObject(Integer id) {
        return systemDomainMapper.selectByPrimaryKey(id);
    }

    public PageUtils queryListPage(SystemDomain domain, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<SystemDomain> domains=domainMapper.queryByConditions(domain);
        return BeanUtil.toPagedResult(domains);
    }

    /**
     * 不分页
     * @param domain
     * @return
     */
    public List<SystemDomain> queryListPage(SystemDomain domain) {
        return domainMapper.queryByConditions(domain);
    }

    public void save(SystemDomain systemDomain) throws CloneNotSupportedException {
        Date d =new Date();
        systemDomain.setCreateTime(d);
        systemDomain.setModifyTime(d);
        systemDomain.setBind(2);
        systemDomain.setState(1);
        String domainUrls= CommonUtil.remKong(systemDomain.getDomainUrl());
        if(domainUrls.indexOf(",") == 0) {
            systemDomainMapper.insert(systemDomain);
        }else {
            String[] domainUrl =domainUrls.split(",");
            List<SystemDomain> systemDomains =new ArrayList<>();
            for (String url :domainUrl){
                if(!StringUtils.isEmpty(url)){
                    SystemDomain systemDomainCopy = new SystemDomain();
                    try {
                        systemDomainCopy =(SystemDomain)systemDomain.clone();
                        systemDomainCopy.setDomainUrl(url);
                    } catch (CloneNotSupportedException e) {
                        log.error("克隆异常" + e);
                        throw e;
                    }
                    systemDomains.add(systemDomainCopy);
                }
            }
            domainMapper.multiInsert(systemDomains);
        }
    }

    public void update(SystemDomain systemDomain) {
        systemDomain.setModifyTime(new Date());
        systemDomainMapper.updateByPrimaryKeySelective(systemDomain);
    }

    public void delete(Integer id) {
            systemDomainMapper.deleteByPrimaryKey(id);
    }

    /**
     * 批量删除
     * @param ids
     */
    public void deleteBatch(String ids) {
        domainMapper.delByIds(ids);
    }

    /**
     * 模糊查询
     * @param domain
     * @param pageNo
     * @param pageSize
     * @return
     */
    public PageUtils queryByConditions(SystemDomain domain, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<SystemDomain> domains=domainMapper.queryByConditions(domain);
        return BeanUtil.toPagedResult(domains);
    }

    public void domainExportExcel(SystemDomain domain, HttpServletResponse response){
        String fileName = "站点域名" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        List<Map<String, Object>> list = Lists.newLinkedList();
        domainMapper.queryByConditions(domain).stream().forEach(
                cs->{
                    Map<String, Object> param = new HashMap<>(8);
                    param.put("name", cs.getName());
                    param.put("domainType", DomainType.getName(cs.getDomainType()));
                    param.put("domainUrl", cs.getDomainUrl());
                    param.put("bind", cs.getBind()==1?"绑定":"未绑定");
                    param.put("state", StateType.getName(cs.getState()));
                    list.add(param);
                }
        );
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList", domainExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            log.error("error:" + e);
        }
    }
}
