package com.senao.warehouse.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;

import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.DateCode;
import com.senao.warehouse.database.ItemOnHand;
import com.senao.warehouse.database.ItemOnHandType;
import com.senao.warehouse.database.Locator;
import com.senao.warehouse.util.Util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ListItemOnHandAdapter extends BaseAdapter implements Filterable {
    private final LayoutInflater mInflater;
    private List<ItemOnHand> mList;
    private List<ItemOnHand> originalitem;
    private Context mContext;

    public ListItemOnHandAdapter(Context context, List<ItemOnHand> list) {
        mList = list;
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.on_hand_item, parent, false);

            holder.mItemNo = convertView.findViewById(R.id.txt_item_no);
            holder.mItemControl = convertView.findViewById(R.id.txt_item_control);
            holder.mItemDescription = convertView.findViewById(R.id.txt_item_description);
            holder.mNotInStorageQty = convertView.findViewById(R.id.txt_not_in_storage_qty);
            holder.mLlTempReceive = convertView.findViewById(R.id.ll_temp_receive);
            holder.mTempReceiveQty = convertView.findViewById(R.id.txt_temp_receive_qty);
            holder.mLlTempReceiveDetail = convertView.findViewById(R.id.ll_temp_receive_detail);
            holder.mLlWaitInspect = convertView.findViewById(R.id.ll_wait_inspect);
            holder.mWaitInspectQty = convertView.findViewById(R.id.txt_wait_inspect_qty);
            holder.mLlWaitInspectDetail = convertView.findViewById(R.id.ll_wait_inspect_detail);
            holder.mLlInspected = convertView.findViewById(R.id.ll_inspected);
            holder.mInspectedQty = convertView.findViewById(R.id.txt_inspected_qty);
            holder.mLlInspectedDetail = convertView.findViewById(R.id.ll_inspected_detail);
            holder.mLlStorage = convertView.findViewById(R.id.ll_storage);
            holder.mStorageQty = convertView.findViewById(R.id.txt_storage_qty);
            holder.mLlStorageDetail = convertView.findViewById(R.id.ll_storage_detail);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mItemNo.setText(mList.get(position).getItemNo());
        holder.mItemControl.setText(mList.get(position).getItemControl());
        holder.mItemDescription.setText(mList.get(position).getItemDescription());
        double notInQty = 0;
        double inQty = 0;
        double tempReceiveQty = 0;
        double waitInspectQty = 0;
        double inspectedQty = 0;
        holder.mLlTempReceiveDetail.removeAllViews();
        holder.mLlWaitInspectDetail.removeAllViews();
        holder.mLlInspectedDetail.removeAllViews();
        holder.mLlStorageDetail.removeAllViews();

        for (ItemOnHandType typeItem : mList.get(position).getTypeList()) {
            if (typeItem.getType().equals("庫存")) {
                inQty = typeItem.getQty().doubleValue();

                if (mList.get(position).getItemControl().equals("DC")) {
                    addStorageDcView(holder.mLlStorageDetail, typeItem);
                } else {
                    addStorageView(holder.mLlStorageDetail, typeItem);
                }
            } else {
                notInQty += typeItem.getQty().doubleValue();
                LinearLayout targetLayout = null;

                if (typeItem.getType().equals("待驗")) {
                    waitInspectQty = typeItem.getQty().doubleValue();
                    targetLayout = holder.mLlWaitInspectDetail;
                } else if (typeItem.getType().equals("已驗")) {
                    inspectedQty = typeItem.getQty().doubleValue();
                    targetLayout = holder.mLlInspectedDetail;
                } else {
                    tempReceiveQty = typeItem.getQty().doubleValue();
                    targetLayout = holder.mLlTempReceiveDetail;
                }

                if (mList.get(position).getItemControl().equals("DC")) {
                    AppController.debug("typeItem DC = " + typeItem.toString());
                    addNotStorageDcView(targetLayout, typeItem);
                } else {
                    AppController.debug("typeItem NoDC = " + typeItem.toString());
                    addNotStorageView(targetLayout, typeItem);
                }
            }
        }

        holder.mNotInStorageQty.setText(Util.fmt(notInQty));
        holder.mTempReceiveQty.setText(Util.fmt(tempReceiveQty));
        holder.mWaitInspectQty.setText(Util.fmt(waitInspectQty));
        holder.mInspectedQty.setText(Util.fmt(inspectedQty));
        holder.mStorageQty.setText(Util.fmt(inQty));

        if (tempReceiveQty > 0) {
            holder.mLlTempReceive.setVisibility(View.VISIBLE);
        } else {
            holder.mLlTempReceive.setVisibility(View.GONE);
        }

        if (waitInspectQty > 0) {
            holder.mLlWaitInspect.setVisibility(View.VISIBLE);
        } else {
            holder.mLlWaitInspect.setVisibility(View.GONE);
        }

        if (inspectedQty > 0) {
            holder.mLlInspected.setVisibility(View.VISIBLE);
        } else {
            holder.mLlInspected.setVisibility(View.GONE);
        }

        if (inQty > 0) {
            holder.mLlStorage.setVisibility(View.VISIBLE);
        } else {
            holder.mLlStorage.setVisibility(View.GONE);
        }
        return convertView;
    }

    private void addNotStorageDcView(LinearLayout v, final ItemOnHandType typeItem) {
        if (typeItem.getLocatorList() != null) {
            for (Locator locator : typeItem.getLocatorList()) {
                addNotStorageDcSubView(v, locator);
            }
        }
    }

    private void addNotStorageDcSubView(LinearLayout v, final Locator locator) {
        LinearLayout locatorContainer = (LinearLayout) mInflater.inflate(R.layout.dc_locator_item, null);
        TextView tvLocator = locatorContainer.findViewById(R.id.txt_locator);
        LinearLayout llDcContainer = locatorContainer.findViewById(R.id.ll_dc_container);
        TextView label_locator = locatorContainer.findViewById(R.id.label_locator);
        label_locator.setVisibility(View.GONE);
        tvLocator.setVisibility(View.GONE);

        Set<String> dcItemContainerName = locator.getLocator_dateCodeList().keySet();
        AppController.debug("dcItemContainerName1 = " + dcItemContainerName);//[Q0108, Q0107]
        AppController.debug("dcItemContainerName1 = " + Arrays.toString(dcItemContainerName.toArray()));//[Q0108, Q0107]

        //把前後的括弧刪掉 Q0108, Q0107
        String dcItemContainerName1 = Arrays.toString(dcItemContainerName.toArray()).substring(1, dcItemContainerName.toString().length() - 1);

        for (String data : dcItemContainerName1.split(",")) {
            data = data.trim();
            AppController.debug("dcItemContainerName data = " + data); //儲位

            if (locator.getLocator() == null) {
                //tvLocator.setText("");
            } else {
                //tvLocator.setText(data);
                int i = 0;

                for (DateCode dc : locator.getDateCodeList()) {
                    //有儲位的UI
                    LinearLayout dcItemContainer2 = (LinearLayout) mInflater.inflate(R.layout.dc_locator_item2, null);
                    TextView tvLocator1 = dcItemContainer2.findViewById(R.id.txt_locator);
                    TextView label_locator1 = dcItemContainer2.findViewById(R.id.label_locator);
                    label_locator1.setVisibility(View.VISIBLE);
                    tvLocator1.setVisibility(View.VISIBLE);

                    //顯示DC，無儲位
                    LinearLayout dcItemContainer = (LinearLayout) mInflater.inflate(R.layout.dc_item_2, null);
                    TextView tvDc = dcItemContainer.findViewById(R.id.txt_datecode);
                    TextView tvQty = dcItemContainer.findViewById(R.id.txt_qty);

                    if (data.equals(dc.getLocator())) {
                        //AppController.debug("dcItemContainerName data1 = " + data); //儲位
                        tvLocator1.setText(data);
                        tvDc.setText(dc.getDateCode());
                        tvQty.setText(Util.fmt(dc.getQty()));

                        if (i > 0) { //i>0代表有兩個以上的儲位
                            label_locator1.setVisibility(View.GONE);
                            tvLocator1.setVisibility(View.GONE);
                        }

                        i++;
                        llDcContainer.addView(dcItemContainer2);
                        dcItemContainer2.addView(dcItemContainer);
                    }
                }
            }
        }

        v.addView(locatorContainer);

        /*if (locator.getLocator() == null) {
            tvLocator.setText("");
        } else {
            tvLocator.setText(locator.getLocator());
        }

        for (DateCode dc : locator.getDateCodeList()) {
            LinearLayout dcItemContainer = (LinearLayout) mInflater.inflate(R.layout.dc_item_2, null);
            TextView tvDc = dcItemContainer.findViewById(R.id.txt_datecode);
            TextView tvQty = dcItemContainer.findViewById(R.id.txt_qty);
            tvDc.setText(dc.getDateCode());
            tvQty.setText(Util.fmt(dc.getQty()));
            llDcContainer.addView(dcItemContainer);
        }

        v.addView(locatorContainer);*/
    }

    private void addNotStorageView(LinearLayout v, final ItemOnHandType typeItem) {
        if (typeItem.getLocatorList() != null) {
            for (Locator locator : typeItem.getLocatorList()) {
                addNotStorageSubView(v, locator);
            }
        }
    }

    private void addNotStorageSubView(LinearLayout v, final Locator locator) {
        LinearLayout locatorContainer = (LinearLayout) mInflater.inflate(R.layout.locator_item, null);
        TextView tvLocator = locatorContainer.findViewById(R.id.txt_locator);
        TextView tvQty = locatorContainer.findViewById(R.id.txt_qty);

        if (locator.getLocator() == null) {
            tvLocator.setText("");
        } else {
            tvLocator.setText(locator.getLocator());
        }

        tvQty.setText(Util.fmt(locator.getQty()));
        v.addView(locatorContainer);
    }

    private void setSubText(TextView tv, String sub) {
        tv.setSelected(true);

        if (TextUtils.isEmpty(sub)) {
            tv.setText("");
        } else {
            switch (sub) {
                case "302":
                    tv.setText("302(林口RMA暫存倉)");
                    break;
                case "303":
                    tv.setText("303(呆滯倉)");
                    break;
                case "316":
                    tv.setText("316(Pchome-成品倉)");
                    break;
                case "317":
                    tv.setText("317(成品倉)");
                    break;
                case "308":
                    tv.setText("308(林口銷退待處理倉)");
                    break;
                case "304":
                    tv.setText("304(報廢倉)");
                    break;
                case "341":
                    tv.setText("341(半成品)");
                    break;
                case "307":
                    tv.setText("307(材料倉)");
                    break;
                case "310":
                    tv.setText("310(費用倉)");
                    break;
                case "311":
                    tv.setText("311(成品倉)");
                    break;
                case "318":
                    tv.setText("318(GIT-318倉)");
                    break;
                case "326":
                    tv.setText("326(HUB-326倉)");
                    break;
                case "320":
                    tv.setText("320(材料待退倉)");
                    break;
                case "327":
                    tv.setText("327(客供費用倉)");
                    break;
                case "Stage":
                    tv.setText("Stage(待出貨倉)");
                    break;
                default:
                    tv.setText(sub);
                    break;
            }
        }
    }

    private void addStorageView(LinearLayout v, final ItemOnHandType typeItem) {
        if (typeItem.getLocatorList() != null) {
            for (Locator locator : typeItem.getLocatorList()) {
                AppController.debug("locator.getSubinventory() = " + locator.getSubinventory());
                AppController.debug("locator.getLocator() = " + locator.getLocator());
                addStorageSubView(v, locator);
            }
        }
    }


    private void addStorageSubView(LinearLayout v, final Locator locator) {
        AppController.debug("addStorageSubView  = " + locator.getSubinventory()); //儲位
        LinearLayout tmpContainer;
        TextView tmpSub;
        LinearLayout layoutContainer = null;
        TextView tvSubinventory;
        TextView tvTotal;
        LinearLayout layoutLocatorContainer;

        if (v.getChildCount() > 0) {
            for (int i = 0; i < v.getChildCount(); i++) {
                tmpContainer = (LinearLayout) v.getChildAt(0);
                tmpSub = tmpContainer.findViewById(R.id.txt_subinventory);
                if (compareSub(tmpSub, locator.getSubinventory())) {
                    //AppController.debug("addStorageSubView  = " + i); //儲位
                    layoutContainer = tmpContainer;
                    break;
                }
            }
        }

        if (layoutContainer == null) {
            layoutContainer = (LinearLayout) mInflater.inflate(R.layout.sub_item, null);
            v.addView(layoutContainer);
        }

        tvSubinventory = layoutContainer.findViewById(R.id.txt_subinventory);
        tvTotal = layoutContainer.findViewById(R.id.txt_total);
        layoutLocatorContainer = layoutContainer.findViewById(R.id.ll_locator_container);
        setSubText(tvSubinventory, locator.getSubinventory());
        double total = 0;
        BigDecimal total01 = new BigDecimal(0);

        /*if (!TextUtils.isEmpty(tvTotal.getText())) {
            total = Double.parseDouble(tvTotal.getText().toString());
        }

        total += locator.getQty().doubleValue();
        tvTotal.setText(Util.fmt(total));
        tvTotal.setSelected(true);*/

        LinearLayout layoutCon = (LinearLayout) mInflater.inflate(R.layout.locator_item_2, null);
        LinearLayout layoutCon01 = layoutCon.findViewById(R.id.ll_container);
        Set<String> dcItemContainerName = locator.getLocator_dateCodeList().keySet();
        //locator_dateCodeList = [key:G0401,value:[DateCode{dateCode='', qty=22, locator='SG01'}, DateCode{dateCode='null', qty=135, locator='G0401'}] ]
        //locator_dateCodeList = [key:SG01,value:[DateCode{dateCode='', qty=22, locator='SG01'}] ]
        AppController.debug("dcItemContainerName = " + dcItemContainerName);//[Q0108, Q0107]
        AppController.debug("dcItemContainerName = " + Arrays.toString(dcItemContainerName.toArray())); //[Q0108, Q0107]

        //把前後的括弧刪掉 Q0108, Q0107
        String dcItemContainerName1 = Arrays.toString(dcItemContainerName.toArray()).substring(1, dcItemContainerName.toString().length() - 1);
        total01= total01.add(locator.getQty());
        tvTotal.setText(Util.fmt(total01));

        for (String data : dcItemContainerName1.split(",")) {
            data = data.trim();
            AppController.debug("dcItemContainerName data = " + data); //儲位

            if (locator.getLocator() == null) {
                //tvLocator.setText("");
            } else {
                //tvLocator.setText(data);
                int i = 0;
                LinearLayout locatorContainer = (LinearLayout) mInflater.inflate(R.layout.locator_item, null);
                TextView tvLocator = locatorContainer.findViewById(R.id.txt_locator);
                TextView tvQty = locatorContainer.findViewById(R.id.txt_qty);
                tvLocator.setText(data);

                for (DateCode dc : locator.getDateCodeList()) {
                    if (data.equals(dc.getLocator())) {
                        tvQty.setText(Util.fmt(dc.getQty()));
                        //total01 = total01.add(dc.getQty());
                        layoutCon01.addView(locatorContainer);
                    }
                }

                //tvTotal.setText(Util.fmt(total01));
            }
        }

        layoutLocatorContainer.addView(layoutCon);

        /*if (locator.getLocator() == null) {
            tvLocator.setText("");
        } else {
            tvLocator.setText(locator.getLocator());
        }

        tvQty.setText(Util.fmt(locator.getQty()));
        layoutLocatorContainer.addView(locatorContainer);
        AppController.debug("addStorageSubView  => end " );*/
    }

    private void addStorageDcView(LinearLayout v, final ItemOnHandType typeItem) {
        if (typeItem.getLocatorList() != null) {
            for (Locator locator : typeItem.getLocatorList()) {
                //AppController.debug("locator.getSubinventory() = "+locator.getSubinventory());
                //AppController.debug("locator.getLocator() = "+locator.getLocator());
                addStorageDcSubView(v, locator);
            }
        }
    }

    private boolean compareSub(TextView tv, String sub) {
        String from = tv.getText().toString() == null ? "" : tv.getText().toString();
        String toOri = sub == null ? "" : sub;
        String to;

        switch (toOri) {
            case "304":
                to = "304(報廢倉)";
                break;
            case "307":
                to = "307(材料倉)";
                break;
            case "310":
                to = "310(費用倉)";
                break;
            case "311":
                to = "311(成品倉)";
                break;
            case "318":
                to = "318(GIT-318倉)";
                break;
            case "326":
                to = "320(HUB-326倉)";
                break;
            case "320":
                to = "320(材料待退倉)";
                break;
            case "Stage":
                to = "Stage(待出貨倉)";
                break;
            default:
                to = toOri;
                break;
        }

        return from.equals(to);
    }

    private void addStorageDcSubView(LinearLayout v, final Locator locator) {
        LinearLayout tmpContainer;
        TextView tmpSub;
        LinearLayout layoutContainer = null;
        TextView tvSubinventory;
        TextView tvTotal;
        LinearLayout layoutLocatorContainer;

        if (v.getChildCount() > 0) {
            for (int i = 0; i < v.getChildCount(); i++) {
                tmpContainer = (LinearLayout) v.getChildAt(0);
                tmpSub = tmpContainer.findViewById(R.id.txt_subinventory);

                if (compareSub(tmpSub, locator.getSubinventory())) {
                    layoutContainer = tmpContainer;
                    break;
                }
            }
        }

        if (layoutContainer == null) {
            layoutContainer = (LinearLayout) mInflater.inflate(R.layout.sub_item, null);
            v.addView(layoutContainer);
        }

        tvSubinventory = layoutContainer.findViewById(R.id.txt_subinventory);
        tvTotal = layoutContainer.findViewById(R.id.txt_total);
        layoutLocatorContainer = layoutContainer.findViewById(R.id.ll_locator_container);
        setSubText(tvSubinventory, locator.getSubinventory());
        double total = 0;

        if (!TextUtils.isEmpty(tvTotal.getText())) {
            total = Double.parseDouble(tvTotal.getText().toString());
        }

        total += locator.getQty().doubleValue();
        tvTotal.setText(Util.fmt(total));
        //tvTotal.setText("54564534229999999");
        tvTotal.setSelected(true);

        LinearLayout locatorContainer = (LinearLayout) mInflater.inflate(R.layout.dc_locator_item, null); //數量資訊
        TextView tvLocator = locatorContainer.findViewById(R.id.txt_locator);
        TextView label_locator = locatorContainer.findViewById(R.id.label_locator);
        label_locator.setVisibility(View.GONE);
        tvLocator.setVisibility(View.GONE);
        LinearLayout llDcContainer = locatorContainer.findViewById(R.id.ll_dc_container);
        Set<String> dcItemContainerName = locator.getLocator_dateCodeList().keySet();
        AppController.debug("dcItemContainerName = " + dcItemContainerName);//[Q0108, Q0107]
        AppController.debug("dcItemContainerName = " + Arrays.toString(dcItemContainerName.toArray()));//[Q0108, Q0107]

        //把前後的括弧刪掉 Q0108, Q0107
        String dcItemContainerName1 = Arrays.toString(dcItemContainerName.toArray()).substring(1, dcItemContainerName.toString().length() - 1);

        for (String data : dcItemContainerName1.split(",")) {
            data = data.trim();
            AppController.debug("dcItemContainerName data = " + data); //儲位

            if (locator.getLocator() == null) {
                //tvLocator.setText("");
            } else {
                //tvLocator.setText(data);
                int i = 0;

                for (DateCode dc : locator.getDateCodeList()) {
                    //有儲位的UI
                    LinearLayout dcItemContainer2 = (LinearLayout) mInflater.inflate(R.layout.dc_locator_item2, null);
                    TextView tvLocator1 = dcItemContainer2.findViewById(R.id.txt_locator);
                    TextView label_locator1 = dcItemContainer2.findViewById(R.id.label_locator);
                    label_locator1.setVisibility(View.VISIBLE);
                    tvLocator1.setVisibility(View.VISIBLE);

                    //顯示DC，無儲位
                    LinearLayout dcItemContainer = (LinearLayout) mInflater.inflate(R.layout.dc_item_2, null);
                    TextView tvDc = dcItemContainer.findViewById(R.id.txt_datecode);
                    TextView tvQty = dcItemContainer.findViewById(R.id.txt_qty);

                    if (data.equals(dc.getLocator())) {
                        //AppController.debug("dcItemContainerName data1 = " + data); //儲位
                        tvLocator1.setText(data);
                        tvDc.setText(dc.getDateCode());
                        tvQty.setText(Util.fmt(dc.getQty()));

                        if (i > 0) { //i>0代表有兩個以上的儲位
                            label_locator1.setVisibility(View.GONE);
                            tvLocator1.setVisibility(View.GONE);
                        }

                        i++;
                        llDcContainer.addView(dcItemContainer2);
                        dcItemContainer2.addView(dcItemContainer);
                    }
                }
            }
        }

        layoutLocatorContainer.addView(locatorContainer);

        /*if (locator.getLocator() == null) {
            tvLocator.setText("");
        } else {
            tvLocator.setText(locator.getLocator());
        }

        for (DateCode dc : locator.getDateCodeList()) {
            LinearLayout dcItemContainer = (LinearLayout) mInflater.inflate(R.layout.dc_item_2, null);
            TextView tvDc = dcItemContainer.findViewById(R.id.txt_datecode);
            TextView tvQty = dcItemContainer.findViewById(R.id.txt_qty);
            tvDc.setText(dc.getDateCode());
            tvQty.setText(Util.fmt(dc.getQty()));
            llDcContainer.addView(dcItemContainer);
        }

        layoutLocatorContainer.addView(locatorContainer);*/
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                constraint = constraint.toString();
                FilterResults result = new FilterResults();

                if (originalitem == null) {
                    synchronized (this) {
                        originalitem = new ArrayList<>(mList);
                        //若originalitem 沒有資料，會複製一份item的過來.
                    }
                }

                if (constraint != null && constraint.toString().trim().length() > 0) {
                    ArrayList<ItemOnHand> filteredItem = new ArrayList<>();

                    for (int i = 0; i < originalitem.size(); i++) {
                        ItemOnHand item = originalitem.get(i);

                        if (item.getItemNo().contains(constraint)) {
                            filteredItem.add(item);
                        }
                    }

                    result.count = filteredItem.size();
                    result.values = filteredItem;
                } else {
                    synchronized (this) {
                        ArrayList<ItemOnHand> list = new ArrayList<>(originalitem);
                        result.values = list;
                        result.count = list.size();
                    }
                }

                return result;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mList = (ArrayList<ItemOnHand>) results.values;

                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    private static class ViewHolder {
        public TextView mItemNo;
        public TextView mItemControl;
        public TextView mItemDescription;
        public TextView mNotInStorageQty;
        public LinearLayout mLlTempReceive;
        public TextView mTempReceiveQty;
        public LinearLayout mLlTempReceiveDetail;
        public LinearLayout mLlWaitInspect;
        public TextView mWaitInspectQty;
        public LinearLayout mLlWaitInspectDetail;
        public LinearLayout mLlInspected;
        public TextView mInspectedQty;
        public LinearLayout mLlInspectedDetail;
        public LinearLayout mLlStorage;
        public TextView mStorageQty;
        public LinearLayout mLlStorageDetail;
    }
}
