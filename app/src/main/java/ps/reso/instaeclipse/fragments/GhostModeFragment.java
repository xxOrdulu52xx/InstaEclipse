package ps.reso.instaeclipse.fragments;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ps.reso.instaeclipse.utils.Preferences;
import ps.reso.instaeclipse.R;

public class GhostModeFragment extends Fragment {
    private static final String ENABLE_ALL_KEY = "enableAllGhostMode";
    private static final String DM_KEY = "ghostModeSeen";
    private static final String TYPING_KEY = "ghostModeTyping";
    private static final String SCREEN_SHOT_KEY = "ghostModeScreenShot";
    private static final String VIEW_ONCE_KEY = "ghostModeViewOnce";
    private static final String STORY_KEY = "ghostModeStory";
    private static final String LIVE_KEY = "ghostModeLive";

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch enableAllToggle;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch dmToggle;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch typingToggle;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch screenShotToggle;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch viewOnceToggle;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch storyToggle;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch liveToggle;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ghost_mode, container, false);

        preferences = Preferences.getPrefs();
        editor = preferences.edit();

        // Initialize toggles
        enableAllToggle = view.findViewById(R.id.toggle_all);
        dmToggle = view.findViewById(R.id.dm_toggle);
        typingToggle = view.findViewById(R.id.dm_typing_toggle);
        screenShotToggle = view.findViewById(R.id.screenshot_toggle);
        viewOnceToggle = view.findViewById(R.id.view_once_toggle);
        storyToggle = view.findViewById(R.id.story_toggle);
        liveToggle = view.findViewById(R.id.live_toggle);

        // Load saved states
        loadToggleStates();

        // Handle enable/disable all toggle
        enableAllToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveToggleState(ENABLE_ALL_KEY, isChecked);
            dmToggle.setChecked(isChecked);
            typingToggle.setChecked(isChecked);
            screenShotToggle.setChecked(isChecked);
            viewOnceToggle.setChecked(isChecked);
            storyToggle.setChecked(isChecked);
            liveToggle.setChecked(isChecked);
        });

        // Individual toggles
        dmToggle.setOnCheckedChangeListener((buttonView, isChecked) -> saveToggleState(DM_KEY, isChecked));
        typingToggle.setOnCheckedChangeListener((buttonView, isChecked) -> saveToggleState(TYPING_KEY, isChecked));
        screenShotToggle.setOnCheckedChangeListener((buttonView, isChecked) -> saveToggleState(SCREEN_SHOT_KEY, isChecked));
        viewOnceToggle.setOnCheckedChangeListener((buttonView, isChecked) -> saveToggleState(VIEW_ONCE_KEY, isChecked));
        storyToggle.setOnCheckedChangeListener((buttonView, isChecked) -> saveToggleState(STORY_KEY, isChecked));
        liveToggle.setOnCheckedChangeListener((buttonView, isChecked) -> saveToggleState(LIVE_KEY, isChecked));

        return view;
    }

    private void loadToggleStates() {
        enableAllToggle.setChecked(preferences.getBoolean(ENABLE_ALL_KEY, false));
        dmToggle.setChecked(preferences.getBoolean(DM_KEY, false));
        typingToggle.setChecked(preferences.getBoolean(TYPING_KEY, false));
        screenShotToggle.setChecked(preferences.getBoolean(SCREEN_SHOT_KEY, false));
        viewOnceToggle.setChecked(preferences.getBoolean(VIEW_ONCE_KEY, false));
        storyToggle.setChecked(preferences.getBoolean(STORY_KEY, false));
        liveToggle.setChecked(preferences.getBoolean(LIVE_KEY, false));
    }

    private void saveToggleState(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }
}
