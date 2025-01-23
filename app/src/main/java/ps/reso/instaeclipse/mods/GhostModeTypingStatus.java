package ps.reso.instaeclipse.mods;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class GhostModeTypingStatus {

    public void handleTypingStatus(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            performDynamicAnalysisForTypingStatus(lpparam);
        } catch (Exception e) {
            XposedBridge.log("GhostModeTypingStatus Error handling typing status: " + e.getMessage());
        }
    }


    private void hookGhostTypingStatus(String classToHook, ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                    classToHook,
                    classLoader,
                    "onTextChanged",
                    CharSequence.class, int.class, int.class, int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            CharSequence charSequence = (CharSequence) param.args[0];
                            int i = (int) param.args[1];
                            int i2 = (int) param.args[2];
                            int i3 = (int) param.args[3];

                            // Add your custom logic here
                            if (charSequence.length() > 0 || i != 0 || i2 != 0 || i3 != 0) {

                                // Prevent original typing status logic from running
                                XposedBridge.log("Typing ghost mode activated: Blocking typing status.");
                                param.setResult(null); // Prevent original logic from executing

                                XposedHelpers.findAndHookMethod("X.10S", classLoader, "FVN", boolean.class, boolean.class, new XC_MethodHook() {
                                    @Override
                                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                                        param.args[0] = true; // Example: Always set first param to true
                                        param.args[1] = true; // Example: Always set second param to true
                                    }
                                    @Override
                                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    }
                                });
                            }
                            else{
                                XposedHelpers.findAndHookMethod("X.10S", classLoader, "FVN", boolean.class, boolean.class, new XC_MethodHook() {
                                    @Override
                                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                                        param.args[0] = false; // Example: Always set first param to true
                                        param.args[1] = false; // Example: Always set second param to true
                                    }
                                    @Override
                                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    }
                                });
                            }
                        }
                    }
            );
        } catch (Exception e) {
            XposedBridge.log("(GhostModeTypingStatus) Failed to hook onTextChanged in class: " + classToHook + " - " + e.getMessage());
        }
    }

    private void performDynamicAnalysisForTypingStatus(XC_LoadPackage.LoadPackageParam lpparam) {

        String characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        // Iterate through potential class names
        for (char first : characters.toCharArray()) {
            for (char second : characters.toCharArray()) {
                for (char third : characters.toCharArray()) {
                    String classToHook = "X." + first + second + third;

                    try {
                        // Dynamically find the class
                        Class<?> targetedClass = XposedHelpers.findClass(classToHook, lpparam.classLoader);

                        // Check if the class contains the required methods and fields
                        boolean hasOnTextChanged = false;
                        boolean hasBeforeTextChanged = false;
                        boolean hasAfterTextChanged = false;
                        boolean hasRequiredFields;
                        boolean hasListAdapterField = false;
                        boolean hasXField = false;

                        // Check methods
                        for (Method method : targetedClass.getDeclaredMethods()) {
                            String methodName = method.getName();
                            Class<?>[] paramTypes = method.getParameterTypes();

                            if (methodName.equals("onTextChanged") &&
                                    paramTypes.length == 4 &&
                                    paramTypes[0] == CharSequence.class &&
                                    paramTypes[1] == int.class &&
                                    paramTypes[2] == int.class &&
                                    paramTypes[3] == int.class) {
                                hasOnTextChanged = true;
                            } else if (methodName.equals("beforeTextChanged") &&
                                    paramTypes.length == 4 &&
                                    paramTypes[0] == CharSequence.class &&
                                    paramTypes[1] == int.class &&
                                    paramTypes[2] == int.class &&
                                    paramTypes[3] == int.class) {
                                hasBeforeTextChanged = true;
                            } else if (methodName.equals("afterTextChanged") &&
                                    paramTypes.length == 1 &&
                                    paramTypes[0].getName().equals("android.text.Editable")) {
                                hasAfterTextChanged = true;
                            }
                        }

                        // Check fields
                        for (Field field : targetedClass.getDeclaredFields()) {
                            if (field.getType().getName().equals("android.widget.ListAdapter")) {
                                hasListAdapterField = true;
                            }
                            if (field.getType().getName().startsWith("X.")) {
                                hasXField = true;
                            }
                        }

                        hasRequiredFields = hasListAdapterField && hasXField;


                        // If all conditions are met, hook the class
                        if (hasOnTextChanged && hasBeforeTextChanged && hasAfterTextChanged && hasRequiredFields) {
                            hookGhostTypingStatus(classToHook, lpparam.classLoader);

                            XposedBridge.log("(GhostModeTypingStatus) Successfully hooked onTextChanged in class: " + classToHook);
                            return; // Exit once the class is found and hooked
                        }
                    } catch (NoClassDefFoundError | XposedHelpers.ClassNotFoundError e) {
                        // Skip classes with missing dependencies
                    } catch (Exception e) {
                        //XposedBridge.log("(GhostModeTypingStatus) Error inspecting class: " + classToHook + " - " + e.getMessage());
                    }
                }
            }
        }

        // Log if no match is found
        XposedBridge.log("(GhostModeTypingStatus) No matching class found for onTextChanged.");
    }




}
