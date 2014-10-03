package com.moac.android.opensecretsanta.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.moac.android.inject.dagger.InjectingFragment;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.activity.Intents;
import com.moac.android.opensecretsanta.adapter.ContactMethodAdapter;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.Assignment;
import com.moac.android.opensecretsanta.model.ContactMethod;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.PersistableObject;
import com.moac.android.opensecretsanta.util.validator.ContactDetailsValidator;
import com.moac.android.opensecretsanta.util.validator.MemberNameValidator;
import com.moac.android.opensecretsanta.util.validator.Validator;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

public class MemberEditFragment extends InjectingFragment implements Saveable {

    private static final String TAG = MemberEditFragment.class.toString();

    @Inject
    DatabaseManager mDb;

    private Member mMember;
    private EditText mMemberNameEditView;
    private View mContactDetailsLineSeparator;
    private EditText mContactDetailsEditText;
    private Spinner mContactMethodSpinner;

    public static MemberEditFragment create(long _memberId) {
        MemberEditFragment fragment = new MemberEditFragment();
        Bundle args = new Bundle();
        args.putLong(Intents.MEMBER_ID_INTENT_EXTRA, _memberId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        long memberId = getArguments().getLong(Intents.MEMBER_ID_INTENT_EXTRA);
        // The database is injected in the parent class onActivityCreated
        mMember = mDb.queryById(memberId, Member.class);

        // Populate the views with content
        TextView titleTextView = (TextView) getView().findViewById(R.id.content_title_textview);
        titleTextView.setText(String.format(getString(R.string.edit_member_title),mMember.getName()));

        ImageView avatarImageView = (ImageView) getView().findViewById(R.id.imageView_avatar);
        if (mMember.getContactId() == PersistableObject.UNSET_ID || mMember.getLookupKey() == null) {
            Picasso.with(getActivity()).load(R.drawable.ic_contact_picture).into(avatarImageView);
        } else {
            Uri lookupUri = ContactsContract.Contacts.getLookupUri(mMember.getContactId(), mMember.getLookupKey());
            Uri contactUri = ContactsContract.Contacts.lookupContact(getActivity().getContentResolver(), lookupUri);
            Picasso.with(getActivity()).load(contactUri)
                    .placeholder(R.drawable.ic_contact_picture).error(R.drawable.ic_contact_picture)
                    .into(avatarImageView);
        }
        mMemberNameEditView.setText(mMember.getName());

        ContactMethodAdapter adapter = new ContactMethodAdapter(getActivity(), ContactMethod.values());
        mContactMethodSpinner.setAdapter(adapter);
        mContactMethodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ContactMethod selected = (ContactMethod) mContactMethodSpinner.getItemAtPosition(position);
                switch (selected) {
                    case EMAIL:
                        setContactDetails(selected);
                        mContactDetailsEditText.setVisibility(View.VISIBLE);
                        mContactDetailsLineSeparator.setVisibility(View.VISIBLE);
                        mContactDetailsEditText.setInputType(InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                                | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                        mContactDetailsEditText.setHint(getString(R.string.edit_email_address_hint));
                        mContactDetailsEditText.requestFocus();
                        break;
                    case SMS:
                        setContactDetails(selected);
                        mContactDetailsEditText.setVisibility(View.VISIBLE);
                        mContactDetailsLineSeparator.setVisibility(View.VISIBLE);
                        mContactDetailsEditText.setInputType(InputType.TYPE_CLASS_PHONE);
                        mContactDetailsEditText.setHint(getString(R.string.edit_sms_number_hint));
                        mContactDetailsEditText.requestFocus();
                        break;
                    case REVEAL_ONLY:
                        mContactDetailsEditText.setVisibility(View.INVISIBLE);
                        mContactDetailsLineSeparator.setVisibility(View.INVISIBLE);
                        mContactDetailsEditText.setText("");
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported Contact Method: " + selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mContactMethodSpinner.setSelection(mMember.getContactMethod().ordinal());
        mContactDetailsEditText.setText(mMember.getContactDetails());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_member_editor, container, false);

        mMemberNameEditView = (EditText) view.findViewById(R.id.et_edit_name);
        mContactMethodSpinner = (Spinner) view.findViewById(R.id.spnr_contact_method);

        // Contact details area
        mContactDetailsLineSeparator = view.findViewById(R.id.contact_details_separator_line);
        mContactDetailsEditText = (EditText) view.findViewById(R.id.et_edit_contact_detail);

        return view;
    }

    // If the user already has a contact method, then set those details
    public void setContactDetails(ContactMethod selected) {
        if (selected == mMember.getContactMethod()) {
            mContactDetailsEditText.setText(mMember.getContactDetails());
        } else {
            mContactDetailsEditText.setText("");
        }
    }

    @Override
    public boolean save() {
        Log.d(TAG, "doSaveAction() - start");

        String name = mMemberNameEditView.getText().toString().trim();
        ContactMethod contactMethod = (ContactMethod) mContactMethodSpinner.getSelectedItem();
        // Doubly enforce that Reveal mode have no contact details
        String contactDetails = contactMethod == ContactMethod.REVEAL_ONLY ? null : mContactDetailsEditText.getText().toString().trim();
        Log.d(TAG, "doSaveAction() - name: " + name + " contactMethod: " + contactMethod + " contactDetails: " + contactDetails);

        boolean isValid =
                checkField(mMemberNameEditView, new MemberNameValidator(mDb, mMember.getGroupId(), mMember.getId(), name))
                        && checkField(mContactDetailsEditText, new ContactDetailsValidator(contactMethod, contactDetails));

        if (!isValid) return false;

        /*
         * Important, handle the following scenarios -
         *
         * 1. Edited member is has no Assignment; can change freely
         * 2. If edited member has Assignment and name changed, need to reset all
         *      assignment status to Assigned
         * 3. If edited member has Assignment and only contact method/details changed
         *      then reset only the edited Member's assignment status to Assigned
         *
         * The most critical thing to note here is that we - KEEP ANY ASSIGNMENTS AND ONLY CHANGE THEIR SEND STATUS
         */
        Log.d(TAG, "doSaveAction() - content is valid");
        Assignment assignment = mDb.queryAssignmentForMember(mMember.getId());
        boolean hasAssignment = assignment != null;

        // Apply rules as above
        if (hasAssignment) {
            if (!name.equals(mMember.getName())) {
                Log.d(TAG, "doSaveAction() - name has changed");
                mDb.updateAllAssignmentsInGroup(mMember.getGroupId(), Assignment.Status.Assigned);
            } else if (!mMember.getContactMethod().equals(contactMethod)
                    || (contactDetails != null && !contactDetails.equals(mMember.getContactDetails()))
                    || (contactDetails == null && mMember.getContactDetails() != null)) {
                Log.d(TAG, "doSaveAction() - contact method or details have changed");
                assignment.setSendStatus(Assignment.Status.Assigned);
                mDb.update(assignment);
            }
        }

        // Update the member
        mMember.setName(name);
        mMember.setContactMethod(contactMethod);
        mMember.setContactDetails(contactDetails);
        mDb.update(mMember);
        return true;
    }

    private boolean checkField(final EditText et, Validator _validator) {
        boolean isFieldValid = _validator.isValid();
        if (!isFieldValid) {
            et.setError(_validator.getMsg());
        }
        return isFieldValid;
    }
}
