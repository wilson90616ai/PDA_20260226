package com.senao.warehouse.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper { //本地資料庫
    private static final String DATABASE_NAME = "connectionSettings.db"; //資料庫名稱
    private static final int DATABASE_VERSION = 2; //資料庫版本，資料結構改變的時候要更改這個數字，通常是+1

    public static final String TABLE_NAME = "settings";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_IP = "ip";
    public static final String COLUMN_PORT = "port";
    public static final String COLUMN_SFC_IP = "sfc_ip";
    public static final String COLUMN_API_1 = "api_1"; //for WMS
    public static final String COLUMN_API_2 = "api_2"; //for ERP的EIQC帳號
    public static final String COLUMN_API_3 = "api_3"; //for SFC DB2
    public static final String COLUMN_ERP_IP = "erp_ip";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_IP + " TEXT, " +
                    COLUMN_PORT + " TEXT, " +
                    COLUMN_SFC_IP + " TEXT, " +
                    COLUMN_API_1 + " TEXT, " +
                    COLUMN_API_2 + " TEXT, " +
                    COLUMN_API_3 + " TEXT, " +
                    COLUMN_ERP_IP + " TEXT);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
