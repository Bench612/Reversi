import javax.swing.JPanel;

public class ReversiGUIPlayer extends JPanel implements ReversiPlayer {


	ReversiPanel rPanel;
	int moveR, moveC;
	
	public ReversiGUIPlayer() {
	}
	
	public void setReversiPanel(ReversiPanel panel){
		rPanel = panel;
	}
	

	@Override
	public void playMove() {
		rPanel.getMove();
		moveR = rPanel.getMoveRow();
		moveC = rPanel.getMoveCol();
	}

	@Override
	public int getMoveRow() {
		return moveR;
	}

	@Override
	public int getMoveCol() {
		return moveC;
	}

	@Override
	public void enemyPlayMove(int row, int col) {
	}

	@Override
	public void quitGame() {
		rPanel.quitGame();
	}

}
