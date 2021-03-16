import java.awt.*;
import java.awt.event.KeyEvent;

public class MenuScene {
    private WindowManager wm;
    private SceneEditor se;
    private StringPosition sp = new StringPosition();

    // transition animation
    private String startAnimationStatus;
    private int startAnimationAlpha;
    private long startAnimationTime;

    private String endAnimationStatus;
    private int endAnimationAlpha;
    private long endAnimationTime;

    // color settings
    private static final Color pressedColor = new Color(200, 200, 200);
    private static final Color notPressedColor = new Color(255, 255, 255);

    // strings settings
    private static final int string1FontSize = 48;
    private static final String string1Text = "Press space to play";
    private Color string1Color;

    private static final int string2FontSize = 36;
    private static final String string2Text = "Esc - exit";
    private static final int string2Padding = 15; // px
    private Color string2Color;

    public MenuScene(WindowManager wm, SceneEditor se) {
        this.wm = wm;
        this.se = se;

        startAnimationStatus = "up";
        startAnimationAlpha = 255;
        startAnimationTime = System.currentTimeMillis();

        endAnimationStatus = "";
        endAnimationAlpha = 0;

        string1Color = notPressedColor;
        string2Color = notPressedColor;
    }

    public void frame(Graphics g) {
        drawBackground(g);
        drawString1(g);
        drawString2(g);
        startTransitionAnimation(g);
        endTransitionAnimation(g);
    }

    private void drawString1(Graphics g) {
        g.setColor(string1Color);
        g.setFont(se.fd.gameFont.deriveFont(Font.PLAIN, string1FontSize));
        sp.drawStringByCenterCoordinate(g, string1Text, wm.jFrame.getWidth() / 2, wm.jFrame.getHeight() / 2);
    }

    private void drawString2(Graphics g) {
        g.setColor(string2Color);
        g.setFont(se.fd.gameFont.deriveFont(Font.PLAIN, string2FontSize));
        int x = wm.jFrame.getWidth() - sp.getStringWidth(g, string2Text) - string2Padding;
        int y = sp.getStringAscent(g) + string2Padding;
        g.drawString(string2Text, x, y);
    }

    private void startTransitionAnimation(Graphics g) {
        if(startAnimationStatus.equals("up")) {
            startAnimationAlpha = 255 - (int) ((System.currentTimeMillis()-startAnimationTime) / 4);
            if(startAnimationAlpha <= 0) {
                startAnimationAlpha = 0;
                startAnimationStatus = "";
            }

            Color rectColor = new Color(0, 0, 0, startAnimationAlpha);
            g.setColor(rectColor);
            g.fillRect(0,0, wm.jFrame.getWidth(), wm.jFrame.getHeight());
        }
    }

    private void endTransitionAnimation(Graphics g) {
        if(endAnimationStatus.equals("down")) {
            endAnimationAlpha = (int) ((System.currentTimeMillis()-endAnimationTime) / 4);
            if(endAnimationAlpha >= 255) {
                endAnimationAlpha = 255;
                se.setScene("play");
            }

            Color rectColor = new Color(0, 0, 0, endAnimationAlpha);
            g.setColor(rectColor);
            g.fillRect(0,0, wm.jFrame.getWidth(), wm.jFrame.getHeight());
        }
    }

    private void drawBackground(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, wm.jFrame.getWidth(), wm.jFrame.getHeight());
    }

    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_SPACE) string1Color = pressedColor;
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE) string2Color = pressedColor;
    }

    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE) System.exit(0);
        else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
            endAnimationStatus = "down";
            endAnimationTime = System.currentTimeMillis();
        }
    }

}
