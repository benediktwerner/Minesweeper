package de.benedikt_werner.minesweeper;

import java.awt.AWTException;
import java.awt.Color;
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

public class WindowsMinesweeper implements Minesweeper {
	
	private Robot robot;
	private boolean boardDetected = false;
	private int width = 0, height = 0, totalBombs = 0;
	private int squareWidth, halfSquareWidth;
	private Point topLeft, bottomRight;
	private Rectangle windowLocation;
	
	public static void main(String[] args) throws InterruptedException {
		WindowsMinesweeper ms = new WindowsMinesweeper();
		System.out.println("Taking screenshot in 3 seconds...");
		Thread.sleep(3000);
		ms.detect();
	}
	
	public WindowsMinesweeper() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			throw new IllegalStateException("Unable to instantiate robot", e);
		}
	}
	
	public void detect() {
		windowLocation = getWindowLocation("Minesweeper");
		if (windowLocation == null) {
			System.out.println("No Minesweper window found!");
			return;
		}
		
		BufferedImage img = takeScreenShot();
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				Color c = new Color(img.getRGB(x, y));
				// TODO: If black: top left corner
				// TODO: go down and right to find other corner
			}
		}
		
		width = readInt("Width: ");
		height = readInt("Height: ");
		totalBombs = readInt("Bombs: ");
		
		squareWidth = Math.round((bottomRight.y - topLeft.y) / height);
		halfSquareWidth = squareWidth / 2;
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
		
		BufferedImage img = takeScreenShot();
		// TODO: detect game over dialog
		
		return false;
	}

	public int[][] getBoard() {
		if (!boardDetected) throw new IllegalStateException("No detected board");
		
		BufferedImage img = takeScreenShot();
		int[][] board = new int[width][height];
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Color c = new Color(img.getRGB(topLeft.x + x * squareWidth + halfSquareWidth, topLeft.y + y * squareWidth + halfSquareWidth));

				// TODO: if ... board[x][y] = ..;
			}
		}
		
		return board;
	}
	
	private BufferedImage takeScreenShot(){
		if (windowLocation == null) return null;
		BufferedImage img = robot.createScreenCapture(windowLocation);
		return img.getSubimage(50, 150, img.getWidth() - 100, img.getHeight() - 200);
	}

	private interface User32 extends StdCallLibrary {
		User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class,
				W32APIOptions.DEFAULT_OPTIONS);

		HWND FindWindow(String lpClassName, String lpWindowName);

		int GetWindowRect(HWND handle, int[] rect);
	}

	private static Rectangle getWindowLocation(String windowName) {
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
	
	private int readInt(String text) {
		while (true) {
			String s = JOptionPane.showInputDialog(null, text);
			if (s == null) return -1;
	        try {
	            return Integer.parseInt(s.trim());
	        } catch (NumberFormatException e) {}
		}
	}
	
	private void saveImage(BufferedImage img) {
		File file = new File("C:\\Users\\Bene\\Downloads\\Image-"+System.currentTimeMillis()+".png");
		try {
			ImageIO.write(img, "png", file);
			System.out.println("Saved image");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
