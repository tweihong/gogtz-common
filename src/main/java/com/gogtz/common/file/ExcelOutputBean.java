package com.gogtz.common.file;

import lombok.Data;

import java.util.List;

/**
 * 输出Excel用的bean，可以带下拉列表
 */
@Data
public class ExcelOutputBean {
    /**
     * 单元格值
     */
    private String value;
    /**
     * 下拉列表
     */
    private List<String> dataValidationList;

    public ExcelOutputBean() {

    }

    public ExcelOutputBean(String pValue) {
        value = pValue;
    }

    public ExcelOutputBean(String pValue, List<String> pDataValidationList) {
        value = pValue;
        dataValidationList = pDataValidationList;
    }
}
