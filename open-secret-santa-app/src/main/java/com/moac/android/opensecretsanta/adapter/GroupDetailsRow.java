package com.moac.android.opensecretsanta.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.model.Member;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GroupDetailsRow implements DrawerListAdapter.Item {

    private static final SimpleDateFormat format = new SimpleDateFormat("d MMM yyyy");

    protected long mId;
    protected String mName;
    protected long mCreationDate;
    protected List<Member> mMembers;

    public GroupDetailsRow(long _groupId, String _groupName, long _groupCreationDate, List<Member> _groupMembers) {
        mId = _groupId;
        mName = _groupName;
        mCreationDate = _groupCreationDate;
        mMembers = _groupMembers;
    }

    @Override
    public int getItemType() {
        return DrawerListAdapter.ItemType.GROUP.ordinal();
    }

    @Override
    public long getItemId() {
        return mId;
    }

    @Override
    public View getView(Context _context, View _convertView, ViewGroup _parent) {
        View view = _convertView;
        TextView groupNameView;
        TextView groupDateView;

        if(view == null) {
            view = LayoutInflater.from(_context).inflate(R.layout.group_row_view, _parent, false);
            groupNameView = (TextView) view.findViewById(R.id.group_name_textView);
            groupDateView = (TextView) view.findViewById(R.id.group_date_textView);
            view.setTag(R.id.group_name_textView, groupNameView);
            view.setTag(R.id.group_date_textView, groupDateView);
        } else {
            groupNameView = (TextView) view.getTag(R.id.group_name_textView);
            groupDateView = (TextView) view.getTag(R.id.group_date_textView);
        }

        groupNameView.setText(mName);
        groupDateView.setText(getCreationDate());

        return view;
    }

    public long getId() { return mId; }

    public String getName() { return mName; }

    public String getCreationDate() {
        Date date = new Date(mCreationDate);
        return format.format(date);
    }

    public List<Member> getMembers() { return mMembers; }
}
