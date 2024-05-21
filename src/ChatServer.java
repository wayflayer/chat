import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private static final ExecutorService pool = Executors.newFixedThreadPool(6);

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);

        System.out.println("Chat Server is running on port " + PORT);

        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientThread = new ClientHandler(clientSocket, clients);
                clients.add(clientThread);
                pool.execute(clientThread);
            }
        } finally {
            serverSocket.close();
            pool.shutdown();
        }
    }
}

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final List<ClientHandler> clients;
    private PrintWriter out;
    private BufferedReader in;
    private String name;

    public ClientHandler(Socket socket, List<ClientHandler> clients) {
        this.clientSocket = socket;
        this.clients = clients;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            for (;;) {
                out.println("Enter your name: ");
                name = in.readLine();
                if (name != null && !name.isEmpty()) {
                    break;
                }
            }

            System.out.println(name + " joined the chat.");
            broadcastMessage(name + " joined the chat.");

            String message;
            while ((message = in.readLine()) != null) {
                if ("quit".equalsIgnoreCase(message)) {
                    break;
                }
                broadcastMessage(name + ": " + message);
            }
        } catch (IOException e) {
            System.err.println("IO exception in client handler: " + e.getMessage());
        } finally {
            closeConnections();
            System.out.println(name + " left the chat.");
            broadcastMessage(name + " left the chat.");
            clients.remove(this);
        }
    }

    private void broadcastMessage(String message) {
        for (ClientHandler aClient : clients) {
            aClient.out.println(message);
        }
    }

    private void closeConnections() {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}