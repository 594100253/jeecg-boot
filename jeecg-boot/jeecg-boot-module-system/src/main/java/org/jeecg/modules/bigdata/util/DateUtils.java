package org.jeecg.modules.bigdata.util;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 日期工具类
 * </p>
 *
 * @author Caratacus
 */
public class DateUtils {

    public static String secondFmt = "yyyy-MM-dd HH:mm:ss";
    public static String dayFmt = "yyyy-MM-dd";


    public static String[][] WEEK_FMT = {
        { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" },
        { "日", "一", "二", "三", "四", "五", "六" },
        { "周日", "周一", "周二", "周三", "周四", "周五", "周六" }
    };

    /**
     * 计算两个日期 相差天数（相差是23个小时的时候，它就不算一天了）
     * @param date1
     * @param date2
     * @return
     */
    public static int compareDayNum(Date date1, Date date2)
    {
        int days = (int) ((date2.getTime() - date1.getTime()) / (1000*3600*24));
        return days;
    }

    /**
     * 日期转换成字符串
     * @param date
     * @return str
     */
    public static String DateToStr(Date date,String fmtStr) {
        SimpleDateFormat format = new SimpleDateFormat(fmtStr);
        String str = format.format(date);
        return str;
    }

    /**
     * 字符串转换成日期
     * @param str
     * @return date
     */
    public static Date StrToDate(String str,String fmtStr) {

        SimpleDateFormat format = new SimpleDateFormat(fmtStr);
        Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
    * 指定日期加上天数后的日期
    * @param date 创建时间
    * @param num 为增加的天数
    * @return
    * @throws ParseException
    */
    public static Date addDate(Date date, long num){
        long time = date.getTime(); // 得到指定日期的毫秒数
        time += num * 24 * 60 * 60 * 1000; // 相加得到新的毫秒数
        return new Date(time); // 将毫秒数转换成日期
    }

    /**
     * 获取当前日期是星期几<br>
     *
     * @param date
     * @param fmt 格式 日周1  星期1
     * @return 当前日期是星期几
     */
    public static String getWeek(Date date,int fmt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return WEEK_FMT[fmt][w];
    }

    public static void main(String[] args) {
    	Long[] a = new  Long[]{1L,2L};
	    System.out.println(a.toString());
	    List<Long> b = new ArrayList<>();
	    b.add(1L);
	    b.add(1123L);
	    System.out.println(b.toString());
    }
}
