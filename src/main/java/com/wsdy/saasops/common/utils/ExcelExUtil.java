package com.wsdy.saasops.common.utils;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.modules.operate.entity.TOpActtmpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * Excel 相关操作类(大数据量写入但受Excel数据行数限制)
 * 先写入Excel标题(writeExcelTitle)，再写入数据(writeExcelData)，最后释放资源(dispose)
 */
@Slf4j
public class ExcelExUtil {

    /**
     * 生成Workbook字节流: 投注记录
     * @param list
     * @return
     * @throws Exception
     */
    public static byte[] getExcelByteArray(List<Map<String, Object>> list) throws Exception {
        SXSSFWorkbook sxssfWorkbook = null;
        ByteArrayOutputStream output = null;
        try {
            // 获取workbook对象
            sxssfWorkbook = new SXSSFWorkbook(500);    // 内存存储条数
            sxssfWorkbook.setCompressTempFiles(true);//临时文件将被gzip压缩
            // 设置样式，获取样式map
            Map<String, XSSFCellStyle> cellStyleMap = styleMap(sxssfWorkbook);
            // 表头样式
            CellStyle headStyle = cellStyleMap.get("head");
            // 生成一个表格
            Sheet sheet = sxssfWorkbook.createSheet();
            // 创建第一行,作为header表头
            Row header = sheet.createRow(0);
            // 表头数据
            List<String> columnNames = new LinkedList<>();
            columnNames.add("会员名");
            columnNames.add("注单号");
            columnNames.add("投注时间");
            columnNames.add("游戏平台");
            columnNames.add("投注金额");
            columnNames.add("有效投注");
            columnNames.add("盈亏");
            columnNames.add("赔率");
            columnNames.add("游戏名称");
            columnNames.add("玩法类型");
            columnNames.add("联赛名称");
            columnNames.add("比赛队伍");
            columnNames.add("下注时比分");
            columnNames.add("开奖结果");
            columnNames.add("结算状态");
            columnNames.add("派彩时间");
            columnNames.add("输赢");
            columnNames.add("投注详情");
            // 产生表格表头列标题行
            for (int i = 0; i < columnNames.size(); i++) {
                Cell cell = header.createCell(i);
                cell.setCellStyle(headStyle);
                RichTextString text = new XSSFRichTextString(columnNames.get(i));
                cell.setCellValue(text);
            }
            // 遍历集合数据,产生数据行
            for (int rownum = 1; rownum <= list.size(); rownum++) { // 行
                // 第rownum行的数据
                Map<String, Object> dataObject = list.get(rownum - 1);
                Row row = sheet.createRow(rownum);
                for (int cellnum = 0; cellnum < columnNames.size(); cellnum++) { // 列
                    Cell contentCell = row.getCell(cellnum);
                    if (contentCell == null) {
                        contentCell = row.createCell(cellnum);
                    }

                    // 根据单元格所属,录入相应内容
                    if (cellnum == 0) {             // 会员名
                        contentCell.setCellValue(String.valueOf(dataObject.get("userName")));
                    } else if (cellnum == 1) {      // 注单号
                        contentCell.setCellValue(String.valueOf(dataObject.get("id")));
                    } else if (cellnum == 2) {      // 投注时间
                        contentCell.setCellValue(String.valueOf(dataObject.get("betTime")));
                    } else if (cellnum == 3) {      // 游戏平台
                        contentCell.setCellValue(String.valueOf(dataObject.get("platform")));
                    } else if (cellnum == 4) {      // 投注金额
                        contentCell.setCellValue(String.valueOf(dataObject.get("bet")));
                    } else if (cellnum == 5) {      // 有效投注
                        contentCell.setCellValue(String.valueOf(dataObject.get("validBet")));
                    } else if (cellnum == 6) {      // 派彩
                        contentCell.setCellValue(String.valueOf(dataObject.get("payout")));
                    } else if (cellnum == 7) {      // 赔率
                        contentCell.setCellValue(String.valueOf(dataObject.get("odds")));
                    } else if (cellnum == 8) {      // 游戏名称
                        contentCell.setCellValue(String.valueOf(dataObject.get("gameName")));
                    } else if (cellnum == 9) {      // 玩法类型
                        contentCell.setCellValue(String.valueOf(dataObject.get("playType")));
                    } else if (cellnum == 10) {      // 联赛名称
                        contentCell.setCellValue(String.valueOf(dataObject.get("leagueName")));
                    } else if (cellnum == 11) {      // 比赛队伍
                        contentCell.setCellValue(String.valueOf(dataObject.get("team")));
                    } else if (cellnum == 12) {      // 下注时比分
                        contentCell.setCellValue(String.valueOf(dataObject.get("betScore")));
                    } else if (cellnum == 13) {      // 开奖结果
                        contentCell.setCellValue(String.valueOf(dataObject.get("resultOpen")));
                    } else if (cellnum == 14) {      // 结算状态
                        contentCell.setCellValue(String.valueOf(dataObject.get("status")));
                    } else if (cellnum == 15) {      // 派彩时间
                        contentCell.setCellValue(String.valueOf(dataObject.get("payoutTime")));
                    } else if (cellnum == 16) {      // 输赢
                        contentCell.setCellValue(String.valueOf(dataObject.get("result")));
                    } else if (cellnum == 17) {      // 投注详情
                        contentCell.setCellValue(String.valueOf(dataObject.get("openResultDetail")));
                    }
                }

                // 显示置空
                dataObject.clear();
                dataObject = null;
                // 清空内存中缓存的行数
                if (rownum % 500 == 0) {
                    ((SXSSFSheet) sheet).flushRows();
                }
            }

            if(Objects.isNull(sxssfWorkbook)){
                log.error("ExcelUtil==getExcelByteArrayEx==workbook==null");
            }
            output = new ByteArrayOutputStream();
            sxssfWorkbook.write(output);
            if(Objects.isNull(output)){
                log.error("ExcelUtil==getExcelByteArrayEx==output==null" );
            }
            byte[] dataByte= output.toByteArray();
            return dataByte;
        }catch(Exception e){
            log.error("ExcelUtil==getExcelByteArrayEx==" +e);
        }finally {
            if(sxssfWorkbook != null) {
                // dispose of temporary files backing this workbook on disk -> 处 理SXSSFWorkbook导出excel时，产生的临时文件
                sxssfWorkbook.dispose();
                sxssfWorkbook.close();
                sxssfWorkbook = null;
            }
            if(output != null) {
                output.close();
            }
        }

        return null;
    }

