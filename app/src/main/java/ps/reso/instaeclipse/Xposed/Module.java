package ps.reso.instaeclipse.Xposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.annotation.SuppressLint;
import android.app.AndroidAppHelper;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.ClassData;
import org.luckypray.dexkit.result.MethodData;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import ps.reso.instaeclipse.R;
import ps.reso.instaeclipse.mods.DevOptionsEnable;
import ps.reso.instaeclipse.mods.GhostModeDM;
import ps.reso.instaeclipse.mods.GhostModeTypingStatus;
import ps.reso.instaeclipse.mods.Interceptor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


@SuppressLint("UnsafeDynamicallyLoadedCode")
public class Module implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private static final String TAG = String.valueOf(R.string.app_name);
    private static final String IG_PACKAGE_NAME = "com.instagram.android";
    private static final String MY_PACKAGE_NAME = "ps.reso.instaeclipse";

    private ClassLoader hostClassLoader;
    public static DexKitBridge dexKitBridge;

    List<Predicate<URI>> uriConditions = new ArrayList<>();
    Boolean isDevEnabled;

    Boolean isGhost_Enabled;
    Boolean isGhost_DM_Enabled;

    Boolean isGhost_Typing_Enabled;
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
    private static String moduleSourceDir;
    private static String moduleLibDir;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        XposedBridge.log(TAG + " | Zygote initialized.");
        // Save the module's APK path
        moduleSourceDir = startupParam.modulePath;
        String abi = Build.SUPPORTED_ABIS[0]; // Primary ABI
        abi = abi.replaceAll("-v\\d+a$", ""); // Regex to remove version suffix
        moduleLibDir = moduleSourceDir.substring(0, moduleSourceDir.lastIndexOf("/")) + "/lib/" + abi;


        XposedBridge.log(TAG + " | Module paths initialized:" +
                "\nSourceDir: " + moduleSourceDir +
                "\nLibDir: " + moduleLibDir);
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
                isGhost_Typing_Enabled = XposedPreferences.getPrefs().getBoolean("ghostModeTyping", false);
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


        } catch (Exception e) {
            XposedBridge.log(TAG + " | Failed to initialize preferences: " + e.getMessage());
        }
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // Ensure preferences are loaded
        loadPreferences();

        XposedBridge.log(TAG + " | Loaded package: " + lpparam.packageName);

        // Initialize DexKitBridge using your module's APK and library path
        if (dexKitBridge == null) {
            try {
                System.load(moduleLibDir + "/libdexkit.so"); // Load your library
                dexKitBridge = DexKitBridge.create(moduleSourceDir); // Initialize with your APK
                XposedBridge.log("DexKitBridge initialized successfully.");
            } catch (Exception e) {
                XposedBridge.log("Failed to initialize DexKitBridge: " + e.getMessage());
            }
        }

        // Hook into your module
        if (lpparam.packageName.equals(MY_PACKAGE_NAME)) {
            hookOwnModule(lpparam);
        }

        // Hook into Instagram
        if (lpparam.packageName.equals(IG_PACKAGE_NAME)) {
            if (dexKitBridge != null) {
                this.hostClassLoader = lpparam.classLoader; // Use Instagram's ClassLoader
                hookInstagram(lpparam);
            } else {
                XposedBridge.log(TAG + " | DexKitBridge is null; skipping Instagram hooks.");
            }
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
                logAllClasses(dexKitBridge);
                hookIsEmployee(dexKitBridge);
            }

            if (isGhost_Enabled) {
                if (isGhost_DM_Enabled) {
                    GhostModeDM ghostModeDM = new GhostModeDM();
                    ghostModeDM.handleGhostMode(lpparam);
                }

                if (isGhost_Typing_Enabled){
                    GhostModeTypingStatus ghostModeTypingStatus = new GhostModeTypingStatus();
                    ghostModeTypingStatus.handleTypingStatus(lpparam);
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

    private void hookIsEmployee(DexKitBridge bridge) {
        try {
            MethodData methodData = bridge.findMethod(FindMethod.create()
                    .matcher(MethodMatcher.create()
                            .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                            .returnType("boolean")
                            .paramTypes("com.instagram.common.session.UserSession")
                            .usingStrings("is_employee")
                    )
            ).singleOrThrow(() -> new IllegalStateException("The returned result is not unique"));

            Method method = methodData.getMethodInstance(hostClassLoader);

            XposedBridge.hookMethod(method, XC_MethodReplacement.returnConstant(true));

            XposedBridge.log(TAG + " | Successfully hooked is_employee: " +
                    method.getDeclaringClass().getName() + "." + method.getName());
        } catch (Exception e) {
            XposedBridge.log(TAG + " | Failed to hook is_employee: " + e.getMessage());
        }

    }


    private void logAllClasses(DexKitBridge bridge) {
        try {
            List<ClassData> classes = bridge.findClass(FindClass.create());
            for (ClassData classData : classes) {
                XposedBridge.log(TAG + " | Found class: " + classData.getName());
            }
            XposedBridge.log(TAG + " | Total classes found: " + classes.size());
        } catch (Exception e) {
            XposedBridge.log(TAG + " | Error while logging all classes: " + e.getMessage());
        }
    }


    private void findClassWithMethod(DexKitBridge bridge) {
        try {
            // Find the class "X.5AD"
            ClassData classData = bridge.findClass(FindClass.create()
                    .matcher(ClassMatcher.create()
                            .className("X.5AD") // Match the class name "X.5AD"
                    )
            ).singleOrThrow(() -> new IllegalStateException("Class X.5AD not found"));

            // Log the class name
            XposedBridge.log(TAG + " | Found class: " + classData.getName());

            // Check for the "A00" method in this class
            List<MethodData> methods = bridge.findMethod(FindMethod.create()
                    .matcher(MethodMatcher.create()
                            .declaredClass("X.5AD") // Limit search to X.5AD
                            .name("A00")            // Match the method name "A00"
                            .returnType("boolean")  // Match return type
                    )
            );

            if (methods.isEmpty()) {
                XposedBridge.log(TAG + " | No methods named A00 found in class X.5AD.");
                return;
            }

            // Log each found method
            for (MethodData method : methods) {
                XposedBridge.log(TAG + " | Found method: " +
                        method.getClassName() + "." + method.getName() +
                        " | Descriptor: " + method.getDescriptor() +
                        " | Modifiers: " + method.getModifiers());
            }

        } catch (Exception e) {
            XposedBridge.log(TAG + " | Error finding class or method: " + e.getMessage());
        }
    }



    // for dev usage
    public static void showToast(final String text) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(AndroidAppHelper.currentApplication().getApplicationContext(), text, Toast.LENGTH_LONG).show());
    }
}