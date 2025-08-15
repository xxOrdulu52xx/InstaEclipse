package ps.reso.instaeclipse.mods.misc;

import android.app.AndroidAppHelper;
import android.content.Context;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import ps.reso.instaeclipse.utils.feature.FeatureStatusTracker;
import ps.reso.instaeclipse.utils.toast.CustomToast;

public class FollowerIndicator {

    public static class FollowMethodResult {
        public final String methodName;
        public final String userClassName;

        public FollowMethodResult(String methodName, String userClassName) {
            this.methodName = methodName;
            this.userClassName = userClassName;
        }
    }

    public FollowMethodResult findFollowerStatusMethod(DexKitBridge bridge) {
        try {
            // 🔍 Step 1: Try new method detection first (obfuscated User class)
            String obfUserClass = null;
            List<MethodData> errMethods = bridge.findMethod(
                    FindMethod.create().matcher(
                            MethodMatcher.create().usingStrings("ERROR_INSERT_EXPIRED_URL")
                    )
            );
            if (!errMethods.isEmpty()) {
                obfUserClass = errMethods.get(0).getClassName();
                // XposedBridge.log("🔍 Found obfuscated User class: " + obfUserClass);
            }

            if (obfUserClass != null) {
                List<MethodData> methods = bridge.findMethod(
                        FindMethod.create().matcher(
                                MethodMatcher.create()
                                        .usingStrings("", "", "")
                                        .paramTypes("com.instagram.common.session.UserSession", obfUserClass)
                        )
                );

                for (MethodData method : methods) {
                    // XposedBridge.log("📌 Inspecting (new) method: " + method.getClassName() + "." + method.getName());

                    for (MethodData invoked : method.getInvokes()) {
                        String className = invoked.getClassName();
                        String returnType = String.valueOf(invoked.getReturnType());

                        if (className.contains(obfUserClass) && returnType.contains("boolean")) {
                            XposedBridge.log("✅ Found follower status method (new): " + invoked.getName());
                            return new FollowMethodResult(invoked.getName(), obfUserClass);
                        }
                    }
                }
            }

            // 🔄 Step 2: Fallback to old detection
            List<MethodData> methodsOld = bridge.findMethod(
                    FindMethod.create().matcher(
                            MethodMatcher.create().usingStrings("", "", "").paramCount(2)
                    )
            );

            for (MethodData method : methodsOld) {
                List<String> paramTypes = new ArrayList<>();
                for (Object param : method.getParamTypes()) {
                    paramTypes.add(String.valueOf(param));
                }
                if (paramTypes.size() == 2 &&
                        paramTypes.get(0).contains("com.instagram.common.session.UserSession") &&
                        paramTypes.get(1).contains("com.instagram.user.model.User")) {
                    for (MethodData invoked : method.getInvokes()) {
                        if (invoked.getClassName().contains("com.instagram.user.model.User") &&
                                String.valueOf(invoked.getReturnType()).contains("boolean")) {
                            XposedBridge.log("✅ Found follower status method (old): " + invoked.getName());
                            return new FollowMethodResult(invoked.getName(), "com.instagram.user.model.User");
                        }
                    }
                }
            }

        } catch (Throwable e) {
            XposedBridge.log("❌ Error in findFollowerStatusMethod: " + e.getMessage());
        }
        return null;
    }

    public void checkFollow(ClassLoader classLoader, String followerStatusMethod, String userClassName) {
        try {
            if (followerStatusMethod == null || userClassName == null) {
                XposedBridge.log("❌ method or class name not found. Skipping hook.");
                return;
            }

            XposedHelpers.findAndHookMethod(userClassName, classLoader, followerStatusMethod, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object user = param.thisObject;

                    String userId = (String) XposedHelpers.callMethod(user, "getId");
                    String username = null;
                    try {
                        username = (String) XposedHelpers.callMethod(user, "getUsername");
                    } catch (Throwable ignored) {
                        // skip username for now in obfuscated versions
                    }

                    Boolean followsMe = (Boolean) param.getResult();
                    String targetId = ps.reso.instaeclipse.utils.tracker.FollowIndicatorTracker.currentlyViewedUserId;

                    if (userId != null && userId.equals(targetId)) {
                        Context context = AndroidAppHelper.currentApplication().getApplicationContext();
                        String message;
                        if (username != null && !username.isEmpty()) {
                            message = "@" + username + " (" + userId + ") " +
                                    (followsMe ? "follows you ✅" : "doesn’t follow you ❌");
                        } else {
                            message = " (" + userId + ") " +
                                    (followsMe ? "follows you ✅" : "doesn’t follow you ❌");
                        }
                        CustomToast.showCustomToast(context, message);
                        ps.reso.instaeclipse.utils.tracker.FollowIndicatorTracker.currentlyViewedUserId = null;
                    }
                }
            });

            XposedBridge.log("✅ Hooked follower status method in: " + userClassName + "." + followerStatusMethod);
            FeatureStatusTracker.setHooked("ShowFollowerToast");

        } catch (Exception e) {
            XposedBridge.log("❌ Error hooking follower status: " + e.getMessage());
        }
    }
}
