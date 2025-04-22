package ps.reso.instaeclipse.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CustomToast {

    private static boolean toastShown = false;

    public static void showCustomToast(Context context, String message) {
        if (context == null) {
            XposedBridge.log("❌ CustomToast: Context is null!");
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                TextView toastText = new TextView(context);
                toastText.setText(message);
                toastText.setTextColor(Color.WHITE);
                toastText.setBackgroundColor(Color.parseColor("#CC000000")); // semi-transparent black
                toastText.setPadding(40, 25, 40, 25);
                toastText.setTextSize(16);
                toastText.setGravity(Gravity.CENTER);

                android.widget.Toast toast = new android.widget.Toast(context);
                toast.setView(toastText);
                toast.setDuration(android.widget.Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM, 0, 150);
                toast.show();

            } catch (Throwable t) {
                XposedBridge.log("❌ Failed to show custom toast: " + Log.getStackTraceString(t));
            }
        });
    }

    public static void hookMainActivity(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                    "com.instagram.mainactivity.LauncherActivity",
                    lpparam.classLoader,
                    "onCreate",
                    Bundle.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            if (toastShown) return;
                            toastShown = true;

                            Context context = ((Activity) param.thisObject).getApplicationContext();
                            if (context == null || !FeatureStatusTracker.hasEnabledFeatures()) return;

                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                StringBuilder sb = new StringBuilder("InstaEclipse Loaded 🎯\n");

                                for (Map.Entry<String, Boolean> entry : FeatureStatusTracker.getStatus().entrySet()) {
                                    sb.append(entry.getValue() ? "✅ " : "❌ ").append(entry.getKey()).append("\n");
                                }

                                showCustomToast(context, sb.toString().trim());
                            }, 1000);
                        }
                    }
            );
        } catch (Throwable t) {
            XposedBridge.log("❌ Failed to hook LauncherActivity for toast: " + Log.getStackTraceString(t));
        }
    }
}
