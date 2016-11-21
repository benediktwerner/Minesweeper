package de.benedikt_werner.minesweeper;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

public class Solver {
	
	private static final int COMPLETE_SOLVER_LIMIT = 15;

	private boolean[][] flags;
	private boolean[][] solved;
	private int[][] bombsAround;
	private int[][] board; //-2: unknown, -1: bomb, 0: empty, 1+: number
	private HashSet<Point> borderSquares;
	private HashSet<Point> unopendSquares;
	private HashSet<boolean[]> combinations;
	private ArrayList<Point> borderList;
	private Minesweeper ms;
	private int width, height, bombsLeft;
	private boolean changeMade, recurseComplete;

	public static void main(String[] args) {
		Solver s = new Solver();
		Game game = new Game();
		game.setup(20, 20, 60);
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
		bombsLeft = ms.getTotalBombCount();
		flags = new boolean[width][height];
		solved = new boolean[width][height];
		bombsAround = new int[width][height];
		
		// Start solving
		ms.click(width / 2, height / 2);

		while (!ms.isGameOver()) {
			nextMove();
		}
	}
	
	private void nextMove() {
		changeMade = false;
		board =  ms.getBoard();
		borderSquares = new HashSet<>();
		unopendSquares = new HashSet<>();
		
		// Try simple deduction
		for (int x = 0; x < board.length; x++) {
			for (int y = 0; y < board[x].length; y++) {
				if (!solved[x][y]) {
					switch (board[x][y]) {
						case -2: unopendSquares.add(new Point(x, y)); break;
						case -1: solved[x][y] = true; break;
						case  0: solved[x][y] = true; break;
						default: solved[x][y] = checkSquare(x, y);
					}
				}
			}
		}
		
		if (changeMade) return;
		
		// Nothing found! Try backtrack solver
		recurseComplete = unopendSquares.size() < COMPLETE_SOLVER_LIMIT;
		if (recurseComplete) System.out.println("Recursing over all tiles left.");
		
		if (!borderSquares.isEmpty() || recurseComplete) {
			System.out.println("Simple deduction failed! Starting backtrack solver ...");
			long startTime = System.nanoTime();
			
			backtrackSolver();
			
			long totalTime = System.nanoTime() - startTime;
			System.out.println("Backtrack solver finished after " + (totalTime / 1000000000.0) + "s");
			
			if (changeMade) return;
		}
		else {
			System.out.println("No border squares found");
		}
		
		// Still no change. Click randomly
		System.out.println("No deduction possible! Clicking square with best probability.");
		clickRandom();
	}
	
	private void backtrackSolver() {
		if (recurseComplete) borderList = new ArrayList<>(unopendSquares);
		else borderList = new ArrayList<>(borderSquares);
		boolean[] borderBombs = new boolean[borderList.size()];
		combinations = new HashSet<>();
		
		for (Point p : borderSquares) System.out.printf("(%d|%d)", p.x, p.y);
		System.out.println();
		
		recurseCombinations(borderBombs, 0, 0);
		
		if (combinations.isEmpty()) {
			flagAll();
			((Game) ms).printBoard(true);
			((Game) ms).printBoard(false);
			throw new IllegalStateException("No possible combinations found!");
		}
		
		for (boolean[] ba : combinations) {
			String s = "";
			for (boolean b : ba) s += b ? "1" : "0";
			System.out.println(s);
		}
		
		boolean[] hasBomb = new boolean[borderList.size()]; // Square has at least one bomb
		boolean[] isBomb = new boolean[borderList.size()];  // Square is definitely (always) a bomb
		Arrays.fill(isBomb, true);
		
		for (boolean[] b : combinations)
			for (int i = 0; i < b.length; i++)
				if (b[i]) hasBomb[i] = true;
				else isBomb[i] = false;
		
		for (int i = 0; i < borderBombs.length; i++) {
			if (!hasBomb[i])
				click(borderList.get(i), "BacktrackSolver");
			else if (isBomb[i])
				flag(borderList.get(i), "BacktrackSolver");
		}
	}
	
