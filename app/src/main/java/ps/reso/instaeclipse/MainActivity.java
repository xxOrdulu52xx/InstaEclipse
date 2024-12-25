package ps.reso.instaeclipse;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import com.google.android.material.bottomnavigation.BottomNavigationView;

import ps.reso.instaeclipse.fragments.FeaturesFragment;
import ps.reso.instaeclipse.fragments.HelpFragment;
import ps.reso.instaeclipse.fragments.HomeFragment;
import ps.reso.instaeclipse.utils.Preferences;
import ps.reso.instaeclipse.utils.VersionCheckUtility;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VersionCheckUtility.checkForUpdates(this);

        // Load preferences at the start of the app
        SharedPreferences sharedPreferences = Preferences.loadPreferences(this);
        SharedPreferences.Editor editor = Preferences.getEditor();

        setContentView(R.layout.activity_main);

        // Make content appear under the status bar
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.setStatusBarColor(getResources().getColor(android.R.color.transparent));

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);

        // Load the HomeFragment by default
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        // Handle bottom navigation item clicks
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_features) {
                selectedFragment = new FeaturesFragment();
            } else if (item.getItemId() == R.id.nav_help) {
                selectedFragment = new HelpFragment();
            }


            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            // If there are fragments in the back stack, pop the stack
            getSupportFragmentManager().popBackStack();
        } else {
            // Otherwise, handle the default back button behavior
            super.onBackPressed();
        }
    }

    public static boolean isModuleActive() {
        return false;
    }


    private static boolean checkForRootAccess() {
        try {
            Process process = Runtime.getRuntime().exec("su -c echo success");
            int exitCode = process.waitFor();

            return exitCode == 0; // Root access granted
        } catch (Exception e) {
            // Root access denied
            return false; // No root access or error occurred
        }
    }

    public static boolean hasRootAccess = checkForRootAccess();

}
