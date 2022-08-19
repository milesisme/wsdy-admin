package com.wsdy.saasops.modules.agent.service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.api.modules.apisys.service.TGmApiService;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.agent.dao.AgentMaterialDetailMapper;
import com.wsdy.saasops.modules.agent.dao.AgentMaterialMapper;
import com.wsdy.saasops.modules.agent.entity.AgentMaterial;
import com.wsdy.saasops.modules.agent.entity.AgentMaterialDetail;
import com.wsdy.saasops.modules.agent.mapper.MaterialMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@Transactional
public class AgentMaterialService {

    @Autowired
    private AgentMaterialMapper agentMaterialMapper;
    @Autowired
    private AgentMaterialDetailMapper materialDetailMapper;
    @Autowired
    private MaterialMapper materialMapper;
    @Autowired
    private QiNiuYunUtil qiNiuYunUtil;
    @Autowired
    private TGmApiService tGmApiService;

    public PageUtils materialList(AgentMaterial material, Integer pageNo, Integer pageSize) {
        if (StringUtils.isEmpty(material.getName())) {
            PageHelper.startPage(pageNo, pageSize);
            List<AgentMaterial> list = materialMapper.materialList(material);
            return BeanUtil.toPagedResult(list);
        }
        AgentMaterialDetail detail = new AgentMaterialDetail();
        detail.setName(material.getName());
        return materialDetailList(detail, pageNo, pageSize);
    }

    public void addMaterial(AgentMaterial material) {
        material.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        material.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        agentMaterialMapper.insert(material);
    }

    public void updateMaterial(AgentMaterial material) {
        material.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        agentMaterialMapper.updateByPrimaryKeySelective(material);
    }

    public void deleteMaterial(AgentMaterial material) {
        for (Integer id : material.getIds()) {
            material.setId(id);
            AgentMaterialDetail detail = new AgentMaterialDetail();
            detail.setMaterialId(material.getId());
            List<AgentMaterialDetail> materialDetails = materialDetailMapper.select(detail);
            if (Collections3.isNotEmpty(materialDetails)) {
                materialDetails.stream().forEach(ms -> deleteMaterialDetail(ms));
            }
            agentMaterialMapper.deleteByPrimaryKey(material.getId());
        }
    }

    public PageUtils materialDetailList(AgentMaterialDetail detail, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<AgentMaterialDetail> list = materialMapper.materialDetailList(detail);
        if (Collections3.isNotEmpty(list)) {
            list.stream().forEach(ls -> {
                if (StringUtils.isNotEmpty(ls.getFileName())) {
                    ls.setFileName(tGmApiService.queryGiniuyunUrl() + ls.getFileName());
                }
            });
        }
        return BeanUtil.toPagedResult(list);
    }

    public void addMaterialDetail(AgentMaterialDetail detail, MultipartFile uploadFile) {
        if (nonNull(uploadFile)) {
            try {
                byte[] fileBuff = IOUtils.toByteArray(uploadFile.getInputStream());
                String fileName = qiNiuYunUtil.uploadFileKey(fileBuff);
                detail.setFileName(fileName);
            } catch (Exception e) {
                log.error("addMaterialDetail", e);
                throw new RRException("上传图片出错");
            }
        }
        detail.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        detail.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        materialDetailMapper.insert(detail);
    }

    public void deleteMaterialDetail(AgentMaterialDetail detail) {
        for (Integer id : detail.getIds()) {
            detail.setId(id);
            AgentMaterialDetail materialDetail = materialDetailMapper.selectByPrimaryKey(detail.getId());
            if (nonNull(materialDetail)) {
                if (StringUtil.isNotEmpty(materialDetail.getFileName())) {
                    qiNiuYunUtil.deleteFile(materialDetail.getFileName());
                }
                materialDetailMapper.deleteByPrimaryKey(detail.getId());
            }
        }
    }
}

