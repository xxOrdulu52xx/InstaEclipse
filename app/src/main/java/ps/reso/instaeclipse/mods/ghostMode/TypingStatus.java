package ps.reso.instaeclipse.mods.ghostMode;

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
import ps.reso.instaeclipse.utils.FeatureFlags;
import ps.reso.instaeclipse.utils.FeatureStatusTracker;

public class TypingStatus {

    public void handleTypingBlock(DexKitBridge bridge) {
        try {
            // Step 1: Find methods containing the string "is_typing_indicator_enabled"
            List<MethodData> methods = bridge.findMethod(FindMethod.create()
                    .matcher(MethodMatcher.create().usingStrings("is_typing_indicator_enabled")));

            if (methods.isEmpty()) {
                XposedBridge.log("(InstaEclipse | TypingBlock): ❌ No methods found containing 'is_typing_indicator_enabled'");
                return;
            }

            for (MethodData method : methods) {
                ClassDataList paramTypes = method.getParamTypes();
                String returnType = String.valueOf(method.getReturnType());

                Method reflectMethod;
                try {
                    reflectMethod = method.getMethodInstance(Module.hostClassLoader);
                } catch (Throwable e) {
                    // Skip method if it can't be resolved
                    continue;
                }

                int modifiers = reflectMethod.getModifiers();

                // Step 2: Match: static final void method(ClassType, boolean)
                if (Modifier.isStatic(modifiers) &&
                        Modifier.isFinal(modifiers) &&
                        returnType.contains("void") &&
                        paramTypes.size() == 2 &&
                        String.valueOf(paramTypes.get(1)).contains("boolean")) {

                    try {
                        // Step 3: Hook method dynamically
                        XposedBridge.hookMethod(reflectMethod, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                if (FeatureFlags.isGhostTyping) {
                                    // If ghost typing is enabled, block typing ping
                                    param.setResult(null);
                                }
                            }
                        });

                        XposedBridge.log("(InstaEclipse | TypingBlock): ✅ Hooked (dynamic check): " +
                                method.getClassName() + "." + method.getName());
                        FeatureStatusTracker.setHooked("GhostTyping");
                        return;

                    } catch (Throwable e) {
                        XposedBridge.log("(InstaEclipse | TypingBlock): ❌ Hook error: " + e.getMessage());
                    }
                }
            }

        } catch (Throwable t) {
            XposedBridge.log("(InstaEclipse | TypingBlock): ❌ Exception: " + t.getMessage());
        }
    }
}
