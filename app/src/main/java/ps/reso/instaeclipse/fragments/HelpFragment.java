package ps.reso.instaeclipse.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ps.reso.instaeclipse.R;

public class HelpFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate the layout once
        View view = inflater.inflate(R.layout.fragment_help, container, false);

        // Find the GitHub button
        ImageButton githubButton = view.findViewById(R.id.github_button);

        // Find the Telegram button
        ImageButton telegramButton = view.findViewById(R.id.telegram_button);


        // Set the click listener for the GitHub button
        githubButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ReSo7200/InstaEclipse/tree/main"));
            startActivity(intent);
        });

        // Set the click listener for the Telegram button
        telegramButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/InstaEclipse"));
            startActivity(intent);
        });

        return view;
    }
}
