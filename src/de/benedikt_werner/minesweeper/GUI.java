package de.benedikt_werner.minesweeper;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class GUI {
	
	public static final int BUTTON_SIZE = 50;
	public static final int BUTTON_OFFSET = 20;

	private Game game;
	private JFrame frame;
	private JButton btnNewGame;
	private JLabel lblBombCount;
	private JButton[][] buttons = new JButton[0][0];
	
	private boolean[][] flags;
	private HashMap<JButton, Point> buttonMap;
	
	public static void main(String[] args) {
		Game game = new Game();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI gui = new GUI(game);
					gui.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public GUI(Game game) {
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
				btnNewGame.setVisible(false);
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
			width =  Integer.parseInt(strWidth);
			String strHeight = JOptionPane.showInputDialog("Enter board height: ", "10");
			height = Integer.parseInt(strHeight);
			String strBombs = JOptionPane.showInputDialog("Enter bombs: ", "" + (int) (height * width * 0.15));
			bombs = Integer.parseInt(strBombs);
		}
		catch (NumberFormatException e) {
			gameFinished("Invalid value!");
			return;
		}
		
		game.setup(width, height, bombs);

		buttons = new JButton[height][width];
		flags = new boolean[height][width];
		buttonMap = new HashMap<>();
		
		for (int x = 0; x < height; x++) {
			for (int y = 0; y < width; y++) {
				buttons[x][y] = new JButton();
				buttons[x][y].setBounds(y*BUTTON_SIZE, x*BUTTON_SIZE + BUTTON_OFFSET, BUTTON_SIZE, BUTTON_SIZE);
				buttonMap.put(buttons[x][y], new Point(x, y));
				buttons[x][y].addMouseListener(new MouseListener() {
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
						Point p = buttonMap.get((JButton) e.getSource());
						if (e.getButton() == MouseEvent.BUTTON1) {
							if (flags[p.x][p.y]) return;
							
							if (!game.click(p)) {
								gameFinished("GAME OVER!");
								return;
							}

							if (game.isWon()) {
								gameFinished("You won!");
								return;
							}
						} else {
							flags[p.x][p.y] = !flags[p.x][p.y];
						}
						redrawGame();
					}
				});
				frame.getContentPane().add(buttons[x][y]);
			}
		}
		lblBombCount.setVisible(true);
		redrawGame();
	}
	
	private void gameFinished(String text) {
		redrawGame();
		JOptionPane.showMessageDialog(frame, text);
		
		for (int x = 0; x < buttons.length; x++) {
			for (int y = 0; y < buttons[x].length; y++) {
				buttons[x][y].setVisible(false);
				frame.getContentPane().remove(buttons[x][y]);
			}
		}
		
		buttons = null;
		btnNewGame.setVisible(true);
		lblBombCount.setVisible(false);
	}
	
	private void redrawGame() {
		int[][] board = game.getBoard();
		boolean[][] visible =  game.getVisible();
		
		int flagCount = 0;
		
		for (int x = 0; x < board.length; x++) {
			for (int y = 0; y < board[x].length; y++) {
				if (visible[x][y]) {
					if  (board[x][y] == -1) {
						buttons[x][y].setText("X");
						buttons[x][y].setBackground(Color.RED);
					}
					else if (flags[x][y]) {
						buttons[x][y].setText(board[x][y] == 0 ? "" : board[x][y] + "");
						buttons[x][y].setBackground(Color.GREEN);
					}
					else {
						buttons[x][y].setText(board[x][y] == 0 ? "" : board[x][y] + "");
						buttons[x][y].setBackground(Color.WHITE);
					}
				}
				else if (flags[x][y]) {
					buttons[x][y].setText("F");
					buttons[x][y].setBackground(Color.RED);
					flagCount++;
				}
				else {
					buttons[x][y].setText("");
					buttons[x][y].setBackground(Color.CYAN);
				}
			}
		}
		lblBombCount.setText(game.getTotalBombCount() - flagCount + "");
	}
}
