package de.benedikt_werner.minesweeper;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class Windows10Minesweeper extends WindowsMinesweeper {
    private static final int IMAGE_OFFSET_X = 30, IMAGE_OFFSET_TOP = 130, IMAGE_OFFSET_BOTTOM = 100;
    private static final int NUMBER_DETECTION_PIXELS = 10;
    private static final int NUMBER_DETECTION_PIXELS_HALF = NUMBER_DETECTION_PIXELS / 2;

    private static final Color BOMB_COLOR = new Color(0, 20, 60);
    private static final Color FLAG_COLOR = new Color(250, 205, 60);
    private static final Color BLANK_COLOR = Color.WHITE;
    private static final Color ONE_COLOR = new Color(25, 188, 225);
    private static final Color TWO_COLOR = new Color(150, 180, 90);
    private static final Color THREE_COLOR = new Color(227, 87, 130);
    private static final Color FOUR_COLOR = new Color(30, 87, 192);
    private static final Color FIVE_COLOR = new Color(187, 41, 41);

    public static void main(String[] args) throws InterruptedException {
        Windows10Minesweeper ms = new Windows10Minesweeper();
        System.out.println("Taking screenshot in 3 seconds...");
        Util.sleep(300);
        Util.printCountdown(3);

        System.out.println("Done.\n"
                + "Detecting board...");
        try {
            ms.findBoardAndCallibrate();
        }
        catch (IllegalStateException e) {
            System.out.println("Failed to find board: " + e.getMessage());
            return;
        }

        Util.sleep(500);
        new Solver(ms).solve();
    }

    public void findBoardAndCallibrate() {
        windowLocation = Util.getWindowLocation("Microsoft Minesweeper");
        if (windowLocation == null)
            throw new IllegalStateException("No Minesweper window found!");

        BufferedImage img = takeScreenshot();
        img = img.getSubimage(IMAGE_OFFSET_X, IMAGE_OFFSET_TOP,
                img.getWidth() - 2 * IMAGE_OFFSET_X,
                img.getHeight() - IMAGE_OFFSET_TOP - IMAGE_OFFSET_BOTTOM);

        findTopLeftCorner(img);
        findBottomRightCorner(img);
        checkCornerLocations(img);

        windowLocation.x += IMAGE_OFFSET_X + topLeft.x;
        windowLocation.y += IMAGE_OFFSET_TOP + topLeft.y;
        windowLocation.width = bottomRight.x - topLeft.x;
        windowLocation.height = bottomRight.y - topLeft.y;

        findWidthAndHeight(img);

        showCornerFrames();
        totalBombs = Util.readInt("Bombs: ");
        hideCornerFrames();

        if (totalBombs == -1)
            throw new IllegalStateException("No bombs found");

        squareWidth = Math.round((bottomRight.x - topLeft.x) / (float) width);
        halfSquareWidth = squareWidth / 2;
        boardDetected = true;
        gameOver = false;
    }

    private void findTopLeftCorner(BufferedImage img) {
        for (int y = 0; y < img.getHeight(); y++)
            for (int i = 0; i < y; i++)
                if (!isBlack(img.getRGB(i, y - i))) {
                    topLeft = new Point(i, y - i);
                    System.out.printf("Found top left corner: (%d|%d)\n", topLeft.x, topLeft.y);
                    return;
                }
        throw new IllegalStateException("No top left corner found");
    }

    private void findBottomRightCorner(BufferedImage img) {
        final int imgWidth = img.getWidth();
        final int imgHeight = img.getHeight();

        for (int y = 0; y < img.getHeight(); y++)
            for (int i = 1; i < y; i++)
                if (!isBlack(img.getRGB(imgWidth - i, imgHeight - y + i))) {
                    bottomRight = new Point(imgWidth - i, imgHeight - y + i);
                    System.out.printf("Found bottom right corner: (%d|%d)\n", bottomRight.x, bottomRight.y);
                    return;
                }
        throw new IllegalStateException("No bottom right corner found");
    }

    private void findWidthAndHeight(BufferedImage img) {
        Point innerCorner = new Point(topLeft.x + 15, topLeft.y + 15);

        width = 0;
        boolean onSquare = true;
        for (int x = innerCorner.x; x <= bottomRight.x; x++) {
            if (onSquare && isBlack(img.getRGB(x, innerCorner.y))) {
                onSquare = false;
                width++;
            }
            else if (!onSquare && !isBlack(img.getRGB(x, innerCorner.y)))
                onSquare = true;
        }
        if (onSquare) width++;

        height = 0;
        onSquare = true;
        for (int y = innerCorner.y; y <= bottomRight.y; y++) {
            if (onSquare && isBlack(img.getRGB(innerCorner.x, y))) {
                onSquare = false;
                height++;
            }
            else if (!onSquare && !isBlack(img.getRGB(innerCorner.x, y)))
                onSquare = true;
        }
        if (onSquare) height++;

        if (width == 0 || height == 0) {
            System.out.println("Automatic width and height detection failed.");
            width = Util.readInt("Width: ");
            if (width == -1)
                throw new IllegalStateException("No width found");
            height = Util.readInt("Height: ");
            if (height == -1)
                throw new IllegalStateException("No height found");
        }
        else System.out.printf("Detected width and height of board: %dx%d\n", width, height);
    }

    public int[][] getBoard() {
        checkBoardDetected();

        moveMouse(-1, -1);
        BufferedImage img = takeScreenshot();

        if (detectGameOverDialog(img)) {
            gameOver = true;
            return null;
        }
        return detectBoard(img);
    }

    private boolean detectGameOverDialog(BufferedImage img) {
        int midX = img.getWidth() / 2;
        int midY = img.getHeight() / 2;
        int blackCount = 0;

        for (int x = midX - 20; x < midX + 20; x++)
            for (int y = midY - 20; y < midY + 20; y++)
                if (isBlack(img.getRGB(x, y)))
                    blackCount++;

        return blackCount > 1100;
    }

    /**
     * @return -10: bomb, -2: unknown, -1: flag, 0: empty, 1+: number
     */
    protected int detectNumber(BufferedImage img, int x, int y) {
        int imgX = (x * squareWidth) + halfSquareWidth;
        int imgY = (y * squareWidth) + halfSquareWidth;

        boolean hasColorOfOne = false;
        boolean hasColorOfFive = false;
        boolean hasColorOfBlank = false;

        for (int i = imgX - NUMBER_DETECTION_PIXELS_HALF; i <= imgX + NUMBER_DETECTION_PIXELS_HALF; i++) {
            for (int j = imgY - NUMBER_DETECTION_PIXELS_HALF; j <= imgY + NUMBER_DETECTION_PIXELS_HALF; j++) {
                Color pixel = new Color(img.getRGB(i, j));

                if (Util.colorDifference(pixel, BOMB_COLOR) < 30)
                    return -10;
                else if (Util.colorDifference(pixel, FLAG_COLOR) < 30)
                    return -1;

                if (Util.colorDifference(pixel, ONE_COLOR) < 20)
                    hasColorOfOne = true;
                if (Util.colorDifference(pixel, FIVE_COLOR) < 20)
                    hasColorOfFive = true;
                if (Util.colorDifference(pixel, BLANK_COLOR) < 10)
                    hasColorOfBlank = true;

                //TODO: Missing 6,7 and 8
                if (Util.colorDifference(pixel, THREE_COLOR) < 20) return 3;
                if (Util.colorDifference(pixel, TWO_COLOR) < 30) return 2;
                if (Util.colorDifference(pixel, FOUR_COLOR) < 25) return 4;
                if (hasColorOfBlank && hasColorOfFive) return 5;
                if (hasColorOfOne && hasColorOfBlank) return 1;
            }
        }
        if (hasColorOfBlank)
            return 0;
        else return -2;
    }

    private boolean isBlack(int color) {
        Color c = new Color(color);
        return (c.getRed() < 60 && c.getGreen() < 65 && c.getBlue() < 70);
    }
}
