package de.benedikt_werner.minesweeper;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class Windows7Minesweeper extends WindowsMinesweeper {
    private static final int IMAGE_OFFSET_X = 30, IMAGE_OFFSET_Y = 80;
    private static final int NUMBER_DETECTION_PIXELS = 15;
    private static final int NUMBER_DETECTION_PIXELS_HALF = NUMBER_DETECTION_PIXELS / 2;

    private static final Color GAME_OVER_DIALOG_COLOR = new Color(240, 240, 240);
    private static final Color BOMB_COLOR = new Color(110, 110, 110);
    private static final Color FLAG_COLOR = new Color(255, 0, 0);
    private static final Color BLANK_COLOR = new Color(200, 210, 230);
    private static final Color ONE_COLOR = new Color(65, 79, 188);
    private static final Color TWO_COLOR = new Color(30, 105, 5);
    private static final Color THREE_COLOR = new Color(165, 5, 5);
    private static final Color FOUR_COLOR = new Color(0, 0, 140);
    private static final Color FIVE_COLOR = new Color(125, 0, 5);
    private static final Color SIX_COLOR = new Color(10, 120, 130);

    public static void main(String[] args) throws InterruptedException {
        Windows7Minesweeper ms = new Windows7Minesweeper();
        System.out.println("Taking screenshot in 3 seconds...");
        Util.sleep(300);
        Util.printCountdown(3);

        try {
            ms.findBoardAndCallibrate();
        } catch (IllegalStateException e) {
            System.out.println("Failed to find board: " + e.getMessage());
            return;
        }

        Util.sleep(500);
        new Solver(ms).solve();
    }

    public void findBoardAndCallibrate() {
        windowLocation = Util.getWindowLocation("Minesweeper");
        if (windowLocation == null)
            throw new IllegalStateException("No Minesweper window found!");

        BufferedImage img = takeScreenshot();
        img = img.getSubimage(IMAGE_OFFSET_X, IMAGE_OFFSET_Y, img.getWidth() - 2 * IMAGE_OFFSET_X, img.getHeight() - IMAGE_OFFSET_Y);

        findTopLeftCorner(img);
        findBottomRightCorner(img);
        checkCornerLocations(img);

        windowLocation.x += IMAGE_OFFSET_X + topLeft.x;
        windowLocation.y += IMAGE_OFFSET_Y + topLeft.y;
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

        // DEBUG STUFF
        //System.out.println(squareWidth + ", " + halfSquareWidth);
        //int x = topLeft.x + squareWidth + halfSquareWidth;
        //int y = topLeft.y + squareWidth + halfSquareWidth;
        //saveImage(takeScreenshot().getSubimage(x - 7, y - 7, 15, 15));
    }

    private void findTopLeftCorner(BufferedImage img) {
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                if (Util.isBlack(img.getRGB(x, y))) {
                    topLeft = new Point(x, y);
                    System.out.printf("(%d|%d)\n", topLeft.x, topLeft.y);
                    return;
                }
            }
        }
        throw new IllegalStateException("No top left corner found");
    }

    private void findBottomRightCorner(BufferedImage img) {
        int rightX = -1, bottomY = -1;
        for (int x = topLeft.x; x < img.getWidth(); x++) {
            if (!Util.isBlack(img.getRGB(x, topLeft.y))) {
                rightX = x - 1;
                break;
            }
        }
        for (int y = topLeft.y; y < img.getHeight(); y++) {
            if (!Util.isBlack(img.getRGB(topLeft.x, y))) {
                bottomY = y - 1;
                break;
            }
        }
        if (rightX == -1)
            rightX = img.getWidth() - 1;
        if (bottomY == -1)
            bottomY = img.getHeight() - 1;
        bottomRight = new Point(rightX, bottomY);
        System.out.printf("(%d|%d)\n", bottomRight.x, bottomRight.y);
    }

    private void findWidthAndHeight(BufferedImage img) {
        Point innerCorner = null;
        for (int i = 1; i < 10; i++)
            if (!Util.isBlack(img.getRGB(topLeft.x + i, topLeft.y + i)))
                innerCorner = new Point(topLeft.x + i, topLeft.y + i);

        if (innerCorner != null) {
            width = 0;
            boolean onSquare = true;
            for (int x = innerCorner.x; x <= bottomRight.x; x++) {
                if (onSquare && Util.isBlack(img.getRGB(x, innerCorner.y))) {
                    onSquare = false;
                    width++;
                }
                else if (!onSquare && !Util.isBlack(img.getRGB(x, innerCorner.y)))
                    onSquare = true;
            }
            height = 0;
            onSquare = true;
            for (int y = innerCorner.y; y <= bottomRight.y; y++) {
                if (onSquare && Util.isBlack(img.getRGB(innerCorner.x, y))) {
                    onSquare = false;
                    height++;
                }
                else if (!onSquare && !Util.isBlack(img.getRGB(innerCorner.x, y)))
                    onSquare = true;
            }
        }

        if (innerCorner == null || width == 0 || height == 0) {
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

        int[][] board = detectBoard(img);
        if (board != null)
            Util.printBoard(board);

        return board;
    }

    private boolean detectGameOverDialog(BufferedImage img) {
        int midX = img.getWidth() / 2;
        int midY = img.getHeight() / 2;
        int countWhite = 0;

        for (int x = midX - 20; x < midX + 20; x++)
            for (int y = midY - 20; y < midY + 20; y++)
                if (Util.colorDifference(new Color(img.getRGB(x, y)), GAME_OVER_DIALOG_COLOR) < 12)
                    countWhite++;
        return (countWhite > 1100);
    }

    /**
     * @return -10: bomb, -2: unknown, -1: flag, 0: empty, 1+: number
     */
    protected int detectNumber(BufferedImage img, int x, int y){
        int imgX = x * squareWidth + halfSquareWidth;
        int imgY = y * squareWidth + halfSquareWidth;

        boolean hasColorOfOneSquare = false;
        boolean hasColorOfBlank = false;

        for (int i = imgX-NUMBER_DETECTION_PIXELS_HALF; i <= imgX+NUMBER_DETECTION_PIXELS_HALF; i++) {
            for (int j = imgY-NUMBER_DETECTION_PIXELS_HALF; j <= imgY+NUMBER_DETECTION_PIXELS_HALF; j++) {
                Color pixel = new Color(img.getRGB(i,j));

                if (Util.colorDifference(pixel, BOMB_COLOR) < 20)
                    return -10;
                else if (Util.colorDifference(pixel, FLAG_COLOR) < 30)
                    return -1;

                if (Util.colorDifference(pixel, ONE_COLOR) < 20)
                    hasColorOfOneSquare = true;

                if (pixel.getBlue() > pixel.getRed() && pixel.getBlue() > pixel.getGreen()
                        && pixel.getGreen() > pixel.getRed()
                        && Util.colorDifference(pixel, BLANK_COLOR) < 100)
                    hasColorOfBlank = true;

                if (Util.colorDifference(pixel, THREE_COLOR) < 30) return 3;
                if (Util.colorDifference(pixel, TWO_COLOR) < 30)   return 2;
                if (Util.colorDifference(pixel, FOUR_COLOR) < 30)  return 4;
                if (Util.colorDifference(pixel, FIVE_COLOR) < 30)  return 5;
                if (Util.colorDifference(pixel, SIX_COLOR) < 30)   return 6;
                if (hasColorOfOneSquare && hasColorOfBlank)        return 1;
            }
        }

        // Determine how 'same' the area is.
        // This is to separate the empty areas which are relatively the same from
        // the unexplored areas which have a gradient of some sort.
        Color c0 = new Color(img.getRGB(imgX-NUMBER_DETECTION_PIXELS_HALF, imgY-NUMBER_DETECTION_PIXELS_HALF));
        for (int i = imgX-NUMBER_DETECTION_PIXELS_HALF; i <= imgX+NUMBER_DETECTION_PIXELS_HALF; i++)
            for (int j = imgY-NUMBER_DETECTION_PIXELS_HALF; j <= imgY+NUMBER_DETECTION_PIXELS_HALF; j++)
                if (Util.colorDifference(new Color(img.getRGB(i,j)), c0) > 50)
                    return -2;

        if (hasColorOfBlank) return 0;
        else return -2;
    }
}
