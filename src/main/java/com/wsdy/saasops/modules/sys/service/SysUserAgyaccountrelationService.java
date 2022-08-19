package com.wsdy.saasops.modules.sys.service;

import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.sys.dao.SysUserAgyaccountrelationMapper;
import com.wsdy.saasops.modules.sys.entity.SysUserAgyaccountrelation;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;


@Service
public class SysUserAgyaccountrelationService{

    @Autowired
    private SysUserAgyaccountrelationMapper sysUserAgyaccountrelationMapper;

    public SysUserAgyaccountrelation queryObject(Integer id) {
        return sysUserAgyaccountrelationMapper.selectByPrimaryKey(id);
    }

    public PageUtils queryListPage(SysUserAgyaccountrelation sysUserAgyaccountrelation, Integer pageNo, Integer pageSize,String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if(!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        List<SysUserAgyaccountrelation> list = sysUserAgyaccountrelationMapper.selectAll();
        return BeanUtil.toPagedResult(list);
    }

    public void save(SysUserAgyaccountrelation sysUserAgyaccountrelation) {
            sysUserAgyaccountrelationMapper.insert(sysUserAgyaccountrelation);
    }

    public void saveList(List<SysUserAgyaccountrelation> sysUserAgyaccountrelations) {
        sysUserAgyaccountrelationMapper.insertList(sysUserAgyaccountrelations);
    }
    public void update(SysUserAgyaccountrelation sysUserAgyaccountrelation) {
            sysUserAgyaccountrelationMapper.updateByPrimaryKeySelective(sysUserAgyaccountrelation);
    }

    public void delete(Integer id) {
            sysUserAgyaccountrelationMapper.deleteByPrimaryKey(id);
    }
    

}
