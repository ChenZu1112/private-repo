package edu.coursework.reversi.model;

import java.util.List;
import java.util.Optional;

public interface ReversiModel {
    int BOARD_SIZE = 8;

    void reset();

    void newGame();

    boolean placeDisc(int row, int column);

    boolean undo();

    boolean isLegalMove(int row, int column);

    List<Move> getLegalMoves();

    Optional<Move> getHint();

    Disc getCell(int row, int column);

    Disc[][] getBoardCopy();

    Disc getCurrentPlayer();

    int getScore(Disc player);

    boolean isGameOver();

    Optional<Disc> getWinner();

    boolean isValidationFeedbackEnabled();

    void setValidationFeedbackEnabled(boolean enabled);

    boolean isHintEnabled();

    void setHintEnabled(boolean enabled);

    boolean isRandomStartingPlayerEnabled();

    void setRandomStartingPlayerEnabled(boolean enabled);

    boolean canUndo();
}
