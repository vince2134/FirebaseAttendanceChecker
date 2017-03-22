package com.example.vince.firebaseattendancechecker;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.vince.firebaseattendancechecker.support.RecyclerViewEmptySupport;
import com.firebase.client.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Vince on 3/22/2017.
 */

public class AttendanceFragment extends Fragment {

    private static int tabID;
    AttendanceFilter filter;
    RecyclerViewEmptySupport recView;
    AttendanceAdapter adapter;
    DatabaseReference mDatabase;

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
        View v = inflater.inflate(R.layout.activity_list, container, false);

        Firebase.setAndroidContext(getContext());
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        filter = new AttendanceFilter();
        filter.setBuilding(getBuilding());
        filter.setRID(getRID());
        filter.setStartMillis(getStartMillis());
        filter.setDone(getDone());
        filter.setSubmitted(getSubmitted());

        recView = (RecyclerViewEmptySupport) v.findViewById(R.id.rec_list);
        recView.setLayoutManager(new LinearLayoutManager(getContext()));
        recView.setEmptyView(v.findViewById(R.id.empty_view));

        if(getContext() != null) {
            adapter = new AttendanceAdapter(Attendance.class, R.layout.list_item, AttendanceAdapter.AttendanceViewHolder.class, mDatabase);
            recView.setAdapter(adapter);
            adapter.setOnItemClickListener(new AttendanceAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(String name) {
                    Toast.makeText(getContext(), "CLICKKKKK", Toast.LENGTH_SHORT).show();
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
        return (getArguments().getInt("START_M"));
    }

    boolean getDone(){ return (getArguments().getBoolean("ISDONE"));}

    boolean getSubmitted() { return (getArguments().getBoolean("ISSUBMITTED"));}
}
