import java.awt.*;
import java.awt.event.KeyEvent;

public class SceneEditor {

    private WindowManager wm;
    public String scene = "";
    public FontsData fd;

    // scenes
    private StartScene startScene = null;
    private MenuScene menuScene = null;
    private PlayingScene playingScene = null;

    public void init(WindowManager wm) {
        this.wm = wm;
        fd = new FontsData();
    }

    public void setScene(String scene) {
        this.scene = scene;
        switch(scene) {
            case "start":
                startScene = new StartScene(wm, this);
                break;
            case "menu":
                menuScene = new MenuScene(wm, this);
                break;
            case "play":
                playingScene = new PlayingScene(wm, this);
                break;
        }
    }

    public void frame(Graphics g)  {
        switch(scene) {
            case "start":
                if(startScene != null) startScene.frame(g);
                break;
            case "menu":
                if(menuScene != null) menuScene.frame(g);
                break;
            case "play":
                if(playingScene != null) playingScene.frame(g);
                break;
        }
    }

    public void keyPressed(KeyEvent e) {
        switch(scene) {
            case "menu":
                menuScene.keyPressed(e);
                break;
            case "play":
                playingScene.keyPressed(e);
                break;
        }
    }

    public void keyReleased(KeyEvent e) {
        switch(scene) {
            case "menu":
                menuScene.keyReleased(e);
                break;
            case"play":
                playingScene.keyReleased(e);
                break;
        }
    }
}
