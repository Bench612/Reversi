import java.util.ArrayList;

/*
 * Benjamin Chang
 * March 2016
 * This class makes no attempts at optimization
 */

public class Reversi implements Runnable {
	final static byte PLAYER_ONE = 1;
	final static byte PLAYER_TWO = -1;
	final static byte EMPTY = 0;

	// time in milliseconds
	static final long TIME_LIMIT = 1000 * 30;
	static final long TURN_TIME_LIMIT = 3 * 1000;
	static final int DEFAULT_BOARD_SIZE = 8;
	static final boolean DEFAULT_AI_FIRST = false;

	final private byte[][] board;
	final private ReversiPlayer playerOne, playerTwo;
	private byte currPlayer;
	private ArrayList<ReversiSpectator> spectators;
	
	private volatile boolean quitGame = false;

	public static void main(String[] args) {
		ReversiApplication.main(args);
	}

	public byte getBoard(int r, int c) {
		return board[r][c];
	}

	public byte getCurrentPlayer() {
		return currPlayer;
	}

	public int getBoardSize() {
		return board.length;
	}

	public Reversi(int boardSize, ReversiPlayer p1, ReversiPlayer p2) {
		board = new byte[boardSize][boardSize];
		playerOne = p1;
		playerTwo = p2;
		currPlayer = PLAYER_ONE;
		spectators = new ArrayList<ReversiSpectator>();
		quitGame = false;
	}

	public void addSpectator(ReversiSpectator spec) {
		spectators.add(spec);
	}

	// returns if the move was valid
	private boolean applyMove(int moveR, int moveC, byte player) {
		int boardSize = board.length;
		if (moveR < 0 || moveC < 0 || moveR >= boardSize || moveC >= boardSize
				|| board[moveR][moveC] != EMPTY)
			return false;
		board[moveR][moveC] = player;
		boolean shifted = false;
		for (int rShift = -1; rShift <= 1; rShift++) {
			for (int cShift = -1; cShift <= 1; cShift++) {
				if (rShift != 0 || cShift != 0) {
					int amount = 1;
					while (moveR + rShift * amount >= 0
							&& moveR + rShift * amount < boardSize
							&& moveC + cShift * amount < boardSize
							&& moveC + cShift * amount >= 0
							&& board[moveR + rShift * amount][moveC + cShift
									* amount] == -player) {
						amount++;
					}
					if (amount > 1
							&& moveR + rShift * amount >= 0
							&& moveR + rShift * amount < boardSize
							&& moveC + cShift * amount < boardSize
							&& moveC + cShift * amount >= 0
							&& board[moveR + rShift * amount][moveC + cShift
									* amount] == player) {
						shifted = true;
						for (amount--; amount > 0; amount--)
							board[moveR + rShift * amount][moveC + cShift
									* amount] = player;

					}
				}
			}
		}
		if (!shifted)
			board[moveR][moveC] = EMPTY;
		return shifted;
	}

	private boolean canMove(int player) {
		for (int i = 0; i < board.length; i++)
			for (int j = 0; j < board.length; j++)
				if (board[i][j] == EMPTY && isValid(i, j, player))
					return true;
		return false;
	}

	public boolean canMove(int row, int col) {
		return isValid(row, col, currPlayer);
	}

	// returns if this is a valid move
	private boolean isValid(int moveR, int moveC, int player) {
		int boardSize = board.length;
		if (moveR < 0 || moveC < 0 || moveR >= boardSize || moveC >= boardSize
				|| board[moveR][moveC] != EMPTY)
			return false;
		for (int rShift = -1; rShift <= 1; rShift++) {
			for (int cShift = -1; cShift <= 1; cShift++) {
				if (rShift != 0 || cShift != 0) {
					int amount = 1;
					while (moveR + rShift * amount >= 0
							&& moveR + rShift * amount < boardSize
							&& moveC + cShift * amount < boardSize
							&& moveC + cShift * amount >= 0
							&& board[moveR + rShift * amount][moveC + cShift
									* amount] == -player)
						amount++;
					if (amount > 1
							&& moveR + rShift * amount >= 0
							&& moveR + rShift * amount < boardSize
							&& moveC + cShift * amount < boardSize
							&& moveC + cShift * amount >= 0
							&& board[moveR + rShift * amount][moveC + cShift
									* amount] == player)
						return true;
				}
			}
		}
		return false;
	}

