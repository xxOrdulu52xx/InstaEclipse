package ps.reso.instaeclipse.mods.misc;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.util.List;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import ps.reso.instaeclipse.Xposed.Module;

public class AutoPlayDisable {
    public void handleAutoPlayDisable(DexKitBridge bridge) {
        try {
            findAndHookDynamicMethod(bridge);
        } catch (Exception e) {
            XposedBridge.log("(InstaEclipse | AutoPlayDisable): Error: " + e.getMessage());
        }
    }

    private void findAndHookDynamicMethod(DexKitBridge bridge) {
        try {
            // Step 1: Find methods referencing "ig_disable_video_autoplay"
            List<MethodData> methods = bridge.findMethod(FindMethod.create()
                    .matcher(MethodMatcher.create()
                            .usingStrings("ig_disable_video_autoplay") // Match methods referencing the string
                    )
            );

            if (methods.isEmpty()) {
                XposedBridge.log("(InstaEclipse | AutoPlayDisable): No matching methods found.");
                return;
            }

            // Step 2: Narrow down to methods with correct signature
            for (MethodData method : methods) {
                // Check if the method has the correct return type
                boolean returnTypeMatch = String.valueOf(method.getReturnType()).contains("boolean"); // Ensure return type is boolean

                // Check if the method takes exactly one parameter
                boolean paramTypesMatch = method.getParamTypes().size() == 1;

                if (returnTypeMatch && paramTypesMatch) {
                    // Step 3: Hook the method
                    hookMethod(method);
                    return; // Exit after hooking the first valid method
                }
            }

            XposedBridge.log("(InstaEclipse | AutoPlayDisable): No matching methods with the correct signature were found.");
        } catch (Exception e) {
            XposedBridge.log("(InstaEclipse | AutoPlayDisable): Error during dynamic method discovery: " + e.getMessage());
        }
    }

    private void hookMethod(MethodData method) {
        try {
            // Get the target method instance
            Method targetMethod = method.getMethodInstance(Module.hostClassLoader);

            // Hook the method to always return true
            XposedBridge.hookMethod(targetMethod, XC_MethodReplacement.returnConstant(true));

            XposedBridge.log("(InstaEclipse | AutoPlayDisable): Successfully hooked method: " +
                    method.getClassName() + "." + method.getName());
        } catch (Exception e) {
            XposedBridge.log("(InstaEclipse | AutoPlayDisable): Error hooking method: " + e.getMessage());
        }
    }
}
