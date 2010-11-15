package com.mngraves.superblockstack;

import android.graphics.Point;
import android.util.Log;
/**
 *  
 * @author Michael Graves
 * 
 * The block group forms a tetromino - a geometric shape containing four blocks
 */
public class BlockGroup {
	private static final String TAG = "BlockGroup";
	private int mColumns;
	private int mRows;
	private String mLabel;
	/** coordinates on the main grid **/
	private int mX;
	private int mY;
	private int mPrevX;
	private int mPrevY;
	private int mBlockColor;
	
	/** the local grid **/
	Block[][] mGrid;
	/** coordinates of blocks on this grid **/
	private Point[] mBlockPoints;
	/** holds the points for rotated groups **/
	private Point[][] mBlockGroupLayouts;
	private int mCurBlockGroupLayoutIndex;
	private int mBlockCount = 0;
	private int mLeftEdge = 0;
	private int mRightEdge = 0;
	private boolean mIsPreviewSize = false;
	
	BlockGroup(int cols, int rows, String label, Point[][] points, int blockColor){
		Point[] curPoints = points[0];
		mBlockGroupLayouts = points;
		mCurBlockGroupLayoutIndex = 0;
		mX = 0;
		mY = 0;
		mPrevX = 0;
		mPrevY = 0;
		mColumns = cols;
		mRows = rows;
		mLabel = label;
		mBlockColor = blockColor;
		initializeGrid(curPoints);
	}
	
	/**
	 * Clears complete rows and returns number of rows cleared
	 * @return result number of rows cleared
	 */
	public int clearRows(){
		Log.d(TAG, "Clearing rows...");
		int result = 0;
		boolean rowFull = true;
		
		for(int y = 0; y < mGrid.length; y++){
			rowFull = true;
			for(int x = 0; x < mGrid[y].length && rowFull; x++){
				if(mGrid[y][x] == null){
					rowFull = false;
				}
			}
			if(rowFull){
				Log.d(TAG, "Detected full row: " + y);
				mGrid[y] = new Block[mColumns];
				mBlockCount -= mColumns;
				result++;
				/**
				 * Shift rows down
				 */
				shiftRowsDown(y);
			}
		}
		
		return result;
	}
	
	/**
	 * Returns true if the current block group is to be drawn as a preview
	 * @return true if the block group is to be drawn as a preview
	 */
	public boolean getmIsPreviewSize(){
		return mIsPreviewSize;
	}
	
	/**
	 * Sets whether or not the block group will be drawn as a preview
	 * @param val boolean value indicating whether or not the group is to be drawn as a preview
	 */
	public void setmIsPreviewSize(boolean val){
		mIsPreviewSize = val;
	}
	
	/**
	 * Shifts all rows above the empty row down one row
	 * @param emptyRow the empty row number
	 */
	private void shiftRowsDown(int emptyRow){
		Log.d(TAG, "Shifting Rows down from " + emptyRow);
		for(int y = emptyRow-1; y >= 0; y--){
			for(int x = 0; x < mGrid[y].length; x++){
				if(mGrid[y][x] != null){
					mGrid[y+1][x] = new Block(x, y+1, mGrid[y][x].getmColor());
					mGrid[y][x] = null;
				}
			}
		}
	}
	
	/**
	 * Add blocks to the empty (local) grid
	 * @param points locations of the blocks in this group
	 */
	private void initializeGrid(Point[] points){
		mGrid = new Block[mRows][mColumns];
		mBlockCount = 0;
		mLeftEdge = mColumns;
		mRightEdge = 0;
		
		for(int i = 0; i < points.length; i++){
			if(points[i] != null){
				mGrid[points[i].y][points[i].x] = new Block(points[i].x, points[i].y, mBlockColor);
				mBlockCount++;
				mLeftEdge = points[i].x < mLeftEdge ? points[i].x : mLeftEdge;
				mRightEdge = points[i].x > mRightEdge ? points[i].x : mRightEdge;
			}
		}
	}
	
	/**
	 * @return the x coordinate value of the leftmost block in this group
	 */
	public int getLeftEdge(){
		return mLeftEdge;
	}
	
	/**
	 * 
	 * @return the x coordinate value of the leftmost block in this group
	 */
	public int getRightEdge(){
		return mRightEdge+1;
	}
	
	/**
	 * Attempt to undo a downward movement of this block group on the game grid
	 * @return true if the downward movement undo was successful
	 */
	public boolean backOffDownMovement(){
		/**
		 * If current position is equal to previous position we can't back off, return false
		 */
		if(mY == mPrevY){
			//Log.d(TAG, "Back off movement failed.");
			return false;
		}
		mY = mPrevY;
		return true;
	}
	
	/**
	 * Attempt to undo a horizontal movement of this block group on the game grid
	 * @return true if the horizontal movement undo was successful
	 */
	public boolean backOffHMovement(){
		if(mX == mPrevX){
			return false;
		}
		mX = mPrevX;
		return true;
	}
	
