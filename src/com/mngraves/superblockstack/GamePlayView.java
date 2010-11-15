package com.mngraves.superblockstack;

import java.util.Hashtable;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * @author Michael Graves
 * The Game Play View draws the game grid and controls user input during a game
 */
public class GamePlayView extends View implements OnTouchListener{
	private static final String TAG = "GamePlayView";
	private boolean mIsPaused = false;
	private boolean mIsMuted = false;
	private boolean mIsGameOver = false;
	private Timer mGameUpdateTimer;
	private Timer mPieceMoveTimer;
	private static long GAME_UPDATE_PERIOD = 50;
	private static int NUM_PIECES = 7;
	private Paint mPaint;
	private int mCurPlayerLevel = 0;
	private int mFreeFallIterations = 0;
	private Random mRandom;
	private boolean mGameStateChanged = false;
	private boolean mDropPiece = false;
	private boolean mGameUpdating = false;
	private int mPathLeft = -1;
	private int mPathRight = -1;
	
	/** The number of rows considered a tetris **/
	private static int TETRIS_NUM_ROWS = 4;
	
	private static int SCREEN_WIDTH = 480;
	private static int SCREEN_HEIGHT = 800;

	private static int BLOCK_BORDER_COLOR = Color.BLACK;
	private static int BLOCK_WIDTH = 35;
	private static int BLOCK_HEIGHT = 35;
	
	private static int GRID_NUM_HIDDEN_ROWS = 2;
	private static int GRID_COLOR = Color.rgb(203, 216, 222);
	private static int GRID_LINE_COLOR = Color.rgb(175, 199, 210);
	private static int GRID_NUM_COLS = 10;
	private static int GRID_NUM_ROWS = 20 + GRID_NUM_HIDDEN_ROWS;
	private static int GRID_WIDTH = GRID_NUM_COLS*BLOCK_WIDTH;
	private static int GRID_HEIGHT = (GRID_NUM_ROWS - GRID_NUM_HIDDEN_ROWS)*BLOCK_HEIGHT;
	private static int GRID_CUR_PATH_COLOR = Color.rgb(153, 154, 154);
	
	private static int PIECE_O_COLOR = Color.YELLOW;
	private static int PIECE_I_COLOR = Color.CYAN;
	private static int PIECE_S_COLOR = Color.GREEN;
	private static int PIECE_Z_COLOR = Color.RED;
	private static int PIECE_L_COLOR = Color.rgb(255, 156, 49);
	private static int PIECE_J_COLOR = Color.BLUE;
	private static int PIECE_T_COLOR = Color.rgb(186, 20, 227);
	
	private Hashtable<Integer, BlockGroup> mPieces;
	private BlockGroup mCurPiece;
	private BlockGroup mTheGrid;
	private BlockGroup mTheGridCurState;
	private BlockGroup mNextPiece;
	
	private static int NEXT_BLOCK_WIDTH = 20;
	private static int NEXT_BLOCK_HEIGHT = 20;
	
	
	private static int SCORE_TEXT_COLOR = Color.WHITE;
	private static int SCORE_TEXT_X = 384;
	private static int SCORE_TEXT_Y = 178;
	
	private static int TETRIS_TEXT_COLOR = Color.WHITE;
	private static int TETRIS_TEXT_X = 384;
	private static int TETRIS_TEXT_Y = 226;	
	
	private static int LEVEL_TEXT_COLOR = Color.WHITE;
	private static int LEVEL_TEXT_X = 384;
	private static int LEVEL_TEXT_Y = 75;
	
	private static int LINES_TEXT_COLOR = Color.WHITE;
	private static int LINES_TEXT_X = 384;
	private static int LINES_TEXT_Y = 125;
	
	/**
	 * Valid horizontal move values: -1 (left), 0 (idle), 1 (right)
	 */
	private int mHMoveRequest = 0;
	/**
	 * Valid rotate values: 0 (idle), 1 (rotate counter clockwise)
	 */
	private int mRotateRequest = 0;
	private int mVMoveRequest = 0;
	
	private Player mPlayer1;
	private Context mContext;
	
	private Drawable mGameBackground;
	private Drawable mGameOverText;
	private Drawable mGamePausedText;
	private Drawable mBlockBevel;
	private Drawable mSoundOnIcon;
	private Drawable mSoundOffIcon;
	
	private MediaPlayer mSoundTrack;
	private MediaPlayer mDropSound;
	private MediaPlayer mMoveSound;
	private MediaPlayer mClearRowsSound;
	
