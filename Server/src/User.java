import java.io.*;
import java.net.*;

public class User {
    public Socket socket;
    public BufferedReader in;
    public BufferedWriter out;

    private Room userRoom = null;

    public String command = "";

    public User(Socket socket) throws IOException {
        this.socket = socket;

        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void sendMessage(String msg) {
        try {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException ignored) {
        }
    }

    public boolean checkUserConnect() {
        try {
            out.write("\n");
            out.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public Room getUserRoom() {
        return userRoom;
    }

    public void setUserRoom(Room userRoom) {
        this.userRoom = userRoom;
    }
}
