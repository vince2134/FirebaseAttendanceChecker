package com.mobapde.vince.mobapde;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NotificationCompat;
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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.mobapde.vince.mobapde.MainActivity.AlarmReceiver.NOTIFY_ANNOUNCEMENT;

public class MainActivity extends AppCompatActivity {
    public static final int TAB_NUMBERS = 2;
    public static final int PENDING_TAB = 0;
    public static final int DONE_TAB = 1;
    public static int PENDING_ALARMRECEIVER = 1;
    public static int PENDING_NEXT = 2;
    final static String GROUP_KEY_NOTIFS = "group_key_notifs";

    private static int ATTENDANCE_DETAIL_CODE = 0;
    private static final String TAG = "MainActivity";

    public static Boolean submitted = false;
    private Toolbar toolbar;
    private ViewPager viewPager;
    private static ViewPagerAdapter pagerAdapter;
    private SlidingTabLayout tabSlider;
    private CharSequence tabList[] = {"pending", "done"};
    private String profileUrl;
    private static String startTime;
    private static String endTime;
    private String name;
    private Boolean initialized = false;

    String userId;

    NavigationView mNavigationView;
    DrawerLayout drawer;                                  // Declaring DrawerLayout
    ActionBarDrawerToggle mDrawerToggle;                  // Declaring Action Bar Drawer Toggle

    TextView emptyView, tvName, tvEmail;
    Button btnSubmit;
    CircleImageView civProfilePic;
    View nView;

    static AttendanceFilter primaryFilter;

    private FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    DatabaseReference mDatabaseUsers;
    static DatabaseReference mDatabaseTimeSlots;

    ProgressDialog mProgress;

    public static ArrayList<TimeSlot> timeSlots = new ArrayList<>();
    public static ArrayList<String> notifications = new ArrayList<>();
    public static ArrayList<String> adminNotifs = new ArrayList<>();
    public static ArrayList<Object> filterCounts = new ArrayList<>();
    static ArrayList<String> buildings = new ArrayList<>();
    ArrayList<Integer> buildingIDs = new ArrayList<>();

    int count = 0;

    public static Context mainContext;
    public static Boolean setupAccount = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainContext = MainActivity.this;

        initializeBuildingIds();

