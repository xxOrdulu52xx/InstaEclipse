package ps.reso.instaeclipse.mods.ui.utils;

import static ps.reso.instaeclipse.mods.ghost.ui.GhostEmojiManager.addGhostEmojiNextToInbox;
import static ps.reso.instaeclipse.mods.ui.UIHookManager.getCurrentActivity;
import static ps.reso.instaeclipse.mods.ui.UIHookManager.setupHooks;

import android.app.Activity;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.ClassDataList;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import ps.reso.instaeclipse.Xposed.Module;
import ps.reso.instaeclipse.utils.ghost.GhostModeUtils;

public class BottomSheetHookUtil {

    public static void hookBottomSheetNavigator(DexKitBridge bridge) {
        try {
            List<MethodData> methods = bridge.findMethod(
                    FindMethod.create()
                            .matcher(
                                    MethodMatcher.create()
                                            .usingStrings("BottomSheetConstants")
                            )
            );

            if (methods.isEmpty()) {
                return;
            }

            for (MethodData method : methods) {
                // ✅ Filter to only methods inside the InstagramMainActivity class
                if (!method.getClassName().equals("com.instagram.mainactivity.InstagramMainActivity")) {
                    continue;
                }

                Method reflectMethod;
                try {
                    reflectMethod = method.getMethodInstance(Module.hostClassLoader);
                } catch (Throwable e) {
                    continue;
                }

                int modifiers = reflectMethod.getModifiers();
                String returnType = String.valueOf(method.getReturnType());
                ClassDataList paramTypes = method.getParamTypes();

                // ✅ Match: final, non-static, non-void return, 0-args
                if (!Modifier.isStatic(modifiers)
                        && Modifier.isFinal(modifiers)
                        && !returnType.contains("void")
                        && paramTypes.size() == 0) {

                    XposedBridge.hookMethod(reflectMethod, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            final Activity activity = getCurrentActivity();
                            if (activity != null) {
                                activity.runOnUiThread(() -> {
                                    try {
                                        setupHooks(activity);
                                        addGhostEmojiNextToInbox(activity, GhostModeUtils.isGhostModeActive());
                                    } catch (Exception ignored) {
                                    }
                                });
                            }
                        }
                    });

                    XposedBridge.log("(InstaEclipse | BottomSheet): ✅ Hooked: " +
                            method.getClassName() + "." + method.getName());
                    return;
                }
            }

        } catch (Throwable e) {
            XposedBridge.log("(InstaEclipse | BottomSheet): ❌ DexKit exception: " + e.getMessage());
        }
    }
}

