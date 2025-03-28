package com.zkcompany.entity;

import java.util.Calendar;
import java.util.Date;

public class WorldTime {
    //相对于UTC时间，中国时间比UTC晚8小时
    public final static int  chinese_time = 8;
    //相对于UTC时间，泰国时间比UTC晚7小时
    public final static int Thailand_time = 7;
    //相对于UTC时间，日本时间比UTC晚9小时
    public final static int japan_time = 9;

    public static Date chinese_time(Date date){
        return WorldTime.date_deal(date , chinese_time);
    }

    public static Date Thailand_time(Date date){
        return WorldTime.date_deal(date , Thailand_time);
    }

    public static Date japan_time(Date date){
        return WorldTime.date_deal(date , japan_time);
    }
    //中国时间
    public static Date date_deal(Date date,int hour){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, hour);// 24小时制
        date = calendar.getTime();
        return date;
    }


}
