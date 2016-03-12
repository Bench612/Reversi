/*
 * Benjamin Chang
 * March 2016
 */

import java.util.ArrayList;
import java.util.Collections;

public class ReversiAI implements ReversiPlayer {

	final static int WIN = 2000000000;
	final static int LOSE = -WIN;
	final static int DRAW = WIN / 2;

	int minDepth = 1000;
	int maxDepth = 0;
	// variables to calculate how long it takes to initialize
	long initTime[];
	long numMoves[];
	long numAtMoves[];
	boolean outOfMemory; // based on depth not total moves
	// variables to change the heuristic and stuff
	int cutoff1; // when to switch to more basic heuristic
	int cutoff2; // when to switch to most basic heuristic
	int cutoff3; // when to start allocating more of the
					// remaining time
	int nodesCreated;

	int[] permShiftType;
	int[] permShiftNotType;
	int[] dirOffsets;
	int[] queue;
	// first numBoardSquares of pBaseBoardWeights is for player
	int[] pBaseBoardWeights;
	int boardSize;
	int numBoardSquares;

	SearchTree head;
	long totalTime;
	long turnTimeLimit;

	public ReversiAI(int boardSize, long totalTime, long turnTimeLimit,
			boolean first) {
		this.totalTime = totalTime;
		this.turnTimeLimit = turnTimeLimit;
		initTime = new long[boardSize * boardSize + 2];
		numMoves = new long[boardSize * boardSize + 2];
		numAtMoves = new long[boardSize * boardSize + 2];
		int boardDim = boardSize + 2;
		dirOffsets = new int[] { -boardDim - 1, -boardDim, -boardDim + 1, -1,
				boardDim - 1, boardDim + 1, boardDim, 1 };
		initHeuristics(boardSize);
		head = new SearchTree(boardSize, first);
	}

	// helper function for initHeuristics
	private void setCornerSides(int[] toSet, int val) {
		int boardDim = boardSize + 2;
		toSet[2 + boardDim] = val;
		toSet[2 * boardDim + 1] = val;
		toSet[boardDim + boardSize - 1] = val;
		toSet[2 * boardDim + boardSize] = val;
		toSet[(boardSize - 1) * boardDim + 1] = val;
		toSet[boardSize * boardDim + 2] = val;
		toSet[(boardSize - 1) * boardDim + boardSize] = val;
		toSet[(boardSize) * boardDim + boardSize - 1] = val;
	}

	// helper function for initHeuristics
	private void setCorners(int[] toSet, int value) {
		int boardDim = boardSize + 2;
		toSet[boardDim + 1] = value;
		toSet[boardDim + boardSize] = value;
		toSet[1 + boardSize * boardDim] = value;
		toSet[boardDim * boardDim - boardDim - 2] = value;
	}

