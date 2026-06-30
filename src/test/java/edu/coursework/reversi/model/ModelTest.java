package edu.coursework.reversi.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelTest {
    /*
     * Scenario 1:
     * @requires a new standard game
     * @ensures Black has exactly the four standard legal opening moves
     */
    @Test
    void newGameProvidesStandardOpeningMoves() {
        Model model = new Model();

        List<Move> moves = model.getLegalMoves();

        assertEquals(4, moves.size());
        assertTrue(moves.contains(new Move(2, 3)));
        assertTrue(moves.contains(new Move(3, 2)));
        assertTrue(moves.contains(new Move(4, 5)));
        assertTrue(moves.contains(new Move(5, 4)));
        assertEquals(2, model.getScore(Disc.BLACK));
        assertEquals(2, model.getScore(Disc.WHITE));
    }

    /*
     * Scenario 2:
     * @requires Black attempts an occupied square and a non-capturing square
     * @ensures both moves are rejected and the board score is unchanged
     */
    @Test
    void illegalMovesAreRejectedWithoutChangingBoard() {
        Model model = new Model();

        assertFalse(model.placeDisc(3, 3));
        assertFalse(model.placeDisc(0, 0));

        assertEquals(2, model.getScore(Disc.BLACK));
        assertEquals(2, model.getScore(Disc.WHITE));
        assertEquals(Disc.BLACK, model.getCurrentPlayer());
    }

    /*
     * Scenario 3:
     * @requires Black makes a valid opening move
     * @ensures one white disc is flipped, turn passes to White, and undo restores the initial state
     */
    @Test
    void validMoveFlipsDiscAndUndoRestoresPreviousState() {
        Model model = new Model();

        assertTrue(model.placeDisc(2, 3));
        assertEquals(4, model.getScore(Disc.BLACK));
        assertEquals(1, model.getScore(Disc.WHITE));
        assertEquals(Disc.WHITE, model.getCurrentPlayer());

        assertTrue(model.undo());
        assertEquals(2, model.getScore(Disc.BLACK));
        assertEquals(2, model.getScore(Disc.WHITE));
        assertEquals(Disc.BLACK, model.getCurrentPlayer());
        assertFalse(model.canUndo());
    }
}
