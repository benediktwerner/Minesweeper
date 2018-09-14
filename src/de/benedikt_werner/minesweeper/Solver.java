package de.benedikt_werner.minesweeper;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Solver {

    // If bombs < LIMIT the solver will also check non-border squares
    private static final int COMPLETE_SOLVER_LIMIT = 15;
    private static final boolean PLACE_FLAGS = true;

    private boolean[][] flags;
    private boolean[][] solved;
    private int[][] bombsAround;
    private int[][] board; //-2: unknown, -1: bomb, 0: empty, 1+: number
    private LinkedList<HashSet<Point>> borderSquares;
    private HashSet<Point> unopendSquares;
    private HashSet<boolean[]> combinations;
    private HashMap<Point, Double> bombProbability;
    private ArrayList<Point> borderList;
    private Minesweeper ms;
    private int width, height, bombsLeft, noBoardCounter;
    private boolean changeMade, recurseComplete;

    public Solver(Minesweeper minesweeper) {
        ms = minesweeper;
        width = ms.getWidth();
        height = ms.getHeight();
        bombsLeft = ms.getTotalBombCount();
        flags = new boolean[width][height];
        solved = new boolean[width][height];
        bombsAround = new int[width][height];
        noBoardCounter = 0;
    }

    public void solve() {
        click(new Point(width / 2, height / 2));
        Util.sleep(100);

        try {
            while (!ms.isGameOver() && noBoardCounter < 10)
                nextMove();
        }
        catch (IllegalStateException e) {
            System.out.println("Aborted solving: " + e.getMessage());
        }

        if (noBoardCounter >= 10)
            System.out.println("Aborted solving: Unable to detect board");
        else System.out.println("Game ended!");
    }

    private void nextMove() {
        changeMade = false;
        board = ms.getBoard();
        borderSquares = new LinkedList<>();
        unopendSquares = new HashSet<>();
        bombProbability = new HashMap<>();

        if (!boardExists())
            return;

        simpleDeduction();
        if (changeMade)
            return;
        recurseComplete = (unopendSquares.size() < COMPLETE_SOLVER_LIMIT);
        if (!borderSquares.isEmpty() || recurseComplete) {
            backtrackSolver();
            if (!changeMade) {
                probabilitySolver();
            }
        }
        else {
            System.out.println("No border squares found!");
            noBoardCounter++;
            Util.sleep(100);
        }
    }

    private boolean boardExists() {
        if (board == null) {
            noBoardCounter++;
            Util.sleep(100);
            return false;
        }
        else return true;
    }

    private void simpleDeduction() {
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                if (!solved[x][y]) {
                    switch (board[x][y]) {
                        case -2:
                            unopendSquares.add(new Point(x, y));
                            break;
                        case -1:
                            solved[x][y] = true;
                            flags[x][y] = true;
                            break;
                        case 0:
                            solved[x][y] = true;
                            break;
                        default:
                            solved[x][y] = checkSquare(x, y);
                    }
                }
            }
        }
    }

    private void backtrackSolver() {
        LinkedList<HashSet<Point>> regions;
        if (recurseComplete) {
            regions = new LinkedList<>();
            regions.add(unopendSquares);
        }
        else regions = mergeRegions();

        for (HashSet<Point> region : regions) {
            borderList = new ArrayList<>(region);
            boolean[] borderBombs = new boolean[borderList.size()];
            combinations = new HashSet<>();
            recurseCombinations(borderBombs, 0, 0);

            if (combinations.isEmpty())
                throw new IllegalStateException("No possible combinations found!");

            int[] bombCases = new int[borderList.size()];
            for (boolean[] b : combinations)
                for (int i = 0; i < b.length; i++)
                    if (b[i]) bombCases[i]++;

            for (int i = 0; i < bombCases.length; i++) {
                if (bombCases[i] == 0)
                    click(borderList.get(i));
                else if (bombCases[i] == combinations.size())
                    flag(borderList.get(i));
            }

            // Compute probabilities for probability solver
            if (!changeMade)
                for (int i = 0; i < bombCases.length; i++)
                bombProbability.put(borderList.get(i), bombCases[i] * 100.0 / combinations.size());
        }
    }

    private LinkedList<HashSet<Point>> mergeRegions() {
        LinkedList<HashSet<Point>> regions = new LinkedList<>();

        while (!borderSquares.isEmpty()) {
            HashSet<Point> newRegion = borderSquares.removeFirst();
            LinkedList<HashSet<Point>> overlappingRegions = new LinkedList<>();
            HashSet<Point> overlappingCombined = new HashSet<>();

            for (Point p : newRegion) {
                for (HashSet<Point> other : borderSquares)
                    if (other.contains(p))
                        overlappingRegions.add(other);
                for (HashSet<Point> set : overlappingRegions) {
                    overlappingCombined.addAll(set);
                    borderSquares.remove(set);
                }
                overlappingRegions.clear();
            }

            while (!overlappingCombined.isEmpty()) {
                HashSet<Point> newOverlappingCombined = new HashSet<>();

                for (Point p : overlappingCombined) {
                    for (HashSet<Point> other : borderSquares)
                        if (other.contains(p))
                            overlappingRegions.add(other);
                    for (HashSet<Point> set : overlappingRegions) {
                        newOverlappingCombined.addAll(set);
                        borderSquares.remove(set);
                    }
                    overlappingRegions.clear();
                }

                newRegion.addAll(overlappingCombined);
                overlappingCombined = newOverlappingCombined;
            }
            regions.add(newRegion);
        }
        return regions;
    }

    private void recurseCombinations(boolean[] borderBombs, int index, int bombs) {
        if (bombs > bombsLeft)
            return;

        // Check combination
        int[][] bombsAroundCopy = copy2DInt(bombsAround);

        for (int i = 0; i < borderBombs.length; i++) {
            if (!borderBombs[i]) continue;

            Point p = borderList.get(i);
            final int maxX = p.x < width - 1 ? p.x + 2 : width;
            final int maxY = p.y < height - 1 ? p.y + 2 : height;

            for (int x = (p.x > 0 ? p.x - 1 : 0); x < maxX; x++)
                for (int y = (p.y > 0 ? p.y - 1 : 0); y < maxY; y++)
                    bombsAroundCopy[x][y]++;
        }

        boolean combinationPerfect = true;
        for (Point p : borderList) {
            final int maxX = p.x < width - 1 ? p.x + 2 : width;
            final int maxY = p.y < height - 1 ? p.y + 2 : height;

            for (int x = (p.x > 0 ? p.x - 1 : 0); x < maxX; x++)
                for (int y = (p.y > 0 ? p.y - 1 : 0); y < maxY; y++)
                    if (!solved[x][y] && board[x][y] > 0)
                        if (board[x][y] < bombsAroundCopy[x][y])
                        return;
                        else if (board[x][y] != bombsAroundCopy[x][y])
                            combinationPerfect = false;
        }

        if (combinationPerfect) {
            if (!recurseComplete || bombs == bombsLeft)
                combinations.add(borderBombs.clone());
            return;
        }

        if (index == borderBombs.length)
            return;

        // Recurse
        borderBombs[index] = true;
        recurseCombinations(borderBombs, index + 1, bombs + 1);
        borderBombs[index] = false;
        recurseCombinations(borderBombs, index + 1, bombs);
    }

    private boolean checkSquare(int x, int y) {
        final LinkedList<Point> unopendSquares = new LinkedList<>();
        final int maxX = x < width - 1 ? x + 2 : width;
        final int maxY = y < height - 1 ? y + 2 : height;
        bombsAround[x][y] = 0;

        // Get information about surrounding squares
        for (int i = (x > 0 ? x - 1 : 0); i < maxX; i++) {
            for (int j = (y > 0 ? y - 1 : 0); j < maxY; j++) {
                if (flags[i][j])
                    bombsAround[x][y]++;
                else if (board[i][j] == -2)
                    unopendSquares.add(new Point(i, j));
            }
        }

        // Evaluate information
        if (!unopendSquares.isEmpty()) {
            if (bombsAround[x][y] == board[x][y])// Enough bombs marked around
                if (PLACE_FLAGS)
                    ms.chord(x, y);
                else unopendSquares.forEach(this::click);
            else if (bombsAround[x][y] + unopendSquares.size() == board[x][y]) // All unknowns around must be bombs
                unopendSquares.forEach(this::flag);
            else {
                borderSquares.add(new HashSet<Point>(unopendSquares));
                return false;
            }
        }
        return true;
    }

    private void click(Point p) {
        changeMade = true;
        ms.click(p);
    }

    private void flag(Point p) {
        if (flags[p.x][p.y])
            return;
        if (PLACE_FLAGS)
            ms.flag(p);
        changeMade = true;
        flags[p.x][p.y] = true;
        solved[p.x][p.y] = true;
        bombsLeft--;
    }

    private void probabilitySolver() {
        double minProb = Double.POSITIVE_INFINITY;
        Point minPoint = null;
        for (Point p : bombProbability.keySet()) {
            double prob = bombProbability.get(p);
            if (prob < minProb) {
                minProb = prob;
                minPoint = p;
            }
        }
        if (minPoint == null)
            throw new IllegalStateException("No square left to click");
        click(minPoint);
    }

    private int[][] copy2DInt(int[][] array) {
        int[][] result = new int[array.length][];
        for (int i = 0; i < result.length; i++)
            result[i] = array[i].clone();
        return result;
    }
}
