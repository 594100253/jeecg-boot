package org.jeecg.modules.bigdata.util;


import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

// 专为 Kylin 事件分析提供的工具类
public class KylinUtil {


    public static String DateType_YEAR = "YEAR";
    public static String DateType_MONTH = "MONTH";
    public static String DateType_DAY = "DAY";

    public static String DateFormat_1 = "yyyy-mm-dd";

    public static String getDateStr(Date date,String formatStr){
        SimpleDateFormat f=new SimpleDateFormat(DateFormat_1);
        return f.format(date);
    }

    public static Date getStrDate(String dateStr,String formatStr){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat(DateFormat_1);
        Date date=null;
        try {
            date =simpleDateFormat.parse(dateStr);
        } catch(ParseException px) {
            px.printStackTrace();
        }
        return date;
    }

    /**
     * 根据两个日期范围 和 日期类型 返回每个时间段数组
     * @param type 类型 year,month,day
     * @param startData  2000,2000-01,2000-01-01
     * @param endData   2003,2000-03,2000-01-03
     * @return
     *          ['2000','2001',2003]
     *          ['2000-01','2000-02',2000-03]
     *          ['2000-01-01','2000-01-02',2000-01-03]
     */
    public static List<Date> getEachDate(Date startData,Date endData,String type) {
        List<Date> lDate = new ArrayList<Date>();
        if(type == DateType_DAY){
            //定义一个接受时间的集合
            lDate.add(startData);
            Calendar calBegin = Calendar.getInstance();
            // 使用给定的 Date 设置此 Calendar 的时间
            calBegin.setTime(startData);
            Calendar calEnd = Calendar.getInstance();
            // 使用给定的 Date 设置此 Calendar 的时间
            calEnd.setTime(endData);
            // 测试此日期是否在指定日期之后
            while (endData.after(calBegin.getTime()))  {
                // 根据日历的规则，为给定的日历字段添加或减去指定的时间量
                calBegin.add(Calendar.DAY_OF_MONTH, 1);
                lDate.add(calBegin.getTime());
            }
        }
        return lDate;
    }

    /**
     * 作用同 getEachDate()
     * @param startDataStr
     * @param endDataStr
     * @param type
     * @return
     * @throws ParseException
     */
    public static List<String> getEachDate(String startDataStr,String endDataStr,String type){
        Date startData = getStrDate(startDataStr,DateFormat_1);
        Date endData = getStrDate(endDataStr,DateFormat_1);
        List<Date> dateAr = getEachDate(startData,endData,type);
        List<String> dateStrAr = new ArrayList<>();
        for (int i = 0; i < dateAr.size() ; i++) {
            dateStrAr.add(getDateStr(dateAr.get(i),DateFormat_1));
        }
        return dateStrAr;
    }



    private static String[] zeroFillItemAr = new String[]{"timeyear","timemonth","timeday","timehour"};
    private static int[] zeroFillItemZeroAr = new int[]{4,2,2,2};
    /**
     * 若指标为：zeroFillItemAr=[年 月 日 时]，需补零 zeroFillItemZeroAr对应索引的位数
     * @param key 指标名
     * @param value 值
     * @return
     */
    public static String zeroFill(String key,Object value){
        if(value==null){
            return null;
        }
        for (int i = 0; i < zeroFillItemAr.length ; i++) {
            if(zeroFillItemAr[i].equals(key.toLowerCase())){
                NumberFormat nf = NumberFormat.getInstance();
                //设置是否使用分组
                nf.setGroupingUsed(false);
                //设置最大整数位数
                nf.setMaximumIntegerDigits(zeroFillItemZeroAr[i]);
                //设置最小整数位数
                nf.setMinimumIntegerDigits(zeroFillItemZeroAr[i]);
                return nf.format(Integer.parseInt(value.toString()));
            }
        }
        return value.toString();
    }
}
