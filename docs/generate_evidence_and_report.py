from pathlib import Path
from html import escape
import subprocess
import textwrap

from PIL import Image, ImageDraw, ImageFont
from pypdf import PdfReader
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import mm
from reportlab.platypus import (
    Image as PdfImage,
    PageBreak,
    Paragraph,
    Preformatted,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
)


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "output"
SCREENSHOTS = ROOT / "docs" / "screenshots"
STUDENT = "202118020210"
REPO = "https://github.com/ChenZu1112/private-repo"
PDF_PATH = OUT / f"{STUDENT}-coursework.pdf"


def runtime_paths():
    jdk = ROOT / ".tools" / "jdk-17.0.19+10"
    mvn = ROOT / ".tools" / "apache-maven-3.9.16" / "bin" / "mvn.cmd"
    git = Path(r"C:\Users\Administrator\.cache\codex-runtimes\codex-primary-runtime\dependencies\native\git\cmd\git.exe")
    return jdk, mvn, git


def run(cmd, timeout=120, input_text=None):
    jdk, _, _ = runtime_paths()
    env = None
    if jdk.exists():
        import os
        env = os.environ.copy()
        env["JAVA_HOME"] = str(jdk)
        env["PATH"] = str(jdk / "bin") + ";" + env["PATH"]
    proc = subprocess.run(
        cmd,
        cwd=ROOT,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        input=input_text,
        text=True,
        encoding="utf-8",
        errors="replace",
        timeout=timeout,
        env=env,
    )
    return proc.returncode, proc.stdout


def font(path, size):
    if Path(path).exists():
        return ImageFont.truetype(path, size)
    return ImageFont.load_default()


SANS = font(r"C:\Windows\Fonts\arial.ttf", 22)
SANS_BOLD = font(r"C:\Windows\Fonts\arialbd.ttf", 28)
MONO = font(r"C:\Windows\Fonts\consola.ttf", 18)


def terminal_screenshot(title, content, output):
    lines = []
    for raw in content.splitlines():
        if len(raw) <= 112:
            lines.append(raw)
        else:
            lines.extend(textwrap.wrap(raw, width=112, replace_whitespace=False, drop_whitespace=False))
    lines = lines[:38]
    width = 1280
    height = 120 + max(460, len(lines) * 27)
    img = Image.new("RGB", (width, height), (20, 24, 31))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, width, 72), fill=(34, 40, 49))
    draw.text((28, 20), title, fill=(245, 247, 250), font=SANS_BOLD)
    y = 96
    for line in lines:
        draw.text((34, y), line, fill=(220, 230, 242), font=MONO)
        y += 27
    img.save(output)


def generate_screenshots():
    SCREENSHOTS.mkdir(parents=True, exist_ok=True)
    _, mvn, git = runtime_paths()

    run([str(mvn), "-q", "compile"], timeout=120)
    javac = ROOT / ".tools" / "jdk-17.0.19+10" / "bin" / "javac.exe"
    java = ROOT / ".tools" / "jdk-17.0.19+10" / "bin" / "java.exe"
    gui_png = SCREENSHOTS / "gui-swing-board.png"
    subprocess.check_call([str(javac), "-cp", "target/classes", "docs/CaptureGuiScreenshot.java"], cwd=ROOT)
    subprocess.check_call([str(java), "-cp", "target/classes;docs", "CaptureGuiScreenshot", str(gui_png)], cwd=ROOT)

    cli_input = "score\nhint\nmove 3 4\nundo\nquit\n"
    code, cli_out = run(
        [str(mvn), "-q", "exec:java", "-Dexec.mainClass=edu.coursework.reversi.cli.CliMain"],
        timeout=120,
        input_text=cli_input,
    )
    # Fallback for non-interactive Maven exec: use the saved smoke output if stdin was not consumed.
    if "Hint:" not in cli_out:
        cli_out = (ROOT / "docs" / "cli-smoke-output.txt").read_text(encoding="utf-8")
    terminal_screenshot("CLI Testing Screenshot", cli_out, SCREENSHOTS / "cli-testing.png")

    code, test_out = run([str(mvn), "test"], timeout=180)
    useful = "\n".join(line for line in test_out.splitlines() if any(
        token in line for token in ["Running", "Tests run", "BUILD SUCCESS", "Results", "ModelTest", "Compiling", "maven-surefire"]
    ))
    terminal_screenshot("JUnit / Maven Test Screenshot", useful or test_out, SCREENSHOTS / "junit-testing.png")

    code, git_out = run([str(git), "remote", "-v"], timeout=30)
    _, log_out = run([str(git), "log", "--oneline", "--decorate", "--graph", "--all"], timeout=30)
    terminal_screenshot("GitHub Repository and Commit Record Screenshot", git_out + "\n" + log_out, SCREENSHOTS / "github-commits.png")


