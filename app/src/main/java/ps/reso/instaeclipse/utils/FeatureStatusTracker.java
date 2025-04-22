package ps.reso.instaeclipse.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FeatureStatusTracker {
    private static final Map<String, Boolean> features = Collections.synchronizedMap(new HashMap<>());

    public static void setEnabled(String name) {
        features.put(name, false); // default: not hooked
    }

    public static void setHooked(String name) {
        if (features.containsKey(name)) {
            features.put(name, true);
        }
    }

    public static Map<String, Boolean> getStatus() {
        return features;
    }

    public static boolean hasEnabledFeatures() {
        return !features.isEmpty();
    }

    public static void reset() {
        features.clear();
    }
}
