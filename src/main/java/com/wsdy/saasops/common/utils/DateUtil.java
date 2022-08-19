
package com.wsdy.saasops.common.utils;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.RRException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 日期时间工具类
 */
@Slf4j
public class DateUtil {

    private static final int step = 3;
    private static final int fullCursor = 2;
    private static final int fullIndex = 4 * 5;
    private static final String MATCH_TEMPLATE = "yyyy/MM/dd HH:mm:ss:SSS";

    private static final String PATTERN_TEMPLATE = "0000/00/00 00:00:00:000";

    public static final String FORMAT_26_DATE_TIME = "yyyy/MM/dd HH:mm:ss";
    public static final String FORMAT_25_DATE_TIME = "yyyy-MM-dd HH:mm:ss.SSSSSS";
    public static final String FORMAT_22_DATE_TIME = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String FORMAT_18_DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_18_DATE_TIME2 = "yyyyMMddHHmmss";
    public static final String FORMAT_12_DATE_TIME = "yyyy-MM-dd HH";
    public static final String FORMAT_10_DATE = "yyyy-MM-dd";
    public static final String FORMAT_6_DATE = "yyyy-MM";
    public static final String FORMAT_DD_DATE = "dd";
    public static final String FORMAT_5_DATE = "MM-dd";
    public static final String FORMAT_8_DATE = "yyyyMMdd";
    public static final String FORMAT_8_TIME = "HH:mm:ss";
    public static final String FORMAT_DATE_T = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final String DATE_STATE = "00:00:00";
    public static final String DATE_END = "23:59:59";
    public static final String FORMAT_4_DATE = "yyyy";
    private static String suffix = ".000Z";

