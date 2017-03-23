package com.mobapde.vince.mobapde;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public static final int TAB_NUMBERS = 2;
    public static final int PENDING_TAB = 0;
    public static final int DONE_TAB = 1;

    public static Boolean submitted = true;
    private Toolbar toolbar;
    private ViewPager viewPager;
    private ViewPagerAdapter pagerAdapter;
    private SlidingTabLayout tabSlider;
    private CharSequence tabList[] = {"pending", "done"};

    NavigationView mNavigationView;
    DrawerLayout drawer;                                  // Declaring DrawerLayout
    ActionBarDrawerToggle mDrawerToggle;                  // Declaring Action Bar Drawer Toggle

    TextView emptyView;
    Button btnSubmit;

    AttendanceFilter primaryFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        primaryFilter = new AttendanceFilter();
        primaryFilter.setTab(0);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitle("Attendance");

        btnSubmit = (Button) findViewById(R.id.btn_submit);

        initializeTabs();
        initializeDrawer();
    }

    public void initializeTabs(){
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

                if(position == PENDING_TAB)
                    btnSubmit.setVisibility(View.GONE);
                else if(position == DONE_TAB)
                    btnSubmit.setVisibility(View.VISIBLE);

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

    public void initializeDrawer(){
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
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
    }
}
