package ps.reso.instaeclipse.mods;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import ps.reso.instaeclipse.utils.Utils;

/**
 * Handles Ghost Mode for Direct Messages (DM) in Instagram.
 */
public class GhostModeDM {
    public void handleGhostMode(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Load UserSession class
            Class<?> UserSessionClass = XposedHelpers.findClass(Utils.USER_SESSION_CLASS, lpparam.classLoader);

            // Perform dynamic analysis
            performDynamicAnalysis(lpparam, UserSessionClass);
        } catch (Exception e) {
            XposedBridge.log("(InstaEclipse | GhostModeDM): Error handling Ghost Mode: " + e.getMessage());
        }
    }

    private void hookGhostMode(String classToHook, String classAsInput, Class<?> UserSessionClass, String targeted_MethodName, ClassLoader classLoader) {
        try {
            // Find the parameter class dynamically
            Class<?> param_Class = XposedHelpers.findClass(classAsInput, classLoader);

            // Hook the targeted method
            XposedHelpers.findAndHookMethod(
                    classToHook,
                    classLoader,
                    targeted_MethodName,
                    param_Class,
                    UserSessionClass,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    long.class,
                    boolean.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            // Block the "seen" status update
                            param.setResult(null);
                        }
                    }
            );

            XposedBridge.log("(InstaEclipse | GhostModeDM): Successfully hooked method: " + targeted_MethodName + " in class: " + classToHook);
        } catch (Exception e) {
            XposedBridge.log("(InstaEclipse | GhostModeDM): Failed to hook method: " + targeted_MethodName + " in class: " + classToHook + " - " + e.getMessage());
        }
    }

    private void performDynamicAnalysis(XC_LoadPackage.LoadPackageParam lpparam, Class<?> UserSessionClass) {
        String characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String targeted_MethodName;

        // Dynamic search for the appropriate classes and methods
        outerLoop:
        for (char first : characters.toCharArray()) {
            for (char second : characters.toCharArray()) {
                for (char third : characters.toCharArray()) {
                    String classToHook = "X." + first + second + third;

                    try {
                        // Dynamically find the class
                        Class<?> cls = XposedHelpers.findClass(classToHook, lpparam.classLoader);

                        // Iterate through the declared methods of the class
                        for (Method method : cls.getDeclaredMethods()) {
                            try {
                                // Match methods named A00, A01, ..., A09 with 8 parameters
                                if (method.getName().matches("A0[0-9]") && method.getParameterCount() == 8) {
                                    Class<?>[] paramTypes = method.getParameterTypes();

                                    // Validate the parameter types
                                    if (paramTypes[1] == UserSessionClass &&
                                            paramTypes[2] == String.class &&
                                            paramTypes[3] == String.class &&
                                            paramTypes[4] == String.class &&
                                            paramTypes[5] == String.class &&
                                            paramTypes[6] == long.class &&
                                            paramTypes[7] == boolean.class) {

                                        targeted_MethodName = method.getName();
                                        String param_Class = paramTypes[0].getName();

                                        // Hook the matched method
                                        hookGhostMode(classToHook, param_Class, UserSessionClass, targeted_MethodName, lpparam.classLoader);

                                        // XposedBridge.log("(InstaEclipse | GhostModeDM): Hooked method: " + method.getName() + " in class: " + classToHook);
                                        break outerLoop; // Exit the loops once a match is found
                                    }
                                }
                            } catch (NoClassDefFoundError | XposedHelpers.ClassNotFoundError e) {
                                // XposedBridge.log("(InstaEclipse) Skipping method due to missing dependency: " + e.getMessage());
                            } catch (Exception e) {
                                // XposedBridge.log("(InstaEclipse) General exception while inspecting method: " + e.getMessage());
                            }
                        }
                    } catch (NoClassDefFoundError | XposedHelpers.ClassNotFoundError e) {
                        // XposedBridge.log("(InstaEclipse) Skipping class " + classToHook + " due to missing dependency: " + e.getMessage());
                    } catch (Exception e) {
                        // XposedBridge.log("(InstaEclipse) General exception while inspecting class: " + e.getMessage());
                    }
                }
            }
        }
    }


}
