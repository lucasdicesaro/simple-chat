
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import server.ClientHandler;

public class Server {
    private static final int TCP_PORT = 12345;

    private static final AtomicInteger clientIdCounter = new AtomicInteger(1);

    public static void main(String[] args) {
        startServer();
    }

    private static void startServer() {
        System.out.println("Servidor iniciado...");
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                int clientId = clientIdCounter.getAndIncrement();
                System.out.println("Cliente conectado (ID: " + clientId + "): "
                        + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                Runnable clientHandler = new ClientHandler(clientSocket, clientId);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
