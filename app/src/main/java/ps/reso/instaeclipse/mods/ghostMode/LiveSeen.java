package ps.reso.instaeclipse.mods.ghostMode;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;
import org.luckypray.dexkit.result.ClassDataList;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import ps.reso.instaeclipse.Xposed.Module;
import ps.reso.instaeclipse.utils.FeatureStatusTracker;

public class LiveSeen {
    public void handleLiveSeenBlock(DexKitBridge bridge) {
        try {
            // Step 1: Find methods containing "heartbeat_and_get_viewer_count"
            List<MethodData> methods = bridge.findMethod(
                    FindMethod.create().matcher(
                            MethodMatcher.create().usingStrings("heartbeat_and_get_viewer_count")
                    )
            );

            if (methods.isEmpty()) {
                XposedBridge.log("(InstaEclipse | LiveGhost): ❌ No methods found for live heartbeat");
                return;
            }

            for (MethodData method : methods) {
                ClassDataList paramTypes = method.getParamTypes();

                Method reflectMethod;
                try {
                    reflectMethod = method.getMethodInstance(Module.hostClassLoader);
                } catch (Throwable e) {
                    continue;
                }

                int modifiers = reflectMethod.getModifiers();

                // Match: final method with 2 params, second is String
                if (Modifier.isFinal(modifiers)
                        && paramTypes.size() == 2
                        && String.valueOf(paramTypes.get(1)).contains("String")) {

                    try {
                        XposedBridge.hookMethod(reflectMethod, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                /*
                                Debug purposes
                                XposedBridge.log("(InstaEclipse | LiveGhost): 🚫 Blocked live viewer count heartbeat");
                                */
                                param.setResult(null);
                            }
                        });

                        XposedBridge.log("(InstaEclipse | LiveGhost): ✅ Hooked: " +
                                method.getClassName() + "." + method.getName());
                        FeatureStatusTracker.setHooked("GhostLive");
                        return;

                    } catch (Throwable e) {
                        XposedBridge.log("(InstaEclipse | LiveGhost): ❌ Hook error: " + e.getMessage());
                    }
                }
            }

        } catch (Throwable t) {
            XposedBridge.log("(InstaEclipse | LiveGhost): ❌ Exception: " + t.getMessage());
        }
    }
}
