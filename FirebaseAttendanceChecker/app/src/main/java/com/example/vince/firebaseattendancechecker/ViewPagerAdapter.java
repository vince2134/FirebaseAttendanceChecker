package com.example.vince.firebaseattendancechecker;

/**
 * Created by avggo on 10/23/2016.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import static com.example.vince.firebaseattendancechecker.MainActivity.submitted;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    CharSequence Titles[];
    int NumbOfTabs;
    AttendanceFilter filter;

    private AttendanceFragment al;
    private int currentTab;

    public ViewPagerAdapter(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabs, AttendanceFilter filter) {
        super(fm);
        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabs;
        this.currentTab = 0;
        this.filter = filter;
    }

    public Fragment getItem(int position) {
        //Log.i("tagg", "ViewPagerAdapter.getItem()   -- called");
        if (position == 0) {
            Log.i("tagg", "ViewPagerAdapter.getItem()   -- position ZERO called");
            al = AttendanceFragment.newInstance(filter);
            return al;
        } else if (position == 1) {
            Log.i("tagg", "ViewPagerAdapter.getItem()   -- position ONE called");
            filter.setDone(true);
            filter.setSubmitted(!submitted);
            if(!submitted)
                filter.setSubmitted(submitted);
             al = AttendanceFragment.newInstance(filter);
            return al;
        } else
            return null;
    }

    public AttendanceFragment getFragment(){
        return this.al;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }

    public int getCount() {
        return NumbOfTabs;
    }
}
