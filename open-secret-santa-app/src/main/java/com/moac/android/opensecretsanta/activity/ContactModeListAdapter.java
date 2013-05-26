package com.moac.android.opensecretsanta.activity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.moac.android.opensecretsanta.R;

import java.util.List;

public class ContactModeListAdapter extends ArrayAdapter<ContactModeRowDetails> {

    private final static String TAG = "ContactModeListAdapter";

    int resource;

    public ContactModeListAdapter(Context context, int _resource,
                                  List<ContactModeRowDetails> items) {
        super(context, _resource, items);
        resource = _resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LinearLayout newView;

        if(convertView == null) {
            newView = new LinearLayout(getContext());
            String inflator = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
              inflator);
            vi.inflate(resource, newView, true);
        } else {
            newView = (LinearLayout) convertView;
        }

        // Get the individual parts of the draw_row and populate.
        ContactModeRowDetails modeDetails = getItem(position);
        TextView tv = (TextView) newView.findViewById(R.id.contactModeTextView);
        tv.setText(modeDetails.toString());

        ImageView iv = (ImageView) newView.findViewById(R.id.contactModeImage);
        if(modeDetails.contactMode == Constants.SMS_CONTACT_MODE) {
            iv.setImageResource(R.drawable.ic_phone);
        } else if(modeDetails.contactMode == Constants.NAME_ONLY_CONTACT_MODE) {
            iv.setImageResource(R.drawable.ic_menu_view);
        } else if(modeDetails.contactMode == Constants.EMAIL_CONTACT_MODE) {
            iv.setImageResource(R.drawable.ic_email);
        }

        Log.v(TAG, "getView() - end");

        return newView;
    }
}