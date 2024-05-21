import java.io.*;
import java.net.*;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to chat server");
            final String[] fromServer = new String[1];
            String fromUser;
            new Thread(() -> {
                try {
                    while ((fromServer[0] = in.readLine()) != null) {
                        System.out.println(fromServer[0]);
                    }
                } catch (IOException e) {
                    System.out.println("Server connection lost.");
                    System.exit(1);
                }
            }).start();

            while (true) {
                fromUser = stdIn.readLine();
                if (fromUser != null) {
                    out.println(fromUser);
                    if ("quit".equalsIgnoreCase(fromUser)) {
                        break;
                    }
                }
            }

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + SERVER_ADDRESS);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    SERVER_ADDRESS);
            System.exit(1);
        }
    }
}