        mProgress = new ProgressDialog(MainActivity.this);
        mProgress.setMessage("Loading data...");
        mProgress.show();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    finish();
                    startActivity(loginIntent);
                } else ;
                //FirebaseUtils.initialize();
            }
        };

        mAuth.addAuthStateListener(mAuthListener);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);

        Calendar calendar = Calendar.getInstance();

        //replace the current time by the time provided in the parameter
        calendar.set(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0);
        calendar.set(Calendar.MILLISECOND, 0);

        primaryFilter = new AttendanceFilter();
        primaryFilter.setTab(0);
        primaryFilter.setStartMillis(calendar.getTimeInMillis());

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitle("Attendance");
        toolbar.setTitleTextColor(Color.BLACK);
        toolbar.setBackgroundColor(Color.WHITE);

        btnSubmit = (Button) findViewById(R.id.btn_submit);
        btnSubmit.setBackgroundColor(Color.WHITE);
        btnSubmit.setElevation(100);
        btnSubmit.setTextColor(Color.rgb(8, 120, 48));

        checkUserExist();
        //initializeUser();
        //initializeTimeSlots();
        //udpateFilterCounts();

        mProgress.dismiss();
        //mAuth.signOut();
        //FirebaseUtils.generateTables(new TableFilters());
    }

    public void setCurrentDate(){
        Calendar calendar = Calendar.getInstance();

        //replace the current time by the time provided in the parameter
        calendar.set(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0);
        calendar.set(Calendar.MILLISECOND, 0);

        primaryFilter.setStartMillis(calendar.getTimeInMillis());
    }

    public static Boolean timeSlotExists(TimeSlot timeSlot) {

        if (!timeSlots.isEmpty()) {
            for (int i = 0; i < timeSlots.size(); i++) {
                if (timeSlot.getStartMillis() == timeSlots.get(i).getStartMillis())
                    return true;
            }
        }

        return false;
    }

    public void initializeTimeSlots() {
        timeSlots.clear();
        buildings.clear();
        //primaryFilter.setBuilding("ALL");

        //SETS TIME FILTER TO CURRENT DATE WITHOUT TIME TO RETRIEVE UNIQUE TIME SLOTS
        setCurrentDate();

        mDatabaseTimeSlots = FirebaseDatabase.getInstance().getReference().child(primaryFilter.getFilterString());
        mDatabaseTimeSlots.orderByChild("startTime");

        mDatabaseTimeSlots.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                count++;
                Log.d("TIMESLOT_CHILD_ADDED", dataSnapshot.getValue().toString());

                TimeSlot curTimeSlot = new TimeSlot();

                for (DataSnapshot attendance : dataSnapshot.getChildren()) {
                    if (attendance.getKey().equals("endTime")) {
                        endTime = attendance.getValue().toString();
                        curTimeSlot.setEndMillis(Long.parseLong(endTime));
                    } else if (attendance.getKey().equals("startTime")) {
                        startTime = attendance.getValue().toString();
                        curTimeSlot.setStartMillis(Long.parseLong(startTime));
                        Log.d("START_TIME", attendance.getValue().toString());
                    } else if (attendance.getKey().equals("building")) {
                        if (!buildings.contains(attendance.getValue().toString())) {
                            buildings.add(attendance.getValue().toString());
                        }
                    }
                }

                if (!timeSlotExists(curTimeSlot)) {
                    timeSlots.add(curTimeSlot);
                    Log.d("ADDED", curTimeSlot.getStartMillis() + "");
                }

                Log.d("COUNT", count + "");
                Log.d("COUNTS", dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseTimeSlots.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Log.d("TIMESLOT_FINISH", dataSnapshot.getChildrenCount() + "");
                    //initializeDrawer();

                    Collections.sort(timeSlots, new Comparator<TimeSlot>() {
                        @Override
                        public int compare(TimeSlot o1, TimeSlot o2) {
                            if(o1.getStartMillis() > o2.getStartMillis())
                                return 1;
                            else if(o1.getStartMillis() < o2.getStartMillis())
                                return -1;
                            else
                                return 0;
                        }
                    });

                    for(TimeSlot t: timeSlots)
                        Log.d("TIMESLOTSSSSS", t.getStartMillis() + "");

                    updateFilterCounts();
                    primaryFilter.setStartMillis(-1);
                    initializeNotifications();
                    pagerAdapter.notifyDataSetChanged();
                } catch (Exception e) {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void initializeNotifications() {
        Log.d("TIMESLOT_INIT", timeSlots.size() + "");
        notifications.clear();

        if(!adminNotifs.isEmpty()) {
            notifications.add(adminNotifs.get(0));
            adminNotifs.clear();
        }

        for (TimeSlot t : timeSlots) {
            Log.d("TIMESLOT_ARRAY", t.getStartMillis() + "");
        }

        if (!timeSlots.isEmpty()) {
            Integer withinSlot = timeSlots.get(0).withinTimeSlot();

            if (withinSlot == null)
                Toast.makeText(mainContext, "TimeSlot is null", Toast.LENGTH_SHORT).show();
            else {
                // Remove exceeded time slots
                if (withinSlot == 1) {
                    while (!timeSlots.isEmpty() && timeSlots.get(0).withinTimeSlot() == 1)
                        timeSlots.remove(0);

                    Toast.makeText(mainContext, "You missed several classes.", Toast.LENGTH_LONG).show();
                    notifications.add("You missed several classes.");
                }

                if (timeSlots.size() > 0) {
                    withinSlot = timeSlots.get(0).withinTimeSlot();

                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(timeSlots.get(0).getStartMillis());
                    String curTimeSlotFormat = "";
                    String nextClass = "";
                    String timeFormat = "hh:mm a";
                    SimpleDateFormat formatter = new SimpleDateFormat(timeFormat);
                    curTimeSlotFormat = formatter.format(c.getTime());

                    //Next time slot is too early
                    if (withinSlot == -1) {
                        Toast.makeText(mainContext, "The next classes will start at " + curTimeSlotFormat + ".", Toast.LENGTH_LONG).show();
                        notifications.add("The next classes will start at " + curTimeSlotFormat + ".");
                    }

                    AlarmManager alarmManager = (AlarmManager) mainContext.getSystemService(Service.ALARM_SERVICE);

                    if (timeSlots.size() > 1) {
                        c.setTimeInMillis(timeSlots.get(1).getStartMillis());
                        nextClass = "The next classes will start at " + formatter.format(c.getTime()) + ".";
                        //notifications.add(curTimeSlotFormat + " classes have been filtered. " + nextClass);
                    } else {
                        nextClass = "There are no classes left for today.";
                        //notifications.add("There are no classes left for today.");
                    }

                    Intent intentAlarm = new Intent(mainContext, AlarmReceiver.class);
                    intentAlarm.putExtra("CUR_TIME_SLOT", curTimeSlotFormat);
                    intentAlarm.putExtra("NEXT_CLASS", nextClass);
                    intentAlarm.putExtra("CUR_TIME_LONG", timeSlots.get(0).getStartMillis());

                    PendingIntent pendingAlarm = PendingIntent.getBroadcast(mainContext, PENDING_ALARMRECEIVER, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
                    //alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + (Integer.parseInt(delayET.getText().toString())*1000), pendingAlarm); //5 secs delay
                    alarmManager.set(AlarmManager.RTC_WAKEUP, timeSlots.get(0).getStartMillis(), pendingAlarm);

                    Log.d("CUR_TIME_SLOT_MAIN", curTimeSlotFormat);

                    Log.d("ALARM_SET_MAIN", "SET");

                    timeSlots.remove(0);
                } else {
                    //Exceeded all time slots
                    primaryFilter.setStartMillis(Calendar.getInstance().getTimeInMillis());
                    pagerAdapter.notifyDataSetChanged();
                    Toast.makeText(mainContext, "Exceeded all time slots.", Toast.LENGTH_LONG).show();
                    notifications.add("Exceeded all time slots.");
                    //notify("Exceeded all time slots.");
                    startInitializer();
                }
            }
        } else {
            Toast.makeText(mainContext, "No attendance retrieved.", Toast.LENGTH_LONG).show();
            notifications.add("No attendance retrieved.");
            startInitializer();
            pagerAdapter.notifyDataSetChanged();
            //initializeTimeSlots();
            //initializeNotifications();
        }

        notifyUser("");
        notifications.clear();
    }

    public void initializeUser() {
        if (mAuth.getCurrentUser() != null) {
            //primaryFilter.setRotationId(mAuth.getCurrentUser().);
            primaryFilter.setBuilding("ALL");

            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d(TAG, dataSnapshot.getValue() + "");

                    if (dataSnapshot.getKey().equals("image")) {
                        profileUrl = dataSnapshot.getValue().toString();
                    } else if (dataSnapshot.getKey().equals("name")) {
                        name = dataSnapshot.getValue().toString();
                    } else if (dataSnapshot.getKey().equals("rotationId")) {
                        primaryFilter.setRotationId(dataSnapshot.getValue().toString());

                        if(primaryFilter.getRotationId().equals("_")) {
                            notifyUser("There are no assigned classes for you yet. Please contact the administrator for help.");
                            initializeDrawer();
                            initialized = true;
                        } else {
                            initializeTimeSlots();
                            Log.d("HASROTATION", primaryFilter.getFilterString());
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Log.d(TAG, "onChildChanged");

                    if (dataSnapshot.getKey().equals("rotationId")) {
                        primaryFilter.setRotationId(dataSnapshot.getValue().toString());

                        if(primaryFilter.getRotationId().equals("_")) {
                            //Log.d("PRIMARYFILTER", primaryFilter.getFilterString());
                            notifyUser("There are no assigned classes for you yet. Please contact the administrator for help.");
                            initializeDrawer();
                        } else {
                            //notifyUser("The administrator assigned new classes for you.");
                            adminNotifs.add("The administrator assigned new classes.");
                            initializeTimeSlots();
                            Log.d("HASROTATION", primaryFilter.getFilterString());
                        }
                    }
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
        tabSlider.setBackgroundColor(Color.WHITE);
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
                Log.d("TABSWITCH", primaryFilter.getFilterString());
                pagerAdapter.notifyDataSetChanged();
                updateFilterCounts();

                if (position == PENDING_TAB)
                    btnSubmit.setVisibility(View.GONE);
                else if (position == DONE_TAB) {
                    btnSubmit.setVisibility(View.VISIBLE);
                }

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

    public void updateDrawerCounter() {

    }

    private void setMenuCounter(@IdRes int itemId, int count) {
        Log.d("Count", count + "");

        TextView view = (TextView) mNavigationView.getMenu().findItem(itemId).getActionView();
        view.setText(count >= 0 ? String.valueOf(count) : null);
    }

    public void initializeBuildingIds() {
        buildingIDs.add(R.id.nav_allbuildings);
        buildingIDs.add(R.id.nav_lasallehall);
        buildingIDs.add(R.id.nav_yuchengco);
        buildingIDs.add(R.id.nav_saintjoseph);
        buildingIDs.add(R.id.nav_velasco);
        buildingIDs.add(R.id.nav_miguel);
        buildingIDs.add(R.id.nav_gokongwei);
        buildingIDs.add(R.id.nav_andrew);
        buildingIDs.add(R.id.nav_razon);
    }

    public void updateFilterCounts() {
        filterCounts.clear();
        filterCounts.add(new ArrayList<Integer>());
        filterCounts.add(new ArrayList<String>());

        final DatabaseReference filterCountsReference = FirebaseDatabase.getInstance().getReference().child("filterCounts");

        filterCountsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("FILTERCOUNTS", dataSnapshot.getKey().toString());

                ((ArrayList<String>) filterCounts.get(0)).add(dataSnapshot.getKey().toString());

                for (DataSnapshot filter : dataSnapshot.getChildren()) {
                    if (filter.getKey().equals("count"))
                        Log.d("FILTERCOUNTS", filter.getValue().toString());
                    ((ArrayList<Integer>) filterCounts.get(1)).add(Integer.parseInt(filter.getValue().toString()));
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        filterCountsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    initializeDrawer();
                } catch (Exception e) {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void initializeDrawer() {
        String buildingBefore = primaryFilter.getBuilding();
        long startTimeBefore = primaryFilter.getStartMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.set(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0);
        calendar.set(Calendar.MILLISECOND, 0);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        /*if(!initialized) {
            Log.d("INITIALIZE", "YE");
            mNavigationView.setCheckedItem(R.id.nav_allbuildings);
            ((TextView) mNavigationView.getMenu().getItem(0).getSubMenu().getItem(0).getActionView()).setTextColor(Color.WHITE);
        }*/

        for (int i = 1; i < mNavigationView.getMenu().getItem(0).getSubMenu().size(); i++) {
            mNavigationView.getMenu().getItem(0).getSubMenu().getItem(i).setVisible(false);
            Log.d(TAG + "Buildings", mNavigationView.getMenu().getItem(0).getSubMenu().getItem(i).getTitle().toString().toUpperCase().replaceAll("\\s+", "") + "");
        }

        //CALL ONLY WHEN THERE IS ASSIGNED ROTOTATION ID
        if(!primaryFilter.getRotationId().equals("_")) {
            for (String filter : (ArrayList<String>) filterCounts.get(0))
                Log.d("FILTERARRAY", filter);
            for (Integer filter2 : (ArrayList<Integer>) filterCounts.get(1))
                Log.d("FILTERARRAY", filter2 + "");

            ArrayList<String> filterStrings = (ArrayList<String>) filterCounts.get(0);
            primaryFilter.setStartMillis(calendar.getTimeInMillis());
            Log.d("PRIMARYFILTERSTRINGB", primaryFilter.getFilterString());
            int indexOfFilterString = filterStrings.indexOf(primaryFilter.getFilterString());
            ArrayList<Integer> filterCounts2 = (ArrayList<Integer>) filterCounts.get(1);
            try {
                int curCount = filterCounts2.get(indexOfFilterString);

                Log.d("INDEX", indexOfFilterString + "");

                setMenuCounter(buildingIDs.get(0), Math.abs(curCount));
            } catch (Exception ex) {

            }

            for (String building : buildings) {
                for (int i = 1; i < mNavigationView.getMenu().getItem(0).getSubMenu().size(); i++) {
                    if (building.equals(mNavigationView.getMenu().getItem(0).getSubMenu().getItem(i).getTitle().toString().toUpperCase().replaceAll("\\s+", "") + "")) {
                        mNavigationView.getMenu().getItem(0).getSubMenu().getItem(i).setVisible(true);
                        primaryFilter.setBuilding(mNavigationView.getMenu().getItem(0).getSubMenu().getItem(i).getTitle().toString().toUpperCase().replaceAll("\\s+", "") + "");
                        primaryFilter.setStartMillis(calendar.getTimeInMillis());

                        filterStrings = (ArrayList<String>) filterCounts.get(0);
                        Log.d("PRIMARYFILTERSTRING", primaryFilter.getFilterString());
                        indexOfFilterString = filterStrings.indexOf(primaryFilter.getFilterString());
                        filterCounts2 = (ArrayList<Integer>) filterCounts.get(1);
                        try {
                            int curCount = filterCounts2.get(indexOfFilterString);

                            Log.d("INDEX", indexOfFilterString + "");

                            setMenuCounter(buildingIDs.get(i), Math.abs(curCount));
                        } catch (Exception ex) {

                        }
                    }
                }
            }
        }

        primaryFilter.setBuilding(buildingBefore);
        primaryFilter.setStartMillis(startTimeBefore);

        nView = mNavigationView.getHeaderView(0);
        civProfilePic = (CircleImageView) nView.findViewById(R.id.civ_profile_pic);


        civProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent updateAccount = new Intent(MainActivity.this, SetupAccountActivity.class);
                updateAccount.putExtra("EDIT_NAME", name);
                updateAccount.putExtra("EDIT_PROFILE_URL", profileUrl);

                startActivity(updateAccount);
            }
        });

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

                for(int i = 0; i < mNavigationView.getMenu().getItem(0).getSubMenu().size(); i++){
                    ((TextView)mNavigationView.getMenu().getItem(0).getSubMenu().getItem(i).getActionView()).setTextColor(Color.rgb(109, 109, 109));
                }

                switch (menuItem.getItemId()) {
                    case R.id.nav_logout:
                        mAuth.signOut();
                        break;
                    case R.id.nav_allbuildings:
                        ((TextView)menuItem.getActionView()).setTextColor(Color.WHITE);
                        primaryFilter.setBuilding("ALL");
                        break;
                    case R.id.nav_gokongwei:
                        ((TextView)menuItem.getActionView()).setTextColor(Color.WHITE);
                        primaryFilter.setBuilding("GOKONGWEI");
                        break;
                    case R.id.nav_andrew:
                        ((TextView)menuItem.getActionView()).setTextColor(Color.WHITE);
                        primaryFilter.setBuilding("ANDREW");
                        break;
                    case R.id.nav_lasallehall:
                        ((TextView)menuItem.getActionView()).setTextColor(Color.WHITE);
                        primaryFilter.setBuilding("LASALLEHALL");
                        break;
                    case R.id.nav_miguel:
                        ((TextView)menuItem.getActionView()).setTextColor(Color.WHITE);
                        primaryFilter.setBuilding("MIGUEL");
                        break;
                    case R.id.nav_razon:
                        ((TextView)menuItem.getActionView()).setTextColor(Color.WHITE);
                        primaryFilter.setBuilding("RAZON");
                        break;
                    case R.id.nav_saintjoseph:
                        ((TextView)menuItem.getActionView()).setTextColor(Color.WHITE);
                        primaryFilter.setBuilding("SAINTJOSEPH");
                        break;
                    case R.id.nav_yuchengco:
                        ((TextView)menuItem.getActionView()).setTextColor(Color.WHITE);
                        primaryFilter.setBuilding("YUCHENGCO");
                        break;
                    case R.id.nav_velasco:
                        ((TextView)menuItem.getActionView()).setTextColor(Color.WHITE);
                        primaryFilter.setBuilding("VELASCO");
                        break;
                    case R.id.nav_help:
                        Intent help = new Intent();
                        help.setClass(getBaseContext(), HelpActivity.class);
                        startActivity(help);
                        break;
                }

                ((DrawerLayout) findViewById(R.id.DrawerLayout)).closeDrawers();

                Log.d("NAVDRAWERSELECT", primaryFilter.getFilterString());

                if(menuItem.getItemId() != R.id.nav_help)
                    pagerAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    private void checkUserExist() {
        mProgress.setMessage("Checking if user profile exists...");
        mProgress.show();

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
            DatabaseReference curUser = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

            curUser.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d("USER", dataSnapshot.getKey());
                    Log.d("USER", dataSnapshot.getValue() + "");

                    if (dataSnapshot.getKey().equals("image")) {
                        if (dataSnapshot.getValue().toString().equals("default")) {
                            Intent setupIntent = new Intent(MainActivity.this, SetupAccountActivity.class);
                            setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            mProgress.dismiss();
                            finish();
                            startActivity(setupIntent);
                        } else {
                            try {
                                Log.d("SETUP", "SETUP");
                                initializeUser();
                                initializeTabs();
                            } catch (Exception e) {

                            }
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

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

            Attendance attendance = (Attendance) data.getSerializableExtra(DetailActivity.DONE_ATTENDANCE);

            if (attendance.getStatus().equals("PENDING")) {
                attendance.setStatus("DONE");
                FirebaseUtils.updateAttendance(attendance);
            } else
                FirebaseUtils.updateAttendanceByIdOnly(attendance);

            pagerAdapter.notifyDataSetChanged();
            updateFilterCounts();
        }
    }

    public static void filterTime(long timeSlot) {
        primaryFilter.setStartMillis(timeSlot);
        Log.d("CUR_FILTER", primaryFilter.getFilterString());
        pagerAdapter.notifyDataSetChanged();
    }

    public static void startInitializer() {
        AlarmManager alarmManager = (AlarmManager) mainContext.getSystemService(Service.ALARM_SERVICE);
        Intent intentAlarm = new Intent(mainContext, FilterReceiver.class);

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        PendingIntent pendingAlarm = PendingIntent.getBroadcast(mainContext, PENDING_ALARMRECEIVER, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        //alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + (Integer.parseInt(delayET.getText().toString())*1000), pendingAlarm); //5 secs delay
        alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingAlarm);
    }

    public static void reinitializeTimeSlots() {
        timeSlots.clear();

        Calendar calendar = Calendar.getInstance();

        //replace the current time by the time provided in the parameter
        calendar.set(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0);
        calendar.set(Calendar.MILLISECOND, 0);

        primaryFilter.setStartMillis(calendar.getTimeInMillis());
        mDatabaseTimeSlots = FirebaseDatabase.getInstance().getReference().child(primaryFilter.getFilterString());

        mDatabaseTimeSlots.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //count++;
                Log.d("TIMESLOT_CHILD_ADDED", dataSnapshot.getValue().toString());

                TimeSlot curTimeSlot = new TimeSlot();

                for (DataSnapshot attendance : dataSnapshot.getChildren()) {
                    if (attendance.getKey().equals("endTime")) {
                        endTime = attendance.getValue().toString();
                        curTimeSlot.setEndMillis(Long.parseLong(endTime));
                    } else if (attendance.getKey().equals("startTime")) {
                        startTime = attendance.getValue().toString();
                        curTimeSlot.setStartMillis(Long.parseLong(startTime));
                        Log.d("START_TIME", attendance.getValue().toString());
                    } else if (attendance.getKey().equals("building")) {
                        if (!buildings.contains(attendance.getValue().toString())) {
                            buildings.add(attendance.getValue().toString());
                        }
                    }
                }

                if (!timeSlotExists(curTimeSlot)) {
                    timeSlots.add(curTimeSlot);
                    Log.d("ADDED", curTimeSlot.getStartMillis() + "");
                }

                //Log.d("COUNT", count + "");
                Log.d("COUNTS", dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseTimeSlots.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("TIMESLOT_FINISH", dataSnapshot.getChildrenCount() + "");

                Collections.sort(timeSlots, new Comparator<TimeSlot>() {
                    @Override
                    public int compare(TimeSlot o1, TimeSlot o2) {
                        if(o1.getStartMillis() > o2.getStartMillis())
                            return 1;
                        else if(o1.getStartMillis() < o2.getStartMillis())
                            return -1;
                        else
                            return 0;
                    }
                });

                initializeNotifications();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static class FilterReceiver extends BroadcastReceiver {

        public static final int NOTIFY_ANNOUNCEMENT = 0;
        public static final int PENDING_NEXT = 0;

        public FilterReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            reinitializeTimeSlots();
        }
    }

    public static class AlarmReceiver extends BroadcastReceiver {

        public static final int NOTIFY_ANNOUNCEMENT = 0;
        public static final int PENDING_NEXT = 0;

        public AlarmReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationManager notifManager = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);

            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.check2);

            Intent intentNext = new Intent(mainContext, MainActivity.class);
            intentNext.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intentNext.setAction(Intent.ACTION_MAIN);
            intentNext.addCategory(Intent.CATEGORY_LAUNCHER);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, PENDING_NEXT, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);

            notifyUser(intent.getStringExtra("CUR_TIME_SLOT") + " classes have been filtered. " + intent.getStringExtra("NEXT_CLASS"));

            Log.d("CUR_TIME_LONG", intent.getLongExtra("CUR_TIME_LONG", -1) + "");

            MainActivity.filterTime(intent.getLongExtra("CUR_TIME_LONG", -1));

            Log.d("ON_RECEIVE", "RECEIVED");

            if (!timeSlots.isEmpty())
                setNewAlarm(context);
            else
                startInitializer();
        }

        public void setNewAlarm(Context context) {
            // Initialize current time slot in calendar
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(timeSlots.get(0).getStartMillis());

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
            String curTimeSlotFormat = "";
            String nextClass = "";
            String timeFormat = "hh:mm a";
            SimpleDateFormat formatter = new SimpleDateFormat(timeFormat);
            curTimeSlotFormat = formatter.format(c.getTime());

            if (timeSlots.size() > 1) {
                c.setTimeInMillis(timeSlots.get(1).getStartMillis());
                nextClass = "The next classes will start at " + formatter.format(c.getTime()) + ".";
                //filterTime(-1);
            } else
                nextClass = "There are no classes left for today.";

            Intent intentAlarm = new Intent(context, AlarmReceiver.class);
            intentAlarm.putExtra("CUR_TIME_SLOT", curTimeSlotFormat);
            intentAlarm.putExtra("NEXT_CLASS", nextClass);
            intentAlarm.putExtra("CUR_TIME_LONG", timeSlots.get(0).getStartMillis());

            PendingIntent pendingAlarm = PendingIntent.getBroadcast(context, PENDING_ALARMRECEIVER, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
            //alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + (Integer.parseInt(delayET.getText().toString())*1000), pendingAlarm); //5 secs delay
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeSlots.get(0).getStartMillis(), pendingAlarm);

            Toast.makeText(context, "Alarm set for " + curTimeSlotFormat, Toast.LENGTH_SHORT).show();

            timeSlots.remove(0);
        }

    }

    public static void notifyUser(String text) {
        NotificationManager notifManager = (NotificationManager) mainContext.getSystemService(Service.NOTIFICATION_SERVICE);

        Bitmap bitmap = BitmapFactory.decodeResource(mainContext.getResources(), R.drawable.check2);

        Intent intentNext = new Intent(mainContext, MainActivity.class);
        intentNext.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intentNext.setAction(Intent.ACTION_MAIN);
        intentNext.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent = PendingIntent.getActivity(mainContext, PENDING_NEXT, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder notifBuilder = new Notification.Builder(mainContext)
                .setContentTitle("Attendance Checker")
                .setContentText(text)
                .setSmallIcon(R.drawable.small)
                .setTicker(text)
                .setAutoCancel(true)
                .setStyle(new Notification.BigPictureStyle().bigPicture(bitmap))
                .setStyle(new Notification.BigTextStyle().bigText(text))
                .setLargeIcon(bitmap)
                .setContentIntent(pendingIntent);

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

        for (String n : notifications) {
            style.addLine(n);
        }

        style.setSummaryText("You have new notifications.")
                .setBigContentTitle("Attendance Checker");

        if (!notifications.isEmpty()) {
            Notification notif = new NotificationCompat.Builder(mainContext)
                    .setContentTitle("Attendance Checker")
                    .setContentText(notifications.get(notifications.size() - 1))
                    .setTicker(notifications.get(notifications.size() - 1))
                    .setSmallIcon(R.drawable.small)
                    .setLargeIcon(bitmap)
                    .setNumber(notifications.size())
                    .setGroup(GROUP_KEY_NOTIFS)
                    .setStyle(style)
                    .build();

            if (notifications.size() > 1)
                notifManager.notify(NOTIFY_ANNOUNCEMENT, notif);
        }

        if (!text.isEmpty() && notifications.isEmpty())
            notifManager.notify(NOTIFY_ANNOUNCEMENT, notifBuilder.build());
        if (text.isEmpty() && notifications.size() == 1) {
            notifBuilder.setContentText(notifications.get(0))
                    .setTicker(notifications.get(0))
                    .setStyle(new Notification.BigPictureStyle().bigPicture(bitmap))
                    .setStyle(new Notification.BigTextStyle().bigText(notifications.get(0)));
            notifManager.notify(NOTIFY_ANNOUNCEMENT, notifBuilder.build());
        }
    }
}
