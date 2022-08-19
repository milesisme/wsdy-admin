package com.wsdy.saasops.modules.system.msgtemple.service;

import org.springframework.util.StringUtils;

import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.wsdy.saasops.modules.system.msgtemple.dao.SystemMsgmoldetypeMapper;
import com.wsdy.saasops.modules.system.msgtemple.entity.SystemMsgmoldetype;


@Service
public class SystemMsgmoldetypeService {
    @Autowired
    private SystemMsgmoldetypeMapper systemMsgmoldetypeMapper;

    public SystemMsgmoldetype queryObject(Integer id) {
        return systemMsgmoldetypeMapper.selectByPrimaryKey(id);
    }

    public PageUtils queryListPage(SystemMsgmoldetype systemMsgmoldetype, Integer pageNo, Integer pageSize,String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if(!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        List<SystemMsgmoldetype> list = systemMsgmoldetypeMapper.selectAll();
        return BeanUtil.toPagedResult(list);
    }
    public List<SystemMsgmoldetype> queryListAll() {
        List<SystemMsgmoldetype> list = systemMsgmoldetypeMapper.selectAll();
        return list;
    }

    public void save(SystemMsgmoldetype systemMsgmoldetype) {
            systemMsgmoldetypeMapper.insert(systemMsgmoldetype);
    }

    public void update(SystemMsgmoldetype systemMsgmoldetype) {
            systemMsgmoldetypeMapper.updateByPrimaryKeySelective(systemMsgmoldetype);
    }

    public void delete(Integer id) {
            systemMsgmoldetypeMapper.deleteByPrimaryKey(id);
    }

    public void deleteBatch(Integer[]ids) {
        //systemMsgmoldetypeMapper.deleteBatch(ids);
    }

}
