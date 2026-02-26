package com.senao.warehouse.ui;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tscdll.TscWifiActivity;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.ExtremePalletItem;
import com.senao.warehouse.util.CharUtil;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestExcelActivity extends Activity {
    private static final String TAG = TestExcelActivity.class.getSimpleName();
    private Button exportButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excel_test);

        exportButton = findViewById(R.id.button_export);
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportExcel();
            }
        });
    }

    private void exportExcel() {
        String fileName = "excel_file.xls";
        String filePath = Environment.getExternalStorageDirectory() + File.separator + fileName;
        File file = new File(filePath);

        try {
            // 创建工作簿
            WritableWorkbook workbook = Workbook.createWorkbook(file);
            // 创建工作表
            WritableSheet sheet = workbook.createSheet("Sheet1", 0);

            // 添加数据
            Label label = new Label(0, 0, "Hello");
            sheet.addCell(label);

            label = new Label(0, 1, "World");
            sheet.addCell(label);

            // 保存Excel文件
            workbook.write();
            workbook.close();

            Toast.makeText(this, "Excel exported successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            AppController.debug(e.toString());
            Toast.makeText(this, "Error exporting Excel", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            AppController.debug(e.toString());
            Toast.makeText(this, "Error exporting Excel", Toast.LENGTH_SHORT).show();
        }
    }
}
