package de.benedikt_werner.minesweeper;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public abstract class WindowsMinesweeper implements Minesweeper {
	
	protected static final int DELAY_BETWEEN_CLICKS = 10;
	protected static final int DELAY_AFTER_CLICKS = 25;
	protected static final int DELAY_AFTER_MOUSE_MOVE = 10;
	
	protected Robot robot;
	protected boolean boardDetected = false;
	protected int width = 0, height = 0, totalBombs = 0;
	protected int squareWidth, halfSquareWidth;
	protected Point topLeft, bottomRight;
	protected Rectangle windowLocation;
	protected boolean gameOver;
	
	public WindowsMinesweeper() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			throw new IllegalStateException("Unable to instantiate robot", e);
		}
	}
	
	public abstract boolean detect();
	public abstract int[][] getBoard();
	public abstract int getImageOffsetX();
	public abstract int getImageOffsetY();
	
	protected void moveMouse(int x, int y) {
		robot.mouseMove(
				windowLocation.x + getImageOffsetX() + topLeft.x + (x * squareWidth) + halfSquareWidth,
				windowLocation.y + getImageOffsetY() + topLeft.y + (y * squareWidth) + halfSquareWidth
				);
		try {
			Thread.sleep(DELAY_AFTER_MOUSE_MOVE);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void click(int x, int y) {
		if (!boardDetected) throw new IllegalStateException("No detected board");
		try {
			moveMouse(x, y);
			robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			Thread.sleep(DELAY_BETWEEN_CLICKS);
			robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			Thread.sleep(DELAY_AFTER_CLICKS);
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
			moveMouse(x, y);
			robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
			Thread.sleep(DELAY_BETWEEN_CLICKS);
			robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
			Thread.sleep(DELAY_AFTER_CLICKS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void chord(int x, int y) {
		if (!boardDetected) throw new IllegalStateException("No detected board");
		try {
			moveMouse(x, y);
			robot.mousePress(InputEvent.BUTTON3_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK);
			Thread.sleep(DELAY_BETWEEN_CLICKS);
			robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK);
			Thread.sleep(DELAY_AFTER_CLICKS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean isGameOver() {
		if (!boardDetected) throw new IllegalStateException("No detected board");
		return gameOver;
	}

	protected int colorDifference(int r1, int g1, int b1, int r2, int g2, int b2){
		return Math.abs(r1 - r2) + Math.abs(b1 - b2) + Math.abs(g1 - g2);
	}
	
	protected BufferedImage takeScreenshot(){
		if (windowLocation == null) return null;
		BufferedImage img = robot.createScreenCapture(windowLocation);
		return img.getSubimage(getImageOffsetX(), getImageOffsetY(), img.getWidth() - 2 * getImageOffsetX(), img.getHeight() - getImageOffsetX() - getImageOffsetX());
	}

	private interface User32 extends StdCallLibrary {
		User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class,
				W32APIOptions.DEFAULT_OPTIONS);

		HWND FindWindow(String lpClassName, String lpWindowName);

		int GetWindowRect(HWND handle, int[] rect);
	}

	protected static Rectangle getWindowLocation(String windowName) {
		HWND hwnd = User32.INSTANCE.FindWindow(null, windowName);
		if (hwnd == null) return null;

		int[] rect = {0, 0, 0, 0};
		int result = User32.INSTANCE.GetWindowRect(hwnd, rect);
		if (result == 0) return null;

		return new Rectangle(rect[0], rect[1], rect[2] - rect[0], rect[3] - rect[1]);
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
	
	protected int readInt(String text) {
		while (true) {
			String s = JOptionPane.showInputDialog(null, text);
			if (s == null) return -1;
	        try {
	            return Integer.parseInt(s.trim());
	        } catch (NumberFormatException e) {}
		}
	}
	
	protected boolean isBlack(int i) {
		int red = (i >> 16) & 0xFF;
		int green = (i >> 8) & 0xFF;
		int blue = i & 0xFF;
		return (red < 30 && green < 30 && blue < 35);
	}
	
	public static void saveImage(BufferedImage img) {
		File file = new File("C:\\Users\\Bene\\Downloads\\Image-"+System.currentTimeMillis()+".png");
		try {
			ImageIO.write(img, "png", file);
			System.out.println("Saved image");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
