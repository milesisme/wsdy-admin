package com.wsdy.saasops.modules.system.agencydomain.service;

import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.system.agencydomain.dao.SystemAgencyUrlMapper;
import com.wsdy.saasops.modules.system.agencydomain.entity.SystemAgencyUrl;
import com.wsdy.saasops.modules.system.agencydomain.mapper.MySystemAgencyUrlMapper;
import com.wsdy.saasops.modules.system.domain.service.SystemDomainService;
import com.github.pagehelper.PageHelper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
@Service
public class SystemAgencyUrlService {
	
    @Autowired
    private SystemAgencyUrlMapper systemAgencyUrlMapper;
    @Autowired
    private MySystemAgencyUrlMapper mySystemAgencyUrlMapper;

    public SystemAgencyUrl queryObject(Integer agencyId) {
        return systemAgencyUrlMapper.selectByPrimaryKey(agencyId);
    }

    public PageUtils queryListPage(Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        } else {
            PageHelper.orderBy("modifyTime Desc");
        }
        List<SystemAgencyUrl> list = systemAgencyUrlMapper.selectAll();
        return BeanUtil.toPagedResult(list);
    }

    /**
     * 插入，包括批量和单插入
     *
     * @param systemAgencyUrl
     */
    public void save(SystemAgencyUrl systemAgencyUrl) throws CloneNotSupportedException {
        String urls = CommonUtil.remKong(systemAgencyUrl.getUrl());
        if (!StringUtils.isEmpty(urls)) {
            if (urls.indexOf(",") == 0) {
                systemAgencyUrlMapper.insert(systemAgencyUrl);
            } else {
                String[] urlArr = urls.split(",");
                List<SystemAgencyUrl> systemAgencyUrls = new ArrayList<>();
                for (String url : urlArr) {
                    if (!StringUtils.isEmpty(url)) {
                        SystemAgencyUrl sau = new SystemAgencyUrl();
                        try {
                            sau = (SystemAgencyUrl) systemAgencyUrl.clone();
                            sau.setUrl(url);
                        } catch (CloneNotSupportedException e) {
                            log.error("克隆异常:" + e);
                            throw e;
                        }
                        systemAgencyUrls.add(sau);
                    }
                }
                mySystemAgencyUrlMapper.multiInsert(systemAgencyUrls);
            }
        }
    }

    public void update(SystemAgencyUrl systemAgencyUrl) {
        systemAgencyUrl.setModifyTime(new Date());
        systemAgencyUrlMapper.updateByPrimaryKeySelective(systemAgencyUrl);
    }

    public void delete(Integer agencyId) {
        systemAgencyUrlMapper.deleteByPrimaryKey(agencyId);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(String ids) {
        mySystemAgencyUrlMapper.multiDelete(ids);
    }

    /**
     * 关联查询
     *
     * @param systemAgencyUrl
     * @param pageNo
     * @param pageSize
     * @param orderBy
     * @return
     */
    public PageUtils queryConditions(SystemAgencyUrl systemAgencyUrl, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        } else {
            PageHelper.orderBy("modifyTime Desc");
        }
        List<SystemAgencyUrl> list = mySystemAgencyUrlMapper.queryConditions(systemAgencyUrl);
        return BeanUtil.toPagedResult(list);
    }
}
