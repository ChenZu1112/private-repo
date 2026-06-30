package edu.coursework.reversi.gui;

import edu.coursework.reversi.model.Disc;
import edu.coursework.reversi.model.Move;
import edu.coursework.reversi.model.ReversiModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

@SuppressWarnings("deprecation")
public class ReversiFrame extends JFrame implements Observer, ReversiView {
    private static final Color BOARD_GREEN = new Color(20, 126, 82);
    private static final Color LEGAL_MOVE_GREEN = new Color(146, 224, 144);
    private static final Color HINT_YELLOW = new Color(247, 210, 86);
    private static final Font STATUS_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 16);

    private final ReversiModel model;
    private final ReversiController controller;
    private final SquareButton[][] squares;
    private final JLabel currentPlayerLabel;
    private final JLabel scoreLabel;
    private final JCheckBox validationCheckBox;
    private final JCheckBox hintCheckBox;
    private final JCheckBox randomStartCheckBox;
    private Move highlightedHint;

    public ReversiFrame(ReversiModel model) {
        super("Reversi");
        this.model = model;
        this.controller = new ReversiController(model, this);
        this.squares = new SquareButton[ReversiModel.BOARD_SIZE][ReversiModel.BOARD_SIZE];
        this.currentPlayerLabel = new JLabel();
        this.scoreLabel = new JLabel();
        this.validationCheckBox = new JCheckBox("Show legal moves");
        this.hintCheckBox = new JCheckBox("Enable hints");
        this.randomStartCheckBox = new JCheckBox("Random starting player");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        add(createStatusPanel(), BorderLayout.NORTH);
        add(createBoardPanel(), BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.SOUTH);
        refresh();
        pack();
        setMinimumSize(new Dimension(640, 720));
        setLocationRelativeTo(null);
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        currentPlayerLabel.setFont(STATUS_FONT);
        scoreLabel.setFont(STATUS_FONT);
        scoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(currentPlayerLabel);
        panel.add(scoreLabel);
        return panel;
    }

    private JPanel createBoardPanel() {
        JPanel boardPanel = new JPanel(new GridLayout(ReversiModel.BOARD_SIZE, ReversiModel.BOARD_SIZE));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        for (int row = 0; row < ReversiModel.BOARD_SIZE; row++) {
            for (int column = 0; column < ReversiModel.BOARD_SIZE; column++) {
                SquareButton square = new SquareButton(row, column);
                square.addActionListener(event -> controller.selectSquare(square.row, square.column));
                squares[row][column] = square;
                boardPanel.add(square);
            }
        }
        return boardPanel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));

        JPanel buttons = new JPanel(new GridLayout(1, 4, 8, 0));
        JButton undoButton = new JButton("Undo");
        JButton hintButton = new JButton("Hint");
        JButton resetButton = new JButton("Reset");
        JButton newGameButton = new JButton("New Game");
        undoButton.addActionListener(event -> controller.undo());
        hintButton.addActionListener(event -> controller.requestHint());
        resetButton.addActionListener(event -> controller.reset());
        newGameButton.addActionListener(event -> showSettingsDialog());
        buttons.add(undoButton);
        buttons.add(hintButton);
        buttons.add(resetButton);
        buttons.add(newGameButton);

        JPanel settings = new JPanel(new GridLayout(1, 3, 8, 0));
        validationCheckBox.addActionListener(event ->
                controller.setValidationFeedbackEnabled(validationCheckBox.isSelected()));
        hintCheckBox.addActionListener(event ->
                controller.setHintEnabled(hintCheckBox.isSelected()));
        randomStartCheckBox.addActionListener(event ->
                controller.setRandomStartingPlayerEnabled(randomStartCheckBox.isSelected()));
        settings.add(validationCheckBox);
        settings.add(hintCheckBox);
        settings.add(randomStartCheckBox);

        panel.add(buttons);
        panel.add(settings);
        return panel;
    }

    private void showSettingsDialog() {
        JCheckBox validation = new JCheckBox("Show legal moves", model.isValidationFeedbackEnabled());
        JCheckBox hints = new JCheckBox("Enable hints", model.isHintEnabled());
        JCheckBox randomStart = new JCheckBox("Random starting player", model.isRandomStartingPlayerEnabled());
        JPanel settingsPanel = new JPanel(new GridLayout(3, 1));
        settingsPanel.add(validation);
        settingsPanel.add(hints);
        settingsPanel.add(randomStart);
        int result = JOptionPane.showConfirmDialog(
                this,
                settingsPanel,
                "New Game Settings",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            controller.setValidationFeedbackEnabled(validation.isSelected());
            controller.setHintEnabled(hints.isSelected());
            controller.setRandomStartingPlayerEnabled(randomStart.isSelected());
            controller.newGame();
        }
    }

    @Override
    public void update(Observable observable, Object argument) {
        refresh();
    }

    @Override
    public void refresh() {
        Set<Move> actualLegalMoves = new HashSet<>(model.getLegalMoves());
        Set<Move> visibleLegalMoves = model.isValidationFeedbackEnabled()
                ? new HashSet<>(model.getLegalMoves())
                : Set.of();
        if (highlightedHint != null && !actualLegalMoves.contains(highlightedHint)) {
            highlightedHint = null;
        }
        for (int row = 0; row < ReversiModel.BOARD_SIZE; row++) {
            for (int column = 0; column < ReversiModel.BOARD_SIZE; column++) {
                Move move = new Move(row, column);
                squares[row][column].setDisc(model.getCell(row, column));
                squares[row][column].setLegalMove(visibleLegalMoves.contains(move));
                squares[row][column].setHint(highlightedHint != null && highlightedHint.equals(move));
            }
        }
        currentPlayerLabel.setText("Turn: " + playerName(model.getCurrentPlayer()));
        scoreLabel.setText("Black " + model.getScore(Disc.BLACK) + " : " + model.getScore(Disc.WHITE) + " White");
        validationCheckBox.setSelected(model.isValidationFeedbackEnabled());
        hintCheckBox.setSelected(model.isHintEnabled());
        randomStartCheckBox.setSelected(model.isRandomStartingPlayerEnabled());
    }

    @Override
    public void showInvalidMove() {
        JOptionPane.showMessageDialog(this, "That move is not legal.", "Invalid Move", JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public void showHint(int row, int column) {
        highlightedHint = new Move(row, column);
        refresh();
    }

    @Override
    public void showHintUnavailable() {
        JOptionPane.showMessageDialog(this, "No hint is available.", "Hint", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void showGameComplete(String message) {
        JOptionPane.showMessageDialog(this, message, "Game Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    private String playerName(Disc disc) {
        return disc == Disc.BLACK ? "Black" : "White";
    }

    private static class SquareButton extends JButton {
        private final int row;
        private final int column;
        private Disc disc;
        private boolean legalMove;
        private boolean hint;

        SquareButton(int row, int column) {
            this.row = row;
            this.column = column;
            this.disc = Disc.EMPTY;
            setPreferredSize(new Dimension(70, 70));
            setBorder(BorderFactory.createLineBorder(new Color(8, 69, 45)));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(true);
        }

        void setDisc(Disc disc) {
            this.disc = disc;
            repaint();
        }

        void setLegalMove(boolean legalMove) {
            this.legalMove = legalMove;
            repaint();
        }

        void setHint(boolean hint) {
            this.hint = hint;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(hint ? HINT_YELLOW : legalMove ? LEGAL_MOVE_GREEN : BOARD_GREEN);
            g.fillRect(0, 0, getWidth(), getHeight());

            if (disc != Disc.EMPTY) {
                int padding = Math.max(8, getWidth() / 8);
                g.setColor(disc == Disc.BLACK ? Color.BLACK : Color.WHITE);
                g.fillOval(padding, padding, getWidth() - 2 * padding, getHeight() - 2 * padding);
                g.setColor(Color.DARK_GRAY);
                g.drawOval(padding, padding, getWidth() - 2 * padding, getHeight() - 2 * padding);
            } else if (legalMove || hint) {
                int size = Math.max(10, getWidth() / 6);
                g.setColor(new Color(28, 83, 55));
                g.fillOval((getWidth() - size) / 2, (getHeight() - size) / 2, size, size);
            }
            g.dispose();
        }
    }
}
