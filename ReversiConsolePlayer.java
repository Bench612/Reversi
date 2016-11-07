/*
 * Benjamin Chang
 * March 2016
 */
import java.io.IOException;

public class ReversiConsolePlayer implements ReversiPlayer {

	byte[] buffer = new byte[256];
	int boardSize;
	int moveRow, moveCol;

	public ReversiConsolePlayer(int boardSize) {
		this.boardSize = boardSize;
	}

	@Override
	public void playMove() {
		int length = 0;
		boolean correctInput;
		do {
			correctInput = true;
			try {
				System.out.print("> ");
				System.gc();
				length = System.in.read(buffer);
				while (length > 0
						&& Character.isWhitespace((char) buffer[length - 1]))
					length--;
				if (length >= 2 && Character.isAlphabetic(buffer[0])) {
					moveCol = buffer[0] - 'a';
					moveRow = Integer
							.parseInt(new String(buffer, 1, length - 1)) - 1;
				} else {
					System.out.println("Invalid format \""
							+ new String(buffer, 0, length)
							+ "\". Example entry: a1");
				}
			} catch (IOException e) {
				System.out.println("ERR: Reading from system in.");
				System.exit(-1);
			} catch (NumberFormatException e) {
				System.out.println("Invalid format \""
						+ new String(buffer, 0, length)
						+ "\". Example entry: a1");
				correctInput = false;
			}
		} while (!correctInput);
	}

	@Override
	public int getMoveRow() {
		return moveRow;
	}

	@Override
	public int getMoveCol() {
		return moveCol;
	}

	public static int stringToMove(String s, int boardSize) {
		if (s.length() >= 2) {
			String substring = s.substring(1);
			if (Character.isAlphabetic(s.charAt(0))
					&& substring.matches("\\d++"))
				return (Character.toLowerCase(s.charAt(0)) - 'a')
						+ (Integer.parseInt(s.substring(1))) * (boardSize + 2);
		}
		return -1;
	}

	@Override
	public void enemyPlayMove(int moveR, int moveC) {
	}

	@Override
	public void quitGame() {
		//TODO: no way to cancel System.in.read
	}

}
