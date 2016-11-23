package de.benedikt_werner.minesweeper;

import java.awt.Point;
import java.awt.image.BufferedImage;

public class Windows10Minesweeper extends WindowsMinesweeper {
	
	private static final int CORNER_DETECTION_JUMP = 10;
	protected static final int IMAGE_OFFSET_X = 150, IMAGE_OFFSET_Y = 450;
	public int getImageOffsetX() {return IMAGE_OFFSET_X;}
	public int getImageOffsetY() {return IMAGE_OFFSET_X;}

	public static void main(String[] args) throws InterruptedException{
		WindowsMinesweeper ms = new Windows10Minesweeper();
		System.out.print("Taking screenshot in 3 seconds...");
		Thread.sleep(300);
		System.out.print("3..");
		Thread.sleep(1000);
		System.out.print("2..");
		Thread.sleep(1000);
		System.out.print("1..");
		Thread.sleep(1000);
		if (!ms.detect()) return;
		ms.getBoard();
		
		Thread.sleep(500);
		
		Solver solver = new Solver();
		solver.solve(ms);
	}

	public boolean detect() {
		// Find window
		windowLocation = getWindowLocation("Microsoft Minesweeper");
		if (windowLocation == null) {
			System.out.println("No Minesweper window found!");
			return false;
		}
		
		boolean foundCorner = false;
		BufferedImage img = takeScreenshot();
		System.out.println("Done.");
		System.out.println("Detecting board...");

		// Find top left corner
		for (int y = 0; y < img.getHeight(); y++) {
			for (int i = 0; i < y; i++) {
				if (!isBlack(img.getRGB(i, y-i))) {
					topLeft = new Point(i, y-i);
					foundCorner = true;
					break;
				}
			}
			if (foundCorner) break;
		}
		if (!foundCorner) return false;
		int origY = topLeft.y;
		for (int y = topLeft.y; y > 0; y--) {
			if (isBlack(img.getRGB(topLeft.x + CORNER_DETECTION_JUMP, y))) {
				topLeft.y = y + 1;
				break;
			}
		}
		for (int x = topLeft.x; x > 0; x--) {
			if (isBlack(img.getRGB(x, origY + CORNER_DETECTION_JUMP))) {
				topLeft.x = x + 1;
				break;
			}
		}
		System.out.printf("Found top left corner: (%d|%d)\n", topLeft.x, topLeft.y);
		
		//Find bottom right corner
		foundCorner = false;
		for (int y = 0; y < img.getHeight(); y++) {
			for (int i = 1; i < y; i++) {
				if (!isBlack(img.getRGB(img.getWidth() - i, img.getHeight() - y + i))) {
					bottomRight = new Point(img.getWidth() - i, img.getHeight() - y + i);
					foundCorner = true;
					break;
				}
			}
			if (foundCorner) break;
		}
		if (!foundCorner) return false;
		origY = bottomRight.y;
		for (int y = bottomRight.y; y < img.getWidth(); y++) {
			if (isBlack(img.getRGB(bottomRight.x - CORNER_DETECTION_JUMP, y))) {
				bottomRight.y = y - 1;
				break;
			}
		}
		for (int x = bottomRight.x; x < img.getWidth(); x++) {
			if (isBlack(img.getRGB(x, origY + CORNER_DETECTION_JUMP))) {
				bottomRight.x = x - 1;
				break;
			}
		}
		System.out.printf("Found bottom right corner: (%d|%d)\n", bottomRight.x, bottomRight.y);
//		saveImage(img);
//		saveImage(img.getSubimage(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y));
		System.out.println("Found board.");
		System.out.println("Detecting width and height...");
		
		//Find width and height
		Point innerCorner = new Point(topLeft.x + 15, topLeft.y + 15);
		Point innerCornerEnd = new Point(bottomRight.x + 5, bottomRight.y + 5);
		width = 0;
		boolean onSquare = true;
		for (int x = innerCorner.x; x <= innerCornerEnd.x; x++) {
			if (onSquare && isBlack(img.getRGB(x, innerCorner.y))) {
				onSquare = false;
				width++;
			}
			else if (!onSquare && !isBlack(img.getRGB(x, innerCorner.y))) {
				onSquare = true;
			}
		}
		height = 0;
		onSquare = true;
		for (int y = innerCorner.y; y <= innerCornerEnd.y; y++) {
			if (onSquare && isBlack(img.getRGB(innerCorner.x, y))) {
				onSquare = false;
				height++;
			}
			else if (!onSquare && !isBlack(img.getRGB(innerCorner.x, y))) {
				onSquare = true;
			}
		}

		if (width == 0 || height == 0) {
			System.out.println("Automatic width and height detection failed.");
			width = readInt("Width: ");
			if (width == -1) return false;
			height = readInt("Height: ");
			if (height == -1) return false;
		}
		else System.out.printf("Detected width and height of board: %dx%d\n", width, height);

		// Ask for number of bombs
		totalBombs = readInt("Bombs: ");
		if (totalBombs == -1) return false;

		// Calculate values
		squareWidth = Math.round((bottomRight.x - topLeft.x) / (float) width);
		halfSquareWidth = squareWidth / 2;
		boardDetected = true;
		gameOver = false;

//		System.out.println(squareWidth + ", " + halfSquareWidth);
//		int x = topLeft.x + squareWidth + halfSquareWidth;
//		int y = topLeft.y + squareWidth + halfSquareWidth;
//		saveImage(takeScreenshot().getSubimage(x - 7, y - 7, 15, 15));
		return true;
	}
	
