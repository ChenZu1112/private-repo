# Video Presentation Outline

Keep the recording under five minutes.

1. Show the project structure and explain that GUI and CLI share `Model`.
2. Open `Model.java` and point out rule validation, flags, undo, hint, game completion, JML-style specifications, and assertions.
3. Run `mvn test` and show the three JUnit model scenarios passing.
4. Run the CLI with `mvn compile exec:java -Dexec.mainClass=edu.coursework.reversi.cli.CliMain`.
5. Demonstrate CLI commands: `hint`, a legal `move`, an illegal `move`, `score`, `undo`, `reset`, and `new`.
6. Run the GUI with `mvn compile exec:java -Dexec.mainClass=edu.coursework.reversi.gui.GuiMain`.
7. Demonstrate legal move highlighting, placing discs, score/current player updates, Hint, Undo, Reset, and the New Game settings dialog.
8. End by showing the UML class diagram and the report PDF.
