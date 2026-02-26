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
import com.senao.warehouse.database.TransferLocatorFormInfoHelper;
import com.senao.warehouse.database.TransferLocatorFormItemInfoHelper;
import com.senao.warehouse.database.TransferLocatorInfoHelper;

import java.util.List;
import java.util.Locale;

public class ListTransferLocatorFormItemAdapter extends BaseAdapter {
    private List<TransferLocatorFormInfoHelper> mList;
    private LayoutInflater mInflater;
    private Context mContext;
    private AdapterListener mListener;

    public ListTransferLocatorFormItemAdapter(Context context, List<TransferLocatorFormInfoHelper> list, AdapterListener listener) {
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
            convertView = mInflater.inflate(R.layout.transfer_form_item, null);
            holder.mStatus = convertView.findViewById(R.id.txt_status);
            holder.mWorkItem = convertView.findViewById(R.id.txt_work_item);
            holder.mType = convertView.findViewById(R.id.txt_type);
            holder.mTotalNumber = convertView.findViewById(R.id.txt_total_number);
            holder.mNumberOfProcessed = convertView.findViewById(R.id.txt_number_of_processed);
            holder.mNumberOfUnProcessed = convertView.findViewById(R.id.txt_number_of_unprocessed);
            holder.mFormNo = convertView.findViewById(R.id.txt_form_no);
            holder.mDetailList = convertView.findViewById(R.id.layout_detail);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TransferLocatorFormInfoHelper item = mList.get(position);
        holder.mStatus.setText(item.getStatus());
        holder.mWorkItem.setText(item.getWorkItemName());
        holder.mType.setText(item.getType());
        holder.mTotalNumber.setText(String.valueOf(item.getTotalNumber().intValue()));
        holder.mNumberOfProcessed.setText(String.valueOf(item.getTotalIn().intValue()));
        holder.mNumberOfUnProcessed.setText(String.valueOf(item.getTotalNumber().subtract(item.getTotalIn()).intValue()));
        holder.mFormNo.setText(item.getFormSerialNumber());
        holder.mDetailList.removeAllViews();

        if (item.getList() != null) {
            for (TransferLocatorFormItemInfoHelper detailItem : item.getList()) {
                //detailItem.setFormSerialNumber(item.getFormSerialNumber());
                addView(holder.mDetailList, detailItem);
            }
        }

        return convertView;
    }

