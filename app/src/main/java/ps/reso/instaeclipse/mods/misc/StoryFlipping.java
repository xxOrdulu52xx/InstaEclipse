package ps.reso.instaeclipse.mods.misc;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import ps.reso.instaeclipse.Xposed.Module;

import java.lang.reflect.Method;
import java.util.List;

public class StoryFlipping {
    public void handleStoryFlippingDisable(DexKitBridge bridge) {
        try {
            findAndHookMethod(bridge);
        } catch (Exception e) {
            XposedBridge.log("(InstaEclipse | StoryFlipping): Error handling Story Flipping hook: " + e.getMessage());
        }
    }

    private void findAndHookMethod(DexKitBridge bridge) {
        try {
            // Find methods referencing the string "upsell_impressions"
            List<MethodData> methods = bridge.findMethod(FindMethod.create()
                    .matcher(MethodMatcher.create()
                            .declaredClass("instagram.features.stories.fragment.ReelViewerFragment") // Target class
                            .usingStrings("upsell_impressions") // Look for methods referencing this string
                    )
            );

            if (methods.isEmpty()) {
                XposedBridge.log("(InstaEclipse | StoryFlipping): No methods found referencing 'upsell_impressions'.");
                return;
            }

            // Hook the first matched method
            for (MethodData method : methods) {
                try {
                    Method targetMethod = method.getMethodInstance(Module.hostClassLoader);

                    XposedBridge.hookMethod(targetMethod, XC_MethodReplacement.DO_NOTHING); // Disable the method

                    XposedBridge.log("(InstaEclipse | StoryFlipping): Successfully hooked method: " +
                            method.getClassName() + "." + method.getName());
                    return; // Exit after hooking the first match
                } catch (Exception e) {
                    XposedBridge.log("(InstaEclipse | StoryFlipping): Error hooking method: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            XposedBridge.log("(InstaEclipse | StoryFlipping): Error during dynamic method discovery: " + e.getMessage());
        }
    }
}
