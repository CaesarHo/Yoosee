package com.home.yoosee.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.home.yoosee.R;
import com.home.yoosee.activitys.SearchListActivity;
import com.home.yoosee.global.Constants;
import com.home.yoosee.utils.LanguageComparator_CN;
import com.home.yoosee.utils.PinYinSort;

import java.util.Collections;

/**
 * Created by wade on 2017/10/15.
 */

public class SearchListAdapter extends BaseExpandableListAdapter {

    // 字符串
    private String[] data;

    private PinYinSort assort = new PinYinSort();

    private Context context;

    private LayoutInflater inflater;
    // 中文排序
    private LanguageComparator_CN cnSort = new LanguageComparator_CN();

    public SearchListAdapter(Context context, String[] data) {
        super();
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.data = data;
        // 排序
        sort();
    }

    private void sort() {
        // 分类
        for (String str : data) {
            assort.getHashList().add(str);
        }
        assort.getHashList().sortKeyComparator(cnSort);
        for (int i = 0, length = assort.getHashList().size(); i < length; i++) {
            Collections.sort((assort.getHashList().getValueListIndex(i)), cnSort);
        }
    }

    public Object getChild(int group, int child) {
        return assort.getHashList().getValueIndex(group, child);
    }

    public long getChildId(int group, int child) {
        return child;
    }

    public View getChildView(int group, int child, boolean arg2, View contentView, ViewGroup arg4) {
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.list_searchlist_item, null);
        }
        TextView name = (TextView) contentView.findViewById(R.id.name);
        TextView count = (TextView) contentView.findViewById(R.id.county_count);
        final String[] info = assort.getHashList().getValueIndex(group, child).split(":");
        name.setText(info[0]);
        count.setText(info[1]);
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Constants.Action.ACTION_COUNTRY_CHOOSE);
                i.putExtra("info", info);
                context.sendBroadcast(i);
                ((SearchListActivity) context).finish();
            }
        });
        return contentView;
    }

    public int getChildrenCount(int group) {
        return assort.getHashList().getValueListIndex(group).size();
    }

    public Object getGroup(int group) {
        return assort.getHashList().getValueListIndex(group);
    }

    public int getGroupCount() {
        return assort.getHashList().size();
    }

    public long getGroupId(int group) {
        return group;
    }

    public View getGroupView(int group, boolean arg1, View contentView, ViewGroup arg3) {
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.title_search_list, null);
            contentView.setClickable(true);
        }
        TextView textView = (TextView) contentView.findViewById(R.id.name);
        textView.setText(assort.getFirstChar(assort.getHashList()
                .getValueIndex(group, 0)));
        // 禁止伸展

        return contentView;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable(int arg0, int arg1) {
        return true;
    }

    public PinYinSort getAssort() {
        return assort;
    }

}

