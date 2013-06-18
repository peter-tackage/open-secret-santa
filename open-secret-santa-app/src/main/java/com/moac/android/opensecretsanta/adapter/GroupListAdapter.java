package com.moac.android.opensecretsanta.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.model.Group;

import java.util.List;

public class GroupListAdapter extends BaseAdapter {

    private final Context mContext;
    private List<Group> mGroups;

    public GroupListAdapter(Context _context, List<Group> _groups){
        mContext = _context;
        mGroups = _groups;
    }

    @Override
    public int getCount() {
        return mGroups.size();
    }

    @Override
    public Group getItem(int _position) {
        return mGroups.get(_position);
    }

    @Override
    public long getItemId(int _position) {
        return mGroups.get(_position).getId();
    }

    @Override
    public View getView(int _position, View _convertView, ViewGroup _parent) {
        View v = _convertView;
        TextView groupNameView;

        if(v == null) {
            v = LayoutInflater.from(mContext).inflate(R.layout.group_row_view, _parent, false);
            groupNameView = (TextView) v.findViewById(R.id.group_name_textView);
            v.setTag(R.id.group_name_textView, groupNameView);
        } else {
            // Recycled View is available, retrieve the holder instance from the View
            groupNameView = (TextView)v.getTag(R.id.group_name_textView);
        }

        Group group = getItem(_position);
        groupNameView.setText(group.getName());

        return v;
    }
}
