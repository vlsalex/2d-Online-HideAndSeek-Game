import org.json.JSONException;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Main {
    static final int countRoomUsers = 4;

    static ArrayList<Room> rooms = new ArrayList<>();

    static public void main(String[] args) throws IOException, JSONException {

        Network net = new Network();

        net.openServerSocket(8080);
        System.out.println("Server started");

        try {
            while (true) {
                Socket socket = net.getAcceptedSocket();
                System.out.println("User connected");

                User user = new User(socket);
                addUserIntoRoom(user);
            }
        } finally {
            net.getServer().close();
        }
    }

    static void addUserIntoRoom(User user) throws JSONException {
        Room lastRoom = null;
        if (rooms.size() > 0) {
            lastRoom = rooms.get(rooms.size() - 1);
        }

        if (lastRoom != null && lastRoom.getCountOfUsersInRoom() < countRoomUsers) {
            lastRoom.addUser(user, countRoomUsers);
        }
        else {
            createRoom(user);
        }
    }

    static void createRoom(User user) throws JSONException {
        Room newRoom = new Room(rooms);
        rooms.add(newRoom);
        newRoom.addUser(user, countRoomUsers);
    }

}
