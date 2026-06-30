package edu.coursework.reversi.gui;

import edu.coursework.reversi.model.Model;

import javax.swing.SwingUtilities;

public final class GuiMain {
    private GuiMain() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Model model = new Model();
            ReversiFrame frame = new ReversiFrame(model);
            model.addObserver(frame);
            frame.setVisible(true);
        });
    }
}
