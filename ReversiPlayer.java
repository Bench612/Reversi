/*
 * Benjamin Chang
 * March 2016
 */
public interface ReversiPlayer {
	
	//after this method returns, getMoveRow() and getMoveCol() should return the next move row and col
	public void playMove();

	//returns the last move played
	public int getMoveRow();
	public int getMoveCol();

	public void enemyPlayMove(int row, int col);
	
	//should unblock anything waiting
	public void quitGame();
}