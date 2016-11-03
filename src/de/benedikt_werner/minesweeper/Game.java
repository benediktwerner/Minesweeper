package de.benedikt_werner.minesweeper;

import java.awt.Point;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

public class Game {
	private int[][] board;
	private boolean[][] visible;
	private boolean gameOver;
	private int openSquares;
	private int squaresToOpen;
	
	private boolean generated;
	private int width, height, bombs;
	
	public void setup(int width, int height, int bombs) {
		this.width = width;
		this.height = height;
		this.bombs = bombs;
		generated = false;
		
		board = new int[height][width];
		visible = new boolean[height][width];
	}
	
	private void generate(Point start) {
		if (bombs + 9 > width*height) {
			throw new IllegalArgumentException("Too many bombs: " + bombs + " bombs on " + (width*height) + " squares.");
		}
		gameOver = false;
		board = new int[height][width];
		visible = new boolean[height][width];
		
		LinkedList<Point> possibleBombs = new LinkedList<Point>();
		for (int x = 0; x < height; x++) {
			for (int y = 0; y < width; y++) {
				if (x >= start.x - 1 && x <= start.x + 1 &&
						y >= start.y - 1 && y <= start.y + 1) {
					continue;
				}
				possibleBombs.add(new Point(x, y));
			}
		}
		
		Random random = new  Random();
		for (int i = 0; i < bombs; i++) {
			placeBomb(possibleBombs.remove(random.nextInt(possibleBombs.size())));
		}
		
		openSquares = 0;
		squaresToOpen = width * height - bombs;
	}
	
	public boolean click(Point p) {
		return click(p.x, p.y);
	}
	
	public boolean click(int x, int y) {
		if (!generated) {
			generate(new Point(x, y));
			generated = true;
		}
		if (visible[x][y]) return true;
		
		if (board[x][y] == -1) {
			gameOver();
			return false;
		}
		if (board[x][y] == 0) {
			floodOpen(x, y);
		}
		else {
			visible[x][y] = true;
			openSquares++;
		}
		return true;
	}
	
	private void gameOver() {
		gameOver = true;
		for (int x = 0; x < visible.length; x++) {
			for (int y = 0; y < visible[x].length; y++) {
				visible[x][y] = true;
			}
		}
	}
	
	public boolean isGameOver() {
		return gameOver;
	}
	
	private void floodOpen(int x, int y) {
		LinkedList<Point> stack = new LinkedList<>();
		stack.add(new Point(x, y));
		
		while (!stack.isEmpty()) {
			Point p = stack.removeFirst();
			if (p.x < 0 || p.x >= board.length ||
					p.y < 0 || p.y >= board[0].length ||
					getVisible(p)) {
				continue;
			}
			visible[p.x][p.y] = true;
			openSquares++;
			if (getPos(p) == 0) {
				for (int a = p.x - 1; a <= p.x + 1;  a++) {
					for (int b = p.y - 1; b <= p.y + 1; b++) {
						if (a == p.x && b == p.y) continue;
						stack.addLast(new Point(a, b));
					}
				}
			}
		}
	}
	
	public boolean isWon() {
		return generated && openSquares == squaresToOpen;
	}
	
	private boolean getVisible(Point p) {
		return visible[p.x][p.y];
	}
	
	private int getPos(Point p) {
		return board[p.x][p.y];
	}
	
	public void printBoard(boolean showAll) {
		String line = "+";
		for (int i = 0; i < board[0].length; i++) {
			line += "-";
		}
		System.out.println(line + "+");
		
		for (int x = 0; x < board.length; x++) {
			String s = "|";
			for (int y = 0; y < board[x].length; y++) {
				if (!showAll && !visible[x][y]) {
					s += "°";
					continue;
				}
				
				switch (board[x][y]) {
					case -1: s += "X"; break;
					case  0: s += " "; break;
					default: s += board[x][y] + "";
				}
			}
			System.out.println(s + "|");
		}
		System.out.println(line + "+");
	}
	
	private void placeBomb(Point position) {
		board[position.x][position.y] = -1;
		for (int x = position.x - 1; x <= position.x + 1; x++) {
			for (int y = position.y - 1; y <= position.y + 1; y++) {
				if ((x == position.x && y == position.y) ||
						x < 0 || x >= board.length ||
						y < 0 || y >= board[x].length ||
						board[x][y] == -1) {
					continue;
				}
				
				board[x][y]++;
			}
		}
	}
	
	public int[][] getBoard() {
		return board;
	}
	
	public boolean[][] getVisible() {
		return visible;
	}
	
	public int getTotalBombCount() {
		return bombs;
	}
}
