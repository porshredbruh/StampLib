
package com.example.stamplib.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.stamplib.models.FriendRelation;
import com.example.stamplib.repository.FriendRepository;

import java.util.List;
import java.util.function.Consumer;

public class FriendsViewModel extends ViewModel {

    private final FriendRepository repository;
    private final MutableLiveData<Boolean> friendListShouldRefresh = new MutableLiveData<>(false);

    public FriendsViewModel() {
        this.repository = new FriendRepository();
    }

    public LiveData<List<FriendRelation>> getFriends(int userId) {
        return repository.getFriends(userId);
    }

    public LiveData<List<FriendRelation>> getIncomingRequests(int userId) {
        return repository.getIncoming(userId);
    }

    public LiveData<List<FriendRelation>> getOutgoingRequests(int userId) {
        return repository.getOutgoing(userId);
    }

    public void acceptFriend(int myId, int fromUserId, Runnable onSuccess) {
        repository.acceptFriend(myId, fromUserId, () -> {
            friendListShouldRefresh.postValue(true);
            onSuccess.run();
        });
    }

    public void rejectFriend(int myId, int fromUserId, Runnable onSuccess) {
        repository.rejectFriend(myId, fromUserId, onSuccess);
    }

    public LiveData<Boolean> getFriendListShouldRefresh() {
        return friendListShouldRefresh;
    }

    public void resetFriendListRefreshFlag() {
        friendListShouldRefresh.setValue(false);
    }

    public void notifyFriendsRefresh() {
        friendListShouldRefresh.setValue(true);
    }

    public void addFriendByCode(int userId, String code, Consumer<Boolean> callback) {
        repository.addFriendByCode(userId, code, callback);
    }

}
