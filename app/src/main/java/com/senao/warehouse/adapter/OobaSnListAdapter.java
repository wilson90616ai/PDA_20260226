package com.senao.warehouse.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.senao.warehouse.R;
import com.senao.warehouse.handler.OobaSnMsHelper;

import java.util.List;

public class OobaSnListAdapter  extends BaseAdapter {
    private List<OobaSnMsHelper> mList;
    private LayoutInflater mInflater;
    private Context mContext;
    private AdapterListener mListener;

    public OobaSnListAdapter(Context context, List<OobaSnMsHelper> list, AdapterListener listener) {
        mContext = context;
        mList = list;
        mListener = listener;
        mInflater = LayoutInflater.from(context);
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
            convertView = mInflater.inflate(R.layout.ooba_sn_list, null);
            holder.sfis_Status = convertView.findViewById(R.id.sfis_status); //SFIS 狀態
            holder.ora_Status = convertView.findViewById(R.id.ora_status); //Oracle 狀態
            holder.plan_ship = convertView.findViewById(R.id.plan_ship); //預計出貨週別
            holder.item_num = convertView.findViewById(R.id.item_num); //料號
            holder.item_desc = convertView.findViewById(R.id.item_desc); //品名
            holder.sn_num = convertView.findViewById(R.id.sn_num); //序號
            holder.box_num = convertView.findViewById(R.id.box_num); //箱號
            holder.box_num_1 = convertView.findViewById(R.id.box_num_1); //箱號數量
            holder.ban_num = convertView.findViewById(R.id.ban_num); //板號
            holder.ban_num_1 = convertView.findViewById(R.id.ban_num_1); //板號數量
            holder.wo_num = convertView.findViewById(R.id.wo_num); //製令
            holder.wo_num_1 = convertView.findViewById(R.id.wo_num_1); //製令數量
            holder.q_no = convertView.findViewById(R.id.q_no); //檢驗單號
            holder.sc001 = convertView.findViewById(R.id.sc001); //入庫單號
            holder.mfg_date = convertView.findViewById(R.id.mfg_date); //生產日
            holder.q_date = convertView.findViewById(R.id.q_date); //檢驗日
            holder.stock_date = convertView.findViewById(R.id.stock_date); //入庫日
            holder.salesDate = convertView.findViewById(R.id.salesDate); //銷貨日
            holder.shippingDN = convertView.findViewById(R.id.shippingDN); //出貨DN
            holder.shippingCustom = convertView.findViewById(R.id.shippingCustom); //出貨客戶
            holder.shippingBoxNum = convertView.findViewById(R.id.shippingBoxNum); //出貨箱號
            holder.shippingVerNum = convertView.findViewById(R.id.shippingVerNum); //出貨板號
            holder.shippingOE = convertView.findViewById(R.id.shippingOE); //出貨OE
            holder.shippingPo = convertView.findViewById(R.id.shippingPo); //出貨PO
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        OobaSnMsHelper item = mList.get(position);
        holder.sfis_Status.setText(item.getMO012C()); //SFIS 狀態 getMO012C getState
        holder.ora_Status.setText(item.getOraHelper().getP_status()); //Oracle 狀態
        holder.plan_ship.setText(item.getPlan_date()); //預計出貨週別
        holder.item_num.setText(item.getItemNo()); //料號
        holder.item_desc.setText(item.getItem_desc()); //品名
        holder.sn_num.setText(item.getItemSn()); //序號
        holder.box_num.setText(item.getMO012()); //箱號
        //holder.box_num_1.setText("item.getState()"); //箱號數量
        holder.ban_num.setText(item.getMo017()); //板號
        //holder.ban_num_1.setText("item.getMo017()"); //板號數量
        holder.wo_num.setText(item.getMo()); //製令
        holder.wo_num_1.setText(item.getQty()+""); //製令數量
        holder.q_no.setText(item.getQ_no()); //檢驗單號
        holder.sc001.setText(item.getSc001()); //入庫單號
        holder.mfg_date.setText(item.getMfg_date()); //生產日
        holder.q_date.setText(item.getQ_date()); //檢驗日
        holder.stock_date.setText(item.getStock_date()); //入庫日
        holder.salesDate.setText(item.getOraHelper().getSalesDate()); //銷貨日
        holder.shippingDN.setText(item.getOraHelper().getShippingDN()+""); //出貨DN
        holder.shippingCustom.setText(item.getOraHelper().getShippingCustom()); //出貨客戶
        holder.shippingBoxNum.setText(item.getOraHelper().getShippingBoxNum()); //出貨箱號
        holder.shippingVerNum.setText(item.getOraHelper().getShippingVerNum()); //出貨板號
        holder.shippingOE.setText(item.getOraHelper().getShippingOE()+""); //出貨OE
        holder.shippingPo.setText(item.getOraHelper().getShippingPo()); //出貨PO

        /*holder.mStatus.setText(item.getStatus());
        holder.mWorkItem.setText(item.getWorkItemName());
        holder.mType.setText(item.getType());
        holder.mTotalNumber.setText(String.valueOf(item.getTotalNumber().intValue()));
        holder.mNumberOfProcessed.setText(String.valueOf(item.getTotalIn().intValue()));
        holder.mNumberOfUnProcessed.setText(String.valueOf(item.getTotalNumber().subtract(item.getTotalIn()).intValue()));
        holder.mFormNo.setText(item.getFormSerialNumber());
        holder.mDetailList.removeAllViews();

        if (item.getList() != null) {
            for (OobaSnHelper detailItem : item.getList()) {
                //detailItem.setFormSerialNumber(item.getFormSerialNumber());
                //addView(holder.mDetailList, detailItem);
            }
        }*/

        return convertView;
    }

    private class ViewHolder {
        public TextView sfis_Status; //SFIS 狀態
        public TextView ora_Status; //Oracle 狀態
        public TextView plan_ship; //預計出貨週別
        public TextView item_num; //料號
        public TextView item_desc; //品名
        public TextView sn_num; //序號
        public TextView box_num; //箱號
        public TextView box_num_1; //箱號數量
        public TextView ban_num; //板號
        public TextView ban_num_1; //板號數量
        public TextView wo_num; //製令
        public TextView wo_num_1; //製令數量
        public TextView q_no; //檢驗單號
        public TextView sc001; //入庫單號
        public TextView mfg_date; //生產日
        public TextView q_date; //檢驗日
        public TextView stock_date; //入庫日
        public TextView salesDate; //銷貨日
        public TextView shippingDN; //出貨DN
        public TextView shippingCustom; //出貨客戶
        public TextView shippingBoxNum; //出貨箱號
        public TextView shippingVerNum; //出貨板號
        public TextView shippingOE; //出貨OE
        public TextView shippingPo; //出貨PO
        public LinearLayout mDetailList;
    }
}