	public GamePlayView(Context context){
		super(context);
		mContext = context;
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setOnTouchListener(this);
        Point[][] gridPoints = {{}};
        mTheGrid = new BlockGroup(GRID_NUM_COLS, GRID_NUM_ROWS, "The Grid", gridPoints, Color.rgb(248, 111, 0));
        mTheGrid.setmY(-(BLOCK_HEIGHT*2));
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPieces = new Hashtable<Integer, BlockGroup>();
        mRandom = new Random();
        setPieces();
        mPlayer1 = new Player("Player 1");
        mNextPiece = null;
    	
        initializeGraphics();
    	initializeSounds(context);
	}

	private void initializeGraphics(){
    	Resources res = mContext.getResources();
    	Rect bounds;
    	
    	mGameBackground = res.getDrawable(R.drawable.tetrisbghdpi);
    	bounds = new Rect(0, 0, SCREEN_WIDTH, mGameBackground.getIntrinsicHeight());
    	mGameBackground.setBounds(bounds);
    	
    	mSoundOffIcon = res.getDrawable(R.drawable.soundoff);
    	bounds = new Rect(400, 475, 400+mSoundOffIcon.getIntrinsicWidth(), 475+mSoundOffIcon.getIntrinsicHeight());
    	mSoundOffIcon.setBounds(bounds);
    	
    	mSoundOnIcon = res.getDrawable(R.drawable.soundon);
    	mSoundOnIcon.setBounds(bounds);
    	
    	mGameOverText = res.getDrawable(R.drawable.tetrisgameovertext);
    	bounds = new Rect(50, 320, mGameOverText.getIntrinsicWidth()+50, 320+mGameOverText.getIntrinsicHeight());
    	mGameOverText.setBounds(bounds);
    	
    	mGamePausedText = res.getDrawable(R.drawable.tetrispausedtext);
    	bounds = new Rect(50, 320, mGamePausedText.getIntrinsicWidth()+50, 320+mGamePausedText.getIntrinsicHeight());
    	mGamePausedText.setBounds(bounds); 	
    	
    	mBlockBevel = res.getDrawable(R.drawable.blockbevel);
	}
	
	private void initializeSounds(Context context){
    	mSoundTrack = MediaPlayer.create(context, R.raw.tetrismusic);
    	mSoundTrack.setLooping(true);
    	mSoundTrack.setVolume((float).25, (float).25);
    	mSoundTrack.start();
    	
    	mDropSound = MediaPlayer.create(context, R.raw.drop1);
    	mDropSound.setVolume((float).65, (float).65);

    	mMoveSound = MediaPlayer.create(context, R.raw.move1);
    	mMoveSound.setVolume((float).45, (float).45);
    	
    	mClearRowsSound = MediaPlayer.create(context, R.raw.rowscleared1);
    	mClearRowsSound.setVolume((float).45, (float).45);    	
	}
	
	/**
	 * Mute the game
	 */
	private void mute(){
		mIsMuted = true;
		mSoundTrack.pause();
	}
	
	/**
	 * Turn off mute
	 */
	private void unMute(){
		mIsMuted = false;
		if(!mIsPaused){
			mSoundTrack.start();
		}
	}
	
	/**
	 * Pause the game
	 */
	public void pauseGame(){
		mIsPaused = true;
		mSoundTrack.pause();
		invalidate();
	}
	
	/**
	 * Resume game from paused state
	 */
	public void unPauseGame(){
		mIsPaused = false;
		if(!mIsMuted){
			mSoundTrack.start();
		}
	}
	
	/**
	 * Set game over status and clean up
	 */
	public void gameOver(){
		mIsGameOver = true;
		mSoundTrack.stop();
		stopGameUpdateTimer();
		stopPieceMoveTimer();
	}
	
	/**
	 * 
	 * @return true if game is over
	 */
	public boolean isGameOver(){
		return mIsGameOver;
	}
	
