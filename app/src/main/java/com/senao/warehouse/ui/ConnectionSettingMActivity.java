package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.util.DatabaseHelper;

public class ConnectionSettingMActivity extends Activity {
    private DatabaseHelper dbHelper;
    private ListView connectionList;
    private Button addButton, saveButton, selectButton;
    private EditText connectionName, ip, port, sfcIp, erpIp;
    private EditText api_1, api_2, api_3; //20260213 Ann Add
    private View settingsLayout;
    private TextView currentConnection;
    private String originalName;
    private long selectedConnectionId = -1;
    private long currentSettingId = -1;
    private ConnectionAdapter adapter;
    private static final String PREFS_NAME = "ConnectionPrefs";
    private static final String PREF_CURRENT_CONNECTION_ID = "currentConnectionId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_setting_m);

        dbHelper = new DatabaseHelper(this);
        connectionName = findViewById(R.id.connection_name);
        ip = findViewById(R.id.ip);
        port = findViewById(R.id.port);
        sfcIp = findViewById(R.id.sfc_ip);
        api_1 = findViewById(R.id.api_1);
        api_2 = findViewById(R.id.api_2);
        api_3 = findViewById(R.id.api_3);
        erpIp = findViewById(R.id.erp_ip);
        settingsLayout = findViewById(R.id.settings_layout);
        currentConnection = findViewById(R.id.current_connection);

        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsLayout();
            }
        });

        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });

        selectButton = findViewById(R.id.select_button);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCurrentSetting();
            }
        });

        connectionList = findViewById(R.id.connection_list);
        connectionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loadSettingDetails(id);
            }
        });
        connectionList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteConfirmationDialog(id);
                return true;
            }
        });

        loadSettings();
        loadCurrentConnection();

        //檢查並插入預設連線資料
        //insertDefaultConnection();
    }

    private void insertDefaultConnection() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, null, null, null, null, null, null);

        if (cursor.getCount() == 0) {
            SQLiteDatabase writableDb = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();

            //神準正式區   10.0.200.172   8080     https://scmportal.senao.com/MES/invoke3?sCode=
            values.put(DatabaseHelper.COLUMN_NAME, "神準正式區");
            values.put(DatabaseHelper.COLUMN_IP, "10.0.200.172");
            values.put(DatabaseHelper.COLUMN_PORT, "8080");
            values.put(DatabaseHelper.COLUMN_SFC_IP, "http://wmsportal.senao.com/MES"); //https://scmportal.senao.com/MES/invoke6?sCode=
            values.put(DatabaseHelper.COLUMN_API_1, "5"); //WMS
            values.put(DatabaseHelper.COLUMN_API_2, "9"); //SFC DB2
            values.put(DatabaseHelper.COLUMN_API_3, "10"); //ERP
            values.put(DatabaseHelper.COLUMN_ERP_IP, "");

            //writableDb.insert(DatabaseHelper.TABLE_NAME, null, values);
            selectedConnectionId = writableDb.insert(DatabaseHelper.TABLE_NAME, null, values);

            //插入第二行預設值  神準測試區  10.0.204.170   18080    https://apiportal02.senao.com/MES/invoke9?sCode=
            ContentValues values2 = new ContentValues();
            values2.put(DatabaseHelper.COLUMN_NAME, "神準測試區");
            values2.put(DatabaseHelper.COLUMN_IP, "10.0.204.170");
            values2.put(DatabaseHelper.COLUMN_PORT, "18080");
            values2.put(DatabaseHelper.COLUMN_SFC_IP, "https://apiportal02.senao.com/MES"); //https://apiportal02.senao.com/MES/invoke9?sCode=
            values2.put(DatabaseHelper.COLUMN_API_1, "2"); //WMS
            values2.put(DatabaseHelper.COLUMN_API_2, "9"); //SFC DB2
            values2.put(DatabaseHelper.COLUMN_API_3, "3"); //ERP
            values2.put(DatabaseHelper.COLUMN_ERP_IP, "");
            writableDb.insert(DatabaseHelper.TABLE_NAME, null, values2);

            /*ContentValues values3 = new ContentValues();
            values3.put(DatabaseHelper.COLUMN_NAME, "BLD正式區");
            values3.put(DatabaseHelper.COLUMN_IP, "10.0.208.172");
            values3.put(DatabaseHelper.COLUMN_PORT, "8080");
            values3.put(DatabaseHelper.COLUMN_SFC_IP, "https://scmportal.senao.com/MES"); //https://scmportal.senao.com/MES/invoke6?sCode=
            values3.put(DatabaseHelper.COLUMN_ERP_IP, "");
            writableDb.insert(DatabaseHelper.TABLE_NAME, null, values3);*/

            /*VM  VNFG01
            10.0.210.12
            8080
            https://10.0.210.9/MES/invoke9?sCode=
            https://vntst-apiportal.senao.com/MES/invok9?sCode=*/
            /*ContentValues values4 = new ContentValues();
            values4.put(DatabaseHelper.COLUMN_NAME, "VM");
            values4.put(DatabaseHelper.COLUMN_IP, "10.0.210.12");
            values4.put(DatabaseHelper.COLUMN_PORT, "8080");
            values4.put(DatabaseHelper.COLUMN_SFC_IP, "https://vntst-apiportal.senao.com/MES"); //https://vntst-apiportal.senao.com/MES/invoke9?sCode=
            values4.put(DatabaseHelper.COLUMN_ERP_IP, "");
            writableDb.insert(DatabaseHelper.TABLE_NAME, null, values4);*/

            ContentValues values5 = new ContentValues();
            values5.put(DatabaseHelper.COLUMN_NAME, "越南正式區");
            values5.put(DatabaseHelper.COLUMN_IP, "10.1.200.4");
            values5.put(DatabaseHelper.COLUMN_PORT, "8080");
            values5.put(DatabaseHelper.COLUMN_SFC_IP, "http://vnwmsportal.senao.com/MES"); //https://vn-scmportal.senao.com/MES/invoke6?sCode=
            values5.put(DatabaseHelper.COLUMN_API_1, "5"); //WMS
            values5.put(DatabaseHelper.COLUMN_API_2, "9"); //SFC DB2
            values5.put(DatabaseHelper.COLUMN_API_3, "10"); //ERP
            values5.put(DatabaseHelper.COLUMN_ERP_IP, "");
            writableDb.insert(DatabaseHelper.TABLE_NAME, null, values5);

            ContentValues values6 = new ContentValues();
            values6.put(DatabaseHelper.COLUMN_NAME, "墨西哥TSMT");
            values6.put(DatabaseHelper.COLUMN_IP, "172.18.8.5");
            values6.put(DatabaseHelper.COLUMN_PORT, "8080");
            values6.put(DatabaseHelper.COLUMN_SFC_IP, "http://wmsportal.senao.com/MES"); //https://scmportal.senao.com/MES/invoke6?sCode=
            values6.put(DatabaseHelper.COLUMN_API_1, "5"); //WMS
            values6.put(DatabaseHelper.COLUMN_API_2, "9"); //SFC DB2
            values6.put(DatabaseHelper.COLUMN_API_3, "10"); //ERP
            values6.put(DatabaseHelper.COLUMN_ERP_IP, "");
            writableDb.insert(DatabaseHelper.TABLE_NAME, null, values6);

            Toast.makeText(this, "已插入預設連線資料", Toast.LENGTH_SHORT).show();
            saveCurrentConnection(selectedConnectionId);

            cursor.close();
            loadSettings(); //重新加載設置以顯示預設連線
        } else {
            cursor.close();
        }
    }

    private void loadSettings() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, null, null, null, null, null, null);

        if (cursor.getCount() == 0) {
            showSettingsLayout();
        } else {
            showConnectionList(cursor);
        }
    }

    private void showSettingsLayout() {
        connectionList.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);
        selectButton.setVisibility(View.GONE);
        settingsLayout.setVisibility(View.VISIBLE);
        originalName = null;
        currentSettingId = -1;
    }

    private void showConnectionList(Cursor cursor) {
        if (cursor == null) {
            return;
        }

        settingsLayout.setVisibility(View.GONE);
        addButton.setVisibility(View.VISIBLE);
        selectButton.setVisibility(View.VISIBLE);
        currentConnection.setVisibility(View.VISIBLE);
        adapter = new ConnectionAdapter(cursor);
        connectionList.setVisibility(View.VISIBLE);
        connectionList.setAdapter(adapter);
    }

    private void loadSettingDetails(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME,
                null,
                DatabaseHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            connectionName.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
            ip.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_IP)));
            port.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PORT)));
            sfcIp.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SFC_IP)));
            api_1.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_API_1)));
            api_2.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_API_2)));
            api_3.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_API_3)));
            erpIp.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ERP_IP)));

            originalName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
            currentSettingId = id;
            showSettingsLayout();

            cursor.close();
        }
    }

    private void saveSettings() {
        String name = connectionName.getText().toString();
        String ipAddress = ip.getText().toString();
        String portNumber = port.getText().toString();
        String sfcIpAddress = sfcIp.getText().toString();
        String api_1_number = api_1.getText().toString();
        String api_2_number = api_2.getText().toString();
        String api_3_number = api_3.getText().toString();
        String erpIpAddress = erpIp.getText().toString();

        if (name.isEmpty() || ipAddress.isEmpty() || portNumber.isEmpty() || sfcIpAddress.isEmpty() || api_1_number.isEmpty() || api_2_number.isEmpty() || api_3_number.isEmpty()) {
            Toast.makeText(this, getString(R.string.label_fill_in_all_required_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_IP, ipAddress);
        values.put(DatabaseHelper.COLUMN_PORT, portNumber);
        values.put(DatabaseHelper.COLUMN_SFC_IP, sfcIpAddress);
        values.put(DatabaseHelper.COLUMN_API_1, api_1_number);
        values.put(DatabaseHelper.COLUMN_API_2, api_2_number);
        values.put(DatabaseHelper.COLUMN_API_3, api_3_number);
        values.put(DatabaseHelper.COLUMN_ERP_IP, erpIpAddress);

        //檢查名稱是否已存在
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME,
                new String[]{DatabaseHelper.COLUMN_ID},
                DatabaseHelper.COLUMN_NAME + "=?",
                new String[]{name},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            //名稱已存在，更新資料
            long id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
            db.update(DatabaseHelper.TABLE_NAME, values, DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
            Toast.makeText(this, getString(R.string.label_device_updated), Toast.LENGTH_SHORT).show();
        } else {
            //名稱不存在，新增資料
            db.insert(DatabaseHelper.TABLE_NAME, null, values);
            Toast.makeText(this, getString(R.string.label_device_saved), Toast.LENGTH_SHORT).show();
        }

        if (cursor != null) {
            cursor.close();
        }

        connectionName.setText("");
        ip.setText("");
        port.setText("");
        sfcIp.setText("");
        api_1.setText("");
        api_2.setText("");
        api_3.setText("");
        erpIp.setText("");

        loadSettings();
    }

    private void showDeleteConfirmationDialog(final long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME,
                new String[]{DatabaseHelper.COLUMN_NAME},
                DatabaseHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
            cursor.close();

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.label_delete_settings))
                    .setMessage(getString(R.string.label_delete_this_setting) + "：" + name + "?")
                    .setPositiveButton(getString(R.string.label_delete2), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            deleteSetting(id);
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }
    }

    private void deleteSetting(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        Toast.makeText(this, getString(R.string.label_setting_deleted), Toast.LENGTH_SHORT).show();
        loadSettings();
    }

    private void selectCurrentSetting() {
        if (selectedConnectionId != -1) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(DatabaseHelper.TABLE_NAME,
                    null,
                    DatabaseHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(selectedConnectionId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String selectedName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
                currentConnection.setText(getString(R.string.label_current_connection) + "：" + selectedName);
                currentConnection.setVisibility(View.VISIBLE);

                //保存當前連線ID到SharedPreferences
                saveCurrentConnection(selectedConnectionId);

                //在此處添加任何需要執行的連接操作
                //例如，連接到該設定的服務器
                Toast.makeText(this, getString(R.string.label_connection_setting_selected) + "：" + selectedName, Toast.LENGTH_SHORT).show();

                cursor.close();
            }
        } else {
            Toast.makeText(this, getString(R.string.label_select_a_setting), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCurrentConnection(long connectionId) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(PREF_CURRENT_CONNECTION_ID, connectionId);
        editor.apply();
    }

    private void loadCurrentConnection() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long connectionId = preferences.getLong(PREF_CURRENT_CONNECTION_ID, -1);

        if (connectionId != -1) {
            selectedConnectionId = connectionId;
            selectCurrentSetting();
        }
    }

    @Override
    public void onBackPressed() {
        if (settingsLayout.getVisibility() == View.VISIBLE) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, null, null, null, null, null, null);
            showConnectionList(cursor);
        } else {
            super.onBackPressed();
        }
    }

    private class ConnectionAdapter extends BaseAdapter {
        private Cursor cursor;

        public ConnectionAdapter(Cursor cursor) {
            this.cursor = cursor;
        }

        @Override
        public int getCount() {
            return cursor.getCount();
        }

        @Override
        public Object getItem(int position) {
            cursor.moveToPosition(position);
            return cursor;
        }

        @Override
        public long getItemId(int position) {
            cursor.moveToPosition(position);
            return cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_connection, parent, false);
            }

            cursor.moveToPosition(position);

            TextView nameTextView = convertView.findViewById(R.id.connection_name);
            nameTextView.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));

            RadioButton radioButton = convertView.findViewById(R.id.connection_radio_button);
            radioButton.setChecked(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID)) == selectedConnectionId);
            radioButton.setTag(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID)));
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedConnectionId = (Long) v.getTag();
                    notifyDataSetChanged();
                }
            });

            return convertView;
        }
    }
}
