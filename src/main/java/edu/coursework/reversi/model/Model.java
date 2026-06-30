package edu.coursework.reversi.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Optional;
import java.util.Random;

/**
 * Shared Reversi model. It contains all board state, rule validation, flags,
 * score calculation, undo state, and completion detection.
 *
 * @invariant board.length == BOARD_SIZE
 * @invariant forall r,c: 0 <= r,c < BOARD_SIZE ==> board[r][c] in {EMPTY, BLACK, WHITE}
 * @invariant currentPlayer in {BLACK, WHITE}
 */
@SuppressWarnings("deprecation")
public class Model extends Observable implements ReversiModel {
    private static final int[][] DIRECTIONS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1},  {1, 0},  {1, 1}
    };

    private final Random random;
    private Disc[][] board;
    private Disc currentPlayer;
    private Disc initialPlayer;
    private boolean gameOver;
    private boolean validationFeedbackEnabled;
    private boolean hintEnabled;
    private boolean randomStartingPlayerEnabled;
    private Snapshot undoSnapshot;

    public Model() {
        this(new Random());
    }

    Model(Random random) {
        this.random = random;
        validationFeedbackEnabled = true;
        hintEnabled = true;
        randomStartingPlayerEnabled = false;
        startFreshGame(false);
        assert invariant();
    }

    /**
     * @requires true
     * @ensures board is the standard opening board
     * @ensures currentPlayer is unchanged from the configured starting rule
     * @ensures !isGameOver() && !canUndo()
     */
    @Override
    public void reset() {
        assert invariant();
        initialiseBoard(initialPlayer);
        notifyModelChanged();
        assert invariant();
        assert !gameOver;
        assert undoSnapshot == null;
    }

    /**
     * @requires true
     * @ensures board is the standard opening board
     * @ensures currentPlayer == BLACK || currentPlayer == WHITE
     * @ensures !isGameOver() && !canUndo()
     */
    @Override
    public void newGame() {
        assert invariant();
        startFreshGame(true);
        notifyModelChanged();
        assert invariant();
        assert !gameOver;
        assert undoSnapshot == null;
    }

    /**
     * @requires true
     * @ensures result == true ==> the board contains one more placed disc and any captured discs are flipped
     * @ensures result == false ==> board and current player are unchanged
     */
    @Override
    public boolean placeDisc(int row, int column) {
        assert invariant();
        if (!isLegalMove(row, column) || gameOver) {
            assert invariant();
            return false;
        }

        undoSnapshot = new Snapshot(copyBoard(board), currentPlayer, gameOver);
        List<Move> captured = capturedDiscs(row, column, currentPlayer);
        board[row][column] = currentPlayer;
        for (Move move : captured) {
            board[move.row()][move.column()] = currentPlayer;
        }

        advanceTurnAfterMove();
        notifyModelChanged();
        assert invariant();
        assert board[row][column] != Disc.EMPTY;
        return true;
    }

    /**
     * @requires true
     * @ensures result == true ==> board equals the state before the most recent accepted move
     * @ensures result == false ==> board is unchanged
     */
    @Override
    public boolean undo() {
        assert invariant();
        if (undoSnapshot == null) {
            return false;
        }
        board = copyBoard(undoSnapshot.board);
        currentPlayer = undoSnapshot.currentPlayer;
        gameOver = undoSnapshot.gameOver;
        undoSnapshot = null;
        notifyModelChanged();
        assert invariant();
        assert undoSnapshot == null;
        return true;
    }

    /**
     * @requires true
     * @ensures result == true <==> selected square is empty and captures at least one opponent disc
     */
    @Override
    public boolean isLegalMove(int row, int column) {
        assert invariant();
        return isInside(row, column)
                && !gameOver
                && board[row][column] == Disc.EMPTY
                && !capturedDiscs(row, column, currentPlayer).isEmpty();
    }

    /**
     * @requires true
     * @ensures every returned move is legal for currentPlayer
     */
    @Override
    public List<Move> getLegalMoves() {
        assert invariant();
        return legalMovesFor(currentPlayer);
    }

    /**
     * @requires true
     * @ensures hint flag disabled ==> result is empty
     * @ensures result present ==> result is a legal move for currentPlayer
     */
    @Override
    public Optional<Move> getHint() {
        assert invariant();
        if (!hintEnabled) {
            return Optional.empty();
        }
        List<Move> legalMoves = getLegalMoves();
        return legalMoves.isEmpty() ? Optional.empty() : Optional.of(legalMoves.get(0));
    }

    @Override
    public Disc getCell(int row, int column) {
        assert invariant();
        if (!isInside(row, column)) {
            throw new IllegalArgumentException("Cell coordinates must be between 0 and 7");
        }
        return board[row][column];
    }

    @Override
    public Disc[][] getBoardCopy() {
        assert invariant();
        return copyBoard(board);
    }

    @Override
    public Disc getCurrentPlayer() {
        assert invariant();
        return currentPlayer;
    }

    @Override
    public int getScore(Disc player) {
        assert invariant();
        if (!player.isPlayer()) {
            throw new IllegalArgumentException("Score can only be requested for BLACK or WHITE");
        }
        int score = 0;
        for (Disc[] row : board) {
            for (Disc cell : row) {
                if (cell == player) {
                    score++;
                }
            }
        }
        return score;
    }

    @Override
    public boolean isGameOver() {
        assert invariant();
        return gameOver;
    }

    @Override
    public Optional<Disc> getWinner() {
        assert invariant();
        if (!gameOver) {
            return Optional.empty();
        }
        int blackScore = getScore(Disc.BLACK);
        int whiteScore = getScore(Disc.WHITE);
        if (blackScore == whiteScore) {
            return Optional.empty();
        }
        return Optional.of(blackScore > whiteScore ? Disc.BLACK : Disc.WHITE);
    }

    @Override
    public boolean isValidationFeedbackEnabled() {
        return validationFeedbackEnabled;
    }

    @Override
    public void setValidationFeedbackEnabled(boolean enabled) {
        assert invariant();
        validationFeedbackEnabled = enabled;
        notifyModelChanged();
        assert invariant();
    }

    @Override
    public boolean isHintEnabled() {
        return hintEnabled;
    }

    @Override
    public void setHintEnabled(boolean enabled) {
        assert invariant();
        hintEnabled = enabled;
        notifyModelChanged();
        assert invariant();
    }

    @Override
    public boolean isRandomStartingPlayerEnabled() {
        return randomStartingPlayerEnabled;
    }

    @Override
    public void setRandomStartingPlayerEnabled(boolean enabled) {
        assert invariant();
        randomStartingPlayerEnabled = enabled;
        notifyModelChanged();
        assert invariant();
    }

    @Override
    public boolean canUndo() {
        return undoSnapshot != null;
    }

    private void startFreshGame(boolean respectRandomFlag) {
        Disc startingPlayer = respectRandomFlag ? chooseStartingPlayer() : Disc.BLACK;
        initialiseBoard(startingPlayer);
    }

    private Disc chooseStartingPlayer() {
        if (!randomStartingPlayerEnabled) {
            return Disc.BLACK;
        }
        return random.nextBoolean() ? Disc.BLACK : Disc.WHITE;
    }

    private void initialiseBoard(Disc startingPlayer) {
        board = new Disc[BOARD_SIZE][BOARD_SIZE];
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                board[row][column] = Disc.EMPTY;
            }
        }
        board[3][3] = Disc.WHITE;
        board[3][4] = Disc.BLACK;
        board[4][3] = Disc.BLACK;
        board[4][4] = Disc.WHITE;
        initialPlayer = startingPlayer;
        currentPlayer = startingPlayer;
        gameOver = false;
        undoSnapshot = null;
    }

    private void advanceTurnAfterMove() {
        Disc opponent = currentPlayer.opponent();
        if (hasLegalMove(opponent)) {
            currentPlayer = opponent;
            gameOver = false;
        } else if (hasLegalMove(currentPlayer)) {
            gameOver = false;
        } else {
            gameOver = true;
        }
    }

    private boolean hasLegalMove(Disc player) {
        return !legalMovesFor(player).isEmpty();
    }

    private List<Move> legalMovesFor(Disc player) {
        List<Move> legalMoves = new ArrayList<>();
        if (!player.isPlayer() || gameOver) {
            return legalMoves;
        }
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                if (board[row][column] == Disc.EMPTY && !capturedDiscs(row, column, player).isEmpty()) {
                    legalMoves.add(new Move(row, column));
                }
            }
        }
        return legalMoves;
    }

    private List<Move> capturedDiscs(int row, int column, Disc player) {
        List<Move> captured = new ArrayList<>();
        if (!isInside(row, column) || board[row][column] != Disc.EMPTY || !player.isPlayer()) {
            return captured;
        }

        for (int[] direction : DIRECTIONS) {
            captured.addAll(capturedInDirection(row, column, direction[0], direction[1], player));
        }
        return captured;
    }

    private List<Move> capturedInDirection(int row, int column, int rowStep, int columnStep, Disc player) {
        List<Move> candidates = new ArrayList<>();
        int nextRow = row + rowStep;
        int nextColumn = column + columnStep;
        while (isInside(nextRow, nextColumn) && board[nextRow][nextColumn] == player.opponent()) {
            candidates.add(new Move(nextRow, nextColumn));
            nextRow += rowStep;
            nextColumn += columnStep;
        }
        if (candidates.isEmpty() || !isInside(nextRow, nextColumn) || board[nextRow][nextColumn] != player) {
            return List.of();
        }
        return candidates;
    }

    private boolean invariant() {
        if (board == null
                || board.length != BOARD_SIZE
                || currentPlayer == null
                || !currentPlayer.isPlayer()
                || initialPlayer == null
                || !initialPlayer.isPlayer()) {
            return false;
        }
        for (Disc[] row : board) {
            if (row == null || row.length != BOARD_SIZE) {
                return false;
            }
            for (Disc cell : row) {
                if (cell == null) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isInside(int row, int column) {
        return row >= 0 && row < BOARD_SIZE && column >= 0 && column < BOARD_SIZE;
    }

    private Disc[][] copyBoard(Disc[][] source) {
        Disc[][] copy = new Disc[BOARD_SIZE][BOARD_SIZE];
        for (int row = 0; row < BOARD_SIZE; row++) {
            System.arraycopy(source[row], 0, copy[row], 0, BOARD_SIZE);
        }
        return copy;
    }

    private void notifyModelChanged() {
        setChanged();
        notifyObservers();
    }

    private record Snapshot(Disc[][] board, Disc currentPlayer, boolean gameOver) {
    }
}
