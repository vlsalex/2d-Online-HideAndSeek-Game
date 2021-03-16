import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;

public class PlayingScene {
    private WindowManager wm;
    private SceneEditor se;
    private StringPosition sp = new StringPosition();
    private RayTracingDrawing rtd;
    private ReadMessages rm;
    private Connection connection;

    private static final String ip = "localhost";
    private static final int host = 8080;

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    private String status = "connection";
    private String playerRole = "";

    private String gameOverStatus = "";

    private static final String[] map = {
            "sssssssssss",
            "s    s  s s",
            "s    s    s",
            "s ss s ss s",
            "s    s    s",
            "s ss s ss s",
            "s s     s s",
            "sss s s sss",
            "s s s s s s",
            "s   sss   s",
            "s s s s s s",
            "sss s s sss",
            "s s     s s",
            "s ss s ss s",
            "s    s    s",
            "s ss s ss s",
            "s    s    s",
            "s    s    s",
            "sssssssssss",
    };

    private static final double playerSize = 0.5;
    private static final int dotSize = 100;

    private ArrayList<Player> hideCommand = new ArrayList<>();
    private ArrayList<Player> seekCommand = new ArrayList<>();

    private long endTime;
    private static final int gameTimeSeconds = 120;

    private Color gameOverExitStringColor;

    // color settings
    private static final Color pressedColor = new Color(200, 200, 200);
    private static final Color notPressedColor = new Color(255, 255, 255);

    // transition
    private int transition1Alpha = 255;
    private String transition1Status = "down";
    private long transition1StartTime;
    private double transition1Time = 2.0;

    private int transition2Alpha = 0;
    private String transition2Status = "";
    private long transition2StartTime;
    private double transition2Time = 2.0;

    private int transition3Alpha = 0;
    private String transition3Status = "";
    private long transition3StartTime;
    private double transition3Time = 1.0;

    private int text1Alpha = 255;
    private String text1Status = "waiting";
    private long text1StartTime;
    private double text1Time1 = 5.0;
    private double text1Time2 = 2.0;

    // interface
    private static final int interfaceStep = 30;
    private static final int interfaceSize = 20;

    // waiting
    private int roomPlayers;
    private static final int stringX = 100;
    private static final int stringY = 100;
    private static final int fontSize = 32;

    // colors
    private static final Color backgroundColor = new Color(60, 60, 60);

    private BufferedImage block1Image;
    private BufferedImage block2Image;
    private BufferedImage floor1Image;
    private BufferedImage floor2Image;
    private BufferedImage lighterFloor1Image;
    private BufferedImage lighterFloor2Image;
    private BufferedImage playerHideImage;
    private BufferedImage playerHideAlphaImage;
    private BufferedImage playerSeekImage;

    public PlayingScene(WindowManager wm, SceneEditor se) {
        this.wm = wm;
        this.se = se;

        rtd = new RayTracingDrawing(map);

        try {
            block1Image = ImageIO.read(getClass().getResourceAsStream("texture_block.png"));
            block2Image = ImageIO.read(getClass().getResourceAsStream("texture_block_up.png"));
            floor1Image = ImageIO.read(getClass().getResourceAsStream("texture_floor.png"));
            floor2Image = ImageIO.read(getClass().getResourceAsStream("texture_floor_down.png"));
            lighterFloor1Image = ImageIO.read(getClass().getResourceAsStream("texture_lighter_floor.png"));
            lighterFloor2Image = ImageIO.read(getClass().getResourceAsStream("texture_lighter_floor_down.png"));
            playerHideImage = ImageIO.read(getClass().getResourceAsStream("texture_player_hide.png"));
            playerHideAlphaImage = ImageIO.read(getClass().getResourceAsStream("texture_player_hide_alpha.png"));
            playerSeekImage = ImageIO.read(getClass().getResourceAsStream("texture_player_seek.png"));
        } catch (IOException ignored) {
        }

        connection = new Connection();
        connection.start();

        gameOverExitStringColor = notPressedColor;

        endTime = System.currentTimeMillis() + gameTimeSeconds * 1000;
    }

