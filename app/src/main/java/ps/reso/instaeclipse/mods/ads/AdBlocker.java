package ps.reso.instaeclipse.mods.ads;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.util.List;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import ps.reso.instaeclipse.utils.FeatureStatusTracker;

public class AdBlocker {

    public void disableSponsoredContent(DexKitBridge bridge, ClassLoader classLoader) {
        try {
            List<MethodData> methods = bridge.findMethod(
                    FindMethod.create().matcher(
                            MethodMatcher.create().usingStrings("SponsoredContentController.insertItem")
                    )
            );

            if (methods.isEmpty()) {
                XposedBridge.log("(InstaEclipse | AdBlocker): ❌ No methods found referencing 'SponsoredContentController.insertItem'");
                return;
            }

            for (MethodData method : methods) {
                String returnType = String.valueOf(method.getReturnType());
                if (!returnType.contains("boolean")) continue;

                try {
                    Method reflectedMethod = method.getMethodInstance(classLoader);
                    XposedBridge.hookMethod(reflectedMethod, XC_MethodReplacement.returnConstant(false));

                    XposedBridge.log("(InstaEclipse | AdBlocker): ✅ Hooked method: " +
                            method.getClassName() + "." + method.getName());
                    FeatureStatusTracker.setHooked("AdBlocker");
                    return; // Stop after the first successful hook

                } catch (Throwable hookEx) {
                    XposedBridge.log("(InstaEclipse | AdBlocker): ❌ Failed to hook: " +
                            method.getName() + " → " + hookEx.getMessage());
                }
            }

            XposedBridge.log("(InstaEclipse | AdBlocker): ❌ No valid methods hooked.");

        } catch (Throwable t) {
            XposedBridge.log("(InstaEclipse | AdBlocker): ❌: " + t.getMessage());
        }
    }
}
