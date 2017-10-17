package com.timkonieczny.rss;

import android.animation.ObjectAnimator;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    protected static DbList<Article> articles = null;
    protected static SourcesList sources = null;

    private DrawerArrowDrawable drawerArrow;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private View upButton;
    private NavigationView navigationView;

    protected static boolean goToSettings = false;
    protected static boolean isFragmentSelected = false;

    private OverviewFragment overviewFragment = null;
    private SourcesFragment sourcesFragment = null;
    private SettingsFragment settingsFragment = null;
    private ArticleFragment articleFragment = null;

    protected static int viewWidth;

    boolean isActivityResumed = true;
    boolean isDrawerToggleArrow = false;
    boolean isAnyFragmentAttached;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // onCreate() is called when recreate() is being called.
        // No point in running this code if everything needs to be rebuilt anyway.
        if(setTheme()) {
            setContentView(R.layout.activity_main);

            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);
            viewWidth = size.x;

            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            drawer = findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();


            drawerArrow = new DrawerArrowDrawable(this);
            drawerArrow.setColor(Color.WHITE);

            toolbar.setNavigationIcon(drawerArrow);

            if(savedInstanceState!=null) {
                isDrawerToggleArrow = savedInstanceState.getBoolean("isDrawerToggleArrow");
                if(isDrawerToggleArrow) {
                    drawerArrow.setProgress(1f);
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onBackPressed();
                        }
                    });
                }
            }

            ArrayList<View> views = new ArrayList<>();
            findViewById(R.id.toolbar).findViewsWithText(views, getString(R.string.navigation_drawer_open), View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
            if(views.size() != 0) upButton = views.get(0); // TODO: check other strings too: close drawer, navigate up
            upButton.setTransitionName("up_button");

            navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    FragmentManager fragmentManager = getFragmentManager();
                    int backStackCount = fragmentManager.getBackStackEntryCount();
                    if(backStackCount>0) {
                        int menuId = Integer.parseInt(fragmentManager.getBackStackEntryAt(backStackCount - 1).getName());
                        MenuItem menuItem = navigationView.getMenu().getItem(menuId);
                        if(!menuItem.isChecked()) menuItem.setChecked(true);
                    }else{  // menu entry for OverviewFragment is not added to BackStack
                        MenuItem menuItem = navigationView.getMenu().getItem(0);
                        if(!menuItem.isChecked()) menuItem.setChecked(true);
                    }
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isDrawerToggleArrow", isDrawerToggleArrow);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if ((!isFragmentSelected||!isAnyFragmentAttached)&&navigationView!=null) {
            if (goToSettings) {
                onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_preferences));
                navigationView.getMenu().findItem(R.id.nav_preferences).setChecked(true);
                goToSettings = false;
            } else {
                onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_news));
                navigationView.getMenu().findItem(R.id.nav_news).setChecked(true);
            }
            isFragmentSelected = true;
        }
        rescheduleBackgroundUpdate();

        isActivityResumed = true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(sourcesFragment!=null && sourcesFragment.isAttached) { // handle circular reveal in SourcesFragment
                if (sourcesFragment.onBackPressed()) {
                    super.onBackPressed();
                }
            }else super.onBackPressed();
        }

        ObjectAnimator.ofFloat(drawerArrow, "progress", 0).start();
        isDrawerToggleArrow = false;
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_news) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            if(overviewFragment == null) overviewFragment = new OverviewFragment();
            else fragmentTransaction.addToBackStack("0");  // don't add to BackStack on creation because there should always be one fragment attached
            fragmentTransaction.replace(R.id.fragment_container, overviewFragment).commit();
        } else if (id == R.id.nav_sources) {
            if(sourcesFragment == null) sourcesFragment = new SourcesFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.addToBackStack("1");
            fragmentTransaction.replace(R.id.fragment_container, sourcesFragment).commit();
        } else if (id == R.id.nav_preferences) {
            if(settingsFragment == null) settingsFragment = new SettingsFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.addToBackStack("2");
            fragmentTransaction.replace(R.id.fragment_container, settingsFragment).commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // returns true if mode was already applied
    public boolean setTheme() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        int configBefore = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int delegateBefore = AppCompatDelegate.getDefaultNightMode();
        if (sharedPreferences.getBoolean("pref_day_night_theme", true)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
            overviewFragment = null;    // Make Listeners invalid because system-calculated day/night times can't be accessed
        } else {
            if (sharedPreferences.getString("pref_theme_dark", "false").equals("true")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }

        int configAfter = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int delegateAfter = AppCompatDelegate.getDefaultNightMode();

        // optimized if statement
        return delegateBefore == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM ||
                delegateBefore == delegateAfter ||
                configBefore == configAfter &&
                        (delegateBefore == AppCompatDelegate.MODE_NIGHT_AUTO ||
                                delegateAfter == AppCompatDelegate.MODE_NIGHT_AUTO);
    }

    @Override
    public void onClick(View view) {
        long dbId = (long)view.getTag();

        if(articleFragment == null) articleFragment = new ArticleFragment();

        if(articleFragment.getArguments() == null) {
            Bundle args = new Bundle();
            args.putLong("dbId", dbId);
            articleFragment.setArguments(args);
        }else{
            articleFragment.getArguments().putLong("dbId", dbId);
        }

        Intent intent = new Intent(this, ArticleActivity.class);
        intent.putExtra("dbId", dbId);

        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, view.findViewById(R.id.article_header), dbId + "_header");

        int ARTICLE_ACTIVITY = 1;
        startActivityForResult(intent, ARTICLE_ACTIVITY,options.toBundle());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        overviewFragment.cancelRefresh = true;
        super.onActivityResult(requestCode, resultCode, data);
    }

    void rescheduleBackgroundUpdate(){

        int jobId = 4701147;

        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if(jobScheduler.getPendingJob(jobId) != null) jobScheduler.cancel(jobId);


        int updateFrequency = Integer.parseInt(PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString("pref_frequency", "7200000"));

        JobInfo.Builder builder = new JobInfo.Builder(jobId, new ComponentName(this, UpdateService.class));
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setPeriodic(updateFrequency);
        builder.setPersisted(true);
        jobScheduler.schedule(builder.build());
    }
}