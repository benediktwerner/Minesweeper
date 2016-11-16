package de.benedikt_werner.minesweeper;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Random;

public class Solver {

	private boolean[][] flags;
	private boolean[][] solved;
	private int[][] board; //-2: unknown, -1: bomb, 0: empty, 1+: number
	private Minesweeper ms;
	private int width, height, bombs;
	private boolean changeMade;

	public static void main(String[] args) {
		Solver s = new Solver();
		Game game = new Game();
		game.setup(9, 9, 10);
		s.solve(game);
		game.printBoard(true);
		game.printBoard(false);
		System.out.println("Game over: " + game.isGameOver());
		System.out.println("Game won: " + game.isWon());
	}
	
	public void solve(Minesweeper minesweeper) {
		ms = minesweeper;
		width = ms.getWidth();
		height = ms.getHeight();
		bombs = ms.getTotalBombCount();
		flags = new boolean[width][height];
		solved = new boolean[width][height];
		
		// Start solving
		ms.click(width / 2, height / 2);

		while (!ms.isGameOver()) {
			changeMade = false;
			
			nextMove();
			
			if (!changeMade) {
				System.out.println("No change made! Clicking random square!");
				clickRandom();
			}
		}
	}
	
	private void nextMove() {
		board =  ms.getBoard();
		for (int x = 0; x < board.length; x++) {
			for (int y = 0; y < board[x].length; y++) {
				if (!solved[x][y]) {
					switch (board[x][y]) {
						case -2: continue;
						case -1: continue;
						case  0: solved[x][y] = true; continue;
						default: solved[x][y] = checkSquare(x, y);
					}
				}
			}
		}
	}
	
	private boolean checkSquare(int x, int y) {
		final LinkedList<Point> unopendSquares = new LinkedList<>();
		final int maxX = x < width - 1 ? x + 2 : width;
		final int maxY = y < height - 1 ? y + 2 : height;
		int bombsAround = 0;
		
		// Get information about surrounding squares
		for (int i = (x > 0 ? x - 1 : 0); i < maxX; i++) {
			for (int j = (y > 0 ? y - 1 : 0); j < maxY; j++) {
				if (flags[i][j]) bombsAround++;
				else if (board[i][j] == -2) unopendSquares.add(new Point(i, j));
			}
		}
		
		// Evaluate information
		if (!unopendSquares.isEmpty()) {
			System.out.println(String.format("%d|%d = %d - b: %d, uS: %d", x, y, board[x][y], bombsAround, unopendSquares.size()));
			if (bombsAround == board[x][y]) { // Enough bombs marked around
				changeMade = true;
				for (Point p : unopendSquares) {
					System.out.println(String.format("Clicking %d|%d", p.x, p.y));
					ms.click(p);
				}
			}
			else if (bombsAround + unopendSquares.size() == board[x][y]) { // All unknowns around must be bombs
				changeMade = true;
				for (Point p : unopendSquares) {
					System.out.println(String.format("Flagging %d|%d", p.x, p.y));
					flags[p.x][p.y] = true;
					solved[p.x][p.y] = true;
				}
			}
			else return false;
		}
		return true;
	}
	
	private void clickRandom() {
		Random random = new Random();
		while (true) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			if (board[x][y] == -2) {
				System.out.println(String.format("Clicking %d|%d", x, y));
				ms.click(x, y);
			}
		}
	}
}
