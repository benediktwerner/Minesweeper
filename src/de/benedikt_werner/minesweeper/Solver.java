package de.benedikt_werner.minesweeper;

import java.awt.Point;

public class Solver {
	
	private Game game;
	private int width, height, bombs;

	public static void main(String[] args) {
		Solver s = new Solver();
		s.setup(10, 10, 10);
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
		game.click(width / 2, height / 2);
		
		while (!game.isGameOver()) {
			nextClick();
		}
		if (game.isWon()) System.out.println("Game solved!");
		else System.out.println("Solving failed!");
	}
	
	private void nextClick() {
		int[][] board = game.getBoard();
		boolean[][] visible = game.getVisible();
		
		for (int x = 0; x < visible.length; x++) {
			for (int y = 0; y < visible[x].length; y++) {
				if (visible[x][y]) {
					// TODO: Find square to click and click
				}
			}
		}
	}
}