	/**
	 * 
	 * @return the current player
	 */
	public Player getPlayer(){
		return mPlayer1;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch(keyCode){
		case KeyEvent.KEYCODE_BACK:
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}



	@Override
	public boolean onTouch(View view, MotionEvent event) {
		float x = event.getRawX();
		float y = event.getRawY();
		
		if(y < BLOCK_HEIGHT*(GRID_NUM_ROWS-GRID_NUM_HIDDEN_ROWS)){
			if(x < GRID_NUM_COLS*BLOCK_WIDTH){
				mRotateRequest = 1;
			} else {
				/**
				 * Sound on/off button action
				 * 
				 * - Ignore press if game is paused
				 */
				Rect bounds = mSoundOffIcon.getBounds();
				if(!mIsPaused && bounds.contains((int)x, (int)y)){
					if(mIsMuted){
						unMute();
					} else {
						mute();
					}
				}
				/**
				 * Drop piece button action
				 */
				bounds = new Rect(GRID_WIDTH, 580, SCREEN_WIDTH, GRID_HEIGHT);
				if(bounds.contains((int)x, (int)y)){
					mDropPiece = true;
				}
					
			}
		} else {
			mHMoveRequest = event.getRawX() > (SCREEN_WIDTH/2) ? 1 : -1;
		}
		return false;
	}
	
    @Override
    public void onDraw(Canvas canvas) {
    	drawGrid(canvas);
    	drawBlockGroup(mTheGridCurState, canvas);
    	
    	/** Draw Score Text **/
    	mPaint.setColor(SCORE_TEXT_COLOR);
    	canvas.drawText(mPlayer1.getScore() + "", SCORE_TEXT_X, SCORE_TEXT_Y, mPaint);
    	
       	/** Draw Lines Text **/
    	mPaint.setColor(LINES_TEXT_COLOR);
    	canvas.drawText(mPlayer1.getLines() + "", LINES_TEXT_X, LINES_TEXT_Y, mPaint);
    	
       	/** Draw Level Text **/
    	mPaint.setColor(LEVEL_TEXT_COLOR);
    	canvas.drawText(mPlayer1.getLevelDisplay() + "", LEVEL_TEXT_X, LEVEL_TEXT_Y, mPaint);
    	
    	/** Draw Tetris Text **/
    	mPaint.setColor(TETRIS_TEXT_COLOR);
    	canvas.drawText(mPlayer1.getTetrisCount() + "", TETRIS_TEXT_X, TETRIS_TEXT_Y, mPaint);
    	
    	/** Draw next piece **/
    	mNextPiece.setmX(GRID_WIDTH+NEXT_BLOCK_WIDTH+5);
    	mNextPiece.setmY(Math.round(GRID_HEIGHT/2)-10);
    	mNextPiece.setmIsPreviewSize(true);
    	drawBlockGroup(mNextPiece, canvas);
    	
    	/** if game over, draw game over text **/
    	if(mIsGameOver){
    		mGameOverText.draw(canvas);
    	}
    	/** if game is paused, draw the paused text **/
    	if(mIsPaused && !mIsGameOver){
    		mGamePausedText.draw(canvas);
    	}
    	
    	/** Draw sound on/off button **/
    	if(mIsMuted){
    		mSoundOffIcon.draw(canvas);
    	} else {
    		mSoundOnIcon.draw(canvas);
    	}
    	  	
    }
    
    private void resetMovementRequests(){
    	mHMoveRequest = 0;
    	mRotateRequest = 0;
    	setmVMoveRequest(0);
    }
    
    /**
     * Updates the game data for current iteration
     */
    synchronized private void updateGame(){
		mGameUpdating = true;
		int moveHorizontal = mHMoveRequest;
		int moveVertical = mVMoveRequest;
		int rotate = mRotateRequest;
		boolean gridUpdateComplete = false;
		int rowsCleared = 0;
		
		if(mCurPiece == null){
    		setCurPiece();
    		setGameStateChanged(true);
    		resetMovementRequests();
    	} else {
    		/**
    		 * Process all movements (Drop, Left, Right, Rotate)
    		 */
    		
    		/** Move horizontal **/
    		if(moveHorizontal != 0){
    			mCurPiece.setmX(mCurPiece.getmX() + mHMoveRequest);
    			setGameStateChanged(true);
    		}
    		/** Rotate **/
    		if(rotate != 0){
    			mCurPiece.rotate();
    			setGameStateChanged(true);
    		}
    		/** Move vertical **/
    		if(mDropPiece || moveVertical != 0){
    			movePieceDown();
    		}
    	}
		
		/**
		 * If the game state changed...
		 */
    	if(mGameStateChanged){
	    	setCurGridState();
    		
    		if(mTheGridCurState.addBlockGroup(mCurPiece)){
	    		/**
	    		 * Successfully added the block group
	    		 */
    			//Log.d(TAG, "+++ Added block group, block count: " + mTheGridCurState.getmBlockCount());
	    	}
	    	else {
	    		/**
	    		 * Move or add of piece failed... either collision or game is over (grid full)
	    		 */
	    		
				/** back off horizontal movement **/
				if(!gridUpdateComplete && moveHorizontal != 0){
					Log.d(TAG, "Backing off Horizontal movement...");
					if(mCurPiece.backOffHMovement()){
						if (!mTheGridCurState.addBlockGroup(mCurPiece)){
							/** undo horizontal movement backoff **/
						} else {
							gridUpdateComplete = true;
						}
					}
				}
				/** back off rotation **/
				if(!gridUpdateComplete && rotate != 0){
					if(mCurPiece.backOffRotation()){
						Log.d(TAG, "Backed off rotation. Adding to grid.");
						if (!mTheGridCurState.addBlockGroup(mCurPiece)){
							/** undo rotate backoff **/
							Log.d(TAG, "Rotation backoff did not work...");
							
						} else {
							gridUpdateComplete = true;
						}
					}
				}
	    		if(!gridUpdateComplete && (mDropPiece || moveVertical != 0)){
					if(mCurPiece.backOffDownMovement()){
		    			if(mTheGridCurState.addBlockGroup(mCurPiece)){
		    				mTheGrid.addBlockGroup(mCurPiece);
		    	    		mCurPiece = null;
		    	    		gridUpdateComplete = true;
		    	    		if(!mIsMuted){
		    	    			mDropSound.start();
		    	    		}
		    			} else {
		    				//Log.d(TAG, "Redoing movement...");
		    				/** back off down movement didn't work... redo **/
		    			}
		    		}
	    		}
				/**
				 * If gridUpdateComplete is still false... clear current piece?
				 */
				if(!gridUpdateComplete){
					mCurPiece = null;
					gameOver();
				}
	    		//Log.d(TAG, "+++ Failed to add block group");
	    	}
			
    		/**
			 * If we added a piece to the grid, clear rows
			 */
			if(mCurPiece == null){
				rowsCleared = mTheGrid.clearRows();
				setCurGridState();
				mPlayer1.setScore(mFreeFallIterations);
			}
			
    		resetMovementRequests();
			mPlayer1.addLines(rowsCleared);
			mPlayer1.setLevel();
			/** 
			 * if level up, change iteration delay
			 */
			if(mPlayer1.getLevel() != mCurPlayerLevel){
				mCurPlayerLevel = mPlayer1.getLevel();
				stopPieceMoveTimer();
				startPieceMoveTimer();
			}
			setGameStateChanged(false);
    	}
    	/** Add to tetris count **/
    	if(rowsCleared >= TETRIS_NUM_ROWS){
    		mPlayer1.addTetris();
    	}
    	if(rowsCleared > 0 && !mIsMuted){
    		mClearRowsSound.start();
    	}
    	setCurGridPath();
    	mGameUpdating = false;
    }
	
    /**
     * Sets the left and right bounds of the path of the current tetromino
     */
	private void setCurGridPath(){
		if(mCurPiece == null){
			mPathLeft = -1;
			mPathRight = -1;
		} else {
			mPathLeft = BLOCK_WIDTH * (mCurPiece.getmX() + mCurPiece.getLeftEdge());
			mPathRight = BLOCK_WIDTH * (mCurPiece.getmX() + mCurPiece.getRightEdge());
		}
	}
	
	/**
	 * Create a copy of the real grid
	 */
	private void setCurGridState(){
		Point[][] points = {mTheGrid.getBlockPoints()};
		mTheGridCurState = new BlockGroup(mTheGrid.getmColumns(), mTheGrid.getmRows(), "The Grid Current State", points, mTheGrid.getmBlockColor());
		mTheGridCurState.setmY(mTheGrid.getmY());
	}
	
	synchronized private void setGameStateChanged(boolean val){
		mGameStateChanged = val;
	}
	
	/**
	 * Moves the current piece down on the grid
	 */
	synchronized private void movePieceDown(){
    	if(mCurPiece != null){
    		mCurPiece.setmY(mCurPiece.getmY() + 1);
    		setGameStateChanged(true);
    	}
	}
	
	/**
	 * Sets the vertical movement request
	 * @param val 
	 */
	synchronized private void setmVMoveRequest(int val){
		mVMoveRequest = val;
	}
	
	/**
	 * Sets the current piece and update next piece.
	 */
    private void setCurPiece(){
    	mDropPiece = false;
    	mFreeFallIterations = 0;
    	
    	BlockGroup tmp = mPieces.get(new Integer(1 + mRandom.nextInt(NUM_PIECES)));
    	mCurPiece = new BlockGroup(tmp.getmColumns(), tmp.getmRows(), "Current Piece", tmp.getmBlockGroupLayouts(), tmp.getmBlockColor());
    	
    	if(mNextPiece == null){
    		mCurPiece = new BlockGroup(tmp.getmColumns(), tmp.getmRows(), "Current Piece", tmp.getmBlockGroupLayouts(), tmp.getmBlockColor());
    	} else {
    		mCurPiece = mNextPiece;
    		mCurPiece.setmX(0);
    		mCurPiece.setmY(0);
    		mCurPiece.setmIsPreviewSize(false);
    	}
    	tmp = mPieces.get(new Integer(1 + mRandom.nextInt(NUM_PIECES)));
    	mNextPiece = new BlockGroup(tmp.getmColumns(), tmp.getmRows(), "Current Piece", tmp.getmBlockGroupLayouts(), tmp.getmBlockColor());
    	setGameStateChanged(true);
    }
    
    /**
     * Draw a single block on the grid
     * @param startX the x coordinate of the block
     * @param startY the y coordinate of the block
     * @param width the width of the block in pixels
     * @param height the height of the block in pixels
     * @param color the color of the block
     * @param canvas the canvas
     */
    private void drawBlock(int startX, int startY, int width, int height, int color, Canvas canvas){
    	/**
    	 * Draw a block with border.
    	 */
        mPaint.setColor(color);
        canvas.drawRect(startX, startY, width+startX, height+startY, mPaint);
        /** Add bevel **/
        Rect bounds = new Rect(startX, startY, width+startX, height+startY);
        mBlockBevel.setBounds(bounds);
        mBlockBevel.draw(canvas);
    }
    
    /**
     * Draw an entire block group on the grid
     * @param blockGroup the block group to be drawn
     * @param canvas the canvas
     */
    private void drawBlockGroup(BlockGroup blockGroup, Canvas canvas){
    	int startX = blockGroup.getmX();
    	int startY = blockGroup.getmY();
    	int rows = blockGroup.getmRows();
    	int cols = blockGroup.getmColumns();
    	int blockWidth = (blockGroup.getmIsPreviewSize()) ? NEXT_BLOCK_WIDTH : BLOCK_WIDTH;
    	int blockHeight = (blockGroup.getmIsPreviewSize()) ?NEXT_BLOCK_HEIGHT : BLOCK_HEIGHT;
    	
    	for(int curRow = 0; curRow < rows; curRow++){
    		for(int curCol = 0; curCol < cols; curCol++){
    			if(!blockGroup.isSpaceEmpty(curCol, curRow)){
    				drawBlock(startX+(curCol*blockWidth), startY+(curRow*blockHeight), blockWidth, blockHeight, blockGroup.getBlock(curCol, curRow).getmColor(), canvas);
    			}
    		}
    	}
    }    
    
    /**
     * Draw a grid
     * @param canvas the canvas
     */
    private void drawGrid(Canvas canvas){
    	mPaint.setColor(GRID_COLOR);
    	mGameBackground.draw(canvas);
    	
    	for(int col = 1; col < GRID_NUM_COLS; col++){
    		int x = col*BLOCK_WIDTH;
    		if(x == mPathLeft || x == mPathRight){
    			mPaint.setColor(GRID_CUR_PATH_COLOR);
    		} else {
    			mPaint.setColor(GRID_LINE_COLOR);
    		}
    		canvas.drawLine(x, 0, col*BLOCK_WIDTH, GRID_HEIGHT, mPaint);
    	}
    	
    	mPaint.setColor(GRID_LINE_COLOR);
    	for(int row = 1; row < (GRID_NUM_ROWS-GRID_NUM_HIDDEN_ROWS); row++){
    		canvas.drawLine(0, row*BLOCK_WIDTH, GRID_WIDTH, row*BLOCK_WIDTH, mPaint);
    	}
    }
    
    /**
     * Initialize all tetrominoes
     */
    private void setPieces(){
    	// TODO: Possibly move the piece map data to XML
    	Point[][] o = { {new Point(1,1), new Point(2,1), new Point(1,2), new Point(2,2)} };
    	mPieces.put(new Integer("1"), new BlockGroup(4, 4, "O", o, PIECE_O_COLOR));

    	Point[][] i = {{new Point(0,1), new Point(1,1), new Point(2,1), new Point(3,1)},
    			{ new Point(2,0), new Point(2,1), new Point(2,2), new Point(2,3)}
    	};
    	mPieces.put(new Integer("2"), new BlockGroup(4, 4, "I", i, PIECE_I_COLOR));
    	
    	Point[][] s = {{new Point(2,1), new Point(3,1), new Point(1,2), new Point(2,2)},
    			{ new Point(2,0), new Point(2,1), new Point(3,1), new Point(3,2) }
    	};
    	mPieces.put(new Integer("3"), new BlockGroup(4, 4, "S", s, PIECE_S_COLOR));
    	
    	Point[][] z = {{new Point(1,1), new Point(2,1), new Point(2,2), new Point(3,2)},
    			{ new Point(3,0), new Point(2,1), new Point(3,1), new Point(2,2) }
    	};
    	mPieces.put(new Integer("4"), new BlockGroup(4, 4, "Z", z, PIECE_Z_COLOR));
    	
    	Point[][] l = {{new Point(1,1), new Point(2,1), new Point(3,1), new Point(1,2)},
    			{ new Point(2,0), new Point(2,1), new Point(2,2), new Point(3,2) },
    			{ new Point(3,0), new Point(1,1), new Point(2,1), new Point(3,1) },
    			{ new Point(1,0), new Point(2,0), new Point(2,1), new Point(2,2) },
    	};
    	mPieces.put(new Integer("5"), new BlockGroup(4, 4, "L", l, PIECE_L_COLOR));
    	
    	Point[][] j = {{new Point(1,1), new Point(2,1), new Point(3,1), new Point(3,2)},
    			{ new Point(2,0), new Point(3,0), new Point(2,1), new Point(2,2) },
    			{ new Point(1,0), new Point(1,1), new Point(2,1), new Point(3,1) },
    			{ new Point(2,0), new Point(2,1), new Point(2,2), new Point(1,2) },
    	};
    	mPieces.put(new Integer("6"), new BlockGroup(4, 4, "J", j, PIECE_J_COLOR));
    	
    	Point[][] t = {{new Point(1,1), new Point(2,1), new Point(3,1), new Point(2,2)},
    			{ new Point(2,0), new Point(2,1), new Point(3,1), new Point(2,2) },
    			{ new Point(2,0), new Point(1,1), new Point(2,1), new Point(3,1) },
    			{ new Point(2,0), new Point(1,1), new Point(2,1), new Point(2,2) },
    	};
    	mPieces.put(new Integer("7"), new BlockGroup(4, 4, "T", t, PIECE_T_COLOR));
    }

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		startGameUpdateTimer();
		startPieceMoveTimer();
	}
    
