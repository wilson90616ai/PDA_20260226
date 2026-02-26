package com.senao.warehouse.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.senao.warehouse.R;
import com.senao.warehouse.database.TransferItemInfoHelper;

import java.util.List;

public class ListTransferItemAdapter extends BaseAdapter {
    private List<TransferItemInfoHelper> mList;
    private LayoutInflater mInflater;

    public ListTransferItemAdapter(Context context, List<TransferItemInfoHelper> list) {
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
            convertView = mInflater.inflate(R.layout.transfer_item, null);
            holder.mPartNo = convertView.findViewById(R.id.txt_part_no);
            holder.mTransQty = convertView.findViewById(R.id.txt_transfer_qty);
            holder.mPartDesc = convertView.findViewById(R.id.txt_part_desc);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mPartNo.setText(mList.get(position).getPartNo());
        holder.mTransQty.setText(String.valueOf(Math.round(mList.get(position).getTransQty().doubleValue())));
        holder.mPartDesc.setText(mList.get(position).getItemDescription());

        return convertView;
    }

    private class ViewHolder {
        public TextView mPartNo;
        public TextView mTransQty;
        public TextView mPartDesc;
    }
}
