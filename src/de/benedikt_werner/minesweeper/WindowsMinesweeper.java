package de.benedikt_werner.minesweeper;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;

import javax.swing.JOptionPane;

public class WindowsMinesweeper implements Minesweeper {
	
	private Robot robot;
	private boolean boardDetected = false;
	private int width = 0, height = 0, totalBombs = 0;
	private int squareWidth;
	private Point topLeft, bottomRight;
	
	public WindowsMinesweeper() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			throw new IllegalStateException("Unable to instantiate robot", e);
		}
	}
	
	public void detect() {
		//TODO: Detect board
		
		width = readInt("Width: ");
		height = readInt("Height: ");
		totalBombs = readInt("Bombs: ");
		
		squareWidth = Math.round((bottomRight.y - topLeft.y) / height);
		boardDetected = true;
	}

	public void click(int x, int y) {
		if (!boardDetected) throw new IllegalStateException("No detected board");
		try {
			robot.mouseMove(topLeft.x + (x * squareWidth) + (squareWidth / 2), topLeft.y + (y * squareWidth) + (squareWidth / 2));
			Thread.sleep(300);
			robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			Thread.sleep(300);
			robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param flag is not supported by WindowsMinesweeper. This function just cycles through the possible flag states
	 */
	public void flag(int x, int y, boolean flag) {
		if (!boardDetected) throw new IllegalStateException("No detected board");
		try {
			robot.mouseMove(topLeft.x + (x * squareWidth) + (squareWidth / 2), topLeft.y + (y * squareWidth) + (squareWidth / 2));
			Thread.sleep(300);
			robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
			Thread.sleep(300);
			robot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean isGameOver() {
		if (!boardDetected) throw new IllegalStateException("No detected board");
		// TODO Auto-generated method stub
		return false;
	}

	public int[][] getBoard() {
		if (!boardDetected) throw new IllegalStateException("No detected board");
		// TODO Auto-generated method stub
		return null;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getTotalBombCount() {
		return totalBombs;
	}
	
	private int readInt(String text) {
		while (true) {
			String s = JOptionPane.showInputDialog(null, text);
			if (s == null) return -1;
	        try {
	            return Integer.parseInt(s.trim());
	        } catch (NumberFormatException e) {}
		}
	}
}
