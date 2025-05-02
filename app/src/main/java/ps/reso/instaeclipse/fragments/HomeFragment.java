package ps.reso.instaeclipse.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.List;

import ps.reso.instaeclipse.utils.Contributor;
import ps.reso.instaeclipse.MainActivity;
import ps.reso.instaeclipse.R;
import ps.reso.instaeclipse.utils.Utils;

public class HomeFragment extends Fragment {

    private MaterialButton launchInstagramButton;
    private MaterialButton downloadButton;
    private final boolean hasRootAccess = MainActivity.hasRootAccess;
    private MaterialCardView instagramStatusCard;
    private TextView instagramStatusText;
    private ImageView instagramLogo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        // Initialize views
        launchInstagramButton = view.findViewById(R.id.launch_instagram_button);
        downloadButton = view.findViewById(R.id.download_instagram_button);


        // Find the Card, TextView and Logo to display Instagram status
        instagramStatusCard = view.findViewById(R.id.instagram_status_card);
        instagramStatusText = view.findViewById(R.id.instagram_status_text);
        instagramLogo = view.findViewById(R.id.instagram_logo);

        // Check Instagram installation and version
        checkInstagramStatus();

        // Launch Instagram Button Listener
        launchInstagramButton.setOnClickListener(v -> {
            PackageManager pm = requireContext().getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage("com.instagram.android");
            if (launchIntent != null) {
                startActivity(launchIntent);
            } else {
                Toast.makeText(getActivity(), getString(R.string.not_installed_instagram), Toast.LENGTH_SHORT).show();
            }
        });

        // Download APK Button Logic
        downloadButton.setOnClickListener(v -> {
            String url = "https://www.apkmirror.com/uploads/?appcategory=instagram-instagram";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        // Setup Contributors and Special Thanks
        setupContributorsAndSpecialThanks(view);

        return view;
    }

        @SuppressLint("SetTextI18n")
        private void checkInstagramStatus() {
            String instagramPackage = Utils.IG_PACKAGE_NAME; // IG package name
            PackageManager pm = requireContext().getPackageManager(); // Get PackageManager

            try {
                PackageInfo packageInfo = pm.getPackageInfo(instagramPackage, 0);
                String versionName = packageInfo.versionName;

                instagramStatusText.setText(getString(R.string.installed_instagram_version) + "ver." + " " + versionName);
                instagramStatusCard.setCardBackgroundColor(getResources().getColor(R.color.green));
            instagramLogo.setImageResource(R.drawable.ic_instagram_logo);
            } catch (PackageManager.NameNotFoundException e) {
                instagramStatusText.setText(getString(R.string.not_installed_instagram));
                instagramStatusCard.setCardBackgroundColor(getResources().getColor(R.color.dark_red));
            instagramLogo.setImageResource(R.drawable.ic_cancel);
                launchInstagramButton.setBackgroundColor(android.graphics.Color.parseColor("#262626"));
            } catch (Exception e) {
                instagramStatusText.setText(getString(R.string.error_instagram));
                instagramStatusCard.setCardBackgroundColor(getResources().getColor(R.color.dark_red));
            instagramLogo.setImageResource(R.drawable.ic_error);
                launchInstagramButton.setBackgroundColor(android.graphics.Color.parseColor("#262626"));
            }
        }


    private void setupContributorsAndSpecialThanks(View rootView) {
        LinearLayout contributorsContainer = rootView.findViewById(R.id.contributors_container);
        LinearLayout specialThanksContainer = rootView.findViewById(R.id.special_thanks_container);

        List<Contributor> contributors = Arrays.asList(
                new Contributor("ReSo7200", "https://github.com/ReSo7200", "https://linkedin.com/in/abdalhaleem-altamimi", null),
                new Contributor("frknkrc44", "https://github.com/frknkrc44", null, null),
                new Contributor("BrianML", "https://github.com/brianml31", null, "https://t.me/instamoon_channel"),
		new Contributor("silvzr", "https://github.com/silvzr", null, null)
        );

        List<Contributor> specialThanks = Arrays.asList(
		new Contributor("xHookman", "https://github.com/xHookman", null, null),
                new Contributor("Bluepapilte", null, null, "https://t.me/instasmashrepo"),
		new Contributor("BdrcnAYYDIN", null, null, "https://t.me/BdrcnAYYDIN"),
                new Contributor("Amàzing World", null, null, null)
        );

        for (Contributor contributor : contributors) {
            View contributorView = LayoutInflater.from(getContext()).inflate(R.layout.contributor_card, contributorsContainer, false);
            setupContributorCard(contributorView, contributor);
            contributorsContainer.addView(contributorView);
        }

        for (Contributor thanks : specialThanks) {
            View thanksView = LayoutInflater.from(getContext()).inflate(R.layout.contributor_card, specialThanksContainer, false);
            setupContributorCard(thanksView, thanks);
            specialThanksContainer.addView(thanksView);
        }
    }

    private void setupContributorCard(View view, Contributor contributor) {
        TextView nameTextView = view.findViewById(R.id.contributor_name);
        nameTextView.setText(contributor.getName());

        ImageButton githubButton = view.findViewById(R.id.github_button);
        if (contributor.getGithubUrl() != null) {
            githubButton.setVisibility(View.VISIBLE);
            githubButton.setOnClickListener(v -> openLink(contributor.getGithubUrl()));
        } else {
            githubButton.setVisibility(View.GONE);
        }

        ImageButton linkedinButton = view.findViewById(R.id.linkedin_button);
        if (contributor.getLinkedinUrl() != null) {
            linkedinButton.setVisibility(View.VISIBLE);
            linkedinButton.setOnClickListener(v -> openLink(contributor.getLinkedinUrl()));
        } else {
            linkedinButton.setVisibility(View.GONE);
        }

        ImageButton telegramButton = view.findViewById(R.id.telegram_button);
        if (contributor.getTelegramUrl() != null) {
            telegramButton.setVisibility(View.VISIBLE);
            telegramButton.setOnClickListener(v -> openLink(contributor.getTelegramUrl()));
        } else {
            telegramButton.setVisibility(View.GONE);
        }
    }

    private void openLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }


}
