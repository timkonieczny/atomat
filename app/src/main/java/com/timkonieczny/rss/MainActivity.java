package com.timkonieczny.rss;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected static ArticlesList articles = null;
    protected static SourcesList sources = null;

    protected static ActionBarDrawerToggle toggle;
    protected static boolean goToSettings = false;
    protected static boolean isFragmentSelected = false;

    protected static DbManager dbManager;

    private OverviewFragment overviewFragment = null;
    private SourcesFragment sourcesFragment = null;
    private SettingsFragment settingsFragment = null;

    protected static int viewWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // onCreate() is called when recreate() is being called.
        // No point in running this code if everything needs to be rebuilt anyway.
        if(setTheme()) {
            setContentView(R.layout.activity_main);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            dbManager = new DbManager(this);

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
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(dbManager.db != null) dbManager.db.close();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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

//         un-optimized if statement
//        if(delegateBefore == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM){
//            return true;    // returns on first Activity initialization, before theme is applied
//        }else if(delegateBefore == delegateAfter){
//            return true;
//        }else if(configBefore == configAfter &&
//                (delegateBefore == AppCompatDelegate.MODE_NIGHT_AUTO ||
//                        delegateAfter == AppCompatDelegate.MODE_NIGHT_AUTO)){
//            return true;
//        }else{
//            return false;
//        }
//
//        * VALUES BEFORE AND AFTER CHANGING APP THEME
//        *
//        * ISDARK
//        * switch to daynight (day)
//        * before    Configuration.UI_MODE_NIGHT_YES   AppCompatDelegate.MODE_NIGHT_YES
//        * after     Configuration.UI_MODE_NIGHT_YES   AppCompatDelegate.MODE_NIGHT_AUTO
//        * before    Configuration.UI_MODE_NIGHT_NO    AppCompatDelegate.MODE_NIGHT_AUTO
//        * after     Configuration.UI_MODE_NIGHT_NO    AppCompatDelegate.MODE_NIGHT_AUTO
//        *
//        * switch from daynight (day)
//        * before    Configuration.UI_MODE_NIGHT_NO    AppCompatDelegate.MODE_NIGHT_AUTO
//        * after     Configuration.UI_MODE_NIGHT_NO    AppCompatDelegate.MODE_NIGHT_YES
//        * before    Configuration.UI_MODE_NIGHT_YES   AppCompatDelegate.MODE_NIGHT_YES
//        * after     Configuration.UI_MODE_NIGHT_YES   AppCompatDelegate.MODE_NIGHT_YES
//        *
//        * switch to light
//        * before    Configuration.UI_MODE_NIGHT_YES   AppCompatDelegate.MODE_NIGHT_YES
//        * after     Configuration.UI_MODE_NIGHT_YES   AppCompatDelegate.MODE_NIGHT_NO
//        * before    Configuration.UI_MODE_NIGHT_NO    AppCompatDelegate.MODE_NIGHT_NO
//        * after     Configuration.UI_MODE_NIGHT_NO    AppCompatDelegate.MODE_NIGHT_NO
//        *
//        * ISLIGHT
//        * switch to daynight (day)
//        * before:   Configuration.UI_MODE_NIGHT_NO    AppCompatDelegate.MODE_NIGHT_NO
//        * after:    Configuration.UI_MODE_NIGHT_NO    AppCompatDelegate.MODE_NIGHT_AUTO
//        *
//        * switch from daynight (day)
//        * before:   Configuration.UI_MODE_NIGHT_NO    AppCompatDelegate.MODE_NIGHT_AUTO
//        * after:    Configuration.UI_MODE_NIGHT_NO    AppCompatDelegate.MODE_NIGHT_NO
//        *
//        * switch to dark
//        * before:   Configuration.UI_MODE_NIGHT_NO    AppCompatDelegate.MODE_NIGHT_NO
//        * after:    Configuration.UI_MODE_NIGHT_NO    AppCompatDelegate.MODE_NIGHT_YES
//        * before:   Configuration.UI_MODE_NIGHT_YES   AppCompatDelegate.MODE_NIGHT_YES
//        * after:    Configuration.UI_MODE_NIGHT_YES   AppCompatDelegate.MODE_NIGHT_YES
//        *
//        *
    }

    private void printCurrentStatus(){
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_NO:
                Log.d("MainActivity", "Configuration.UI_MODE_NIGHT_NO");
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                Log.d("MainActivity", "Configuration.UI_MODE_NIGHT_YES");
                break;
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                Log.d("MainActivity", "Configuration.UI_MODE_NIGHT_UNDEFINED");
                break;
        }
        switch (AppCompatDelegate.getDefaultNightMode()){
            case AppCompatDelegate.MODE_NIGHT_AUTO:
                Log.d("MainActivity", "AppCompatDelegate.MODE_NIGHT_AUTO");
                break;
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                Log.d("MainActivity", "AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM");
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                Log.d("MainActivity", "AppCompatDelegate.MODE_NIGHT_NO");
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                Log.d("MainActivity", "AppCompatDelegate.MODE_NIGHT_YES");
                break;
        }
    }
}
