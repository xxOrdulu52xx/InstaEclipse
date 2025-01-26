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

import ps.reso.instaeclipse.R;
import ps.reso.instaeclipse.utils.Preferences;

public class MiscFragment extends Fragment {
    private static final String ENABLE_ALL_KEY = "enableAllMiscOptions";
    private static final String STORY_FLIPPING_KEY = "storyFlipping";
    private static final String VIDEO_AUTOPLAY_KEY = "videoAutoPlay";

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch enableAllToggle;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch storyFlippingToggle;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch videoAutoPlayToggle;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_miscellaneous, container, false);

        preferences = Preferences.getPrefs();
        editor = preferences.edit();

        // Initialize toggles
        enableAllToggle = view.findViewById(R.id.toggle_all);
        storyFlippingToggle = view.findViewById(R.id.story_flipping_toggle);
        videoAutoPlayToggle = view.findViewById(R.id.video_autoplay_toggle);


        // Load saved states
        loadToggleStates();

        // Handle enable/disable all toggle
        enableAllToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveToggleState(ENABLE_ALL_KEY, isChecked);
            storyFlippingToggle.setChecked(isChecked);
            videoAutoPlayToggle.setChecked(isChecked);
        });

        // Individual toggles
        storyFlippingToggle.setOnCheckedChangeListener((buttonView, isChecked) -> saveToggleState(STORY_FLIPPING_KEY, isChecked));
        videoAutoPlayToggle.setOnCheckedChangeListener((buttonView, isChecked) -> saveToggleState(VIDEO_AUTOPLAY_KEY, isChecked));

        return view;
    }

    private void loadToggleStates() {
        enableAllToggle.setChecked(preferences.getBoolean(ENABLE_ALL_KEY, false));
        storyFlippingToggle.setChecked(preferences.getBoolean(STORY_FLIPPING_KEY, false));
        videoAutoPlayToggle.setChecked(preferences.getBoolean(VIDEO_AUTOPLAY_KEY, false));
    }

    private void saveToggleState(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }
}
