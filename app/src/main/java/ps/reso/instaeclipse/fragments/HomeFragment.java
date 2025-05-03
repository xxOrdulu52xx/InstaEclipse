package ps.reso.instaeclipse.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
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

import java.util.Arrays;
import java.util.List;

import ps.reso.instaeclipse.utils.core.Contributor;
import ps.reso.instaeclipse.R;
import ps.reso.instaeclipse.utils.core.CommonUtils;

public class HomeFragment extends Fragment {

    private MaterialButton launchInstagramButton;
    private MaterialCardView instagramStatusCard;
    private TextView instagramStatusText;
    private ImageView instagramLogo, instagramInfoIcon;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        // Initialize views
        launchInstagramButton = view.findViewById(R.id.launch_instagram_button);
        MaterialButton downloadButton = view.findViewById(R.id.download_instagram_button);


        // Find the Card, TextView and Logo to display Instagram status
        instagramStatusCard = view.findViewById(R.id.instagram_status_card);
        instagramStatusText = view.findViewById(R.id.instagram_status_text);
        instagramLogo = view.findViewById(R.id.instagram_logo);
        instagramInfoIcon = view.findViewById(R.id.instagram_info_icon);


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
            String instagramPackage = CommonUtils.IG_PACKAGE_NAME; // IG package name
            PackageManager pm = requireContext().getPackageManager(); // Get PackageManager

            try {
                PackageInfo packageInfo = pm.getPackageInfo(instagramPackage, 0);
                String versionName = packageInfo.versionName;

                String installedText = getString(R.string.installed_instagram_version);
                String versionText = getString(R.string.instagram_version) + ": " + versionName;
                String fullText = installedText + "\n" + versionText;

                SpannableString spannableString = new SpannableString(fullText);

                spannableString.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, installedText.length(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                int versionStartIndex = installedText.length() + 1;
                spannableString.setSpan(new android.text.style.RelativeSizeSpan(0.85f), versionStartIndex, fullText.length(), 0);

                instagramStatusText.setText(spannableString);
                instagramStatusCard.setCardBackgroundColor(getResources().getColor(R.color.green));
                instagramLogo.setImageResource(R.drawable.ic_instagram_logo);

                // Add OnClickListener to open app settings if Instagram is installed
                instagramInfoIcon.setOnClickListener(v -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + instagramPackage));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                });


            } catch (PackageManager.NameNotFoundException e) {
                instagramStatusText.setText(getString(R.string.not_installed_instagram));
                instagramStatusText.setTypeface(null, android.graphics.Typeface.BOLD);
                instagramStatusCard.setCardBackgroundColor(getResources().getColor(R.color.dark_red));
                instagramLogo.setImageResource(R.drawable.ic_cancel);
                launchInstagramButton.setBackgroundColor(android.graphics.Color.parseColor("#262626"));

            } catch (Exception e) {
                instagramStatusText.setText(getString(R.string.error_instagram));
                instagramStatusText.setTypeface(null, android.graphics.Typeface.BOLD);
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
        nameTextView.setText(contributor.name());

        ImageButton githubButton = view.findViewById(R.id.github_button);
        if (contributor.githubUrl() != null) {
            githubButton.setVisibility(View.VISIBLE);
            githubButton.setOnClickListener(v -> openLink(contributor.githubUrl()));
        } else {
            githubButton.setVisibility(View.GONE);
        }

        ImageButton linkedinButton = view.findViewById(R.id.linkedin_button);
        if (contributor.linkedinUrl() != null) {
            linkedinButton.setVisibility(View.VISIBLE);
            linkedinButton.setOnClickListener(v -> openLink(contributor.linkedinUrl()));
        } else {
            linkedinButton.setVisibility(View.GONE);
        }

        ImageButton telegramButton = view.findViewById(R.id.telegram_button);
        if (contributor.telegramUrl() != null) {
            telegramButton.setVisibility(View.VISIBLE);
            telegramButton.setOnClickListener(v -> openLink(contributor.telegramUrl()));
        } else {
            telegramButton.setVisibility(View.GONE);
        }
    }

    private void openLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }


}