	private void initHeuristics(int boardSize) {
		this.boardSize = boardSize;
		int boardDim = boardSize + 2;
		int numSquares = boardSize * boardSize;
		numBoardSquares = boardDim * boardDim;
		// compute full heuristic for first 70p
		cutoff1 = numSquares * 70 / 100;
		// maximize efficiency for last squares
		cutoff2 = numSquares * 85 / 100;
		// maximize depth and time for final squares
		cutoff3 = numSquares - 20;
		permShiftType = new int[numBoardSquares];
		permShiftNotType = new int[numBoardSquares];
		pBaseBoardWeights = new int[numBoardSquares * 2];
		queue = new int[numSquares * 9 + 1];
		// set corners
		int cornerVal = numSquares * numSquares / 3;
		int cornerSideVal = -cornerVal / 2;
		setCorners(pBaseBoardWeights, cornerVal);
		setCornerSides(pBaseBoardWeights, cornerSideVal);
		// set the sides
		int sideVal = (boardSize - 1) * (boardSize - 1) * numSquares
				/ (4 * (boardSize + 2));
		for (int i = 2; i < boardSize - 2; i++) {
			pBaseBoardWeights[i + 1 + boardDim] = sideVal;
			pBaseBoardWeights[boardSize * boardDim + i + 1] = sideVal;
			pBaseBoardWeights[(i + 1) * boardDim + 1] = sideVal;
			pBaseBoardWeights[(i + 1) * boardDim + boardSize] = sideVal;
		}

		int outside = boardSize * 4 - 4;
		for (int i = 1; i < boardSize / 2; i++) {
			int inside = ((boardSize - (i * 2)) * 4 - 4);
			int val = (numSquares - outside - inside) * numSquares / outside;
			for (int j = i; j < boardSize - i; j++) {
				pBaseBoardWeights[(i + 1) * boardDim + j + 1] = val; // top
				pBaseBoardWeights[(boardSize - i) * boardDim + j + 1] = val; // bottom
				pBaseBoardWeights[(j + 1) * boardDim + (i + 1)] = val; // left
				pBaseBoardWeights[(j + 1) * boardDim + (boardSize - i)] = val; // right
			}
			pBaseBoardWeights[(i + 1) * boardDim + i + 1] = 2 * val;
			pBaseBoardWeights[(i + 1) * boardDim + boardDim - i] = 2 * val;
			pBaseBoardWeights[(boardSize - i) * boardDim + i + 1] = 2 * val;
			pBaseBoardWeights[(boardSize - i) * boardDim + boardSize - i] = 2 * val;
			outside += inside;
		}

		for (int i = 0; i < numBoardSquares; i++) {
			pBaseBoardWeights[numBoardSquares + i] = pBaseBoardWeights[i];
			pBaseBoardWeights[i] *= -1;
		}

		for (byte i = 0; i < boardSize / 2; i++) {
			int scale = boardSize / 2 - i;
			for (int j = i; j < boardSize - i; j++) {
				int valShift = -scale * 3
						* pBaseBoardWeights[(i + 1) * boardDim + j + 1] / 2;
				permShiftType[(i + 1) * boardDim + j + 1] = valShift; // top
				permShiftType[(boardSize - i) * boardDim + j + 1] = valShift; // bottom
				permShiftType[(j + 1) * boardDim + (i + 1)] = valShift; // left
				permShiftType[(j + 1) * boardDim + (boardSize - i)] = valShift; // right
				int valNotShift = (scale - 1)
						* pBaseBoardWeights[(i + 1) * boardDim + j + 1]
						/ (scale);
				permShiftNotType[(i + 1) * boardDim + j + 1] = valNotShift; // top
				permShiftNotType[(boardSize - i) * boardDim + j + 1] = valNotShift; // bottom
				permShiftNotType[(j + 1) * boardDim + (i + 1)] = valNotShift; // left
				permShiftNotType[(j + 1) * boardDim + (boardSize - i)] = valNotShift; // right
			}
		}
		setCornerSides(permShiftType, -3 * cornerSideVal / 2);
		setCornerSides(permShiftNotType, (boardSize / 3 - 1) * cornerSideVal
				/ (boardSize / 3));
	}

	@Override
	public void playMove() {
		long startTime = System.currentTimeMillis();
		nodesCreated = 0;
		minDepth = 1000;
		maxDepth = 0;
		outOfMemory = false;
		long duration = Math.min(
				head.totalMoves >= cutoff3 ? totalTime * 40 / 100 : totalTime
						/ ((boardSize * boardSize) - head.totalMoves),
				turnTimeLimit);
		System.out.println("\nTime remaining: " + totalTime / 1000 + "s"
				+ ", Turn time: " + duration + "ms\nAI thinking...");
		head = head.makeMove(head.performSearch(duration));
		// find the best heuristic
		long timeSpent = System.currentTimeMillis() - startTime;
		System.out.println("Move: "
				+ Reversi.moveToString(getMoveRow(), getMoveCol())
				+ ", Estimate: " + head.heuristic);
		System.out.println("Min Depth: " + minDepth + ", Max depth: "
				+ maxDepth + ", New nodes: " + nodesCreated + ", Time Spent: "
				+ timeSpent + "ms");
		totalTime -= timeSpent; // update total
								// time
	}

	@Override
	public int getMoveRow() {
		return (head.move / (boardSize + 2)) - 1;
	}

	@Override
	public int getMoveCol() {
		return (head.move % (boardSize + 2)) - 1;
	}

