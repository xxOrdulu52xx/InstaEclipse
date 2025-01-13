package ps.reso.instaeclipse.mods;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodDataList;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class DevOptionsEnable {

    private static final String TAG = "DevOptionsEnable";

    public void handleDevOptions(DexKitBridge dexKit, ClassLoader classLoader) {
        if (dexKit == null) {
            XposedBridge.log(TAG + " | DexKit is null. Aborting.");
            return;
        }

        try {
            MethodDataList methodList = dexKit.findMethod(
                    FindMethod.create()
                            .matcher(MethodMatcher.create()
                                    .paramTypes("com.instagram.service.session.UserSession")
                                    .returnType(boolean.class)
                                    .addUsingString("DevOptions", StringMatchType.Contains)
                            )
            );

            if (methodList.isEmpty()) {
                XposedBridge.log(TAG + " | No suitable method found.");
                return;
            }

            for (var methodData : methodList) {
                XposedBridge.log(TAG + " | Found method: " + methodData.getDeclaredClassName() + "." + methodData.getName());
            }

            var methodData = methodList.get(0);
            hookDevOptions(methodData.getDeclaredClassName(), methodData.getName(), classLoader);
        } catch (Exception e) {
            XposedBridge.log(TAG + " | Error while handling Dev Options: " + e.getMessage());
        }
    }


    private void hookDevOptions(String className, String methodName, ClassLoader classLoader) {
        try {
            XposedBridge.log(TAG + ": Hooking method " + methodName + " in class " + className);

            Class<?> targetClass = XposedHelpers.findClass(className, classLoader);
            Class<?> userSessionClass = XposedHelpers.findClass("com.instagram.service.session.UserSession", classLoader);

            XposedHelpers.findAndHookMethod(
                    targetClass,
                    methodName,
                    userSessionClass,
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) {
                            XposedBridge.log(TAG + ": Successfully hooked method " + methodName);
                            return true;
                        }
                    }
            );
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Error while hooking method: " + e.getMessage());
        }
    }
}
