package com.wsdy.saasops.modules.base.service;

import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.base.dao.TOprNoticeMapper;
import com.wsdy.saasops.modules.base.entity.TOprNotice;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class TOprNoticeService {

    @Autowired
    private TOprNoticeMapper oprNoticeMapper;

    public List<TOprNotice> oprNoticeList() {
        TOprNotice oprNotice = new TOprNotice();
        oprNotice.setAvailable((byte) 1);
        oprNotice.setShowType(0);
        oprNotice.setNoticeType(1);
        return oprNoticeMapper.select(oprNotice);
    }

    public PageUtils queryListPage(Integer pageNo, Integer pageSize, Boolean isRead) {
        PageHelper.startPage(pageNo, pageSize);
        TOprNotice oprNotice = new TOprNotice();
        oprNotice.setAvailable((byte) 1);
        oprNotice.setNoticeType(1);
        oprNotice.setShowType(0);
        oprNotice.setIsRead(isRead);
        List<TOprNotice> list = oprNoticeMapper.select(oprNotice);
        return BeanUtil.toPagedResult(list);
    }

    public void oprNoticeRead(List<Integer> ids) {
        if (Collections3.isNotEmpty(ids)) {
            ids.forEach(id -> {
                TOprNotice oprNotice = new TOprNotice();
                oprNotice.setId(id);
                oprNotice.setIsRead(Boolean.TRUE);
                oprNoticeMapper.updateByPrimaryKeySelective(oprNotice);
            });
        }
    }
}
