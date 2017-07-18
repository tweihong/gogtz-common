/**
 * Description:利用poi导出excel
 * Copyright: Copyright (gogtz Corporation)2017
 * Company: gogtz Corporation
 *
 * @author: t
 * @version: 1.0
 * Created at: 2017年7月18日10:39:00
 * Modification History:
 * Modified by :
 */
package com.gogtz.common.file;

import com.gogtz.common.string.GetterUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 输出Excel
 */
public class ExportExcel<T> implements Serializable {

    /**
     * serialVersionUID:序列化id
     */
    private static final long serialVersionUID = -5142024015171582474L;

    /**
     * 创建Excel的Sheet，只包含标题
     *
     * @param titles
     * @param sheetName
     * @return
     */
    public static HSSFSheet createHSSFWorkbookTitle(HSSFWorkbook workbook, String[] titles, String sheetName) {

        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet(sheetName);
        Row row = sheet.createRow(0);
        for (int celLength = 0; celLength < titles.length; celLength++) {
            // 创建相应的单元格
            Cell cell = row.createCell(celLength);
            cell.setCellValue(titles[celLength]);
        }

        return sheet;
    }

    /**
     * 创建导出的工作簿
     *
     * @param titles
     * @param sheetName
     * @return
     */
    public static HSSFWorkbook createHSSFWorkbookInfo(String[] titles, List<Map<String, String>> infoList, String sheetName) {
        // 生成一个表格
        //        XSSFWorkbook xBook = new XSSFWorkbook();// 2007版本用此workbook解析
        HSSFWorkbook workbook = new HSSFWorkbook();
        if (infoList == null || infoList.isEmpty()) {
            // 创建sheet
            HSSFSheet sheet = workbook.createSheet(sheetName);
            // 创建表头
            Row row = sheet.createRow(0);
            for (int celLength = 0; celLength < titles.length; celLength++) {
                // 创建相应的单元格
                Cell cell = row.createCell(celLength);
                cell.setCellValue(titles[celLength]);
            }

            return workbook;
        }

        // 数据总件数
        int listSize = infoList.size();
        // sheet总长度
        int sheetSize = listSize;
        if (listSize > 65534 || listSize < 1) {
            sheetSize = 65534;
        }
        //样式
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("@"));

        //因为2003的Excel一个工作表最多可以有65536条记录，除去列头剩下65535条
        //所以如果记录太多，需要放到多个工作表中，其实就是个分页的过程
        // 计算一共有多少个工作表
        int sheetNum = (int) Math.ceil((double) listSize / (double) sheetSize);
        for (int i = 0; i < sheetNum; i++) {
            // 行号
            int rowNumber = 0;
            // 创建sheet
            HSSFSheet sheet = workbook.createSheet(sheetName + (i + 1));

            //获取开始索引和结束索引
            int firstIndex = i * sheetSize;
            int lastIndex = (i + 1) * sheetSize > listSize ? listSize : (i + 1) * sheetSize;

            // 构建临时数据
            List<Map<String, String>> tempList = infoList.subList(firstIndex, lastIndex);

            // 创建表头
            Row row = sheet.createRow(rowNumber);
            for (int celLength = 0; celLength < titles.length; celLength++) {
                // 创建相应的单元格
                Cell cell = row.createCell(celLength);
                cell.setCellValue(titles[celLength]);
                sheet.setDefaultColumnStyle(celLength, style);
            }

            if (!tempList.isEmpty()) {
                for (Map<String, String> info : tempList) {
                    rowNumber++;
                    row = sheet.createRow(rowNumber);
                    for (int celLength = 0; celLength < titles.length; celLength++) {
                        // 创建相应的单元格
                        Cell cell = row.createCell(celLength);
                        cell.setCellValue(info.get(titles[celLength]));

                        sheet.setDefaultColumnStyle(celLength, style);
                    }
                }
            }
        }

