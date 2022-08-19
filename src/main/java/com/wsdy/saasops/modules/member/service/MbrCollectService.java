package com.wsdy.saasops.modules.member.service;

import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.modules.member.dao.MbrCollectMapper;
import com.wsdy.saasops.modules.member.entity.MbrCollect;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.sys.entity.SysMenuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class MbrCollectService {

    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private MbrCollectMapper collectMapper;

    public List<SysMenuEntity> findAccountMenuByRoleId(int roleId) {
        return mbrMapper.findAccountMenuByRoleId(roleId);
    }

    public List<MbrCollect> findCollectList(Long userId, Integer roleId) {
        return mbrMapper.findCollectList(userId, roleId);
    }

    public void collectInsert(Long userId, List<Long> menuIds) {
        MbrCollect collect = new MbrCollect();
        collect.setUserId(userId);
        collectMapper.delete(collect);
        if (Collections3.isNotEmpty(menuIds)) {
            List<MbrCollect> collects = menuIds.stream().map(d -> {
                MbrCollect mbrCollect = new MbrCollect();
                mbrCollect.setUserId(userId);
                mbrCollect.setMenuId(d);
                return mbrCollect;
            }).collect(Collectors.toList());
            collectMapper.insertList(collects);
        }
    }
}
