import javax.swing.JFrame;

public class ReversiApplication extends JFrame {
	public static void main(String[] args) {
		new ReversiApplication();
	}

	public ReversiApplication() {

		int boardSize = 8;
		ReversiGUIPlayer guiP = new ReversiGUIPlayer();

		Reversi game;
		if (true)
			game = new Reversi(boardSize, guiP, new ReversiAI(boardSize,
					ReversiAI.BRUTALER, Reversi.TIME_LIMIT,
					Reversi.TURN_TIME_LIMIT, false));
		else
			game = new Reversi(boardSize,new ReversiAI(boardSize,
					ReversiAI.BRUTALER, Reversi.TIME_LIMIT,
					Reversi.TURN_TIME_LIMIT, true), new ReversiAI(boardSize,
					ReversiAI.BRUTAL, Reversi.TIME_LIMIT,
					Reversi.TURN_TIME_LIMIT, false));

		ReversiPanel panel = new ReversiPanel(game);
		guiP.setReversiPanel(panel);
		this.setContentPane(panel);
		new Thread(game).start();
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setBounds(100, 100, 700, 700);
		this.setVisible(true);
	}
}
