package com.mngraves.superblockstack;

import android.content.Context;
import android.util.Log;
/**
 * 
 * @author Michael Graves
 * The Player class
 */
public class Player {
	private static final String TAG = "Player";
	private String mName;
	/** 1 based level **/
	private int mLevel = 0;
	private int mLines = 0;
	private long mScore = 0;
	private int mTetrisCount = 0;

	Player(String name){
		mName = name;
	}

	public String getmName() {
		return mName;
	}
	
	/**
	 * Set the player level as a function of the current number of lines
	 */
	public void setLevel(){
		int earnedLevel = Math.round((mLines - 1) / 10);
		if (earnedLevel < 0)  earnedLevel = 0;
		else if (earnedLevel > 9)  earnedLevel = 9;
		mLevel = earnedLevel;
	}
	
	public void setScore(int freeFallIterations){
		int points = Math.round((24 + (3*mLevel)) - freeFallIterations);
		
		mScore = mScore + points;
	}
	
	public void setmScore(long val){
		mScore = val;
	}
	
	public void setmName(String mName) {
		this.mName = mName;
	}
	
	public int getLevel() {
		return mLevel;
	}
	
	/**
	 * Since level is 0 based, level display is for showing score on UI
	 * @return level plus one
	 */
	public int getLevelDisplay(){
		return mLevel+1;
	}

	public int getLines() {
		return mLines;
	}

	public long getScore() {
		return mScore;
	}
	
	public void addLine(){
		mLines++;
	}
	
	public int getTetrisCount(){
		return mTetrisCount;
	}
	
	public void addTetris(){
		mTetrisCount++;
	}
	
	public void setmTetrisCount(int val){
		mTetrisCount = val;
	}
	
	public void setmLevel(int val){
		mLevel = val;
	}
	
	public void addLines(int numLines){
		mLines += numLines;
	}
	
	/**
	 * Save the player stats to the game db
	 * @param context
	 */
	public void recordStats(Context context){
		GameDbAdapter dbHelper = new GameDbAdapter(context);
		dbHelper.open();
		Log.d(TAG, "Recording score.");
		dbHelper.createScore(mName, (int)mScore, getLevelDisplay(), mTetrisCount);
		dbHelper.close();
	}
	
}
