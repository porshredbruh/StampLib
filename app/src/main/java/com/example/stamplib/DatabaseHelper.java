package com.example.stamplib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.stamplib.models.Friend;
import com.example.stamplib.models.FriendRelation;
import com.example.stamplib.network.ApiClient;
import com.example.stamplib.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DB_NAME = "stamps-mark.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSeriesTable = "CREATE TABLE series (" +
                "id INTEGER PRIMARY KEY, " +
                "title TEXT, " +
                "year INTEGER, " +
                "image_path TEXT);";

        String createStampsTable = "CREATE TABLE stamps (" +
                "id INTEGER PRIMARY KEY, " +
                "series_id INTEGER REFERENCES series(id), " +
                "period TEXT, " +
                "name TEXT, " +
                "year INTEGER, " +
                "print_type TEXT, " +
                "perforation_type TEXT, " +
                "perforation_value TEXT, " +
                "paper TEXT, " +
                "watermark TEXT, " +
                "image_path TEXT);";

        String createUserStampsTable = "CREATE TABLE user_stamps (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "stamp_id INTEGER, " +
                "added_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "note TEXT, " +
                "rating INTEGER, " +
                "condition TEXT);";

        String createFriendsTable = "CREATE TABLE friends (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "friend_id INTEGER NOT NULL, " +
                "unic_code TEXT," +
                "nickname TEXT," +
                "is_confirmed INTEGER DEFAULT 0, " +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE (user_id, friend_id)" +
                ");";

        String createArticlesTable = "CREATE TABLE IF NOT EXISTS articles (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title_ru TEXT NOT NULL, " +
                "content_ru TEXT NOT NULL, " +
                "title_en TEXT NOT NULL, " +
                "content_en TEXT NOT NULL, " +
                "published_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

        db.execSQL(createArticlesTable);
        db.execSQL(createSeriesTable);
        db.execSQL(createStampsTable);
        db.execSQL(createUserStampsTable);
        db.execSQL(createFriendsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS friends");
        db.execSQL("DROP TABLE IF EXISTS user_stamps");
        db.execSQL("DROP TABLE IF EXISTS stamps");
        db.execSQL("DROP TABLE IF EXISTS series");
        db.execSQL("DROP TABLE IF EXISTS articles");
        onCreate(db);
    }



    public boolean isStampCollected(int userId, long stampId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM user_stamps WHERE user_id = ? AND stamp_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(stampId)});
        boolean collected = cursor.moveToFirst();
        cursor.close();
        return collected;
    }

    public void addStampToUser(int userId, long stampId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("stamp_id", stampId);
        db.insertWithOnConflict("user_stamps", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void removeStampFromUser(int userId, long stampId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("user_stamps", "user_id = ? AND stamp_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(stampId)});
    }

    public int getCollectedStampsCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM user_stamps WHERE user_id = ?",
                new String[]{String.valueOf(userId)});
        int count = cursor.moveToFirst() ? cursor.getInt(0) : 0;
        cursor.close();
        return count;
    }

    public int getCollectedSeriesCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(DISTINCT s.series_id) FROM user_stamps us " +
                        "JOIN stamps s ON us.stamp_id = s.id WHERE us.user_id = ?",
                new String[]{String.valueOf(userId)});
        int count = cursor.moveToFirst() ? cursor.getInt(0) : 0;
        cursor.close();
        return count;
    }

    public int getTotalStampsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM stamps", null);
        int count = cursor.moveToFirst() ? cursor.getInt(0) : 0;
        cursor.close();
        return count;
    }

    public int getTotalSeriesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM series", null);
        int count = cursor.moveToFirst() ? cursor.getInt(0) : 0;
        cursor.close();
        return count;
    }

    public int getFullyCollectedSeriesCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor seriesCursor = db.rawQuery("SELECT id FROM series", null);
        int fullCount = 0;

        while (seriesCursor.moveToNext()) {
            int seriesId = seriesCursor.getInt(0);

            Cursor totalStamps = db.rawQuery("SELECT COUNT(*) FROM stamps WHERE series_id = ?",
                    new String[]{String.valueOf(seriesId)});
            Cursor collectedStamps = db.rawQuery(
                    "SELECT COUNT(*) FROM user_stamps WHERE stamp_id IN " +
                            "(SELECT id FROM stamps WHERE series_id = ?) AND user_id = ?",
                    new String[]{String.valueOf(seriesId), String.valueOf(userId)});

            if (totalStamps.moveToFirst() && collectedStamps.moveToFirst()) {
                if (totalStamps.getInt(0) > 0 &&
                        totalStamps.getInt(0) == collectedStamps.getInt(0)) {
                    fullCount++;
                }
            }
            totalStamps.close();
            collectedStamps.close();
        }

        seriesCursor.close();
        return fullCount;
    }


    public Cursor getConfirmedFriends(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM friends WHERE (user_id = ? OR friend_id = ?) AND is_confirmed = 1",
                new String[]{String.valueOf(userId), String.valueOf(userId)}
        );
    }

    public List<Friend> getConfirmedFriendsList(int userId) {
        List<Friend> friends = new ArrayList<>();
        Cursor cursor = getConfirmedFriends(userId);

        if (cursor.moveToFirst()) {
            do {
                int uid = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                int fid = cursor.getInt(cursor.getColumnIndexOrThrow("friend_id"));
                int otherId = (uid == userId) ? fid : uid;

                String nickname = cursor.getString(cursor.getColumnIndexOrThrow("nickname"));
                String unicCode = cursor.getString(cursor.getColumnIndexOrThrow("unic_code"));

                if (nickname == null) nickname = "Друг " + otherId;
                if (unicCode == null) unicCode = String.format("#%06d", otherId);

                friends.add(new Friend(nickname, unicCode, otherId));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return friends;
    }

    public void syncFriendsFromServer(Context context, int userId) {
        ApiService api = ApiClient.getService();
        SQLiteDatabase db = this.getWritableDatabase();

        api.getFriends(userId).enqueue(new Callback<List<FriendRelation>>() {
            @Override
            public void onResponse(Call<List<FriendRelation>> call, Response<List<FriendRelation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    db.beginTransaction();
                    try {
                        db.delete("friends", "user_id = ? OR friend_id = ?", new String[]{
                                String.valueOf(userId), String.valueOf(userId)
                        });

                        for (FriendRelation fr : response.body()) {
                            int uid = fr.user_id;
                            int fid = fr.friend_id;

                            if (uid != userId && fid != userId) continue;

                            int otherId = (uid == userId) ? fid : uid;

                            ContentValues values = new ContentValues();
                            values.put("user_id", userId);
                            values.put("friend_id", otherId);
                            values.put("nickname", fr.nickname);
                            values.put("unic_code", fr.unic_code);
                            values.put("is_confirmed", 1);
                            values.put("created_at", fr.created_at);

                            db.insertWithOnConflict("friends", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                        }

                        db.setTransactionSuccessful();
                        Log.d(TAG, "Синхронизация друзей завершена успешно.");
                    } finally {
                        db.endTransaction();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<FriendRelation>> call, Throwable t) {
                Log.e(TAG, "Ошибка при синхронизации друзей", t);
            }
        });
    }

    public int getFriendsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM friends", null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }


    public void clearFriendsTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("friends", null, null);
        db.close();
    }
}