	private void printBoard(int lastMoveR, int lastMoveC, byte currPlayer) {
		int boardSize = board.length;
		int score1 = 0;
		int score2 = 0;
		if (boardSize > 8) {
			System.out.print("   ");
			for (int i = 0; i < boardSize; i++) {
				System.out.print((char) (i + 'a'));
			}
			System.out.println();
			System.out.print("   ");
			for (int i = 0; i < boardSize; i++) {
				System.out.print("-");
			}
			System.out.println();
			for (int i = 0; i < boardSize; i++) {
				System.out.format("%2d", i + 1);
				System.out.print("|");
				for (int j = 0; j < boardSize; j++) {
					if (board[i][j] == Reversi.PLAYER_ONE) {
						score1++;
						System.out.print("o");
					} else if (board[i][j] == Reversi.PLAYER_TWO) {
						score2++;
						System.out.print("*");
					} else
						System.out.print(" ");
				}
				System.out.println();
			}
		} else {
			System.out.print("  ");
			for (int i = 0; i < boardSize; i++)
				System.out.print("  " + (char) (i + 'a') + " ");
			System.out.print("\n  +");
			for (int i = 0; i < boardSize; i++)
				System.out.print("---+");
			System.out.println();
			for (int i = 0; i < boardSize; i++) {
				System.out.print((i + 1) + " |");
				for (int j = 0; j < boardSize; j++) {
					if ((board[i][j] == Reversi.PLAYER_ONE)) {
						score1++;
						if (lastMoveR == i && lastMoveC == j)
							System.out.print("-D-|");
						else
							System.out.print(" D |");
					} else if (board[i][j] == Reversi.PLAYER_TWO) {
						score2++;
						if (lastMoveR == i && lastMoveC == j)
							System.out.print("-L-|");
						else
							System.out.print(" L |");
					} else if (isValid(i, j, currPlayer))
						System.out.print(" . |");
					else
						System.out.print("   |");
				}
				System.out.print("\n  +");
				for (int j = 0; j < boardSize; j++)
					System.out.print("---+");
				System.out.println();
			}
		}
		System.out.println("Score: Light " + score2 + " - Dark " + score1);
	}

	public static String moveToString(int moveR, int moveC) {
		return (char) (moveC + 'a') + ("" + (moveR + 1));
	}
	
	public void quitGame(){
		quitGame = true;
		playerOne.quitGame();
		playerTwo.quitGame();
		for(ReversiSpectator spec : spectators){
			spec.quitGame();
		}
	}
	
	// plays the game, players cannot be substituted mid-game
	@Override
	public void run() {
		ReversiPlayer[] players = new ReversiPlayer[] { playerTwo, null,
				playerOne }; // 0 is 2p and 2 is 1p
		currPlayer = PLAYER_ONE;
		int boardSize = board.length;

		// clear the board
		for (int i = 0; i < boardSize; i++)
			for (int j = 0; j < boardSize; j++)
				board[i][j] = EMPTY;

		board[(boardSize / 2) - 1][boardSize / 2 - 1] = PLAYER_TWO;
		board[(boardSize / 2) - 1][boardSize / 2] = PLAYER_ONE;
		board[(boardSize / 2)][boardSize / 2 - 1] = PLAYER_ONE;
		board[boardSize / 2][boardSize / 2] = PLAYER_TWO;

		for (ReversiSpectator spec : spectators)
			spec.gameUpdated(-1, -1);

		printBoard(-1, -1, currPlayer);
		do {
			System.out.println((currPlayer == PLAYER_ONE ? "Dark" : "Light")
					+ " player's turn.");
			do {
				players[currPlayer + 1].playMove();
				if (quitGame)
					return;
				if (applyMove(players[currPlayer + 1].getMoveRow(),
						players[currPlayer + 1].getMoveCol(), currPlayer))
					break;
				else
					System.out.println("\""
							+ moveToString(
									players[currPlayer + 1].getMoveRow(),
									players[currPlayer + 1].getMoveCol())
							+ "\" is not a valid move!. Try again.");

			} while (true);
			byte nextPlayer;
			// figure out the next player
			if (canMove(-currPlayer))
				nextPlayer = (byte) -currPlayer;
			else if (canMove(currPlayer)) {
				nextPlayer = currPlayer;
				if (-currPlayer == PLAYER_ONE)
					System.out
							.println("No valid moves for dark player. Skipping turn!");
				else
					System.out
							.println("No valid moves for light player. Skipping turn!");

			} else {
				nextPlayer = EMPTY;
			}
			int moveR = players[currPlayer + 1].getMoveRow(), moveC = players[currPlayer + 1]
					.getMoveCol();
			printBoard(moveR, moveC, nextPlayer);
			System.out.println("Move played: "
					+ Reversi.moveToString(moveR, moveC));
			// allow the enemy to update
			players[-currPlayer + 1].enemyPlayMove(moveR, moveC);
			currPlayer = nextPlayer;
			System.out.flush();
			for (ReversiSpectator spec : spectators)
				spec.gameUpdated(moveR, moveC);
		} while (currPlayer != EMPTY && !quitGame);
		System.out.println("Game Over!");
	}
}
