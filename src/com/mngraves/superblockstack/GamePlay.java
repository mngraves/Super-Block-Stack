package com.mngraves.superblockstack;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
/**
 *  
 * @author Michael Graves
 * 
 * The Game Play Activity handles operation of the game and monitors for game over status
 *
 */
public class GamePlay extends Activity{
	GamePlayView mGamePlayView;
	
	/**
	 * Set some game rules
	 */
	public static final int MAX_GAME_LEVEL = 9;
	public static final String KEY_PLAYER_SCORE = "player_score";
	public static final String KEY_PLAYER_LINES = "player_lines";
	public static final String KEY_PLAYER_TETRIS_COUNT = "player_tetris_count";
	public static final String KEY_PLAYER_LEVEL = "player_level";
	
	private static final int GAME_STATUS_INTERVAL = 50;
	
	private Timer mGameStatusTimer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        /**
         * Set the custom game play view full screen and start game status timer
         */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                         WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mGamePlayView = new GamePlayView(this);
        setContentView(mGamePlayView);
        mGamePlayView.requestFocus();
        startGameStatusTimer();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
        return true;
    }

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		// TODO Auto-generated method stub
		mGamePlayView.pauseGame();
		return super.onMenuOpened(featureId, menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.resume_game:
			mGamePlayView.unPauseGame();
			return true;
			
		case R.id.quit_game:
			quitGame();
			break;
		
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		endGame();
	}
	
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		
		if(!hasFocus){
			mGamePlayView.pauseGame();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		
		
	}
	
	/**
	 * Quit the game and return back to the main screen
	 */
	private void quitGame(){
		setResult(Main.ACTIVITY_QUIT_GAME);
		stopGameStatusTimer();
		finish();	
	}
	
	/**
	 * End the game and prepare score data that can be stored at Game Over
	 */
	private void endGame(){
		Intent i = getIntent();
		Player p1 = mGamePlayView.getPlayer();
		
		i.putExtra(KEY_PLAYER_LEVEL, p1.getLevel());
		i.putExtra(KEY_PLAYER_SCORE, p1.getScore());
		i.putExtra(KEY_PLAYER_LINES, p1.getLines());
		i.putExtra(KEY_PLAYER_TETRIS_COUNT, p1.getTetrisCount());
		
		setResult(Main.ACTIVITY_GAME_OVER, i);
		stopGameStatusTimer();
		finish();
	}
	
	/**
	 * Starts the game status timer
	 */
    public void startGameStatusTimer(){
    	mGameStatusTimer = new Timer();
    	mGameStatusTimer.schedule(new GameStatusTask(), 0, GAME_STATUS_INTERVAL);
    }
    
    /**
     * Stops the game status timer
     */
    public void stopGameStatusTimer(){
    	if(mGameStatusTimer != null){
    		mGameStatusTimer.cancel();
    	}
    }
	
    /**
     * TimerTask class used to detect game over status
     */
    private class GameStatusTask extends TimerTask{
    	@Override
    	public void run(){
    		if(mGamePlayView.isGameOver()){
    			//endGame();
    			stopGameStatusTimer();
    			endGame();
    		}
    	}
    }
	
}
