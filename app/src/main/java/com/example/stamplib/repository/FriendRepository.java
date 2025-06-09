package com.example.stamplib.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.stamplib.models.ApiResponse;
import com.example.stamplib.models.FriendRelation;
import com.example.stamplib.network.ApiClient;
import com.example.stamplib.network.ApiService;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendRepository {

    private final ApiService api;

    public FriendRepository() {
        this.api = ApiClient.getService();
    }

    public LiveData<List<FriendRelation>> getFriends(int userId) {
        MutableLiveData<List<FriendRelation>> data = new MutableLiveData<>();
        api.getFriends(userId).enqueue(new Callback<List<FriendRelation>>() {
            @Override
            public void onResponse(Call<List<FriendRelation>> call, Response<List<FriendRelation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body());
                } else {
                    data.setValue(Collections.emptyList());
                }
            }

            @Override
            public void onFailure(Call<List<FriendRelation>> call, Throwable t) {
                data.setValue(Collections.emptyList());
            }
        });
        return data;
    }

    public LiveData<List<FriendRelation>> getIncoming(int userId) {
        MutableLiveData<List<FriendRelation>> data = new MutableLiveData<>();
        api.getIncomingRequests(userId).enqueue(new Callback<List<FriendRelation>>() {
            @Override
            public void onResponse(Call<List<FriendRelation>> call, Response<List<FriendRelation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body());
                } else {
                    data.setValue(Collections.emptyList());
                }
            }

            @Override
            public void onFailure(Call<List<FriendRelation>> call, Throwable t) {
                data.setValue(Collections.emptyList());
            }
        });
        return data;
    }

    public LiveData<List<FriendRelation>> getOutgoing(int userId) {
        MutableLiveData<List<FriendRelation>> data = new MutableLiveData<>();
        api.getOutgoingRequests(userId).enqueue(new Callback<List<FriendRelation>>() {
            @Override
            public void onResponse(Call<List<FriendRelation>> call, Response<List<FriendRelation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body());
                } else {
                    data.setValue(Collections.emptyList());
                }
            }

            @Override
            public void onFailure(Call<List<FriendRelation>> call, Throwable t) {
                data.setValue(Collections.emptyList());
            }
        });
        return data;
    }

    public void acceptFriend(int userId, int fromUserId, Runnable onSuccess) {
        api.acceptRequest(userId, fromUserId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && onSuccess != null) {
                    onSuccess.run();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {}
        });
    }


    public void rejectFriend(int userId, int fromUserId, Runnable onSuccess) {
        api.rejectRequest(userId, fromUserId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && onSuccess != null) {
                    onSuccess.run();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {}
        });
    }

    public void addFriendByCode(int currentUserId, String code, Consumer<Boolean> callback) {
        api.getUserByCode(code).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int friendId = response.body().get("user_id").getAsInt();
                    if (friendId == currentUserId) {
                        callback.accept(false);
                        return;
                    }

                    api.addFriend(currentUserId, friendId).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> resp) {
                            callback.accept(resp.isSuccessful());
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            callback.accept(false);
                        }
                    });

                } else {
                    callback.accept(false);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.accept(false);
            }
        });
    }
}
