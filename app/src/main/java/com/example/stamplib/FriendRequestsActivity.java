
package com.example.stamplib;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stamplib.adapters.FriendRequestAdapter;
import com.example.stamplib.adapters.FriendRequestAdapter.OnFriendRequestAction;
import com.example.stamplib.models.FriendRelation;
import com.example.stamplib.viewmodel.FriendsViewModel;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class FriendRequestsActivity extends AppCompatActivity {

    private FriendsViewModel viewModel;
    private FriendRequestAdapter incomingAdapter;
    private FriendRequestAdapter outgoingAdapter;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String language = LocaleHelper.getAppLanguage(this);
        LocaleHelper.setLocale(this, language);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);
        getSupportActionBar().hide();

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "Не удалось определить пользователя", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new FriendsViewModel();

        setupIncomingRecyclerView();
        setupOutgoingRecyclerView();
        observeFriendRequests();
    }

    private void setupIncomingRecyclerView() {
        RecyclerView incomingRecyclerView = findViewById(R.id.incomingRecyclerView);
        incomingRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        incomingAdapter = new FriendRequestAdapter(
                new ArrayList<>(),
                this,
                currentUserId,
                false,
                new OnFriendRequestAction() {
                    @Override
                    public void onAccept(FriendRelation request) {
                        viewModel.acceptFriend(currentUserId, request.user_id, () -> {
                            Toast.makeText(FriendRequestsActivity.this, "Заявка принята", Toast.LENGTH_SHORT).show();

                            DatabaseHelper dbHelper = new DatabaseHelper(FriendRequestsActivity.this);
                            dbHelper.syncFriendsFromServer(FriendRequestsActivity.this, currentUserId);

                            viewModel.notifyFriendsRefresh();
                            observeFriendRequests();
                        });
                    }

                    @Override
                    public void onReject(FriendRelation request) {
                        viewModel.rejectFriend(currentUserId, request.user_id, () -> {
                            Toast.makeText(FriendRequestsActivity.this, "Заявка отклонена", Toast.LENGTH_SHORT).show();

                            DatabaseHelper dbHelper = new DatabaseHelper(FriendRequestsActivity.this);
                            dbHelper.syncFriendsFromServer(FriendRequestsActivity.this, currentUserId);

                            viewModel.notifyFriendsRefresh();
                            observeFriendRequests();
                        });
                    }
                }
        );

        incomingRecyclerView.setAdapter(incomingAdapter);
    }

    private void setupOutgoingRecyclerView() {
        RecyclerView outgoingRecyclerView = findViewById(R.id.outgoingRecyclerView);
        outgoingRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        outgoingAdapter = new FriendRequestAdapter(
                new ArrayList<>(),
                this,
                currentUserId,
                true,
                new OnFriendRequestAction() {
                    @Override
                    public void onAccept(FriendRelation request) { }

                    @Override
                    public void onReject(FriendRelation request) {
                        Toast.makeText(FriendRequestsActivity.this, "Заявка отозвана", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        outgoingRecyclerView.setAdapter(outgoingAdapter);
    }

    private void observeFriendRequests() {
        viewModel.getIncomingRequests(currentUserId).observe(this, incomingAdapter::updateData);
        viewModel.getOutgoingRequests(currentUserId).observe(this, outgoingAdapter::updateData);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}
