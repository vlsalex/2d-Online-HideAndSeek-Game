import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class WindowManager {

    public JFrame jFrame;
    private BufferedImage icon;

    public void init() {
        try {
            icon = ImageIO.read(getClass().getResourceAsStream("icon.png"));
        } catch (IOException ignored) {
        }

        jFrame = new JFrame("Hide And Seek");
        jFrame.setIconImage(icon);

        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setSize(1980, 1080);
        jFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        jFrame.setUndecorated(true);
        jFrame.setVisible(true);
    }

}
