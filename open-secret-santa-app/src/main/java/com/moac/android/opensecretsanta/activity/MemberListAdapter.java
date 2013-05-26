package com.moac.android.opensecretsanta.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.moac.android.opensecretsanta.R;

import java.util.List;

public class MemberListAdapter extends ArrayAdapter<MemberRowDetails> {

    private final String TAG = "MemberListAdapter";

    private int resource;
    private OnClickListener mRestrictClickListener;
    private OnClickListener mDeleteClickListener;

    public MemberListAdapter(Context context, int _resource,
                             List<MemberRowDetails> _draws, View.OnClickListener _restrictListener, View.OnClickListener _deleteListener) {
        super(context, _resource, _draws);
        resource = _resource;
        mRestrictClickListener = _restrictListener;
        mDeleteClickListener = _deleteListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//		Log.v(TAG, "getView() - start");
//		Log.v(TAG, "getView() - position: " + position);

        View v = convertView;
        ViewHolder holder;
        LinearLayout newView;

        if(v == null) {
            newView = new LinearLayout(getContext());
            String inflator = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflator);
            v = vi.inflate(resource, newView, true);

            // the ViewHolder keeps references to avoid making unnecessary calls to findViewById
            holder = new ViewHolder();
            holder.memberName = (TextView) newView.findViewById(R.id.memberName);
            holder.restrictionsCount = (TextView) newView.findViewById(R.id.restrictionCountTextView);

            holder.restrictMembersButton = (ImageView) newView.findViewById(R.id.restrictMembersImage);
            holder.deleteButton = (ImageView) newView.findViewById(R.id.removeMemberButton);
            holder.contactMode = (TextView) newView.findViewById(R.id.contactMode);
            holder.memberEntryLayout = (LinearLayout) newView.findViewById(R.id.memberEntryLayout);

            v.setTag(holder);
        } else {
            // view already exists, get the holder instance from the view
            holder = (ViewHolder) v.getTag();
        }

        // Get the individual parts of the draw_row and populate.
        MemberRowDetails details = getItem(position);

        holder.memberName.setText(details.memberName);
        holder.contactMode.setText(details.contactDetail);

        String count = (details.restrictionCount > 0) ? String.valueOf(details.restrictionCount) : "";
        holder.restrictionsCount.setText(count);
        holder.restrictionsCount.setVisibility((details.restrictionCount == 0) ? View.GONE : View.VISIBLE);

        // the delete button should be clickable to trigger a delete of the member
        holder.deleteButton.setTag(new DeleteViewTag(details.memberId, details.memberName));
        holder.deleteButton.setOnClickListener(mDeleteClickListener);

        // the open restrictions button too...
        holder.restrictMembersButton.setTag(details);
        holder.restrictMembersButton.setOnClickListener(mRestrictClickListener);

        v.setTag(holder);
        return v;
    }

    class ViewHolder {
        ImageView deleteButton;
        TextView memberName;

        TextView restrictionsCount;
        ImageView restrictMembersButton;

        TextView contactMode;
        ImageView contactModeImage;

        LinearLayout memberEntryLayout;
    }

    class DeleteViewTag {
        long memberId;
        String memberName;

        public DeleteViewTag(long _memberId, String _memberName) {
            this.memberId = _memberId;
            this.memberName = _memberName;
        }
    }
}