	public void enemyPlayMove(int row, int col) {
		head = head.makeMove((row + 1) * (boardSize + 2) + col + 1);
	}

	private class SearchTree implements Comparable<SearchTree> {
		final static byte PERIMETER = 2;
		final static byte OUT = 4;
		final static byte ENEMY = -1;
		final static byte EMPTY = 0;
		final static byte AI = 1;

		// type of move just played and position
		final byte type;
		final byte parentType;
		final int move;
		final int totalMoves;

		// set before initialization
		private int rawHeuristicDiff;
		int heuristic; // value is > 0 if AI is winning < 0 o.w
		// removed after initialization
		byte[] board;
		private ArrayList<Integer> parentPerimeter;
		private byte[] boardStruct;
		private byte[] perimCounts; // first 4 bytes represent the player
		// set after initialization
		private final int mask4 = 0b1111;
		private boolean initialized;
		private ArrayList<SearchTree> movesSearch;

		final static byte normal = 0;
		final static byte permPerim = 1;
		final static byte perm = 2;

		SearchTree(int boardSize, boolean aiFirst) {
			boardStruct = new byte[numBoardSquares];
			perimCounts = new byte[numBoardSquares];

			int boardDim = boardSize + 2;

			// set the perm squares
			for (int i = 0; i < boardDim; i++) {
				boardStruct[(boardDim - 1) * boardDim + i] = -1;
				boardStruct[i] = -1;
				boardStruct[i * boardDim] = -1;
				boardStruct[i * boardDim + boardDim - 1] = -1;
			}
			// set corners
			boardStruct[boardDim + 1] = 5;
			boardStruct[boardDim + boardSize] = 5;
			boardStruct[1 + boardSize * boardDim] = 5;
			boardStruct[numBoardSquares - boardDim - 2] = 5;

			// hardcode the initial board
			board = new byte[numBoardSquares];
			for (int i = 0; i < boardDim; i++) {
				board[(boardDim - 1) * boardDim + i] = OUT;
				board[i] = OUT;
				board[i * boardDim] = OUT;
				board[i * boardDim + boardDim - 1] = OUT;
			}
			type = aiFirst ? ENEMY : AI;
			parentType = (byte) -type;
			board[(boardSize / 2) * boardDim + boardSize / 2] = type;
			board[(boardSize / 2) * boardDim + boardSize / 2 + 1] = (byte) -type;
			board[(boardSize / 2 + 1) * boardDim + boardSize / 2 + 1] = type;
			board[(boardSize / 2 + 1) * boardDim + boardSize / 2] = (byte) -type;
			totalMoves = 4;
			move = -1;

			rawHeuristicDiff = 0;
			heuristic = rawHeuristicDiff;

			// find the perimeter and moves
			movesSearch = new ArrayList<SearchTree>(4);
			ArrayList<Integer> perimeter = new ArrayList<Integer>(12);
			for (int rO = -1; rO <= 2; rO++)
				for (int cO = -1; cO <= 2; cO++) {
					int pos = (boardSize / 2 + rO) * boardDim + boardSize / 2
							+ cO;
					if (board[pos] == EMPTY) {
						board[pos] = PERIMETER;
						perimeter.add(pos);
						if (verifyPossibleMove(pos, (byte) -type))
							movesSearch.add(new SearchTree(this, perimeter,
									(byte) -type, pos));
					}
				}
			initialized = true;
		}

