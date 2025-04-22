package ps.reso.instaeclipse.mods.ghostMode;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.ClassData;
import org.luckypray.dexkit.result.ClassDataList;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import ps.reso.instaeclipse.Xposed.Module;
import ps.reso.instaeclipse.utils.FeatureStatusTracker;

public class ScreenshotDetection {

    public void handleScreenshotBlock(DexKitBridge bridge) {
        try {
            // Step 1: Find class referencing "ScreenshotNotificationManager"
            List<ClassData> classes = bridge.findClass(FindClass.create()
                    .matcher(ClassMatcher.create().usingStrings("ScreenshotNotificationManager")));

            if (classes.isEmpty()) {
                XposedBridge.log("(InstaEclipse | ScreenshotBlock): ❌ No class found containing 'ScreenshotNotificationManager'");
                return;
            }

            for (ClassData classData : classes) {
                String className = classData.getName();

                // Step 2: Find all methods in that class
                List<MethodData> methods = bridge.findMethod(FindMethod.create()
                        .matcher(MethodMatcher.create().declaredClass(className)));

                for (MethodData method : methods) {
                    ClassDataList paramTypes = method.getParamTypes();
                    String returnType = String.valueOf(method.getReturnType());

                    // Match: void method(long)
                    if (returnType.contains("void") &&
                            paramTypes.size() == 1 &&
                            String.valueOf(paramTypes.get(0)).contains("long")) {

                        try {
                            Method targetMethod = method.getMethodInstance(Module.hostClassLoader);

                            XposedBridge.hookMethod(targetMethod, new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) {
                                    /*
                                    Debug purposes
                                    XposedBridge.log("(InstaEclipse | ScreenshotBlock): 🛑 Screenshot blocked");
                                    */
                                    param.setResult(null); // Block logic
                                }
                            });

                            XposedBridge.log("(InstaEclipse | ScreenshotBlock): ✅ Hooked: " +
                                    method.getClassName() + "." + method.getName());
                            FeatureStatusTracker.setHooked("GhostScreenshot");
                            return;

                        } catch (Throwable e) {
                            XposedBridge.log("(InstaEclipse | ScreenshotBlock): ❌ Hook error: " + e.getMessage());
                        }
                    }
                }
            }

        } catch (Throwable e) {
            XposedBridge.log("(InstaEclipse | ScreenshotBlock): ❌ Exception: " + e.getMessage());
        }
    }
}
