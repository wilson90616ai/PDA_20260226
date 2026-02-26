package com.senao.warehouse.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.senao.warehouse.R;
import com.senao.warehouse.database.ReceivingInfoHelper;
import com.senao.warehouse.util.Util;

import java.util.List;

public class ListTempItemAdapter extends BaseAdapter {
    private List<ReceivingInfoHelper> mList;
    private LayoutInflater mInflater;

    public ListTempItemAdapter(Context context, List<ReceivingInfoHelper> list) {
        mList = list;
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
            convertView = mInflater.inflate(R.layout.receiving_temp_item, null);
            holder.mVendorInfo =  convertView.findViewById(R.id.txt_vendor_info);
            holder.mPartNo =  convertView.findViewById(R.id.txt_part_number);
            holder.mPartDesc =  convertView.findViewById(R.id.txt_part_desc);
            holder.mTempRecQty =  convertView.findViewById(R.id.txt_receive_temp_qty);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mVendorInfo.setText(mList.get(position).getVendorName());
        holder.mPartNo.setText(mList.get(position).getPartNo());
        holder.mPartDesc.setText(mList.get(position).getPartDesc());
        holder.mTempRecQty.setText(Util.fmt(mList.get(position).getTempQty().doubleValue()));

        return convertView;
    }

    private class ViewHolder {
        public TextView mVendorInfo;
        public TextView mPartNo;
        public TextView mPartDesc;
        public TextView mTempRecQty;
    }
}
