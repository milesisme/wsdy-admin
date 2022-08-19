package com.wsdy.saasops.modules.sys.service;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.wsdy.saasops.api.modules.apisys.dao.TI18nCollectMapper;
import com.wsdy.saasops.api.modules.apisys.dao.TI18nMapper;
import com.wsdy.saasops.api.modules.apisys.entity.TI18n;
import com.wsdy.saasops.api.modules.apisys.entity.TI18nCollect;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.sys.dao.SysI18nDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;


@Slf4j
@Service
@Transactional
public class SysI18nService {
    @Autowired
    private TI18nMapper t18nMapper;
    @Autowired
    private TI18nCollectMapper tI18nCollectMapper;

    @Async("i18nCollectAsyncExecutor")
    public void i18nCollect(String source){
            ThreadLocalCache.setSiteCodeAsny(null);
            log.info("i18nCollect==" + source);
            // 1.查询多语言redis里是否有该source
            TI18n record = new TI18n();
            record.setSource(source);
            List<TI18n> recordList = t18nMapper.select(record);
            // 存在则不处理
            if(Objects.nonNull(recordList) && recordList.size() > 0){
                return ;
            }

            // 2.不存在多语言翻译，则查询collect表是否有该字段
            TI18nCollect collect = new TI18nCollect();
            collect.setSource(source);
            List<TI18nCollect> collectList = tI18nCollectMapper.select(collect);
            // 存在则不处理
            if(Objects.nonNull(collectList) && collectList.size() > 0){
                return ;
            }

            // 不存在则插入一条collecg数据
            collect.setTranslateFlag(Constants.EVNumber.one);
            collect.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
            collect.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
            tI18nCollectMapper.insert(collect);
    }

    public List<SysI18nDto> getInputEntityList(MultipartFile file) {
        try {
            ImportParams params = new ImportParams();
            params.setStartSheetIndex(0);
            List<SysI18nDto> result = ExcelImportUtil.importExcel(file.getInputStream(),SysI18nDto.class, params);
            return result;
        } catch (Exception e) {
            log.info("解析文件出错", e);
        }
        return null;
    }

    @Transactional
    public void insertI18n(SysI18nDto dto){
        TI18n record = new TI18n();
        record.setSource(dto.getSource());
        record.setTranslate(dto.getTranslate());
        record.setI18nflag(dto.getI18nflag());
        t18nMapper.insert(record);
        // 显示回收
        record = null;
    }

    @Transactional
    public void insertI18nBatch(List<SysI18nDto> list){
        t18nMapper.insertI18nBatch(list);
        // 显示回收
        list.clear();
    }
}
