package ps.reso.instaeclipse.utils;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import ps.reso.instaeclipse.mods.ui.InstagramUI;

public class GhostModeUtils {
    public static boolean isGhostModeActive() {
        if (FeatureFlags.quickToggleSeen && FeatureFlags.isGhostSeen) return true;
        if (FeatureFlags.quickToggleTyping && FeatureFlags.isGhostTyping) return true;
        if (FeatureFlags.quickToggleScreenshot && FeatureFlags.isGhostScreenshot) return true;
        if (FeatureFlags.quickToggleViewOnce && FeatureFlags.isGhostViewOnce) return true;
        if (FeatureFlags.quickToggleStory && FeatureFlags.isGhostStory) return true;
        return FeatureFlags.quickToggleLive && FeatureFlags.isGhostLive;
    }


    public static void toggleSelectedGhostOptions(Context context) {
        boolean anySelected = false;
        boolean shouldDisable = false;

        if (FeatureFlags.quickToggleSeen) {
            anySelected = true;
            if (FeatureFlags.isGhostSeen) shouldDisable = true;
        }
        if (FeatureFlags.quickToggleTyping) {
            anySelected = true;
            if (FeatureFlags.isGhostTyping) shouldDisable = true;
        }
        if (FeatureFlags.quickToggleScreenshot) {
            anySelected = true;
            if (FeatureFlags.isGhostScreenshot) shouldDisable = true;
        }
        if (FeatureFlags.quickToggleViewOnce) {
            anySelected = true;
            if (FeatureFlags.isGhostViewOnce) shouldDisable = true;
        }
        if (FeatureFlags.quickToggleStory) {
            anySelected = true;
            if (FeatureFlags.isGhostStory) shouldDisable = true;
        }
        if (FeatureFlags.quickToggleLive) {
            anySelected = true;
            if (FeatureFlags.isGhostLive) shouldDisable = true;
        }

        if (!anySelected) {
            Activity activity = InstagramUI.getCurrentActivity();
            if (activity != null) {
                InstagramUI.addGhostEmojiNextToInbox(activity, false);
            }
            Toast.makeText(context, "❗ No Ghost Mode options selected!", Toast.LENGTH_SHORT).show();
            return; // Nothing to do
        }

        boolean newState = !shouldDisable; // true = enable, false = disable

        if (FeatureFlags.quickToggleSeen) FeatureFlags.isGhostSeen = newState;
        if (FeatureFlags.quickToggleTyping) FeatureFlags.isGhostTyping = newState;
        if (FeatureFlags.quickToggleScreenshot) FeatureFlags.isGhostScreenshot = newState;
        if (FeatureFlags.quickToggleViewOnce) FeatureFlags.isGhostViewOnce = newState;
        if (FeatureFlags.quickToggleStory) FeatureFlags.isGhostStory = newState;
        if (FeatureFlags.quickToggleLive) FeatureFlags.isGhostLive = newState;

        // Save changes
        SettingsManager.saveAllFlags();

        Activity activity = InstagramUI.getCurrentActivity();
        if (activity != null) {
            InstagramUI.addGhostEmojiNextToInbox(activity, newState); // true = show ghost, false = hide
        }


        // Toast
        if (newState) {
            Toast.makeText(context, "👻 Ghost Mode Enabled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "❌ Ghost Mode Disabled", Toast.LENGTH_SHORT).show();
        }
    }
}