    private static final ThreadLocal<SimpleDateFormat> local = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat();
        }
    };
    
    /**
     * 	yyyyMMdd 转时间字符串
     * 
     * @param yyyyMMdd
     * @return
     * @throws ParseException
     */
    public static String yyyyMMddToStrStart(String yyyyMMdd) throws ParseException {
    	return new SimpleDateFormat(FORMAT_10_DATE).format(new SimpleDateFormat(FORMAT_8_DATE).parse(yyyyMMdd)) + " " + DATE_STATE;
    }
    
    public static String yyyyMMddToStrEnd(String yyyyMMdd) throws ParseException {
    	return new SimpleDateFormat(FORMAT_10_DATE).format(new SimpleDateFormat(FORMAT_8_DATE).parse(yyyyMMdd)) + " " + DATE_END;
    }
    
    /**
     * 获取SimpleDateFormat实例
     *
     * @param pattern 模式串
     * @return
     */
    public static SimpleDateFormat getSimpleDateFormat(String pattern) {
        SimpleDateFormat format = local.get();
        format.applyPattern(pattern);
        return format;
    }

    /**
     * 获取当前日期向前推某个月的时间
     *
     * @param month
     * @return
     */
    public static Date getBeforeDate(Integer month) {
        Integer m = 0 - month;
        Date dNow = new Date();   //当前时间
        Date dBefore = new Date();
        Calendar calendar = Calendar.getInstance(); //得到日历
        calendar.setTime(dNow);//把当前时间赋给日历
        calendar.add(Calendar.MONTH, m);  //设置为前多少月
        dBefore = calendar.getTime();   //得到前多少月的时间
        return dBefore;
    }

    /**
     * 获取两个日期之间所有日期的集合
     *
     * @param dBegin
     * @param dEnd
     * @return
     */
    public static List<Date> findDates(Date dBegin, Date dEnd) {
        List<Date> lDate = new ArrayList<Date>();
        lDate.add(dBegin);
        Calendar calBegin = Calendar.getInstance();
        calBegin.setTime(dBegin);
        Calendar calEnd = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        calEnd.setTime(dEnd);
        // 测试此日期是否在指定日期之后
        while (dEnd.after(calBegin.getTime())) {
            // 根据日历的规则，为给定的日历字段添加或减去指定的时间量
            calBegin.add(Calendar.DAY_OF_MONTH, 1);
            lDate.add(calBegin.getTime());
        }
        return lDate;
    }


    /**
     * 获取本周 周一的日期
     *
     * @param pattern
     * @return
     */
    public static String getMonday(String pattern) {
        SimpleDateFormat sf = new SimpleDateFormat(pattern, Locale.CHINA);
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return sf.format(calendar.getTime());
    }

    /**
     * 获取本周 周日的日期
     *
     * @param pattern
     * @return
     */
    public static String getWeek(String pattern) {
        SimpleDateFormat sf = new SimpleDateFormat(pattern, Locale.CHINA);
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return sf.format(calendar.getTime());
    }

    /**
     * 获取过去第几天的日期
     *
     * @param past
     * @return
     */
    public static String getPastDate(int past, String pattern) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(today);
    }


    /**
     * 获取表示当前时间的字符串
     *
     * @param pattern 模式串
     * @return
     */
    public static String getCurrentDate(String pattern) {
        return format(new Date(), pattern);
    }

    public static String getTodayStart(String pattern) {
        return format(new Date(), pattern) + " " + DATE_STATE;
    }

    public static String getTodayEnd(String pattern) {
        return format(new Date(), pattern) + " " + DATE_END;
    }

    public static String getTodayStartWithoutTime(String pattern) {
        return format(new Date(), pattern);
    }

    public static String getTodayEndWithoutTime(String pattern) {
        return format(new Date(), pattern);
    }

    /**
     * 获取表示当前美东时间
     *
     * @return
     */
    public static String getAmericaDate(String pattern, Date date) {
        SimpleDateFormat sf = new SimpleDateFormat(pattern);
        sf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        return sf.format(date);
    }

    /**
     * 日期时间格式化, 自动匹配格式化模式串
     *
     * @param date Date
     * @return
     */
    public static String format(Date date) {
        SimpleDateFormat format = getSimpleDateFormat(FORMAT_22_DATE_TIME);
        String dateStr = format.format(date);
        String ms = dateStr.substring(dateStr.length() - step);
        if (Short.parseShort(ms) == 0) {
            dateStr = dateStr.substring(0, dateStr.length() - step - 1);
        } else {
            return dateStr;
        }
        String time = dateStr.substring(dateStr.length() - step * 3 + 1);
        time = time.replace(":", "");
        if (Integer.parseInt(time) == 0) {
            dateStr = dateStr.substring(0, dateStr.length() - step * 3);
        } else {
            return dateStr;
        }
        return dateStr;
    }

    /**
     * 日期时间格式化
     *
     * @param date    Date
     * @param pattern 模式串
     * @return
     */
    public static String format(Date date, String pattern) {
        try {
            SimpleDateFormat format = getSimpleDateFormat(pattern);
            return format.format(date);
        } catch (Exception e) {
            log.error("format error:", e);
        }
        return null;
    }

    
    /**
     * 日期时间格式化转字符串
     *
     * @return
     */
    public static String formatDate(Date date) {
    	if (date == null) {
    		return null;
    	}
		SimpleDateFormat sdf =   new SimpleDateFormat(FORMAT_18_DATE_TIME); 
		return  sdf.format(date); 
    }

    /**
     * 日期时间格式化
     *
     * @param dateStr    String
     * @param pattern 模式串
     * @return
     */
    public static String format(String dateStr, String pattern) {
        try {
            Date date = parse(dateStr, FORMAT_18_DATE_TIME);
            SimpleDateFormat format = getSimpleDateFormat(pattern);
            return format.format(date);
        } catch (Exception e) {
            log.error("format error:", e);
        }
        return null;
    }

    /**
     * 解析字符串类型日期, 为参数自动匹配解析模式串
     *
     * @param date 日期字符串
     * @return
     */
    public static Date parse(String date) {
        try {
            String[] mapper = format(date);
            SimpleDateFormat format = getSimpleDateFormat(mapper[0]);
            return format.parse(mapper[1]);
        } catch (ParseException e) {
            throw new RRException("Unparseable date: \"" + date + "\"");
        } catch (Throwable e) {
            throw new RRException(e.getMessage());
        }
    }

    /**
     * 解析字符串类型日期
     *
     * @param date    日期字符串
     * @param pattern 模式串
     * @return
     */
    public static Date parse(String date, String pattern) {
        try {
            SimpleDateFormat format = getSimpleDateFormat(pattern);
            return format.parse(date);
        } catch (Throwable e) {
            throw new RRException(e.getMessage());
        }
    }

    /**
     * 格式化参数日期字符串, 源日期字符填充到预设模板
     *
     * @param date 日期字符串
     * @return
     */
    private static String[] format(String date) {
        char[] origin = date.toCharArray();
        char[] pattern = PATTERN_TEMPLATE.toCharArray();
        char o, p;
        int cursor = 0, j = 0;
        for (int i = 0; i < origin.length; i++, j++) {
            o = origin[i];
            p = pattern[j];
            if (isCursor(o)) {
                if (!isCursor(p)) {
                    moveToNext(pattern, j - 1);
                    j++;
                }
                cursor = j;
            }
            if (isCursor(p)) {
                if (!isCursor(o)) {
                    cursor = j;
                    j++;
                }
            }
            pattern[j] = o;
        }
        j--;
        if (cursor < fullIndex - 1 && j - cursor == 1) {
            moveToNext(pattern, j);
        }
        cursor = pattern.length;
        for (int i = 0; i < fullCursor; i++) {
            if (pattern[cursor - 1] == '0') {
                cursor--;
            } else {
                break;
            }
        }
        if (cursor != pattern.length) {
            char[] target = new char[cursor];
            System.arraycopy(pattern, 0, target, 0, cursor);
            pattern = target;
        }
        char[] match = MATCH_TEMPLATE.toCharArray();
        for (int i = 4; i < fullIndex; i += step) {
            match[i] = pattern[i];
        }
        return new String[]{new String(match), new String(pattern)};
    }

    /**
     * 字符是否为非数值
     *
     * @param ch 被测试的字符
     * @return
     */
    private static boolean isCursor(char ch) {
        if (ch >= '0' && ch <= '9') {
            return false;
        }
        return true;
    }

    /**
     * 后移元素
     *
     * @param pattern 数组
     * @param i       被移动的元素的索引值
     */
    private static void moveToNext(char[] pattern, int i) {
        pattern[i + 1] = pattern[i];
        pattern[i] = '0';
    }

    public static Integer subtractTime(Date expireTime) {
        if (StringUtils.isEmpty(expireTime)) {
            return 0;
        }
        Long second = (expireTime.getTime() - (new Date()).getTime()) / 1000;
        if (second <= 0) {
            return 0;
        } else {
            return new Integer(String.valueOf(second));
        }
    }

    public static Date addTime(Integer second) {
        return new Date(System.currentTimeMillis() + second * 1000);
    }


    /**
     * 求现在n小时前的时间
     *
     * @param hour
     * @return
     */
    public static String getBrforeTime(Date d, Integer hour) {
        DateFormat df = new SimpleDateFormat(FORMAT_18_DATE_TIME);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.HOUR_OF_DAY, -hour);
        return df.format(cal.getTime());
    }

    /**
     * 日期时间格式化 特定获取有效投注
     *
     * @param date
     * @return
     */
    public static String formatEsDate(String date) {
        SimpleDateFormat format = getSimpleDateFormat(FORMAT_18_DATE_TIME);
        String da = format.format(parse(date, FORMAT_18_DATE_TIME));
        return da.replace(" ", "T") + suffix;
    }

    /**
     * 获取两个日期之间相隔日期的数组，包含开始和结束
     * 间隔不超过30天
     * @return
     */
    public static List<String> getListDateBetween(String startDate, String endDate) {
        List<String> result = new ArrayList<>();
        Date start = parse(startDate, FORMAT_10_DATE);
        Date end = parse(endDate, FORMAT_10_DATE);
        String first = format(start, FORMAT_10_DATE);
        result.add(first);
        Date lastNext = start;
        while (true) {
            lastNext = addHours(lastNext, 24);
            if (lastNext.before(end)) {
                result.add(format(lastNext, FORMAT_10_DATE));
            } else {
                break;
            }
        }
        if (start.before(end)) {
            result.add(format(end, FORMAT_10_DATE));
        }
        return result;
    }

    /**
     * 把ES时间转为yyyy-mm-dd hh:mm:ss时间
     *
     * @param esDate
     * @return
     */
    public static String formatEsDateToTime(String esDate) {
        return esDate.replace("T", " ").replace(suffix, "");
    }

    /**
     * 得到几天前的时间
     *
     * @param d
     * @param day
     * @return
     */
    public static Date getDateBefore(Date d, int day) {
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
        return now.getTime();
    }

    /**
     * 得到几天后的时间
     *
     * @param d
     * @param day
     * @return
     */
    public static Date getDateAfter(Date d, int day) {
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.DATE) + day);
        return now.getTime();
    }

    public static String getUTCTimeStr() {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        StringBuffer UTCTimeBuffer = new StringBuffer();
        // 1、取得本地时间：
        Calendar cal = Calendar.getInstance();
        // 2、取得时间偏移量：
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
        // 3、取得夏令时差：
        int dstOffset = cal.get(Calendar.DST_OFFSET);
        // 4、从本地时间里扣除这些差量，即可以取得UTC时间：
        cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        int millisecond = cal.get(Calendar.MILLISECOND);
        UTCTimeBuffer.append(year).append("-").append(month).append("-").append(day);
        UTCTimeBuffer.append(" ").append(hour).append(":").append(minute).append(":").append(second).append(".")
                .append(millisecond);
        try {
            format.parse(UTCTimeBuffer.toString());
            return UTCTimeBuffer.toString() + " UTC";
        } catch (ParseException e) {
            log.error("error:" + e);
        }
        return null;
    }

    /**
     * 得到多少分钟之后
     *
     * @param date
     * @return
     */
    public static Date addDate(String date, int ms) {
        long time = ms * 60 * 1000;//30分钟
        Date afterDate = new Date(DateUtil.parse(date).getTime() + time);
        return afterDate;
    }

    /**
     * 得到多少小时之后
     *
     * @param date
     * @return
     */
    public static Date addHours(String date, int hours) {
        long time = hours * 60 * 60 * 1000;
        Date afterDate = new Date(DateUtil.parse(date).getTime() + time);
        return afterDate;
    }

    /**
     * 得到多少小时之后
     *
     * @param date
     * @return
     */
    public static Date addHours(Date date, int hours) {
        long time = hours * 60 * 60 * 1000;
        Date afterDate = new Date(date.getTime() + time);
        return afterDate;
    }

    /**
     * Java将Unix时间戳转换成指定格式日期字符串
     *
     * @param timestampString 时间戳 如："1473048265";
     * @param formats         要格式化的格式 默认："yyyy-MM-dd HH:mm:ss";
     */
    public static String timeStamp2Date(String timestampString, String formats) {
        Long timestamp = Long.parseLong(timestampString) * 1000;
        return new SimpleDateFormat(formats, Locale.CHINA).format(new Date(timestamp));
    }

    /**
     * 日期格式字符串转换成时间戳
     */
    public static String date2TimeStamp(String dateStr, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return String.valueOf(sdf.parse(dateStr).getTime() / 1000);
        } catch (Exception e) {
            log.error("error:" + e);
        }
        return org.apache.commons.lang3.StringUtils.EMPTY;
    }

    /**
     * 是否多少天内
     *
     * @param addtime
     * @param now
     * @return
     */
    public static boolean isLatestWeek(Date addtime, Date now, int day) {
        Calendar calendar = Calendar.getInstance();  //得到日历
        calendar.setTime(now);//把当前时间赋给日历
        calendar.add(Calendar.DAY_OF_MONTH, -day);  //设置为7天前
        Date before7days = calendar.getTime();   //得到7天前的时间
        if (before7days.getTime() < addtime.getTime()) {
            return true;
        }
        return false;
    }
