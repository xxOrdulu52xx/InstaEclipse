package ps.reso.instaeclipse.mods.ghost.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class GhostEmojiManager {

    @SuppressLint("StaticFieldLeak")
    private static TextView ghostEmojiView;

    public static void addGhostEmojiNextToInbox(Activity activity, boolean showGhost) {
        try {
            int inboxButtonId = activity.getResources().getIdentifier("action_bar_inbox_button", "id", activity.getPackageName());
            View inboxButton = activity.findViewById(inboxButtonId);
            int translationY = -65; // default for action_bar_inbox_button

            // If "action_bar_inbox_button" wasn't found, try "direct_tab"
            if (inboxButton == null) {
                inboxButtonId = activity.getResources().getIdentifier("direct_tab", "id", activity.getPackageName());
                inboxButton = activity.findViewById(inboxButtonId);
                translationY = 35;
            }

            if (inboxButton != null) {
                ViewGroup parent = (ViewGroup) inboxButton.getParent();

                if (showGhost) {
                    if (ghostEmojiView == null || ghostEmojiView.getParent() == null) {
                        ghostEmojiView = new TextView(activity);
                        ghostEmojiView.setText("👻");
                        ghostEmojiView.setTextSize(18);
                        ghostEmojiView.setTextColor(android.graphics.Color.WHITE);
                        ghostEmojiView.setPadding(8, 0, 0, 0);
                        ghostEmojiView.setTranslationY(translationY);

                        int index = parent.indexOfChild(inboxButton);
                        parent.addView(ghostEmojiView, index + 1);
                    }
                    ghostEmojiView.setVisibility(View.VISIBLE);
                } else {
                    if (ghostEmojiView != null) {
                        ghostEmojiView.setVisibility(View.GONE);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

}
