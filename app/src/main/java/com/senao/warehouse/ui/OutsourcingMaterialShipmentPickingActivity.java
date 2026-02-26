package com.senao.warehouse.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.senao.warehouse.AppController;
import com.senao.warehouse.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class OutsourcingMaterialShipmentPickingActivity extends Activity {

    private Button exportButton;
    private RadioButton rbAdd, rbDownload;
    private LinearLayout DownloadLayout, OperationLayout;
    private CheckBox Email,StorePath;
    private Button btnConfirm, btnCancel,btnReturn;
    private EditText ETMergeNo;
    private String filePath,Merge;
    private TextView Path;
    private Intent intent;
    private ArrayList<HashMap<String, String>> DataList = new ArrayList<>();

    private RadioGroup.OnCheckedChangeListener rgListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio_add:
                    if (rbAdd.isChecked()) {
                        DownloadLayout.setVisibility(View.GONE);
                    }
                    break;
                case R.id.radio_download:
                    if (rbDownload.isChecked()) {
                        DownloadLayout.setVisibility(View.VISIBLE);
                        Path.setText("no data");
                        filePath="no data";

                        if (Merge!=null){
                            String fileName = Merge+".xls";
                            Path.setText(fileName);
                            filePath = Environment.getExternalStorageDirectory() + File.separator + fileName;
                            Toast.makeText(getApplicationContext(), filePath, Toast.LENGTH_SHORT).show();
                            DataList =(ArrayList<HashMap<String, String>>) intent.getSerializableExtra("DataList");
                        }else{
                            Toast.makeText(getApplicationContext(), getString(R.string.no_data), Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }
        }




    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outsourcing_material_and_shipment_picking);

        RadioGroup rg = findViewById(R.id.radio_group);
        rg.setOnCheckedChangeListener(rgListener);

        rbAdd = findViewById(R.id.radio_add);
        rbAdd.setChecked(true);

        DownloadLayout = findViewById(R.id.DownloadLayout);
        DownloadLayout.setVisibility(View.GONE);

        rbDownload = findViewById(R.id.radio_download);
        Email = findViewById(R.id.cb_email);
        StorePath = findViewById(R.id.cb_store_file);
        btnCancel = findViewById(R.id.button_cancel);
        ETMergeNo = findViewById(R.id.edittext_merge_no);
        Path = findViewById(R.id.label_storage_path);

        intent = getIntent();
        Merge = intent.getStringExtra("Merge");
        if (Merge != null) {
            ETMergeNo.setText(Merge);
        }

        btnConfirm = findViewById(R.id.button_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Confirm", String.valueOf(v.getId()));

                if (rbAdd.isChecked()) {
                    if (ETMergeNo.getText().toString().trim().equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.merge_no_not_null), Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent= new Intent(OutsourcingMaterialShipmentPickingActivity.this, OutsourcingPackingMethodActivity.class);
                        intent.putExtra("MergeNum",ETMergeNo.getText().toString());
                        startActivity(intent);
                    }
                } else if (rbDownload.isChecked()) {
                    if (filePath == "no data") {

                    } else {
                        exportExcel();
                    }
                }
            }
        });

        btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnPage();
            }
        });
    }

    private void exportExcel() {
        File file = new File(filePath);

        try {
            // 创建工作簿
            WritableWorkbook workbook = Workbook.createWorkbook(file);

            // 创建工作表
            WritableSheet sheet = workbook.createSheet("Sheet1", 0);

            // 添加数据
            Label label = new Label(0, 0, getString(R.string.Outsourceing_Merge_Number));
            sheet.addCell(label);
            label = new Label(1, 0, getString(R.string.Outsourceing_Pallet));
            sheet.addCell(label);
            label = new Label(2, 0, getString(R.string.Outsourceing_Box));
            sheet.addCell(label);
            label = new Label(3, 0, getString(R.string.Outsourceing_Carton_Weight));
            sheet.addCell(label);
            label = new Label(4, 0, getString(R.string.Outsourceing_SKU));
            sheet.addCell(label);
            label = new Label(5, 0, getString(R.string.Outsourceing_Description));
            sheet.addCell(label);
            label = new Label(6, 0, getString(R.string.Outsourceing_Qty));
            sheet.addCell(label);
            label = new Label(7, 0, getString(R.string.Outsourceing_DC));
            sheet.addCell(label);
            label = new Label(8, 0, getString(R.string.Outsourceing_Reel_Id));
            sheet.addCell(label);
            String Pallet=intent.getStringExtra(getString(R.string.Outsourceing_Pallet2));
            Integer i=1;

            for (HashMap<String, String> arrayitem : DataList){
                label = new Label(0, i, Merge);
                sheet.addCell(label);
                label = new Label(1, i, Pallet);
                sheet.addCell(label);
                label = new Label(2, i, arrayitem.get("carton"));
                sheet.addCell(label);
                label = new Label(3, i, arrayitem.get("weight"));
                sheet.addCell(label);
                label = new Label(4, i, arrayitem.get("PartNo"));
                sheet.addCell(label);
                label = new Label(5, i, "品名");
                sheet.addCell(label);
                label = new Label(6, i,  arrayitem.get("quantity"));
                sheet.addCell(label);
                label = new Label(7, i,  arrayitem.get("reelid").substring(18,28));
                sheet.addCell(label);
                label = new Label(8, i, arrayitem.get("reelid"));
                sheet.addCell(label);
                AppController.debug( arrayitem.get("reelid"));
                i++;
            }

            // 保存Excel文件
            workbook.write();
            AppController.debug("Write");
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

    private void returnPage() {
        hideKeyboard(ETMergeNo);
        finish();
    }

    private void hideKeyboard(View edit) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
    }
}