		private SearchTree(SearchTree parent,
				ArrayList<Integer> parentPerimeter, byte t, int move) {
			this.move = move;
			this.parentType = parent.type;
			this.totalMoves = parent.totalMoves + 1;
			this.parentPerimeter = parentPerimeter;
			this.boardStruct = parent.boardStruct;
			this.perimCounts = parent.perimCounts;
			board = parent.board.clone();
			type = t;
			initialized = false;

			board[move] = t;
			final int iType = (type + 1) / 2;
			final int iNotType = (1 - type) / 2;
			final int pMoveType = iType * numBoardSquares + move;
			final int pMoveNotType = iNotType * numBoardSquares + move;
			int rawDiff = pBaseBoardWeights[pMoveType]
					+ (permShiftType[move] * ((parent.perimCounts[move] >> iType) & mask4))
					- pBaseBoardWeights[pMoveNotType]
					- (permShiftNotType[move] * ((parent.perimCounts[move] >> iNotType) & mask4));
			// flip everything
			for (int i = 0; i < 8; i++) {
				int amount = 1;
				while (board[amount * dirOffsets[i] + move] == -type)
					amount++;
				if (board[amount * dirOffsets[i] + move] == type) {
					for (amount--; amount > 0; amount--) {
						int pos = dirOffsets[i] * amount + move;
						board[pos] = type;
						rawDiff += 2 * (pBaseBoardWeights[pMoveType]
								+ (permShiftType[pos] * ((parent.perimCounts[pos] >> iType) & mask4))
								- pBaseBoardWeights[pMoveNotType] - (permShiftNotType[pos] * ((parent.perimCounts[move] >> iNotType) & mask4)));
					}
				}
			}
			rawHeuristicDiff = rawDiff + parent.rawHeuristicDiff;
			heuristic = rawHeuristicDiff;
		}

		// creates the searched moves and updates the structure
		private boolean initialize() {
			long startTime = System.currentTimeMillis();
			try {
				// calculate the structure changes
				if (totalMoves < cutoff2 && boardStruct[move] == permPerim
						&& isNewPerm(move)) {
					// make copies
					byte[] newBoardStruct = boardStruct.clone();
					byte[] newPerimCounts = perimCounts.clone();
					newBoardStruct[move] = perm;
					queue[0] = -move;
					int queueTail = 1;
					for (int k = 0; k < queueTail; k++) {
						final int posS = queue[k];
						if (posS < 0) {
							for (int i = 0; i < 8; i++) {
								int posShift = -posS + dirOffsets[i];
								if (newBoardStruct[posShift] == normal
										|| ((totalMoves < cutoff1) && newBoardStruct[posShift] >= 0)) {
									newBoardStruct[posShift] = permPerim;
									newPerimCounts[posShift] += 0b10000 >> ((board[-posS] + 1) / 2) * 4;
									queue[queueTail] = posShift;
									queueTail++;
								}
							}
						} else {
							if (board[posS] % 2 != 0 && isNewPerm(posS)) {
								newBoardStruct[posS] = perm;
								queue[queueTail] = -posS;
								queueTail++;
							}
						}
					}
					boardStruct = newBoardStruct;
					perimCounts = newPerimCounts;
				}

				// calculate the possible moves
				// update perimeter
				ArrayList<Integer> perimeter = new ArrayList<Integer>(
						parentPerimeter.size() + 7);
				for (Integer i : parentPerimeter)
					if (i != move)
						perimeter.add(i);
				for (int i = 0; i < 8; i++) {
					int pos = move + dirOffsets[i];
					if (board[pos] == EMPTY) {
						board[pos] = PERIMETER;
						perimeter.add(pos);
					}
				}
				ArrayList<SearchTree> newMovesSearch = new ArrayList<SearchTree>(
						perimeter.size() * 3 / 4);
				// check everything on the perimeter
				for (Integer i : perimeter)
					if (verifyPossibleMove(i, (byte) -type))
						newMovesSearch.add(new SearchTree(this, perimeter,
								(byte) -type, i));
				// if no moves for next player
				if (newMovesSearch.size() == 0) {
					for (Integer i : perimeter)
						if (verifyPossibleMove(i, type))
							newMovesSearch.add(new SearchTree(this, perimeter,
									type, i));
					if (newMovesSearch.size() == 0) {
						// then game is over, do a raw count and find the winner
						int diff = 0;
						for (int i = 0; i < board.length; i++)
							diff += board[i] % 2;
						heuristic = diff == 0 ? DRAW : (diff > 0 ? WIN : LOSE);
					}
				}
				// Hurray no out of memory error!
				board = null;
				parentPerimeter = null;
				boardStruct = null;
				perimCounts = null;
				movesSearch = newMovesSearch;
				nodesCreated += movesSearch.size();
				initialized = true;
				// average over 3
				numMoves[totalMoves] += movesSearch.size();
				initTime[totalMoves] = System.currentTimeMillis() - startTime;
				numAtMoves[totalMoves]++;
				return true;
			} catch (java.lang.OutOfMemoryError e) {
				System.out.println("Out of memory! Returning best answer");
				outOfMemory = true;
				return false;
			}
		}

