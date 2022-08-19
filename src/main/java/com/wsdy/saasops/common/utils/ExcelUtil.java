package com.wsdy.saasops.common.utils;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class ExcelUtil {

    public static Workbook commonExcelExportList(String mapKey,String templatePath, List<Map<String, Object>> list) {
        TemplateExportParams params = new TemplateExportParams(templatePath, true);
        Map<String, Object> map = new HashMap<>(16);
        map.put(mapKey, list);
        Workbook workbook = ExcelExportUtil.exportExcel(params, map);
        return workbook;
    }

    public static void writeExcel(HttpServletResponse response, Workbook work, String fileName) throws IOException {
        OutputStream out = null;
        try {
            out = response.getOutputStream();
            response.setContentType("application/ms-excel;charset=UTF-8");
            response.setHeader("Content-Disposition","attachment;filename=".concat(String.valueOf(URLEncoder.encode(fileName + ".xls", "UTF-8"))));
            work.write(out);
        } catch (Exception e) {
            log.error("ExcelUtil==writeExcel==" + e);
        } finally {
            out.close();
        }

    }

    public static byte[] getExcelByteArray( String mapKey,String templatePath, List<Map<String, Object>> list) throws IOException {
        Workbook workbook = null;
        ByteArrayOutputStream output = null;
        try {
            workbook = commonExcelExportList(mapKey, templatePath, list);
            output = new ByteArrayOutputStream();
            workbook.write(output);
            byte[] dataByte= output.toByteArray();
            return dataByte;
        }catch(IOException e){
            log.error("ExcelUtil==getExcelByteArray==" + e);
        }finally {
            if(workbook !=null){
                workbook.close();
            }
            output.close();
        }
        return null;
    }

    public static byte[] getExcelByteArrayEx( String mapKey,String templatePath, List<Map<String, Object>> list) throws Exception {
        Workbook workbook = null;
        ByteArrayOutputStream output = null;
        try {
            log.error("ExcelUtil==getExcelByteArrayEx==start");
            // 获取workbook对象
            workbook = commonExcelExportList(mapKey, templatePath, list);
            if(Objects.isNull(workbook)){
                log.error("ExcelUtil==getExcelByteArrayEx==workbook==null");
            }
            output = new ByteArrayOutputStream();
            workbook.write(output);
            if(Objects.isNull(output)){
                log.error("ExcelUtil==getExcelByteArrayEx==output==null" );
            }
            byte[] dataByte= output.toByteArray();
            return dataByte;
        }catch(Exception e){
            log.error("ExcelUtil==getExcelByteArrayEx==" +e);
        }finally {
            if(workbook !=null){
                workbook.close();
            }
            output.close();
        }
        return null;
    }

    public static byte[] getExcelByteArray( String templatePath, Map<String,Object> data) throws IOException {
        Workbook workbook = null;
        ByteArrayOutputStream output = null;
        try {
            workbook = excelExportList(templatePath, data);
            output = new ByteArrayOutputStream();
            workbook.write(output);
            byte[] dataByte= output.toByteArray();
            return dataByte;
        }catch(IOException e){
            log.error("ExcelUtil==getExcelByteArray2==" + e);
        }finally {
            if(workbook !=null){
                workbook.close();
            }
            output.close();
        }
        return null;
    }

    public static byte[] excelExportMulSheet(List<Map<String, Object>> sheetsList) throws IOException {
        Workbook workbook = null;
        ByteArrayOutputStream output = null;
        try {
            output = new ByteArrayOutputStream();
            workbook = ExcelExportUtil.exportExcel(sheetsList, ExcelType.HSSF);
            workbook.write(output);
            byte[] dataByte = output.toByteArray();
            return dataByte;
        }
        catch (IOException e) {
            log.error("ExcelUtil==getExcelByteArray2==" + e);
        } catch (Exception ex){
            log.error("ExcelUtil==getExcelByteArray2==" + ex);
        }finally {
            if (workbook != null) {
                workbook.close();
            }
            output.close();
        }
        return null;
    }

    public static Workbook excelExportList(String templatePath, Map<String,Object> data) {
        TemplateExportParams params = new TemplateExportParams(templatePath, true);
        Workbook workbook = ExcelExportUtil.exportExcel(params, data);
        return workbook;
    }
}
