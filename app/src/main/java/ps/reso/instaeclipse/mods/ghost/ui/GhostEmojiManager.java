package ps.reso.instaeclipse.mods.ghost.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class GhostEmojiManager {

    @SuppressLint("StaticFieldLeak")
    private static TextView ghostEmojiView;

    @SuppressLint("DiscouragedApi")
    public static void addGhostEmojiNextToInbox(Activity activity, boolean showGhost) {
        try {
            int inboxButtonId1 = activity.getResources().getIdentifier("action_bar_inbox_button", "id", activity.getPackageName());
            int inboxButtonId2 = activity.getResources().getIdentifier("direct_tab", "id", activity.getPackageName());

            View inboxButton1 = activity.findViewById(inboxButtonId1);
            View inboxButton2 = activity.findViewById(inboxButtonId2);

            View inboxButton = inboxButton1 != null ? inboxButton1 : inboxButton2;

            if (inboxButton != null) {
                ViewGroup parent = (ViewGroup) inboxButton.getParent();

                if (showGhost) {
                    if (ghostEmojiView != null && ghostEmojiView.getParent() != null) {
                        ((ViewGroup) ghostEmojiView.getParent()).removeView(ghostEmojiView);
                    }
                    if (ghostEmojiView == null || ghostEmojiView.getParent() == null) {
                        ghostEmojiView = new TextView(activity);
                        ghostEmojiView.setText("👻");
                        ghostEmojiView.setTextSize(18);
                        ghostEmojiView.setTextColor(android.graphics.Color.WHITE);
                        ghostEmojiView.setPadding(0, inboxButton.getPaddingTop(), 0, inboxButton.getPaddingBottom());
                        ghostEmojiView.setTranslationY(inboxButton.getTranslationY());

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
