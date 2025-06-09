package com.example.stamplib.network;
import com.example.stamplib.models.AnalyzeResponse;
import com.example.stamplib.models.ApiResponse;
import com.example.stamplib.models.Article;
import com.example.stamplib.models.FriendRelation;
import com.example.stamplib.models.FriendStats;
import com.example.stamplib.models.UserStamp;
import com.google.gson.JsonObject;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    @FormUrlEncoded
    @POST("register_user")
    Call<JsonObject> registerUser(
            @Field("nicname") String nickname,
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("login_user")
    Call<JsonObject> loginUser(
            @Field("email") String email,
            @Field("password") String password
    );

    @GET("user_stamps/{user_id}")
    Call<List<UserStamp>> getUserStamps(@Path("user_id") int userId);

    @FormUrlEncoded
    @POST("user_stamps_add/")
    Call<Void> addUserStamp(
            @Field("user_id") int userId,
            @Field("stamp_id") long stampId,
            @Field("note") String note,
            @Field("rating") int rating,
            @Field("condition") String condition
    );

    @FormUrlEncoded
    @POST("user_stamps_remove/")
    Call<Void> removeUserStamp(
            @Field("user_id") int userId,
            @Field("stamp_id") long stampId
    );

    @GET("user_by_code/{unic_code}")
    Call<JsonObject> getUserByCode(@Path("unic_code") String code);

    @FormUrlEncoded
    @POST("add_friend")
    Call<Void> addFriend(
            @Field("user_id") int userId,
            @Field("friend_id") int friendId
    );

    @FormUrlEncoded
    @POST("friend_requests/accept")
    Call<Void> acceptFriendRequest(
            @Field("user_id") int userId,
            @Field("from_user_id") int fromUserId
    );

    @FormUrlEncoded
    @POST("friend_requests/reject")
    Call<Void> rejectFriendRequest(
            @Field("user_id") int userId,
            @Field("from_user_id") int fromUserId
    );


    @POST("accept_friend")
    @FormUrlEncoded
    Call<Void> acceptFriend(
            @Field("user_id") int userId,
            @Field("friend_id") int friendId
    );

    @POST("remove_friend")
    @FormUrlEncoded
    Call<Void> removeFriend(
            @Field("user_id") int userId,
            @Field("friend_id") int friendId
    );

    @GET("friend_requests/{user_id}")
    Call<List<FriendRelation>> getFriendRequests(@Path("user_id") int userId);

    @GET("friend_requests/outgoing/{user_id}")
    Call<List<FriendRelation>> getOutgoingFriendRequests(@Path("user_id") int userId);

    @FormUrlEncoded
    @POST("friend_requests/cancel")
    Call<Void> cancelOutgoingRequest(
            @Field("user_id") int userId,
            @Field("friend_id") int friendId
    );

    @GET("user_by_id/{user_id}")
    Call<JsonObject> getUserById(@Path("user_id") int userId);

    @Multipart
    @POST("/analyze_stamp_photo")
    Call<AnalyzeResponse> uploadStampPhoto(@Part MultipartBody.Part file);

    @GET("friend_requests/{user_id}")
    Call<List<FriendRelation>> getIncomingRequests(@Path("user_id") int userId);

    @GET("friend_requests/outgoing/{user_id}")
    Call<List<FriendRelation>> getOutgoingRequests(@Path("user_id") int userId);

    @FormUrlEncoded
    @POST("friend_requests/accept")
    Call<ApiResponse> acceptRequest(@Field("user_id") int myId, @Field("from_user_id") int fromUserId);

    @FormUrlEncoded
    @POST("friend_requests/reject")
    Call<ApiResponse> rejectRequest(@Field("user_id") int myId, @Field("from_user_id") int fromUserId);

    @GET("friends/{user_id}")
    Call<List<FriendRelation>> getFriends(@Path("user_id") int userId);

    @GET("user_stats/{user_id}")
    Call<FriendStats> getUserStats(@Path("user_id") int userId);

    @POST("update_nicname")
    Call<Void> updateNickname(@Body JsonObject body);

    @GET("export_articles")
    Call<List<Article>> getArticles();

}

