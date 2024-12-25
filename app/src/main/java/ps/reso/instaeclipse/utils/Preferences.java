package ps.reso.instaeclipse.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.coniy.fileprefs.FileSharedPreferences;

import ps.reso.instaeclipse.MainActivity;

public class Preferences {
    private static SharedPreferences pref;
    private static SharedPreferences.Editor editor;

    /**
     * Initialize the preferences
     * @param context the application context
     * @return the preferences object
     */
    @SuppressLint("WorldReadableFiles")
    public static SharedPreferences loadPreferences(MainActivity context) {
        try {
            //noinspection deprecation
            pref = context.getSharedPreferences(Utils.PREFS_NAME, Context.MODE_WORLD_READABLE);
        } catch (SecurityException ignored) {
            pref = context.getSharedPreferences(Utils.PREFS_NAME, Context.MODE_PRIVATE);
        }
        FileSharedPreferences.makeWorldReadable(context.getPackageName(), Utils.PREFS_NAME);
        editor = pref.edit();
        return pref;
    }

    /**
     * @return the preferences object
     */
    public static SharedPreferences getPrefs() {
        return pref;
    }

    /**
     * @return the editor object for preferences
     */
    public static SharedPreferences.Editor getEditor() {
        return editor;
    }
}
