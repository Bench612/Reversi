import java.awt.event.*;
import javax.swing.*;

public class ReversiApplication extends JFrame implements ActionListener {

	PlayerSelection playerOne;
	PlayerSelection playerTwo;

	Reversi game;

	final static int boardSize = 8;

	public static void main(String[] args) {
		new ReversiApplication();
	}

	class PlayerSelection extends JMenu {
		ButtonGroup group;
		boolean first;

		public PlayerSelection(String menuTitle, boolean playerOne,
				String defaultSelected) {
			super(menuTitle);
			first = playerOne;

			String[] options = { "Player", "Easy", "Medium", "Hard", "Brutal",
					"Brutaler" };
			group = new ButtonGroup();
			for (int i = 0; i < options.length; i++) {
				JRadioButton button = new JRadioButton(options[i]);
				button.setActionCommand(i + "");
				group.add(button);
				this.add(button);
				if (options[i] == defaultSelected)
					button.setSelected(true);
			}
		}

		public ReversiPlayer getPlayer() {
			if (group.getSelection() == null
					|| group.getSelection().getActionCommand().equals("0"))
				return new ReversiGUIPlayer();
			else {
				int difficulty = Integer.parseInt(group.getSelection()
						.getActionCommand());
				return new ReversiAI(boardSize, difficulty, Reversi.TIME_LIMIT,
						Reversi.TURN_TIME_LIMIT, first);
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		// The only action is "New Game"
		game.quitGame();
		startNewGame();
	}

	private void startNewGame() {
		ReversiPlayer p1 = playerOne.getPlayer();
		ReversiPlayer p2 = playerTwo.getPlayer();
		game = new Reversi(boardSize, p1, p2);
		ReversiPanel panel = new ReversiPanel(game);
		if (p1 instanceof ReversiGUIPlayer)
			((ReversiGUIPlayer) p1).setReversiPanel(panel);
		if (p2 instanceof ReversiGUIPlayer)
			((ReversiGUIPlayer) p2).setReversiPanel(panel);
		this.setContentPane(panel);
		new Thread(game).start();
		revalidate();
		repaint();
	}

	public ReversiApplication() {
		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		playerOne = new PlayerSelection("Player One", true, "Player");
		playerTwo = new PlayerSelection("Player Two", false, "Brutaler");
		menuBar.add(playerOne);
		menuBar.add(playerTwo);
		JButton start = new JButton("New Game");
		start.addActionListener(this);
		menuBar.add(start);

		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setBounds(100, 100, 700, 700);
		startNewGame();
		this.setVisible(true);
	}
}
