import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // Get username from client
            username = in.readLine();
            System.out.println(username + " joined the chat");
            ChatServer.broadcastMessage("SERVER: " + username + " joined the chat", this);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(username + ": " + message);
                ChatServer.broadcastMessage(username + ": " + message, this);
            }
        } catch (IOException e) {
            System.out.println(username + " disconnected");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ChatServer.removeClient(this);
            ChatServer.broadcastMessage("SERVER: " + username + " left the chat", this);
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}
