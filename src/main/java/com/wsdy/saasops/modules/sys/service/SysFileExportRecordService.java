package com.wsdy.saasops.modules.sys.service;

import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.sys.dao.SysFileExportRecordMapper;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.beans.Transient;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

@Slf4j
@Service
public class SysFileExportRecordService extends BaseService<SysFileExportRecordMapper, SysFileExportRecord> {

    private final String activityAuditListExportModule = "activityAuditListExport";
    private final String allActivityAuditListExportModule = "allActivityAuditListExport";
    @Autowired
    SysFileExportRecordMapper sysFileExportRecordMapper;
    @Autowired
    MbrMapper mbrMapper;
    @Autowired
    private QiNiuYunUtil qiNiuYunUtil;
    @Autowired
    private RedisService redisService;
    @Autowired
    private MbrAccountLogService accountLogService;

    @Transient
    public SysFileExportRecord saveAsynFileExportRecord(Long loginUserId, String module) {
        String fileId = module + loginUserId;
        String key = RedisConstants.EXCEL_EXPORT + CommonUtil.getSiteCode() + fileId;
        Boolean isExpired =  true;
               // redisService.setRedisExpiredTimeBo(key, fileId, 10, TimeUnit.MINUTES);
        log.info("saveAsynFileExportRecord==isExpired==isExpired" + isExpired);
        log.info("saveAsynFileExportRecord==key" + key);
        if (isExpired) {
            // 查询历史上传记录
            SysFileExportRecord hisRecord = sysFileExportRecordMapper.getAsynFileExportRecordByUserId(loginUserId, module);
            // 存在上传记录，删除记录并删除七牛云上的文件
            if (null != hisRecord) {
                // 删除记录
                deleteById(hisRecord.getId());
                if (StringUtil.isNotEmpty(hisRecord.getFileName())) {
                    // 同步删除七牛云上的文件
                    qiNiuYunUtil.deleteFile(hisRecord.getFileName());
                }
            }

            // 插入文件上传记录
            SysFileExportRecord asynFileExportRecord = new SysFileExportRecord();
            asynFileExportRecord.setUserId(loginUserId);
            asynFileExportRecord.setModule(module);
            asynFileExportRecord.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
            if (save(asynFileExportRecord) > 0) {
                log.info("saveAsynFileExportRecord==key==" + key);
                return asynFileExportRecord;
            }
        }
        log.info("saveAsynFileExportRecord==isExpired==key" + key);
        return null;
    }

