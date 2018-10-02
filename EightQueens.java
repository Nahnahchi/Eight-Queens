import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import java.awt.*;

public class EightQueens extends JPanel {

	private static final long serialVersionUID = 1443370054948073843L;
	private static final int BOARD_SIZE = 8;
	private static final String QUEEN = "Q";
	private static final String EMPTY = "E";
	private final JPanel gui = new JPanel(new BorderLayout(3, 3));
	private final JLabel message = new JLabel();
	private static ImageIcon icon;
	private JButton[][] cells;
	private int count;

	public EightQueens() {
		initialize();
	}

	private void startNewGame() {
		Random r = new Random();
		int rr = r.nextInt(BOARD_SIZE);
		int rc = r.nextInt(BOARD_SIZE);
		gui.setVisible(false);
		for (JButton[] buttons : cells) {
			for (JButton cell : buttons) {
				cell.setIcon(null);
				cell.setText(EMPTY);
				cell.setEnabled(true);
				cell.setBorder(new LineBorder(Color.BLACK));
			}
		}
		cells[rr][rc].setIcon(icon);
		cells[rr][rc].setText(QUEEN);
		cells[rr][rc].setEnabled(false);
		cells[rr][rc].setBorder(new LineBorder(Color.YELLOW, 4));
		count = 1;
		gui.setVisible(true);
		updateBoard();
	}

	private void queenAction(JButton cell) {
		if ((cell.getText()).equals(QUEEN)) {
			cell.setIcon(null);
			cell.setText(EMPTY);
			count--;
		} else {
			if (count == BOARD_SIZE) {
				message.setText("NO QUEENS LEFT");
				return;
			} else {
				cell.setIcon(icon);
				cell.setText(QUEEN);
				count++;
			}
		}
		updateBoard();
	}

	private void updateBoard() {

		boolean war = false;
		Object[] queens = new Object[count];
		
		gui.setVisible(false);
		
		for (int i = 0, r = 0; r < BOARD_SIZE; r++) {
			for (int c = 0; c < BOARD_SIZE; c++) {
				switch (cells[r][c].getText()) {
				case EMPTY:
					cells[r][c].setBackground((r + c) % 2 == 0 ? Color.WHITE : Color.BLACK);
					break;
				case QUEEN:
					queens[i++] = new int[] { r, c };
					break;
				}
			}
		}

		for (int i = 0; i < count; i++) {
			int[] qn = (int[]) queens[i];
			int r1 = qn[0];
			int c1 = qn[1];
			boolean combat = false;
			for (int j = 0; j < count; j++) {
				int[] q = (int[]) queens[j];
				int r2 = q[0];
				int c2 = q[1];
				if (Math.abs(r1 - r2) == Math.abs(c1 - c2) || r1 == r2 || c1 == c2) {
					if (!qn.equals(q)) {
						combat = true;
						break;
					}
				}
			}
			cells[r1][c1].setBackground(combat ? Color.RED : Color.BLUE);
			war |= combat;
		}
		
		gui.setVisible(true);

		if (BOARD_SIZE == count && !war) {
			gameComplete(queens);
		} else {
			message.setText("QUEENS LEFT: " + (BOARD_SIZE - count));
		}

	}

	private void gameComplete(Object[] qns) {
		gui.setVisible(false);
		for (int i = 0; i < count; i++) {
			int[] qn = (int[]) qns[i];
			int r = qn[0];
			int c = qn[1];
			cells[r][c].setEnabled(false);
			cells[r][c].setBorder(new LineBorder(Color.MAGENTA, 4));

		}
		gui.setVisible(true);
		message.setText("YOU WON");
		int game = JOptionPane.showOptionDialog(gui, "Start a new game?", "Game Complete", 
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
		if (game == JOptionPane.YES_OPTION) {
			startNewGame();
		}
	}

	private void save(Object b, File f) {
		try {
			if (!f.exists()) {
				f.createNewFile();
			}
		} catch (IOException ioe) {
			message.setText(ioe.getMessage());
		}
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
			oos.writeObject(b);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(gui, ex.getMessage());
		}
	}

