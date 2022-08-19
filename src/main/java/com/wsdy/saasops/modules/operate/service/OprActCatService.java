package com.wsdy.saasops.modules.operate.service;

import java.util.List;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.operate.dao.OprActCatActivityMapper;
import com.wsdy.saasops.modules.operate.entity.OprActCatActivity;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.operate.dao.OprActCatMapper;
import com.wsdy.saasops.modules.operate.entity.OprActCat;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class OprActCatService extends BaseService<OprActCatMapper, OprActCat> {

    @Autowired
    private OprActCatMapper oprActCatMapper;
    @Autowired
    private OprActCatActivityMapper catActivityMapper;
    @Autowired
    private OperateActivityMapper operateActivityMapper;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;


    public List<OprActCat> findOprActCatList() {
        return operateActivityMapper.findOprActCatList();
    }

    public List<OprActCat> queryListPage() {
        return operateActivityMapper.findOprActCatListPage();
    }

    public void delete(Integer id) {
        OprActCatActivity catActivity = new OprActCatActivity();
        catActivity.setCatId(id);
        int count = catActivityMapper.selectCount(catActivity);
        if (count > 0) {
            throw new R200Exception("分类下面有活动，不能删除");
        }
        oprActCatMapper.deleteByPrimaryKey(id);
    }

    public void updateOprActCat(OprActCat oprActCat) {
        checkoutAvailable(oprActCat);
        oprActCatMapper.updateByPrimaryKeySelective(oprActCat);

        // 操作日志
        mbrAccountLogService.updateOprActCat(oprActCat);
    }

    public void updateAvailable(OprActCat oprActCat) {
        checkoutAvailable(oprActCat);
        OprActCat actCat = new OprActCat();
        actCat.setId(oprActCat.getId());
        actCat.setAvailable(oprActCat.getAvailable());
        actCat.setDisable(oprActCat.getDisable());
        oprActCatMapper.updateByPrimaryKeySelective(actCat);
    }

    private void checkoutAvailable(OprActCat oprActCat){
        if (oprActCat.getAvailable() == Constants.EVNumber.zero) {
            OprActCatActivity actActivity = new OprActCatActivity();
            actActivity.setCatId(oprActCat.getId());
            int count = catActivityMapper.selectCount(actActivity);
            if (count > 0) {
                throw new R200Exception("分类下面有活动，不能禁用");
            }
        }
    }
}