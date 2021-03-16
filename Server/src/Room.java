import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Room {
    private ArrayList<User> roomUsers = new ArrayList<>();
    private ArrayList<Room> allRooms;
    private Room thisRoom = this;

    private boolean isRun = false;

    private Game game = null;

    public Room(ArrayList<Room> allRooms) {
        this.allRooms = allRooms;
        new CheckIsUsersExitedClass();
    }

    public void addUser(User newUser, int countRoomUsers) throws JSONException {
        roomUsers.add(newUser);
        newUser.setUserRoom(this);
        noticeNewUser(newUser);
        noticeUsersAboutChangeUsers(1);

        if (roomUsers.size() == countRoomUsers) {
            runGame();
        }
    }

    public int getCountOfUsersInRoom() {
        return roomUsers.size();
    }

    private void noticeNewUser(User user) throws JSONException {
        JSONObject msg = new JSONObject();
        msg.put("status", "msg");
        msg.put("command", "loginIntoRoom");
        msg.put("playersCount", roomUsers.size());

        user.sendMessage(msg.toString() + "\n");
    }

    private void noticeUsers(String msg, int beginOffset, int endOffset) {
        for (int i = beginOffset; i < roomUsers.size() - endOffset; i++) {
            roomUsers.get(i).sendMessage(msg + "\n");
        }
    }

    private void noticeUsersAboutChangeUsers(int endOffset) throws JSONException {
        JSONObject msg = new JSONObject();
        msg.put("status", "msg");
        msg.put("command", "changeCountUserInRoom");
        msg.put("playersCount", roomUsers.size());

        noticeUsers(msg.toString(), 0, endOffset);
    }

    private void noticeAllUsersAboutStartGame() throws JSONException {
        for (int i = 0; i < roomUsers.size(); i++) {
            JSONObject msg = new JSONObject();
            msg.put("status", "msg");
            msg.put("command", "startGame");
            msg.put("type", roomUsers.get(i).command);
            roomUsers.get(i).sendMessage(msg + "\n");
        }
    }

    private void runGame() throws JSONException {
        isRun = true;

        this.game = new Game(roomUsers);

        noticeAllUsersAboutStartGame();
        new PlayingGame();
    }

    private class PlayingGame extends Thread {
        public PlayingGame() {
            start();
        }

        @Override
        public void run() {
            super.run();

            long last_time = System.currentTimeMillis();
            while (game.isRunning()) {
                long time = System.currentTimeMillis();
                double delta_time = ((double) (time - last_time)) / 1000;
                last_time = time;
                try {
                    game.frame(delta_time);
                } catch (JSONException ignored) {
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
            }
            allRooms.remove(this);
        }
    }

    private class CheckIsUsersExitedClass extends Thread {

        public CheckIsUsersExitedClass() {
            start();
        }

        @Override
        public void run() {
            super.run();

            while (!isRun) {
                for (int i = 0; i < roomUsers.size(); i++) {
                    User currentUser = roomUsers.get(i);
                    if (!currentUser.checkUserConnect()) {
                        roomUsers.remove(currentUser);
                        System.out.println("User disconnected");
                        if (roomUsers.size() > 0) {
                            try {
                                noticeUsersAboutChangeUsers(0);
                            } catch (JSONException e) {
                            }
                        } else {
                            allRooms.remove(thisRoom);
                        }
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
            }
        }
    }

}