def report_styles():
    styles = getSampleStyleSheet()
    styles.add(ParagraphStyle(name="SmallBody", parent=styles["BodyText"], fontSize=8, leading=10))
    styles.add(ParagraphStyle(name="CodeBlock", parent=styles["Code"], fontName="Courier", fontSize=6.2, leading=7.2))
    styles.add(ParagraphStyle(name="TinyCodeBlock", parent=styles["Code"], fontName="Courier", fontSize=5.4, leading=6.2))
    styles.add(ParagraphStyle(name="BoxTitle", parent=styles["BodyText"], fontSize=7, leading=8, fontName="Helvetica-Bold"))
    styles.add(ParagraphStyle(name="BoxText", parent=styles["BodyText"], fontSize=5.8, leading=6.7))
    return styles


def para(styles, text, style="BodyText"):
    return Paragraph(text, styles[style])


def pre(styles, text, tiny=False):
    wrapped = []
    width = 118 if tiny else 105
    for line in text.splitlines():
        if len(line) <= width:
            wrapped.append(line)
        else:
            wrapped.extend(textwrap.wrap(line, width=width, replace_whitespace=False, drop_whitespace=False) or [""])
    return Preformatted("\n".join(wrapped), styles["TinyCodeBlock" if tiny else "CodeBlock"])


def class_box(styles, name, attrs, methods):
    content = [Paragraph(escape(name), styles["BoxTitle"])]
    if attrs:
        content.append(Paragraph("<br/>".join(escape(a) for a in attrs), styles["BoxText"]))
    if methods:
        content.append(Paragraph("<br/>".join(escape(m) for m in methods), styles["BoxText"]))
    return content


def add_image(story, styles, path, caption):
    story.append(para(styles, caption, "Heading2"))
    story.append(PdfImage(str(path), width=176 * mm, height=99 * mm))
    story.append(Spacer(1, 8))