    public void frame(Graphics g) {
        switch (status) {
            case "connection":
            case "waiting":
                roomWaitingFrame(g);
                break;
            case "playing":
                gameDrawing(g);
                break;
            case "gameOver":
                drawGameOverScene(g);
                break;
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (status.equals("gameOver")) {
                gameOverExitStringColor = pressedColor;
            } else return;
        }

        if (!isPlayerDied()) {
            try {
                JSONObject msgToServer = new JSONObject();
                msgToServer.put("status", "syncPlayerAction");

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_D:
                        msgToServer.put("action", "right");
                        break;
                    case KeyEvent.VK_A:
                        msgToServer.put("action", "left");
                        break;
                    case KeyEvent.VK_W:
                        msgToServer.put("action", "up");
                        break;
                    case KeyEvent.VK_S:
                        msgToServer.put("action", "down");
                        break;
                }

                sendMessage(msgToServer.toString());

            } catch (JSONException ignored) {
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && status.equals("gameOver")) {
            transition3StartTime = System.currentTimeMillis();
            transition3Status = "up";
            return;
        }
        else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            status = "exit";
            se.setScene("menu");
            return;
        }

        if (!isPlayerDied()) {
            try {
                JSONObject msgToServer = new JSONObject();
                msgToServer.put("status", "syncPlayerAction");
                msgToServer.put("action", "none");

                sendMessage(msgToServer.toString());

            } catch (JSONException ignored) {
            }
        }
    }

    private void gameDrawing(Graphics g) {
        if(isPlayerDied()) playersUpdate();
        drawBackground(g);
        mapDrawing(g);
        drawRayTracing(g);
        playersDrawing(g);
        interfaceDrawing(g);
        drawText1(g);
        drawTransition1(g);
        drawTransition2(g);
    }

    private void drawGameOverScene(Graphics g) {
        drawBackground(g);

        String gameOverCenterString = "";
        if (gameOverStatus.equals("win")) gameOverCenterString = "You win!";
        else gameOverCenterString = "You lose!";

        g.setColor(Color.WHITE);
        g.setFont(se.fd.gameFont.deriveFont(Font.PLAIN, 72));
        sp.drawStringByCenterCoordinate(g, gameOverCenterString, wm.jFrame.getWidth() / 2, wm.jFrame.getHeight() / 2);

        g.setColor(gameOverExitStringColor);
        g.setFont(se.fd.gameFont.deriveFont(Font.PLAIN, 36));
        sp.drawStringByCenterCoordinate(g, "Press esc to exit", wm.jFrame.getWidth() / 2, wm.jFrame.getHeight() / 2 + 200);

        drawTransition1(g);
        drawTransition3(g);
    }

    private void drawRayTracing(Graphics g) {
        double x, y;
        x = getPlayerX();
        y = getPlayerY();

        for (int i = 0; i < seekCommand.size(); i++) {
            ArrayList<Coordinate> viewBlocks = rtd.getPlayerRays(seekCommand.get(i));
            for (int j = 0; j < viewBlocks.size(); j++) {
                double dotX = viewBlocks.get(j).x;
                double dotY = viewBlocks.get(j).y;

                double x_r = dotX - x;
                double y_r = dotY - y;
                int rectX = (int) (wm.jFrame.getWidth() / 2 + x_r * dotSize);
                int rectY = (int) (wm.jFrame.getHeight() / 2 + y_r * dotSize);

                if (dotY > 0 && map[(int) (dotY - 1)].charAt((int) dotX) == 's')
                    g.drawImage(lighterFloor2Image, rectX, rectY, dotSize, dotSize, null);
                else g.drawImage(lighterFloor1Image, rectX, rectY, dotSize, dotSize, null);
            }
        }
    }

    private void playersDrawing(Graphics g) {
        double x, y;
        x = getPlayerX();
        y = getPlayerY();
        for (int i = 0; i < hideCommand.size(); i++) {
            if (playerRole.equals("hide") || hideCommand.get(i).isDied) {

                double x_r = hideCommand.get(i).x - x;
                double y_r = hideCommand.get(i).y - y;

                int rectX = (int) (wm.jFrame.getWidth() / 2 + x_r * dotSize);
                int rectY = (int) (wm.jFrame.getHeight() / 2 + y_r * dotSize);

                BufferedImage thisPlayerImage;
                if (hideCommand.get(i).isDied) thisPlayerImage = playerHideAlphaImage;
                else thisPlayerImage = playerHideImage;

                g.drawImage(
                        thisPlayerImage,
                        (int) (rectX - playerSize * dotSize / 2),
                        (int) (rectY - playerSize * dotSize / 2),
                        (int) (playerSize * dotSize),
                        (int) (playerSize * dotSize),
                        null
                );
            }
        }

        for (int i = 0; i < seekCommand.size(); i++) {
            double x_r = seekCommand.get(i).x - x;
            double y_r = seekCommand.get(i).y - y;

            int rectX = (int) (wm.jFrame.getWidth() / 2 + x_r * dotSize);
            int rectY = (int) (wm.jFrame.getHeight() / 2 + y_r * dotSize);

            g.drawImage(
                    playerSeekImage,
                    (int) (rectX - playerSize * dotSize / 2),
                    (int) (rectY - playerSize * dotSize / 2),
                    (int) (playerSize * dotSize),
                    (int) (playerSize * dotSize),
                    null
            );
        }
    }

    private void interfaceDrawing(Graphics g) {
        int timeRemained = (int) ((endTime - System.currentTimeMillis()) / 1000);
        if (timeRemained < 0) timeRemained = 0;

        g.setColor(Color.WHITE);
        g.setFont(se.fd.pixelFont.deriveFont(Font.PLAIN, 72));
        sp.drawStringByCenterCoordinate(g, Integer.toString(timeRemained), wm.jFrame.getWidth() / 2, sp.getStringAscent(g));

        for (int i = 0; i < hideCommand.size(); i++) {
            BufferedImage thisPlayerImage;
            if (hideCommand.get(i).isDied) thisPlayerImage = playerHideAlphaImage;
            else thisPlayerImage = playerHideImage;

            g.drawImage(thisPlayerImage,
                    wm.jFrame.getWidth() - interfaceStep - interfaceSize / 2,
                    (i + 1) * interfaceStep - interfaceSize / 2,
                    interfaceSize,
                    interfaceSize,
                    null);
        }
    }

    private void mapDrawing(Graphics g) {
        double x, y;
        x = getPlayerX();
        y = getPlayerY();

        int leftX = (int) (x - (double) wm.jFrame.getWidth() / 2 / dotSize);
        int rightX = (int) (x + (double) wm.jFrame.getWidth() / 2 / dotSize);
        int topY = (int) (y - (double) wm.jFrame.getHeight() / 2 / dotSize);
        int bottomY = (int) (y + (double) wm.jFrame.getHeight() / 2 / dotSize);

        for (int dotX = leftX; dotX <= rightX; dotX++) {
            for (int dotY = topY; dotY <= bottomY; dotY++) {
                if (dotX < 0 || dotX >= map[0].length()) continue;
                if (dotY < 0 || dotY >= map.length) continue;

                double x_r = dotX - x;
                double y_r = dotY - y;
                int rectX = (int) (wm.jFrame.getWidth() / 2 + x_r * dotSize);
                int rectY = (int) (wm.jFrame.getHeight() / 2 + y_r * dotSize);

                if (map[dotY].charAt(dotX) == ' ') {
                    if (dotY > 0 && map[dotY - 1].charAt(dotX) == 's')
                        g.drawImage(floor2Image, rectX, rectY, dotSize, dotSize, null);
                    else g.drawImage(floor1Image, rectX, rectY, dotSize, dotSize, null);
                } else {
                    if (dotY == map.length - 1 || map[dotY + 1].charAt(dotX) == ' ')
                        g.drawImage(block1Image, rectX, rectY, dotSize, dotSize, null);
                    else g.drawImage(block2Image, rectX, rectY, dotSize, dotSize, null);
                }
            }
        }
    }

    private void playersUpdate() {
        for (int i = 0; i < hideCommand.size(); i++) {
            hideCommand.get(i).userUpdate();
        }

        for (int i = 0; i < seekCommand.size(); i++) {
            seekCommand.get(i).userUpdate();
        }
    }

    private double getPlayerX() {
        int playerIndex;
        if (isPlayerDied()) playerIndex = 1;
        else playerIndex = 0;

        if (playerRole.equals("hide")) {
            return hideCommand.get(playerIndex).x;
        } else {
            return seekCommand.get(playerIndex).x;
        }
    }

    private double getPlayerY() {
        int playerIndex;
        if (isPlayerDied()) playerIndex = 1;
        else playerIndex = 0;

        if (playerRole.equals("hide")) {
            return hideCommand.get(playerIndex).y;
        } else {
            return seekCommand.get(playerIndex).y;
        }
    }

    private boolean isPlayerDied() {
        if(!status.equals("playing")) return true;

        if (playerRole.equals("hide")) {
            return hideCommand.get(0).isDied;
        } else {
            return seekCommand.get(0).isDied;
        }
    }

    private void roomWaitingFrame(Graphics g) {
        drawBackground(g);
        g.setColor(Color.WHITE);
        g.setFont(se.fd.gameFont.deriveFont(Font.PLAIN, fontSize));

        switch (status) {
            case "connection":
                g.drawString("Connection...", stringX, stringY);
                break;
            case "waiting":
                g.drawString((roomPlayers + " players"), stringX, stringY);
                break;
        }
    }

    private void drawBackground(Graphics g) {
        if (status.equals("playing")) g.setColor(backgroundColor);
        else g.setColor(Color.BLACK);
        g.fillRect(0, 0, wm.jFrame.getWidth(), wm.jFrame.getHeight());
    }

    private void drawTransition2(Graphics g) {
        if(transition2Status.equals("up")) {
            transition2Alpha = (int) (((double) (System.currentTimeMillis() - transition2StartTime)) / 1000 / transition2Time * 255);
            if(transition2Alpha > 255) {
                transition2Alpha = 255;
                transition2Status = "";
                this.status = "gameOver";
                transition1StartTime = System.currentTimeMillis();
                transition1Status = "down";
            }
            g.setColor(new Color(0, 0, 0, transition2Alpha));
            g.fillRect(0, 0, wm.jFrame.getWidth(), wm.jFrame.getHeight());
        }
    }

    private void drawTransition3(Graphics g) {
        if(transition3Status.equals("up")) {
            transition3Alpha = (int) (((double) (System.currentTimeMillis() - transition3StartTime)) / 1000 / transition3Time * 255);
            if(transition3Alpha > 255) {
                transition3Alpha = 255;
                transition3Status = "";
                status = "exit";
                se.setScene("menu");
            }
            g.setColor(new Color(0, 0, 0, transition3Alpha));
            g.fillRect(0, 0, wm.jFrame.getWidth(), wm.jFrame.getHeight());
        }
    }

    private void drawTransition1(Graphics g) {
        if(transition1Status.equals("down")) {
            transition1Alpha = (int) (255 - ((double) (System.currentTimeMillis() - transition1StartTime)) / 1000 / transition1Time * 255);
            if(transition1Alpha < 0) {
                transition1Alpha = 0;
                transition1Status = "";
            }
            g.setColor(new Color(0, 0, 0, transition1Alpha));
            g.fillRect(0, 0, wm.jFrame.getWidth(), wm.jFrame.getHeight());
        }
    }

    private void drawText1(Graphics g) {
        if(status.equals("playing")) {
            if(text1Status.equals("waiting")) {
                if(System.currentTimeMillis() - text1StartTime > text1Time1 * 1000) {
                    text1StartTime = System.currentTimeMillis();
                    text1Status = "down";
                }
            }
            if(text1Status.equals("down")) {
                text1Alpha = (int) (255 - ((double) System.currentTimeMillis() - text1StartTime) / 1000 / text1Time2 * 255);
                if(text1Alpha < 0) {
                    text1Alpha = 0;
                    text1Status = "";
                }
            }

            String str;
            if(playerRole.equals("hide")) str = "Hide";
            else str = "Seek";

            g.setColor(new Color(255, 255, 255, text1Alpha));
            g.setFont(se.fd.gameFont.deriveFont(Font.PLAIN, 72));
            sp.drawStringByCenterCoordinate(g, str, wm.jFrame.getWidth() / 2, wm.jFrame.getHeight() - 130);
        }
    }

    private void updateCoordinates(JSONObject jsonMsg) throws JSONException {
        if (playerRole.equals("hide")) {
            hideCommand.get(0).setXY(jsonMsg.getDouble("yourX"), jsonMsg.getDouble("yourY"));
            hideCommand.get(0).isDied = jsonMsg.getBoolean("yourDied");

            hideCommand.get(1).setXY(jsonMsg.getDouble("secondX"), jsonMsg.getDouble("secondY"));
            hideCommand.get(1).isDied = jsonMsg.getBoolean("secondDied");

            seekCommand.get(0).setXY(jsonMsg.getDouble("firstSeekX"), jsonMsg.getDouble("firstSeekY"));

            seekCommand.get(1).setXY(jsonMsg.getDouble("secondSeekX"), jsonMsg.getDouble("secondSeekY"));
        } else {
            seekCommand.get(0).setXY(jsonMsg.getDouble("yourX"), jsonMsg.getDouble("yourY"));

            seekCommand.get(1).setXY(jsonMsg.getDouble("secondX"), jsonMsg.getDouble("secondY"));

            hideCommand.get(0).isDied = jsonMsg.getBoolean("firstHideDied");
            hideCommand.get(1).isDied = jsonMsg.getBoolean("secondHideDied");

            if (hideCommand.get(0).isDied) {
                hideCommand.get(0).setXY(jsonMsg.getDouble("firstHideX"), jsonMsg.getDouble("firstHideY"));
            }

            if (hideCommand.get(1).isDied) {
                hideCommand.get(1).setXY(jsonMsg.getDouble("secondHideX"), jsonMsg.getDouble("secondHideY"));
            }
        }

        endTime = System.currentTimeMillis() + jsonMsg.getLong("timeRemained");
    }

    private void checkJson(JSONObject jsonMsg) throws JSONException {
        String status = jsonMsg.getString("status");
        if (status.equals("msg")) {
            String command = jsonMsg.getString("command");
            switch (command) {
                case "loginIntoRoom":
                case "changeCountUserInRoom":
                    roomPlayers = jsonMsg.getInt("playersCount");
                    this.status = "waiting";
                    break;
                case "startGame":
                    hideCommand.add(new Player(1.5, 1.5, false));
                    hideCommand.add(new Player(1.5, 1.5, false));
                    seekCommand.add(new Player(1.5, 1.5, false));
                    seekCommand.add(new Player(1.5, 1.5, false));
                    playerRole = jsonMsg.getString("type");
                    this.status = "playing";
                    transition1StartTime = System.currentTimeMillis();
                    text1StartTime = System.currentTimeMillis();
                    break;
                case "setCoordinates":
                    updateCoordinates(jsonMsg);
                    break;
                case "gameOver":
                    gameOverStatus = jsonMsg.getString("type");
                    transition2StartTime = System.currentTimeMillis();
                    transition2Status = "up";
                    break;
            }
        }
    }

    public void sendMessage(String msg) {
        new SendMessageClass(msg);
    }

    class ReadMessages extends Thread {
        @Override
        public void run() {
            super.run();

            try {
                while (!status.equals("exit")) {
                    String msg = in.readLine();
                    if (msg.equals("")) continue;

                    //System.out.println("Server: " + msg);
                    JSONObject jsonMsg;
                    try {
                        jsonMsg = new JSONObject(msg);
                        checkJson(jsonMsg);
                    } catch (JSONException ignored) {
                    }
                }
                socket.close();
                in.close();
                out.close();
            } catch (IOException ignored) {
            }
        }
    }

    class SendMessageClass extends Thread {
        String msg;

        public SendMessageClass(String msg) {
            this.msg = msg;
            start();
        }

        @Override
        public void run() {
            super.run();
            try {
                out.write(msg + '\n');
                out.flush();
            } catch (IOException ignored) {
            }
        }
    }

    class Connection extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                socket = new Socket(ip, host);
            } catch (IOException e) {
                System.exit(0);
            }

            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException e) {
                System.exit(0);
            }

            rm = new ReadMessages();
            rm.start();
        }
    }

}
