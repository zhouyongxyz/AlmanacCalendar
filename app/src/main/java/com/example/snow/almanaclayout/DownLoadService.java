package com.example.snow.almanaclayout;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class DownLoadService extends Service implements AlmanacTask.OnDownloadDone{
    private final static String TAG = "DownLoadService";
    private DownLoadBinder binder = new DownLoadBinder();
    private boolean mTaskIsRunning = false;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public class DownLoadBinder extends Binder {
        public DownLoadService getService() {
            return DownLoadService.this;
        }
    }

    public void test() {
        Log.d(TAG, "test service...");
    }

    public void download(int year,int start,int end) {
        if(!mTaskIsRunning) {
            mTaskIsRunning = true;
            Log.d(TAG,"download task is running ...");
            new AlmanacTask(this).execute(AlmanacTask.OP_GET_MONTHS, year, start, end);
        }
    }

    @Override
    public void updateDateUI() {
        Log.d(TAG,"download task is done .");
        mTaskIsRunning = false;
    }
}