	/**
	 * Starts the game update timer
	 */
    public void startGameUpdateTimer(){
    	mGameUpdateTimer = new Timer();
    	mGameUpdateTimer.schedule(new GameUpdateTask(), 0, GAME_UPDATE_PERIOD);
    }
    
    /**
     * Stops the game update timer
     */
    public void stopGameUpdateTimer(){
    	if(mGameUpdateTimer != null){
    		mGameUpdateTimer.cancel();
    	}
    }
    
    /**
     * Starts the piece movement timer
     */
    public void startPieceMoveTimer(){
    	mPieceMoveTimer = new Timer();
    	mPieceMoveTimer.schedule(new CurPieceMoveTask(), 0, getIterationDelay());
    }
    
    /**
     * Stops the piece movement timer
     */
    public void stopPieceMoveTimer(){
    	if(mPieceMoveTimer != null){
    		mPieceMoveTimer.cancel();
    	}
    }

	/**
	 * Gets the iteration delay of falling blocks, which is a function of the player level
	 * 
	 * @param level the player level
	 * @return the iteration delay in milliseconds
	 */
	private long getIterationDelay(){
		double iterationDelay = ((10.0 - mPlayer1.getLevel()) / 20.0);
		return Math.round(iterationDelay * 1000.0);
	}
    
	/**
	 * The "game loop" timer task
	 */
    private class GameUpdateTask extends TimerTask{
    	@Override
    	public void run(){
    		if(!mGameUpdating && !mIsPaused && !mIsGameOver){
    			updateGame();
    			postInvalidate();
    		}
    	}
    }
    
    /**
     * Moves and tracks iterations of the currently falling piece
     */
    private class CurPieceMoveTask extends TimerTask{
    	@Override
    	public void run(){
    		/**
    		 * Ignore the current piece if it is falling
    		 */
    		if(!mDropPiece && !mIsPaused && !mIsGameOver){
    			mFreeFallIterations++;
    			setmVMoveRequest(1);
    		}
    	}
    }

}
