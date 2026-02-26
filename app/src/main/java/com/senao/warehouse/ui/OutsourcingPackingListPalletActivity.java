package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.handler.ReceivingHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class OutsourcingPackingListPalletActivity extends Activity implements View.OnClickListener {

    private static final String TAG = OutsourcingPackingListPalletActivity.class.getSimpleName();
    private TextView textViewMergenum, textViewPalletNum, textViewCartonQTY, LabelPartNo, LabelReelId, mConnection;
    private EditText txtQRCode, txtCNo, txtcWeight;
    private String errorInfo;
    private ListView listView;
    private SimpleAdapter adapter;
    private Button btnConfim, btnReturn, btnCancel, btnStockIn;
    private ArrayList<HashMap<String, String>> list = new ArrayList<>();
    private int delItemInfoPosition;
    private boolean isItemDelete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outsourcing_packing_method_list_pallet);

        textViewMergenum=findViewById(R.id.textview_merge_number);
        textViewCartonQTY=findViewById(R.id.textview_carton_qty);
        textViewPalletNum=findViewById(R.id.textview_ban_num);
        LabelPartNo=findViewById(R.id.label_part_no);
        LabelReelId=findViewById(R.id.label_reel_id);
        LabelPartNo.setText(getString(R.string.label_part_no2,"0"));
        LabelReelId.setText(getString(R.string.label_reel_id2,"0"));

        listView=findViewById(R.id.list_item);

        Bundle bundle = getIntent().getExtras();
        String MergeNum = bundle.getString("MergeNum");
        String Pallet = bundle.getString("Pallet");

        textViewCartonQTY.setText("0");
        textViewMergenum.setText(MergeNum);
        textViewPalletNum.setText(Pallet);

        txtQRCode = findViewById(R.id.edittext_qr_code);
        txtCNo = findViewById(R.id.edittext_c_no);
        txtcWeight = findViewById(R.id.edittext_c_weight);
        btnConfim=findViewById(R.id.button_confirm);
        btnReturn =  findViewById(R.id.button_return);
        btnCancel=findViewById(R.id.button_cancel);
        btnStockIn=findViewById(R.id.button_stock_in);

        btnReturn.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnStockIn.setOnClickListener(this);

        txtQRCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (txtQRCode.getText().toString().equals("")) {
                        txtQRCode.requestFocus();
                    } else {
                        txtCNo.requestFocus();
                    }
                }

                return false;
            }
        });

        txtCNo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (txtCNo.getText().toString().equals("")) {
                        txtCNo.requestFocus();
                    } else {
                        txtcWeight.requestFocus();
                    }
                }

                return false;
            }
        });

        txtcWeight.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (txtQRCode.getText().toString().equals("")) {
                        txtQRCode.requestFocus();
                        Toast.makeText(getApplicationContext(), getString(R.string.QR_CODE_is_not_null), Toast.LENGTH_SHORT).show();
                    } else if (txtCNo.getText().toString().equals("")) {
                        txtCNo.requestFocus();
                        Toast.makeText(getApplicationContext(), getString(R.string.box_num_is_not_null), Toast.LENGTH_SHORT).show();
                    } else if (txtcWeight.getText().toString().equals("")) {
                        txtcWeight.requestFocus();
                        Toast.makeText(getApplicationContext(), getString(R.string.c_weight_is_not_null), Toast.LENGTH_SHORT).show();
                    }
                }

                return false;
            }
        });

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(OutsourcingPackingListPalletActivity.this);
                    dialog.setTitle("Error Msg");
                    dialog.setMessage(errorInfo);
                    dialog.setIcon(android.R.drawable.ic_dialog_alert);
                    dialog.setCancelable(false);
                    dialog.setNegativeButton( getString(R.string.btn_ok),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int arg1) {
                                    dialog.dismiss();
                                }

                            });

                    dialog.show();
                }
            }
        });

        btnConfim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Confim", String.valueOf(v.getId()));
                if (txtQRCode.getText().toString().equals("")) {
                    txtQRCode.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.QR_CODE_is_not_null), Toast.LENGTH_SHORT).show();
                } else if (txtCNo.getText().toString().equals("")) {
                    txtCNo.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.box_num_is_not_null), Toast.LENGTH_SHORT).show();
                } else if (txtcWeight.getText().toString().equals("")) {
                    txtcWeight.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.c_weight_is_not_null), Toast.LENGTH_SHORT).show();
                } else {
                    hideKeyboard(txtcWeight);
                    if(doCheckReelID()){
                        cleanData();
                    }

                }
            }

        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            // Yes button clicked