    /**
     * 生成Workbook字节流: 投注记录
     * @param list
     * @return
     * @throws Exception
     */
    public static byte[] getExcelByteArrayActivityAudit(List<Map<String, Object>> list) throws Exception {
        SXSSFWorkbook sxssfWorkbook = null;
        ByteArrayOutputStream output = null;
        try {
            // 获取workbook对象
            sxssfWorkbook = new SXSSFWorkbook(500);    // 内存存储条数
            sxssfWorkbook.setCompressTempFiles(true);//临时文件将被gzip压缩
            // 设置样式，获取样式map
            Map<String, XSSFCellStyle> cellStyleMap = styleMap(sxssfWorkbook);
            // 表头样式
            CellStyle headStyle = cellStyleMap.get("head");
            // 生成一个表格
            Sheet sheet = sxssfWorkbook.createSheet();
            // 创建第一行,作为header表头
            Row header = sheet.createRow(0);
            // 表头数据
            List<String> columnNames = new LinkedList<>();
            columnNames.add("会员名");
            columnNames.add("所属代理");
            columnNames.add("充值金额");
            columnNames.add("赠送金额");
            columnNames.add("状态");
            columnNames.add("流水倍数");
            columnNames.add("流水金额");
            columnNames.add("申请时间");
            columnNames.add("申请人");
            columnNames.add("审核时间");
            columnNames.add("审核人");
            columnNames.add("添加时备注");
            columnNames.add("备注");
            columnNames.add("子规则名称");
            columnNames.add("是否黑名单");


            // 产生表格表头列标题行
            for (int i = 0; i < columnNames.size(); i++) {
                Cell cell = header.createCell(i);
                cell.setCellStyle(headStyle);
                RichTextString text = new XSSFRichTextString(columnNames.get(i));
                cell.setCellValue(text);
            }
            // 遍历集合数据,产生数据行
            for (int rownum = 1; rownum <= list.size(); rownum++) { // 行
                // 第rownum行的数据
                Map<String, Object> dataObject = list.get(rownum - 1);
                Row row = sheet.createRow(rownum);
                for (int cellnum = 0; cellnum < columnNames.size(); cellnum++) { // 列
                    Cell contentCell = row.getCell(cellnum);
                    if (contentCell == null) {
                        contentCell = row.createCell(cellnum);
                    }

                    // 根据单元格所属,录入相应内容
                    if (cellnum == 0) {             // 会员名
                        contentCell.setCellValue(String.valueOf(dataObject.get("loginName")));
                    } else if (cellnum == 1) {      // 所属代理
                        contentCell.setCellValue(String.valueOf(dataObject.get("agyAccount")));
                    } else if (cellnum == 2) {      // 充值金额
                        contentCell.setCellValue(String.valueOf(dataObject.get("depositedAmount")));
                    } else if (cellnum == 3) {      // 赠送金额
                        Object bonusAmount = dataObject.get("bonusAmount");
                        if(Objects.isNull(bonusAmount)){
                            contentCell.setCellValue("0.00");
                        }else{
                            contentCell.setCellValue(String.valueOf(dataObject.get("bonusAmount")));
                        }
                    } else if (cellnum == 4) {      // 状态
                        String status = String.valueOf(dataObject.get("status"));
                        if(String.valueOf(Constants.EVNumber.zero).equals(status)){
                            contentCell.setCellValue("拒绝");
                        }
                        if(String.valueOf(Constants.EVNumber.one).equals(status)){
                            contentCell.setCellValue("通过");
                        }
                        if(String.valueOf(Constants.EVNumber.two).equals(status)){
                            contentCell.setCellValue("待处理");
                        }

                    } else if (cellnum == 5) {      // 流水倍数
                        contentCell.setCellValue(String.valueOf(dataObject.get("discountAudit")));
                    } else if (cellnum == 6) {      // 流水金额
                        contentCell.setCellValue(String.valueOf(dataObject.get("auditAmount")));
                    } else if (cellnum == 7) {      // 申请时间
                        contentCell.setCellValue(String.valueOf(dataObject.get("applicationTime")));
                    } else if (cellnum == 8) {      // 申请人
                        contentCell.setCellValue(String.valueOf(dataObject.get("createUser")));
                    } else if (cellnum == 9) {      // 审核时间
                        contentCell.setCellValue(String.valueOf(dataObject.get("auditTime")));
                    } else if (cellnum == 10) {     // 审核人
                        contentCell.setCellValue(String.valueOf(dataObject.get("auditUser")));
                    } else if (cellnum == 11) {     // 添加时备注
                        contentCell.setCellValue(String.valueOf(dataObject.get("applicationMemo")));
                    } else if (cellnum == 12) {     // 审核备注
                        contentCell.setCellValue(String.valueOf(dataObject.get("memo")));
                    } else if (cellnum == 13) {     // 子规则名称
                        String subRuleTmplCode = String.valueOf(dataObject.get("subRuleTmplCode"));
                        if(TOpActtmpl.depositSentCode.equals(subRuleTmplCode)){
                            contentCell.setCellValue("存就送");
                        }
                        if(TOpActtmpl.bettingGiftCode.equals(subRuleTmplCode)){
                            contentCell.setCellValue("投就送");
                        }
                        if(TOpActtmpl.rescueCode.equals(subRuleTmplCode)){
                            contentCell.setCellValue("救援金");
                        }
                        if(TOpActtmpl.otherCode.equals(subRuleTmplCode)){
                            contentCell.setCellValue("其他");
                        }
                    }else if (cellnum == 14) {     // 是否黑名单
                        String isBlack = String.valueOf(dataObject.get("isBlack"));
                        if (isBlack.equals("0")){
                            contentCell.setCellValue("否");
                        }else{
                            contentCell.setCellValue("是");
                        }

                    }

                }

                // 显示置空
                dataObject.clear();
                dataObject = null;
                // 清空内存中缓存的行数
                if (rownum % 500 == 0) {
                    ((SXSSFSheet) sheet).flushRows();
                }
            }

            if(Objects.isNull(sxssfWorkbook)){
                log.error("ExcelUtil==getExcelByteArrayActivityAudit==workbook==null");
            }
            output = new ByteArrayOutputStream();
            sxssfWorkbook.write(output);
            if(Objects.isNull(output)){
                log.error("ExcelUtil==getExcelByteArrayActivityAudit==output==null" );
            }
            byte[] dataByte= output.toByteArray();
            return dataByte;
        }catch(Exception e){
            log.error("ExcelUtil==getExcelByteArrayActivityAudit==" +e);
        }finally {
            if(sxssfWorkbook != null) {
                // dispose of temporary files backing this workbook on disk -> 处 理SXSSFWorkbook导出excel时，产生的临时文件
                sxssfWorkbook.dispose();
                sxssfWorkbook.close();
                sxssfWorkbook = null;
            }
            if(output != null) {
                output.close();
            }
        }

        return null;
    }


