package de.benedikt_werner.minesweeper;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class GUI {
	
	public static final int BUTTON_SIZE = 50;

	private Game game;
	private JFrame frame;
	private JButton btnNewGame;
	private JButton[][] buttons;
	private boolean[][] flags;
	private HashMap<JButton, Point> buttonMap;
	
	public static void main(String[] args) {
		Game game = new Game();
		new GUI(game);
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
		frame.getContentPane().setLayout(null);
		
		btnNewGame = new JButton("New Game");
		btnNewGame.setBounds(0, 0, 1000, 1000);
		btnNewGame.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				newGame();
				btnNewGame.setVisible(false);
			}
		});
		frame.getContentPane().add(btnNewGame);
		
		frame.setVisible(true);
	}
	
	private void newGame() {
		int width = 10;
		int height = 10;
		int bombs = 10;
		
		game.setup(width, height, bombs);

		buttons = new JButton[height][width];
		flags = new boolean[height][width];
		buttonMap = new HashMap<>();
		
		for (int x = 0; x < height; x++) {
			for (int y = 0; y < width; y++) {
				buttons[x][y] = new JButton();
				buttons[x][y].setBounds(y*BUTTON_SIZE, x*BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
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
						} else {
							flags[p.x][p.y] = !flags[p.x][p.y];
						}
						redrawGame();
					}
				});
				frame.getContentPane().add(buttons[x][y]);
			}
		}
	}
	
	private void gameFinished(String text) {
		System.out.println(text);
		
		for (int x = 0; x < buttons.length; x++) {
			for (int y = 0; y < buttons[x].length; y++) {
				frame.getContentPane().remove(buttons[x][y]);
			}
		}
		
		buttons = null;
		btnNewGame.setVisible(true);
	}
	
	private void redrawGame() {
		if (game.isWon()) {
			gameFinished("You won!");
			return;
		}
		
		int[][] board = game.getBoard();
		boolean[][] visible =  game.getVisible();
		
		for (int x = 0; x < board.length; x++) {
			for (int y = 0; y < board[x].length; y++) {
				if (visible[x][y]) {
					buttons[x][y].setText(board[x][y] == 0 ? "" : board[x][y] + "");
					buttons[x][y].setBackground(Color.WHITE);
				}
				else if (flags[x][y]) {
					buttons[x][y].setText("P");
					buttons[x][y].setBackground(Color.RED);
				}
				else {
					buttons[x][y].setText("");
					buttons[x][y].setBackground(Color.CYAN);
				}
			}
		}
	}
}
