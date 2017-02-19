package de.benedikt_werner.minesweeper;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public abstract class WindowsMinesweeper implements Minesweeper {
    private static final int DELAY_BETWEEN_CLICKS = 10;
    private static final int DELAY_AFTER_CLICKS = 25;
    private static final int DELAY_AFTER_MOUSE_MOVE = 10;

    private Robot robot;
    protected boolean boardDetected = false;
    protected int width = 0, height = 0, totalBombs = 0;
    protected int squareWidth, halfSquareWidth;
    protected Point topLeft, bottomRight;
    protected Rectangle windowLocation;
    protected boolean gameOver;

    protected JFrame[] cornerFrames;

    public WindowsMinesweeper() {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new IllegalStateException("Unable to instantiate robot", e);
        }
    }

    public abstract Point boardToScreen(int x, int y);

    public void click(int x, int y) {
        checkBoardDetected();
        moveMouse(x, y);
        clickMouse(InputEvent.BUTTON1_DOWN_MASK);
    }

    public void flag(int x, int y) {
        checkBoardDetected();
        moveMouse(x, y);
        clickMouse(InputEvent.BUTTON3_DOWN_MASK);
    }

    public void chord(int x, int y) {
        checkBoardDetected();
        moveMouse(x, y);
        clickMouse(InputEvent.BUTTON3_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK);
    }

    protected void moveMouse(int x, int y) {
        checkBoardDetected();
        final Point screenPoint = boardToScreen(x, y);
        robot.mouseMove(screenPoint.x, screenPoint.y);;
        Util.sleep(DELAY_AFTER_MOUSE_MOVE);
    }

    private void clickMouse(int buttons) {
        robot.mousePress(buttons);
        Util.sleep(DELAY_BETWEEN_CLICKS);
        
        robot.mouseRelease(buttons);
        Util.sleep(DELAY_AFTER_CLICKS);
    }

    public boolean isGameOver() {
        checkBoardDetected();
        return gameOver;
    }

    protected BufferedImage takeScreenshot() {
        return robot.createScreenCapture(windowLocation);
    }
    
    protected void checkBoardDetected() {
        if (!boardDetected)
            throw new IllegalStateException("No detected board");
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTotalBombCount() {
        return totalBombs;
    }
}
