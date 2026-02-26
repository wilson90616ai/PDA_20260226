package com.senao.warehouse.adapter;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;

import com.senao.warehouse.R;

public class SimpleExpandableListAdapterWithEmptyGroups extends SimpleExpandableListAdapter {
    private static final String LOG_TAG = SimpleExpandableListAdapterWithEmptyGroups.class.getSimpleName();
    private static final int[] EMPTY_STATE_SET = {};
    private static final int[] GROUP_EXPANDED_STATE_SET = {android.R.attr.state_expanded};
    private static final int[][] GROUP_STATE_SETS = {
            EMPTY_STATE_SET, // 0
            GROUP_EXPANDED_STATE_SET // 1
    };

    public SimpleExpandableListAdapterWithEmptyGroups(Context context,
                                                      List<? extends Map<String, ?>> groupData, int groupLayout,
                                                      String[] groupFrom, int[] groupTo,
                                                      List<? extends List<? extends Map<String, ?>>> childData,
                                                      int childLayout, String[] childFrom, int[] childTo) {
        super(context, groupData, groupLayout, groupFrom, groupTo, childData, childLayout, childFrom, childTo);
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View v = super.getGroupView(groupPosition, isExpanded, convertView, parent);
        View ind = v.findViewById(R.id.explist_indicator);

        if (ind != null) {
            ImageView indicator = (ImageView) ind;

            if (getChildrenCount(groupPosition) == 0) {
                indicator.setVisibility(View.INVISIBLE);
            } else {
                indicator.setVisibility(View.VISIBLE);
                int stateSetIndex = (isExpanded ? 1 : 0);
                Drawable drawable = indicator.getDrawable();
                drawable.setState(GROUP_STATE_SETS[stateSetIndex]);
            }
        }

        return v;
    }
}
