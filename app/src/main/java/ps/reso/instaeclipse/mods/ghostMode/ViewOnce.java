package ps.reso.instaeclipse.mods.ghostMode;

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
import ps.reso.instaeclipse.utils.FeatureFlags;
import ps.reso.instaeclipse.utils.FeatureStatusTracker;

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
                                // If Ghost View Once is disabled, allow normal execution
                                return;
                            }

                            Object rw = param.args[2]; // Third argument (likely visual item object)

                            if (rw != null) {
                                Method[] allMethods = rw.getClass().getDeclaredMethods();

                                for (Method m : allMethods) {
                                    if (m.getParameterTypes().length == 0 &&
                                            m.getReturnType() == String.class) {
                                        try {
                                            m.setAccessible(true);
                                            String value = (String) m.invoke(rw);

                                            if (value != null && value.contains("send_visual_item_seen_marker")) {
                                                // If it matches visual seen marker, block it
                                                param.setResult(null);
                                                return;
                                            }
                                        } catch (Throwable ignored) {
                                            // Ignore reflection exceptions
                                        }
                                    }
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
