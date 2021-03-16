import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class Game {
    private ArrayList<Movement> command1 = new ArrayList<>(); // hide
    private ArrayList<Movement> command2 = new ArrayList<>(); // seek

    private boolean isRunning = true;

    private static final int gameTimeSeconds = 120;
    private long endTime;

    private static final double hideSpeed = 6.0;
    private static final double seekSpeed = 3.5;

    private double command1StartPositionX = 1.5;
    private double command1StartPositionY = 1.5;

    private double command2StartPositionX;
    private double command2StartPositionY;

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

    private static final int framesCountAfterSendData = 2;
    private int i = 0;

    private RayTracing rayTracing = new RayTracing(map);
    private static final double rayTracingStep = 0.05;

    public Game(ArrayList<User> players) {
        command2StartPositionX = map[0].length() - 1.5;
        command2StartPositionY = map.length - 1.5;

        distributePlayersToCommands(usersToMovement(players));
        setStartCoordinatesAndSpeed();

        endTime = System.currentTimeMillis() + gameTimeSeconds * 1000;
    }

    public void frame(double delta_time) throws JSONException {
        processAllUsers(delta_time);
        checkObstacles();
        counterCheck();
        checkGameOver();
    }

    private void sendUsersData() throws JSONException {
        long timeRemained = endTime - System.currentTimeMillis();

        for (int i = 0; i < command1.size(); i++) {
            JSONObject msg = new JSONObject();
            msg.put("status", "msg");
            msg.put("command", "setCoordinates");

            msg.put("yourDied", command1.get(i).isDied);
            msg.put("yourX", command1.get(i).x);
            msg.put("yourY", command1.get(i).y);
            msg.put("secondDied", command1.get(1 - i).isDied);
            msg.put("secondX", command1.get(1 - i).x);
            msg.put("secondY", command1.get(1 - i).y);

            msg.put("firstSeekX", command2.get(0).x);
            msg.put("firstSeekY", command2.get(0).y);
            msg.put("secondSeekX", command2.get(1).x);
            msg.put("secondSeekY", command2.get(1).y);

            msg.put("timeRemained", timeRemained);

            command1.get(i).user.sendMessage(msg.toString());
        }

        for (int i = 0; i < command2.size(); i++) {
            JSONObject msg = new JSONObject();
            msg.put("status", "msg");
            msg.put("command", "setCoordinates");

            msg.put("yourX", command2.get(i).x);
            msg.put("yourY", command2.get(i).y);
            msg.put("secondX", command2.get(1 - i).x);
            msg.put("secondY", command2.get(1 - i).y);

            msg.put("firstHideDied", command1.get(0).isDied);
            if (command1.get(0).isDied) {
                msg.put("firstHideX", command1.get(0).x);
                msg.put("firstHideY", command1.get(0).y);
            }

            msg.put("secondHideDied", command1.get(1).isDied);
            if (command1.get(1).isDied) {
                msg.put("secondHideX", command1.get(1).x);
                msg.put("secondHideY", command1.get(1).y);
            }

            msg.put("timeRemained", timeRemained);

            command2.get(i).user.sendMessage(msg.toString());
        }
    }

    private void counterCheck() {
        if (i == 0) new SendDataThread();
        i++;
        if (i == framesCountAfterSendData) i = 0;
    }

    private void checkObstacles() {
        for (int i = 0; i < command2.size(); i++) {
            for (int j = 0; j < command1.size(); j++) {
                if (!command1.get(j).isDied) {
                    boolean isObstacle = rayTracing.isObstacle(command1.get(j), command2.get(i));
                    if (!isObstacle) {
                        command1.get(j).isDied = true;
                    }

                    // if (!isObstacle) System.out.println("No obstacle");
                }
            }
        }
    }

    private void checkGameOver() {
        boolean isHideCommandDied = true;
        for (int i = 0; i < command1.size(); i++) {
            if (!command1.get(i).isDied) {
                isHideCommandDied = false;
                break;
            }
        }

        double timeRemained = (double) (endTime - System.currentTimeMillis()) / 1000;

        try {
            JSONObject msgForLose = new JSONObject();
            msgForLose.put("status", "msg");
            msgForLose.put("command", "gameOver");
            msgForLose.put("type", "lose");
            String msgForLoseStr = msgForLose.toString();

            JSONObject msgForWin = new JSONObject();
            msgForWin.put("status", "msg");
            msgForWin.put("command", "gameOver");
            msgForWin.put("type", "win");
            String msgForWinStr = msgForWin.toString();

            if (isHideCommandDied) {
                for(int i = 0; i < command1.size(); i++) {
                    command1.get(i).user.sendMessage(msgForLoseStr);
                }
                for(int i = 0; i < command2.size(); i++) {
                    command2.get(i).user.sendMessage(msgForWinStr);
                }

                isRunning = false;
                System.out.println("Game over");
            } else if(timeRemained <= 0) {
                for(int i = 0; i < command1.size(); i++) {
                    command1.get(i).user.sendMessage(msgForWinStr);
                }
                for(int i = 0; i < command2.size(); i++) {
                    command2.get(i).user.sendMessage(msgForLoseStr);
                }

                isRunning = false;
                System.out.println("Game over");
            }
        } catch (JSONException ignored) {
        }
    }

    private void processAllUsers(double delta_time) {
        for (int i = 0; i < command1.size(); i++) {
            if (!command1.get(i).isDied) processUser(command1.get(i), delta_time);
        }
        for (int i = 0; i < command2.size(); i++) processUser(command2.get(i), delta_time);
    }

    private void processUser(Movement movement, double delta_time) {
        if (movement.x_delta != 0) movement.x = userMoveX(movement, movement.x + movement.x_delta * delta_time);
        if (movement.y_delta != 0) movement.y = userMoveY(movement, movement.y + movement.y_delta * delta_time);
    }

    private double userMoveX(Movement movement, double newPositionX) {
        int direction = (movement.x < newPositionX) ? 1 : -1;
        double deltaX = Math.abs(newPositionX - movement.x) + playerSize / 2;
        double rayX = movement.x;

        while (Math.abs(rayX - movement.x) < deltaX) {
            rayX += rayTracingStep * direction;
            char upSymbol = map[(int) (movement.y - playerSize / 2)].charAt((int) rayX);
            char downSymbol = map[(int) (movement.y + playerSize / 2)].charAt((int) rayX);
            if (upSymbol == 's' || downSymbol == 's') break;
        }

        rayX -= rayTracingStep * direction;
        return rayX - playerSize / 2 * direction;
    }

    private double userMoveY(Movement movement, double newPositionY) {
        int direction = (movement.y < newPositionY) ? 1 : -1;
        double deltaY = Math.abs(newPositionY - movement.y) + playerSize / 2;
        double rayY = movement.y;

        while (Math.abs(rayY - movement.y) < deltaY) {
            rayY += rayTracingStep * direction;
            char leftSymbol = map[(int) rayY].charAt((int) (movement.x - playerSize / 2));
            char rightSymbol = map[(int) rayY].charAt((int) (movement.x + playerSize / 2));
            if (leftSymbol == 's' || rightSymbol == 's') break;
        }

        rayY -= rayTracingStep * direction;
        return rayY - playerSize / 2 * direction;
    }

    private ArrayList<Movement> usersToMovement(ArrayList<User> users) {
        ArrayList<Movement> movements = new ArrayList<>();

        for (int i = 0; i < users.size(); i++) {
            Movement currentMovement = new Movement(users.get(i), this);
            movements.add(currentMovement);
        }

        return movements;
    }

    private void distributePlayersToCommands(ArrayList<Movement> movements) {
        Random random = new Random();

        int command1_player1 = random.nextInt(4);
        int command1_player2;

        do {
            command1_player2 = random.nextInt(4);
        } while (command1_player1 == command1_player2);

        for (int i = 0; i < movements.size(); i++) {
            if (i == command1_player1 || i == command1_player2) {
                command1.add(movements.get(i));
                movements.get(i).user.command = "hide";
            } else {
                command2.add(movements.get(i));
                movements.get(i).user.command = "seek";
            }
        }
    }

    private void setStartCoordinatesAndSpeed() {
        for (int i = 0; i < command1.size(); i++) {
            command1.get(i).setCoordinate(command1StartPositionX, command1StartPositionY);
            command1.get(i).speed = hideSpeed;
        }

        for (int i = 0; i < command2.size(); i++) {
            command2.get(i).setCoordinate(command2StartPositionX, command2StartPositionY);
            command2.get(i).speed = seekSpeed;
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    class SendDataThread extends Thread {
        public SendDataThread() {
            start();
        }

        @Override
        public void run() {
            super.run();
            try {
                sendUsersData();
            } catch (JSONException ignored) {
            }
        }
    }
}
