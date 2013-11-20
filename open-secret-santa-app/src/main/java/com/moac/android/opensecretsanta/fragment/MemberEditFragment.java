package com.moac.android.opensecretsanta.fragment;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.activity.Intents;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.model.ContactMethod;
import com.moac.android.opensecretsanta.model.Member;
import com.moac.android.opensecretsanta.model.PersistableObject;
import com.moac.android.opensecretsanta.util.ContactDetailsValidator;
import com.moac.android.opensecretsanta.util.MemberNameValidator;
import com.moac.android.opensecretsanta.util.Validator;
import com.squareup.picasso.Picasso;

public class MemberEditFragment extends Fragment {

    private DatabaseManager mDb;
    private Member mMember;
    private EditText mMemberNameEditView;
    private EditText mContactDetailsEditText;
    private Spinner mContactMethodSpinner;
    private ImageView mAvatarImageView;

    public static MemberEditFragment create(long _memberId) {
        MemberEditFragment fragment = new MemberEditFragment();
        Bundle args = new Bundle();
        args.putLong(Intents.MEMBER_ID_INTENT_EXTRA, _memberId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDb = OpenSecretSantaApplication.getInstance().getDatabase();
        long memberId = getArguments().getLong(Intents.MEMBER_ID_INTENT_EXTRA);
        mMember = mDb.queryById(memberId, Member.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_member_editor, container, false);
        TextView titleTextView = (TextView) view.findViewById(R.id.content_title_textview);
        titleTextView.setText("Edit Member - " + mMember.getName());

        mAvatarImageView = (ImageView) view.findViewById(R.id.iv_avatar);
        mMemberNameEditView = (EditText) view.findViewById(R.id.et_edit_name);
        mContactMethodSpinner = (Spinner) view.findViewById(R.id.spnr_contact_method);
        mContactDetailsEditText = (EditText) view.findViewById(R.id.et_edit_contact_detail);

        // Assign the view with its content.
        if(mMember.getContactId() == PersistableObject.UNSET_ID || mMember.getLookupKey() == null) {
            Picasso.with(getActivity()).load(R.drawable.ic_contact_picture).into(mAvatarImageView);
        } else {
            Uri lookupUri = ContactsContract.Contacts.getLookupUri(mMember.getContactId(), mMember.getLookupKey());
            Uri contactUri = ContactsContract.Contacts.lookupContact(getActivity().getContentResolver(), lookupUri);
            Picasso.with(getActivity()).load(contactUri)
              .placeholder(R.drawable.ic_contact_picture).error(R.drawable.ic_contact_picture)
              .into(mAvatarImageView);
        }
        mMemberNameEditView.setText(mMember.getName());

        ArrayAdapter adapter = new ArrayAdapter<ContactMethod>(getActivity(), android.R.layout.simple_spinner_item, ContactMethod.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mContactMethodSpinner.setAdapter(adapter);
        mContactMethodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ContactMethod selected = (ContactMethod) mContactMethodSpinner.getItemAtPosition(position);
                switch(selected) {
                    case EMAIL:
                        setContactDetails(selected);
                        mContactDetailsEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                        mContactDetailsEditText.setVisibility(View.VISIBLE);
                        mContactDetailsEditText.setHint("Email Address");
                        break;
                    case SMS:
                        setContactDetails(selected);
                        mContactDetailsEditText.setInputType(InputType.TYPE_CLASS_PHONE);
                        mContactDetailsEditText.setVisibility(View.VISIBLE);
                        mContactDetailsEditText.setHint("Mobile Number");
                        break;
                    case REVEAL_ONLY:
                        mContactDetailsEditText.setVisibility(View.INVISIBLE);
                        mContactDetailsEditText.setText("");
                        break;
                    default:
                        // TODO Throw

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mContactMethodSpinner.setSelection(mMember.getContactMethod().ordinal());
        mContactDetailsEditText.setText(mMember.getContactDetails());
        return view;
    }

    public void setContactDetails(ContactMethod selected) {
        if(selected == mMember.getContactMethod()) {
            mContactDetailsEditText.setText(mMember.getContactDetails());
        } else {
            mContactDetailsEditText.setText("");
        }
    }

    public boolean doSaveAction() {

        String name = mMemberNameEditView.getText().toString().trim();
        ContactMethod contactMethod = (ContactMethod) mContactMethodSpinner.getSelectedItem();
        // Doubly enforce that Reveal mode have no contact details
        String contactDetails = contactMethod == ContactMethod.REVEAL_ONLY ? null : mContactDetailsEditText.getText().toString().trim();

        boolean isValid =
          checkField(mMemberNameEditView, new MemberNameValidator(mDb, mMember.getGroupId(), mMember.getId(), name), "")
            && checkField(mContactDetailsEditText, new ContactDetailsValidator(contactMethod, contactDetails), "");

        if(!isValid) {
            return false;
        }

        mMember.setName(name);
        mMember.setContactMethod((ContactMethod) mContactMethodSpinner.getSelectedItem());
        mMember.setContactDetails(contactDetails);
        mDb.update(mMember);
        return true;
    }

    private boolean checkField(final EditText et, Validator _validator, String msg) {
        boolean isFieldValid = _validator.isValid();
        if(!isFieldValid) {
            et.setError(msg);
        }
        return isFieldValid;
    }
}
