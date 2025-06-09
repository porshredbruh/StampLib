package com.example.stamplib.models;

import com.google.gson.annotations.SerializedName;

public class FriendRelation {
    public int id;
    public int user_id;
    public int friend_id;
    public boolean is_confirmed;

    @SerializedName(value = "nickname", alternate = {"username"})
    public String nickname;

    @SerializedName("unic_code")
    public String unic_code;

    public String created_at;
}