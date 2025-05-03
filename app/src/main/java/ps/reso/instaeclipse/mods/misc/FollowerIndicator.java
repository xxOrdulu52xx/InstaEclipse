package ps.reso.instaeclipse.mods.misc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
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

    public void checkFollow(ClassLoader classLoader, String followerStatusMethod) {
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
                            Activity activity = ps.reso.instaeclipse.mods.ui.InstagramUI.getCurrentActivity();

                            if (activity != null) {
                                activity.runOnUiThread(() -> {
                                    try {
                                        @SuppressLint("DiscouragedApi")
                                        int titleId = activity.getResources().getIdentifier("action_bar_title", "id", activity.getPackageName());
                                        View titleView = activity.findViewById(titleId);

                                        if (titleView instanceof TextView titleTextView) {

                                            // Delay the text modification
                                            titleTextView.postDelayed(() -> {
                                                // Get existing username text
                                                String existingText = titleTextView.getText().toString();

                                                if (!existingText.contains("✅") && !existingText.contains("❌")) {
                                                    String newText = existingText + (followsMe ? " ✅" : " ❌");
                                                    titleTextView.setText(newText);
                                                }
                                            }, 1000); // Wait 1 second (1000 ms)
                                        }
                                    } catch (Exception e) {
                                        XposedBridge.log("InstaEclipse: Error modifying action_bar_title: " + e.getMessage());
                                    }
                                });

                            }

                            FollowIndicatorTracker.currentlyViewedUserId = null;
                        }
                    }
                }
            });



        } catch (Exception e) {
            XposedBridge.log("❌ Error hooking User class: " + e.getMessage());
        }
    }

}
