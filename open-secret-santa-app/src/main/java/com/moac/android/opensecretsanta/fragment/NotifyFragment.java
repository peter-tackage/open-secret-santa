package com.moac.android.opensecretsanta.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.Group;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.receiver.SmsSendReceiver;
import com.moac.android.opensecretsanta.util.Notifier;
import com.moac.android.opensecretsanta.util.SmsNotifier;

public class NotifyFragment extends DialogFragment {

    // TODO Handle rotation.

    private static final String TAG = NotifyFragment.class.getSimpleName();

    protected EditText mMsgField;
    protected final DatabaseManager mDb;
    protected Group mGroup;
    protected long[] mMemberIds;

    public NotifyFragment(DatabaseManager _db, Group _group, long[] _memberIds) {
        mDb = _db;
        mGroup = _group;
        mMemberIds = _memberIds;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflate layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.notify_fragment_dialog, null);

        // Take the values and populate the dialog
        builder.setTitle("Notify Group");
        builder.setIcon(R.drawable.ic_menu_notify);

        mMsgField = (EditText) view.findViewById(R.id.messageTxtEditText);
        mMsgField.setText(mGroup.getMessage());

        final TextView charCountView = (TextView) view.findViewById(R.id.msg_char_count);
        charCountView.setText(String.valueOf(mMsgField.length()));

        // Add the callback to the field
        mMsgField.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Update the reported character length
                charCountView.setText(String.valueOf(s.length()));
            }
        });

        builder.setCancelable(true);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                executeNotify();
            }
        });

        builder.setView(view);
        return builder.create();
    }

    private void executeNotify() {
        // Get the custom message.
        mGroup.setMessage(mMsgField.getText().toString().trim());
        mDb.update(mGroup);

        // Iterate through the provided members - get their Assignment.
        for(long memberId : mMemberIds) {
            Member member = mDb.queryById(memberId, Member.class);
            Assignment assignment = mDb.queryAssignmentForMember(memberId);
            if(assignment == null) {
                Log.e(TAG, "executeNotify() - No Assignment for Member: " + member.getName());
                continue;
            }
            Member giftReceiver = mDb.queryById(assignment.getReceiverMemberId(), Member.class);

            switch(member.getContactMode()) {
                case SMS:
                    Log.i(TAG, "executeNotify() - Building SMS Notifier for: " + member.getName());
                    Notifier notifier = new SmsNotifier(getActivity(), new SmsSendReceiver(mDb), true);
                    notifier.notify(member, giftReceiver.getName(), mGroup.getMessage());
                    break;
                case EMAIL:
                    Log.i(TAG, "executeNotify() - Building Email Notifier for: " + member.getName());
                    break;
                case REVEAL_ONLY:
                    break;
                default:
                    Log.e(TAG, "executeNotify() - Unknown contact mode: " + member.getContactMode());
            }
        }
    }
}
