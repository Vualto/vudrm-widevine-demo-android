package com.vualto.vudrm.widevinedemo.view.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.vualto.vudrm.widevinedemo.view.models.SourceItem;
import com.vualto.vudrm.widevinedemo.view.models.SourceItemHolder;

import java.util.List;

/**
 * Created by Adam Gerber on 29/04/2019.
 * <p>
 * Copyright © 2019 Vualto Ltd. All rights reserved.
 */

public class ContentAdapter extends BaseExpandableListAdapter {

    private final Context _context;
    private final List<SourceItemHolder> _sourceItemHolderList;

    public ContentAdapter(Context context, List<SourceItemHolder> sourceItemHolderList) {
        _context = context;
        _sourceItemHolderList = sourceItemHolderList;
    }

    @Override
    public int getGroupCount() {
        return _sourceItemHolderList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return getGroup(groupPosition).sourceItemList.size();
    }

    @Override
    public SourceItemHolder getGroup(int groupPosition) {
        return _sourceItemHolderList.get(groupPosition);
    }

    @Override
    public SourceItem getChild(int groupPosition, int childPosition) {
        return getGroup(groupPosition).sourceItemList.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(_context).inflate(android.R.layout.simple_expandable_list_item_1,
                    parent, false);
        }
        ((TextView) view).setText(getGroup(groupPosition).title);
        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean b, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(_context).inflate(android.R.layout.simple_list_item_1, parent,
                    false);
        }
        ((TextView) view).setText(getChild(groupPosition, childPosition).name);
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