    @Transient
    public SysFileExportRecord saveAsynFileExportRecordEx(Long loginUserId, String module) {
        log.info("export==userId==" + loginUserId + "==module==" + module + "==保存记录==start");
        Long startTime = System.currentTimeMillis();

        String fileId = module;
        String key = RedisConstants.EXCEL_EXPORT + CommonUtil.getSiteCode() + fileId;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, fileId, 10, TimeUnit.SECONDS);  // TODO
        if (isExpired) {
            // 查询历史上传记录
            SysFileExportRecord hisRecord = sysFileExportRecordMapper.getAsynFileExportRecordByUserId(loginUserId, module);
            // 存在上传记录，删除记录并删除七牛云上的文件
            if (null != hisRecord) {
                // 删除记录
                deleteById(hisRecord.getId());
                if (StringUtil.isNotEmpty(hisRecord.getFileName())) {
                    // 异步删除七牛云上的文件，如果删除异常没删掉也不管，不影响后续
                    qiNiuYunUtil.deleteFileAsync(hisRecord.getFileName());
                }
            }
            // 插入文件上传记录
            SysFileExportRecord asynFileExportRecord = new SysFileExportRecord();
            asynFileExportRecord.setUserId(loginUserId);
            asynFileExportRecord.setModule(module);
            asynFileExportRecord.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
            if (save(asynFileExportRecord) > 0) {
                log.info("export==userId==" + loginUserId + "==module==" + module + "==保存新的记录成功==time==" + (System.currentTimeMillis() - startTime));
                asynFileExportRecord.setSaveFlag("success");
                return asynFileExportRecord;
            }
            // 没有保存新的记录成功
            asynFileExportRecord.setSaveFlag("fail");
            redisService.del(key);
            log.info("export==userId==" + loginUserId + "==module==" + module + "==没有保存新的记录成功");
        }
        log.info("export==userId==" + loginUserId + "==module==" + module + "==isExpired==未过期/处理中");
        return null;
    }

    @Transient
    public int updateFileId(Long userId, String module, String fileId) {
        return sysFileExportRecordMapper.updateFileId(userId, module, fileId);
    }

    public SysFileExportRecord getAsynFileExportRecordByUserId(Long userId, String module) {
        return sysFileExportRecordMapper.getAsynFileExportRecordByUserId(userId, module);
    }

    public void downloadFile(HttpServletResponse response, String fileId, String fileName) {
        OutputStream out = null;
        InputStream in = null;
        try {
            response.reset();
            out = response.getOutputStream();
            response.setContentType("application/ms-excel");
            response.setCharacterEncoding("UTF-8");
            //            response.setHeader("Content-Disposition", "attachment;filename=" + new String((fileName).getBytes("UTF-8"), "iso-8859-1"));
            response.setHeader("Content-Disposition", "attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            in = qiNiuYunUtil.downLoadFile(fileId);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
            // 记录日志
            accountLogService.downloadFileLog(fileName);
        } catch (IOException e) {
            throw new R200Exception("文件下载失败!");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (null != out) {
                    out.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }


    /**
     * 异步生成excel，并上传文件： 简单excel模板，List<Map<String,Object>> list
     *
     * @param tempPath
     * @param list
     * @param userId
     * @param module
     * @param siteCode
     */
    @Async
    public void exportExcel(String tempPath, List<Map<String, Object>> list, Long userId, String module, String siteCode) {
        ThreadLocalCache.setSiteCodeAsny(siteCode);
        try {
            byte[] excelByteArray = ExcelUtil.getExcelByteArray("mapList", tempPath, list);
            String fileId = qiNiuYunUtil.uploadFileKey(excelByteArray);
            updateFileId(userId, module, fileId);
        } catch (IOException e) {
            log.error("exportExcel==" + e.getMessage());
        } finally {
            String key = RedisConstants.EXCEL_EXPORT + siteCode + module + userId;
            redisService.del(key);
        }
    }


    /**
     * 同步生成excel，并上传文件： 简单excel模板，List<Map<String,Object>> list
     *
     * @param list
     * @param userId
     * @param module
     * @param siteCode
     */
    public void exportExcelSyn(Long userId, List<Map<String, Object>> list, String module, String siteCode) {
        try {
            log.info("export==userId==" + userId + "==module==" + module + "==执行上传文件==list==" + list.size());
            Long zjstart = System.currentTimeMillis();
            // 生成excel字节流
            byte[] excelByteArray = ExcelExUtil.getExcelByteArray(list);
            log.info("export==userId==" + userId + "==module==" + module + "==执行上传文件==excelByteArray==" + excelByteArray.length);
            Long zjend = System.currentTimeMillis();
            long r = zjend - zjstart;
            log.info("生成字节耗时:" + r);

            long qis = System.currentTimeMillis();
            // 上传文件
            String fileId = qiNiuYunUtil.uploadFileKey(excelByteArray);
            log.info("export==userId==" + userId + "==module==" + module + "==执行上传文件==fileId==" + fileId);
            updateFileId(userId, module, fileId);
            long qie = System.currentTimeMillis();
            long k = qie - qis;
            log.info("上传七牛耗时:" + k);
        } catch (Exception e) {
            log.error("export==userId==" + userId + "==module==" + module + "==执行上传文件==error==" + e);
        } finally {
            String key = RedisConstants.EXCEL_EXPORT + siteCode + module;
            redisService.del(key);
        }
    }

    /**
     * 同步生成excel，并上传文件： 简单excel模板，List<Map<String,Object>> list
     *
     * @param userId
     * @param module
     * @param siteCode
     */
    public void exportExcelSynNew(Long userId, byte[] excelByteArray, String module, String siteCode) {
        try {
            long startTime = System.currentTimeMillis();
            String fileId = qiNiuYunUtil.uploadFileKey(excelByteArray);
            long endTime = System.currentTimeMillis();
            log.info("betDetailsExportExcelAsyn 文件上传七牛云耗时 " + (endTime - startTime) / 1000 + "秒");

            updateFileId(userId, module, fileId);
        } catch (Exception e) {
            log.error("exportExcelSynNew==userId==" + userId + "==module==" + module + "==执行上传文件==error==" + e);
        } finally {
            String key = RedisConstants.EXCEL_EXPORT + siteCode + module;
            redisService.del(key);
        }
    }


    /**
     * 同步生成excel，并上传文件： 简单excel模板，List<Map<String,Object>> list
     *
     * @param list
     * @param userId
     * @param module
     * @param siteCode
     */
    public void exportExcelSynByModule(Long userId, List<Map<String, Object>> list, String module, String siteCode) {
        if (activityAuditListExportModule.equals(module)) {    // 红利列表导出
            activityAuditListExport(userId, list, module, siteCode);
        }
        if (allActivityAuditListExportModule.equals(module)){
            allActivityAuditListExport(userId, list, module, siteCode);
        }
    }

    public void activityAuditListExport(Long userId, List<Map<String, Object>> list, String module, String siteCode) {
        try {
            log.info("export==userId==" + userId + "==module==" + module + "==执行上传文件==list==" + list.size());
            // 生成excel字节流
            byte[] excelByteArray = ExcelExUtil.getExcelByteArrayActivityAudit(list);
            log.info("export==userId==" + userId + "==module==" + module + "==执行上传文件==excelByteArray==" + excelByteArray.length);
            // 上传文件
            String fileId = qiNiuYunUtil.uploadFileKey(excelByteArray);
            log.info("export==userId==" + userId + "==module==" + module + "==执行上传文件==fileId==" + fileId);
            updateFileId(userId, module, fileId);
        } catch (Exception e) {
            log.error("export==userId==" + userId + "==module==" + module + "==执行上传文件==error==" + e);
        } finally {
//            String key = RedisConstants.EXCEL_EXPORT + siteCode +module+userId;
            String key = RedisConstants.EXCEL_EXPORT + siteCode + module;
            redisService.del(key);
        }
    }
    public void allActivityAuditListExport(Long userId, List<Map<String, Object>> list, String module, String siteCode) {
        try {
            log.info("export==userId==" + userId + "==module==" + module + "==执行上传文件==list==" + list.size());
            // 生成excel字节流
            byte[] excelByteArray = ExcelExUtil.getExcelByteArrayAllActivityAudit(list);
            log.info("export==userId==" + userId + "==module==" + module + "==执行上传文件==excelByteArray==" + excelByteArray.length);
            // 上传文件
            String fileId = qiNiuYunUtil.uploadFileKey(excelByteArray);
            log.info("export==userId==" + userId + "==module==" + module + "==执行上传文件==fileId==" + fileId);
            updateFileId(userId, module, fileId);
        } catch (Exception e) {
            log.error("export==userId==" + userId + "==module==" + module + "==执行上传文件==error==" + e);
        } finally {
//            String key = RedisConstants.EXCEL_EXPORT + siteCode +module+userId;
            String key = RedisConstants.EXCEL_EXPORT + siteCode + module;
            redisService.del(key);
        }
    }
    /**
     * 异步生成excel，并上传文件： 复杂excel模板，Map<String,Object> data， 需在map中定义mapList
     *
     * @param tempPath
     * @param data
     * @param userId
     * @param module
     * @param siteCode
     */
    @Async
    public void exportExcel(String tempPath, Map<String, Object> data, Long userId, String module, String siteCode) {
        log.info("exportExcel==start==");
        ThreadLocalCache.setSiteCodeAsny(siteCode);
        try {
            log.info("exportExcel==start==getExcelByteArray==start");
            byte[] excelByteArray = ExcelUtil.getExcelByteArray(tempPath, data);
            log.info("exportExcel==start==uploadFileKey==start");
            String fileId = qiNiuYunUtil.uploadFileKey(excelByteArray);
            log.info("exportExcel==start==updateFileId==start");
            updateFileId(userId, module, fileId);
            log.info("exportExcel==start==updateFileId==end");
        } catch (IOException e) {
            log.error("exportExcel==", e);
        } finally {
            String key = RedisConstants.EXCEL_EXPORT + siteCode + module + userId;
            redisService.del(key);
        }
        log.info("exportExcel==end==");
    }


    @Async
    public void exportMilSheet(List<Map<String, Object>> sheetsList, Long userId, String module, String siteCode){
        log.info("exportMilSheet==start==");
        ThreadLocalCache.setSiteCodeAsny(siteCode);
        try {
            byte[] excelByteArray = ExcelUtil.excelExportMulSheet(sheetsList);
            log.info("exportMilSheet==start==uploadFileKey==start");
            String fileId = qiNiuYunUtil.uploadFileKey(excelByteArray);
            log.info("exportMilSheet==start==updateFileId==start");
            updateFileId(userId, module, fileId);
            log.info("exportMilSheet==start==updateFileId==end");
        }catch (IOException e) {
        log.info("exportMilSheet==" + e);
        } finally {
        String key = RedisConstants.EXCEL_EXPORT + siteCode + module + userId;
        redisService.del(key);
         }
        log.info("exportMilSheet==end==");
    }

}
