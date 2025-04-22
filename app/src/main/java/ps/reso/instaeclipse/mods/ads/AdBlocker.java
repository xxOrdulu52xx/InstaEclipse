package ps.reso.instaeclipse.mods.ads;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import ps.reso.instaeclipse.Xposed.Module;
import ps.reso.instaeclipse.utils.FeatureStatusTracker;

public class AdBlocker {

    public void handleAdInsertionBlock(DexKitBridge bridge) {
        try {
            // Step 1: Find methods that reference "SponsoredContentController.insertItem"
            List<MethodData> methods = bridge.findMethod(
                    FindMethod.create().matcher(
                            MethodMatcher.create().usingStrings("SponsoredContentController.insertItem")
                    )
            );

            if (methods.isEmpty()) {
                XposedBridge.log("(InstaEclipse | AdBlocker): ❌ No ad insertion methods found");
                return;
            }

            for (MethodData method : methods) {
                String returnType = String.valueOf(method.getReturnType());

                // Match only boolean-returning methods
                if (!returnType.contains("boolean")) continue;

                Method reflectMethod;
                try {
                    reflectMethod = method.getMethodInstance(Module.hostClassLoader);
                } catch (Throwable e) {
                    continue;
                }

                int modifiers = reflectMethod.getModifiers();

                // Optional: only hook final methods (or adjust as needed)
                if (!Modifier.isFinal(modifiers)) continue;

                try {
                    XposedBridge.hookMethod(reflectMethod, XC_MethodReplacement.returnConstant(false));

                    XposedBridge.log("(InstaEclipse | AdBlocker): ✅ Hooked ad blocker method: " +
                            method.getClassName() + "." + method.getName());
                    FeatureStatusTracker.setHooked("AdBlocker");
                    return;

                } catch (Throwable e) {
                    XposedBridge.log("(InstaEclipse | AdBlocker): ❌ Hook failed: " + e.getMessage());
                }
            }

        } catch (Throwable t) {
            XposedBridge.log("(InstaEclipse | AdBlocker): ❌ Exception: " + t.getMessage());
        }
    }
}
