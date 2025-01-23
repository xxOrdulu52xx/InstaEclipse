package ps.reso.instaeclipse.mods;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.ClassData;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.util.List;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import ps.reso.instaeclipse.Xposed.Module;


public class DevOptionsEnable {
    public void handleDevOptions(DexKitBridge bridge) {
        try {
            findAndHookDynamicMethod(bridge);
        } catch (Exception e) {
            XposedBridge.log("(InstaEclipse | DevOptionsEnable): Error handling Dev Options: " + e.getMessage());
        }
    }

    private void findAndHookDynamicMethod(DexKitBridge bridge) {
        try {
            // Step 1: Find classes containing "is_employee"
            List<ClassData> classes = bridge.findClass(FindClass.create()
                    .matcher(ClassMatcher.create()
                            .usingStrings("is_employee") // Match classes referencing "is_employee"
                    )
            );

            if (classes.isEmpty()) {
                return;
            }


            // Step 2: Inspect each class for methods referencing "is_employee"
            for (ClassData classData : classes) {
                String className = classData.getName();
                if (!className.startsWith("X.")) {
                    continue; // Skip non-relevant classes
                }

                // Step 3: Locate methods referencing "is_employee"
                List<MethodData> methods = bridge.findMethod(FindMethod.create()
                        .matcher(MethodMatcher.create()
                                .declaredClass(className) // Inspect methods in the current class
                                .usingStrings("is_employee") // Reference "is_employee"
                        )
                );

                if (methods.isEmpty()) {
                    continue;
                }

                for (MethodData method : methods) {

                    // Step 4: Inspect the invoked methods
                    inspectInvokedMethods(method);
                }
            }
        } catch (Exception e) {
            XposedBridge.log("(InstaEclipse | DevOptionsEnable): Error during dynamic method discovery and hooking: " + e.getMessage());
        }
    }

    private void inspectInvokedMethods(MethodData method) {
        try {
            // Step 1: Get all methods invoked by the current method
            List<MethodData> invokedMethods = method.getInvokes(); // Access the invoked methods directly.

            if (invokedMethods.isEmpty()) {
                return;
            }

            for (MethodData invokedMethod : invokedMethods) {

                boolean returnTypeMatch = String.valueOf(invokedMethod.getReturnType()).contains("boolean");

                // Directly check if the parameter type matches "com.instagram.common.session.UserSession"
                boolean paramTypesMatch = false;
                if (!invokedMethod.getParamTypes().isEmpty()) {
                    String rawParamTypeName = String.valueOf(invokedMethod.getParamTypes().get(0)); // Get raw param type

                    // Simplify the check by matching the expected type directly
                    if (rawParamTypeName.contains("com.instagram.common.session.UserSession")) {
                        paramTypesMatch = true;
                    }
                }

                if (returnTypeMatch && paramTypesMatch) {

                    // Hook the target method
                    try {
                        Method targetMethod = invokedMethod.getMethodInstance(Module.hostClassLoader);
                        XposedBridge.hookMethod(targetMethod, XC_MethodReplacement.returnConstant(true));
                        XposedBridge.log("(InstaEclipse | DevOptionsEnable): Successfully hooked target method: " +
                                invokedMethod.getClassName() + "." + invokedMethod.getName());
                    } catch (Exception e) {
                        XposedBridge.log("(InstaEclipse | DevOptionsEnable): Error hooking method: " + e.getMessage());
                    }
                    return; // Exit after hooking the target
                }
            }


        } catch (Exception e) {
            XposedBridge.log("(InstaEclipse | DevOptionsEnable): Error inspecting invoked methods: " + e.getMessage());
        }
    }
}