package com.moac.android.opensecretsanta.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.moac.android.opensecretsanta.R;

import java.util.Collections;
import java.util.List;

public class RestrictionListAdapter extends BaseAdapter {

    private final static String TAG = RestrictionListAdapter.class.getSimpleName();

    private final Context mContext;
   // private OnClickListener mRestrictClickListener;
    private List<RestrictionRowDetails> mItems = Collections.emptyList();

    public RestrictionListAdapter(Context _context, List<RestrictionRowDetails> _items) {
        mContext = _context;
        mItems = _items;
     //   mRestrictClickListener = _onClickListener;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public RestrictionRowDetails getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position; // Not meaningful.
    }

    @Override
    public View getView(int _position, View _convertView, ViewGroup _parent) {

        View v = _convertView;
        ImageView memberImageView;
        TextView memberNameView;
        CheckBox checkBoxView;

        if(v == null) {
            v = LayoutInflater.from(mContext).inflate(R.layout.restriction_row, _parent, false);

            memberImageView = (ImageView) v.findViewById(R.id.member_imageview);
            memberNameView = (TextView) v.findViewById(R.id.member_name_textview);
            checkBoxView = (CheckBox) v.findViewById(R.id.restrict_checkbox);

            v.setTag(R.id.member_imageview, memberImageView);
            v.setTag(R.id.member_name_textview, memberNameView);
            v.setTag(R.id.restrict_checkbox, checkBoxView);

        } else {
            memberImageView = (ImageView)v.getTag(R.id.member_imageview);
            memberNameView = (TextView)v.getTag(R.id.member_name_textview);
            checkBoxView = (CheckBox)v.getTag(R.id.restrict_checkbox);
        }

        RestrictionRowDetails res = getItem(_position);

        // TODO Load contact avatar into memberViewImage
        memberNameView.setText(res.getToMemberName());
        checkBoxView.setChecked(!res.isRestricted());
      //  checkBoxView.setOnClickListener(mRestrictClickListener);
     //   checkBoxView.setTag(res); // Provide item as tag data for callbacks

        return v;
    }

}