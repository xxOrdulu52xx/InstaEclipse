package ps.reso.instaeclipse.mods.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import ps.reso.instaeclipse.utils.DialogUtils;
import ps.reso.instaeclipse.utils.GhostModeUtils;

public class InstagramUI {
    @SuppressLint("StaticFieldLeak")
    private static Activity currentActivity;
    @SuppressLint("StaticFieldLeak")
    private static TextView ghostEmojiView;


    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    public static void addGhostEmojiNextToInbox(Activity activity, boolean showGhost) {
        try {
            @SuppressLint("DiscouragedApi") int inboxButtonId = activity.getResources().getIdentifier("action_bar_inbox_button", "id", activity.getPackageName());
            View inboxButton = activity.findViewById(inboxButtonId);

            if (inboxButton != null) {
                ViewGroup parent = (ViewGroup) inboxButton.getParent();

                if (showGhost) {
                    if (ghostEmojiView == null || ghostEmojiView.getParent() == null) {
                        ghostEmojiView = new TextView(activity);
                        ghostEmojiView.setText("👻");
                        ghostEmojiView.setTextSize(18);
                        ghostEmojiView.setTextColor(android.graphics.Color.WHITE);
                        ghostEmojiView.setPadding(8, 0, 0, 0);
                        ghostEmojiView.setTranslationY(-65);

                        int index = parent.indexOfChild(inboxButton);
                        parent.addView(ghostEmojiView, index + 1);
                    }
                    ghostEmojiView.setVisibility(View.VISIBLE);
                } else {
                    if (ghostEmojiView != null) {
                        ghostEmojiView.setVisibility(View.GONE);
                    }
                }
            } else {
                XposedBridge.log("InstaEclipse: action_bar_inbox_button not found");
            }
        } catch (Exception ignored) {

        }
    }

    private static void setupHooks(Activity activity) {
        // Hook Search Tab (open InstaEclipse Settings)
        hookLongPress(activity, "search_tab", v -> {
            DialogUtils.showEclipseOptionsDialog(activity);
            vibrate(activity);
            return true;
        }, "search_tab");

        // Hook Inbox Button (toggle Ghost Quick Options)
        hookLongPress(activity, "action_bar_inbox_button", v -> {
            GhostModeUtils.toggleSelectedGhostOptions(activity);
            vibrate(activity);
            return true;
        }, "action_bar_inbox_button");
        addGhostEmojiNextToInbox(activity, GhostModeUtils.isGhostModeActive());
    }

    private static void hookLongPress(Activity activity, String viewName, View.OnLongClickListener listener, String logName) {
        try {
            @SuppressLint("DiscouragedApi") int viewId = activity.getResources().getIdentifier(viewName, "id", activity.getPackageName());
            View view = activity.findViewById(viewId);

            if (view != null) {
                view.setOnLongClickListener(listener);
            }
        } catch (Exception ignored) {
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private static void vibrate(Context context) {
        try {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate(50); // For older Android versions
                }
            }
        } catch (Exception e) {
            XposedBridge.log("InstaEclipse (vibrate error): " + e.getMessage());
        }
    }


    private static boolean isAnyGhostOptionEnabled() {
        return GhostModeUtils.isGhostModeActive();
    }

    public void mainActivity(ClassLoader classLoader) {
        // Hook onCreate of InstagramMainActivity
        XposedHelpers.findAndHookMethod("com.instagram.mainactivity.InstagramMainActivity", classLoader, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                final Activity activity = (Activity) param.thisObject;
                currentActivity = activity; // Save the activity
                activity.runOnUiThread(() -> {
                    try {
                        setupHooks(activity);
                        addGhostEmojiNextToInbox(activity, isAnyGhostOptionEnabled());
                    } catch (Exception ignored) {

                    }
                });
            }
        });

        // Hook onResume
        XposedHelpers.findAndHookMethod("com.instagram.mainactivity.InstagramMainActivity", classLoader, "onResume", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                final Activity activity = (Activity) param.thisObject;
                currentActivity = activity;
                activity.runOnUiThread(() -> {
                    try {
                        setupHooks(activity);
                        addGhostEmojiNextToInbox(activity, isAnyGhostOptionEnabled());
                    } catch (Exception ignored) {
                    }
                });
            }
        });

        XposedHelpers.findAndHookMethod("com.instagram.mainactivity.InstagramMainActivity", classLoader, "getBottomSheetNavigator", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                final Activity activity = (Activity) InstagramUI.getCurrentActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        try {
                            InstagramUI.setupHooks(activity);
                            InstagramUI.addGhostEmojiNextToInbox(activity, GhostModeUtils.isGhostModeActive());
                        } catch (Exception ignored) {
                        }
                    });
                }
            }
        });

    }
}
