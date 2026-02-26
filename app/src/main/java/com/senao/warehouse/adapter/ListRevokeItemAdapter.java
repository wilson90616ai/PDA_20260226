package com.senao.warehouse.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.senao.warehouse.R;
import com.senao.warehouse.database.DocumentInfoHelper;

import java.util.List;

public class ListRevokeItemAdapter extends BaseAdapter {
    private List<DocumentInfoHelper> mList;
    private LayoutInflater mInflater;

    public ListRevokeItemAdapter(Context context, List<DocumentInfoHelper> list) {
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
            convertView = mInflater.inflate(R.layout.revoke_item, null);
            holder.mEzflowNo = convertView.findViewById(R.id.txt_ezflow_no);
            holder.mFlowSeq = convertView.findViewById(R.id.txt_flow_seq);
            holder.mApplyDepartment = convertView.findViewById(R.id.txt_apply_department);
            holder.mApplicant = convertView.findViewById(R.id.txt_applicant);
            holder.mNoOfApllication = convertView.findViewById(R.id.txt_number_of_application);
            holder.mWorkItemName = convertView.findViewById(R.id.txt_work_item_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mEzflowNo.setText(mList.get(position).getDocumentNo());
        holder.mFlowSeq.setText(mList.get(position).getProcessNo());
        holder.mApplyDepartment.setText(mList.get(position).getApplicationDepartment());
        holder.mApplicant.setText(mList.get(position).getApplicant());
        holder.mNoOfApllication.setText(String.valueOf(mList.get(position).getList().length));
        holder.mWorkItemName.setText(mList.get(position).getWorkItemName());

        return convertView;
    }

    private static class ViewHolder {
        public TextView mEzflowNo;
        public TextView mFlowSeq;
        public TextView mApplyDepartment;
        public TextView mApplicant;
        public TextView mNoOfApllication;
        public TextView mWorkItemName;
    }
}
