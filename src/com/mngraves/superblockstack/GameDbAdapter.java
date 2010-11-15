package com.mngraves.superblockstack;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 *  
 * @author Michael Graves
 * 
 * Database helper class
 */
public class GameDbAdapter {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_PLAYER = "player";
    public static final String KEY_SCORE = "score";
    public static final String KEY_LEVEL = "level";
    public static final String KEY_TETRIS_COUNT = "tetris_count";
    public static final String KEY_TIMESTAMP = "datetime";

    private static final String TAG = "GameDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "superblockstack.db";
    private static final String SCORES_TABLE = "scores";
    private static final int DATABASE_VERSION = 3;
    
    /**
     * Database creation SQL statements
     */
    private static final String DATABASE_CREATE_SCORES =
        "create table " + SCORES_TABLE + " (" + KEY_ROWID + " integer primary key autoincrement, " +
        KEY_PLAYER + " text not null, " + KEY_SCORE + " integer not null, " + KEY_LEVEL + " integer not null, " +
        KEY_TETRIS_COUNT + " integer not null, " + KEY_TIMESTAMP + " text not null );";
    
    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE_SCORES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + SCORES_TABLE);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public GameDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the game database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public GameDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }
    
    /**
     * Add score to the scores table
     * @param player
     * @param score
     * @param level
     * @param tetrisCount
     * @return 
     */
    public long createScore(String player, int score, int level, int tetrisCount) {
        Calendar now = Calendar.getInstance();
    	ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_PLAYER, player);
        initialValues.put(KEY_SCORE, score);
        initialValues.put(KEY_LEVEL, level);
        initialValues.put(KEY_TETRIS_COUNT, tetrisCount);
        initialValues.put(KEY_TIMESTAMP, now.toString());
        
        return mDb.insert(SCORES_TABLE, null, initialValues);
    }

    /**
     * Return a Cursor over the list of all scores in the database
     * 
     * @return Cursor over all scores
     */
    public Cursor fetchAllScores() {

    	return mDb.query(SCORES_TABLE, new String[] {KEY_PLAYER, KEY_SCORE, KEY_LEVEL, KEY_TETRIS_COUNT}, 
        		null, null, null, null, KEY_SCORE + " desc");
    }

    /**
     * Return a Cursor positioned at the score that matches the given rowId
     * 
     * @param rowId id of score to retrieve
     * @return Cursor positioned to matching score, if found
     * @throws SQLException if score could not be found/retrieved
     */
    public Cursor fetchScore(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, SCORES_TABLE, new String[] {KEY_ROWID, KEY_PLAYER, KEY_SCORE, KEY_LEVEL, KEY_TETRIS_COUNT, KEY_TIMESTAMP}, 
            		KEY_ROWID + "=" + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    /**
     * Gets the last player name stored in the scores table
     * @return the last player name stored in the scores table
     */
    public String getLastPlayerName(){
    	String name = "";
    	Cursor mCursor = 
    		mDb.query(SCORES_TABLE, new String[]{KEY_PLAYER}, null, null, null, null, KEY_ROWID + " desc");
    	if(mCursor != null && mCursor.getCount() > 0){
    		mCursor.moveToFirst();
    		name = mCursor.getString(0);
    	}
    	return name;
    }
}
