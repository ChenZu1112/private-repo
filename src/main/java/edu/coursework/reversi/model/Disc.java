package edu.coursework.reversi.model;

/**
 * Values that can appear on the board.
 */
public enum Disc {
    EMPTY("."),
    BLACK("B"),
    WHITE("W");

    private final String symbol;

    Disc(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    public Disc opponent() {
        assert this == BLACK || this == WHITE : "Only a player disc has an opponent";
        return this == BLACK ? WHITE : BLACK;
    }

    public boolean isPlayer() {
        return this == BLACK || this == WHITE;
    }
}
