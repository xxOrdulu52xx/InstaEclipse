package ps.reso.instaeclipse.fragments;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.coniy.fileprefs.FileSharedPreferences;

import ps.reso.instaeclipse.utils.Preferences;
import ps.reso.instaeclipse.R;

public class FeaturesFragment extends Fragment {

    private static final String PREFS_NAME = "InstaEclipsePrefs";
    private static final String DEV_OPTIONS_KEY = "enableDev";
    private static final String GHOST_MODE_KEY = "enableGhostMode";
    private static final String DISTRACTION_FREE_KEY = "enableDistractionFree";
    private static final String REMOVE_ADS_KEY = "removeAds";
    private static final String REMOVE_ANALYTICS_KEY = "removeAnalytics";
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch devOptionsToggle;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch ghostModeToggle;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch distractionFreeToggle;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch removeAdsToggle;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch removeAnalyticsToggle;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public void init(){
        // Initialize writable preferences for app use
        sharedPreferences = Preferences.getPrefs();
        editor = Preferences.getEditor();


    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_features, container, false);

        this.init();

        // Initialize toggles
        devOptionsToggle = view.findViewById(R.id.dev_options_toggle);
        ghostModeToggle = view.findViewById(R.id.ghost_mode_toggle);
        distractionFreeToggle = view.findViewById(R.id.distraction_free_toggle);
        removeAdsToggle = view.findViewById(R.id.remove_ads_toggle);
        removeAnalyticsToggle = view.findViewById(R.id.remove_Analytics_toggle);

        // Initialize card views
        CardView devOptionsCard = view.findViewById(R.id.dev_options_card);
        CardView ghostModeCard = view.findViewById(R.id.ghost_mode_card);
        CardView distractionFreeCard = view.findViewById(R.id.distraction_free_card);

        // Load and set toggle states
        loadToggleStates();

        // Set up card click listener for Developer Options
        devOptionsCard.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DeveloperOptionsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Set up card click listener for Ghost Mode
        ghostModeCard.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new GhostModeFragment()) // Navigate to GhostModeFragment
                    .addToBackStack(null)
                    .commit();
        });

        // Set up card click listener for Distraction Free
        distractionFreeCard.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DistractionFreeFragment()) // Navigate to DistractionFreeFragment
                    .addToBackStack(null)
                    .commit();
        });

        // Add listeners for toggles
        devOptionsToggle.setOnCheckedChangeListener((buttonView, isChecked) -> saveToggleState(DEV_OPTIONS_KEY, isChecked));
        ghostModeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> saveToggleState(GHOST_MODE_KEY, isChecked));
        distractionFreeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveToggleState(DISTRACTION_FREE_KEY, isChecked);
            if (isChecked) {
                Toast.makeText(getContext(), R.string.clear_cache_insta_toast, Toast.LENGTH_SHORT).show();
            }
        });

        removeAdsToggle.setOnCheckedChangeListener((buttonView, isChecked) -> saveToggleState(REMOVE_ADS_KEY, isChecked));
        removeAnalyticsToggle.setOnCheckedChangeListener((buttonView, isChecked) -> saveToggleState(REMOVE_ANALYTICS_KEY, isChecked));

        return view;
    }

    /**
     * Load the saved states of toggles from SharedPreferences
     */
    private void loadToggleStates() {
        devOptionsToggle.setChecked(sharedPreferences.getBoolean(DEV_OPTIONS_KEY, false));
        ghostModeToggle.setChecked(sharedPreferences.getBoolean(GHOST_MODE_KEY, false));
        distractionFreeToggle.setChecked(sharedPreferences.getBoolean(DISTRACTION_FREE_KEY, false));
        removeAdsToggle.setChecked(sharedPreferences.getBoolean(REMOVE_ADS_KEY, false));
        removeAnalyticsToggle.setChecked(sharedPreferences.getBoolean(REMOVE_ANALYTICS_KEY, false));
    }

    /**
     * Save the toggle state in writable SharedPreferences
     *
     * @param key   The key for the preference
     * @param value The state of the toggle
     */
    private void saveToggleState(String key, boolean value) {

        this.init();
        editor.putBoolean(key, value);
        editor.apply();

        // Make preferences world-readable
        FileSharedPreferences.makeWorldReadable(requireContext().getPackageName(), PREFS_NAME);

    }


}
