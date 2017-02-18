package de.benedikt_werner.minesweeper;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public class Util {
    private interface User32 extends StdCallLibrary {
        User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class,
                W32APIOptions.DEFAULT_OPTIONS);

        HWND FindWindow(String lpClassName, String lpWindowName);

        int GetWindowRect(HWND handle, int[] rect);
    }

    public static Rectangle getWindowLocation(String windowName) {
        HWND hwnd = User32.INSTANCE.FindWindow(null, windowName);
        if (hwnd == null) return null;

        int[] rect = {0, 0, 0, 0};
        int result = User32.INSTANCE.GetWindowRect(hwnd, rect);
        if (result == 0) return null;

        return new Rectangle(rect[0], rect[1], rect[2] - rect[0], rect[3] - rect[1]);
    }
    
    public static void printCountdown(int start) throws InterruptedException {
        for (int i = 3; i > 0; i--) {
            System.out.print(i + "..");
            Thread.sleep(1000);
        }
    }

    public static void printBoard(int[][] board) {
        String line = "+";
        for (int i = 0; i < board.length; i++)
            line += "-";
        line += "+";
        System.out.println(line);

        for (int y = 0; y < board[0].length; y++) {
            String s = "|";
            for (int x = 0; x < board.length; x++) {
                switch (board[x][y]) {
                    case -2: s += "*"; break;
                    case -1: s += "X"; break;
                    case  0: s += " "; break;
                    default: s += board[x][y] + "";
                }
            }
            System.out.println(s + "|");
        }
        System.out.println(line);
    }
    
    public static int readInt(String text) {
        while (true) {
            String s = JOptionPane.showInputDialog(null, text);
            if (s == null) return -1;
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException e) {}
        }
    }
    
    public static int colorDifference(Color c1, Color c2) {
        return Math.abs(c1.getRed() - c2.getRed())
                + Math.abs(c1.getBlue() - c2.getBlue())
                + Math.abs(c1.getGreen() - c2.getGreen());
    }
    
    public static int colorDifference(int r1, int g1, int b1, int r2, int g2, int b2){
        return Math.abs(r1 - r2) + Math.abs(b1 - b2) + Math.abs(g1 - g2);
    }

    public static boolean isBlack(int i) {
        int red = (i >> 16) & 0xFF;
        int green = (i >> 8) & 0xFF;
        int blue = i & 0xFF;
        return (red < 30 && green < 30 && blue < 35);
    }

    public static void saveImage(BufferedImage img) {
        File file = new File(System.getProperty("user.home") + "\\Downloads\\Minesweeper-"+System.currentTimeMillis()+".png");
        try {
            ImageIO.write(img, "png", file);
            System.out.println("Saved image");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JFrame createCornerFrame(Point position) {
        JFrame frame = new JFrame();
        frame.getContentPane().setBackground(Color.RED);
        frame.setAlwaysOnTop(true);
        frame.setBounds(position.x - 10, position.y - 10, 20, 20);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setVisible(true);
        return frame;
    }
}