		private boolean isNewPerm(int pos) {
			for (int i = 0; i < 4; i++) {
				final int p1 = pos + dirOffsets[i];
				final int p2 = pos - dirOffsets[i];
				final boolean notP1FixType = boardStruct[p1] >= 0
						|| (board[p1] != type && board[p1] != OUT);
				final boolean notP2FixType = boardStruct[p2] >= 0
						|| (board[p2] != type && board[p2] != OUT);
				if (notP1FixType
						&& notP2FixType
						&& (boardStruct[p1] >= 0 || board[p1] != -type || boardStruct[p2] >= 0
								&& board[p2] != -type))
					return false;
			}
			return true;
		}

		private boolean verifyPossibleMove(int pos, byte type) {
			for (int i = 0; i < 8; i++) {
				int j = 1;
				while (board[pos + j * dirOffsets[i]] == -type)
					j++;
				if (j > 1 && board[pos + j * dirOffsets[i]] == type)
					return true;
			}
			return false;
		}

		//returns -1 indicating an error
		int performSearch(long duration) {
			long startTime = System.currentTimeMillis();
			heuristic = performSearch(WIN, startTime + duration, 0);
			// find the best heuristic
			for (int i = 0; i < movesSearch.size(); i++)
				if (movesSearch.get(i).heuristic >= heuristic)
					return movesSearch.get(i).move;
			return -1;
		}

		// depth is the current depth
		private int performSearch(int parentCurrentBest, long endTime, int depth) {
			// if its not initialized, try to initialize if there is enough time
			if (!initialized) {
				final boolean shouldInitialize = !initialized
						&& !outOfMemory
						&& (depth % 2 == 1 || (endTime
								- System.currentTimeMillis() > initTime[totalMoves]
								/ Math.max(numAtMoves[totalMoves], 1)
								+ initTime[totalMoves + 1]
								/ Math.max(numAtMoves[totalMoves + 1], 1)
								* numMoves[totalMoves]
								/ Math.max(numAtMoves[totalMoves + 1], 1)));
				if (!shouldInitialize || !initialize()) {
					minDepth = Math.min(minDepth, depth);
					maxDepth = Math.max(maxDepth, depth);
					return heuristic;
				}
			}
			if (movesSearch.size() == 0) {// game over
				minDepth = Math.min(minDepth, depth);
				maxDepth = Math.max(maxDepth, depth);
				return heuristic;
			}
			Collections.sort(movesSearch);
			final boolean sameTypeAsParent = parentType == type;
			// give each branch equal amount of the remaining time
			final int childType = movesSearch.get(0).type;
			int currentBest = LOSE * childType;
			for (int i = 0; i < movesSearch.size(); i++) {
				long currentTime = System.currentTimeMillis();
				int h = movesSearch.get(i).performSearch(
						sameTypeAsParent ? parentCurrentBest : currentBest,
						currentTime + (endTime - currentTime)
								/ (movesSearch.size() - i), depth + 1);
				if (h * childType > currentBest * childType) {
					// alpha/beta pruning
					if (h * childType >= parentCurrentBest * childType) {
						heuristic = h;
						return heuristic;
					}
					currentBest = h;
				}
			}
			heuristic = currentBest;
			return heuristic;
		}

		// applies a move by returning the new head
		SearchTree makeMove(int move) {
			SearchTree newHead = movesSearch.get(0);
			for (int i = 0; i < movesSearch.size(); i++)
				if (movesSearch.get(i).move == move) {
					newHead = movesSearch.get(i);
					break;
				}
			if (!newHead.initialized)
				newHead.initialize();
			return newHead;
		}

		@Override
		public int compareTo(SearchTree arg0) {
			return (parentType) * (arg0.heuristic - heuristic);
		}
	}
}
