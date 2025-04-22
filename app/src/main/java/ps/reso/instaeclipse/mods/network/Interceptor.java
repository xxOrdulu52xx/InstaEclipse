package ps.reso.instaeclipse.mods.network;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.function.Predicate;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import ps.reso.instaeclipse.Xposed.Module;
import ps.reso.instaeclipse.mods.misc.FollowerIndicator;

public class Interceptor {

    public void handleInterceptor(XC_LoadPackage.LoadPackageParam lpparam, List<Predicate<URI>> uriConditions) {
        try {
            ClassLoader classLoader = lpparam.classLoader;

            // Locate the TigonServiceLayer class dynamically
            Class<?> tigonClass = classLoader.loadClass("com.instagram.api.tigon.TigonServiceLayer");
            Method[] methods = tigonClass.getDeclaredMethods();

            Class<?> random_param_1 = null;
            Class<?> random_param_2 = null;
            Class<?> random_param_3 = null;
            String uriFieldName = null;

            // Analyze methods in TigonServiceLayer
            for (Method method : methods) {
                if (method.getName().equals("startRequest") && method.getParameterCount() == 3) {
                    Class<?>[] paramTypes = method.getParameterTypes();
                    random_param_1 = paramTypes[0];
                    random_param_2 = paramTypes[1];
                    random_param_3 = paramTypes[2];
                    break;
                }
            }

            // Dynamically identify the URI field in c5aE
            if (random_param_1 != null) {
                for (Field field : random_param_1.getDeclaredFields()) {
                    if (field.getType().equals(URI.class)) {
                        uriFieldName = field.getName();
                        break;
                    }
                }
            }

            // If classes and fields are resolved, hook the method
            if (random_param_1 != null && random_param_2 != null && random_param_3 != null && uriFieldName != null) {
                String finalUriFieldName = uriFieldName;
                XposedHelpers.findAndHookMethod("com.instagram.api.tigon.TigonServiceLayer", classLoader, "startRequest",
                        random_param_1, random_param_2, random_param_3, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                Object requestObj = param.args[0]; // Dynamic object
                                URI uri = (URI) XposedHelpers.getObjectField(requestObj, finalUriFieldName);

                                if (uri != null && uri.getPath() != null) {

                                    // Check all conditions passed in as predicates
                                    boolean shouldDrop = uriConditions.stream().anyMatch(condition -> condition.test(uri));

                                    if (shouldDrop) {
                                        // Modify the URI to divert the request to a harmless endpoint
                                        try {
                                            URI fakeUri = new URI("https", "127.0.0.1", "/404", null);
                                            XposedHelpers.setObjectField(requestObj, finalUriFieldName, fakeUri);
                                            // XposedBridge.log("🚫 [InstaEclipse] Changed URI to: " + fakeUri);
                                        } catch (Exception e) {
                                            // XposedBridge.log("❌ [InstaEclipse] Failed to modify URI: " + e.getMessage());
                                        }
                                    }
                                    /*
                                     {
                                        XposedBridge.log("✅ [InstaEclipse] NotBlocked: " + uri.getHost() + uri.getPath());
                                    }*/

                                    if (Module.isShow_Follower_Status_Enabled) {
                                        if (uri.getPath() != null && uri.getPath().startsWith("/api/v1/users/") && uri.getPath().contains("/info_stream/")) {
                                            String[] parts = uri.getPath().split("/");
                                            if (parts.length >= 5) {
                                                // Extracted ID from /api/v1/users/{id}/info_stream/
                                                ps.reso.instaeclipse.utils.FollowToastTracker.currentlyViewedUserId = parts[4];
                                                FollowerIndicator followerIndicator = new FollowerIndicator();
                                                String bridge = followerIndicator.findFollowerStatusMethod(Module.dexKitBridge);
                                                followerIndicator.checkFollow(classLoader, bridge);
                                                // XposedBridge.log("👁️ [InstaEclipse] Viewing profile of user: " + userId);
                                            }
                                        }
                                    }

                                }
                            }
                        }
                );
            } else {
                XposedBridge.log("Could not resolve required classes or fields.");
            }

        } catch (Exception e) {
            XposedBridge.log("Error in interceptor: " + e.getMessage());
        }
    }
}