	private void recurseCombinations(boolean[] borderBombs, int index, int bombs) {
		//String s = "";
		//for (boolean b : borderBombs) s += b ? "1" : "0";
		//System.out.println(s + ": " + index + " " + bombs);
		
		if (bombs > bombsLeft) return;
		
		// Check combination
		int[][] bombsAroundCopy = copy2DInt(bombsAround);
		
		for (int i = 0; i < borderBombs.length; i++) {
			if (!borderBombs[i]) continue;
			
			Point p = borderList.get(i); 
			final int maxX = p.x < width - 1 ? p.x + 2 : width;
			final int maxY = p.y < height - 1 ? p.y + 2 : height;
			
			for (int j = (p.x > 0 ? p.x - 1 : 0); j < maxX; j++)
				for (int k = (p.y > 0 ? p.y - 1 : 0); k < maxY; k++)
					bombsAroundCopy[j][k]++;
		}
		
		boolean combinationPerfect = true;
		for (int x = 0; x < board.length; x++)
			for (int y = 0; y < board[x].length; y++)
				if (!solved[x][y] && board[x][y] > 0)
					if (board[x][y] < bombsAroundCopy[x][y]) return;
					else if (board[x][y] != bombsAroundCopy[x][y]) combinationPerfect = false;
		
		if (combinationPerfect) {
			if (!recurseComplete || bombs == bombsLeft) {
				System.out.println("Found combination!");
				combinations.add(borderBombs.clone());
			}
			return;
		}

		if (index == borderBombs.length) return;
		
		// Recurse
		borderBombs[index] = true;
		recurseCombinations(borderBombs, index+1, bombs+1);
		borderBombs[index] = false;
		recurseCombinations(borderBombs, index+1, bombs);
	}

	private boolean checkSquare(int x, int y) {
		final LinkedList<Point> unopendSquares = new LinkedList<>();
		final int maxX = x < width - 1 ? x + 2 : width;
		final int maxY = y < height - 1 ? y + 2 : height;
		bombsAround[x][y] = 0;
		
		// Get information about surrounding squares
		for (int i = (x > 0 ? x - 1 : 0); i < maxX; i++) {
			for (int j = (y > 0 ? y - 1 : 0); j < maxY; j++) {
				if (flags[i][j]) bombsAround[x][y]++;
				else if (board[i][j] == -2) unopendSquares.add(new Point(i, j));
			}
		}
		
		// Evaluate information
		if (!unopendSquares.isEmpty()) {
			//System.out.println(String.format("%d|%d = %d - b: %d, uS: %d", x, y, board[x][y], bombsAround[x][y], unopendSquares.size()));
			if (bombsAround[x][y] == board[x][y]) // Enough bombs marked around
				for (Point p : unopendSquares) click(p, "SimpleSolver");
			else if (bombsAround[x][y] + unopendSquares.size() == board[x][y]) // All unknowns around must be bombs
				for (Point p : unopendSquares) flag(p, "SimpleSolver");
			else {
				borderSquares.addAll(unopendSquares);
				return false;
			}
		}
		return true;
	}
	
	private void click(Point p, String tag) {
		System.out.println(String.format("[" + tag + "]: Clicking %d|%d", p.x, p.y));
		changeMade = true;
		ms.click(p);
	}
	
	private void flag(Point p, String tag) {
		System.out.println(String.format("[" + tag + "]: Flagging %d|%d", p.x, p.y));
		changeMade = true;
		flags[p.x][p.y] = true;
		solved[p.x][p.y] = true;
		bombsLeft--;
	}
	
	private void clickRandom() {
		printWithFlags();
		
		int[] probability = new int[borderList.size()];
		for (boolean[] ba : combinations) {
			int number = 0;
			for (boolean b : ba) if (b) number++;
			if (number == 0) continue;
			final int p = 100 / number;
			for (int i = 0; i < ba.length; i++) if (ba[i]) probability[i] += p;
		}
		int minValue = probability[0], minIndex = 0;
		for (int i = 1; i < probability.length; i++) {
			if (probability[i] < minValue) {
				minValue = probability[i];
				minIndex = i;
			}
		}
		
		for (Point p : borderList) System.out.printf("(%d|%d)", p.x, p.y);
		System.out.println();
		for (int i : probability) System.out.printf("%d\t", i);
		System.out.println();
		
		click(borderList.get(minIndex), "ProbabilitySolver");
	}
	
	private int[][] copy2DInt(int[][] array) {
		int[][] result = new int[array.length][];
		for (int i = 0; i < result.length; i++) {
			result[i] = array[i].clone();
		}
		return result;
	}
	
	private void flagAll() {
		for (int x = 0; x < flags.length; x++) {
			for (int y = 0; y < flags[x].length; y++) {
				if (flags[x][y]) ms.flag(x, y, true);
			}
		}
	}
	
	private void printWithFlags() {
		System.out.println("Bombs left: " + bombsLeft);
		flagAll();
		((Game) ms).printBoard(false);
	}
}
