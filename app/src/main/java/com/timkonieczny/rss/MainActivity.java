package com.timkonieczny.rss;

import android.animation.ObjectAnimator;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    protected static DbList<Article> articles = null;
    protected static SourcesList sources = null;

    private DrawerArrowDrawable drawerArrow;
    private DrawerLayout drawer;
    private Toolbar toolbar;

    protected static boolean goToSettings = false;
    protected static boolean isFragmentSelected = false;

    private OverviewFragment overviewFragment = null;
    private SourcesFragment sourcesFragment = null;
    private SettingsFragment settingsFragment = null;
    private ArticleFragment articleFragment = null;

    protected static int viewWidth;

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

            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();


            drawerArrow = new DrawerArrowDrawable(this);
            drawerArrow.setColor(Color.WHITE);

            toolbar.setNavigationIcon(drawerArrow);


            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            if (!isFragmentSelected) {
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
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

        ObjectAnimator.ofFloat(drawerArrow, "progress", 0).start();
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
            if(overviewFragment == null) overviewFragment = new OverviewFragment();
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, overviewFragment).commit();
        } else if (id == R.id.nav_sources) {
            if(sourcesFragment == null) sourcesFragment = new SourcesFragment();
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, sourcesFragment).commit();
        } else if (id == R.id.nav_preferences) {
            if(settingsFragment == null) settingsFragment = new SettingsFragment();
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, settingsFragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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

        ImageView sharedElement = (ImageView) view.findViewById(R.id.article_header);

        Transition transition = TransitionInflater.from(sharedElement.getContext()).inflateTransition(R.transition.article_header_transition);
        articleFragment.setSharedElementEnterTransition(transition);
        articleFragment.setSharedElementReturnTransition(transition);
        articleFragment.setEnterTransition(new Slide());
        articleFragment.setExitTransition(new Slide());
        sharedElement.clearColorFilter();

        getFragmentManager()
                .beginTransaction()
                .addSharedElement(sharedElement, dbId + "_header")
                .replace(R.id.fragment_container, articleFragment)
                .addToBackStack(null)
                .commit();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        ObjectAnimator.ofFloat(drawerArrow, "progress", 1).start();
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