    /**
     * 生成Workbook字节流: 投注记录
     * @param list
     * @return
     * @throws Exception
     */
    public static byte[] getExcelByteArrayAllActivityAudit(List<Map<String, Object>> list) throws Exception {
        SXSSFWorkbook sxssfWorkbook = null;
        ByteArrayOutputStream output = null;
        try {
            // 获取workbook对象
            sxssfWorkbook = new SXSSFWorkbook(500);    // 内存存储条数
            sxssfWorkbook.setCompressTempFiles(true);//临时文件将被gzip压缩
            // 设置样式，获取样式map
            Map<String, XSSFCellStyle> cellStyleMap = styleMap(sxssfWorkbook);
            // 表头样式
            CellStyle headStyle = cellStyleMap.get("head");
            // 生成一个表格
            Sheet sheet = sxssfWorkbook.createSheet();
            // 创建第一行,作为header表头
            Row header = sheet.createRow(0);
            // 表头数据
            List<String> columnNames = new LinkedList<>();
            columnNames.add("活动名称");
            columnNames.add("会员名");
            columnNames.add("所属代理");
            columnNames.add("充值金额");
            columnNames.add("赠送金额");
            columnNames.add("状态");
            columnNames.add("流水倍数");
            columnNames.add("流水金额");
            columnNames.add("申请时间");
            columnNames.add("申请人");
            columnNames.add("审核时间");
            columnNames.add("审核人");
            columnNames.add("添加时备注");
            columnNames.add("备注");
            columnNames.add("子规则名称");
            columnNames.add("奖品");
            columnNames.add("是否黑名单");

            // 产生表格表头列标题行
            for (int i = 0; i < columnNames.size(); i++) {
                Cell cell = header.createCell(i);
                cell.setCellStyle(headStyle);
                RichTextString text = new XSSFRichTextString(columnNames.get(i));
                cell.setCellValue(text);
            }
            // 遍历集合数据,产生数据行
            for (int rownum = 1; rownum <= list.size(); rownum++) { // 行
                // 第rownum行的数据
                Map<String, Object> dataObject = list.get(rownum - 1);
                Row row = sheet.createRow(rownum);
                for (int cellnum = 0; cellnum < columnNames.size(); cellnum++) { // 列
                    Cell contentCell = row.getCell(cellnum);
                    if (contentCell == null) {
                        contentCell = row.createCell(cellnum);
                    }

                    // 根据单元格所属,录入相应内容
                    if (cellnum == 0) {             // 活动名称
                        contentCell.setCellValue(String.valueOf(dataObject.get("activityName")));
                    }else if (cellnum == 1) {             // 会员名
                        contentCell.setCellValue(String.valueOf(dataObject.get("loginName")));
                    } else if (cellnum == 2) {      // 所属代理
                        contentCell.setCellValue(String.valueOf(dataObject.get("agyAccount")));
                    } else if (cellnum == 3) {      // 充值金额
                        contentCell.setCellValue(String.valueOf(dataObject.get("depositedAmount")));
                    } else if (cellnum == 4) {      // 赠送金额
                        Object bonusAmount = dataObject.get("bonusAmount");
                        if(Objects.isNull(bonusAmount)){
                            contentCell.setCellValue("0.00");
                        }else{
                            contentCell.setCellValue(String.valueOf(dataObject.get("bonusAmount")));
                        }
                    } else if (cellnum == 5) {      // 状态
                        String status = String.valueOf(dataObject.get("status"));
                        if(String.valueOf(Constants.EVNumber.zero).equals(status)){
                            contentCell.setCellValue("拒绝");
                        }
                        if(String.valueOf(Constants.EVNumber.one).equals(status)){
                            contentCell.setCellValue("通过");
                        }
                        if(String.valueOf(Constants.EVNumber.two).equals(status)){
                            contentCell.setCellValue("待处理");
                        }

                    } else if (cellnum == 6) {      // 流水倍数
                        contentCell.setCellValue(String.valueOf(dataObject.get("discountAudit")));
                    } else if (cellnum == 7) {      // 流水金额
                        contentCell.setCellValue(String.valueOf(dataObject.get("auditAmount")));
                    } else if (cellnum == 8) {      // 申请时间
                        contentCell.setCellValue(String.valueOf(dataObject.get("applicationTime")));
                    } else if (cellnum == 9) {      // 申请人
                        contentCell.setCellValue(String.valueOf(dataObject.get("createUser")));
                    } else if (cellnum == 10) {      // 审核时间
                        contentCell.setCellValue(String.valueOf(dataObject.get("auditTime")));
                    } else if (cellnum == 11) {     // 审核人
                        contentCell.setCellValue(String.valueOf(dataObject.get("auditUser")));
                    } else if (cellnum == 12) {     // 添加时备注
                        contentCell.setCellValue(String.valueOf(dataObject.get("applicationMemo")));
                    } else if (cellnum == 13) {     // 审核备注
                        contentCell.setCellValue(String.valueOf(dataObject.get("memo")));
                    } else if (cellnum == 14) {     // 子规则名称
                        String subRuleTmplCode = String.valueOf(dataObject.get("subRuleTmplCode"));
                        if(TOpActtmpl.depositSentCode.equals(subRuleTmplCode)){
                            contentCell.setCellValue("存就送");
                        }
                        if(TOpActtmpl.bettingGiftCode.equals(subRuleTmplCode)){
                            contentCell.setCellValue("投就送");
                        }
                        if(TOpActtmpl.rescueCode.equals(subRuleTmplCode)){
                            contentCell.setCellValue("救援金");
                        }
                        if(TOpActtmpl.otherCode.equals(subRuleTmplCode)){
                            contentCell.setCellValue("其他");
                        }
                    }else if (cellnum == 15){   //奖品
                        contentCell.setCellValue(String.valueOf(dataObject.get("prizename")));
                    }else if (cellnum == 16){   //是否黑名单
                        String isBlack = String.valueOf(dataObject.get("isBlack"));
                        if (isBlack.equals("0")){
                            contentCell.setCellValue("否");
                        }else{
                            contentCell.setCellValue("是");
                        }
                    }
                }

                // 显示置空
                dataObject.clear();
                dataObject = null;
                // 清空内存中缓存的行数
                if (rownum % 500 == 0) {
                    ((SXSSFSheet) sheet).flushRows();
                }
            }

            if(Objects.isNull(sxssfWorkbook)){
                log.error("ExcelUtil==getExcelByteArrayActivityAudit==workbook==null");
            }
            output = new ByteArrayOutputStream();
            sxssfWorkbook.write(output);
            if(Objects.isNull(output)){
                log.error("ExcelUtil==getExcelByteArrayActivityAudit==output==null" );
            }
            byte[] dataByte= output.toByteArray();
            return dataByte;
        }catch(Exception e){
            log.error("ExcelUtil==getExcelByteArrayActivityAudit==" +e);
        }finally {
            if(sxssfWorkbook != null) {
                // dispose of temporary files backing this workbook on disk -> 处 理SXSSFWorkbook导出excel时，产生的临时文件
                sxssfWorkbook.dispose();
                sxssfWorkbook.close();
                sxssfWorkbook = null;
            }
            if(output != null) {
                output.close();
            }
        }

        return null;
    }

