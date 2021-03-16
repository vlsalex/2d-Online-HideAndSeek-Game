import java.awt.*;
import java.awt.geom.Rectangle2D;

public class StringPosition {

    public void drawStringByCenterCoordinate(Graphics g, String s, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        FontMetrics fm = g2d.getFontMetrics();
        Rectangle2D r = fm.getStringBounds(s, g2d);

        int startX = x - (int) r.getWidth() / 2;
        int startY = y - (int) r.getHeight() / 2 + fm.getAscent();
        g.drawString(s, startX, startY);
    }

    public int getStringWidth(Graphics g, String s) {
        Graphics2D g2d = (Graphics2D) g;
        FontMetrics fm = g2d.getFontMetrics();
        Rectangle2D r = fm.getStringBounds(s, g2d);
        return (int) r.getWidth();
    }

    public int getStringHeight(Graphics g, String s) {
        Graphics2D g2d = (Graphics2D) g;
        FontMetrics fm = g2d.getFontMetrics();
        Rectangle2D r = fm.getStringBounds(s, g2d);
        return (int) r.getHeight();
    }

    public int getStringAscent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        FontMetrics fm = g2d.getFontMetrics();
        return fm.getAscent();
    }
}
