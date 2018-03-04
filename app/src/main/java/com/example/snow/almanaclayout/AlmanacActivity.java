package com.example.snow.almanaclayout;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.app.DatePickerDialog;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import android.widget.ProgressBar;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class AlmanacActivity extends AppCompatActivity implements View.OnClickListener,ViewPager.OnPageChangeListener,AlmanacTask.OnDownloadDone,
                                                            AlmanacPagerAdapter.OnDateNotCache{
    private final static String TAG = "AlmanacActivity";

    private final static int CONTENT_MAX_NUM = 200;
    private final static int CURRENT_INDEX = CONTENT_MAX_NUM/2;
    private TextView tvDatePicker;
    private Button btnBack;
    private Button btnToday;
    private ViewPager vpContentPager;
    private ProgressBar pbDownloadWait;
    private List<View> contentViews;
    private AlmanacTask almanacTask;
    private AlmanacData almanacData;
    private DBHelper dbHelper;
    private String mCurrentDate;
    private String mTodayDate;
    private int mPagerPostion;
    private AlmanacPagerAdapter pagerAdapter;
    private boolean mTaskIsRunning = false;
    private DownLoadService dsTask = null;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG,"service connected ...");
            DownLoadService.DownLoadBinder binder = (DownLoadService.DownLoadBinder)service;
            dsTask = binder.getService();
            dsTask.test();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_almanac);
        getSupportActionBar().hide();

        tvDatePicker = (TextView)findViewById(R.id.title_date);
        btnBack = (Button)findViewById(R.id.btn_back);
        btnToday = (Button)findViewById(R.id.btn_today);
        tvDatePicker.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnToday.setOnClickListener(this);
        pbDownloadWait = (ProgressBar)findViewById(R.id.download_progress);
        almanacTask = new AlmanacTask(this);
        dbHelper = new DBHelper(this);
        almanacData = new AlmanacData(this);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        mCurrentDate = format.format(calendar.getTime());
        mTodayDate = mCurrentDate;

        btnToday.setVisibility(View.INVISIBLE);

        vpContentPager = (ViewPager)findViewById(R.id.content_pager);
        vpContentPager.setOnPageChangeListener(this);
        LayoutInflater layoutInflater = getLayoutInflater().from(this);
        contentViews = new ArrayList<View>();
        for(int i = 0;i<5;i++) {
            View view = layoutInflater.inflate(R.layout.almanac_layout,null);
            contentViews.add(view);
        }
        pagerAdapter = new AlmanacPagerAdapter(this,contentViews,CONTENT_MAX_NUM,CURRENT_INDEX,mCurrentDate);
        vpContentPager.setAdapter(pagerAdapter);
        vpContentPager.setCurrentItem(CURRENT_INDEX);

        Intent intent = new Intent(this,DownLoadService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        /*
        for(int i = 1;i<=31;i++) {
            String date = String.format("2016-01-%02d",i);
            Log.d(TAG,"date = "+date);
            ecalenderTask.execute("http://zhwnlapi.etouch.cn/Ecalender/openapi/huangli/2016-01-09?key=5R0pLm62x04UiuTXHokR5XvbegjE3wkj",date);
        }
        */
        //new EcalenderTask().execute("http://zhwnlapi.etouch.cn/Ecalender/openapi/huangli/2016-01-09?key=5R0pLm62x04UiuTXHokR5XvbegjE3wkj");
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.title_date) {
            int year,month,day;
            year = Integer.valueOf(mTodayDate.split("-")[0]);
            month = Integer.valueOf(mTodayDate.split("-")[1]);
            day = Integer.valueOf(mTodayDate.split("-")[2]);
            DatePickerDialog dpd = new DatePickerDialog(this,mDateSetListener,year,month-1,day);
            dpd.show();
        } else if(v.getId() == R.id.btn_back) {
            finish();
        } else if(v.getId() == R.id.btn_today) {
            mCurrentDate = mTodayDate;
            String date = AlmanacPagerAdapter.getDate(mTodayDate,0,2);
            pagerAdapter.setCurrentDate(date);
            pagerAdapter.setNeedUpdate(false);
            vpContentPager.setAdapter(pagerAdapter);
            vpContentPager.setCurrentItem(CURRENT_INDEX - 2);
            pagerAdapter.setNeedUpdate(true);
            tvDatePicker.setText(mTodayDate.split("-")[0] + "年" + mTodayDate.split("-")[1] + "月");
            btnToday.setVisibility(View.INVISIBLE);
        }
        /*
        if(v.getId() == R.id.get_cache) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            db.delete("ecalender",null,null);
            ecalenderTask.execute("2016-01-01","2016-12-31");
        }else if(v.getId() == R.id.get_data) {

            String date = editDateInput.getText().toString();
            Log.d(TAG,"get data date ="+date);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query("ecalender", new String[]{"jsondata"}, "date = ?",new String[]{date},null,null,null);

            if(cursor.moveToFirst()) {
                EcalenderData edata = new EcalenderData(cursor.getString(0));
                edata.parseJson();
                editShowData.setText(edata.toString());
            }
        }
        */
    }

    @Override
    public void onPageSelected(int position) {
        mPagerPostion = position;
        Log.d(TAG,"onPageSelected position = "+position);
        mCurrentDate = pagerAdapter.getDate(position);
        String[] date = pagerAdapter.getDate(position).split("-");
        tvDatePicker.setText(date[0]+"年"+date[1]+"月");
        if(!mTodayDate.equals(mCurrentDate)) {
            btnToday.setVisibility(View.VISIBLE);
        } else {
            btnToday.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            String date = String.format("%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
            Log.d(TAG, "date = " + date);

            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query("ecalender", new String[]{"jsondata"}, "date = ?", new String[]{date}, null, null, null);
            //the date has been stored in database
            if(cursor.moveToFirst()) {
                vpContentPager.setVisibility(View.VISIBLE);
                pbDownloadWait.setVisibility(View.GONE);
                mCurrentDate = date;
                date = AlmanacPagerAdapter.getDate(date,0,2);
                pagerAdapter.setCurrentDate(date);
                pagerAdapter.setNeedUpdate(false);
                vpContentPager.setAdapter(pagerAdapter);
                vpContentPager.setCurrentItem(CURRENT_INDEX-2);
                pagerAdapter.setNeedUpdate(true);
                tvDatePicker.setText(year+"年"+(monthOfYear<9?"0":"")+(monthOfYear+1)+"月");
            } else {
                if(isNetworkConnected()) {
                    vpContentPager.setVisibility(View.GONE);
                    pbDownloadWait.setVisibility(View.VISIBLE);
                    mCurrentDate = date;
                    new AlmanacTask(AlmanacActivity.this).execute(AlmanacTask.OP_GET_DAYS, date, -5, 5);
                    if (dsTask != null) {
                        dsTask.download(year, monthOfYear > 0 ? monthOfYear : monthOfYear + 1, monthOfYear < 11 ? monthOfYear + 2 : monthOfYear + 1);
                    }
                    tvDatePicker.setText(year + "年" + (monthOfYear < 9 ? "0" : "") + (monthOfYear + 1) + "月");
                }else {
                    Toast.makeText(AlmanacActivity.this,"network is not connected .",Toast.LENGTH_LONG).show();
                }
            }


            Log.d(TAG, "year = " + year + " month = " + monthOfYear + 1 + " day=" + dayOfMonth);
        }
    };

    @Override
    public void updateDateUI() {
        Log.d(TAG,"updateDateUI ..");
        vpContentPager.setVisibility(View.VISIBLE);
        pbDownloadWait.setVisibility(View.GONE);
        mTaskIsRunning = false;
        //mCurrentDate = date;
        String date;
        date = AlmanacPagerAdapter.getDate(mCurrentDate,0,2);
        pagerAdapter.setCurrentDate(date);
        pagerAdapter.setNeedUpdate(false);
        vpContentPager.setAdapter(pagerAdapter);
        vpContentPager.setCurrentItem(CURRENT_INDEX-2);
        pagerAdapter.setNeedUpdate(true);
        //tvDatePicker.setText(year+"年"+(monthOfYear<9?"0":"")+(monthOfYear+1)+"月");
    }

    @Override
    public void downloadHttpDate(String date) {
        Log.d(TAG, "downloadHttpDate date = " + date);
        if(!mTaskIsRunning) {
            vpContentPager.setVisibility(View.GONE);
            pbDownloadWait.setVisibility(View.VISIBLE);
            //mCurrentDate = date;
            new AlmanacTask(this).execute(AlmanacTask.OP_GET_DAYS, date, -5, 5);
            mTaskIsRunning = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo != null) {
            return networkInfo.isAvailable();
        }
        return false;
    }
}
