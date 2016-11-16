package de.benedikt_werner.minesweeper;

import java.awt.Point;

public interface Minesweeper {
	public default void click(Point p) {
		click(p.x, p.y);
	}
	public void click(int x, int y);
	
	public boolean isGameOver();
	
	public int[][] getBoard();
	
	public int getWidth();
	public int getHeight();
	public int getTotalBombCount();
}
