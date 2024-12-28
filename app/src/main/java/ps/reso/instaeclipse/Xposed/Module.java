package ps.reso.instaeclipse.Xposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.app.AndroidAppHelper;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import ps.reso.instaeclipse.mods.DevOptionsEnable;
import ps.reso.instaeclipse.mods.GhostModeDM;
import ps.reso.instaeclipse.mods.Interceptor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


public class Module implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private static final String TAG = "InstaEclipse";
    private static final String IG_PACKAGE_NAME = "com.instagram.android";
    private static final String MY_PACKAGE_NAME = "ps.reso.instaeclipse";

    List<Predicate<URI>> uriConditions = new ArrayList<>();
    Boolean isDevEnabled;

    Boolean isGhost_Enabled;
    Boolean isGhost_DM_Enabled;
    Boolean isGhost_Story_Enabled;
    Boolean isGhost_Live_Enabled;
    Boolean isDistraction_Free_Enabled;
    Boolean isDistraction_Stories_Enabled;
    Boolean isDistraction_Feed_Enabled;
    Boolean isDistraction_Reels_Enabled;
    Boolean isDistraction_Explore_Enabled;

    Boolean isRemove_Ads_Enabled;
    Boolean isRemove_Analysis_Enabled;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        XposedBridge.log(TAG + " | Zygote initialized.");
    }

    public void loadPreferences(){
        try {
            // Load & Reload preferences
            XposedPreferences.loadPreferences();
            XposedPreferences.reloadPrefs();

            // Developer options
            isDevEnabled = XposedPreferences.getPrefs().getBoolean("enableDev", false);

            // Ghost mode
            isGhost_Enabled = XposedPreferences.getPrefs().getBoolean("enableGhostMode", false);

            if (isGhost_Enabled){
                isGhost_DM_Enabled = XposedPreferences.getPrefs().getBoolean("ghostModeDM", false);
                isGhost_Story_Enabled = XposedPreferences.getPrefs().getBoolean("ghostModeLive", false);
                isGhost_Live_Enabled = XposedPreferences.getPrefs().getBoolean("ghostModeStory", false);
            }

            // Distraction free
            isDistraction_Free_Enabled = XposedPreferences.getPrefs().getBoolean("enableDistractionFree", false);

            if (isDistraction_Free_Enabled){
                isDistraction_Stories_Enabled = XposedPreferences.getPrefs().getBoolean("disableStories",false);
                isDistraction_Feed_Enabled = XposedPreferences.getPrefs().getBoolean("disableFeed",false);
                isDistraction_Reels_Enabled = XposedPreferences.getPrefs().getBoolean("disableReels",false);
                isDistraction_Explore_Enabled = XposedPreferences.getPrefs().getBoolean("disableExplore",false);
            }

            // Remove ads
            isRemove_Ads_Enabled = XposedPreferences.getPrefs().getBoolean("removeAds", false);

            // Remove analysis
            isRemove_Analysis_Enabled = XposedPreferences.getPrefs().getBoolean("removeAnalysis", false);


        } catch (Exception e) {
            XposedBridge.log(TAG + " | Failed to initialize preferences: " + e.getMessage());
        }
    }
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        loadPreferences(); // Ensure preferences are loaded

        XposedBridge.log(TAG + " | Loaded package: " + lpparam.packageName);

        // Hook into own app
        if (lpparam.packageName.equals(MY_PACKAGE_NAME)) {
            hookOwnModule(lpparam);
        }

        // Hook into Instagram
        if (lpparam.packageName.equals(IG_PACKAGE_NAME)) {
            hookInstagram(lpparam);
        }
    }

    private void hookOwnModule(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            findAndHookMethod(
                    MY_PACKAGE_NAME + ".MainActivity",
                    lpparam.classLoader,
                    "isModuleActive",
                    XC_MethodReplacement.returnConstant(true)
            );
            XposedBridge.log(TAG + " | Successfully hooked isModuleActive().");
        } catch (Exception e) {
            XposedBridge.log(TAG + " | Failed to hook MainActivity: " + e.getMessage());
        }
    }
    private void hookInstagram(XC_LoadPackage.LoadPackageParam lpparam) {

        try {
            uriConditions.clear();
            XposedBridge.log(TAG + " | Instagram package detected. Hooking...");

            Interceptor interceptor = new Interceptor();

            if (isDevEnabled) {
                DevOptionsEnable devOptionsEnable = new DevOptionsEnable();
                devOptionsEnable.handleDevOptions(lpparam);
            }

            if (isGhost_Enabled) {
                if (isGhost_DM_Enabled) {
                    GhostModeDM ghostModeDM = new GhostModeDM();
                    ghostModeDM.handleGhostMode(lpparam);
                }

                if (isGhost_Story_Enabled) {
                    uriConditions.add(uri -> uri.getPath().contains("/api/v2/media/seen/"));
                }

                if (isGhost_Live_Enabled) {
                    uriConditions.add(uri -> uri.getPath().contains("/heartbeat_and_get_viewer_count/"));
                }
            }

            if (isDistraction_Free_Enabled){
                if (isDistraction_Stories_Enabled){
                    uriConditions.add(uri -> uri.getPath().contains("/feed/reels_tray/") || uri.getPath().contains("/api/v1/feed/reels_media_stream/"));
                    uriConditions.add(uri -> uri.getPath().contains("feed/get_latest_reel_media/"));
                    uriConditions.add(uri -> uri.getPath().contains("direct_v2/pending_inbox/?visual_message"));
                    uriConditions.add(uri -> uri.getPath().contains("stories/hallpass/"));
                }
                if (isDistraction_Feed_Enabled) {
                    uriConditions.add(uri -> uri.getPath().endsWith("/feed/timeline/"));
                }
                if (isDistraction_Reels_Enabled){
                    uriConditions.add(uri -> ((uri.getPath().endsWith("/qp/batch_fetch/") || uri.getPath().contains("api/v1/clips") || uri.getPath().contains("clips") || uri.getPath().contains("mixed_media") || uri.getPath().contains("mixed_media/discover/stream/"))));
                }
                if (isDistraction_Explore_Enabled){
                    uriConditions.add(uri -> ((uri.getPath().contains("/discover/topical_explore") || uri.getPath().contains("/discover/topical_explore_stream"))));
                    uriConditions.add(uri ->  (uri.getHost().contains("i.instagram.com")) && (uri.getPath().contains("/api/v1/fbsearch/top_serp/")));
                }
            }

            if (isRemove_Ads_Enabled){
                uriConditions.add(uri -> uri.getPath().contains("/api/v1/profile_ads/get_profile_ads/"));
                uriConditions.add(uri -> uri.getPath().contains("/ads/"));
                uriConditions.add(uri -> uri.getPath().contains("/feed/injected_reels_media/"));
                uriConditions.add(uri -> uri.getPath().equals("/api/v1/ads/graphql/"));
            }

            if (isRemove_Analysis_Enabled){
                uriConditions.add(uri -> uri.getHost().contains("graph.instagram.com"));
                uriConditions.add(uri -> uri.getPath().contains("/logging_client_events"));
                uriConditions.add(uri -> uri.getHost().contains("graph.facebook.com"));
                uriConditions.add(uri -> uri.getPath().endsWith("/activities"));
            }

            // Pass the dynamically rebuilt conditions to the interceptor
            if (!uriConditions.isEmpty()) {
                interceptor.handleInterceptor(lpparam, uriConditions);
            }




        } catch (Exception e) {
            XposedBridge.log(TAG + " | Failed to hook Instagram: " + e.getMessage());
        }
    }

    // for dev usage
    public static void showToast(final String text) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(AndroidAppHelper.currentApplication().getApplicationContext(), text, Toast.LENGTH_LONG).show());
    }
}
