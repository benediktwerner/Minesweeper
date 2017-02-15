package de.benedikt_werner.minesweeper;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Random;

/*
 * Simple Minesweeper implementation
 */
public class Game implements Minesweeper {
    private int[][] board;
    private int[][] visibleBoard;
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
        gameOver = false;

        visibleBoard = new int[height][width];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                visibleBoard[x][y] = -2;
            }
        }
    }

    private void generate(Point start) {
        if (bombs + 9 > width*height) {
            throw new IllegalArgumentException("Too many bombs: " + bombs + " bombs on " + (width*height) + " squares.");
        }
        board = new int[height][width];

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

    public void click(int x, int y) {
        if (!generated) {
            generate(new Point(x, y));
            generated = true;
        }
        if (visibleBoard[x][y] != -2) return;
        else if (board[x][y] == -1) gameOver();
        else if (board[x][y] == 0) floodOpen(x, y);
        else {
            visibleBoard[x][y] = board[x][y];
            openSquares++;
            if (openSquares == squaresToOpen) gameOver(); // Game won
        }
    }

    public void flag(int x, int y, boolean flag) {
        if (visibleBoard[x][y] == -1 || visibleBoard[x][y] == -2)
            visibleBoard[x][y] = flag ? -1 : -2;
    }

    public void chord(int x, int y) {
        if (visibleBoard[x][y] > 0) {
            final int maxX = x < width - 1 ? x + 2 : width;
            final int maxY = y < height - 1 ? y + 2 : height;
            int bombsAround = 0;

            // Get information about surrounding squares
            for (int i = (x > 0 ? x - 1 : 0); i < maxX; i++)
                for (int j = (y > 0 ? y - 1 : 0); j < maxY; j++)
                    if (visibleBoard[i][j] == -1) bombsAround++;
            if (bombsAround == visibleBoard[x][y]) {
                for (int i = (x > 0 ? x - 1 : 0); i < maxX; i++)
                    for (int j = (y > 0 ? y - 1 : 0); j < maxY; j++)
                        if (visibleBoard[i][j] == -2) click(i,j);
            }
        }
    }

    public void switchFlag(int x, int y) {
        if (visibleBoard[x][y] == -1) visibleBoard[x][y] = -2;
        else if (visibleBoard[x][y] == -2) visibleBoard[x][y] = -1;
    }

    private void gameOver() {
        gameOver = true;
        visibleBoard = board;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    private void floodOpen(int x, int y) {
        LinkedList<Point> stack = new LinkedList<>();
        stack.add(new Point(x, y));

        while (!stack.isEmpty()) {
            Point p = stack.removeFirst();
            if (p.x < 0 || p.x >= width ||
                    p.y < 0 || p.y >= height ||
                    visibleBoard[p.x][p.y] != -2) {
                continue;
            }
            visibleBoard[p.x][p.y] = board[p.x][p.y];
            openSquares++;
            if (board[p.x][p.y] == 0) {
                for (int a = p.x - 1; a <= p.x + 1;  a++) {
                    for (int b = p.y - 1; b <= p.y + 1; b++) {
                        if (a == p.x && b == p.y) continue;
                        stack.addLast(new Point(a, b));
                    }
                }
            }
        }
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

    public void printBoard(boolean showAll) {
        String line = "+";
        for (int i = 0; i < width; i++) {
            line += "-";
        }
        System.out.println(line + "+");

        for (int y = 0; y < height; y++) {
            String s = "|";
            for (int x = 0; x < width; x++) {
                if (showAll) {
                    switch (board[x][y]) {
                        case -1: s += "X"; break;
                        case  0: s += " "; break;
                        default: s += board[x][y] + "";
                    }
                }
                else {
                    switch (visibleBoard[x][y]) {
                        case -2: s += "°"; break;
                        case -1: s += "P"; break;
                        case  0: s += " "; break;
                        default: s += visibleBoard[x][y] + "";
                    }
                }
            }
            System.out.println(s + "|");
        }
        System.out.println(line + "+");
    }

    public boolean isWon() {
        return generated && openSquares == squaresToOpen;
    }

    public int[][] getBoard() {
        return visibleBoard;
    }

    public int getTotalBombCount() {
        return bombs;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
