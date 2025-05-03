package ps.reso.instaeclipse.mods.misc;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import ps.reso.instaeclipse.Xposed.Module;
import ps.reso.instaeclipse.utils.feature.FeatureFlags;

import java.lang.reflect.Method;
import java.util.List;

public class StoryFlipping {

    public void handleStoryFlippingDisable(DexKitBridge bridge) {
        try {
            findAndHookMethod(bridge);
        } catch (Exception e) {
            XposedBridge.log("(InstaEclipse | StoryFlipping): ❌ Error handling Story Flipping hook: " + e.getMessage());
        }
    }

    private void findAndHookMethod(DexKitBridge bridge) {
        try {
            // Step 1: Find methods referencing the string "end_scene"
            List<MethodData> methods = bridge.findMethod(FindMethod.create()
                    .matcher(MethodMatcher.create()
                            .declaredClass("instagram.features.stories.fragment.ReelViewerFragment")
                            .usingStrings("end_scene")
                    )
            );

            if (methods.isEmpty()) {
                XposedBridge.log("(InstaEclipse | StoryFlipping): ❌ No methods found referencing 'end_scene'.");
                return;
            }

            // Step 2: Hook the correct method
            for (MethodData method : methods) {
                try {
                    Method targetMethod = method.getMethodInstance(Module.hostClassLoader);

                    XposedBridge.hookMethod(targetMethod, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (FeatureFlags.disableStoryFlipping) {
                                // If disableStoryFlipping is enabled, block story flipping
                                param.setResult(null); // Skip original method
                            }
                        }
                    });

                    XposedBridge.log("(InstaEclipse | StoryFlipping): ✅ Hooked (dynamic check): " +
                            method.getClassName() + "." + method.getName());
                    return;

                } catch (Exception e) {
                    XposedBridge.log("(InstaEclipse | StoryFlipping): ❌ Error hooking method: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            XposedBridge.log("(InstaEclipse | StoryFlipping): ❌ Error during dynamic method discovery: " + e.getMessage());
        }
    }
}
