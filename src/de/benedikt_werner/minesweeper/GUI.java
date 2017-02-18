package de.benedikt_werner.minesweeper;

import java.awt.Color;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class GUI {
    public static final int BUTTON_SIZE = 50;
    public static final int BUTTON_OFFSET = 20;

    private SimpleMinesweeper game;
    private JFrame frame;
    private JButton btnNewGame;
    private JLabel lblBombCount;
    private JButton[][] buttons = new JButton[0][0];

    public GUI(SimpleMinesweeper game) {
        this.game = game;

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        initialize();
    }

    private void initialize() {
        frame = new JFrame("Minesweeper");
        frame.setBounds(400, 300, 1000, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        btnNewGame = new JButton("New Game");
        btnNewGame.setBounds(0, 0, 1000, 1000);
        btnNewGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                newGame();
            }
        });
        frame.getContentPane().add(btnNewGame);

        lblBombCount = new JLabel("0");
        lblBombCount.setBounds(0, 0, 100, 20);
        frame.getContentPane().add(lblBombCount);
    }	

    private void newGame() {
        int width, height, bombs;
        try {
            String strWidth = JOptionPane.showInputDialog("Enter board width: ", "10");
            if (strWidth == null) return;
            width =  Integer.parseInt(strWidth);
            String strHeight = JOptionPane.showInputDialog("Enter board height: ", "10");
            if (strHeight == null) return;
            height = Integer.parseInt(strHeight);
            String strBombs = JOptionPane.showInputDialog("Enter bombs: ", "" + (int) (height * width * 0.15));
            if (strBombs == null) return;
            bombs = Integer.parseInt(strBombs);
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid value!");
            return;
        }
        game.setup(width, height, bombs);
        btnNewGame.setVisible(false);
        lblBombCount.setVisible(true);
        generateButtons(width, height);
        redrawGame();
    }
    
    private void generateButtons(int width, int height) {
        Container contentPane = frame.getContentPane();
        buttons = new JButton[height][width];

        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                final Point buttonPosition = new Point(x, y);
                JButton newButton = new JButton();
                newButton.setBounds(y*BUTTON_SIZE, x*BUTTON_SIZE + BUTTON_OFFSET, BUTTON_SIZE, BUTTON_SIZE);
                newButton.addMouseListener(new MouseListener() {
                    @Override
                    public void mouseReleased(MouseEvent arg0) {}
                    @Override
                    public void mousePressed(MouseEvent arg0) {}
                    @Override
                    public void mouseExited(MouseEvent arg0) {}
                    @Override
                    public void mouseEntered(MouseEvent arg0) {}
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            game.click(buttonPosition);
                            if (game.isGameOver()) {
                                if (game.isWon()) gameFinished("You won!");
                                else gameFinished("GAME OVER!");
                                return;
                            }
                        }
                        else
                            game.flag(buttonPosition.x, buttonPosition.y);
                        redrawGame();
                    }
                });
                contentPane.add(newButton);
                buttons[x][y] = newButton;
            }
        }
    }

    private void gameFinished(String text) {
        redrawGame();
        JOptionPane.showMessageDialog(frame, text);

        Container contentPane = frame.getContentPane();
        for (int x = 0; x < buttons.length; x++) {
            for (int y = 0; y < buttons[x].length; y++) {
                buttons[x][y].setVisible(false);
                contentPane.remove(buttons[x][y]);
            }
        }

        buttons = null;
        btnNewGame.setVisible(true);
        lblBombCount.setVisible(false);
    }

    private void redrawGame() {
        int[][] board = game.getBoard();

        int flagCount = 0;
        boolean gameOver = game.isGameOver();

        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                switch (board[x][y]) {
                    case -2:
                        buttons[x][y].setText("");
                        buttons[x][y].setBackground(Color.CYAN);
                        break;
                    case -1:
                        buttons[x][y].setText(gameOver ? "X" : "F");
                        buttons[x][y].setBackground(Color.RED);
                        flagCount++;
                        break;
                    case 0:
                        buttons[x][y].setText("");
                        buttons[x][y].setBackground(Color.WHITE);
                        break;
                    default:
                        buttons[x][y].setText(board[x][y] + "");
                        buttons[x][y].setBackground(Color.WHITE);
                        break;
                }
            }
        }
        lblBombCount.setText(game.getTotalBombCount() - flagCount + "");
    }

    public void show() {
        frame.setVisible(true);
    }
}
