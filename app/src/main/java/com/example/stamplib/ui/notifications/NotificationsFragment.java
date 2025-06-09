package com.example.stamplib.ui.notifications;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.stamplib.AccountSettingsActivity;
import com.example.stamplib.DatabaseHelper;
import com.example.stamplib.LoginActivity;
import com.example.stamplib.MyCollectionActivity;
import com.example.stamplib.MyFriendsActivity;
import com.example.stamplib.R;
import com.example.stamplib.RegisterActivity;
import com.example.stamplib.models.FriendRelation;
import com.example.stamplib.viewmodel.FriendsViewModel;

import java.util.List;

public class NotificationsFragment extends Fragment {

    private LinearLayout loginLayout, collectionLayout;
    private Button loginButton, registerButton, collectionButton, friendsButton;
    private ImageButton settingsBtn;
    private TextView nicknameText, userCodeText, stampsCollectedText, seriesCollectedText, friendsCountText;

    private FriendsViewModel friendsViewModel;
    private int userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        loginLayout = root.findViewById(R.id.loginLayout);
        collectionLayout = root.findViewById(R.id.collectionLayout);
        loginButton = root.findViewById(R.id.loginButton);
        registerButton = root.findViewById(R.id.registerButton);
        settingsBtn = root.findViewById(R.id.settingsBtn);
        collectionButton = root.findViewById(R.id.collectionButton);
        friendsButton = root.findViewById(R.id.friendsButton);
        nicknameText = root.findViewById(R.id.nicknameText);
        userCodeText = root.findViewById(R.id.userCodeText);
        stampsCollectedText = root.findViewById(R.id.stampsCollectedText);
        seriesCollectedText = root.findViewById(R.id.seriesCollectedText);
        friendsCountText = root.findViewById(R.id.friendsCountText);

        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        boolean loggedIn = prefs.getBoolean("logged_in", false);
        userId = prefs.getInt("user_id", -1);

        friendsViewModel = new ViewModelProvider(requireActivity()).get(FriendsViewModel.class);

        if (loggedIn && userId != -1) {
            loginLayout.setVisibility(View.GONE);
            collectionLayout.setVisibility(View.VISIBLE);
            settingsBtn.setVisibility(View.VISIBLE);

            String nickname = prefs.getString("nickname", "");
            String code = prefs.getString("unic_code", "");

            nicknameText.setText(nickname);
            userCodeText.setText("#" + code);

            updateCollectionStats(userId);
            subscribeToFriends(userId);
        } else {
            loginLayout.setVisibility(View.VISIBLE);
            collectionLayout.setVisibility(View.GONE);
            settingsBtn.setVisibility(View.GONE);
        }

        loginButton.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), LoginActivity.class)));

        registerButton.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), RegisterActivity.class)));

        collectionButton.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MyCollectionActivity.class)));

        friendsButton.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MyFriendsActivity.class)));

        settingsBtn.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AccountSettingsActivity.class)));

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        boolean loggedIn = prefs.getBoolean("logged_in", false);
        userId = prefs.getInt("user_id", -1);

        if (loggedIn && userId != -1) {
            updateCollectionStats(userId);
            updateNickname();
        }
    }

    private void updateNickname() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String nickname = prefs.getString("nickname", "");
        nicknameText.setText(nickname);
    }

    private void updateCollectionStats(int userId) {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());

        int stampsCollected = dbHelper.getCollectedStampsCount(userId);
        int totalStamps = dbHelper.getTotalStampsCount();
        int seriesCollected = dbHelper.getFullyCollectedSeriesCount(userId);
        int totalSeries = dbHelper.getTotalSeriesCount();

        stampsCollectedText.setText(stampsCollected + "/" + totalStamps + " " + getString(R.string.stamps_collected));
        seriesCollectedText.setText(seriesCollected + "/" + totalSeries + " " + getString(R.string.series_collected));
    }

    private void subscribeToFriends(int userId) {
        friendsViewModel.getFriends(userId).observe(getViewLifecycleOwner(), this::updateFriendCount);
        friendsViewModel.getFriendListShouldRefresh().observe(getViewLifecycleOwner(), shouldRefresh -> {
            if (shouldRefresh != null && shouldRefresh) {
                friendsViewModel.getFriends(userId).observe(getViewLifecycleOwner(), this::updateFriendCount);
                friendsViewModel.resetFriendListRefreshFlag();
            }
        });
    }

    private void updateFriendCount(List<FriendRelation> friendRelations) {
        if (friendRelations != null) {
            int remoteCount = friendRelations.size();

            DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
            int localCount = dbHelper.getFriendsCount();

            if (remoteCount != localCount) {
                dbHelper.syncFriendsFromServer(requireContext(), userId);
            }

            friendsCountText.setText(remoteCount + " " + getString(R.string.friends_count));
        } else {
            friendsCountText.setText("0 " + getString(R.string.friends_count));
        }
    }
}