    private void addView(LinearLayout v, final TransferLocatorFormItemInfoHelper transferLocatorFormItemInfoHelper) {
        LinearLayout layoutContainer = (LinearLayout) mInflater.inflate(R.layout.transfer_form_item_detail, null);

        if (transferLocatorFormItemInfoHelper.getDueNumber().intValue() == transferLocatorFormItemInfoHelper.getInNumber().intValue()) {
            layoutContainer.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.custom_bg_grey));
            //layoutContainer.setClickable(false);
            layoutContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    String message = String.format(Locale.TAIWAN, "是否將此筆%s資料刪除(delete)?", transferLocatorFormItemInfoHelper.getItemNo());
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle( mContext.getString(R.string.btn_ok)).setMessage(message)
                            .setPositiveButton(mContext.getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (mListener != null) {
                                        transferLocatorFormItemInfoHelper.setProcessType("DEL");
                                        mListener.onCallBack(transferLocatorFormItemInfoHelper);
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
        } else {
            layoutContainer.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.custom_bg));
            layoutContainer.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        transferLocatorFormItemInfoHelper.setProcessType("PROCESS");
                        mListener.onCallBack(transferLocatorFormItemInfoHelper);
                    }
                }
            });
        }

        TextView tvOutSubinventory = layoutContainer.findViewById(R.id.txt_out_subinventory);
        TextView tvOutLocator = layoutContainer.findViewById(R.id.txt_out_locator);
        TextView tvDetailStatus = layoutContainer.findViewById(R.id.txt_detail_status);
        TextView tvWarehouseClerk = layoutContainer.findViewById(R.id.txt_warehouse_clerk);
        TextView tvItemNumber = layoutContainer.findViewById(R.id.txt_item_number);
        TextView tvItemDescription = layoutContainer.findViewById(R.id.txt_item_description);
        TextView tvDueNumber = layoutContainer.findViewById(R.id.txt_due_number);
        TextView tvNumberOfCredited = layoutContainer.findViewById(R.id.txt_number_of_credited);
        TextView tvNumberOfUnCredited = layoutContainer.findViewById(R.id.txt_number_of_uncredited);
        TextView tvOnhand = layoutContainer.findViewById(R.id.txt_onhand);
        LinearLayout layoutLocatorDetail = layoutContainer.findViewById(R.id.locator_detail);

        tvOutSubinventory.setText(transferLocatorFormItemInfoHelper.getOutSubinventory());
        tvOutLocator.setText(transferLocatorFormItemInfoHelper.getOutLocator());
        tvDetailStatus.setText(transferLocatorFormItemInfoHelper.getStatus());
        tvWarehouseClerk.setText(transferLocatorFormItemInfoHelper.getWarehouseClerk());
        tvItemNumber.setText(transferLocatorFormItemInfoHelper.getItemNo());
        tvItemDescription.setText(transferLocatorFormItemInfoHelper.getItemDescription());
        tvDueNumber.setText(String.valueOf(transferLocatorFormItemInfoHelper.getDueNumber().intValue()));
        tvNumberOfCredited.setText(String.valueOf(transferLocatorFormItemInfoHelper.getInNumber().intValue()));
        tvNumberOfUnCredited.setText(String.valueOf(transferLocatorFormItemInfoHelper.getDueNumber().subtract(transferLocatorFormItemInfoHelper.getInNumber()).intValue()));
        tvOnhand.setText(String.valueOf(transferLocatorFormItemInfoHelper.getOnHand().intValue()));

        if (transferLocatorFormItemInfoHelper.getLocatorInfo() != null) {
            for (TransferLocatorInfoHelper locatorInfoHelper : transferLocatorFormItemInfoHelper.getLocatorInfo()) {
                addView(layoutLocatorDetail, locatorInfoHelper);
            }
        }

        v.addView(layoutContainer);
    }

    private void addView(LinearLayout v, final TransferLocatorInfoHelper locatorInfoHelper) {
        LinearLayout layoutContainer = (LinearLayout) mInflater.inflate(R.layout.transfer_form_item_locator, null);
        TextView tvSubinventory = layoutContainer.findViewById(R.id.txt_subinventory);
        TextView tvLocator = layoutContainer.findViewById(R.id.txt_locator);
        LinearLayout layoutDcDetail = layoutContainer.findViewById(R.id.dc_detail);
        tvSubinventory.setText(locatorInfoHelper.getSubinventory());
        tvLocator.setText(locatorInfoHelper.getLocator());

        if (locatorInfoHelper.getDateCodeInfo() != null) {
            for (TransferDateCodeInfo dateCodeInfo : locatorInfoHelper.getDateCodeInfo()) {
                addView(layoutDcDetail, dateCodeInfo);
            }
        }

        v.addView(layoutContainer);
    }

    private void addView(LinearLayout v, final TransferDateCodeInfo dateCodeInfo) {
        LinearLayout layoutContainer = (LinearLayout) mInflater.inflate(R.layout.transfer_form_item_dc, null);
        TextView tvDateCode = layoutContainer.findViewById(R.id.txt_datecode);
        TextView tvQty = layoutContainer.findViewById(R.id.txt_qty);
        tvDateCode.setText(dateCodeInfo.getDateCode());
        tvQty.setText(String.valueOf(dateCodeInfo.getQty().intValue()));
        v.addView(layoutContainer);
    }

    private class ViewHolder {
        public TextView mStatus;
        public TextView mWorkItem;
        public TextView mType;
        public TextView mTotalNumber;
        public TextView mNumberOfProcessed;
        public TextView mNumberOfUnProcessed;
        public TextView mFormNo;
        public LinearLayout mDetailList;
    }
}
