package edu.coursework.reversi.model;

import java.util.Objects;

/**
 * Immutable row and column pair. Public coordinates are zero-based.
 */
public final class Move {
    private final int row;
    private final int column;

    public Move(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int row() {
        return row;
    }

    public int column() {
        return column;
    }

    public String toHumanString() {
        return "(" + (row + 1) + ", " + (column + 1) + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Move move)) {
            return false;
        }
        return row == move.row && column == move.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    @Override
    public String toString() {
        return "Move{row=" + row + ", column=" + column + '}';
    }
}
