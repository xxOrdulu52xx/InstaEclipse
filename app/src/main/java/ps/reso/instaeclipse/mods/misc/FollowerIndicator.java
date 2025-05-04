package ps.reso.instaeclipse.mods.misc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import ps.reso.instaeclipse.mods.ui.InstagramUI;
import ps.reso.instaeclipse.utils.tracker.FollowIndicatorTracker;
import ps.reso.instaeclipse.utils.feature.FeatureFlags;
import ps.reso.instaeclipse.utils.feature.FeatureStatusTracker;

public class FollowerIndicator {
    public String findFollowerStatusMethod(DexKitBridge bridge) {
        try {
            List<MethodData> methods = bridge.findMethod(FindMethod.create().matcher(MethodMatcher.create().usingStrings("", "", "") // Look for 3+ empty strings
                    .paramCount(2)));

            for (MethodData method : methods) {
                List<String> paramTypes = new ArrayList<>();
                for (Object param : method.getParamTypes()) {
                    paramTypes.add(String.valueOf(param));
                }

                // Match (UserSession, User)
                if (paramTypes.size() == 2 && paramTypes.get(0).contains("com.instagram.common.session.UserSession") && paramTypes.get(1).contains("com.instagram.user.model.User")) {


                    for (MethodData invoked : method.getInvokes()) {
                        String className = invoked.getClassName();
                        String methodName = invoked.getName();
                        String returnType = String.valueOf(invoked.getReturnType());

                        for (Object param : invoked.getParamTypes()) {
                            paramTypes.add(String.valueOf(param));
                        }

                        // ✅ Just find first method in User class with returnType boolean & no params
                        if (className.contains("com.instagram.user.model.User") && (returnType.contains("boolean"))) {

                            // XposedBridge.log("🎯 Matched Method: " + className + "." + methodName);
                            return methodName;
                        }
                    }
                    // inspectInvokedMethods(bridge, method); // your helper
                }
            }

        } catch (Throwable e) {
            XposedBridge.log("❌ Error in method discovery: " + e.getMessage());
        }
        return null;
    }

    public void checkFollow(ClassLoader classLoader, String followerStatusMethod, DexKitBridge bridge) {
        try {
            if (followerStatusMethod == null) {
                XposedBridge.log("❌ method name not found. Skipping hook.");
                return;
            }

            XposedHelpers.findAndHookMethod("com.instagram.user.model.User", classLoader, followerStatusMethod, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (FeatureFlags.showFollowerToast) {
                        Object user = param.thisObject;

                        String userId = (String) XposedHelpers.callMethod(user, "getId");
                        Boolean followsMe = (Boolean) param.getResult();
                        FeatureStatusTracker.setHooked("ShowFollowerToast");
                        String targetId = FollowIndicatorTracker.currentlyViewedUserId;

                        if (userId != null && userId.equals(targetId)) {

                            FollowIndicatorTracker.followsMe = followsMe;

                            hookConfigureActionBar(bridge, classLoader);

                            FollowIndicatorTracker.currentlyViewedUserId = null;
                        }
                    }
                }
            });

        } catch (Exception e) {
            XposedBridge.log("❌ Error hooking User class: " + e.getMessage());
        }
    }

    public void hookConfigureActionBar(DexKitBridge bridge, ClassLoader classLoader) {
        try {
            List<MethodData> methods = bridge.findMethod(
                    FindMethod.create().matcher(
                            MethodMatcher.create()
                                    .name("configureActionBar")
                                    .declaredClass("com.instagram.profile.fragment.UserDetailFragment")
                                    .returnType("void")
                                    .paramCount(1)
                    )
            );

            if (methods.isEmpty()) {
                XposedBridge.log("❌ DexKit: No configureActionBar method found");
                return;
            }

            MethodData method = methods.get(0);
            Method targetMethod = method.getMethodInstance(classLoader);
            Class<?> paramClass = targetMethod.getParameterTypes()[0];

            XposedHelpers.findAndHookMethod(
                    "com.instagram.profile.fragment.UserDetailFragment",
                    classLoader,
                    "configureActionBar",
                    paramClass,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Activity activity = InstagramUI.getCurrentActivity();
                            if (activity != null && FollowIndicatorTracker.followsMe != null) {
                                try {
                                    @SuppressLint("DiscouragedApi")
                                    int titleId = activity.getResources().getIdentifier("action_bar_title", "id", activity.getPackageName());
                                    View titleView = activity.findViewById(titleId);

                                    if (titleView instanceof TextView titleTextView) {
                                        try {
                                            String existingText = titleTextView.getText().toString();
                                            String newText;

                                            if (FeatureFlags.showFollowerToast) {
                                                if (!existingText.contains("✅") && !existingText.contains("❌")) {
                                                    newText = (FollowIndicatorTracker.followsMe ? "✅ " : "❌ ") + existingText;
                                                    titleTextView.setText(newText);
                                                }
                                            } else {
                                                // If badges exist, strip them
                                                if (existingText.contains("✅") || existingText.contains("❌")) {
                                                    newText = existingText.replace("✅ ", "").replace("❌ ", "");
                                                    titleTextView.setText(newText);
                                                }
                                            }
                                        } catch (Exception ex) {
                                            XposedBridge.log("❌ Error updating badge: " + ex.getMessage());
                                        }
                                    }

                                } catch (Exception e) {
                                    XposedBridge.log("❌ Error locating title: " + e.getMessage());
                                }
                            }
                        }
                    }
            );

        } catch (Throwable t) {
            XposedBridge.log("❌ Exception while hooking configureActionBar: " + t.getMessage());
        }

    }

}
