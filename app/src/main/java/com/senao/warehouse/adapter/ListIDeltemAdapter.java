package com.senao.warehouse.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.senao.warehouse.R;
import com.senao.warehouse.database.RECEIVING_TYPE;
import com.senao.warehouse.database.ReceivingInfoHelper;
import com.senao.warehouse.util.Util;

import java.util.List;

public class ListIDeltemAdapter extends BaseAdapter {
    private List<ReceivingInfoHelper> mList;
    private LayoutInflater mInflater;
    private RECEIVING_TYPE mReceivingType;

    public ListIDeltemAdapter(Context context, RECEIVING_TYPE receivingType,List<ReceivingInfoHelper> list) {
        mList = list;
        mReceivingType=receivingType;
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
            convertView = mInflater.inflate(R.layout.receiving_del_item, null);
            holder.txtVendorInfo =  convertView.findViewById(R.id.txt_vendor_info);
            holder.lblInvoiceNo =  convertView.findViewById(R.id.label_invoice_no);
            holder.txtInvoiceNo =  convertView.findViewById(R.id.txt_invoice_no);
            holder.lblPoNo =  convertView.findViewById(R.id.label_po);
            holder.txtPoNo =  convertView.findViewById(R.id.txt_po);
            holder.txtControl =  convertView.findViewById(R.id.txt_control);
            holder.txtPartNo =  convertView.findViewById(R.id.txt_part_number);
            holder.txtPoQty =  convertView.findViewById(R.id.txt_po_qty);
            holder.txtDeliverableQty =  convertView.findViewById(R.id.txt_deliverable_qty);
            holder.txtPredeliverQty =  convertView.findViewById(R.id.txt_predeliver_qty);
            holder.txtReceivedQty =  convertView.findViewById(R.id.txt_received_qty);
            holder.txtUnreceivedQty =  convertView.findViewById(R.id.txt_unreceived_qty);
            holder.lblReason =  convertView.findViewById(R.id.label_reason);
            holder.txtReason =  convertView.findViewById(R.id.txt_reason);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mReceivingType == RECEIVING_TYPE.DEL_ERROR) {
            holder.lblReason.setVisibility(View.VISIBLE);
            holder.txtReason.setVisibility(View.VISIBLE);
        } else {
            holder.lblReason.setVisibility(View.GONE);
            holder.txtReason.setVisibility(View.GONE);
        }

        holder.txtVendorInfo.setText(mList.get(position).getVendorName());
        holder.txtInvoiceNo.setText(mList.get(position).getInvoiceNo());
        holder.txtPoNo.setText(mList.get(position).getPo());
        holder.txtControl.setText(mList.get(position).getControl());
        holder.txtPartNo.setText(mList.get(position).getPartNo());
        holder.txtPoQty.setText(Util.fmt( mList.get(position).getPoQty().doubleValue()));
        holder.txtDeliverableQty.setText(Util.fmt(mList.get(position).getDeliverableQty().doubleValue()));
        holder.txtPredeliverQty.setText(Util.fmt(mList.get(position).getPredeliverQty().doubleValue()));
        holder.txtReceivedQty.setText(Util.fmt(mList.get(position).getReceivedQty().doubleValue()));
        holder.txtUnreceivedQty.setText(Util.fmt(mList.get(position).getUnreceivedQty().doubleValue()));
        holder.txtReason.setText(mList.get(position).getReason());

        return convertView;
    }

    private class ViewHolder {
        public TextView txtVendorInfo;
        public TextView lblInvoiceNo;
        public TextView txtInvoiceNo;
        public TextView lblPoNo;
        public TextView txtPoNo;
        public TextView txtControl;
        public TextView txtPartNo;
        public TextView txtPartName;
        public TextView txtPoQty;
        public TextView txtDeliverableQty;
        public TextView txtPredeliverQty;
        public TextView txtReceivedQty;
        public TextView txtUnreceivedQty;
        public TextView lblReason;
        public TextView txtReason;
    }
}
