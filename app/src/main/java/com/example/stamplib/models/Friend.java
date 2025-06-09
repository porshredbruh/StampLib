package com.example.stamplib.models;

public class Friend {
    private final String name;
    private final String code;
    private final int friendId;

    public Friend(String name, String code, int friendId) {
        this.name = name;
        this.code = code;
        this.friendId = friendId;
    }

    public String getName() { return name; }
    public String getCode() { return code; }
    public int getFriendId() { return friendId; }
}