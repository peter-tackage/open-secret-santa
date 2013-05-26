package com.moac.android.opensecretsanta.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
import com.moac.android.opensecretsanta.R;
import com.moac.android.opensecretsanta.database.OpenSecretSantaDB;
import com.moac.android.opensecretsanta.types.DrawResult;
import com.moac.android.opensecretsanta.types.DrawResultEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssignmentViewerActivity extends Activity {

    final static String TAG = "AssignmentViewerActivity";

    final static int DIALOG_ASSIGNMENT = 1;

    private long mResultId = -1;

    private List<DrawResultEntry> items;
    private ArrayAdapter<DrawResultEntry> aa;
    ListView mList;

    AlertDialog mViewerDialog;

    OpenSecretSantaDB mDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.assignments_view_wrapper);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            mResultId = extras.getLong(Constants.DRAW_RESULT_ID);
            Log.v(TAG, "onCreate() - got mResultId: " + mResultId);
        }

        mDatabase = OpenSecretSantaApplication.getDatabase();

        mList = (ListView) findViewById(R.id.assignmentsListView);

        items = new ArrayList<DrawResultEntry>();
        aa = new AssignmentsListAdapter(this, R.layout.assignment_row, items, new OnClickListener() {

            @Override
            public void onClick(View view) {

                Log.v(TAG, "setOnItemClickListener() onItemClick - start");

                DrawResultEntry selectedItem = (DrawResultEntry) view.getTag();
                // Has been viewed now - so updated the DB (async)
                updateDrawResultEntry(selectedItem);

                AlertDialog.Builder builder;

                Context mContext = AssignmentViewerActivity.this;
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.assignee_viewer_dialog,
                  (ViewGroup) findViewById(R.id.layout_root));

                ImageView image = (ImageView) layout.findViewById(R.id.image);
                image.setImageResource(R.drawable.ic_present);

                TextView text = (TextView) layout.findViewById(R.id.assignmentMemberText);
                text.setText(selectedItem.getGiverName() + " was assigned - ");
                TextView text2 = (TextView) layout.findViewById(R.id.assignmentAssigneeText);
                Log.v(TAG, "onItemClick() - assignee: " + selectedItem.getReceiverName());
                text2.setText(selectedItem.getReceiverName());

                builder = new AlertDialog.Builder(mContext);
                builder.setView(layout);
                mViewerDialog = builder.create();
                mViewerDialog.show();
            }
        });

        mList.setAdapter(aa);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.v(TAG, "onResume() - start");
        Log.v(TAG, "onResume() mResultId: " + mResultId);

        populateViews();

        Log.v(TAG, "onResume() - end");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Log.v(TAG, "onCreateDialog() - start");

        Dialog dialog;
        switch(id) {
            case DIALOG_ASSIGNMENT:
                Log.v(TAG, "onCreateDialog() - DIALOG_ASSIGNMENT");
                return mViewerDialog;
            default:
                dialog = null;
        }
        return dialog;
    }

    private void populateViews() {
        populateAssignmentsList();
    }

    private class DrawResultDetails {
        DrawResult dr;
        List<DrawResultEntry> dres;
    }

    private void populateAssignmentsList() {

        AsyncTask<Void, Void, DrawResultDetails> task = new AsyncTask<Void, Void, DrawResultDetails>() {

            @Override
            protected DrawResultDetails doInBackground(Void... params) {

                Log.v(TAG, "populateAssignmentsList() - populating assignments list");

                DrawResultDetails drd = new DrawResultDetails();

                // Retrieve the draw - this better work.
                drd.dr = mDatabase.getDrawResultById(mResultId);

                // Get the rows.
                drd.dres = mDatabase.getAllDrawResultEntriesForDrawId(mResultId);

                Log.v(TAG, "populateAssignmentsList() - row count: " + drd.dres.size());

                Collections.sort(drd.dres);
                return drd;
            }

            @Override
            protected void onPostExecute(DrawResultDetails drDetails) {

//				// Set the date to either the send date or the draw date
//				final long drawDate = drDetails.dr.getDrawDate();
//				final long sendDate = drDetails.dr.getSendDate();
//				boolean useSend = (sendDate >= drawDate);
//				Date sentDateObj = new Date(
//						(useSend) ? sendDate : drawDate);
//				final SimpleDateFormat sdf = new SimpleDateFormat("h:mm a EEE, d MMM yyyy");
//				String dateString = sdf.format(sentDateObj);
//				mDateTextView.setText((useSend ? "Shared: " : "Drawn: ") + dateString);

                // Set this here - NOT in the background thread, otherwise might corrupt listadapter.
                items = drDetails.dres;
                aa.clear();
                for(DrawResultEntry dre : items) {
                    aa.add(dre);
                }
                Log.v(TAG, "onPostExecute() rows: " + items.size());
                aa.notifyDataSetChanged();
            }
        };

        task.execute();
    }

    private void updateDrawResultEntry(DrawResultEntry assignment) {

        final DrawResultEntry assign = assignment;

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                assign.setViewedDate(System.currentTimeMillis());
                // TODO BE consistent! id is already part of assign!
                mDatabase.updateDrawResultEntry(assign, assign.getId());
                populateAssignmentsList();

                return null;
            }
        };

        task.execute();
    }
}