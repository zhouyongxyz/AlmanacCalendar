package com.example.snow.almanaclayout;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import android.database.Cursor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by xiaowei.yang on 1/12/16.
 */
public class AlmanacTask extends AsyncTask<Object,Object,Object> {
    private final static String TAG = "AlmanacTask";
    private Context mContext;
    private DBHelper dbHelper;
    private String mDate;
    private final static int[] DAYS_OF_MONTH_1 = {0,31,29,31,30,31,30,31,31,30,31,30,31};
    private final static int[] DAYS_OF_MONTH_2 = {0,31,28,31,30,31,30,31,31,30,31,30,31};

    public final static String OP_GET_DAYS = "get_days";
    public final static String OP_GET_MONTHS = "get_month";

    public interface OnDownloadDone {
        public void updateDateUI();
    }
    public AlmanacTask(Context context) {
        mContext = context;
        dbHelper = new DBHelper(mContext);
    }

    @Override
    protected Object doInBackground(Object... params) {
        String op = params[0].toString();
        String status = "ok";
        if(OP_GET_MONTHS.equals(op)) {
            int year = Integer.valueOf(params[1].toString());
            int smon = Integer.valueOf(params[2].toString());
            int emon = Integer.valueOf(params[3].toString());
            Log.d(TAG,"year = "+year+" smon = "+smon+" emon = "+emon);
            boolean run_year = ((year%4 == 0 && year%100 != 0)||(year%400 == 0));
            for (int mon = smon; mon <= emon; mon++) {
                int days = run_year?DAYS_OF_MONTH_1[mon]:DAYS_OF_MONTH_2[mon];
                for (int i = 1; i <= days; i++) {
                    String date = String.format("%d-%02d-%02d",year, mon, i);
                    Log.d(TAG, "date = " + date);
                    try {
                        URL url = new URL("http://zhwnlapi.etouch.cn/Ecalender/openapi/huangli/" + date + "?key=5R0pLm62x04UiuTXHokR5XvbegjE3wkj");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setReadTimeout(5000);
                        conn.setRequestMethod("GET");
                        if (200 == conn.getResponseCode()) {
                            InputStream in = conn.getInputStream();
                            ByteArrayOutputStream data = new ByteArrayOutputStream();
                            byte[] buffer = new byte[1024];
                            int len = 0;
                            while ((len = in.read(buffer)) != -1) {
                                data.write(buffer, 0, len);
                            }
                            SQLiteDatabase db = dbHelper.getReadableDatabase();
                            ContentValues values = new ContentValues();
                            values.put("date", date);
                            values.put("jsondata", data.toString());
                            Cursor cursor = db.query("ecalender", new String[]{"jsondata"}, "date = ?",new String[]{date},null,null,null);
                            //the date has been stored in database
                            if(cursor.moveToFirst()) {
                                db.update("ecalender", values, "date = ?", new String[]{date});
                            } else {
                                db.insert("ecalender", null, values);
                            }
                            Log.d(TAG, "insert date = " + date);
                            //Log.d(TAG, "data = " + data.toString());
                            //return data.toString();
                        } else {
                            status = "error";
                        }
                    } catch (Exception e) {
                        status = "error";
                        e.printStackTrace();
                    }
                }
            }
        } else if(OP_GET_DAYS.equals(op)) {
            String middate = params[1].toString();
            int start = Integer.valueOf(params[2].toString());
            int end = Integer.valueOf(params[3].toString());
            for(int i = start;i<=end;i++) {
                String date = AlmanacPagerAdapter.getDate(middate,0,i);
                Log.d(TAG, "date = " + date);
                try {
                    URL url = new URL("http://zhwnlapi.etouch.cn/Ecalender/openapi/huangli/" + date + "?key=5R0pLm62x04UiuTXHokR5XvbegjE3wkj");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(5000);
                    conn.setRequestMethod("GET");
                    if (200 == conn.getResponseCode()) {
                        InputStream in = conn.getInputStream();
                        ByteArrayOutputStream data = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len = 0;
                        while ((len = in.read(buffer)) != -1) {
                            data.write(buffer, 0, len);
                        }
                        SQLiteDatabase db = dbHelper.getReadableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("date", date);
                        values.put("jsondata", data.toString());
                        db.insert("ecalender", null, values);
                        Log.d(TAG, "insert date = " + date);
                        //Log.d(TAG, "data = " + data.toString());
                        //return data.toString();
                    } else {
                        status = "error";
                    }
                } catch (Exception e) {
                    status = "error";
                    e.printStackTrace();
                }
            }
        }
        return status;
    }


    @Override
    protected void onPostExecute(Object o) {
        Log.d(TAG,"update date success !");
        if("ok".equals(o.toString())) {
            ((OnDownloadDone) mContext).updateDateUI();
        }
        /*
        try {
            JSONObject jsonObj = new JSONObject(o.toString()).getJSONObject("data");
            String pengzu = jsonObj.get("pengzu").toString();
            String wuxing = jsonObj.get("wuxing").toString();
            String date = jsonObj.get("date").toString();
            JSONArray ji = jsonObj.getJSONArray("ji");
            JSONArray yi = jsonObj.getJSONArray("yi");
            for(int i=0;i<ji.length();i++) {
                JSONObject jio = ji.getJSONObject(i);
                Log.d(TAG,"ji  : "+jio.getString("new"));
            }
            for(int j=0;j<yi.length();j++) {
                JSONObject yio = yi.getJSONObject(j);
                if(!yio.isNull("new")) {
                    Log.d(TAG, "yi  : " + yio.getString("new"));
                }else {
                    Log.d(TAG, "yi  old: " + yio.getString("old"));
                }
            }
            Log.d(TAG,"pengzu = "+pengzu);
            //Log.d(TAG,"wuxing = "+wuxing);
            //Log.d(TAG,"date = "+date);
            //Log.d(TAG, "ji = " + ji.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        super.onPostExecute(o);
    }
}
