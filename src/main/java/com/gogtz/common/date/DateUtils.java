/**
 * Description: 时间共通类
 * Copyright: Copyright (Gogtz Corporation)2017
 * Company: Gogtz Corporation
 *
 * @author: t
 * @version: 1.0
 * Created at: 2017年7月17日15:26:21
 * Modification History:
 * Modified by :
 */
package com.gogtz.common.date;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 时间共通类
 *
 * @author: t
 * @version: 1.0
 */
public class DateUtils {

    /**
     * 获取当前时间
     *
     * @return
     */
    public static Date date() {
        return new Date();
    }

    /**
     * integer转换为日期
     *
     * @param date
     * @return
     */
    public static Date date(Integer date) {
        return new Date(date * 1000);
    }

    /**
     * long转换为日期
     *
     * @param millis
     * @return
     */
    public static Date date(long millis) {
        return new Date(millis);
    }

    /**
     * string转换为日期。失败返回null。
     *
     * @param date
     * @return
     */
    public static Date date(String date) {
        EnumDateStyle EnumDateStyle = null;
        return date(date, EnumDateStyle);
    }

    /**
     * string转换为日期。失败返回null。
     *
     * @param date
     * @param enumDateStyle
     * @return
     */
    public static Date date(String date, EnumDateStyle enumDateStyle) {
        Date myDate = null;
        if (enumDateStyle == null) {
            List<Long> timestamps = new ArrayList<Long>();
            for (EnumDateStyle style : EnumDateStyle.values()) {
                Date dateTmp = date(date, style.getValue());
                if (dateTmp != null) {
                    timestamps.add(dateTmp.getTime());
                }
            }
            myDate = getAccurateDate(timestamps);
        } else {
            myDate = date(date, enumDateStyle.getValue());
        }
        return myDate;
    }

    /**
     * string转换为日期。失败返回null。
     *
     * @param date     日期字符串
     * @param parttern 日期格式
     * @return 日期
     */
    public static Date date(String date, String parttern) {
        Date myDate = null;
        if (date != null) {
            try {
                myDate = getDateFormat(parttern).parse(date);
            } catch (Exception e) {
            }
        }
        return myDate;
    }

    /**
     * 获取SimpleDateFormat
     *
     * @param parttern 日期格式
     * @return SimpleDateFormat对象
     * @throws RuntimeException 异常：非法日期格式
     */
    public static SimpleDateFormat getDateFormat(String parttern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(parttern);
        simpleDateFormat.setLenient(false);
        return simpleDateFormat;
    }

