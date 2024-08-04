package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import messages.MessageContainer;
import messages.MessageHandler;
import utils.NetworkUtils;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private int clientId;
    private static Set<Client> clients = new HashSet<>();

    public ClientHandler(Socket socket, int clientId) {
        this.clientSocket = socket;
        this.clientId = clientId;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Enviar el ID al cliente
            String clientIdPackage = MessageHandler.packMessage(clientId, "");
            out.println(clientIdPackage);
            System.out.println("Handshake - Enviado  [" + clientIdPackage + "]");

            // Recibir el puerto UDP del cliente "UDP:<udpPort>"
            String udpPortPackage = in.readLine();
            System.out.println("Handshake - Recibido [" + udpPortPackage + "]");
            int clientUdpPort = MessageHandler.unpackPortUDP(udpPortPackage);

            synchronized (clients) {
                clients.add(new Client(clientId, clientSocket, clientUdpPort, out));
            }

            String messageFromClient;
            while ((messageFromClient = in.readLine()) != null) {
                System.out.println("Recibido [" + messageFromClient + "]");
                MessageContainer messageContainer = MessageHandler.parseMessage(messageFromClient);
                synchronized (clients) {
                    String messagePackage = MessageHandler.packMessage(messageContainer);
                    NetworkUtils.broadcastMessage(messagePackage, clients);
                    System.out.println("Enviado  [" + messagePackage + "]\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("El cliente [" + clientId + "] se fue");
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            synchronized (clients) {
                clients.removeIf(c -> c.getId() == clientId);
            }
        }
    }

    public static class Client {
        private int id;
        private InetAddress clientAddress;
        private int clientTcpPort;
        private int clientUdpPort;
        private PrintWriter writer;

        public Client(int id, Socket clientSocket, int clientUdpPort,
                PrintWriter writer) {
            this.id = id;
            this.clientAddress = clientSocket.getInetAddress();
            this.clientTcpPort = clientSocket.getPort();
            this.clientUdpPort = clientUdpPort;
            this.writer = writer;
        }

        public int getId() {
            return id;
        }

        public InetAddress getClientAddress() {
            return clientAddress;
        }

        public int getClientTcpPort() {
            return clientTcpPort;
        }

        public int getClientUdpPort() {
            return clientUdpPort;
        }

        public PrintWriter getWriter() {
            return writer;
        }

        @Override
        public String toString() {
            return "CID:" + id + "|IP:" + clientAddress + "|TCP:" + clientTcpPort + "|UDP:" + clientUdpPort;
        }
    }
}
