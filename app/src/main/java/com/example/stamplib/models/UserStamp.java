package com.example.stamplib.models;

import com.google.gson.annotations.SerializedName;

public class UserStamp {

    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("stamp_id")
    private int stampId;

    @SerializedName("added_at")
    private String addedAt;

    @SerializedName("note")
    private String note;

    @SerializedName("rating")
    private int rating;

    @SerializedName("condition")
    private String condition;


    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getStampId() {
        return stampId;
    }

    public String getAddedAt() {
        return addedAt;
    }

    public String getNote() {
        return note;
    }

    public int getRating() {
        return rating;
    }

    public String getCondition() {
        return condition;
    }

    public UserStamp(int userId, int stampId) {
        this.userId = userId;
        this.stampId = stampId;
    }
}
