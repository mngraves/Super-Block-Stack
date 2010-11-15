package com.mngraves.superblockstack;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
/**
 *  
 * @author Michael Graves
 * 
 * The Game Over Activity handles collecting & storing the player's score
 *
 */
public class GameOver extends Activity{
	private static final String TAG = "GameOver";
	private Player mPlayer;
	
	@Override
	protected void onCreate(Bundle data) {
		super.onCreate(data);
		Intent i = getIntent();
		Bundle b = i.getExtras();
		setContentView(R.layout.game_over);
		initializePlayer(b);
		
		/**
		 * Initialize stats passed from the recently ended game
		 */
		TextView score = (TextView)findViewById(R.id.go_stat_score);
		TextView lines = (TextView)findViewById(R.id.go_stat_lines);
		TextView tetrisCount = (TextView)findViewById(R.id.go_stat_tetris_count);
		TextView level = (TextView)findViewById(R.id.go_stat_level);
		EditText playerName = (EditText)findViewById(R.id.player_name);
		
		lines.setText("" + mPlayer.getLines());
		tetrisCount.setText("" + mPlayer.getTetrisCount());
		level.setText("" + mPlayer.getLevelDisplay());
		score.setText("" + mPlayer.getScore());
		playerName.setText(getLastPlayerName());
		
		Button saveScoreButton = (Button)findViewById(R.id.save_score_button);
		Button quitButton = (Button)findViewById(R.id.quit_button);
		
		saveScoreButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(updatePlayerName()){
					saveScore();
				} else {
					showNameSaveError();
				}
			}
		});
		
		quitButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				quit();
			}
		});
	}
	
	/**
	 * Gets the last name saved to the scores table
	 * @return the last name stored in the scores table
	 */
	public String getLastPlayerName(){
		GameDbAdapter dbHelper = new GameDbAdapter(this);
		dbHelper.open();
		String name = dbHelper.getLastPlayerName();
		dbHelper.close();
		return name;
	}
	
	/**
	 * Display error message if there was a problem saving the score
	 */
	private void showNameSaveError(){
		
	}
	
	/**
	 * Attempt to change the name of the player to the one provided through the form
	 * @return true if the name change was successful
	 */
	private boolean updatePlayerName(){
		TextView playerName = (TextView)findViewById(R.id.player_name);
		String newName = playerName.getText().toString();
		if(newName.length() > 0){
			mPlayer.setmName(newName);
			return true;
		}
		return false;
	}
	
	/**
	 * Initialize our player based on the data provided by the bundle
	 * @param b Bundle containing player data
	 */
	private void initializePlayer(Bundle b){
		mPlayer = new Player(getString(R.string.default_player_name));
		mPlayer.addLines(b.getInt(GamePlay.KEY_PLAYER_LINES));
		mPlayer.setmTetrisCount(b.getInt(GamePlay.KEY_PLAYER_TETRIS_COUNT));
		mPlayer.setmLevel(b.getInt(GamePlay.KEY_PLAYER_LEVEL));
		mPlayer.setmScore(b.getLong(GamePlay.KEY_PLAYER_SCORE));
	}
	
	/**
	 * Record the current score
	 */
	private void saveScore(){
		mPlayer.recordStats(this);
		quit();
	}
	
	/**
	 * Quit this activity
	 */
	private void quit(){
		setResult(Main.ACTIVITY_QUIT_GAME);
		finish();
	}
	
}
