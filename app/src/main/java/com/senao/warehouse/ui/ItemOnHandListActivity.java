package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.adapter.ListItemOnHandAdapter;
import com.senao.warehouse.asynctask.AsyncResponse;
import com.senao.warehouse.asynctask.CallWebApi;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.DateCode;
import com.senao.warehouse.database.ItemOnHand;
import com.senao.warehouse.database.ItemOnHandInfoHelper;
import com.senao.warehouse.database.ItemOnHandRecord;
import com.senao.warehouse.database.ItemOnHandType;
import com.senao.warehouse.database.Locator;
import com.senao.warehouse.util.ReturnCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemOnHandListActivity extends Activity implements View.OnClickListener {
    private String errorInfo = "";
    private TextView mConnection;
    private ItemOnHandInfoHelper conditionHelper;
    private ListItemOnHandAdapter mAdapter;
    private List<ItemOnHand> dataList = new ArrayList<>();
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            conditionHelper = new Gson().fromJson(extras.getString("CONDITION_INFO"), ItemOnHandInfoHelper.class);
            //AppController.debug("conditionHelper = " + conditionHelper.toString());

            if (conditionHelper == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_condictions), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_condictions), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_on_hand_list);
        Button btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(this);

        searchView = findViewById(R.id.input_search_text);
        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) searchView.findViewById(id);
        textView.setTextColor(Color.BLACK);
        textView.setHintTextColor(Color.GRAY);
        searchView.setIconifiedByDefault(false); //關閉icon切換
        searchView.setFocusable(false); //不要進畫面就跳出輸入鍵盤
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return true;
            }
        });

        ListView listView = findViewById(R.id.listView);

        mAdapter = new ListItemOnHandAdapter(this, dataList);
        listView.setAdapter(mAdapter);
        queryList();
    }

    private void queryList() {
        CallWebApi task = new CallWebApi(this, AppController.getProperties("GetItemOnHand"), new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                /*AppController.debug("onSuccess = ");

                if(result.toString().length()>4000){
                    for(int i=0;i<result.toString().length();i+=4000){
                        if(i+4000<result.toString().length()){
                            AppController.debug("onSuccess = " + result.toString().substring(i, i+4000));
                        }else{
                            AppController.debug("onSuccess = " + result.toString());
                        }
                    }
                }*/

                AppController.debug("onSuccess1 = " + result.toString());
                conditionHelper = (ItemOnHandInfoHelper) result;
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
                //AppController.debug("conditionHelper1 = " + conditionHelper.toString().substring(0, 4000));
                //AppController.debug("conditionHelper2 = " + conditionHelper.toString().substring(4000));
                //AppController.debug("conditionHelper3 = " + conditionHelper.toString().substring(8000, 12000));
                setListData(conditionHelper); //呈現查詢結果
            }

            @Override
            public void onFailure() {
                AppController.debug("onFailure : result = null");
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                setListData(null); //呈現查詢結果
            }

            @Override
            public void onError(BasicHelper result) {
                AppController.debug("onError = " + result.toString());

                if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                    mConnection.setText(getString(R.string.connect_error));
                } else {
                    mConnection.setText(getString(R.string.db_return_error));
                }

                errorInfo = result.getStrErrorBuf();
                mConnection.setTextColor(Color.RED);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                setListData(conditionHelper); //呈現查詢結果
            }
        });

        task.execute(conditionHelper);
    }

    private void setListData(final ItemOnHandInfoHelper tempInfo) { //呈現查詢結果
        dataList.clear();

        if (tempInfo != null && tempInfo.getRowList() != null) {
            //在處理之前，先把 rowList 依 itemId + lot + locator + subinventory 聚合
            Map<String, ItemOnHandRecord> mergedMap = new LinkedHashMap<>();

            for (ItemOnHandRecord item : tempInfo.getRowList()) {
                String key = item.getItemId() + "_" + item.getLot() + "_"
                        + (item.getLocator() == null ? "" : item.getLocator()) + "_"
                        + (item.getSubinventoryCode() == null ? "" : item.getSubinventoryCode());

                if (mergedMap.containsKey(key)) {
                    ItemOnHandRecord existing = mergedMap.get(key);
                    existing.setQty(existing.getQty().add(item.getQty())); // BigDecimal add
                } else {
                    //放入一個新的 (clone 以免修改原物件)
                    ItemOnHandRecord copy = new ItemOnHandRecord();
                    copy.setGrp(item.getGrp());
                    copy.setType(item.getType());
                    copy.setItemCtrl(item.getItemCtrl());
                    copy.setItemId(item.getItemId());
                    copy.setItemNo(item.getItemNo());
                    copy.setDescription(item.getDescription());
                    copy.setSubinventoryCode(item.getSubinventoryCode());
                    copy.setLot(item.getLot());
                    copy.setLocator(item.getLocator());
                    copy.setQty(item.getQty());
                    mergedMap.put(key, copy);
                }
            }

            //把合併後的結果塞回 tempInfo
            tempInfo.setRowList(new ArrayList<>(mergedMap.values()));
        }

        if (tempInfo != null && tempInfo.getRowList() != null) {
            List<ItemOnHand> list = new ArrayList<>();
            List<ItemOnHandType> typeList;
            List<Locator> locatorList = null;
            List<DateCode> lotList;
            ItemOnHand targetItem;
            ItemOnHandType typeItem;
            Locator locatorItem;
            DateCode lotItem;

            for (ItemOnHandRecord item : tempInfo.getRowList()) {
                AppController.debug("ItemOnHandRecord => item.getType()====>" + item.getType() + " ,item.getLot() = " + item.getLot() + " ,item.getLocator() = " + item.getLocator() + " ,item.getSubinventoryCode() = " + item.getSubinventoryCode() + " ,item.getQty() = " + item.getQty());
            }

            for (ItemOnHandRecord item : tempInfo.getRowList()) {
                //AppController.debug("ItemOnHandRecord => item.getType()====>" + item.getType() + " ,item.getLot() = " + item.getLot() + " ,item.getLocator() = " + item.getLocator() + " ,item.getSubinventoryCode() = " + item.getSubinventoryCode());
                targetItem = null;

                for (ItemOnHand subItem : list) {
                    //AppController.debug("ItemOnHandRecord subItem.getItemNo() = "+subItem.getItemNo());
                    //AppController.debug("ItemOnHandRecord item.getItemNo() = "+item.getItemNo());

                    if (subItem.getItemNo().equals(item.getItemNo())) {//同料號都是true
                        targetItem = subItem;
                        break;
                    }
                }

                if (targetItem == null) {
                    AppController.debug("item==============================>" + item.getLot());
                    AppController.debug("item.getLot() = " + item.getLot() + " ,item.getLocator() = " + item.getLocator() + " ,item.getSubinventoryCode() = " + item.getSubinventoryCode());
                    AppController.debug("item========================end======>" + item.getLot());

                    targetItem = new ItemOnHand();
                    targetItem.setItemId(item.getItemId());
                    targetItem.setItemControl(item.getItemCtrl());
                    targetItem.setItemNo(item.getItemNo());

                    targetItem.setItemDescription(item.getDescription());
                    targetItem.setQty(item.getQty());
                    typeList = new ArrayList<>();
                    typeItem = new ItemOnHandType();
                    typeItem.setType(item.getType());
                    typeItem.setQty(item.getQty());
                    locatorList = new ArrayList<>();
                    locatorItem = new Locator();

                    locatorItem.setSubinventory(item.getSubinventoryCode());
                    //AppController.debug("item.getSubinventoryCode() = "+item.getSubinventoryCode());
                    locatorItem.setLocator(item.getLocator());
                    locatorItem.setQty(item.getQty());
                    lotList = new ArrayList<>();

                    if (!TextUtils.isEmpty(item.getLot())) {
                        lotItem = new DateCode();
                        lotItem.setDateCode(item.getLot());
                        lotItem.setQty(item.getQty());
                        lotItem.setLocator(item.getLocator());
                        lotList.add(lotItem);
                    }else{
                        lotItem = new DateCode();
                        lotItem.setDateCode("");
                        lotItem.setQty(item.getQty());
                        lotItem.setLocator(item.getLocator());
                        lotList.add(lotItem);
                    }

                    locatorItem.setDateCodeList(lotList);
                    locatorItem.setLocator_dateCodeList(item.getLocator(), lotList);

                    locatorList.add(locatorItem);
                    typeItem.setLocatorList(locatorList);
                    typeList.add(typeItem);
                    targetItem.setTypeList(typeList);
                    list.add(targetItem);
                } else {
                    targetItem.setQty(targetItem.getQty().add(targetItem.getQty()));
                    typeItem = null;

                    for (ItemOnHandType tmpTypeItem : targetItem.getTypeList()) {
                        if (tmpTypeItem.getType().equals(item.getType())) {
                            typeItem = tmpTypeItem;
                            break;
                        }
                    }

                    if (typeItem == null) {
                        AppController.debug("item==============================2:" + item.getLot());
                        AppController.debug("item.getLot() = " + item.getLot() + " ,item.getLocator() = " + item.getLocator() + " ,item.getSubinventoryCode() = " + item.getSubinventoryCode());
                        AppController.debug("item====================end==========2:" + item.getLot());

                        typeList = new ArrayList<>(Arrays.asList(targetItem.getTypeList()));
                        typeItem = new ItemOnHandType();
                        typeItem.setType(item.getType());
                        typeItem.setQty(item.getQty());
                        locatorList = new ArrayList<>();
                        locatorItem = new Locator();
                        locatorItem.setSubinventory(item.getSubinventoryCode());
                        locatorItem.setLocator(item.getLocator());
                        locatorItem.setQty(item.getQty());
                        lotList = new ArrayList<>();

                        if (!TextUtils.isEmpty(item.getLot())) {
                            lotItem = new DateCode();
                            lotItem.setDateCode(item.getLot());
                            lotItem.setQty(item.getQty());
                            lotItem.setLocator(item.getLocator());
                            lotList.add(lotItem);
                        }

                        locatorItem.setDateCodeList(lotList);
                        locatorItem.setLocator_dateCodeList(item.getLocator(), lotList);
                        locatorList.add(locatorItem);
                        typeItem.setLocatorList(locatorList);
                        typeList.add(typeItem);
                        targetItem.setTypeList(typeList);
                    } else {
                        typeItem.setQty(typeItem.getQty().add(item.getQty()));
                        locatorItem = null;

                        for (Locator tmpLocatorItem : typeItem.getLocatorList()) {
                            //同倉別且同LOT的話
                            if ((tmpLocatorItem.getSubinventory() == null ? "" : tmpLocatorItem.getSubinventory()).equals(item.getSubinventoryCode() == null ? "" : item.getSubinventoryCode())
                                && (tmpLocatorItem.getLocator() == null ? "" : tmpLocatorItem.getLocator()).equals(item.getLocator() == null ? "" : item.getLocator())) {
                                AppController.debug("tmpLocatorItem==============================3:" + tmpLocatorItem);
                                locatorItem = tmpLocatorItem; //設定locator的地方
                                break;
                            }
                        }

                        if (locatorItem == null) {
                            locatorList = new ArrayList<>(Arrays.asList(typeItem.getLocatorList()));
                            locatorItem = new Locator();
                            AppController.debug("item==============================3:" + item.getLot());
                            AppController.debug("item.getLot() = " + item.getLot() + " ,item.getLocator() = " + item.getLocator() + " ,item.getSubinventoryCode() = " + item.getSubinventoryCode());
                            AppController.debug("item====================end==========3:" + item.getLot());

                            locatorItem.setSubinventory(item.getSubinventoryCode());
                            locatorItem.setLocator(item.getLocator());
                            locatorItem.setQty(item.getQty());
                            lotList = new ArrayList<>();

                            if (!TextUtils.isEmpty(item.getLot())) {
                                lotItem = new DateCode();
                                lotItem.setDateCode(item.getLot());
                                lotItem.setQty(item.getQty());
                                lotItem.setLocator(item.getLocator());
                                lotList.add(lotItem);
                            }

                            locatorItem.setDateCodeList(lotList);
                            locatorItem.setLocator_dateCodeList(item.getLocator(), lotList);
                            locatorList.add(locatorItem);
                            typeItem.setLocatorList(locatorList);
                        } else {
                            AppController.debug("item==============================4:" + item.getLot());
                            AppController.debug("item.getLot() = " + item.getLot() + " ,item.getLocator() = " + item.getLocator() + " ,item.getSubinventoryCode() = " + item.getSubinventoryCode());
                            AppController.debug("item==================end============4:" + item.getLot());
                            //locatorItem.setLocator(item.getLocator());
                            locatorItem.setQty(locatorItem.getQty().add(item.getQty()));//總數
                            lotItem = null;

                            for (DateCode tmpLotItem : locatorItem.getDateCodeList()) {
                                if(item.getLocator() == null){
                                    item.setLocator("null");
                                }

                                //if (item.getLocator().equals(locatorItem.getLocator())) {
                                if ((tmpLotItem.getDateCode() == null ? "" : tmpLotItem.getDateCode()).equals(item.getLot() == null ? "" : item.getLot())) {
                                    AppController.debug("tmpLotItem==============================4:" + tmpLotItem);
                                    lotItem = tmpLotItem;
                                    break;
                                }
                            }

                            if (!TextUtils.isEmpty(item.getLot())) {
                                //for (DateCode tmpLotItem : locatorItem.getDateCodeList()) {
                                    //if (tmpLotItem.getDateCode().equals(item.getLot()) && item.getLocator().equals(locatorItem.getLocator())) {
                                        //AppController.debug("tmpLotItem==============================4:" + tmpLotItem);
                                        //lotItem = tmpLotItem;
                                        //break;
                                    //}
                                //}

                                if (lotItem == null) {
                                    lotList = new ArrayList<>(Arrays.asList(locatorItem.getDateCodeList()));
                                    lotItem = new DateCode();
                                    lotItem.setDateCode(item.getLot());
                                    lotItem.setQty(item.getQty());
                                    lotItem.setLocator(item.getLocator());
                                    lotList.add(lotItem);
                                    locatorItem.setDateCodeList(lotList);
                                    locatorItem.setLocator_dateCodeList(item.getLocator(), lotList);
                                } else {
                                    lotItem.setQty(lotItem.getQty().add(item.getQty()));
                                }
                            }else{
                                AppController.debug("locatorItem=>>>>>>>>>>>>:"+locatorItem );
                                //locatorList = new ArrayList<>(Arrays.asList(typeItem.getLocatorList()));

                                if (lotItem == null) {
                                    lotList = new ArrayList<>(Arrays.asList(locatorItem.getDateCodeList()));
                                    lotItem = new DateCode();

                                    lotItem.setQty(item.getQty());
                                    lotItem.setLocator(item.getLocator());
                                    lotList.add(lotItem);
                                    locatorItem.setSubinventory(item.getSubinventoryCode());
                                    locatorItem.setLocator(item.getLocator());
                                    locatorItem.setQty(item.getQty());
                                    locatorItem.setDateCodeList(lotList);
                                    locatorItem.setLocator_dateCodeList(item.getLocator(), lotList);
                                } else {
                                    lotItem.setQty(lotItem.getQty().add(item.getQty()));
                                }
                            }
                        }
                    }
                }
            }

            tempInfo.setList(list);
        }

        AppController.debug("tempInfo=>>>>>>>>>>>>:" + tempInfo);
        //for (ItemOnHandRecord item : tempInfo.getRowList()) {
            //AppController.debug("ItemOnHandRecord => item.getType()====>" + item.getType() + " ,item.getLot() = " + item.getLot() + " ,item.getLocator() = " + item.getLocator() + " ,item.getSubinventoryCode() = " + item.getSubinventoryCode());
        //}

        if (tempInfo != null && tempInfo.getList() != null) {
            dataList.addAll(Arrays.asList(tempInfo.getList()));
        }

        mAdapter.notifyDataSetChanged();

        if (dataList.size() == 0) {
            Toast.makeText(getApplicationContext(), "無庫存", Toast.LENGTH_LONG).show();
        }

        /*int i=0;
        for (ItemOnHand data: dataList) {
            AppController.debug("dataList = " +dataList);
            AppController.debug("dataList = " +dataList.get(i).getTypeList()[i].getLocatorList()[i].getLocator_dateCodeList());
            i++;
        }*/
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button_return) {
            finish();
        } else if (id == R.id.label_status) {
            showStatus();
        }
    }

    private void showStatus() {
        if (!TextUtils.isEmpty(errorInfo)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Error Msg");
            dialog.setMessage(errorInfo);
            dialog.setIcon(android.R.drawable.ic_dialog_alert);
            dialog.setCancelable(false);
            dialog.setNegativeButton( getString(R.string.btn_ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.dismiss();
                        }
                    });
            dialog.show();
        }
    }
}
