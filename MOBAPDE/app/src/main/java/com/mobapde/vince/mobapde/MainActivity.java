package com.mobapde.vince.mobapde;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    public static final int TAB_NUMBERS = 2;
    public static final int PENDING_TAB = 0;
    public static final int DONE_TAB = 1;

    private static int ATTENDANCE_DETAIL_CODE = 0;
    private static final String TAG = "MainActivity";

    public static Boolean submitted = false;
    private Toolbar toolbar;
    private ViewPager viewPager;
    private ViewPagerAdapter pagerAdapter;
    private SlidingTabLayout tabSlider;
    private CharSequence tabList[] = {"pending", "done"};
    private String profileUrl;
    private String name;

    NavigationView mNavigationView;
    DrawerLayout drawer;                                  // Declaring DrawerLayout
    ActionBarDrawerToggle mDrawerToggle;                  // Declaring Action Bar Drawer Toggle

    TextView emptyView, tvName, tvEmail;
    Button btnSubmit;
    CircleImageView civProfilePic;
    View nView;

    AttendanceFilter primaryFilter;

    private FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    DatabaseReference mDatabaseUsers;

    ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgress = new ProgressDialog(MainActivity.this);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    finish();
                    startActivity(loginIntent);
                } else;
                    //FirebaseUtils.initialize();
            }
        };

        mAuth.addAuthStateListener(mAuthListener);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);

        primaryFilter = new AttendanceFilter();
        primaryFilter.setTab(0);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitle("Attendance");

        btnSubmit = (Button) findViewById(R.id.btn_submit);

        checkUserExist();
        initializeUser();
        initializeTabs();
        pagerAdapter.notifyDataSetChanged();
        //FirebaseUtils.generateTables(new TableFilters());
    }

    public void initializeUser() {
        if (mAuth.getCurrentUser() != null) {
            primaryFilter.setRotationId("A");
            primaryFilter.setBuilding("ALL");

            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d(TAG, dataSnapshot.getValue() + "");

                    if (dataSnapshot.getKey().equals("image")) {
                        profileUrl = dataSnapshot.getValue().toString();
                    } else if (dataSnapshot.getKey().equals("name")) {
                        name = dataSnapshot.getValue().toString();
                        initializeDrawer();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Log.d(TAG, "onChildChanged");
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved");
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    Log.d(TAG, "onChildMoved");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled");
                }
            });
        }
    }

    public void initializeTabs() {
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), tabList, TAB_NUMBERS, primaryFilter);
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0);
        tabSlider = (SlidingTabLayout) findViewById(R.id.tabs);
        tabSlider.setDistributeEvenly(true);
        tabSlider.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            public int getIndicatorColor(int position) {
                return ContextCompat.getColor(getApplicationContext(), R.color.cardview_light_background);
            }
        });
        tabSlider.setViewPager(viewPager);

        tabSlider.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                primaryFilter.setTab(position);
                pagerAdapter.notifyDataSetChanged();

                if (position == PENDING_TAB)
                    btnSubmit.setVisibility(View.GONE);
                else if (position == DONE_TAB)
                    btnSubmit.setVisibility(View.VISIBLE);

                Log.d("PRIMARY FILTER", primaryFilter.getFilterString());

                /*List<Fragment>  fragments = getSupportFragmentManager().getFragments();
                if(fragments != null){
                    for(int i = 0 ; i < fragments.size(); i++) {
                        Log.d("MainActivity.FragID", fragments.get(i).getId() + "");
                    }
                    Log.d("MainActivity.FragsCount", fragments.size() + "");
                }*/
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void initializeDrawer() {
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        nView = mNavigationView.getHeaderView(0);
        civProfilePic = (CircleImageView) nView.findViewById(R.id.civ_profile_pic);
        tvName = (TextView) nView.findViewById(R.id.tv_name);
        tvEmail = (TextView) nView.findViewById(R.id.tv_email);

        Picasso.with(MainActivity.this).load(profileUrl).into(civProfilePic);
        tvName.setText(name);
        tvEmail.setText(mAuth.getCurrentUser().getEmail());

        drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);        // Drawer object Assigned to the view
        mDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // code here will execute once the drawer is opened( As I dont want anything happened whe drawer is
                // open I am not going to put anything here)
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                //TODO Code here will execute once drawer is closed
            }
        };

        drawer.addDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        mDrawerToggle.syncState();               // Finally we set the drawer toggle sync State

        emptyView = (TextView) findViewById(R.id.empty_view);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);

                switch (menuItem.getItemId()) {
                    case R.id.nav_logout:
                        mAuth.signOut();
                        break;
                    case R.id.nav_allbuildings:
                        primaryFilter.setBuilding("ALL");
                        break;
                    case R.id.nav_gokongwei:
                        primaryFilter.setBuilding("GOKONGWEI");
                        break;
                    case R.id.nav_andrew:

                        break;
                    case R.id.nav_lasallehall:

                        break;
                    case R.id.nav_miguel:

                        break;
                    case R.id.nav_razon:

                        break;
                    case R.id.nav_saintjoseph:

                        break;
                    case R.id.nav_yuchengco:

                        break;
                    case R.id.nav_velasco:

                        break;
                    case R.id.nav_help:
                        Intent help = new Intent();
                        help.setClass(getBaseContext(), HelpActivity.class);
                        startActivity(help);
                        break;
                    case R.id.report_uc:

                        break;
                }
                pagerAdapter.notifyDataSetChanged();

                ((DrawerLayout) findViewById(R.id.DrawerLayout)).closeDrawers();
                return true;
            }
        });
    }

    private void checkUserExist() {
        mProgress.setMessage("Checking if user profile exists...");
        mProgress.show();

        if (mAuth.getCurrentUser() != null) {
            final String userId = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (!dataSnapshot.hasChild(userId)) {
                        Intent setupIntent = new Intent(MainActivity.this, SetupAccountActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mProgress.dismiss();
                        finish();
                        startActivity(setupIntent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        mProgress.dismiss();
    }

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
            Log.d("ViewPagerAdapterFilter", filter.getTab() + "");
            Log.i("tagg", "ViewPagerAdapter.getItem()   -- position ZERO called");
            al = AttendanceFragment.newInstance(filter);
            al.setOnSetAttendanceListener(new AttendanceFragment.OnSetAttendanceListener() {
                @Override
                public void onSetAttendance(Attendance model) {
                    // place code here from fragment
                    Intent detailActivity = new Intent(getBaseContext(), DetailActivity.class);
                    detailActivity.putExtra(Attendance.ATTENDANCE, model);

                    startActivityForResult(detailActivity, ATTENDANCE_DETAIL_CODE);
                }
            });
            return al;
        }

        public AttendanceFragment getFragment() {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == ATTENDANCE_DETAIL_CODE) {
            Log.d("AttendanceFragment", "SAVE");

            Attendance done = (Attendance) data.getSerializableExtra(DetailActivity.DONE_ATTENDANCE);

            FirebaseUtils.updateAttendance(done);
        }
    }

}
