package com.thetratruoc.vn.token;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: Vuong
 * Date: 29/07/2013
 * Time: 13:46
 */
public class DBAdapter {

    public static final String TAG = "DBAdapter";

    public static final String SERIAL_ID = "s_serial_id";
    public static final String KEY = "s_key";
    public static final String PLATFORM = "s_platform";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDB;

    private static final String CREATE_TB_TOKEN = "create table platform (s_serial_id VARCHAR(20) NOT NULL PRIMARY KEY, s_key VARCHAR(64), s_platform VARCHAR(128));";
    private static final String DATABASE_NAME = "T-Token";
    private static final String TABLE_TOKEN = "platform";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_PASS = "tbmd";
    private static final String CREATE_TB_PASS = "create table tbmd (s_md VARCHAR(128));";

    public static final String PASSWORD = "s_md";

    private final Context mContext;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name,
                              CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TB_TOKEN);
            db.execSQL(CREATE_TB_PASS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG, "Upgrading DB");
            db.execSQL("DROP TABLE IF EXISTS platform");
            db.execSQL("DROP TABLE IF EXISTS tbmd");
            onCreate(db);
        }
    }

    public DBAdapter(Context ctx) {
        this.mContext = ctx;
    }

    public DBAdapter open() {
        mDbHelper = new DatabaseHelper(mContext, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public long createToken(String serialID, String key, String platform) {
        ContentValues inititalValues = new ContentValues();
        inititalValues.put(SERIAL_ID, serialID);
        inititalValues.put(KEY, key);
        inititalValues.put(PLATFORM, platform);
        return mDB.insert(TABLE_TOKEN, null, inititalValues);
    }

    public boolean deleteToken(String serialID) {
        return mDB.delete(TABLE_TOKEN, SERIAL_ID + "=?", new String[]{serialID}) > 0;
    }

    public boolean updateToken(String serialID, String platform) {
        ContentValues args = new ContentValues();
        args.put(PLATFORM, platform);
        return mDB.update(TABLE_TOKEN, args, SERIAL_ID + "=?", new String[]{serialID}) > 0;
    }

    public Cursor getAllToken() {
        return mDB.query(TABLE_TOKEN, new String[]{SERIAL_ID, KEY, PLATFORM}, null, null, null, null, null);
    }

    public long createPass(String password) {
        ContentValues inititalValues = new ContentValues();
        inititalValues.put(PASSWORD, Common.md5(password));
        return mDB.insert(TABLE_PASS, null, inititalValues);
    }

    public Cursor login(String password) {
        String selection = "s_md = ?";
        String[] selectionArgs = {Common.md5(password)};
        return mDB.query(TABLE_PASS, new String[]{PASSWORD}, selection, selectionArgs, null, null, null);
    }

    public boolean updatePass(String newPassword) {
        ContentValues args = new ContentValues();
        args.put(PASSWORD, Common.md5(newPassword));
        return mDB.update(TABLE_PASS, args, null, null) > 0;
    }

    public Cursor getAllPass() {
        return mDB.query(TABLE_PASS, new String[]{PASSWORD}, null, null, null, null, null);
    }

    public boolean deleteAllToken() {
        return mDB.delete(TABLE_TOKEN, null, null) > 0;
    }

    public boolean deleteAllPass() {
        return mDB.delete(TABLE_PASS, null, null) > 0;
    }

}
