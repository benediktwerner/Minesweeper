package de.benedikt_werner.minesweeper;

import java.awt.Point;
import java.awt.image.BufferedImage;

public class Windows7Minesweeper extends WindowsMinesweeper {
	protected static final int IMAGE_OFFSET_X = 50, IMAGE_OFFSET_Y = 150;
	public int getImageOffsetX() {return IMAGE_OFFSET_X;}
	public int getImageOffsetY() {return IMAGE_OFFSET_X;}
	
	protected void moveMouse(int x, int y) {
		robot.mouseMove(
				windowLocation.x + IMAGE_OFFSET_X + topLeft.x + (x * squareWidth) + halfSquareWidth,
				windowLocation.y + IMAGE_OFFSET_Y + topLeft.y + (y * squareWidth) + halfSquareWidth
				);
		try {
			Thread.sleep(DELAY_AFTER_MOUSE_MOVE);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		WindowsMinesweeper ms = new Windows7Minesweeper();
		System.out.println("Taking screenshot in 3 seconds...");
		Thread.sleep(3000);
		if (!ms.detect()) return;
		
		Thread.sleep(500);
		
		Solver solver = new Solver();
		solver.solve(ms);
	}

	public boolean detect() {
		// Find window
		windowLocation = getWindowLocation("Minesweeper");
		if (windowLocation == null) {
			System.out.println("No Minesweper window found!");
			return false;
		}
		
		// Find top left corner
		boolean foundCorner = false;
		BufferedImage img = takeScreenshot();
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				if (isBlack(img.getRGB(x, y))) {
					topLeft = new Point(x, y);
					foundCorner = true;
					break;
				}
			}
			if (foundCorner) break;
		}
		if (!foundCorner) return false;
		System.out.printf("(%d|%d)\n", topLeft.x, topLeft.y);
		
		//Find bottom right corner
		int rightX = -1, bottomY = -1;
		for (int x = topLeft.x; x < img.getWidth(); x++) {
			if (!isBlack(img.getRGB(x, topLeft.y))) {
				rightX = x - 1;
				break;
			}
		}
		for (int y = topLeft.y; y < img.getHeight(); y++) {
			if (!isBlack(img.getRGB(topLeft.x, y))) {
				bottomY = y - 1;
				break;
			}
		}
		if (rightX == -1) rightX = img.getWidth() - 1;
		if (bottomY == -1) bottomY = img.getHeight() - 1;
		bottomRight = new Point(rightX, bottomY);
		
		System.out.printf("(%d|%d)\n", bottomRight.x, bottomRight.y);
		//saveImage(img);
		//saveImage(img.getSubimage(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y));
		
		//Find width and height
		Point innerCorner = null;
		for (int i = 1; i < 10; i++)
			if (!isBlack(img.getRGB(topLeft.x + i, topLeft.y + i)))
				innerCorner = new Point(topLeft.x + i, topLeft.y + i);
		
		// Auto detection failed
		if (innerCorner != null) {
			width = 0;
			boolean onSquare = true;
			for (int x = innerCorner.x; x <= bottomRight.x; x++) {
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
			for (int y = innerCorner.y; y <= bottomRight.y; y++) {
				if (onSquare && isBlack(img.getRGB(innerCorner.x, y))) {
					onSquare = false;
					height++;
				}
				else if (!onSquare && !isBlack(img.getRGB(innerCorner.x, y))) {
					onSquare = true;
				}
			}
		}

		if (innerCorner == null || width == 0 || height == 0) {
			width = readInt("Width: ");
			if (width == -1) return false;
			height = readInt("Height: ");
			if (height == -1) return false;
		}
		System.out.printf("%d %d\n", width, height);
		
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
		int countWhite = 0;
		for (int x = midX - 20; x < midX + 20; x++) {
			for (int y = midY - 20; y < midY + 20; y++) {
				int rgb = img.getRGB(x, y);
				int red = (rgb >> 16) & 0xFF;
				int green = (rgb >> 8) & 0xFF;
				int blue = rgb & 0xFF;
				if (colorDifference(red, green, blue, 240, 240, 240) < 12) countWhite++;
			}
		}
		if (countWhite > 1100) {
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

		boolean hasColorOfOneSquare = false;
		boolean hasColorOfBlank = false;
		boolean isRelativelyHomogenous = true;

		for(int rgb : areapix){
			int red = (rgb >> 16) & 0xFF;
			int green = (rgb >> 8) & 0xFF;
			int blue = rgb & 0xFF;

			// Detect death
			if(colorDifference(red, green, blue, 110,110,110) < 20) {
				gameOver = true;
				return -10;
			}

			// Detect flagging of any sort
			if(colorDifference(red,green,blue,255,0,0) < 30)
				return -1;

			if(colorDifference(red, green, blue, 65,79,188) < 20)
				hasColorOfOneSquare = true;
			
			if(blue > red && blue > green && green > red &&
					colorDifference(red, green, blue, 200,210,230) < 100){
				hasColorOfBlank = true;
			}
			if(colorDifference(red, green, blue, 165,5,5) < 30) 	return 3; //detect_3_7(areapix); //TODO: check 3 <-> 7
			if(colorDifference(red, green, blue, 30,105,5) < 30)	return 2;
			if(colorDifference(red, green, blue, 0,0,140) < 30)		return 4;
			if(colorDifference(red, green, blue, 125,0,5) < 30)		return 5;
			if(colorDifference(red, green, blue, 10,120,130) < 30)	return 6;
			if(hasColorOfOneSquare && hasColorOfBlank)				return 1;
		}

		// Determine how 'same' the area is.
		// This is to separate the empty areas which are relatively the same from
		// the unexplored areas which have a gradient of some sort.
		int rgb00 = areapix[0];
		int red00 = (rgb00 >> 16) & 0xFF;
		int green00 = (rgb00 >> 8) & 0xFF;
		int blue00 = rgb00 & 0xFF;
		for(int rgb : areapix){
			int red = (rgb >> 16) & 0xFF;
			int green = (rgb >> 8) & 0xFF;
			int blue = rgb & 0xFF;
			if(colorDifference(red, green, blue, red00, green00, blue00) > 50){
				isRelativelyHomogenous = false;
				break;
			}
		}

		if(hasColorOfBlank && isRelativelyHomogenous)
			return 0;

		return -2;
	}
	
	protected int detect_3_7(int[] areapix){
		// Assume it's length 225 and dimensions 15x15.
		// Classify each pixel as red or not.
		// Since we don't have to deal with 5, we can take a greater liberty
		// in deciding on red pixels.

		boolean redx[][] = new boolean[15][15];
		for(int k=0; k<225; k++){
			int i = k % 15;
			int j = k / 15;
			int rgb = areapix[k];
			int red = (rgb >> 16) & 0xFF;
			int green = (rgb >> 8) & 0xFF;
			int blue = rgb & 0xFF;

			if(colorDifference(red, green, blue, 170, 0, 0) < 100)
				redx[i][j] = true;
		}

		// . . .
		//   x
		for(int i=0; i<13; i++){
			for(int j=0; j<13; j++){
				if(!redx[i][j] && !redx[i][j+1] && !redx[i][j+2] && redx[i+1][j+1])
					return 3;
			}
		}

		return 7;
	}
}
