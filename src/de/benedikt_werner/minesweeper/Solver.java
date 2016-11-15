package de.benedikt_werner.minesweeper;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Random;

public class Solver {
	
	private Game game;
	private int width, height, bombs;
	private boolean changeMade;
	private boolean[][] flags;

	public static void main(String[] args) {
		Solver s = new Solver();
		s.setup(9, 9, 10);
		s.start();
	}

	public Solver() {
		game = new Game();
	}
	
	public void setup(int width, int height, int bombs) {
		game.setup(width, height, bombs);
		this.width = width;
		this.height = height;
		this.bombs = bombs;
	}
	
	public void start() {
		flags = new boolean[width][height];
		game.click(width / 2, height / 2);

		game.printBoard(true);
		while (!game.isGameOver() && !game.isWon()) {
			game.printBoard(false, flags);
			changeMade = false;
			if (!nextClick()) {
				System.out.println("Game over!");
				break;
			}
			if (!changeMade) {
				System.out.println("No change made! Clicking random square!");
				if (!clickRandom()) {
					System.out.println("Game over!");
					break;
				}
			}
		}
		if (game.isWon()) System.out.println("Game solved!");
		else System.out.println("Solving failed!");
		
		game.printBoard(true);
	}
	
	private boolean nextClick() {
		int[][] board = game.getBoard();
		boolean[][] visible = game.getVisible();
		
		for (int x = 0; x < visible.length; x++) {
			for (int y = 0; y < visible[x].length; y++) {
				if (visible[x][y] && board[x][y] > 0) {
					if (!checkSquare(x, y, board, visible)) return false;
				}
			}
		}
		return true;
	}
	
	private boolean checkSquare(int x, int y, int[][] board, boolean[][] visible) {
		final LinkedList<Point> unopendSquares = new LinkedList<>();
		final int maxX = x < width - 1 ? x + 2 : width;
		final int maxY = y < height - 1 ? y + 2 : height;
		int bombs = 0;
		for (int i = (x > 0 ? x - 1 : 0); i < maxX; i++) {
			for (int j = (y > 0 ? y - 1 : 0); j < maxY; j++) {
				if (flags[i][j]) bombs++;
				else if (!visible[i][j]) unopendSquares.add(new Point(i, j));
			}
		}
		if (!unopendSquares.isEmpty()) {
			System.out.println(String.format("%d|%d = %d - b: %d, uS: %d", x, y, board[x][y], bombs, unopendSquares.size()));
			if (bombs == board[x][y]) {
				changeMade = true;
				for (Point p : unopendSquares) {
					System.out.println(String.format("Clicking %d|%d", p.x, p.y));
					if (!game.click(p)) return false;
				}
			}
			else if (bombs + unopendSquares.size() == board[x][y]) {
				changeMade = true;
				for (Point p : unopendSquares) {
					System.out.println(String.format("Flagging %d|%d", p.x, p.y));
					flags[p.x][p.y] = true;
				}
			}
		}
		return true;
	}
	
	private boolean clickRandom() {
		Random random = new Random();
		while (true) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			if (!game.isVisible(x, y)) {
				System.out.println(String.format("Clicking %d|%d", x, y));
				return game.click(x, y);
			}
		}
	}
}