    /**
     * 创建单元格表头样式
     *
     * @param sxssfWorkbook 工作薄
     */
    public static XSSFCellStyle createCellHeadStyle(SXSSFWorkbook sxssfWorkbook) {
        XSSFCellStyle xssfCellStyle = (XSSFCellStyle) sxssfWorkbook.createCellStyle();
        Font font = sxssfWorkbook.createFont();
        // 字体大小
        font.setFontHeightInPoints((short) 14);
        // 字体粗细
        font.setBold(true);
        // 将字体应用到样式上面
        xssfCellStyle.setFont(font);
        // 是否自动换行
        xssfCellStyle.setWrapText(false);
        // 水平居中
        xssfCellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 垂直居中
        xssfCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return xssfCellStyle;
    }

    /**
     * 创建单元格正文样式
     *
     * @param sxssfWorkbook 工作薄
     */
    public static XSSFCellStyle createCellContentStyle(SXSSFWorkbook sxssfWorkbook) {
        XSSFCellStyle xssfCellStyle = (XSSFCellStyle) sxssfWorkbook.createCellStyle();
        XSSFDataFormat format = (XSSFDataFormat)sxssfWorkbook.createDataFormat();
        // 是否自动换行
        xssfCellStyle.setWrapText(false);
        // 水平居中
        xssfCellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 边框
        xssfCellStyle.setBorderBottom(BorderStyle.THIN);
        xssfCellStyle.setBorderRight(BorderStyle.THIN);
        xssfCellStyle.setBorderTop(BorderStyle.THIN);
        xssfCellStyle.setBorderLeft(BorderStyle.THIN);
        xssfCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        xssfCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        xssfCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        xssfCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        // 垂直居中
        xssfCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 防止数字过长,excel导出后,显示为科学计数法,如:防止8615192053888被显示为8.61519E+12
        xssfCellStyle.setDataFormat(format.getFormat("0"));
        return xssfCellStyle;
    }

