package com.moac.android.opensecretsanta.activity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.moac.android.opensecretsanta.R;

import java.util.List;

public class RestrictionListAdapter extends ArrayAdapter<RestrictionRowDetails> {

    private final static String TAG = "RestrictionListAdapter";

    int resource;
    OnClickListener mRestrictClickListener;

    public RestrictionListAdapter(Context context, int _resource,
                                  List<RestrictionRowDetails> items, OnClickListener clickListener) {
        super(context, _resource, items);
        resource = _resource;
        mRestrictClickListener = clickListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LinearLayout newView;

        if(convertView == null) {
            newView = new LinearLayout(getContext());
            String inflator = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflator);
            vi.inflate(resource, newView, true);
        } else {
            newView = (LinearLayout) convertView;
        }

        // Get the individual parts of the draw_row and populate.
        RestrictionRowDetails res = getItem(position);
        TextView tv = (TextView) newView.findViewById(R.id.memberNameCell);
        tv.setText(res.getToMemberName());

        // the ViewHolder keeps references to avoid making unecessary calls to findViewById
        ViewHolder holder = new ViewHolder();
        holder.checkBox = (CheckBox) newView.findViewById(R.id.restrictCheckBox);
        Log.v(TAG, "RestrictionListAdapter.getView() - name: " + res.toMemberName + " isRestricted: " + res.restricted);

        holder.checkBox.setChecked(!res.isRestricted());
        holder.checkBox.setOnClickListener(mRestrictClickListener);

        holder.checkBox.setTag(res);
        newView.setTag(holder);

        return newView;
    }

    class ViewHolder {
        CheckBox checkBox;
    }
}