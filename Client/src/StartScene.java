import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class StartScene {
    private WindowManager wm;
    private SceneEditor se;
    private StringPosition sp = new StringPosition();

    private int alpha;
    private long time;
    private String status;

    private int logoFontSize = 48;
    private static final String logoString = "vlasov studio";

    public StartScene(WindowManager wm, SceneEditor se) {
        this.wm = wm;
        this.se = se;

        alpha = 0;
        status = "up";
        time = System.currentTimeMillis();
    }

    public void frame(Graphics g) {
        drawBackground(g);
        drawLogo(g);
        drawAlphaRect(g);
    }

    private void drawLogo(Graphics g) {
        g.setFont(se.fd.logoFont.deriveFont(Font.PLAIN, logoFontSize));
        g.setColor(Color.WHITE);

        sp.drawStringByCenterCoordinate(g, logoString, wm.jFrame.getWidth() / 2, wm.jFrame.getHeight() / 2);
    }

    private void drawAlphaRect(Graphics g) {
        switch (status) {
            case "up":
                alpha = 255 - (int) (System.currentTimeMillis()-time)/4;

                if(alpha <= 0) {
                    alpha = 0;
                    status = "no";
                    time = System.currentTimeMillis();
                }
                break;
            case "no":
                if((System.currentTimeMillis() - time) > 2000) {
                    status = "down";
                    time = System.currentTimeMillis();
                }
                break;
            case "down":
                alpha = (int) (System.currentTimeMillis()-time)/4;

                if(alpha >= 255) {
                    alpha = 255;
                    se.setScene("menu");
                }
                break;
        }

        Color color = new Color(0, 0, 0, alpha);
        g.setColor(color);
        g.fillRect(0, 0, wm.jFrame.getWidth(), wm.jFrame.getHeight());
    }

    private void drawBackground(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, wm.jFrame.getWidth(), wm.jFrame.getHeight());
    }
}
