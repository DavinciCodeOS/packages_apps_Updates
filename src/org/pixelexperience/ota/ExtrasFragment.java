package org.pixelexperience.ota;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import org.pixelexperience.ota.misc.Utils;
import org.pixelexperience.ota.model.MaintainerInfo;
import org.pixelexperience.ota.model.UpdateInfo;

import java.util.ArrayList;
import java.util.Objects;

public class ExtrasFragment extends Fragment {

    private final UpdatesActivity updater;
    private View mainView;
    private LinearLayout maintainersLayout;
    private ExtraCardView donateCard;
    private ExtraCardView forumCard;
    private ExtraCardView websiteCard;
    private ExtraCardView newsCard;
    private ExtraCardView prereleaseToggleCard;

    public ExtrasFragment(UpdatesActivity updater) {
        this.updater = updater;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.extras_fragment, container, false);
        maintainersLayout = mainView.findViewById(R.id.maintainers);
        donateCard = mainView.findViewById(R.id.donate_card);
        forumCard = mainView.findViewById(R.id.forum_card);
        websiteCard = mainView.findViewById(R.id.website_card);
        newsCard = mainView.findViewById(R.id.news_card);
        prereleaseToggleCard = mainView.findViewById(R.id.prerelease_card);
        return mainView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    void updatePrefs(UpdateInfo update) {
        Log.d("ExtrasFragment:updatePrefs", "called");

        prereleaseToggleCard.setOnClickListener(v -> showReleaseChannelDialog());
        prereleaseToggleCard.setClickable(true);
        prereleaseToggleCard.setVisibility(View.VISIBLE);

        if (update == null) {
            Log.d("ExtrasFragment:updatePrefs", "update is null");
            maintainersLayout.removeAllViews();
            donateCard.setVisibility(View.GONE);
            forumCard.setVisibility(View.GONE);
            websiteCard.setVisibility(View.GONE);
            newsCard.setVisibility(View.GONE);
            return;
        }

        ArrayList<MaintainerInfo> maintainers = update.getMaintainers();
        if (maintainers != null && !maintainers.isEmpty()) {
            maintainersLayout.removeAllViews();
            for (MaintainerInfo maintainer : maintainers) {
                ExtraCardView maintainerCard = createMaintainerCard(getActivity());
                maintainerCard.setSummary(maintainer.getName());
                maintainerCard.setOnClickListener(v -> openUrl(Utils.getMaintainerURL(maintainer.getUsername())));
                maintainerCard.setClickable(true);
                maintainersLayout.addView(maintainerCard);
            }
        }

        if (update.getDonateUrl() != null && !update.getDonateUrl().isEmpty()) {
            donateCard.setOnClickListener(v -> openUrl(update.getDonateUrl()));
            donateCard.setClickable(true);
            donateCard.setVisibility(View.VISIBLE);
        }

        if (update.getForumUrl() != null && !update.getForumUrl().isEmpty()) {
            forumCard.setOnClickListener(v -> openUrl(update.getForumUrl()));
            forumCard.setClickable(true);
            forumCard.setVisibility(View.VISIBLE);
        }

        if (update.getWebsiteUrl() != null && !update.getWebsiteUrl().isEmpty()) {
            websiteCard.setOnClickListener(v -> openUrl(update.getWebsiteUrl()));
            websiteCard.setClickable(true);
            websiteCard.setVisibility(View.VISIBLE);
        }

        if (update.getNewsUrl() != null && !update.getNewsUrl().isEmpty()) {
            newsCard.setOnClickListener(v -> openUrl(update.getNewsUrl()));
            newsCard.setClickable(true);
            newsCard.setVisibility(View.VISIBLE);
        }
    }

    private void showReleaseChannelDialog() {
        Activity context = requireActivity();
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                "updates_preferences", Context.MODE_PRIVATE);
        String release_channel = Utils.getReleaseChannel(sharedPrefs);
        int checkedItem;

        if (release_channel.equals("dcos_stable")) {
            checkedItem = 0;
        } else if (release_channel.equals("dcos_pre")) {
            checkedItem = 1;
        } else if (release_channel.equals("dcosx_stable")) {
            checkedItem = 2;
        } else {
            checkedItem = 3;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.release_channel_info_title)
                .setSingleChoiceItems(R.array.release_channel_options, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String release_channel;

                        if (i == 0) {
                            release_channel = "dcos_stable";
                        } else if (i == 1) {
                            release_channel = "dcos_pre";
                        } else if (i == 2) {
                            release_channel = "dcosx_stable";
                        } else {
                            release_channel = "dcosx_pre";
                        }

                        Utils.setReleaseChannel(sharedPrefs, release_channel);
                        updater.downloadUpdatesList(true);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private ExtraCardView createMaintainerCard(Context context) {
        ExtraCardView card = new ExtraCardView(context);
        card.setTitle(getString(R.string.maintainer_info_title));
        card.setImage(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_maintainers_icon));
        card.setCardBackgroundColor(getResources().getColor(R.color.cardview_background, context.getTheme()));
        card.setRadius(getResources().getDimension(R.dimen.extra_card_corner_radius));
        card.setCardElevation(getResources().getDimension(R.dimen.extra_card_elevation));
        int padding = (int) getResources().getDimension(R.dimen.extra_card_content_padding);
        card.setContentPadding(padding, padding, padding, padding);
        int extraMargin = (int) getResources().getDimension(R.dimen.extra_card_layout_margin);
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        buttonLayoutParams.setMargins(extraMargin, extraMargin, extraMargin, extraMargin);
        card.setLayoutParams(buttonLayoutParams);
        return card;
    }

    private void showSnackbar(int stringId, int duration) {
        Snackbar.make(requireActivity().findViewById(R.id.main_container), stringId, duration).show();
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception ex) {
            showSnackbar(R.string.error_open_url, Snackbar.LENGTH_SHORT);
        }
    }
}