package com.mngraves.superblockstack;
/**
 *  
 * @author Michael Graves
 * 
 * Tetris block data structure
 *
 */
public class Block {
	/**
	 * If grouped, coordinates are relative to the block group grid
	 * Otherwise, they are relative to the grid
	 */
	private int mX;
	private int mY;
	private int mColor;
	
	Block(int x, int y, int color){
		mX = x;
		mY = y;
		mColor = color;
	}

	public int getmX() {
		return mX;
	}

	public int getmY() {
		return mY;
	}

	public int getmColor() {
		return mColor;
	}
	
	
}
