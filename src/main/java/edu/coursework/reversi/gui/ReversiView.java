package edu.coursework.reversi.gui;

public interface ReversiView {
    void refresh();

    void showInvalidMove();

    void showHint(int row, int column);

    void showHintUnavailable();

    void showGameComplete(String message);
}
