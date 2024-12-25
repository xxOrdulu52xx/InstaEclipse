package ps.reso.instaeclipse.Xposed;

import android.os.Environment;

import java.io.File;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import ps.reso.instaeclipse.utils.Preferences;
import ps.reso.instaeclipse.utils.Utils;

public class XposedPreferences extends Preferences {
    private static XSharedPreferences pref;

    private static XSharedPreferences getPref() {
        XSharedPreferences pref = new XSharedPreferences(Utils.MY_PACKAGE_NAME, Utils.PREFS_NAME);
        return pref.getFile().canRead() ? pref : null;
    }

    private static XSharedPreferences getLegacyPrefs() {
        File f = new File(Environment.getDataDirectory(), "data/" + Utils.MY_PACKAGE_NAME + "/shared_prefs/" + Utils.PREFS_NAME + ".xml");
        return new XSharedPreferences(f);
    }

    public static void loadPreferences() {
        if (XposedBridge.getXposedVersion() < 93) {
            pref = getLegacyPrefs();
        } else {
            pref = getPref();
        }

        if (pref != null) {
            pref.reload();
        } else {
            XposedBridge.log("Can't load preferences in the module.");
        }
    }

    public static void reloadPrefs() {
        if (pref != null) pref.reload();
    }

    public static XSharedPreferences getPrefs() {
        return pref;
    }
}
