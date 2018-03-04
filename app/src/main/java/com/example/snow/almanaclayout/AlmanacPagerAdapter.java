package com.example.snow.almanaclayout;

import android.util.Log;
import android.support.v4.view.PagerAdapter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AlmanacPagerAdapter extends PagerAdapter {
    private final static String TAG = "AlmanacPagerAdapter";
    private Context mContext;
    private int mContentMaxNum;
    private int mMidContentIndex;
    private String mCurrentDate;
    private List<View> mListViews;
    private AlmanacData almanacData;
    private DBHelper dbHelper;
    private boolean mNeedUpdate = true;

    public interface OnDateNotCache {
        public void downloadHttpDate(String date);
    }

    AlmanacPagerAdapter(Context context,List<View> list,int maxnum,int midindex,String date) {
        mContext = context;
        mListViews = list;
        mContentMaxNum = maxnum;
        mMidContentIndex = midindex;
        mCurrentDate = date;
        dbHelper = new DBHelper(mContext);
        almanacData = new AlmanacData(mContext);
    }

    @Override
    public int getCount() {
        return mContentMaxNum;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.d(TAG,"instantiateItem pos = "+position+" mCurrentDate = "+mCurrentDate);
        int idx = position%mListViews.size();
        container.addView(mListViews.get(idx), 0);
        String datestr = getDate(mCurrentDate,mMidContentIndex,position);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("ecalender", new String[]{"jsondata"}, "date = ?", new String[]{datestr}, null, null, null);

        if(cursor.moveToFirst()) {
            almanacData.initViews(mListViews.get(idx));
            almanacData.setJsonString(cursor.getString(0),datestr);
            almanacData.parseJson();
            almanacData.updateViews();
        } else {
            if(mNeedUpdate) {
                ((OnDateNotCache) mContext).downloadHttpDate(datestr);
            }
        }
        return mListViews.get(idx);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mListViews.get(position % mListViews.size()));
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    public void setCurrentDate(String date) {
        mCurrentDate = date;
    }
    public String getDate(int pos) {
        int offset = pos - mMidContentIndex;
        String res = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = format.parse(mCurrentDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE)+offset);
            res = format.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
    public static String getDate(String curdate,int midindex,int pos) {
        int offset = pos - midindex;
        String res = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = format.parse(curdate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE)+offset);
            res = format.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public void setNeedUpdate(boolean need) {
        mNeedUpdate = need;
    }
}
