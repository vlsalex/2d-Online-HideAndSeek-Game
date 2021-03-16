import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Game extends JPanel implements ActionListener {

    private Timer timer = new Timer(0, this);
    private WindowManager wm = new WindowManager();

    private SceneEditor sceneEditor = new SceneEditor();

    public Game() {
        initGame();
    }

    private void initGame() {
        setFocusable(true);
        addKeyListener(new GameKeyListener());

        wm.init();
        wm.jFrame.add(this);
        sceneEditor.init(wm);
        sceneEditor.setScene("start");

        timer.start();

        addFocusListener(new FocusListener()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e)
            {
                repaint();
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        sceneEditor.frame(g);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    class GameKeyListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            //System.out.println("Pressed: "+e.getKeyChar());
            sceneEditor.keyPressed(e);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            //System.out.println("Released: "+e.getKeyChar());
            sceneEditor.keyReleased(e);
        }
    }
}