    /**
     * 获取日期中的某数值。如获取月份
     *
     * @param date     日期
     * @param dateType 日期格式
     * @return 数值
     */
    private static int getInteger(Date date, int dateType) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(dateType);
    }

    /**
     * 增加日期中某类型的某数值。如增加日期
     *
     * @param date     日期字符串
     * @param dateType 类型
     * @param amount   数值
     * @return 计算后日期字符串
     */
    private static String addInteger(String date, int dateType, int amount) {
        String dateString = null;
        EnumDateStyle EnumDateStyle = getEnumDateStyle(date);
        if (EnumDateStyle != null) {
            Date myDate = date(date, EnumDateStyle);
            myDate = addInteger(myDate, dateType, amount);
            dateString = format(myDate, EnumDateStyle);
        }
        return dateString;
    }

    /**
     * 增加日期中某类型的某数值。如增加日期
     *
     * @param date     日期
     * @param dateType 类型
     * @param amount   数值
     * @return 计算后日期
     */
    private static Date addInteger(Date date, int dateType, int amount) {
        Date myDate = null;
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(dateType, amount);
            myDate = calendar.getTime();
        }
        return myDate;
    }

    /**
     * 获取精确的日期
     *
     * @param timestamps 时间long集合
     * @return 日期
     */
    private static Date getAccurateDate(List<Long> timestamps) {
        Date date = null;
        long timestamp = 0;
        Map<Long, long[]> map = new HashMap<Long, long[]>();
        List<Long> absoluteValues = new ArrayList<Long>();

        if (timestamps != null && timestamps.size() > 0) {
            if (timestamps.size() > 1) {
                for (int i = 0; i < timestamps.size(); i++) {
                    for (int j = i + 1; j < timestamps.size(); j++) {
                        long absoluteValue = Math.abs(timestamps.get(i) - timestamps.get(j));
                        absoluteValues.add(absoluteValue);
                        long[] timestampTmp = {timestamps.get(i), timestamps.get(j)};
                        map.put(absoluteValue, timestampTmp);
                    }
                }

                // 有可能有相等的情况。如2012-11和2012-11-01。时间戳是相等的
                long minAbsoluteValue = -1;
                if (!absoluteValues.isEmpty()) {
                    // 如果timestamps的size为2，这是差值只有一个，因此要给默认值
                    minAbsoluteValue = absoluteValues.get(0);
                }
                for (int i = 0; i < absoluteValues.size(); i++) {
                    for (int j = i + 1; j < absoluteValues.size(); j++) {
                        if (absoluteValues.get(i) > absoluteValues.get(j)) {
                            minAbsoluteValue = absoluteValues.get(j);
                        } else {
                            minAbsoluteValue = absoluteValues.get(i);
                        }
                    }
                }

                if (minAbsoluteValue != -1) {
                    long[] timestampsLastTmp = map.get(minAbsoluteValue);
                    if (absoluteValues.size() > 1) {
                        timestamp = Math.max(timestampsLastTmp[0], timestampsLastTmp[1]);
                    } else if (absoluteValues.size() == 1) {
                        // 当timestamps的size为2，需要与当前时间作为参照
                        long dateOne = timestampsLastTmp[0];
                        long dateTwo = timestampsLastTmp[1];
                        if ((Math.abs(dateOne - dateTwo)) < 100000000000L) {
                            timestamp = Math.max(timestampsLastTmp[0], timestampsLastTmp[1]);
                        } else {
                            long now = new Date().getTime();
                            if (Math.abs(dateOne - now) <= Math.abs(dateTwo - now)) {
                                timestamp = dateOne;
                            } else {
                                timestamp = dateTwo;
                            }
                        }
                    }
                }
            } else {
                timestamp = timestamps.get(0);
            }
        }

        if (timestamp != 0) {
            date = new Date(timestamp);
        }
        return date;
    }

    /**
     * 判断字符串是否为日期字符串
     *
     * @param date 日期字符串
     * @return true or false
     */
    public static boolean isDate(String date) {
        boolean isDate = false;
        if (date != null) {
            if (date(date) != null) {
                isDate = true;
            }
        }
        return isDate;
    }

    /**
     * 获取日期字符串的日期风格。失敗返回null。
     *
     * @param date 日期字符串
     * @return 日期风格
     */
    public static EnumDateStyle getEnumDateStyle(String date) {
        EnumDateStyle EnumDateStyle = null;
        Map<Long, EnumDateStyle> map = new HashMap<Long, EnumDateStyle>();
        List<Long> timestamps = new ArrayList<Long>();
        for (EnumDateStyle style : EnumDateStyle.values()) {
            Date dateTmp = date(date, style.getValue());
            if (dateTmp != null) {
                timestamps.add(dateTmp.getTime());
                map.put(dateTmp.getTime(), style);
            }
        }
        EnumDateStyle = map.get(getAccurateDate(timestamps).getTime());
        return EnumDateStyle;
    }

    /**
     * 将日期转化为日期字符串。失败返回null。
     *
     * @param date     日期
     * @param parttern 日期格式
     * @return 日期字符串
     */
    public static String format(Date date, String parttern) {
        String dateString = null;
        if (date != null) {
            try {
                dateString = getDateFormat(parttern).format(date);
            } catch (Exception e) {
            }
        }
        return dateString;
    }

    /**
     * 将日期转化为日期字符串。失败返回null。
     *
     * @param date          日期
     * @param EnumDateStyle 日期风格
     * @return 日期字符串
     */
    public static String format(Date date, EnumDateStyle EnumDateStyle) {
        String dateString = null;
        if (EnumDateStyle != null) {
            dateString = format(date, EnumDateStyle.getValue());
        }
        return dateString;
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date     旧日期字符串
     * @param parttern 新日期格式
     * @return 新日期字符串
     */
    public static String format(String date, String parttern) {
        return format(date, null, parttern);
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date          旧日期字符串
     * @param EnumDateStyle 新日期风格
     * @return 新日期字符串
     */
    public static String format(String date, EnumDateStyle EnumDateStyle) {
        return format(date, null, EnumDateStyle);
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date         旧日期字符串
     * @param olddParttern 旧日期格式
     * @param newParttern  新日期格式
     * @return 新日期字符串
     */
    public static String format(String date, String olddParttern, String newParttern) {
        String dateString = null;
        if (olddParttern == null) {
            EnumDateStyle style = getEnumDateStyle(date);
            if (style != null) {
                Date myDate = date(date, style.getValue());
                dateString = format(myDate, newParttern);
            }
        } else {
            Date myDate = date(date, olddParttern);
            dateString = format(myDate, newParttern);
        }
        return dateString;
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date             旧日期字符串
     * @param olddDteStyle     旧日期风格
     * @param newEnumDateStyle 新日期风格
     * @return 新日期字符串
     */
    public static String format(String date, EnumDateStyle olddDteStyle, EnumDateStyle newEnumDateStyle) {
        String dateString = null;
        if (olddDteStyle == null) {
            EnumDateStyle style = getEnumDateStyle(date);
            dateString = format(date, style.getValue(), newEnumDateStyle.getValue());
        } else {
            dateString = format(date, olddDteStyle.getValue(), newEnumDateStyle.getValue());
        }
        return dateString;
    }

    /**
     * 增加日期的年份。失败返回null。
     *
     * @param date       日期
     * @param yearAmount 增加数量。可为负数
     * @return 增加年份后的日期字符串
     */
    public static String addYear(String date, int yearAmount) {
        return addInteger(date, Calendar.YEAR, yearAmount);
    }

    /**
     * 增加日期的年份。失败返回null。
     *
     * @param date       日期
     * @param yearAmount 增加数量。可为负数
     * @return 增加年份后的日期
     */
    public static Date addYear(Date date, int yearAmount) {
        return addInteger(date, Calendar.YEAR, yearAmount);
    }

    /**
     * 增加日期的月份。失败返回null。
     *
     * @param date       日期
     * @param yearAmount 增加数量。可为负数
     * @return 增加月份后的日期字符串
     */
    public static String addMonth(String date, int yearAmount) {
        return addInteger(date, Calendar.MONTH, yearAmount);
    }

    /**
     * 增加日期的月份。失败返回null。
     *
     * @param date       日期
     * @param yearAmount 增加数量。可为负数
     * @return 增加月份后的日期
     */
    public static Date addMonth(Date date, int yearAmount) {
        return addInteger(date, Calendar.MONTH, yearAmount);
    }

    /**
     * 增加日期的天数。失败返回null。
     *
     * @param date      日期字符串
     * @param dayAmount 增加数量。可为负数
     * @return 增加天数后的日期字符串
     */
    public static String addDay(String date, int dayAmount) {
        return addInteger(date, Calendar.DATE, dayAmount);
    }

    /**
     * 增加日期的天数。失败返回null。
     *
     * @param date      日期
     * @param dayAmount 增加数量。可为负数
     * @return 增加天数后的日期
     */
    public static Date addDay(Date date, int dayAmount) {
        return addInteger(date, Calendar.DATE, dayAmount);
    }

    /**
     * 增加日期的小时。失败返回null。
     *
     * @param date       日期字符串
     * @param hourAmount 增加数量。可为负数
     * @return 增加小时后的日期字符串
     */
    public static String addHour(String date, int hourAmount) {
        return addInteger(date, Calendar.HOUR_OF_DAY, hourAmount);
    }

    /**
     * 增加日期的小时。失败返回null。
     *
     * @param date       日期
     * @param hourAmount 增加数量。可为负数
     * @return 增加小时后的日期
     */
    public static Date addHour(Date date, int hourAmount) {
        return addInteger(date, Calendar.HOUR_OF_DAY, hourAmount);
    }

    /**
     * 增加日期的分钟。失败返回null。
     *
     * @param date       日期字符串
     * @param hourAmount 增加数量。可为负数
     * @return 增加分钟后的日期字符串
     */
    public static String addMinute(String date, int hourAmount) {
        return addInteger(date, Calendar.MINUTE, hourAmount);
    }

    /**
     * 增加日期的分钟。失败返回null。
     *
     * @param date       日期
     * @param hourAmount 增加数量。可为负数
     * @return 增加分钟后的日期
     */
    public static Date addMinute(Date date, int hourAmount) {
        return addInteger(date, Calendar.MINUTE, hourAmount);
    }

    /**
     * 增加日期的秒钟。失败返回null。
     *
     * @param date       日期字符串
     * @param hourAmount 增加数量。可为负数
     * @return 增加秒钟后的日期字符串
     */
    public static String addSecond(String date, int hourAmount) {
        return addInteger(date, Calendar.SECOND, hourAmount);
    }

    /**
     * 增加日期的秒钟。失败返回null。
     *
     * @param date       日期
     * @param hourAmount 增加数量。可为负数
     * @return 增加秒钟后的日期
     */
    public static Date addSecond(Date date, int hourAmount) {
        return addInteger(date, Calendar.SECOND, hourAmount);
    }

    /**
     * 获取日期的年份。失败返回0。
     *
     * @param date 日期字符串
     * @return 年份
     */
    public static int getYear(String date) {
        return getYear(date(date));
    }

    /**
     * 获取日期的年份。失败返回0。
     *
     * @param date 日期
     * @return 年份
     */
    public static int getYear(Date date) {
        return getInteger(date, Calendar.YEAR);
    }

    /**
     * 获取日期的月份。失败返回0。
     *
     * @param date 日期字符串
     * @return 月份
     */
    public static int getMonth(String date) {
        return getMonth(date(date));
    }

    /**
     * 获取日期的月份。失败返回0。
     *
     * @param date 日期
     * @return 月份
     */
    public static int getMonth(Date date) {
        return getInteger(date, Calendar.MONTH);
    }

    /**
     * 获取日期的天数。失败返回0。
     *
     * @param date 日期字符串
     * @return 天
     */
    public static int getDay(String date) {
        return getDay(date(date));
    }

    /**
     * 获取日期的天数。失败返回0。
     *
     * @param date 日期
     * @return 天
     */
    public static int getDay(Date date) {
        return getInteger(date, Calendar.DATE);
    }

    /**
     * 获取日期的小时。失败返回0。
     *
     * @param date 日期字符串
     * @return 小时
     */
    public static int getHour(String date) {
        return getHour(date(date));
    }

    /**
     * 获取日期的小时。失败返回0。
     *
     * @param date 日期
     * @return 小时
     */
    public static int getHour(Date date) {
        return getInteger(date, Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取日期的分钟。失败返回0。
     *
     * @param date 日期字符串
     * @return 分钟
     */
    public static int getMinute(String date) {
        return getMinute(date(date));
    }

    /**
     * 获取日期的分钟。失败返回0。
     *
     * @param date 日期
     * @return 分钟
     */
    public static int getMinute(Date date) {
        return getInteger(date, Calendar.MINUTE);
    }

    /**
     * 获取日期的秒钟。失败返回0。
     *
     * @param date 日期字符串
     * @return 秒钟
     */
    public static int getSecond(String date) {
        return getSecond(date(date));
    }

    /**
     * 获取日期的秒钟。失败返回0。
     *
     * @param date 日期
     * @return 秒钟
     */
    public static int getSecond(Date date) {
        return getInteger(date, Calendar.SECOND);
    }

    /**
     * 获取日期 。默认yyyy-MM-dd格式。失败返回null。
     *
     * @param date 日期字符串
     * @return 日期
     */
    public static String getDate(String date) {
        return format(date, EnumDateStyle.YYYY_MM_DD);
    }

    /**
     * 获取日期。默认yyyy-MM-dd格式。失败返回null。
     *
     * @param date 日期
     * @return 日期
     */
    public static String getDate(Date date) {
        return format(date, EnumDateStyle.YYYY_MM_DD);
    }

    /**
     * 获取日期的时间。默认HH:mm:ss格式。失败返回null。
     *
     * @param date 日期字符串
     * @return 时间
     */
    public static String getTime(String date) {
        return format(date, EnumDateStyle.HH_MM_SS);
    }

    /**
     * 获取日期的时间。默认HH:mm:ss格式。失败返回null。
     *
     * @param date 日期
     * @return 时间
     */
    public static String getTime(Date date) {
        return format(date, EnumDateStyle.HH_MM_SS);
    }

    /**
     * 获取日期的星期。失败返回null。
     *
     * @param date 日期字符串
     * @return 星期
     */
    public static EnumWeek getEnumWeek(String date) {
        EnumWeek week = null;
        EnumDateStyle EnumDateStyle = getEnumDateStyle(date);
        if (EnumDateStyle != null) {
            Date myDate = date(date, EnumDateStyle);
            week = getEnumWeek(myDate);
        }
        return week;
    }

    /**
     * 获取日期的星期。失败返回null。
     *
     * @param date 日期
     * @return 星期
     */
    public static EnumWeek getEnumWeek(Date date) {
        EnumWeek week = null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int weekNumber = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        switch (weekNumber) {
            case 0:
                week = EnumWeek.SUNDAY;
                break;
            case 1:
                week = EnumWeek.MONDAY;
                break;
            case 2:
                week = EnumWeek.TUESDAY;
                break;
            case 3:
                week = EnumWeek.WEDNESDAY;
                break;
            case 4:
                week = EnumWeek.THURSDAY;
                break;
            case 5:
                week = EnumWeek.FRIDAY;
                break;
            case 6:
                week = EnumWeek.SATURDAY;
                break;
        }
        return week;
    }

    /**
     * 获取两个日期相差的天数
     *
     * @param date      日期字符串
     * @param otherDate 另一个日期字符串
     * @return 相差天数
     */
    public static int getIntervalDays(String date, String otherDate) {
        return getIntervalDays(date(date), date(otherDate));
    }

    /**
     * @param date      日期
     * @param otherDate 另一个日期
     * @return 相差天数
     */
    public static int getIntervalDays(Date date, Date otherDate) {
        date = DateUtils.date(DateUtils.getDate(date));
        long time = Math.abs(date.getTime() - otherDate.getTime());
        return (int) time / (24 * 60 * 60 * 1000);
    }

    /**
     * 返回天的开始时间
     *
     * @param dateStr
     * @return
     */
    public static String getDayStart(String dateStr) {
        return dateStr + " 00:00:00";
    }

    /**
     * 返回天的结束时间
     *
     * @param dateStr
     * @return
     */
    public static String getDayEnd(String dateStr) {
        return dateStr + " 23:59:59";
    }

    /**
     * 获得指定日期所在的自然周的第一天，即周日
     *
     * @param date 日期
     * @return 自然周的第一天
     */
    public static Date getStartDayOfWeek(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, 1);
        date = c.getTime();
        return date;
    }

    /**
     * 获得指定日期所在的自然周的最后一天，即周六
     *
     * @param date
     * @return
     */
    public static Date getLastDayOfWeek(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, 7);
        date = c.getTime();
        return date;
    }

    /**
     * 获得指定日期所在当月第一天
     *
     * @param date
     * @return
     */
    public static Date getStartDayOfMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, 1);
        date = c.getTime();
        return date;
    }

    /**
     * 获得指定日期所在当月最后一天
     *
     * @param date
     * @return
     */
    public static Date getLastDayOfMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DATE, 1);
        c.add(Calendar.MONTH, 1);
        c.add(Calendar.DATE, -1);
        c.set(Calendar.HOUR, 23);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MINUTE, 59);
        date = c.getTime();
        return date;
    }

    /**
     * 获得指定日期的下一个月的第一天
     *
     * @param date
     * @return
     */
    public static Date getStartDayOfNextMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, 1);
        c.set(Calendar.DAY_OF_MONTH, 1);
        date = c.getTime();
        return date;
    }

    /**
     * 获得指定日期的下一个月的最后一天
     *
     * @param date
     * @return
     */
    public static Date getLastDayOfNextMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DATE, 1);
        c.add(Calendar.MONTH, 2);
        c.add(Calendar.DATE, -1);
        date = c.getTime();
        return date;
    }

    /**
     * 把String 日期转换成long型日期
     *
     * @param date   String 型日期；
     * @param format 日期格式；
     * @return
     */
    public static long stringToLong(String date, String format) {
        long lTime = 0;
        if (StringUtils.isNotBlank(date)) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date dt2 = null;
            try {
                dt2 = sdf.parse(date);
                // 继续转换得到秒数的long型
                lTime = dt2.getTime() / 1000;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return lTime;
    }

    /**
     * 把Integer 日期转换成String型日期
     *
     * @param date          Integer 型日期；
     * @param enumDateStyle 日期格式；
     * @return
     */
    public static String integerToString(Integer date, EnumDateStyle enumDateStyle) {
        if (date == null || date <= 0) {
            return "";
        }
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(Long.valueOf(date) * 1000L);
        return format(c.getTime(), enumDateStyle);
    }

    /**
     * 把Integer 日期转换成String型日期
     *
     * @param date     Integer 型日期；
     * @param parttern 日期格式；
     * @return
     */
    public static String integerToString(Integer date, String parttern) {
        if (date == null || date <= 0) {
            return "";
        }
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(Long.valueOf(date) * 1000L);
        return format(c.getTime(), parttern);
    }

    /**
     * 把String 日期转换成time型日期
     *
     * @param date          String 型日期；
     * @param enumDateStyle 日期格式；
     * @return
     */
    public static String stringToTime(String date, EnumDateStyle enumDateStyle) {
        String ret = "";
        Date dt2 = date(date, enumDateStyle);
        // 继续转换得到秒数的long型
        ret = String.valueOf(dt2.getTime());

        return ret;
    }

    /**
     * 获取指定日期类型的字符串时间
     *
     * @param Kind
     * @param currentTime
     * @return
     */
    public static String getServerDateTime(int Kind, Date currentTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String strDate = formatter.format(currentTime);
        StringTokenizer token = new StringTokenizer(strDate, "-");
        String year = token.nextToken();
        String month = token.nextToken();
        String day = token.nextToken();
        String hh = token.nextToken();
        String mm = token.nextToken();
        String ss = token.nextToken();
        String strServerDateTime = "";
        switch (Kind) {
            case 1:
                strServerDateTime = year + month + day;
                break;
            case 2:
                strServerDateTime = year + "-" + month + "-" + day + " " + hh;
                break;
            case 3:
                strServerDateTime = hh + mm + ss;
                break;
            case 4:
                strServerDateTime = hh + ":" + mm + ":" + ss;
                break;
            case 5:
                strServerDateTime = year + month + day + " " + hh + mm + ss;
                break;
            case 6:
                strServerDateTime = year + "-" + month + "-" + day + " " + hh + ":" + mm + ":" + ss;
                break;
            case 7:
                strServerDateTime = year + "-" + month + "-" + day + "|" + hh + ":" + mm + ":" + ss;
                break;
            case 8:
                strServerDateTime = year + month + day + hh + mm + ss;
                break;
            case 9:
                strServerDateTime = year + "-" + month + "-" + day + " " + hh + ":" + mm + ":" + ss;
                break;
            case 10:
                strServerDateTime = year + "-" + month + "-" + day;
                break;
            case 11:
                strServerDateTime = month + "月" + day + "日";
                break;
            default:
                break;
        }
        return strServerDateTime;
    }

    /**
     * 获取服务器时间（精确到毫秒,返回string）
     *
     * @return
     */
    public static String nowLong() {
        return String.valueOf(new Date().getTime());
    }

    /**
     * 获取服务器时间（精确到秒,返回string）
     *
     * @return
     */
    public static String nowInt() {
        return String.valueOf(new Date().getTime() / 1000);
    }

    /**
     * 计算相对于dateToCompare的年龄，长用于计算指定生日在某年的年龄
     *
     * @param birthDay      生日
     * @param dateToCompare 需要对比的日期
     * @return 年龄
     */
    public static int age(Date birthDay, Date dateToCompare) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateToCompare);

        if (cal.before(birthDay)) {
            throw new IllegalArgumentException(String.format("Birthday is after date {}!", format(dateToCompare, EnumDateStyle.YYYY_MM_DD_EN)));
        }

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

        cal.setTime(birthDay);
        int age = year - cal.get(Calendar.YEAR);

        int monthBirth = cal.get(Calendar.MONTH);
        if (month == monthBirth) {
            int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
            if (dayOfMonth < dayOfMonthBirth) {
                // 如果生日在当月，但是未达到生日当天的日期，年龄减一
                age--;
            }
        } else if (month < monthBirth) {
            // 如果当前月份未达到生日的月份，年龄计算减一
            age--;
        }

        return age;
    }

    /**
     * 是否闰年
     *
     * @param year 年
     * @return 是否闰年
     */
    public static boolean isLeapYear(int year) {
        return new GregorianCalendar().isLeapYear(year);
    }

    public static void main(String[] args) {
        System.out.println(DateUtils.integerToString((int) (new Date().getTime() / 1000), "ddMMyyyy HH:mm:ss"));
    }
}