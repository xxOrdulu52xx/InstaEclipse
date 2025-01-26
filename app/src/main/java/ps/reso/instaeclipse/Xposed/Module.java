package ps.reso.instaeclipse.Xposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.annotation.SuppressLint;
import android.app.AndroidAppHelper;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.luckypray.dexkit.DexKitBridge;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import ps.reso.instaeclipse.mods.DevOptionsEnable;
import ps.reso.instaeclipse.mods.GhostModeDM;
import ps.reso.instaeclipse.mods.Interceptor;
import ps.reso.instaeclipse.mods.misc.AutoPlayDisable;
import ps.reso.instaeclipse.mods.misc.StoryFlipping;


@SuppressLint("UnsafeDynamicallyLoadedCode")
public class Module implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private static final String IG_PACKAGE_NAME = "com.instagram.android";
    private static final String MY_PACKAGE_NAME = "ps.reso.instaeclipse";
    public static DexKitBridge dexKitBridge;
    private static String moduleSourceDir;
    private static String moduleLibDir;
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
    Boolean isDistraction_Comments_Enabled;
    Boolean isRemove_Ads_Enabled;
    Boolean isRemove_Analytics_Enabled;
    Boolean is_Misc_Enabled;
    Boolean isStop_Story_Flipping_Enabled;
    Boolean isStop_Video_AutoPlay_Enabled;
    public static ClassLoader hostClassLoader;

    // for dev usage
    public static void showToast(final String text) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(AndroidAppHelper.currentApplication().getApplicationContext(), text, Toast.LENGTH_LONG).show());
    }

    @Override
    public void initZygote(StartupParam startupParam) {
        XposedBridge.log("(InstaEclipse): Zygote initialized.");
        // Save the module's APK path
        moduleSourceDir = startupParam.modulePath;
        String abi = Build.SUPPORTED_ABIS[0]; // Primary ABI
        abi = abi.replaceAll("-v\\d+a$", ""); // Regex to remove version suffix
        moduleLibDir = moduleSourceDir.substring(0, moduleSourceDir.lastIndexOf("/")) + "/lib/" + abi;


        // XposedBridge.log("InstaEclipse | Module paths initialized:" + "\nSourceDir: " + moduleSourceDir + "\nLibDir: " + moduleLibDir);
    }

    public void loadPreferences() {
        try {
            // Load & Reload preferences
            XposedPreferences.loadPreferences();
            XposedPreferences.reloadPrefs();

            // Developer options
            isDevEnabled = XposedPreferences.getPrefs().getBoolean("enableDev", false);

            // Ghost mode
            isGhost_Enabled = XposedPreferences.getPrefs().getBoolean("enableGhostMode", false);

            if (isGhost_Enabled) {
                isGhost_DM_Enabled = XposedPreferences.getPrefs().getBoolean("ghostModeDM", false);
                isGhost_Story_Enabled = XposedPreferences.getPrefs().getBoolean("ghostModeLive", false);
                isGhost_Live_Enabled = XposedPreferences.getPrefs().getBoolean("ghostModeStory", false);
            }

            // Distraction free
            isDistraction_Free_Enabled = XposedPreferences.getPrefs().getBoolean("enableDistractionFree", false);

            if (isDistraction_Free_Enabled) {
                isDistraction_Stories_Enabled = XposedPreferences.getPrefs().getBoolean("disableStories", false);
                isDistraction_Feed_Enabled = XposedPreferences.getPrefs().getBoolean("disableFeed", false);
                isDistraction_Reels_Enabled = XposedPreferences.getPrefs().getBoolean("disableReels", false);
                isDistraction_Explore_Enabled = XposedPreferences.getPrefs().getBoolean("disableExplore", false);
                isDistraction_Comments_Enabled = XposedPreferences.getPrefs().getBoolean("disableComments", false);
            }

            // Remove ads
            isRemove_Ads_Enabled = XposedPreferences.getPrefs().getBoolean("removeAds", false);

            // Remove analysis
            isRemove_Analytics_Enabled = XposedPreferences.getPrefs().getBoolean("removeAnalytics", false);

            // Misc options
            is_Misc_Enabled = XposedPreferences.getPrefs().getBoolean("miscOptions", false);

            if (is_Misc_Enabled){
                isStop_Story_Flipping_Enabled = XposedPreferences.getPrefs().getBoolean("storyFlipping", false);
                isStop_Video_AutoPlay_Enabled = XposedPreferences.getPrefs().getBoolean("videoAutoPlay", false);
            }


        } catch (Exception e) {
            XposedBridge.log("(InstaEclipse): Failed to initialize preferences: " + e.getMessage());
        }
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        // Ensure preferences are loaded
        loadPreferences();

        XposedBridge.log("(InstaEclipse): Loaded package: " + lpparam.packageName);

        // Hook into your module
        if (lpparam.packageName.equals(MY_PACKAGE_NAME)) {
            try {
                if (dexKitBridge == null) {
                    // Load the .so file from your module
                    System.load(moduleLibDir + "/libdexkit.so");
                    XposedBridge.log("libdexkit.so loaded successfully.");

                    // Initialize DexKitBridge with your module's APK (for module-specific tasks, if needed)
                    dexKitBridge = DexKitBridge.create(moduleSourceDir);
                    // XposedBridge.log("DexKitBridge initialized for InstaEclipse.");
                }

                // Hook your module
                hookOwnModule(lpparam);

            } catch (Exception e) {
                XposedBridge.log("(InstaEclipse): Failed to initialize DexKitBridge for InstaEclipse: " + e.getMessage());
            }
        }

        // Hook into Instagram
        if (lpparam.packageName.equals(IG_PACKAGE_NAME)) {
            try {
                if (dexKitBridge == null) {
                    // Load the .so file from your module (if not already loaded)
                    System.load(moduleLibDir + "/libdexkit.so");
                    // XposedBridge.log("libdexkit.so loaded successfully.");

                    // Initialize DexKitBridge with Instagram's APK
                    dexKitBridge = DexKitBridge.create(lpparam.appInfo.sourceDir);
                    // XposedBridge.log("DexKitBridge initialized with Instagram's APK: " + lpparam.appInfo.sourceDir);
                }

                // Use Instagram's ClassLoader
                hostClassLoader = lpparam.classLoader;

                // Call the method to hook Instagram
                hookInstagram(lpparam);

            } catch (Exception e) {
                XposedBridge.log("(InstaEclipse): Failed to initialize DexKitBridge for Instagram: " + e.getMessage());
            }
        }
    }

    private void hookOwnModule(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            findAndHookMethod(MY_PACKAGE_NAME + ".MainActivity", lpparam.classLoader, "isModuleActive", XC_MethodReplacement.returnConstant(true));
            // XposedBridge.log("InstaEclipse | Successfully hooked isModuleActive().");
        } catch (Exception e) {
            XposedBridge.log("(InstaEclipse): Failed to hook MainActivity: " + e.getMessage());
        }
    }

    private void hookInstagram(XC_LoadPackage.LoadPackageParam lpparam) {

        try {
            uriConditions.clear();
            XposedBridge.log("(InstaEclipse): Instagram package detected. Hooking...");

            Interceptor interceptor = new Interceptor();

            if (isDevEnabled) {
                DevOptionsEnable devOptionsEnable = new DevOptionsEnable();
                devOptionsEnable.handleDevOptions(dexKitBridge);
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

            if (isDistraction_Free_Enabled) {
                if (isDistraction_Stories_Enabled) {
                    uriConditions.add(uri -> uri.getPath().contains("/feed/reels_tray/") || uri.getPath().contains("/api/v1/feed/reels_media_stream/"));
                    uriConditions.add(uri -> uri.getPath().contains("feed/get_latest_reel_media/"));
                    uriConditions.add(uri -> uri.getPath().contains("direct_v2/pending_inbox/?visual_message"));
                    uriConditions.add(uri -> uri.getPath().contains("stories/hallpass/"));
                }
                if (isDistraction_Feed_Enabled) {
                    uriConditions.add(uri -> uri.getPath().endsWith("/feed/timeline/"));

                }
                if (isDistraction_Reels_Enabled) {
                    uriConditions.add(uri -> ((uri.getPath().endsWith("/qp/batch_fetch/") || uri.getPath().contains("api/v1/clips") || uri.getPath().contains("clips") || uri.getPath().contains("mixed_media") || uri.getPath().contains("mixed_media/discover/stream/"))));
                }
                if (isDistraction_Explore_Enabled) {
                    uriConditions.add(uri -> ((uri.getPath().contains("/discover/topical_explore") || uri.getPath().contains("/discover/topical_explore_stream"))));
                    uriConditions.add(uri -> (uri.getHost().contains("i.instagram.com")) && (uri.getPath().contains("/api/v1/fbsearch/top_serp/")));
                }
                if (isDistraction_Comments_Enabled) {
                    uriConditions.add(uri -> ((uri.getPath().contains("/api/v1/media/") && uri.getPath().contains("comments/"))));
                }
            }

            if (isRemove_Ads_Enabled) {
                uriConditions.add(uri -> uri.getPath().contains("profile_ads/get_profile_ads/"));
                uriConditions.add(uri -> uri.getPath().contains("/ads/"));
                uriConditions.add(uri -> uri.getPath().contains("/feed/injected_reels_media/"));
                uriConditions.add(uri -> uri.getPath().equals("/api/v1/ads/graphql/"));
            }

            if (isRemove_Analytics_Enabled) {
                uriConditions.add(uri -> uri.getHost().contains("graph.instagram.com"));
                uriConditions.add(uri -> uri.getHost().contains("graph.facebook.com"));
                uriConditions.add(uri -> uri.getPath().contains("/logging_client_events"));
                uriConditions.add(uri -> uri.getPath().endsWith("/activities"));
            }

            if (is_Misc_Enabled){
                if (isStop_Story_Flipping_Enabled){
                    StoryFlipping storyFlipping = new StoryFlipping();
                    storyFlipping.handleStoryFlippingDisable(dexKitBridge);
                }
                if (isStop_Video_AutoPlay_Enabled){
                    AutoPlayDisable autoPlayDisable = new AutoPlayDisable();
                    autoPlayDisable.handleAutoPlayDisable(dexKitBridge);
                }
            }

            // Pass the dynamically rebuilt conditions to the interceptor
            if (!uriConditions.isEmpty()) {
                interceptor.handleInterceptor(lpparam, uriConditions);
            }


        } catch (Exception e) {
            XposedBridge.log("(InstaEclipse): Failed to hook Instagram: " + e.getMessage());
        }
    }
}