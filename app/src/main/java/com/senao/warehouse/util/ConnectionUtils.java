package com.senao.warehouse.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.senao.warehouse.database.ConnectionDetails;

public class ConnectionUtils {

    private static final String PREFS_NAME = "ConnectionPrefs";
    private static final String PREF_CURRENT_CONNECTION_ID = "currentConnectionId";

    public static long getCurrentConnectionId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getLong(PREF_CURRENT_CONNECTION_ID, -1);
    }

    public static ConnectionDetails getCurrentConnectionDetails(Context context) {
        long connectionId = getCurrentConnectionId(context);
        if (connectionId == -1) {
            return null;
        }

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME,
                null,
                DatabaseHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(connectionId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
            String ip = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_IP));
            String port = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PORT));
            String sfcIp = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SFC_IP));
            String api_1 = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_API_1));
            String api_2 = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_API_2));
            String api_3 = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_API_3));
            String erpIp = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ERP_IP));

            cursor.close();

            return new ConnectionDetails(name, ip, port, sfcIp, api_1, api_2, api_3, erpIp);
        }

        return null;
    }
}

