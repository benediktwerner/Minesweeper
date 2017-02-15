package de.benedikt_werner.minesweeper;

import java.awt.Point;

public interface Minesweeper {
    public void click(int x, int y);
    public default void click(Point p) {
        click(p.x, p.y);
    }

    public void flag(int x, int y, boolean flag);
    public default void flag(Point p, boolean flag) {
        flag(p.x, p.y, flag);
    }

    public void chord(int x, int y);
    public default void chord(Point p) {
        chord(p.x, p.y);
    }

    public boolean isGameOver();

    public int[][] getBoard();

    public int getWidth();
    public int getHeight();
    public int getTotalBombCount();
}
