import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Network {
    ServerSocket server;

    void openServerSocket(int ServerPort) throws IOException {
        this.server = new ServerSocket(ServerPort);
    }

    Socket getAcceptedSocket() throws IOException {
        return server.accept();
    }

    public ServerSocket getServer() {
        return server;
    }
}
