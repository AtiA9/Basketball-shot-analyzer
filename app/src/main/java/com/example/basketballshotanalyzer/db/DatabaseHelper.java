// AI-GENERATED (Claude Code) - reviewed and understood by the student
package com.example.basketballshotanalyzer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.basketballshotanalyzer.model.MonthlyStat;
import com.example.basketballshotanalyzer.model.ShotData;
import com.example.basketballshotanalyzer.model.User;
import com.example.basketballshotanalyzer.model.UserSummary;

import java.util.ArrayList;
import java.util.List;

/**
 * שכבת מסד הנתונים (SQLite). יורשת מ-SQLiteOpenHelper שאחראית ליצירה ולשדרוג של המסד.
 * שתי טבלאות: users (משתמשים) ו-shots (זריקות). כל זריקה שייכת למשתמש דרך מפתח זר user_id.
 * כל הגישה למסד באפליקציה עוברת רק דרך המחלקה הזו.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "basketball.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String TABLE_SHOTS = "shots";

    private static final String COL_ID = "id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD_HASH = "password_hash";
    private static final String COL_SALT = "salt";

    private static final String COL_USER_ID = "user_id";
    private static final String COL_ELBOW = "elbow_angle";
    private static final String COL_RELEASE = "release_angle";
    private static final String COL_SCORE = "score";
    private static final String COL_FEEDBACK = "feedback";
    private static final String COL_NOTE = "note";
    private static final String COL_TIMESTAMP = "timestamp";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /** מפעיל אכיפת מפתחות זרים (ב-SQLite היא כבויה כברירת מחדל). */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    /** נקרא פעם אחת כשהמסד נוצר - יוצר את שתי הטבלאות. */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USERNAME + " TEXT NOT NULL UNIQUE, "
                + COL_PASSWORD_HASH + " TEXT NOT NULL, "
                + COL_SALT + " TEXT NOT NULL)");
        db.execSQL("CREATE TABLE " + TABLE_SHOTS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USER_ID + " INTEGER NOT NULL, "
                + COL_ELBOW + " REAL, "
                + COL_RELEASE + " REAL, "
                + COL_SCORE + " INTEGER, "
                + COL_FEEDBACK + " TEXT, "
                + COL_NOTE + " TEXT, "
                + COL_TIMESTAMP + " INTEGER, "
                + "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_ID + ")"
                + " ON DELETE CASCADE)");
    }

    /** נקרא כשמעלים את DB_VERSION - כאן פשוט מוחקים ובונים מחדש. */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ===================== users: CRUD =====================

    /** Create: מוסיף משתמש. מחזיר את ה-id החדש, או -1 אם שם המשתמש תפוס. */
    public long insertUser(User user) {
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, user.getUsername());
        values.put(COL_PASSWORD_HASH, user.getPasswordHash());
        values.put(COL_SALT, user.getSalt());
        return getWritableDatabase().insert(TABLE_USERS, null, values);
    }

    /** Read: מחפש משתמש לפי שם. מחזיר null אם לא נמצא. */
    public User getUserByUsername(String username) {
        Cursor c = getReadableDatabase().query(TABLE_USERS, null,
                COL_USERNAME + " = ?", new String[]{username}, null, null, null);
        User user = null;
        if (c.moveToFirst()) {
            user = new User(c.getLong(c.getColumnIndexOrThrow(COL_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_USERNAME)),
                    c.getString(c.getColumnIndexOrThrow(COL_PASSWORD_HASH)),
                    c.getString(c.getColumnIndexOrThrow(COL_SALT)));
        }
        c.close();
        return user;
    }

    /** Update: מעדכן סיסמה (hash + salt) של משתמש. מחזיר true אם עודכנה שורה. */
    public boolean updateUserPassword(long userId, String newHash, String newSalt) {
        ContentValues values = new ContentValues();
        values.put(COL_PASSWORD_HASH, newHash);
        values.put(COL_SALT, newSalt);
        return getWritableDatabase().update(TABLE_USERS, values,
                COL_ID + " = ?", new String[]{String.valueOf(userId)}) > 0;
    }

    /** Delete: מוחק משתמש. בזכות ON DELETE CASCADE נמחקות גם כל הזריקות שלו. */
    public boolean deleteUser(long userId) {
        return getWritableDatabase().delete(TABLE_USERS,
                COL_ID + " = ?", new String[]{String.valueOf(userId)}) > 0;
    }

    // ===================== shots: CRUD =====================

    /** Create: שומר זריקה חדשה ומחזיר את ה-id שלה. */
    public long insertShot(ShotData shot) {
        ContentValues values = new ContentValues();
        values.put(COL_USER_ID, shot.getUserId());
        values.put(COL_ELBOW, shot.getElbowAngle());
        values.put(COL_RELEASE, shot.getReleaseAngle());
        values.put(COL_SCORE, shot.getScore());
        values.put(COL_FEEDBACK, shot.getFeedback());
        values.put(COL_NOTE, shot.getNote());
        values.put(COL_TIMESTAMP, shot.getTimestamp());
        return getWritableDatabase().insert(TABLE_SHOTS, null, values);
    }

    /** Read: מחזיר את כל הזריקות של משתמש, מהחדשה לישנה. */
    public List<ShotData> getShotsForUser(long userId) {
        List<ShotData> shots = new ArrayList<>();
        Cursor c = getReadableDatabase().query(TABLE_SHOTS, null,
                COL_USER_ID + " = ?", new String[]{String.valueOf(userId)},
                null, null, COL_TIMESTAMP + " DESC");
        while (c.moveToNext()) {
            shots.add(new ShotData(
                    c.getLong(c.getColumnIndexOrThrow(COL_ID)),
                    c.getLong(c.getColumnIndexOrThrow(COL_USER_ID)),
                    c.getDouble(c.getColumnIndexOrThrow(COL_ELBOW)),
                    c.getDouble(c.getColumnIndexOrThrow(COL_RELEASE)),
                    c.getInt(c.getColumnIndexOrThrow(COL_SCORE)),
                    c.getString(c.getColumnIndexOrThrow(COL_FEEDBACK)),
                    c.getString(c.getColumnIndexOrThrow(COL_NOTE)),
                    c.getLong(c.getColumnIndexOrThrow(COL_TIMESTAMP))));
        }
        c.close();
        return shots;
    }

    /** Update: מעדכן את ההערה של זריקה. */
    public boolean updateShotNote(long shotId, String note) {
        ContentValues values = new ContentValues();
        values.put(COL_NOTE, note);
        return getWritableDatabase().update(TABLE_SHOTS, values,
                COL_ID + " = ?", new String[]{String.valueOf(shotId)}) > 0;
    }

    /** Delete: מוחק זריקה אחת. */
    public boolean deleteShot(long shotId) {
        return getWritableDatabase().delete(TABLE_SHOTS,
                COL_ID + " = ?", new String[]{String.valueOf(shotId)}) > 0;
    }

    // ===================== סטטיסטיקה =====================

    /**
     * GROUP BY: ממוצע ציון ומספר זריקות לכל חודש.
     * strftime ממיר את זמן הזריקה (מילישניות) למחרוזת חודש yyyy-MM.
     */
    public List<MonthlyStat> getMonthlyStats(long userId) {
        String sql = "SELECT strftime('%Y-%m', " + COL_TIMESTAMP + " / 1000, 'unixepoch', 'localtime') AS month, "
                + "AVG(" + COL_SCORE + "), COUNT(*) FROM " + TABLE_SHOTS
                + " WHERE " + COL_USER_ID + " = ? GROUP BY month ORDER BY month DESC";
        List<MonthlyStat> stats = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(userId)});
        while (c.moveToNext()) {
            stats.add(new MonthlyStat(c.getString(0), c.getDouble(1), c.getInt(2)));
        }
        c.close();
        return stats;
    }

    /**
     * JOIN: מחבר את טבלת המשתמשים לטבלת הזריקות ומחזיר סיכום של המשתמש.
     * LEFT JOIN מבטיח שנקבל שורה גם למשתמש בלי זריקות בכלל (ואז המספרים 0).
     */
    public UserSummary getUserSummary(long userId) {
        String sql = "SELECT u." + COL_USERNAME + ", COUNT(s." + COL_ID + "), "
                + "IFNULL(AVG(s." + COL_SCORE + "), 0), IFNULL(MAX(s." + COL_SCORE + "), 0) "
                + "FROM " + TABLE_USERS + " u LEFT JOIN " + TABLE_SHOTS + " s "
                + "ON u." + COL_ID + " = s." + COL_USER_ID
                + " WHERE u." + COL_ID + " = ? GROUP BY u." + COL_ID;
        Cursor c = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(userId)});
        UserSummary summary = null;
        if (c.moveToFirst()) {
            summary = new UserSummary(c.getString(0), c.getInt(1), c.getDouble(2), c.getInt(3));
        }
        c.close();
        return summary;
    }
}
