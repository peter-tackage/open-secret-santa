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
import com.moac.android.opensecretsanta.types.DrawResultEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AssignmentsListAdapter extends ArrayAdapter<DrawResultEntry> {
//    final static String TAG = "AssignmentsListAdapter";
//
    int resource;
    View.OnClickListener mViewListener;
//
    public AssignmentsListAdapter(Context context, int _resource,
                                  List<DrawResultEntry> entries, View.OnClickListener _viewListener) {
        super(context, _resource, entries);
        resource = _resource;
        mViewListener = _viewListener;
    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//
//        LinearLayout newView;
//
//        if(convertView == null) {
//            newView = new LinearLayout(getContext());
//            String inflator = Context.LAYOUT_INFLATER_SERVICE;
//            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflator);
//            vi.inflate(resource, newView, true);
//        } else {
//            newView = (LinearLayout) convertView;
//        }
//
//        // Get the individual parts of the draw_row and populate.
//        DrawResultEntry dre = getItem(position);
//
//        // Name
//        TextView tv = (TextView) newView.findViewById(R.id.memberTextView);
//        tv.setText(dre.getGiverName());
//
//        ImageView viewButton = (ImageView) newView.findViewById(R.id.assigneeImageView);
//        viewButton.setImageResource(R.drawable.ic_menu_view);
//        viewButton.setFocusable(true);
//        viewButton.setClickable(true);
//        viewButton.setTag(dre);
//        viewButton.setOnClickListener(mViewListener);
//
//        // Don't allow them to click on the row itself.
//        LinearLayout assigneeEntryLayout = (LinearLayout) newView.findViewById(R.id.assigneeLayout);
//        assigneeEntryLayout.setFocusable(false);
//        assigneeEntryLayout.setClickable(false);
//        assigneeEntryLayout.setTag(dre.getId());
//
//        // Display viewed or sent... or neither.
//        TextView tv2 = (TextView) newView.findViewById(R.id.dateViewTextView);
//
//        if(dre.getViewedDate() == DrawResultEntry.UNVIEWED_DATE && dre.getSentDate() == DrawResultEntry.UNSET_DATE) {
//            Log.v(TAG, "Setting Unviewed: " + dre.getGiverName() + " v: " + dre.getViewedDate() + ", s: " + dre.getSentDate());
//            tv2.setText("");
//        } else {
//            final SimpleDateFormat sdf = new SimpleDateFormat("h:mm a EEE, d MMM yyyy");
//
//            Log.v(TAG, "Setting v|s: " + dre.getGiverName() + " v: " + dre.getViewedDate() + ", s: " + dre.getSentDate());
//
//            // TODO Set some other marker? might be useful if both view & sent.
//            if(dre.getViewedDate() > dre.getSentDate()) {
//                Date date = new Date(dre.getViewedDate());
//                String dateString = sdf.format(date);
//                tv2.setText("Viewed: " + dateString);
//            } else {
//                Date date = new Date(dre.getSentDate());
//                String dateString = sdf.format(date);
//                tv2.setText("Sent: " + dateString);
//            }
//        }
//
//        return newView;
//    }
}