/**
 * Description: Ajax用的工具类
 * Copyright: Copyright (Gogtz Corporation)2017
 * Company: Gogtz Corporation
 *
 * @author: t
 * @version: 1.0
 * Created at: 2017年7月17日15:26:21
 * Modification History:
 * Modified by :
 */
package com.gogtz.common.ajax;

import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * <p><strong>Ajax用的工具类</strong></p>
 * <ol><li> renderData：将数据输出到客户端</li>
 * <li> parseArray：将字符串转换为JSONArray</li></ol>
 *
 * @author t
 * @Time 2017年7月18日11:26:31
 */
@Slf4j
public class AjaxUtils {

    /**
     * 通过PrintWriter将字符串写入response，ajax可以接受到这个数据
     *
     * @param response 输出对象
     * @param data     要输出的内容
     */
    public static void renderData(HttpServletResponse response, String data) {
        PrintWriter printWriter = null;
        try {
            printWriter = response.getWriter();
            printWriter.print(data);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            if (null != printWriter) {
                printWriter.flush();
                printWriter.close();
            }
        }
    }

    /**
     * 通过PrintWriter将对象写入response，ajax可以接受到这个数据
     *
     * @param response 输出对象
     * @param data     要输出的内容
     */
    public static void renderDataObject(HttpServletResponse response, Object data) {
        PrintWriter printWriter = null;
        try {
            printWriter = response.getWriter();
            printWriter.print(data);
        } catch (IOException ex) {
            log.error(ex.getMessage());
        } finally {
            if (null != printWriter) {
                printWriter.flush();
                printWriter.close();
            }
        }
    }

    /**
     * 字符串转换成JSONArray
     *
     * @param str 要转换的字符串
     * @return 返回转换后的JSONArray
     */
    public static JSONArray parseArray(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        if (str.contains(",")) {
            return JSONArray.parseArray(str);
        }
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(str.replaceAll("\\[|\\]", ""));
        return jsonArray;
    }
}