        return workbook;
    }

    /**
     * 创建导出工作簿，可以携带下拉列表
     *
     * @param titles
     * @param sheetName
     * @return
     */
    public static HSSFWorkbook createHSSFWorkbookInfo2(String[] titles, List<Map<String, ExcelOutputBean>> infoList, String sheetName) {
        try (
                // 生成一个表格
                XSSFWorkbook xBook = new XSSFWorkbook();// 2007版本用此workbook解析
                HSSFWorkbook workbook = new HSSFWorkbook();
        ) {
            if (infoList == null || infoList.isEmpty()) {
                // 创建sheet
                HSSFSheet sheet = workbook.createSheet(sheetName);
                // 创建表头
                Row row = sheet.createRow(0);
                for (int celLength = 0; celLength < titles.length; celLength++) {
                    // 创建相应的单元格
                    Cell cell = row.createCell(celLength);
                    cell.setCellValue(titles[celLength]);
                }

                return workbook;
            }


            // 数据总件数
            int listSize = infoList.size();
            // sheet总长度
            int sheetSize = listSize;
            if (listSize > 65534 || listSize < 1) {
                sheetSize = 65534;
            }
            //样式
            CellStyle style = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            style.setDataFormat(format.getFormat("@"));

            //因为2003的Excel一个工作表最多可以有65536条记录，除去列头剩下65535条
            //所以如果记录太多，需要放到多个工作表中，其实就是个分页的过程
            // 计算一共有多少个工作表
            int sheetNum = (int) Math.ceil((double) listSize / (double) sheetSize);
            for (int i = 0; i < sheetNum; i++) {
                // 行号
                int rowNumber = 0;
                // 创建sheet
                HSSFSheet sheet = workbook.createSheet(sheetName + (i + 1));

                //获取开始索引和结束索引
                int firstIndex = i * sheetSize;
                int lastIndex = (i + 1) * sheetSize > listSize ? listSize : (i + 1) * sheetSize;

                // 构建临时数据
                List<Map<String, ExcelOutputBean>> tempList = infoList.subList(firstIndex, lastIndex);

                // 创建表头
                Row row = sheet.createRow(rowNumber);
                for (int celLength = 0; celLength < titles.length; celLength++) {
                    // 创建相应的单元格
                    Cell cell = row.createCell(celLength);
                    cell.setCellValue(titles[celLength]);
                    sheet.setDefaultColumnStyle(celLength, style);
                }

                if (!tempList.isEmpty()) {
                    for (Map<String, ExcelOutputBean> info : infoList) {
                        rowNumber++;
                        row = sheet.createRow(rowNumber);
                        for (int celLength = 0; celLength < titles.length; celLength++) {
                            String key = titles[celLength];
                            // 创建相应的单元格
                            Cell cell = row.createCell(celLength);
                            if (info.get(key) != null) {
                                cell.setCellValue(GetterUtil.getString(info.get(key).getValue()));
                            }
                            // 默认样式
                            sheet.setDefaultColumnStyle(celLength, style);

                            List<String> dataValidationList = (List<String>) info.get(key).getDataValidationList();
                            if (dataValidationList != null && !dataValidationList.isEmpty()) {
                                String[] strs = (String[]) dataValidationList.toArray(new String[dataValidationList.size()]);
                                // 设置下拉列表
                                CellRangeAddressList regions = new CellRangeAddressList(cell.getRowIndex(), cell.getRowIndex(), cell.getColumnIndex(), cell.getColumnIndex());
                                // 创建下拉列表数据
                                DVConstraint constraint = DVConstraint.createExplicitListConstraint(strs);
                                // 绑定
                                HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                                sheet.addValidationData(dataValidation);
                            }
                        }
                    }
                }
            }

            return workbook;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 创建导出工作簿
     *
     * @param titles
     * @param sheetName
     * @return
     */
    public static SXSSFWorkbook createSXSSFWorkbookInfo(String[] titles, List<Map<String, String>> infoList, String sheetName) {
        // 生成一个表格
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);// 2007版本用此workbook解析
        //        HSSFWorkbook workbook = new HSSFWorkbook();
        if (infoList == null || infoList.isEmpty()) {
            // 创建sheet
            SXSSFSheet sheet = workbook.createSheet(sheetName);
            // 创建表头
            Row row = sheet.createRow(0);
            for (int celLength = 0; celLength < titles.length; celLength++) {
                // 创建相应的单元格
                Cell cell = row.createCell(celLength);
                cell.setCellValue(titles[celLength]);
            }

            return workbook;
        }

        // 数据总件数
        int listSize = infoList.size();
        // sheet总长度
        int sheetSize = listSize;
        if (listSize > 65534 || listSize < 1) {
            sheetSize = 65534;
        }
        //样式
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("@"));

        //因为2003的Excel一个工作表最多可以有65536条记录，除去列头剩下65535条
        //所以如果记录太多，需要放到多个工作表中，其实就是个分页的过程
        // 计算一共有多少个工作表
        int sheetNum = (int) Math.ceil((double) listSize / (double) sheetSize);
        for (int i = 0; i < sheetNum; i++) {
            // 行号
            int rowNumber = 0;
            // 创建sheet
            SXSSFSheet sheet = workbook.createSheet(sheetName + (i + 1));

            //获取开始索引和结束索引
            int firstIndex = i * sheetSize;
            int lastIndex = (i + 1) * sheetSize > listSize ? listSize : (i + 1) * sheetSize;

            // 构建临时数据
            List<Map<String, String>> tempList = infoList.subList(firstIndex, lastIndex);

            // 创建表头
            Row row = sheet.createRow(rowNumber);
            for (int celLength = 0; celLength < titles.length; celLength++) {
                // 创建相应的单元格
                Cell cell = row.createCell(celLength);
                cell.setCellValue(titles[celLength]);
                sheet.setDefaultColumnStyle(celLength, style);
            }

            if (!tempList.isEmpty()) {
                for (Map<String, String> info : tempList) {
                    rowNumber++;
                    row = sheet.createRow(rowNumber);
                    for (int celLength = 0; celLength < titles.length; celLength++) {
                        // 创建相应的单元格
                        Cell cell = row.createCell(celLength);
                        cell.setCellValue(info.get(titles[celLength]));

                        sheet.setDefaultColumnStyle(celLength, style);
                    }
                }
            }
        }

        return workbook;
    }


    /**
     * 输出附件
     *
     * @param fileName
     * @return
     */
    public static void writeExcelFile(HttpServletRequest request, HttpServletResponse response, Workbook workbook, String fileName) {
        // 根据列名填充相应的数据
        try (ServletOutputStream out = response.getOutputStream();) {
            response.reset();
            response.setContentType("application/msexcel;charset=utf-8");
            //response.setHeader("content-disposition", "attachment;filename=" + new String((fileName).getBytes("UTF-8"), "ISO8859-1"));
            String userAgent = request.getHeader("user-agent");
            if (userAgent != null && userAgent.indexOf("Edge") >= 0) {
                fileName = URLEncoder.encode(fileName, "UTF8");
            } else if (userAgent.indexOf("Firefox") >= 0 || userAgent.indexOf("Chrome") >= 0
                    || userAgent.indexOf("Safari") >= 0) {
                fileName = new String((fileName).getBytes("UTF-8"), "ISO8859-1");
            } else {
                fileName = URLEncoder.encode(fileName, "UTF8"); //其他浏览器
            }
            response.setHeader("content-disposition", "attachment;filename=" + fileName);

            workbook.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}