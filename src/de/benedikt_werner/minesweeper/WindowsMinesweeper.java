package de.benedikt_werner.minesweeper;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;

public abstract class WindowsMinesweeper implements Minesweeper {
    private static final int DELAY_BETWEEN_CLICKS = 10;
    private static final int DELAY_AFTER_CLICKS = 25;
    private static final int DELAY_AFTER_MOUSE_MOVE = 10;
    
    protected final int IMAGE_OFFSET_X = 0, IMAGE_OFFSET_Y = 0;

    private Robot robot;
    protected boolean boardDetected = false;
    protected int width = 0, height = 0, totalBombs = 0;
    protected int squareWidth, halfSquareWidth;
    protected Point topLeft, bottomRight;
    protected Rectangle windowLocation;
    protected boolean gameOver;

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
        try {
            Thread.sleep(DELAY_AFTER_MOUSE_MOVE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void clickMouse(int buttons) {
        try {
            robot.mousePress(buttons);
            Thread.sleep(DELAY_BETWEEN_CLICKS);
            
            robot.mouseRelease(buttons);
            Thread.sleep(DELAY_AFTER_CLICKS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isGameOver() {
        checkBoardDetected();
        return gameOver;
    }

    // FIXME: add variable for relevant are to take, use that instead of windowLocation
    protected BufferedImage takeScreenshot() {
        if (windowLocation == null)
            return null;
        BufferedImage img = robot.createScreenCapture(windowLocation);
        return img.getSubimage(IMAGE_OFFSET_X, IMAGE_OFFSET_Y, img.getWidth() - 2 * IMAGE_OFFSET_X, img.getHeight() - IMAGE_OFFSET_X - IMAGE_OFFSET_Y);
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
