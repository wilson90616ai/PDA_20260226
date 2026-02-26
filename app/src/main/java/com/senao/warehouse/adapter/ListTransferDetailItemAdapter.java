package com.senao.warehouse.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.senao.warehouse.R;
import com.senao.warehouse.database.TransferDateCodeInfo;
import com.senao.warehouse.database.TransferItemInfoHelper;
import com.senao.warehouse.database.TransferLocatorInfoHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListTransferDetailItemAdapter extends BaseAdapter {
    private List<TransferItemInfoHelper> mList;
    private LayoutInflater mInflater;
    private Context mContext;
    private AdapterListener mListener;

    public ListTransferDetailItemAdapter(Context context, List<TransferItemInfoHelper> list, AdapterListener listener) {
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
            convertView = mInflater.inflate(R.layout.transfer_detail_item, null);
            holder.mPartNo = convertView.findViewById(R.id.txt_part_no);
            holder.mTransQty = convertView.findViewById(R.id.txt_transfer_qty);
            holder.mPartDesc = convertView.findViewById(R.id.txt_part_desc);
            holder.mDcList = convertView.findViewById(R.id.layout_dc);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String partNo = mList.get(position).getPartNo();
        holder.mPartNo.setText(partNo);
        holder.mTransQty.setText(String.valueOf(Math.round(mList.get(position).getTransQty().doubleValue())));
        holder.mPartDesc.setText(mList.get(position).getItemDescription());
        holder.mDcList.removeAllViews();

        if (mList.get(position).getLocatorInfo() != null) {
            for (TransferLocatorInfoHelper locatorInfoHelper : mList.get(position).getLocatorInfo()) {
                if (locatorInfoHelper.getDateCodeInfo() != null) {
                    for (TransferDateCodeInfo dateCodeInfo : locatorInfoHelper.getDateCodeInfo()) {
                        addView(holder.mDcList, partNo, locatorInfoHelper.getSubinventory(), locatorInfoHelper.getLocator(), dateCodeInfo.getDateCode(), dateCodeInfo.getPass().doubleValue());
                    }
                }
            }
        }

        return convertView;
    }

    private void addView(LinearLayout v, final String partNo, final String subinventory, final String locator, final String dateCode, final double pass) {
        LinearLayout layoutContainer = (LinearLayout) mInflater.inflate(R.layout.transfer_detail_dc_item, null);
        layoutContainer.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.custom_bg));
        layoutContainer.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                //String message = String.format(Locale.TAIWAN, "是否將此筆\n%s %s\nDC %s * %d\n資料刪除?", subinventory, locator, dateCode, Math.round(pass));
                String message = String.format(mContext.getString(R.string.chk_del_dc), subinventory, locator, dateCode, Math.round(pass));
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle( mContext.getString(R.string.btn_ok)).setMessage(message)
                        .setPositiveButton(mContext.getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (mListener != null) {
                                    TransferItemInfoHelper item = new TransferItemInfoHelper();
                                    item.setPartNo(partNo);
                                    List<TransferLocatorInfoHelper> locatorList = new ArrayList<>();
                                    TransferLocatorInfoHelper locatorInfo;
                                    locatorInfo = new TransferLocatorInfoHelper();
                                    locatorInfo.setSubinventory(subinventory);
                                    locatorInfo.setLocator(locator);
                                    List<TransferDateCodeInfo> dcList = new ArrayList<>();
                                    TransferDateCodeInfo dcInfo = new TransferDateCodeInfo();
                                    dcInfo.setDateCode(dateCode);
                                    dcInfo.setPass((BigDecimal.valueOf(pass)));
                                    dcList.add(dcInfo);
                                    locatorInfo.setDateCodeInfo(dcList);
                                    locatorList.add(locatorInfo);
                                    item.setLocatorInfo(locatorList);
                                    mListener.onCallBack(item);
                                }
                            }
                        })
                        .setNegativeButton(mContext.getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).setCancelable(false).show();
                return false;
            }
        });

        TextView tvLocator = layoutContainer.findViewById(R.id.tv_locator);
        tvLocator.setText(mContext.getString(R.string.label_subinventory_locator, subinventory, locator));
        TextView tvTransferQty = layoutContainer.findViewById(R.id.tv_transfer_qty);
        tvTransferQty.setText(mContext.getString(R.string.label_detail_transfer_qty, dateCode, Math.round(pass)));
        v.addView(layoutContainer);
    }

    private class ViewHolder {
        public TextView mPartNo;
        public TextView mTransQty;
        public TextView mPartDesc;
        public LinearLayout mDcList;
    }
}