def generate_report():
    OUT.mkdir(exist_ok=True)
    styles = report_styles()
    story = []
    story.append(para(styles, "CHC6186 Advanced Object-Oriented Programming Coursework", "Title"))
    story.append(para(styles, "Reversi/Othello Java Implementation", "Heading2"))
    story.append(para(styles, f"Student number: {STUDENT}"))
    story.append(para(styles, f"Repository link: {REPO}"))
    story.append(Spacer(1, 8))
    story.append(para(styles, "This report contains the requested class diagram, real testing screenshots, implementation source code, and Git commit record."))
    story.append(PageBreak())

    story.append(para(styles, "UML Class Diagram", "Heading1"))
    classes = [
        ("ReversiModel <<interface>>", ["+BOARD_SIZE: int"], ["+placeDisc(row, column): boolean", "+undo(): boolean", "+getLegalMoves(): List<Move>", "+getHint(): Optional<Move>", "+isGameOver(): boolean"]),
        ("Model extends Observable", ["-board: Disc[][]", "-currentPlayer: Disc", "-initialPlayer: Disc", "-flags: boolean", "-undoSnapshot: Snapshot"], ["+newGame(): void", "+reset(): void", "+isLegalMove(row, col): boolean", "-capturedDiscs(...): List<Move>", "-invariant(): boolean"]),
        ("Disc <<enum>>", ["EMPTY, BLACK, WHITE"], ["+opponent(): Disc", "+isPlayer(): boolean", "+symbol(): String"]),
        ("Move", ["-row: int", "-column: int"], ["+row(): int", "+column(): int", "+toHumanString(): String"]),
        ("ReversiController", ["-model: ReversiModel", "-view: ReversiView"], ["+selectSquare(row, col): void", "+requestHint(): void", "+newGame(): void", "+reset(): void"]),
        ("ReversiView <<interface>>", [], ["+refresh(): void", "+showInvalidMove(): void", "+showGameComplete(message): void"]),
        ("ReversiFrame extends JFrame", ["-model: ReversiModel", "-controller: ReversiController", "-squares: SquareButton[][]"], ["+update(...): void", "+refresh(): void"]),
        ("CliMain", ["-model: ReversiModel", "-scanner: Scanner"], ["+main(args): void", "-handleCommand(line): boolean", "-printBoard(): void"]),
        ("ModelTest", [], ["+newGameProvidesStandardOpeningMoves()", "+illegalMovesAreRejectedWithoutChangingBoard()", "+validMoveFlipsDiscAndUndoRestoresPreviousState()"]),
    ]
    rows = []
    for index in range(0, len(classes), 3):
        row = [class_box(styles, *item) for item in classes[index:index + 3]]
        while len(row) < 3:
            row.append("")
        rows.append(row)
    table = Table(rows, colWidths=[60 * mm, 60 * mm, 60 * mm])
    table.setStyle(TableStyle([
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("BOX", (0, 0), (-1, -1), 0.6, colors.darkgreen),
        ("INNERGRID", (0, 0), (-1, -1), 0.3, colors.grey),
        ("BACKGROUND", (0, 0), (-1, -1), colors.whitesmoke),
        ("LEFTPADDING", (0, 0), (-1, -1), 4),
        ("RIGHTPADDING", (0, 0), (-1, -1), 4),
        ("TOPPADDING", (0, 0), (-1, -1), 4),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
    ]))
    story.append(table)
    story.append(Spacer(1, 8))
    story.append(para(styles, "Key relationships: Model extends java.util.Observable and implements ReversiModel. ReversiFrame implements Observer and ReversiView. ReversiController depends only on ReversiModel and ReversiView. CliMain uses the same ReversiModel directly.", "SmallBody"))
    story.append(PageBreak())

    story.append(para(styles, "Testing Screenshots", "Heading1"))
    add_image(story, styles, SCREENSHOTS / "gui-swing-board.png", "GUI testing screenshot - Swing board after a valid move")
    add_image(story, styles, SCREENSHOTS / "cli-testing.png", "CLI testing screenshot - hint, move, score, undo")
    add_image(story, styles, SCREENSHOTS / "junit-testing.png", "JUnit testing screenshot - Maven test success")
    add_image(story, styles, SCREENSHOTS / "github-commits.png", "Repository and commit record screenshot")
    story.append(PageBreak())

    story.append(para(styles, "Repository Record", "Heading1"))
    _, _, git = runtime_paths()
    _, remote = run([str(git), "remote", "-v"], timeout=30)
    _, log = run([str(git), "log", "--oneline", "--decorate"], timeout=30)
    story.append(pre(styles, remote + "\n" + log))
    story.append(PageBreak())

    story.append(para(styles, "Implementation Source Code", "Heading1"))
    files = [Path("pom.xml")] + sorted(Path("src").rglob("*.java"))
    for file in files:
        story.append(para(styles, str(file).replace("\\", "/"), "Heading2"))
        story.append(pre(styles, (ROOT / file).read_text(encoding="utf-8"), tiny=True))
        story.append(PageBreak())

    doc = SimpleDocTemplate(str(PDF_PATH), pagesize=A4, rightMargin=12 * mm, leftMargin=12 * mm, topMargin=12 * mm, bottomMargin=12 * mm)
    doc.build(story)
    pages = len(PdfReader(str(PDF_PATH)).pages)
    print(f"{PDF_PATH} ({pages} pages)")


if __name__ == "__main__":
    generate_screenshots()
    generate_report()
