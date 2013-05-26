package com.moac.android.opensecretsanta.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.database.OpenSecretSantaDB;
import com.moac.android.opensecretsanta.types.DrawResult.DrawResultColumns;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DrawHistoryActivity extends Activity {

    private final static String TAG = "DrawHistoryActivity";

    // Initialise to All groups
    public static final long ALL_GROUPS = -1;
    private long mGroupId = ALL_GROUPS;

    private List<DrawRowDetails> items;
    private ArrayAdapter<DrawRowDetails> aa;
    ListView mList;

    OpenSecretSantaDB mDatabase;

    private ViewSwitcher mSwitcher;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Activity State: onCreate()");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.draw_history_view);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            mGroupId = extras.getLong(Constants.GROUP_ID);
            Log.v(TAG, "onCreate() - got groupId: " + mGroupId);
        }

        mDatabase = OpenSecretSantaApplication.getDatabase();

        mSwitcher = (ViewSwitcher) findViewById(R.id.drawHistorySwitcher);
        mSwitcher.setAnimateFirstView(false);

        mList = (ListView) findViewById(R.id.drawHistoryList);

        // Setup the click listener to open the assignments viewer.
        mList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                Log.v(TAG, "onItemClick");

                DrawRowDetails selected = (DrawRowDetails) aa.getItem(myItemInt);
                long drawResultId = selected.getDrawId();
                Log.v(TAG, "setOnItemClickListener() drawResultId " + drawResultId);

                Intent myIntent = new Intent(myView.getContext(), AssignmentViewerActivity.class);
                myIntent.putExtra(Constants.DRAW_RESULT_ID, drawResultId);

                myView.getContext().startActivity(myIntent);
            }
        });

        // Set up the content.
        items = new ArrayList<DrawRowDetails>();
        aa = new DrawResultListAdapter(this, R.layout.draw_row, items);
        mList.setAdapter(aa);
    }

    @Override
    public void onStart() {
        super.onStart();
        mSwitcher.reset();
        populateDrawList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class DrawRowDetails implements Comparable<DrawRowDetails> {

        private long drawId;
        String groupName;
        long drawDate;
        long sendDate;
        long memberCount; // From the Draw Result - NOT the group.
        long groupId;

        public DrawRowDetails(long drawId, String groupName, long _drawDate, long _sendDate, long _memberCount, long _groupId) {
            this.drawId = drawId;
            this.groupName = groupName;
            this.drawDate = _drawDate;
            this.sendDate = _sendDate;
            this.memberCount = _memberCount;
            this.groupId = _groupId;
        }

        public long getDrawId() {
            return drawId;
        }

        public String getGroupName() {
            return groupName;
        }

        public long getDrawDate() {
            return drawDate;
        }

        public long getSendDate() {
            return sendDate;
        }

        public long getMemberCount() {
            return memberCount;
        }

        public long getGroupId() {
            return groupId;
        }

        @Override
        public int compareTo(DrawRowDetails another) {
            return (int) (Math.max(another.drawDate, another.sendDate) - Math.max(this.drawDate, this.sendDate));
        }
    }

    private void populateDrawList() {

        AsyncTask<Void, Void, List<DrawRowDetails>> task = new AsyncTask<Void, Void, List<DrawRowDetails>>() {

            @Override
            protected List<DrawRowDetails> doInBackground(Void... params) {
                Log.v(TAG, "populateDrawList() - populating draw history list");
                List<DrawRowDetails> rows = new ArrayList<DrawRowDetails>();

                Cursor cursor = null;
                if(mGroupId != DrawHistoryActivity.ALL_GROUPS) {
                    cursor = mDatabase.getAllDrawResultsForGroupCursor(mGroupId);
                } else {
                    cursor = mDatabase.getAllDrawResultsCursor();
                }

                if(cursor.moveToFirst()) {
                    Log.v(TAG, "Cursor: getColumnCount(): " + cursor.getColumnCount());

                    do {
                        long groupId = cursor.getLong(cursor.getColumnIndex(DrawResultColumns.GROUP_ID_COLUMN));
                        String groupName = mDatabase.getGroupById(groupId).getName();
                        long drawDate = cursor.getLong(cursor.getColumnIndex(DrawResultColumns.DRAW_DATE_COLUMN));
                        long sendDate = cursor.getLong(cursor.getColumnIndex(DrawResultColumns.SEND_DATE_COLUMN));
                        long drawId = cursor.getLong(cursor.getColumnIndex(DrawResultColumns._ID));
                        long memberCount = mDatabase.getAllDrawResultEntriesForDrawId(drawId).size();

                        DrawRowDetails row = new DrawRowDetails(drawId, groupName, drawDate, sendDate, memberCount, groupId);
                        rows.add(row);
                    } while(cursor.moveToNext());
                }
                cursor.close();

                Collections.sort(rows);

                Log.v(TAG, "populateDrawList() - row count: " + rows.size());
                return rows;
            }

            @Override
            protected void onPostExecute(List<DrawRowDetails> rows) {

                // Set this here - NOT in the background thread, otherwise might corrupt listadapter.
                items = rows;
                aa.clear();
                for(DrawRowDetails dr : items) {
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

    private class DeleteDrawTag {

        public DeleteDrawTag(long _drawId, long _groupId) {
            this.drawId = _drawId;
            this.groupId = _groupId;
        }

        long drawId;
        long groupId;
    }

    private class DrawResultListAdapter extends ArrayAdapter<DrawRowDetails> {
        int resource;

        public DrawResultListAdapter(Context context, int _resource,
                                     List<DrawRowDetails> draws) {
            super(context, _resource, draws);
            resource = _resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //	Log.v(TAG, "getView() - start");
            //	Log.v(TAG, "getView() - position: " + position);

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
            DrawRowDetails dr = getItem(position);
            TextView tv = (TextView) newView.findViewById(R.id.drawrow_nameTextView);
            tv.setText(dr.getGroupName());

            TextView tv2 = (TextView) newView.findViewById(R.id.drawrow_memberCountTextView);
            tv2.setText(Long.toString(dr.getMemberCount()));

//			TextView tv3 = (TextView) newView.findViewById(R.id.drawrow_sentDateTextView);
            final Date dateToUse = new Date(Math.max(dr.getDrawDate(), dr.getSendDate()));
//			final SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy");
//			String dateString = sdf.format(dateToUse);
//			tv3.setText(dateString);

            TextView tv4 = (TextView) newView.findViewById(R.id.drawrow_DateTextView);
            final SimpleDateFormat sdf2 = new SimpleDateFormat("h:mm a EEE, d MMM yyyy");
            String timeString = sdf2.format(dateToUse);
            tv4.setText(timeString);

            ImageView deleteButton = (ImageView) newView.findViewById(R.id.removeDrawButton);
            deleteButton.setOnClickListener(mDeleteClickListener);
            deleteButton.setTag(new DeleteDrawTag(dr.getDrawId(), dr.getGroupId()));

            //	Log.v(TAG, "getView() - end");

            return newView;
        }
    }

    private View.OnClickListener mDeleteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            final long drawId = ((DeleteDrawTag) view.getTag()).drawId;
            final long groupId = ((DeleteDrawTag) view.getTag()).groupId;

            // Confirm delete of group
            AlertDialog.Builder builder = new AlertDialog.Builder(DrawHistoryActivity.this);
            builder.setTitle("Delete this draw?")
              .setIcon(R.drawable.ic_menu_delete)
              .setNegativeButton("Cancel", null)
              .setCancelable(true)
              .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int id) {
                      // The view is the button, which has as tag the memberId
                      removeDraw(drawId, groupId);
                  }
              });
            AlertDialog alert = builder.create();
            alert.show();
        }
    };

    private void removeDraw(final long _drawId, final long _groupId) {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                // If we have removed the latest draw result for a group, then
                // set that group to be NOT ready.
                // This is slightly heavy handed, but prevents the draw result
                // displayed in the draw builder area from not matching with the
                // group - as it will force a redraw.
                Log.v(TAG, "removeDraw() - removing: " + _drawId + " setting gid not ready: " + _groupId);
                long latestDrawResultId = mDatabase.getLatestDrawResultId(_groupId);
                if(latestDrawResultId == _drawId) {
                    mDatabase.setGroupIsReady(_groupId, false);
                }
                mDatabase.removeDrawResult(_drawId);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                populateDrawList();
            }
        };

        task.execute();
    }
}
