package com.example.snow.almanaclayout;

import android.util.Log;
import java.util.Calendar;

public class LunarDate {
    private final static String TAG = "LunarDate";
    private final static String[] GAN = {"甲","乙","丙","丁","戊","己","庚","辛","壬","癸"};
    private final static String[] ZHI= {"子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥"};
    private final static String[] SHENGXIAO = {"鼠","牛","虎","兔","龙","蛇","马","羊","猴","鸡","狗","猪"};
    private final static double[] PARM_C = {4.6295,3.87,4.15};

    private int mYear;
    private int mMonth;
    private int mDay;
    private String mLYear;
    private String mLMonth;
    private String mLDay;

    LunarDate(int y,int m,int d) {
        mYear = y;
        mMonth = m -1;
        mDay = d;
    }

    private String getCyclical(int index) {
        return GAN[index%10]+ZHI[index%12];
    }

    public void calcLunarDate() {
        if(mMonth<2) {
            mLYear = getCyclical(mYear - 1900 + 36 - 1);
        } else{
            mLYear = getCyclical(mYear - 1900 + 36);
        }
        mLMonth = getCyclical((mYear-1900)*12+mMonth+13);
        Calendar calendar = Calendar.getInstance();
        Calendar calendar1 = Calendar.getInstance();
        calendar.set(mYear,mMonth,mDay);
        calendar1.set(1970,0,1);
        int days = (int)((calendar.getTimeInMillis())/(24*3600000)) + 25567 + 10;
        Log.d(TAG,"days = "+days);
        mLDay = getCyclical(days);

        int year_1 = mYear/100 ,year_2 = mYear%100;
        double parm_c = PARM_C[year_1-19];

        int lichun = (int)(year_2*0.2422 + parm_c) - (year_2-1)/4;
        Log.d(TAG,"lichun = "+lichun);
        if(mMonth == 1 && mDay >= lichun) {
            mLYear = getCyclical(mYear - 1900 + 36);
        }
    }

    public String getLYear() {
        return mLYear;
    }

    public String getLMonth() {
        return mLMonth;
    }

    public String getLDay() {
        return mLDay;
    }

    public String getShengXiao() {
        return SHENGXIAO[(mYear-4)%12];
    }
}
