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
import ps.reso.instaeclipse.utils.CustomToast;

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


                    XposedBridge.log("🔍 Inspecting method: " + method.getClassName() + "." + method.getName());


                    for (MethodData invoked : method.getInvokes()) {
                        String className = invoked.getClassName();
                        String methodName = invoked.getName();
                        String returnType = String.valueOf(invoked.getReturnType());

                        for (Object param : invoked.getParamTypes()) {
                            paramTypes.add(String.valueOf(param));
                        }

                        // Log everything
//                        XposedBridge.log("👉 INVOKED: " + className + "." + methodName +
//                                " | return: " + returnType +
//                                " | params: " + paramTypes);

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
                    Object user = param.thisObject;

                    String userId = (String) XposedHelpers.callMethod(user, "getId");
                    String username = (String) XposedHelpers.callMethod(user, "getUsername");
                    Boolean followsMe = (Boolean) param.getResult();

                    String targetId = ps.reso.instaeclipse.utils.FollowToastTracker.currentlyViewedUserId;

                    if (userId != null && userId.equals(targetId)) {


                        XposedBridge.log("👤 Profile viewed: @" + username + " (ID: " + userId + ") | Follows you: " + followsMe);
                        Context context = AndroidAppHelper.currentApplication().getApplicationContext();
                        String message = "@" + username + " (" + userId + ") " + (followsMe ? "follows you ✅" : "doesn’t follow you ❌");
                        CustomToast.showCustomToast(context, message);

                        ps.reso.instaeclipse.utils.FollowToastTracker.currentlyViewedUserId = null;


                    }
                }
            });

        } catch (Exception e) {
            XposedBridge.log("❌ Error hooking User class: " + e.getMessage());
        }
    }
}