//
//    public static void main(String[] args) {
//    	System.out.println(getCurrentDate(FORMAT_DD_DATE));    }

    /**
     * 判断当前日期是星期几
     *
     * @param pTime 修要判断的时间
     * @return dayForWeek 判断结果
     * @Exception 发生异常
     */
    public static int dayForWeek(String pTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        int dayForWeek = 0;
        try {
            c.setTime(format.parse(pTime));
            if (c.get(Calendar.DAY_OF_WEEK) == 1) {
                dayForWeek = 7;
            } else {
                dayForWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
            }
        } catch (ParseException e) {
            log.error("error:" + e);
        }
        return dayForWeek;
    }

    /**
     * 获得指定时间的年月日
     *
     * @param pTime   字符串日期
     * @param formate 日月年 通配  year = "%tY";  month = "%tm"; day = "%td";
     */
    public static int getDayMonthYear(String pTime, String formate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        Date dt = new Date();
        try {
            cal.setTime(format.parse(pTime));
            dt = cal.getTime();
        } catch (ParseException e) {
            log.error("getDayMonthYear error:", e);
        }
        return (Integer.valueOf(String.format(formate, dt))).intValue();
    }


    /**
     * 获取当月前后amount个月的第一天日期
     *
     * @param pattern
     * @param past
     * @param flag    0表示获取00:00:00 1表示获取59:59:59 null表示当前时间点
     * @return
     */
    public static String getFirstOfMonth(String pattern, Integer past, Integer flag) {
        SimpleDateFormat sf = new SimpleDateFormat(pattern, Locale.CHINA);
        Calendar c = Calendar.getInstance(Locale.CHINA);
        c.add(Calendar.MONTH, 0 - past);
        c.set(Calendar.DAY_OF_MONTH, 1);//设置为1号,当前日期既为本月第一天
        setTime(c, flag);
        String monthFirst = sf.format(c.getTime());
        return monthFirst;
    }

    /**
     * 获取当月前后amount个月的最后一天日期
     *
     * @param pattern
     * @param past
     * @param flag    0表示获取00:00:00 1表示获取59:59:59 null表示当前时间点
     * @return
     */
    public static String getEndOfMonth(String pattern, Integer past, Integer flag) {
        SimpleDateFormat sf = new SimpleDateFormat(pattern, Locale.CHINA);
        Calendar c = Calendar.getInstance(Locale.CHINA);
        c.add(Calendar.MONTH, 0 - past);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        setTime(c, flag);
        String monthLast = sf.format(c.getTime());
        return monthLast;
    }

    /**
     * 获取上几周 周一的日期
     *
     * @param pattern
     * @param past
     * @param flag    0表示获取00:00:00 1表示获取59:59:59 null表示当前时间点
     * @return
     */
    public static String getMonday(String pattern, Integer past, Integer flag) {
        SimpleDateFormat sf = new SimpleDateFormat(pattern, Locale.CHINA);
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.add(Calendar.WEEK_OF_YEAR, 0 - past);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        setTime(calendar, flag);
        return sf.format(calendar.getTime());
    }

    /**
     * 获取上几周 周日的日期
     *
     * @param pattern
     * @param past
     * @param flag    0表示获取00:00:00 1表示获取59:59:59 null表示当前时间点
     * @return
     */
    public static String getWeek(String pattern, Integer past, Integer flag) {
        SimpleDateFormat sf = new SimpleDateFormat(pattern, Locale.CHINA);
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.add(Calendar.WEEK_OF_YEAR, 0 - past);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        setTime(calendar, flag);
        return sf.format(calendar.getTime());
    }

    public static void setTime(Calendar c, Integer flag) {
        if (null != flag) {
            if (Constants.EVNumber.zero == flag) {
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
            } else if (Constants.EVNumber.one == flag) {
                c.set(Calendar.HOUR_OF_DAY, 23);
                c.set(Calendar.MINUTE, 59);
                c.set(Calendar.SECOND, 59);
            }
        }
    }

    /**
     * 当前时间所在一周的周一和周日时间
     *
     * @return
     */
    public static Map<String, String> getWeekDate() {
        Map<String, String> map = new HashMap();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);// 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);// 获得当前日期是一个星期的第几天
        if (dayWeek == 1) {
            dayWeek = 8;
        }

        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - dayWeek);// 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
        Date mondayDate = cal.getTime();
        String weekBegin = sdf.format(mondayDate);

        cal.add(Calendar.DATE, 4 + cal.getFirstDayOfWeek());
        Date sundayDate = cal.getTime();
        String weekEnd = sdf.format(sundayDate);

        map.put("mondayDate", weekBegin);
        map.put("sundayDate", weekEnd);
        return map;
    }

    /**
     * 当前时间所在一周的周一和下周周一的时间
     *
     * @return
     */
    public static Map<String, String> getWeekDateEx() {
        Map<String, String> map = new HashMap();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);// 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);// 获得当前日期是一个星期的第几天
        if (dayWeek == 1) {
            dayWeek = 8;
        }

        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - dayWeek);// 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
        Date mondayDate = cal.getTime();
        String weekBegin = sdf.format(mondayDate);

        cal.add(Calendar.DATE, 5 + cal.getFirstDayOfWeek());
        Date sundayDate = cal.getTime();
        String weekEnd = sdf.format(sundayDate);

        map.put("mondayDate", weekBegin);
        map.put("sundayDate", weekEnd);
        return map;
    }

    /**
     * 根据传入的日期字符串，获取前一天的23:59:59
     *
     * @param date
     * @return
     */
    public static String getLastOneDayEnd(String date) {
        Date end = getDateBefore(parse(date), 1);
        return DateUtil.format(end, DateUtil.FORMAT_10_DATE) + " " + DATE_END;
    }

    /**
     * 根据传入的日期字符串date，获取day天后(不含当天)的pattern格式的日期
     *
     * @param date
     * @return
     */
    public static String getPostDayTime(String date, int day, String pattern) {
        return format(getDateAfter(parse(date, pattern), day));
    }

    /**
     * 比较两个时间字符串大小
     *
     * @param time1
     * @param time2
     * @param formats 格式
     * @return 1 time1 大于 time2 ; 0 time1 等于 time2 ; -1 time1 小于 time2
     */
    public static int timeCompare(String time1, String time2, String formats) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formats);
            LocalDateTime localDateTime1 = LocalDateTime.parse(time1, formatter);
            LocalDateTime localDateTime2 = LocalDateTime.parse(time2, formatter);
            return localDateTime1.compareTo(localDateTime2);
        } catch (Exception e) {
            log.error("timeCompare出错", e);
        }
        return -1;
    }

    /**
     * 两个时间相差距离多少天多少小时多少分多少秒
     *
     * @param str1 时间参数 1 格式：1990-01-01 12:00:00
     * @param str2 时间参数 2 格式：2009-01-01 12:00:00
     * @return 分
     */
    public static Long getDistanceTimes(String str1, String str2) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date one;
        Date two;
        long day = 0;
        long hour = 0;
        long min = 0;
        try {
            one = df.parse(str1);
            two = df.parse(str2);
            long time1 = one.getTime();
            long time2 = two.getTime();
            long diff;
            if (time1 < time2) {
                diff = time2 - time1;
            } else {
                diff = time1 - time2;
            }
            min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
        } catch (Exception e) {
            log.error("error:" + e);
        }
        return min;
    }

    /**
     * 两个时间相差距离多少天多少小时多少分多少秒
     *
     * @param date1 时间参数 1 格式：2020-04-16
     * @param date2 时间参数 2 格式：2020-04-17
     * @return 分
     */
    public static Long daysdifference(String date1, String date2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
        try {
            Date d1 = sdf.parse(date1);
            Date d2 = sdf.parse(date2);
            long daysBetween = (d2.getTime() - d1.getTime() + 1000000) / (60 * 60 * 24 * 1000);
            return daysBetween;
        } catch (ParseException e) {
            log.error("error:" + e);
        }
        return null;
    }

    /**
     * 获取本月第一天
     */
    public static Date getmindate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    /**
     * 获取本月最后一天
     */
    public static Date getmaxdate() {
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(new Date());
        calendar2.set(Calendar.DAY_OF_MONTH, calendar2.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar2.getTime();
    }

    /**
     * 2个日期相差多少天
     *
     * @param smdate
     * @param bdate
     * @return
     */
    public static int daysBetween(String smdate, String bdate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(smdate));
            long time1 = cal.getTimeInMillis();
            cal.setTime(sdf.parse(bdate));
            long time2 = cal.getTimeInMillis();
            long between_days = (time2 - time1) / (1000 * 3600 * 24);
            return Integer.parseInt(String.valueOf(between_days));
        } catch (Exception e) {
            log.error("计算2个时间差出错", e);
        }
        return 0;
    }

    //判断选择的日期是否是今天
    public static boolean isToday(long time) {
        return isThisTime(time, "yyyy-MM-dd");
    }

    //判断选择的日期是否是本周
    public static boolean isThisWeek(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);
        calendar.setTime(new Date(time));
        int paramWeek = calendar.get(Calendar.WEEK_OF_YEAR);
        if (paramWeek == currentWeek) {
            return true;
        }
        return false;
    }

    public static boolean isThisTime(long time, String pattern) {
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        String param = sdf.format(date);//参数时间
        String now = sdf.format(new Date());//当前时间
        if (param.equals(now)) {
            return true;
        }
        return false;
    }

    public static String getLastMonth() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1);
        return sdf.format(cal.getTime());
    }
    public static String getNextMonthDD() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, 1);
        return sdf.format(cal.getTime());
    }
    public static String getLastMonthDD() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1);
        return sdf.format(cal.getTime());
    }

    public static String getLastMonthByTime(String time){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        try {
            Date date = sdf.parse(time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.MONTH, -1);
            String day =sdf.format(cal.getTime());
            return day;
        }catch (Exception e){
            log.error("上个月时间出错", e);
        }
        return null;
    }

    public static boolean isBelong(String startTime, String endTime){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        Date now = null;
        Date start = null;
        Date end = null;

        try {
            now = simpleDateFormat.parse(simpleDateFormat.format(new Date()));
            start = simpleDateFormat.parse(startTime);
            end = simpleDateFormat.parse(endTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return belong(now, start, end);
    }

    public static boolean belong(Date nowTime, Date startTime, Date endTime){

        Calendar now = Calendar.getInstance();
        now.setTime(nowTime);

        Calendar start = Calendar.getInstance();
        now.setTime(startTime);

        Calendar end = Calendar.getInstance();
        now.setTime(endTime);
        if(now.after(start) && now.before(end)){
            return true;
        }
        return false;
    }

}