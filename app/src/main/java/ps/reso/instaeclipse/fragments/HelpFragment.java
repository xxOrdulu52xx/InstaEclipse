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

        // Set the click listener for the GitHub button
        githubButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ReSo7200/InstaEclipse/tree/main"));
            startActivity(intent);
        });

        return view;
    }
}