	/**
	 * Attempt to undo a rotation of this block group
	 * @return true if the rotation movement undo was successful
	 */
	public boolean backOffRotation(){
		if(mBlockGroupLayouts.length <= 1){
			return false;
		}
		mCurBlockGroupLayoutIndex--;
		if(mCurBlockGroupLayoutIndex < 0){
			mCurBlockGroupLayoutIndex = mBlockGroupLayouts.length - 1;
		}
		initializeGrid(mBlockGroupLayouts[mCurBlockGroupLayoutIndex]);
		return true;
	}
	
	/**
	 * Determine all occupied cells in the local grid
	 * @return an array of Points indicating which cells are occupied by blocks
	 */
	public Point[] getBlockPoints(){
		Point[] points = new Point[mRows*mColumns];
		int counter = 0;
		
		for(int y = 0; y < mGrid.length; y++){
			for(int x = 0; x < mGrid[y].length; x++){
				if(mGrid[y][x] != null){
					points[counter] = new Point(mGrid[y][x].getmX(), mGrid[y][x].getmY());
					counter++;
				}
			}
		}
		return points;
	}
	/**
	 * Add a block to this block group
	 * @param block the block to add
	 * @param x the column of the new block
	 * @param y the row of the new block
	 * @return true if the block is successfully added to the grid
	 */
	public boolean addBlock(Block block){
		int x = block.getmX();
		int y = block.getmY();
		
		if(x >= 0 && x < mColumns && y >= 0 && y < mRows && isSpaceEmpty(x, y)){
			mGrid[y][x] = new Block(block.getmX(), block.getmY(), block.getmColor());
			//mBlockPoints[mBlockCount] = new Point(x, y);
			mBlockCount++;
			//Log.d(TAG, "Add block success | (" + x + ", " + y + ")");
			return true;
		}
		//Log.d(TAG, "Add block failed | (" + x + ", " + y + ")");
		return false;
	}
	
	/**
	 * Add an entire block group to this block group
	 * @param blockGroup the block group to add
	 * @return true if the block group add was a success
	 */
	public boolean addBlockGroup(BlockGroup blockGroup){
		// TODO: replace two loops with more efficient algorithm
		
		Point[] points = blockGroup.getBlockPoints();
		/**
		 * First, check for conflicting blocks
		 */
		for(int i = 0; i < blockGroup.getmBlockCount(); i++){
			if(points[i] != null){
				if(!isSpaceEmpty((points[i].x+blockGroup.getmX()), (points[i].y+blockGroup.getmY()))){
					return false;
				}
			}
		}
		
		/**
		 * If no conflicts, add blocks to this grid
		 */
		for(int i = 0; i < blockGroup.getmBlockCount(); i++){
			if(points[i] != null){
				Block curBlock = blockGroup.getBlock(points[i].x, points[i].y);
				Block newBlock = new Block(curBlock.getmX()+blockGroup.getmX(), curBlock.getmY()+blockGroup.getmY(), curBlock.getmColor());
				addBlock(newBlock);
			}
		}
		return true;
	}
	
	/**
	 * Checks if a cell in the block group is empty
	 * @param x the column on the grid
	 * @param y the row on the grid
	 * @return true if the cell is empty
	 */
	public boolean isSpaceEmpty(int x, int y){
		if(x >= 0 && x < mColumns && y >= 0 && y < mRows){
			return mGrid[y][x] == null;
		}
		return false;
	}
	
	/**
	 * Rotate the block group counter-clockwise
	 */
	public void rotate(){
		if(mBlockGroupLayouts.length > 1){
			mCurBlockGroupLayoutIndex++;
			if(mCurBlockGroupLayoutIndex >= mBlockGroupLayouts.length){
				mCurBlockGroupLayoutIndex = 0;
			}
			initializeGrid(mBlockGroupLayouts[mCurBlockGroupLayoutIndex]);
		}
	}
	
	/**
	 * Get a block from the specified cell - use isSpaceEmpty beforehand
	 * @param x the x value of the block coordinate
	 * @param y the y value of the block coordinate
	 * @return the block occupying the specified cell
	 */
	public Block getBlock (int x, int y){
		return mGrid[y][x];
	}
	
	public Point[][] getmBlockGroupLayouts(){
		return mBlockGroupLayouts;
	}

	public int getmBlockCount() {
		return mBlockCount;
	}

	public int getmColumns() {
		return mColumns;
	}

	public int getmRows() {
		return mRows;
	}

	public int getmX() {
		return mX;
	}

	public int getmY() {
		return mY;
	}

	public int getmBlockColor() {
		return mBlockColor;
	}

	public void setmX(int mX) {
		this.mPrevX = this.mX;
		this.mX = mX;
	}

	public void setmY(int mY) {
		this.mPrevY = this.mY;
		this.mY = mY;
	}
	
	
}
