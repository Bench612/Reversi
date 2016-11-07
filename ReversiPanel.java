import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import java.awt.Cursor;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;

//draws the Reversi board for a specific Reversi game.
public class ReversiPanel extends JPanel implements MouseInputListener,
		ReversiSpectator {

	static final Color backgroundColor = Color.black;// new Color(156, 101, 82);
	static final Color green1 = new Color(29, 29, 29);// new Color(29, 129, 66);
	static final Color green2 = new Color(50, 50, 50);

	final static int rectSize = 64;

	Reversi game;

	Semaphore waitMove; // used to block and wait for the player to make a move
						// selection
	Semaphore changeState; // used to modify state (lock type semaphore)
	boolean isWaitingMove;

	BufferedImage boardImage;
	Graphics2D imageGraphics;

	Image zombieImage, happyImage;

	int lastRow, lastCol;
	int moveRow, moveCol;

	public ReversiPanel(Reversi game) {
		this.game = game;
		this.setBackground(backgroundColor);

		waitMove = new Semaphore(0);
		changeState = new Semaphore(1);
		isWaitingMove = false;
		moveRow = -1;
		moveCol = -1;
		game.addSpectator(this);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);

		boardImage = new BufferedImage(game.getBoardSize() * rectSize,
				game.getBoardSize() * rectSize, BufferedImage.TYPE_INT_RGB);
		try {
			zombieImage = ImageIO.read(new File("zombie_line.png"));
			happyImage = ImageIO.read(new File("happy_line.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		imageGraphics = boardImage.createGraphics();
	}

	// returns when the move is available
	public void getMove() {
		// first check the board state, must be in standard state
		boolean correctState = false;
		try {
			changeState.acquire();
			if (!isWaitingMove) {
				correctState = true;
				isWaitingMove = true;
			}
			changeState.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (correctState) {
			try {
				waitMove.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public int getMoveRow() {
		return moveRow;
	}

	public int getMoveCol() {
		return moveCol;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int shiftX = (getWidth() - game.getBoardSize() * rectSize) / 2, shiftY = (getHeight() - game
				.getBoardSize() * rectSize) / 2;
		g.drawImage(boardImage, shiftX, shiftY, null);
		g.setColor(Color.white);
		g.setFont(g.getFont().deriveFont(16f));
		for (int i = 0; i < game.getBoardSize(); i++) {
			String c = (char) ('a' + i) + "";
			g.drawString(c, shiftX + rectSize * i
					+ (rectSize - g.getFontMetrics().stringWidth(c)) / 2,
					shiftY - g.getFontMetrics().getHeight());
		}
		for (int i = 0; i < game.getBoardSize(); i++) {
			String c = i + 1 + "";
			g.drawString(c, shiftX - rectSize
					+ (rectSize - g.getFontMetrics().stringWidth(c)) / 2,
					shiftY + rectSize / 2 + rectSize * i
							+ g.getFontMetrics().getHeight() / 2);
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		try {
			changeState.acquire();
			if (isWaitingMove) {
				int shiftX = (getWidth() - game.getBoardSize() * rectSize) / 2, shiftY = (getHeight() - game
						.getBoardSize() * rectSize) / 2;
				moveRow = (arg0.getY() - shiftY) / rectSize;
				moveCol = (arg0.getX() - shiftX) / rectSize;

				isWaitingMove = false;
				this.setCursor(Cursor.getDefaultCursor());

				waitMove.release();
			}
			changeState.release();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		try {
			changeState.acquire();
			if (isWaitingMove) {
				int shiftX = (getWidth() - game.getBoardSize() * rectSize) / 2, shiftY = (getHeight() - game
						.getBoardSize() * rectSize) / 2;
				moveRow = (arg0.getY() - shiftY) / rectSize;
				moveCol = (arg0.getX() - shiftX) / rectSize;
				if (game.canMove(moveRow, moveCol))
					this.setCursor(Cursor
							.getPredefinedCursor(Cursor.HAND_CURSOR));
				else
					this.setCursor(Cursor.getDefaultCursor());
			}
			changeState.release();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y,
			int width, int height) {
		redrawImage();
		return (infoflags & ImageObserver.ALLBITS) == 0;
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent arg0) {

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

	}

	@Override
	public void mouseDragged(MouseEvent arg0) {

	}

	@Override
	public void gameUpdated(int row, int col) {
		lastRow = row;
		lastCol = col;
		redrawImage();
	}

	public void redrawImage() {
		for (int i = 0; i < game.getBoardSize(); i++)
			for (int j = 0; j < game.getBoardSize(); j++) {
				if ((i + j) % 2 == 0)
					imageGraphics.setColor(green1);
				else
					imageGraphics.setColor(green2);
				imageGraphics.fillRect(j * rectSize, i * rectSize, rectSize,
						rectSize);
				imageGraphics.setColor(Color.black);
				imageGraphics.drawRect(j * rectSize, i * rectSize, rectSize,
						rectSize);
				if (game.getBoard(i, j) != Reversi.EMPTY) {
					if (game.getBoard(i, j) == Reversi.PLAYER_ONE)
						imageGraphics.drawImage(zombieImage, j * rectSize
								+ rectSize * 7 / 40, +i * rectSize + rectSize
								* 7 / 40, rectSize * 7 / 10, rectSize * 7 / 10,
								this);
					else
						imageGraphics.drawImage(happyImage, j * rectSize
								+ rectSize * 7 / 40, +i * rectSize + rectSize
								* 7 / 40, rectSize * 7 / 10, rectSize * 7 / 10,
								this);
				} else if (game.canMove(i, j)) {
					if (game.getCurrentPlayer() == Reversi.PLAYER_ONE)
						imageGraphics.setColor(Color.lightGray);
					else
						imageGraphics.setColor(Color.yellow);
					imageGraphics.fillOval(j * rectSize + rectSize / 2 - 4, +i
							* rectSize + rectSize / 2 - 4, 8, 8);
				}

			}
		repaint();
	}

}