    /**
     * 单元格样式(Integer)列表
     */
    public static XSSFCellStyle createCellContent4IntegerStyle(Workbook sxssfWorkbook) {
        XSSFCellStyle xssfCellStyle = (XSSFCellStyle) sxssfWorkbook.createCellStyle();
        // 是否自动换行
        xssfCellStyle.setWrapText(false);
        // 水平居中
        xssfCellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 边框
        xssfCellStyle.setBorderBottom(BorderStyle.THIN);
        xssfCellStyle.setBorderRight(BorderStyle.THIN);
        xssfCellStyle.setBorderTop(BorderStyle.THIN);
        xssfCellStyle.setBorderLeft(BorderStyle.THIN);
        xssfCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        xssfCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        xssfCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        xssfCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        // 垂直居中
        xssfCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));//数据格式只显示整数
        return xssfCellStyle;
    }

    /**
     * 单元格样式(Double)列表
     */
    public static XSSFCellStyle createCellContent4DoubleStyle(SXSSFWorkbook sxssfWorkbook) {
        XSSFCellStyle xssfCellStyle = (XSSFCellStyle) sxssfWorkbook.createCellStyle();
        // 是否自动换行
        xssfCellStyle.setWrapText(false);
        // 水平居中
        xssfCellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 边框
        xssfCellStyle.setBorderBottom(BorderStyle.THIN);
        xssfCellStyle.setBorderRight(BorderStyle.THIN);
        xssfCellStyle.setBorderTop(BorderStyle.THIN);
        xssfCellStyle.setBorderLeft(BorderStyle.THIN);
        xssfCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        xssfCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        xssfCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        xssfCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        // 垂直居中
        xssfCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0.00"));//保留两位小数点
        return xssfCellStyle;
    }

    /**
     * 单元格样式列表
     */
    public static Map<String, XSSFCellStyle> styleMap(SXSSFWorkbook sxssfWorkbook) {
        Map<String, XSSFCellStyle> styleMap = new LinkedHashMap<>();
        styleMap.put("head", createCellHeadStyle(sxssfWorkbook));
//        styleMap.put("content", createCellContentStyle(sxssfWorkbook));
//        styleMap.put("integer", createCellContent4IntegerStyle(sxssfWorkbook));
//        styleMap.put("double", createCellContent4DoubleStyle(sxssfWorkbook));
        return styleMap;
    }
}