	private Object loadFrom(File f) {
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
			return ois.readObject();
		} catch (Exception ex) {
			return null;
		}
	}
	
	private Image getScaledImage(Image srcImg, int w, int h) {

		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = resizedImg.createGraphics();

		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.drawImage(srcImg, 0, 0, w, h, null);
		g2d.dispose();

		return resizedImg;

	}

	private ActionListener listener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof JButton) {
				JButton btn = (JButton) e.getSource();
				queenAction(btn);
			}
		}

	};

	private final void initialize() {

		File data = new File("data");
		data.mkdirs();
		File qn = new File(data, "icon");
		File save = new File(data, "save");
		save.mkdirs();
		File savefile = new File(save, "savefile.dat");
		try {
			icon = new ImageIcon(getScaledImage(ImageIO.read(new File(qn, "queen.png")), 60, 60));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(gui, e.getMessage() 
					+ " (" + qn.getPath() + File.separator + "queen.png)");
		}

		boolean loaded;
		try {
			Object[] source = (Object[]) loadFrom(savefile);
			cells = (JButton[][]) source[0];
			count = (int) source[1];
			loaded = true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(gui, "New Game");
			cells = new JButton[BOARD_SIZE][BOARD_SIZE];
			count = 1;
			loaded = false;
		}

		message.setText("QUEENS LEFT: " + (BOARD_SIZE - count));
		gui.setBorder(new EmptyBorder(5, 5, 5, 5));
		JToolBar tools = new JToolBar();
		tools.setFloatable(false);
		gui.add(tools, BorderLayout.PAGE_START);
		JButton newGame = new JButton("New Game");
		newGame.addActionListener((ActionEvent event) -> {
			startNewGame();
		});
		tools.add(newGame);
		tools.addSeparator();
		JButton exitGame = new JButton("Save and Quit");
		exitGame.addActionListener((ActionEvent event) -> {
			save(new Object[] { cells, count }, savefile);
			System.exit(0);
		});
		tools.add(exitGame);
		tools.addSeparator();
		tools.add(message);

		JPanel board = new JPanel(new GridLayout(0, 9));
		board.setBorder(new LineBorder(Color.BLACK, 2));
		gui.add(board);

		if (loaded) {
			for (JButton[] buttons : cells) {
				for (JButton cell : buttons) {
					cell.addActionListener(listener);
				}
			}
		} else {
			Insets buttonMargin = new Insets(0, 0, 0, 0);
			for (int row = 0; row < cells.length; row++) {
				for (int col = 0; col < cells.length; col++) {
					JButton btn = new JButton(EMPTY);
					btn.setFont(new Font(null, 0, 0));
					btn.setMargin(buttonMargin);
					btn.setPreferredSize(new Dimension(64, 64));
					btn.setBorder(new LineBorder(Color.BLACK));
					if ((row + col) % 2 == 0) {
						btn.setBackground(Color.WHITE);
					} else {
						btn.setBackground(Color.BLACK);
					}
					cells[row][col] = btn;
					cells[row][col].addActionListener(listener);
				}
			}
			Random r = new Random();
			int rr = r.nextInt(BOARD_SIZE);
			int rc = r.nextInt(BOARD_SIZE);
			cells[rr][rc].setText(QUEEN);
			cells[rr][rc].setIcon(icon);
			cells[rr][rc].setEnabled(false);
			cells[rr][rc].setBorder(new LineBorder(Color.YELLOW, 4));
			cells[rr][rc].setBackground(Color.BLUE);
		}

		for (int col = 0; col <= BOARD_SIZE; col++) {
			if (col != BOARD_SIZE) {
				board.add(new JLabel("          " + "abcdefgh".substring(BOARD_SIZE - col - 1, BOARD_SIZE - col)),
						JLabel.CENTER);
			} else {
				board.add(new JLabel(""), JLabel.CENTER);
			}
		}
		for (int row = 0; row < BOARD_SIZE; row++) {
			for (int col = 0; col < BOARD_SIZE; col++) {
				if (col == 0) {
					board.add(new JLabel("         " + (row + 1)));
				}
				board.add(cells[row][col]);
			}
		}

	}

	public static void main(String[] args) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				EightQueens eq = new EightQueens();
				JFrame fr = new JFrame("Eight Queens");
				fr.add(eq.gui);
				fr.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				fr.setLocationByPlatform(true);
				fr.pack();
				fr.setMinimumSize(fr.getSize());
				fr.setResizable(false);
				fr.setVisible(true);
			}

		};

		SwingUtilities.invokeLater(runnable);

	}

}
