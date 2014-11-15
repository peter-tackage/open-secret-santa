package com.moac.android.opensecretsanta.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

    private static final String TAG = MemberEditFragment.class.getSimpleName();

    @Inject
    DatabaseManager mDb;

    private Member mMember;
    private EditText mMemberNameEditView;
    private EditText mContactDetailsEditText;
    private Spinner mContactMethodSpinner;
    private boolean mIsRecreated;
    boolean mIsSpinnerSelfInitialised;
    private boolean mHaveRestored;

    public static MemberEditFragment create(long memberId) {
        MemberEditFragment fragment = new MemberEditFragment();
        Bundle args = new Bundle();
        args.putLong(Intents.MEMBER_ID_INTENT_EXTRA, memberId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // The database is injected in super.onActivityCreated()
        long memberId = getArguments().getLong(Intents.MEMBER_ID_INTENT_EXTRA);
        mMember = mDb.queryById(memberId, Member.class);
        Log.i(TAG, "onActivityCreated() Member name: " + mMemberNameEditView.getText());
        mIsRecreated = savedInstanceState != null;
        populateViews(mIsRecreated);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        View rootView = inflater.inflate(R.layout.fragment_member_editor, container, false);
        bindViews(rootView);
        return rootView;
    }

    @Override
    public boolean save() {
        Log.d(TAG, "save() - start");

        String name = mMemberNameEditView.getText().toString().trim();
        ContactMethod contactMethod = (ContactMethod) mContactMethodSpinner.getSelectedItem();
        // Strictly enforce that Reveal Only mode has no contact details
        String contactDetails = contactMethod == ContactMethod.REVEAL_ONLY ? null : mContactDetailsEditText.getText().toString().trim();
        Log.d(TAG, "save() - name: " + name + " contactMethod: " + contactMethod + " contactDetails: " + contactDetails);

        boolean isValid =
                validateField(mMemberNameEditView, new MemberNameValidator(mDb, mMember.getGroupId(), mMember.getId(), name))
                        && validateField(mContactDetailsEditText, new ContactDetailsValidator(contactMethod, contactDetails));

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
         * The most critical thing to note here is that we - KEEP ANY ASSIGNMENTS AND ONLY CHANGE THEIR SENT STATUS
         */
        Log.d(TAG, "save() - content is valid");
        Assignment assignment = mDb.queryAssignmentForMember(mMember.getId());
        boolean hasAssignment = assignment != null;

        // Apply rules as above
        if (hasAssignment) {
            if (!name.equals(mMember.getName())) {
                Log.d(TAG, "save() - name has changed");
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

    private void bindViews(View view) {
        mMemberNameEditView = (EditText) view.findViewById(R.id.editText_name);
        mContactMethodSpinner = (Spinner) view.findViewById(R.id.spinner_contact_method);
        mContactDetailsEditText = (EditText) view.findViewById(R.id.editText_contact_detail);
    }

    private void populateViews(final boolean isRecreated) {

        Log.i(TAG, "populateViews() - isRecreated: " + isRecreated);

        // Static, uneditable components
        TextView titleTextView = (TextView) getView().findViewById(R.id.textView_groupName);
        populateAvatar();

        // Member name
        titleTextView.setText(mMember.getName());

        // Contact Method Spinner
        ContactMethodAdapter adapter = new ContactMethodAdapter(getActivity(), ContactMethod.values());
        mContactMethodSpinner.setAdapter(adapter);
        mContactMethodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemSelected() isSpinnerInitialised: " + mIsSpinnerSelfInitialised);
                Log.i(TAG, "onItemSelected() mIsRecreated: " + mIsRecreated);

                // When restoring the Activity we get one bogus call. Ignore it.
                if (mIsRecreated) {
                    if (!mIsSpinnerSelfInitialised) {
                        mIsSpinnerSelfInitialised = true;
                        return;
                    }
                }

                ContactMethod selected = (ContactMethod) mContactMethodSpinner.getItemAtPosition(position);
                Log.i(TAG, "onItemSelected() setting to: " + selected);
                onContactMethodChanged(selected);

                // Once the second programmatic call completes (for a recreate) set the flag
                // to that changes to the spinner may affect the associated edittext content.
                if (mIsRecreated) {
                    mHaveRestored = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing (we always have a selection)
            }
        });

        // Read from database values on initial create
        if (!isRecreated) {
            mMemberNameEditView.setText(mMember.getName());
            mContactMethodSpinner.setSelection(mMember.getContactMethod().ordinal());
            mContactDetailsEditText.setText(mMember.getContactDetails());
        }

    }

    private boolean isSpinnerReady() {
        return !mIsRecreated || mIsSpinnerSelfInitialised && mHaveRestored;
    }

    private void onContactMethodChanged(ContactMethod contactMethod) {
        Log.i(TAG, "onContactMethodChanged() - contactMethod: " + contactMethod);
        boolean isModified = mMember.getContactMethod() != contactMethod;

        switch (contactMethod) {
            case EMAIL:
                if (isSpinnerReady()) {
                    resetContactDetails(isModified);
                }
                mContactDetailsEditText.setVisibility(View.VISIBLE);
                mContactDetailsEditText.setInputType(InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                        | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                mContactDetailsEditText.setHint(getString(R.string.edit_email_address_hint));
                mContactDetailsEditText.requestFocus();
                break;
            case SMS:
                if (isSpinnerReady()) {
                    resetContactDetails(isModified);
                }
                mContactDetailsEditText.setVisibility(View.VISIBLE);
                mContactDetailsEditText.setInputType(InputType.TYPE_CLASS_PHONE);
                mContactDetailsEditText.setHint(getString(R.string.edit_sms_number_hint));
                mContactDetailsEditText.requestFocus();
                break;
            case REVEAL_ONLY:
                mContactDetailsEditText.setVisibility(View.INVISIBLE);
                mContactDetailsEditText.setText("");
                break;
            default:
                throw new IllegalArgumentException("Unsupported Contact Method: " + contactMethod);
        }
    }

    private void resetContactDetails(boolean isContactModeChanged) {
        Log.i(TAG, "resetContactDetails() isContactModeChanged: " + isContactModeChanged);
        if (isContactModeChanged) {
            // Modified from original; clear the field
            Log.i(TAG, "resetContactDetails() CLEARING FIELD");
            mContactDetailsEditText.setText("");

        } else {
            // Not modified from original; use those details
            Log.i(TAG, "resetContactDetails() RESET TO MEMBER");
            mContactDetailsEditText.setText(mMember.getContactDetails());
        }
    }

    private void populateAvatar() {
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
    }

    private static boolean validateField(final EditText editText, Validator validator) {
        boolean isFieldValid = validator.isValid();
        if (!isFieldValid) {
            editText.setError(validator.getMsg());
        }
        return isFieldValid;
    }
}
