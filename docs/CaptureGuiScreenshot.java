import edu.coursework.reversi.gui.ReversiFrame;
import edu.coursework.reversi.model.Model;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;

public class CaptureGuiScreenshot {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Output PNG path required");
        }
        SwingUtilities.invokeAndWait(() -> {
            try {
                Model model = new Model();
                ReversiFrame frame = new ReversiFrame(model);
                model.addObserver(frame);
                model.placeDisc(2, 3);
                frame.setSize(760, 760);
                frame.setLocation(80, 80);
                frame.setVisible(true);
                frame.doLayout();
                frame.refresh();
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });
        Thread.sleep(800);
        BufferedImage image = new Robot().createScreenCapture(new Rectangle(80, 80, 760, 760));
        ImageIO.write(image, "png", new File(args[0]));
        SwingUtilities.invokeAndWait(() -> java.awt.Window.getWindows()[0].dispose());
    }
}
