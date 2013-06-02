package com.moac.android.opensecretsanta.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.database.DatabaseManager;
import com.moac.android.opensecretsanta.types.DrawResult;
import com.moac.android.opensecretsanta.types.Group;
import com.moac.android.opensecretsanta.types.PersistableObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupSelectionActivity extends Activity {

    private final static String TAG = "GroupSelectionActivity";

    private DatabaseManager mDatabase;

    private ImageView nextButton;
    private EditText mgroupNameView;

    private ViewSwitcher mSwitcher;

    private ListView mList;
    private List<GroupRowDetails> items;
    private ArrayAdapter<GroupRowDetails> aa;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_group_view);

        mgroupNameView = (EditText) findViewById(R.id.txtGroupName);
        mgroupNameView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        nextButton = (ImageView) findViewById(R.id.btnCreateGroup);
        nextButton.setOnClickListener(new Clicker());

        mList = (ListView) findViewById(R.id.groupsListView);
        mSwitcher = (ViewSwitcher) findViewById(R.id.groupListSwitcher);
        mSwitcher.setAnimateFirstView(false);

        mDatabase = OpenSecretSantaApplication.getDatabase();

        // Setup the click listener to open the draw builder viewer.
        mList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                Log.v(TAG, "onItemClick - start()");

                GroupRowDetails selectedItem = (GroupRowDetails) ((ListView) myAdapter).getItemAtPosition(myItemInt);

                long groupId = selectedItem.getGroupId();
                Log.v(TAG, "setOnItemClickListener() groupId " + groupId);
                String groupName = selectedItem.getGroupName();

                Intent myIntent = new Intent(myView.getContext(), DrawTabManagerActivity.class);
                myIntent.putExtra(Constants.GROUP_ID, groupId);
                myIntent.putExtra(Constants.GROUP_NAME, groupName);

                myView.getContext().startActivity(myIntent);
            }
        });
        mList.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> myAdapter, View myView,
                                           int myItemInt, long arg3) {
                GroupRowDetails selectedItem = (GroupRowDetails) ((ListView) myAdapter).getItemAtPosition(myItemInt);
                showRenameGroupDialog(selectedItem);
                return true;
            }
        });

        // Set up the content.
        items = new ArrayList<GroupRowDetails>();
        aa = new GroupListAdapter(this, R.layout.group_row, items);
        mList.setAdapter(aa);
    }

    @Override
    public void onStart() {
        super.onStart();
        mgroupNameView.setText("");
        mSwitcher.reset();
        populateGroupList();
    }

    class Clicker implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String groupName = mgroupNameView.getText().toString();
            if(!(groupName == null || groupName.length() == 0)) {
                Group mGroup = new Group();
                mGroup.setName(groupName);
                Long mNewGroupId = mDatabase.create(mGroup);

                if(mNewGroupId != PersistableObject.UNSET_ID) {
                    Intent myIntent = new Intent(v.getContext(), DrawTabManagerActivity.class);
                    myIntent.putExtra(Constants.GROUP_ID, mNewGroupId);
                    myIntent.putExtra(Constants.GROUP_NAME, groupName);
                    startActivity(myIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "Sorry, that group already exists",
                      Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void showRenameGroupDialog(final GroupRowDetails _selectedItem) {
        Log.v(TAG, "showRenameGroupDialog()");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Group");
        builder.setIcon(R.drawable.people);

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setSingleLine();
        input.setBackgroundResource(R.drawable.green_edit_text);
        input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(_selectedItem.groupName);
        // TODO Make constant - See new_group_view.xml
        InputFilter[] filters = { new InputFilter.LengthFilter(25) };
        input.setFilters(filters);

        builder.setView(input);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String groupName = input.getText().toString().trim();
                Log.v(TAG, "groupName:" + groupName);
                if(groupName != null && !groupName.equals("") && !groupName.equals(_selectedItem.groupName)) {
                    Log.v(TAG, "Attempting to updating group name from: " + _selectedItem.groupName + " to " + groupName);
                    updateGroup(_selectedItem.groupId, groupName);
                }
                // Force hide of the keyboard.
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                // Force hide of the keyboard.
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        //display dialog box
        alert.show();
    }

    private class GroupRowDetails implements Comparable<GroupRowDetails> {

        public GroupRowDetails(long groupId, String groupName, long memberCount, long lastSentDate) {
            this.groupId = groupId;
            this.groupName = groupName;
            this.memberCount = memberCount;
        }

        private long groupId;
        String groupName;
        long memberCount; // From the Group.

        public long getGroupId() {
            return groupId;
        }

        public String getGroupName() {
            return groupName;
        }

        public long getMemberCount() {
            return memberCount;
        }

        @Override
        public int compareTo(GroupRowDetails another) {
            return String.CASE_INSENSITIVE_ORDER.compare(this.groupName, another.groupName);
        }
    }

    private void populateGroupList() {

        AsyncTask<Void, Void, List<GroupRowDetails>> task = new AsyncTask<Void, Void, List<GroupRowDetails>>() {

            // ProgressDialog dialog = null;

            @Override
            protected List<GroupRowDetails> doInBackground(Void... params) {
                Log.v(TAG, "populateGroupList() - populating group list");

                List<GroupRowDetails> rows = new ArrayList<GroupRowDetails>();

                List<Group> groups = mDatabase.queryAll(Group.class);

                for(Group group : groups) {

                    long groupId = group.getId();
                    String groupName = group.getName();
                    long memberCount = mDatabase.queryAllMembersForGroup(groupId).size();
                    DrawResult latest = mDatabase.queryLatestDrawResultForGroup(groupId);
                    long date = (latest != null) ?
                      latest.getDrawDate() : Constants.UNDRAWN_DATE;

                    GroupRowDetails row = new GroupRowDetails(groupId, groupName, memberCount, date);
                    rows.add(row);
                }

                Log.v(TAG, "populateDrawList() - row count: " + rows.size());
                Collections.sort(rows);
                return rows;
            }

            @Override
            protected void onPostExecute(List<GroupRowDetails> rows) {

                // Set this here - NOT in the background thread, otherwise might corrupt listadapter.
                items = rows;
                aa.clear();
                for(GroupRowDetails dr : items) {
                    aa.add(dr);
                }
                Log.v(TAG, "onPostExecute() rows: " + rows.size());
                aa.notifyDataSetChanged();
                setExistingGroupsVisible(items.size() > 0);
            }
        };

        task.execute();
    }

    private void setExistingGroupsVisible(boolean visible) {
        if(visible) {
            mSwitcher.setDisplayedChild(1);
        } else {
            mSwitcher.setDisplayedChild(0);
        }
    }

    private class GroupListAdapter extends ArrayAdapter<GroupRowDetails> {
        int resource;

        public GroupListAdapter(Context context, int _resource,
                                List<GroupRowDetails> draws) {
            super(context, _resource, draws);
            resource = _resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //Log.v(TAG, "getView() - start");
            //Log.v(TAG, "getView() - position: " + position);

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
            GroupRowDetails gr = getItem(position);
            TextView tv = (TextView) newView.findViewById(R.id.grouprow_nameTextView);
            tv.setText(gr.getGroupName());

            TextView tv2 = (TextView) newView.findViewById(R.id.grouprow_memberCountTextView);
            tv2.setText(Long.toString(gr.getMemberCount()));

            ImageView iv = (ImageView) newView.findViewById(R.id.removeGroupButton);
            DeleteViewTag tag = new DeleteViewTag();
            tag.groupId = gr.getGroupId();
            tag.groupName = gr.getGroupName();

            iv.setTag(tag);
            iv.setOnClickListener(mDeleteClickListener);

            //Log.v(TAG, "getView() - end");

            return newView;
        }
    }

    private class DeleteViewTag {
        long groupId;
        String groupName;
    }

    private View.OnClickListener mDeleteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            final DeleteViewTag tag = (DeleteViewTag) view.getTag();

            // Confirm delete of group
            AlertDialog.Builder builder = new AlertDialog.Builder(GroupSelectionActivity.this);
            builder.setTitle("Delete the " + tag.groupName + " group and its history?")
              .setIcon(R.drawable.ic_menu_delete)
              .setNegativeButton("Cancel", null)
              .setCancelable(true)
              .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int id) {
                      // The view is the button, which has as tag the memberId
                      removeGroup(tag.groupId);
                  }
              });
            AlertDialog alert = builder.create();
            alert.show();
        }
    };

    private void removeGroup(final long groupId) {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                mDatabase.delete(groupId, Group.class);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                populateGroupList();
            }
        };

        task.execute();
    }

    private void updateGroup(final long _groupId, final String _groupName) {

        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {

                // Get the group with that id.
                Group existingGroup = mDatabase.queryById(_groupId, Group.class);
                existingGroup.setName(_groupName);
                mDatabase.update(existingGroup);

                return Boolean.TRUE;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if(result.equals(Boolean.TRUE)) {
                    populateGroupList();
                } else {
                    // TODO Only true if group isn't deleted from underneath us!
                    Toast.makeText(GroupSelectionActivity.this, "Sorry, that group name already exists", Toast.LENGTH_SHORT).show();
                }
                mgroupNameView.clearFocus();
            }
        };

        task.execute();
    }
}

