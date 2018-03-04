package com.example.snow.almanaclayout;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import android.view.View;
import android.widget.TextView;
import android.content.Context;

import java.lang.Integer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AlmanacData {
    private final static String TAG = "AlmanacData";

    private String mJsonStr;
    private Context mContext;
    private String pengzu;
    private String wuxing;
    private String xingxiu;
    private String chongsha;
    private String nongli;
    private String[] ji;
    private String[] yi;
    private String jieqi;
    private String taishen;
    private String fanwei;
    private String date;
    private String week;
    private int weeknum;
    private String lYear;
    private String lMonth;
    private String lDay;
    private String shengxiao;


    private TextView tvDate;
    private TextView tvLunar;
    private TextView tvJi;
    private TextView tvYi;
    private TextView tvPengZu;
    private TextView tvTaiShen;
    private TextView tvChongSha;
    private TextView tvWuXing;
    private TextView tvXingXiu;
    private TextView tvFu;
    private TextView tvCai;
    private TextView tvSheng;
    private TextView tvXi;
    private TextView tvWeek;
    private TextView tvWeekNum;
    private TextView tvLMonth;
    private TextView tvLDay;

    AlmanacData(Context context) {
       mContext = context;
    }

    AlmanacData(String json) {
        mJsonStr = json;
    }

    public void initViews(View view) {
        tvDate = (TextView)view.findViewById(R.id.tv_date);
        tvLunar = (TextView)view.findViewById(R.id.tv_lunar);
        tvJi = (TextView)view.findViewById(R.id.tv_ji);
        tvYi = (TextView)view.findViewById(R.id.tv_yi);
        tvPengZu = (TextView)view.findViewById(R.id.tv_pengzu);
        tvTaiShen = (TextView)view.findViewById(R.id.tv_taishen);
        tvChongSha = (TextView)view.findViewById(R.id.tv_chongsha);
        tvWuXing = (TextView)view.findViewById(R.id.tv_wuxing);
        tvXingXiu = (TextView)view.findViewById(R.id.tv_xingxiu);
        tvFu = (TextView)view.findViewById(R.id.tv_deity_fu);
        tvCai = (TextView)view.findViewById(R.id.tv_deity_cai);
        tvSheng = (TextView)view.findViewById(R.id.tv_deity_sheng);
        tvXi = (TextView)view.findViewById(R.id.tv_deity_xi);
        tvWeek = (TextView)view.findViewById(R.id.tv_week);
        tvWeekNum = (TextView)view.findViewById(R.id.tv_weeknum);
        tvLMonth = (TextView)view.findViewById(R.id.tv_acient_month);
        tvLDay = (TextView)view.findViewById(R.id.tv_acient_day);
    }

    public void parseJson() {
        try {
            JSONObject jsonObj = new JSONObject(mJsonStr).getJSONObject("data");
            pengzu = jsonObj.get("pengzu").toString();
            wuxing = jsonObj.get("wuxing").toString();
            chongsha = jsonObj.get("cong").toString();
            nongli = jsonObj.get("nongli").toString();
            xingxiu = jsonObj.get("xingxiu").toString();
            //date = jsonObj.get("date").toString();
            week = jsonObj.get("xingqi").toString();
            JSONArray jio = jsonObj.getJSONArray("ji");
            JSONArray yio = jsonObj.getJSONArray("yi");
            ji = new String[jio.length()];
            yi = new String[yio.length()];
            for (int i = 0; i < jio.length(); i++) {
                JSONObject jioo = jio.getJSONObject(i);
                ji[i] = jioo.getString("old");
            }
            for (int j = 0; j < yio.length(); j++) {
                JSONObject yioo = yio.getJSONObject(j);
                yi[j] = yioo.getString("old");
            }
            jieqi = jsonObj.get("jieqi").toString();
            taishen = jsonObj.get("taishen").toString();
            fanwei = jsonObj.get("fanwei").toString();
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date dat = format.parse(date);
            calendar.setTime(dat);
            weeknum = calendar.get(Calendar.WEEK_OF_YEAR);
            int year,month,day;
            year = Integer.valueOf(date.split("-")[0]);
            month = Integer.valueOf(date.split("-")[1]);
            day = Integer.valueOf(date.split("-")[2]);
            LunarDate ldate = new LunarDate(year,month,day);
            ldate.calcLunarDate();
            lYear = ldate.getLYear();
            lMonth = ldate.getLMonth();
            lDay = ldate.getLDay();
            shengxiao = ldate.getShengXiao();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setJsonString(String json,String datestr) {
        mJsonStr = json;
        date = datestr;
    }

    public void updateViews() {
        Log.d(TAG,"date date="+date+" "+date.split("-")[2]);
        tvDate.setText(date.split("-")[2]);
        tvWuXing.setText(wuxing);
        tvTaiShen.setText(taishen);
        tvChongSha.setText(chongsha);
        tvXingXiu.setText(xingxiu);
        String str = "";
        for (int i = 0; i < ji.length; i++) {
            str += ji[i] + " ";
        }
        tvJi.setText(str);
        str = "";
        for (int i = 0; i < yi.length; i++) {
            str += yi[i] + " ";
        }
        tvYi.setText(str);
        tvWeek.setText(rotateStr(week));
        tvWeekNum.setText("第\n"+weeknum+"\n周");
        tvLunar.setText(lYear+shengxiao+"年 "+nongli);
        tvLMonth.setText(rotateStr(lMonth)+"月");
        tvLDay.setText(rotateStr(lDay)+"日");
        setPengZu();
        setZhuShen();
    }

    private void setPengZu() {
        String[] str = new String[4];
        str[0] = pengzu.substring(0,4);
        str[1] = pengzu.substring(4,8);
        str[2] = pengzu.substring(9,13);
        str[3] = pengzu.substring(13);
        for(int i= 0;i<4;i++) {
            Log.d(TAG, "pengzu str[] = " + str[i]);
        }
        tvPengZu.setText(str[0]+"\n"+str[1]+"\n\n"+str[2]+"\n"+str[3]);
    }

    private void setZhuShen() {
        String[] str = fanwei.split(" ");
        for(int i=0;i<str.length;i++) {
            if("福神".equals(str[i])) {
                tvFu.setText(str[i]+"-"+(!"".equals(str[i+1])?str[i+1]:str[i+2]));
            } else if("财神".equals(str[i])) {
                tvCai.setText(str[i]+"-"+(!"".equals(str[i+1])?str[i+1]:str[i+2]));
            } else if("生门".equals(str[i])) {
                tvSheng.setText(str[i]+"-"+(!"".equals(str[i+1])?str[i+1]:str[i+2]));
            } else if("喜神".equals(str[i])) {
                tvXi.setText(str[i]+"-"+(!"".equals(str[i+1])?str[i+1]:str[i+2]));
            }
            Log.d(TAG,"zhushen str[] ="+str[i]);
        }
    }

    private String rotateStr(String str) {
        String res = "";
        for(int i=0;i<str.length();i++) {
            res += str.substring(i,i+1)+"\n";
        }
        Log.d(TAG,"res = "+res);
        return res;
    }
    public String toString() {
        String str = "";
        str = str + "彭祖："+pengzu+"\n";
        str = str +"农历:"+nongli+"\n";
        str = str + "忌:";
        for(int i = 0;i<ji.length;i++) {
            str = str + ji[i] +",";
        }
        str = str + "\n宜:";
        for(int i = 0;i<yi.length;i++) {
            str = str + yi[i] +",";
        }
        str = str + "\n";
        return str;
    }
}
