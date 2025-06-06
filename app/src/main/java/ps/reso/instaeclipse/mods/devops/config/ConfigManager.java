package ps.reso.instaeclipse.mods.devops.config;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import de.robv.android.xposed.XposedBridge;
import ps.reso.instaeclipse.utils.feature.FeatureFlags;

public class ConfigManager {

    // Import meta config from clipboard
    public static void importConfigFromClipboard(Context context) {
        android.app.ProgressDialog progress = new android.app.ProgressDialog(context);
        progress.setMessage("Importing config...");
        progress.setCancelable(false);
        progress.show();

        new Thread(() -> {
            try {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard == null || !clipboard.hasPrimaryClip()) {
                    return;
                }

                ClipData clipData = clipboard.getPrimaryClip();
                if (clipData == null || clipData.getItemCount() == 0) {
                    return;
                }

                CharSequence clipText = clipData.getItemAt(0).getText();
                if (clipText == null || clipText.length() == 0) {
                    return;
                }

                String json = clipText.toString().trim();
                if (!json.startsWith("{") || !json.endsWith("}")) {
                    return;
                }

                File dest = new File(context.getFilesDir(), "mobileconfig/mc_overrides.json");
                if (!Objects.requireNonNull(dest.getParentFile()).exists()) {
                    dest.getParentFile().mkdirs();
                }

                try (FileOutputStream fos = new FileOutputStream(dest, false)) {
                    fos.write(json.getBytes(StandardCharsets.UTF_8));
                    fos.flush();
                }

                new Handler(Looper.getMainLooper()).post(() -> {
                    progress.dismiss();
                    Toast.makeText(context, "✅ Imported into mc_overrides.json", Toast.LENGTH_LONG).show();
                    XposedBridge.log("InstaEclipse | ✅ JSON imported from clipboard into mc_overrides.json");
                });

            } catch (Exception e) {
                XposedBridge.log("InstaEclipse | ❌ Clipboard import failed: " + e.getMessage());
                new Handler(Looper.getMainLooper()).post(() -> {
                    progress.dismiss();
                    Toast.makeText(context, "❌ Failed to import config", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    // Export meta config to Device
    public static void exportCurrentDevConfig(Context context) {
        if (!FeatureFlags.isExportingConfig) {
            return;
        }
        try {
            File source = new File(context.getFilesDir(), "mobileconfig/mc_overrides.json");
            if (!source.exists()) {
                XposedBridge.log("InstaEclipse | ❌ mc_overrides.json not found.");
                return;
            }

            StringBuilder jsonBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(source))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line).append("\n");
                }
            }

            String jsonContent = jsonBuilder.toString().trim();

            if (!jsonContent.startsWith("{") || !jsonContent.endsWith("}")) {
                XposedBridge.log("InstaEclipse | ❌ mc_overrides.json does not contain valid JSON.");
                return;
            }

            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("json", jsonContent);
                clipboard.setPrimaryClip(clip);
                XposedBridge.log("InstaEclipse | ✅ Copied mc_overrides.json to clipboard.");
            }

        } catch (Exception e) {
            XposedBridge.log("InstaEclipse | ❌ Failed to export config: " + e.getMessage());
        } finally {
            FeatureFlags.isExportingConfig = false;
        }

    }
}
