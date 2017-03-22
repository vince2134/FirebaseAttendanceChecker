package com.mobapde.vince.mobapde;

import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public static final int TAB_NUMBERS = 2;

    public static Boolean submitted = true;
    private Toolbar toolbar;
    private ViewPager viewPager;
    private ViewPagerAdapter pagerAdapter;
    private SlidingTabLayout tabSlider;
    private CharSequence tabList[] = {"pending", "done"};

    NavigationView mNavigationView;
    DrawerLayout Drawer;                                  // Declaring DrawerLayout
    ActionBarDrawerToggle mDrawerToggle;                  // Declaring Action Bar Drawer Toggle

    TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitle("Attendance");
        setSupportActionBar(toolbar);
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), tabList, TAB_NUMBERS, new AttendanceFilter());
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

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        Drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);        // Drawer object Assigned to the view
        mDrawerToggle = new ActionBarDrawerToggle(this, Drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

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

        Drawer.addDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        mDrawerToggle.syncState();               // Finally we set the drawer toggle sync State

        emptyView = (TextView) findViewById(R.id.empty_view);

        pagerAdapter.notifyDataSetChanged();
    }
}
