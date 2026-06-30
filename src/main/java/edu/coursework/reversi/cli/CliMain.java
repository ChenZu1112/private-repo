package edu.coursework.reversi.cli;

import edu.coursework.reversi.model.Disc;
import edu.coursework.reversi.model.Model;
import edu.coursework.reversi.model.Move;
import edu.coursework.reversi.model.ReversiModel;

import java.util.Optional;
import java.util.Scanner;

public final class CliMain {
    private final ReversiModel model;
    private final Scanner scanner;

    private CliMain(ReversiModel model, Scanner scanner) {
        this.model = model;
        this.scanner = scanner;
    }

    public static void main(String[] args) {
        new CliMain(new Model(), new Scanner(System.in)).run();
    }

    private void run() {
        System.out.println("Reversi CLI");
        printHelp();
        printBoard();
        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            if (!handleCommand(line)) {
                break;
            }
            if (model.isGameOver()) {
                printGameComplete();
            }
        }
    }

    private boolean handleCommand(String line) {
        String[] parts = line.split("\\s+");
        String command = parts[0].toLowerCase();
        switch (command) {
            case "move", "m" -> placeMove(parts);
            case "undo" -> undo();
            case "hint" -> hint();
            case "reset" -> {
                model.reset();
                printBoard();
            }
            case "new" -> {
                model.newGame();
                printBoard();
            }
            case "score" -> printScore();
            case "board" -> printBoard();
            case "help" -> printHelp();
            case "quit", "exit" -> {
                return false;
            }
            default -> System.out.println("Unknown command. Type help for commands.");
        }
        return true;
    }

    private void placeMove(String[] parts) {
        if (parts.length != 3) {
            System.out.println("Use: move <row> <col>");
            return;
        }
        try {
            int row = Integer.parseInt(parts[1]) - 1;
            int column = Integer.parseInt(parts[2]) - 1;
            if (model.placeDisc(row, column)) {
                printBoard();
            } else if (model.isValidationFeedbackEnabled()) {
                System.out.println("Illegal move rejected.");
            }
        } catch (NumberFormatException exception) {
            System.out.println("Rows and columns must be numbers from 1 to 8.");
        }
    }

    private void undo() {
        if (model.undo()) {
            printBoard();
        } else if (model.isValidationFeedbackEnabled()) {
            System.out.println("Nothing to undo.");
        }
    }

    private void hint() {
        Optional<Move> hint = model.getHint();
        if (hint.isPresent()) {
            System.out.println("Hint: play " + hint.get().toHumanString());
        } else {
            System.out.println("No hint is available.");
        }
    }

    private void printBoard() {
        System.out.println();
        System.out.println("    1 2 3 4 5 6 7 8");
        for (int row = 0; row < ReversiModel.BOARD_SIZE; row++) {
            System.out.print(" " + (row + 1) + "  ");
            for (int column = 0; column < ReversiModel.BOARD_SIZE; column++) {
                System.out.print(model.getCell(row, column).symbol() + " ");
            }
            System.out.println();
        }
        System.out.println("Turn: " + playerName(model.getCurrentPlayer()));
        printScore();
    }

    private void printScore() {
        System.out.println("Score: Black " + model.getScore(Disc.BLACK)
                + " - White " + model.getScore(Disc.WHITE));
    }

    private void printGameComplete() {
        String winner = model.getWinner().map(this::playerName).orElse("Draw");
        System.out.println("Game complete. Winner: " + winner);
    }

    private void printHelp() {
        System.out.println("Commands: move <row> <col>, undo, hint, reset, new, score, board, help, quit");
    }

    private String playerName(Disc disc) {
        return disc == Disc.BLACK ? "Black" : "White";
    }
}
