package ps.reso.instaeclipse.mods.ads;

import android.content.ClipData;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import ps.reso.instaeclipse.utils.feature.FeatureFlags;
import ps.reso.instaeclipse.utils.feature.FeatureStatusTracker;

public class TrackingLinkDisable {
    public void disableTrackingLinks(ClassLoader classLoader) throws Throwable {
        FeatureStatusTracker.setHooked("DisableTrackingLinks");
        Class<?> clipboardManagerClass = XposedHelpers.findClass("android.content.ClipboardManager", classLoader);
        XposedHelpers.findAndHookMethod(clipboardManagerClass, "setPrimaryClip",
                Class.forName("android.content.ClipData"), new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                        if (FeatureFlags.disableTrackingLinks) {
                            ClipData clipData = (ClipData) param.args[0];
                            if (clipData == null || clipData.getItemCount() == 0 || clipData.getItemAt(0) == null || clipData.getItemAt(0).getText() == null) {
                                return;
                            }
                            String x = clipData.getItemAt(0).getText().toString();
                            if (x.contains("https://www.instagram.com/") && (x.contains("igsh=") || (x.contains("ig_rid=")))) { // Global
                                param.args[0] = ClipData.newPlainText("URL", x.replaceAll("\\?.*", ""));
                            } else if (x.contains("https://www.instagram.com/") && x.contains("?utm_source=")) { // Stories
                                param.args[0] = ClipData.newPlainText("URL", x.replaceAll("\\?utm_source=.*", ""));
                            }
                            else if (x.contains("https://www.instagram.com/") && x.contains("?story_media_id=")){ // Highlights
                                param.args[0] = ClipData.newPlainText("URL", x.replaceAll("\\?story_media_id=.*", ""));
                            }
                            // Saved-by rule: match saved-by or saved_by anywhere in query
                            else if (x.contains("https://www.instagram.com/") &&
                                    x.matches("(?i).*saved[-_]by.*")) {
                                param.args[0] = ClipData.newPlainText("URL", x.replaceAll("\\?.*", ""));
                            }
                        }

                    }
                });
    }
}
