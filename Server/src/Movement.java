import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Movement {

    public double speed = 0;

    public double x, y;

    public double x_delta = 0;
    public double y_delta = 0;

    public boolean isDied = false;

    private Game game;
    public User user;

    public Movement(User user, Game game) {
        this.user = user;
        this.game = game;
        startListen();
    }

    public void setCoordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setAction(String action) {
        switch (action) {
            case "right":
                x_delta = speed;
                y_delta = 0;
                break;
            case "left":
                x_delta = -speed;
                y_delta = 0;
                break;
            case "down":
                x_delta = 0;
                y_delta = speed;
                break;
            case "up":
                x_delta = 0;
                y_delta = -speed;
                break;
            default:
                x_delta = 0;
                y_delta = 0;
                break;
        }
    }

    public void startListen() {
        new Listening();
    }

    private class Listening extends Thread {
        public Listening() {
            start();
        }

        @Override
        public void run() {
            super.run();

            while(game.isRunning()) {
                try {
                    String msgString = user.in.readLine();
                    JSONObject msg = new JSONObject(msgString);

                    if(msg.getString("status").equals("syncPlayerAction")) {
                        setAction(msg.getString("action"));
                    }
                } catch (IOException | JSONException | NullPointerException ignored) {
                }
            }
        }
    }
}
