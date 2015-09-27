package com.moac.android.opensecretsanta.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.moac.android.opensecretsanta.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GroupDetailsRow implements DrawerListAdapter.Item {

    private static final SimpleDateFormat format = new SimpleDateFormat("d MMM yyyy");

    protected long mGroupId;
    protected String mName;
    protected long mCreationDate;

    public GroupDetailsRow(long _groupId, String _groupName, long _groupCreationDate) {
        mGroupId = _groupId;
        mName = _groupName;
        mCreationDate = _groupCreationDate;
    }

    @Override
    public int getItemType() {
        return DrawerListAdapter.ItemType.GROUP.ordinal();
    }

    @Override
    public long getItemId() {
        return mGroupId;
    }

    @Override
    public View getView(Context _context, View _convertView, ViewGroup _parent) {
        View view = _convertView;
        TextView groupNameView;
        TextView groupDateView;

        if(view == null) {
            view = LayoutInflater.from(_context).inflate(R.layout.list_item_group, _parent, false);
            groupNameView = (TextView) view.findViewById(R.id.textView_groupName);
            groupDateView = (TextView) view.findViewById(R.id.textView_groupDate);
            view.setTag(R.id.textView_groupName, groupNameView);
            view.setTag(R.id.textView_groupDate, groupDateView);
        } else {
            groupNameView = (TextView) view.getTag(R.id.textView_groupName);
            groupDateView = (TextView) view.getTag(R.id.textView_groupDate);
        }

        groupNameView.setText(mName);
        groupDateView.setText(getCreationDate());

        return view;
    }

    public long getId() { return mGroupId; }

    public String getName() { return mName; }

    public String getCreationDate() {
        Date date = new Date(mCreationDate);
        return format.format(date);
    }
}
