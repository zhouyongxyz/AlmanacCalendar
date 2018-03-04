package com.example.snow.almanaclayout;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";
    private static final String DATABASE_NAME = "ecalender.db";
    private final static String FILE_NAME = "assets/almanac.dat";
    private static final int DATABASE_VERSION = 112;

    private Context mContext;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        loadDatas(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading settings database from version " + oldVersion + " to "
                + newVersion);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE ecalender (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date TEXT," +
                "jsondata TEXT" +
                ");");
    }

    private void loadDatas(SQLiteDatabase db) {
        SQLiteStatement stmt = null;
        try {
            InputStream inputStream = mContext.getClass().getClassLoader().getResourceAsStream(FILE_NAME);
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                data.write(buffer, 0, len);
            }
            JSONArray jsonArray = new JSONObject(data.toString()).getJSONArray("alldata");
            for(int i= 0;i<jsonArray.length();i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                stmt = db.compileStatement("INSERT OR IGNORE INTO ecalender(date,jsondata)"
                        + " VALUES(?,?);");
                stmt.bindString(1, jsonObject.get("date").toString());
                stmt.bindString(2, jsonObject.get("jsondata").toString());
                stmt.execute();
            }
            Log.d(TAG, "json length = " + jsonArray.length());

        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (stmt != null) stmt.close();
        }
    }
}