	public int[][] getBoard() {
		if (!boardDetected) throw new IllegalStateException("No detected board");

		moveMouse(-1, -1);
		BufferedImage img = takeScreenshot();
		
		int midX = img.getWidth() / 2;
		int midY = img.getHeight() / 2;
		
		// Try to detect game over dialog box
		int countBlack = 0;
		for (int x = midX - 20; x < midX + 20; x++) {
			for (int y = midY - 20; y < midY + 20; y++) {
				if (isBlack(img.getRGB(x, y))) countBlack++;
			}
		}
		if (countBlack > 1100) {
			gameOver = true;
			return null;
		}
		
		int[][] board = new int[width][height];
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int i = detect_number(img, x, y);
				if (i == -10) return null;
				else board[x][y] = i;
			}
		}

		// Print
		String line = "+";
		for (int i = 0; i < board.length; i++) {
			line += "-";
		}
		System.out.println(line + "+");
		
		for (int y = 0; y < board[0].length; y++) {
			String s = "|";
			for (int x = 0; x < board.length; x++) {
				switch (board[x][y]) {
					case -2: s += "*"; break;
					case -1: s += "X"; break;
					case  0: s += " "; break;
					default: s += board[x][y] + "";
				}
			}
			System.out.println(s + "|");
		}
		System.out.println(line + "+");
		
		return board;
	}
	
	/**
	 * @return -10: bomb, -2: unknown, -1: flag, 0: empty, 1+: number
	 */
	protected int detect_number(BufferedImage img, int x, int y){
		int imgX = topLeft.x + x * squareWidth + halfSquareWidth;
		int imgY = topLeft.y + y * squareWidth + halfSquareWidth;

		// Take a 15x15 area of pixels
		int areapix[] = new int[625];
		int count = 0;
		for(int i = imgX-12; i <= imgX+12; i++) {
			for(int j = imgY-12; j <= imgY+12; j++){
				areapix[count] = img.getRGB(i,j);
				count++;
			}
		}

		boolean hasColorOfOne = false;
		boolean hasColorOfFive = false;
		boolean hasColorOfBlank = false;

		for(int rgb : areapix){
			int red = (rgb >> 16) & 0xFF;
			int green = (rgb >> 8) & 0xFF;
			int blue = rgb & 0xFF;

			// Detect death
			if(colorDifference(red, green, blue, 0,20,60) < 30) {
				gameOver = true;
				return -10;
			}

			// Detect flagging of any sort
			if(colorDifference(red,green,blue,250,205,60) < 30)
				return -1;

			if(colorDifference(red, green, blue, 25,188,225) < 20)
				hasColorOfOne = true;
			
			if(colorDifference(red, green, blue, 187,41,41) < 20)
				hasColorOfFive = true;
			
			if(colorDifference(red, green, blue, 255,255,255) < 10){
				hasColorOfBlank = true;
			}
			//TODO: Missing 6,7 and 8
			if (colorDifference(red, green, blue, 227,87,130) < 20) return 3;
			if (colorDifference(red, green, blue, 150, 180, 90) < 30)	return 2;
			if (colorDifference(red, green, blue, 30,87,192) < 25)	return 4;
			if (hasColorOfBlank && hasColorOfFive)					return 5;
			if (hasColorOfOne && hasColorOfBlank)					return 1;
		}

		if (hasColorOfBlank) return 0;
		else return -2;
	}
	
	protected boolean isBlack(int color) {
		int red = (color >> 16) & 0xFF;
		int green = (color >> 8) & 0xFF;
		int blue = color & 0xFF;
		return (red < 60 && green < 65 && blue < 70);
	}
}
