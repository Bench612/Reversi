/*
 * Benjamin Chang
 * March 2016
 * This class makes no attempts at optimization
 */

public class Reversi {
	final static byte PLAYER_ONE = 1;
	final static byte PLAYER_TWO = -1;
	final static byte EMPTY = 0;

	// time in milliseconds
	static final long TIME_LIMIT = 1000 * 90;
	static final long TURN_TIME_LIMIT = 1000 * 5;
	static final int DEFAULT_BOARD_SIZE = 8;
	static final boolean DEFAULT_AI_FIRST = false;

	static byte[][] board;
	static byte currentTurn;

	public static void main(String[] args) {
		boolean humanGame = false;
		boolean aiFirst = DEFAULT_AI_FIRST;
		long totalTimeLeft = TIME_LIMIT;
		int boardSize = DEFAULT_BOARD_SIZE;
		long turnTimeLimit = TURN_TIME_LIMIT;
		boolean incorrectArguements = false;
		try {
			for (int i = 0; i < args.length; i++)
				switch (args[i]) {
				case "-l":
					aiFirst = true;
					break;
				case "-n":
					boardSize = Integer.parseInt(args[++i]);
					if (boardSize > 26 || boardSize < 4 || boardSize % 2 != 0)
						incorrectArguements = true;
					break;
				case "-t":
					totalTimeLeft = Integer.parseInt(args[++i]) * 1000;
					if (totalTimeLeft <= 0)
						incorrectArguements = true;
					break;
				case "-u":
					turnTimeLimit = Integer.parseInt(args[++i]) * 1000;
					if (totalTimeLeft <= 0)
						incorrectArguements = true;
					break;
				case "-h":
					humanGame = true;
					break;
				default:
					incorrectArguements = true;
					break;
				}
		} catch (Exception e) {
			incorrectArguements = true;
		} finally {
			if (incorrectArguements) {
				System.out
						.println("Command arguments are [-n <size>] [-l] [-h] [-t <total time limit>] [-u <turn time limit>]");
				System.exit(-1);
			}
		}
		ReversiPlayer[] players = new ReversiPlayer[3]; // 0 is 2p and 2 is 1p
		if (humanGame) {
			players[0] = new ReversiConsolePlayer(boardSize, false);
			players[2] = new ReversiConsolePlayer(boardSize, true);
		} else {
			int aiType = aiFirst ? PLAYER_ONE : PLAYER_TWO;
			players[aiType + 1] = new ReversiAI(boardSize, totalTimeLeft,
					turnTimeLimit, aiFirst);
			players[-aiType + 1] = new ReversiConsolePlayer(boardSize, !aiFirst);
		}
		board = new byte[boardSize][boardSize];
		board[(boardSize / 2) - 1][boardSize / 2 - 1] = PLAYER_TWO;
		board[(boardSize / 2) - 1][boardSize / 2] = PLAYER_ONE;
		board[(boardSize / 2)][boardSize / 2 - 1] = PLAYER_ONE;
		board[boardSize / 2][boardSize / 2] = PLAYER_TWO;
		byte currPlayer = PLAYER_ONE;
		printBoard(-1, -1, currPlayer);
		do {
			System.out.println((currPlayer == PLAYER_ONE ? "Dark" : "Light")
					+ " player's turn.");
			do {
				players[currPlayer + 1].playMove();
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
			printBoard(players[currPlayer + 1].getMoveRow(),
					players[currPlayer + 1].getMoveCol(), nextPlayer);
			System.out.println("Move played: "
					+ Reversi.moveToString(
							players[currPlayer + 1].getMoveRow(),
							players[currPlayer + 1].getMoveCol()));
			// allow the enemy to update
			players[-currPlayer + 1].enemyPlayMove(
					players[currPlayer + 1].getMoveRow(),
					players[currPlayer + 1].getMoveCol());
			currPlayer = nextPlayer;
			System.out.flush();
		} while (currPlayer != EMPTY);
		printBoard(players[currPlayer + 1].getMoveRow(),
				players[currPlayer + 1].getMoveCol(), currPlayer);
		System.out.println("Game Over!");
	}

	// returns if the move was valid
	public static boolean applyMove(int moveR, int moveC, byte player) {
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

	public static boolean canMove(int player) {
		for (int i = 0; i < board.length; i++)
			for (int j = 0; j < board.length; j++)
				if (board[i][j] == EMPTY && isValid(i, j, player))
					return true;
		return false;
	}

	public static boolean isValid(int moveR, int moveC, int player) {
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

	public static void printBoard(int lastMoveR, int lastMoveC, byte currPlayer) {
		int boardSize = board.length;
		int score1 = 0;
		int score2 = 0;
		if (boardSize > 10) {
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
}
