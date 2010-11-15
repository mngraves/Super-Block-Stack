package com.mngraves.superblockstack;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
/**
 * 
 * @author Michael Graves
 * The Main Activity shows the main game screen along with scores table
 *
 */
public class Main extends Activity {
	static final String TAG = "Main";
	static final int ACTIVITY_NEW_GAME = 1;
    static final int ACTIVITY_GAME_OVER = 2;
    static final int ACTIVITY_QUIT_GAME = 3;
    private GameDbAdapter mDbHelper;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mDbHelper = new GameDbAdapter(this);
        getScores();
        
        ImageButton newGameButton = (ImageButton) findViewById(R.id.new_game_button);
        ImageButton exitAppButton = (ImageButton) findViewById(R.id.exit_app_button);
        /**
         * Set anonymous OnClick Listener class to new game button
         */
        newGameButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				startGame();
			}
		});
        
        exitAppButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				finish();
			}
		});        
    }
    
    /**
     * Populates the scores table with all recorded scores
     */
    private void getScores(){
    	mDbHelper.open();
    	
    	// Get all of the rows from the database and create the item list
    	Cursor scoresCursor = mDbHelper.fetchAllScores();
        scoresCursor.moveToFirst();
    	startManagingCursor(scoresCursor);
        TableLayout scoresTable = (TableLayout)findViewById(R.id.scores_table);
        LayoutInflater inflater = LayoutInflater.from(this);
        scoresTable.removeAllViews();
        
        if(scoresCursor.getCount() == 0){
        	/**
        	 * Display the no scores text
        	 */
        	TableRow noScoresRow = new TableRow(this);

        	noScoresRow.setLayoutParams(new LayoutParams(
                    LayoutParams.FILL_PARENT,
                    LayoutParams.FILL_PARENT));
        	
        	TextView noScoresText = (TextView)inflater.inflate(R.layout.no_scores, null);
        	Log.d(TAG, noScoresText.toString());
        	noScoresRow.addView(noScoresText);
        	scoresTable.addView(noScoresRow);
        } else {       
        	/**
        	 * Set the table head and then display the score data rows
        	 */
        	TableRow scoreTableHead = (TableRow)inflater.inflate(R.layout.score_table_head, null);
        	scoresTable.addView(scoreTableHead);
        	while(!scoresCursor.isAfterLast()){
	        	TableRow tr = new TableRow(this);
	        	 tr.setLayoutParams(new LayoutParams(
	                     LayoutParams.FILL_PARENT,
	                     LayoutParams.FILL_PARENT));
	        	 for(int i = 0; i < scoresCursor.getColumnCount(); i++){
	        		 TextView col = (TextView)inflater.inflate(R.layout.score_field, null);
	        		 col.setText(scoresCursor.getString(i));
	        		 col.setLayoutParams(new LayoutParams(
	                         LayoutParams.FILL_PARENT,
	                         LayoutParams.FILL_PARENT,
	                         1));
	        		 tr.addView(col);
	        	 }
	        	 scoresTable.addView(tr);
	        	 scoresCursor.moveToNext();
	        }
        }
        mDbHelper.close();
    }
    
    /**
     * Starts the game play activity
     */
    public void startGame(){
    	Intent i = new Intent(this, GamePlay.class);
    	startActivityForResult(i, ACTIVITY_NEW_GAME);
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		Log.d(TAG, "Activity result...");
		switch(resultCode){
		case ACTIVITY_GAME_OVER:
			showGameOver(data);
			break;
		}
		getScores();
	}
    
	/**
	 * Start the Game Over activity
	 * @param intent
	 */
	private void showGameOver(Intent intent){
		Intent i = new Intent(this, GameOver.class);
		i.putExtras(intent);
		
		startActivityForResult(i, Main.ACTIVITY_GAME_OVER);
	}    
}