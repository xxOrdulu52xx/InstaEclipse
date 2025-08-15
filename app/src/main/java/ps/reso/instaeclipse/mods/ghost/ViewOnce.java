package ps.reso.instaeclipse.mods.ghost;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.ClassDataList;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import ps.reso.instaeclipse.Xposed.Module;
import ps.reso.instaeclipse.utils.feature.FeatureFlags;
import ps.reso.instaeclipse.utils.feature.FeatureStatusTracker;

public class ViewOnce {

    public void handleViewOnceBlock(DexKitBridge bridge) {
        try {
            // Step 1: Find methods containing "visual_item_seen"
            List<MethodData> methods = bridge.findMethod(
                    FindMethod.create().matcher(
                            MethodMatcher.create().usingStrings("visual_item_seen")
                    )
            );

            if (methods.isEmpty()) {
                XposedBridge.log("(InstaEclipse | ViewOnce): ❌ No methods found containing 'visual_item_seen'");
                return;
            }

            for (MethodData method : methods) {
                ClassDataList paramTypes = method.getParamTypes();
                String returnType = String.valueOf(method.getReturnType());

                // Step 2: Match method signature: (?,?,AbstractClassType) -> void
                if (paramTypes.size() == 3 && returnType.contains("void")) {

                    Method reflectMethod;
                    try {
                        reflectMethod = method.getMethodInstance(Module.hostClassLoader);
                    } catch (Throwable e) {
                        // Skip if reflection fails
                        continue;
                    }

                    // Step 3: Hook method
                    XposedBridge.hookMethod(reflectMethod, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (!FeatureFlags.isGhostViewOnce) {
                                return; // Feature disabled → skip
                            }

                            Object rw = param.args[2]; // Third argument (visual item object)
                            if (rw == null) {
                                return;
                            }

                            for (Method m : rw.getClass().getDeclaredMethods()) {
                                // Only check methods with no params returning String
                                if (m.getParameterTypes().length != 0 || m.getReturnType() != String.class) {
                                    continue;
                                }

                                try {
                                    m.setAccessible(true);
                                    String value = (String) m.invoke(rw);
                                    if (value == null) {
                                        continue;
                                    }

                                    if (value.contains("visual_item_seen") ||
                                            value.contains("send_visual_item_seen_marker")) {
                                        // XposedBridge.log("Blocked ViewOnce send: " + value);
                                        param.setResult(null); // Block this call
                                    }
                                } catch (Throwable ignored) {
                                    // Ignore reflection errors
                                }
                            }
                        }
                    });


                    XposedBridge.log("(InstaEclipse | ViewOnce): ✅ Hooked (dynamic check): " +
                            method.getClassName() + "." + method.getName());
                    FeatureStatusTracker.setHooked("GhostViewOnce");
                    return;
                }
            }

        } catch (Throwable e) {
            XposedBridge.log("(InstaEclipse | ViewOnce): ❌ Exception: " + e.getMessage());
        }
    }
}