//                            setDeletePacking();
                            list.remove(delItemInfoPosition);
                            setListData();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            // No button clicked
                            break;
                    }
                }
            };

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int oriPosition = list.indexOf(parent.getItemAtPosition(position));
                AppController.debug("onLongClick item " + oriPosition + " ReelID="
                        + list.get(oriPosition).get("reelid"));

                isItemDelete = true;
                delItemInfoPosition = oriPosition;
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        OutsourcingPackingListPalletActivity.this);
                builder.setTitle( getString(R.string.btn_ok)).setMessage(R.string.Make_sure_to_del_SKU)
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show();
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button_return) {
            returnPage();
        } else if (id == R.id.button_cancel) {
            cleanData();
        } else if (id == R.id.button_stock_in) {
            Intent intent = new Intent();
            intent.setClass(OutsourcingPackingListPalletActivity.this,OutsourcingMaterialShipmentPickingActivity.class);
            intent.putExtra("DataList",list);
            intent.putExtra("Pallet",textViewPalletNum.getText());
            intent.putExtra("Merge",textViewMergenum.getText());
            startActivity(intent);
        }
    }

    private void hideKeyboard(View edit) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
    }

    private void setListData() {
        listView.setVisibility(View.VISIBLE);
        HashSet carton = new HashSet<>();
        HashSet PartNo = new HashSet<>();
        HashSet reelid = new HashSet<>();

        for (HashMap<String, String> arrayitem : list) {
            carton.add(arrayitem.get("carton"));
            PartNo.add(arrayitem.get("PartNo"));
            reelid.add(arrayitem.get("reelid"));
        }

        LabelPartNo.setText(getString(R.string.label_part_no2,String.valueOf(PartNo.size())));
        LabelReelId.setText(getString(R.string.label_reel_id2,String.valueOf(reelid.size())));
        textViewCartonQTY.setText(String.valueOf(carton.size()));
        cleanData();

        adapter = new SimpleAdapter(this, list, R.layout.reel_id_item_carton,
                new String[]{"carton", "PartNo", "quantity","weight","reelid"},
                new int[]{R.id.txt_carton, R.id.txt_part_no, R.id.txt_number,
                        R.id.txt_weight,R.id.txt_reel_id}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                HashMap<String, String> items = (HashMap<String, String>) getItem(position);

                if (String.valueOf(items.get("wait")).equals("0")) {
                    view.setBackgroundColor(Color.GRAY);
                    view.setClickable(false);
                } else if(String.valueOf(items.get("qty")).equals("0")){//20220701
                    view.setBackgroundColor(Color.RED);
//                    view.setClickable(false);
                } else {
                    view.setBackgroundDrawable(getResources().getDrawable(R.drawable.custom_item));
                }

                return view;
            }
        };

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                int oriPosition = list.indexOf(parent.getItemAtPosition(position));
                AppController.debug("onClick item " + oriPosition + " ReelID="  + list.get(oriPosition).get("reelid"));
                AppController.debug("onClick item position ：" + position );
            }
        });
    }

    private String parseReelID(String QRCode){
        String result = null;

        if (!TextUtils.isEmpty(QRCode)) {
            if (QRCode.contains("@")) {
                String[] list = QRCode.split("@");

                if (list.length > 1 && list[0].length()==34) {
                    result = list[0];
                } else {
                    result = "false";
                }
            } else {
                result = "false";
            }
        }

        return result;
    }

    private String parseQty(String QRCode){
        String result = null;

        if (!TextUtils.isEmpty(QRCode)) {
            if (QRCode.contains("@")) {
                String[] list = QRCode.split("@");

                if (list.length > 2) {
                    result = list[2];
                } else {
                    result = "false";
                }
            } else {
                result = "false";
            }
        }

        return result;
    }

    private String parsePartNo(String QRCode){
        String result = null;

        if (!TextUtils.isEmpty(QRCode)) {
            if (QRCode.contains("@")) {
                String[] list = QRCode.split("@");
                if (list.length > 1) {
                    result = list[0].substring(0,12);
                }
            } else {
                result = QRCode;
            }
        }

        return result;
    }

    protected boolean doCheckReelID() {
        String QRCode=txtQRCode.getText().toString();
        String CNo=txtCNo.getText().toString();
        String CWeight=txtcWeight.getText().toString();

        HashMap<String, String> item = new HashMap<>();
        String reelid=parseReelID(QRCode);

        item.put("carton", CNo);
        item.put("PartNo", parsePartNo(QRCode));
        item.put("quantity", parseQty(QRCode));
        item.put("weight", CWeight);
        item.put("reelid", reelid);

        if (item.get("reelid")=="false") {
            Toast.makeText(getApplicationContext(), getString(R.string.reelid_incorrect_len_must_be_34), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (item.get("quantity")=="false") {
            Toast.makeText(getApplicationContext(), getString(R.string.reelid_incorrect_len_must_be_34), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (list.contains(item)) {
            Toast.makeText(getApplicationContext(), getString(R.string.carton_duplicate), Toast.LENGTH_SHORT).show();
            return false;
        }

        for (HashMap<String, String> arrayitem : list) {
            if (item.get("reelid").equals(arrayitem.get("reelid"))) {
                if (item.get("carton").equals(arrayitem.get("carton"))) {
                    Toast.makeText(getApplicationContext(), getString(R.string.carton_duplicate), Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.reelid_duplicate), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }

            if (item.get("carton").equals(arrayitem.get("carton"))) {
                if (!item.get("weight").equals(arrayitem.get("weight"))) {
                    Toast.makeText(getApplicationContext(), getString(R.string.carton_weight_not_equal), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        list.add(item);
        setListData();
        return true;
    }

    private void returnPage() {
        hideKeyboard(txtcWeight);
        finish();
    }

    private void cleanData() {
        txtQRCode.setText("");
        txtCNo.setText("");
        txtcWeight.setText("");
    }
}
