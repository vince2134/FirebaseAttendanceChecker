package com.mobapde.vince.mobapde;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mobapde.vince.mobapde.support.RecyclerViewEmptySupport;

/**
 * Created by Vince on 3/22/2017.
 */

public class AttendanceFragment extends Fragment {

    private static int tabID;
    AttendanceFilter filter;
    RecyclerViewEmptySupport recView;
    AttendanceAdapter adapter;
    DatabaseReference mDatabase;
    TextView empty;
    View v;

    public static AttendanceFragment newInstance(AttendanceFilter filter) {
        AttendanceFragment f = new AttendanceFragment();

        Bundle args = new Bundle();

        args.putString("RID", filter.getRID());
        args.putString("BUILDING", filter.getBuilding());
        args.putLong("START_M", filter.getStartMillis());
        args.putBoolean("ISDONE", filter.getDone());
        args.putBoolean("ISSUBMITTED", filter.getSubmitted());
        args.putInt("TAB_ID", filter.getTab());
        f.setArguments(args);

        tabID = filter.getTab();

        return (f);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.activity_list, container, false);

        initializeFilter();

        Log.d("AttendanceFragment", filter.getTab() + "");

        Firebase.setAndroidContext(getContext());

        //filter(filter);
        handleFilter();

        if(getContext() != null) {
            adapter = new AttendanceAdapter(Attendance.class, R.layout.list_item, AttendanceAdapter.AttendanceViewHolder.class, mDatabase);
            recView.setAdapter(adapter);
            adapter.setOnItemClickListener(new AttendanceAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(String name) {
                    Toast.makeText(getContext(), name, Toast.LENGTH_SHORT).show();
                }
            });
        }

        return v;
    }

    String getRID() {
        return (getArguments().getString("RID"));
    }

    String getBuilding() {
        return (getArguments().getString("BUILDING"));
    }

    long getStartMillis() {
        return (getArguments().getLong("START_M"));
    }

    boolean getDone(){ return (getArguments().getBoolean("ISDONE"));}

    int getTab(){
        return getArguments().getInt("TAB_ID");
    }

    boolean getSubmitted() { return (getArguments().getBoolean("ISSUBMITTED"));}

    public void filter(AttendanceFilter filter){

    }

    public void initializeFilter(){
        //INITIALIZE FILTER
        filter = new AttendanceFilter();
        filter.setBuilding(getBuilding());
        filter.setRID(getRID());
        filter.setStartMillis(getStartMillis());
        filter.setDone(getDone());
        filter.setSubmitted(getSubmitted());
        filter.setTab(getTab());
    }

    public void handleFilter(){
        if(filter.getTab() == 0) {
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
            empty = (TextView) v.findViewById(R.id.empty_view);
            empty.setText("No pending attendance");
        }
        else
            mDatabase = FirebaseDatabase.getInstance().getReference().child("");

        mDatabase.keepSynced(true);

        Log.d("FIREBASE", mDatabase.getKey() + "");

        recView = (RecyclerViewEmptySupport) v.findViewById(R.id.rec_list);
        recView.setLayoutManager(new LinearLayoutManager(getContext()));

        if(mDatabase.getKey() == null) {
            if(filter.getTab() == 1) {
                empty = (TextView) v.findViewById(R.id.empty_view);
                empty.setText("No finished attendance yet");
            }
            recView.setEmptyView(v.findViewById(R.id.empty_view));
        }
    }

    /*private class ShowSpinnerTask extends AsyncTask<Void, Void, Void> {
        SpinnerFragment mSpinnerFragment;

        @Override
        protected void onPreExecute() {
            mSpinnerFragment = new SpinnerFragment();
            getFragmentManager().beginTransaction().add(R.id.activity_list, mSpinnerFragment).commit();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Do some background process here.
            // It just waits 5 sec in this Tutorial
            //mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            getFragmentManager().beginTransaction().remove(mSpinnerFragment).commit();
        }
    }*/

}
