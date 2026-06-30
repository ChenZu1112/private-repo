package edu.coursework.reversi.gui;

import edu.coursework.reversi.model.Disc;
import edu.coursework.reversi.model.Move;
import edu.coursework.reversi.model.ReversiModel;

import java.util.Optional;

public class ReversiController {
    private final ReversiModel model;
    private final ReversiView view;

    public ReversiController(ReversiModel model, ReversiView view) {
        this.model = model;
        this.view = view;
    }

    public void selectSquare(int row, int column) {
        if (model.isLegalMove(row, column)) {
            model.placeDisc(row, column);
            announceCompletionIfNeeded();
        } else if (model.isValidationFeedbackEnabled()) {
            view.showInvalidMove();
        }
    }

    public void undo() {
        if (!model.undo() && model.isValidationFeedbackEnabled()) {
            view.showInvalidMove();
        }
    }

    public void reset() {
        model.reset();
    }

    public void newGame() {
        model.newGame();
    }

    public void requestHint() {
        Optional<Move> hint = model.getHint();
        if (hint.isPresent()) {
            Move move = hint.get();
            view.showHint(move.row(), move.column());
        } else {
            view.showHintUnavailable();
        }
    }

    public void setValidationFeedbackEnabled(boolean enabled) {
        model.setValidationFeedbackEnabled(enabled);
    }

    public void setHintEnabled(boolean enabled) {
        model.setHintEnabled(enabled);
    }

    public void setRandomStartingPlayerEnabled(boolean enabled) {
        model.setRandomStartingPlayerEnabled(enabled);
    }

    private void announceCompletionIfNeeded() {
        if (!model.isGameOver()) {
            return;
        }
        String message = model.getWinner()
                .map(winner -> winnerName(winner) + " wins!")
                .orElse("The game is a draw.");
        view.showGameComplete("Game complete. " + message);
    }

    private String winnerName(Disc winner) {
        return winner == Disc.BLACK ? "Black" : "White";
    }
}
