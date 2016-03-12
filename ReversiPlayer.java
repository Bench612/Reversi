/*
 * Benjamin Chang
 * March 2016
 */
public interface ReversiPlayer {
	public void playMove();

	public int getMoveRow();

	public int getMoveCol();

	public void enemyPlayMove(int row, int col);
}