package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import messages.IncomeMessage;
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
            System.out.println("Enviado ClientId package [" + clientIdPackage + "]");

            // Recibir el puerto UDP del cliente "UDP:<udpPort>"
            String udpPortPackage = in.readLine();
            System.out.println("Recibido UDP port package [" + udpPortPackage + "]");
            int clientUdpPort = MessageHandler.unpackPortUDP(udpPortPackage);

            InetAddress clientAddress = clientSocket.getInetAddress();
            int clientTcpPort = clientSocket.getPort();

            System.out.println(
                    "Nuevo CID:" + clientId + "|IP:" + clientAddress + "|TCP Port:" + clientTcpPort + "|UDP Port:"
                            + clientUdpPort);

            synchronized (clients) {
                clients.add(new Client(clientId, clientAddress, clientTcpPort, clientUdpPort, out));
            }

            String payloadFromClient;
            while ((payloadFromClient = in.readLine()) != null) {
                System.out.println(
                        "Recibido CID:" + clientId + "|IP:" + clientAddress + "|TCP Port:" + clientTcpPort
                                + "|UDP Port:" + clientUdpPort + "|payload:[" + payloadFromClient + "]");
                IncomeMessage message = MessageHandler.parseMessage(payloadFromClient);
                synchronized (clients) {
                    System.out.println("Enviando [" + message.getPayload().getContent() + "]");
                    String packMessage = MessageHandler.packMessage(Integer.parseInt(message.getClientId()),
                            message.getPayload().getContent());
                    NetworkUtils.broadcastMessage(packMessage, clients);
                    System.out.println("");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
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

        public Client(int id, InetAddress clientAddress, int clientTcpPort, int clientUdpPort,
                PrintWriter writer) {
            this.id = id;
            this.clientAddress = clientAddress;
            this.clientTcpPort = clientTcpPort;
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
    }
}
