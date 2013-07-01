package com.moac.android.opensecretsanta.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.moac.android.opensecretsanta.R;
import com.squareup.picasso.Picasso;

public class AssignmentFragment extends DialogFragment {

    // TODO Handle rotation.

    private static final String TAG = AssignmentFragment.class.getSimpleName();

    private final String mGiverName;
    private final String mReceiverName;
    private final Uri mAvatarUri;

    public AssignmentFragment(String _giverName, String _receiverName, Uri _avatarUri) {
        mGiverName = _giverName;
        mReceiverName = _receiverName;
        mAvatarUri = _avatarUri;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get a layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.assignment_fragment_dialog, null);

        // Set the values
        TextView giver = (TextView) view.findViewById(R.id.giver_name_textview);
        giver.setText(mGiverName + " was assigned");
        TextView receiver = (TextView) view.findViewById(R.id.receiver_name_textview);
        receiver.setText(mReceiverName);

        ImageView avatarView = (ImageView) view.findViewById(R.id.receiver_avatar_imageview);
        if(mAvatarUri != null) {
            Picasso.with(getActivity()).load(mAvatarUri).error(R.drawable.ic_contact_picture).into(avatarView);
        } else {
            Picasso.with(getActivity()).load(R.drawable.ic_contact_picture).into(avatarView);
        }

        builder.setView(view);
        return builder.create();
    }
}
