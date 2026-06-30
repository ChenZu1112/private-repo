# AOOP Reversi Coursework

This project implements Reversi/Othello in Java for the CHC6186 Advanced Object-Oriented Programming coursework.

## Contents

- `src/main/java/edu/coursework/reversi/model` - shared model and game rules.
- `src/main/java/edu/coursework/reversi/gui` - Swing MVC GUI.
- `src/main/java/edu/coursework/reversi/cli` - command-line program using the same model.
- `src/test/java/edu/coursework/reversi/model` - JUnit model tests.
- `docs` - UML and report support material.

## Run

GUI:

```bash
mvn compile exec:java -Dexec.mainClass=edu.coursework.reversi.gui.GuiMain
```

CLI:

```bash
mvn compile exec:java -Dexec.mainClass=edu.coursework.reversi.cli.CliMain
```

Tests:

```bash
mvn test
```

## CLI Commands

- `move <row> <col>` or `m <row> <col>` places a disc. Rows and columns are 1 to 8.
- `undo` reverts the last board-changing move.
- `hint` displays one legal move if hints are enabled.
- `reset` restores the current game to its initial board.
- `new` starts a new game.
- `score` displays the current score.
- `board` redraws the board.
- `help` displays commands.
- `quit` exits.
