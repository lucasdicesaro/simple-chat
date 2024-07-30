package server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private int clientId;
    private static Set<Client> clients = new HashSet<>();

    private static final String MESSAGE_PATTERN = "CID:<clientId>|MSG:<message>";

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
            out.println("CID:" + clientId);
            InetAddress clientAddress = clientSocket.getInetAddress();
            int clientPort = clientSocket.getPort();

            // Recibir el puerto UDP del cliente "UDP:<udpPort>"
            String udpPortPackage = in.readLine();
            String value = udpPortPackage.split(":")[1];
            int clientUdpPort = Integer.parseInt(value); 
            System.out.println("Puerto UDP del CID:" + clientId + ": " + clientUdpPort);

            synchronized (clients) {
                clients.add(new Client(clientId, clientAddress, clientPort, clientUdpPort, out));
            }

            String mensajeDelCliente;
            while ((mensajeDelCliente = in.readLine()) != null) {
                System.out.println("Mensaje recibido del cliente: " + clientId + ": "+ mensajeDelCliente);
                String paquete = MESSAGE_PATTERN.replaceFirst("<clientId>", String.format("%05d", clientId)).replaceFirst("<message>", mensajeDelCliente);
                synchronized (clients) {
                    ClientHandler.broadcastMessage(paquete, clientAddress, clientPort);
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

    private static void broadcastMessage(String message, InetAddress senderAddress, int senderPort) {
        try (DatagramSocket udpSocket = new DatagramSocket()) {
            udpSocket.setBroadcast(true);
            byte[] buffer = message.getBytes();
            
            System.out.println("Propagando el mensaje '" + message + "' de " + senderAddress + ":" + senderPort);
            for (Client client : clients) {
                // Evita propagar el mensaje al autor, pero quizas sirva que lo reciba para sincronizar cosas.
                //if (client.getClientPort() != senderPort) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, client.getClientAddress(), client.getClientUdpPort());
                    udpSocket.send(packet);
                //}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Client {
        private int id;
        private InetAddress clientAddress;
        private int clientPort;
        private int clientUdpPort;
        private PrintWriter writer;

        public Client(int id, InetAddress clientAddress, int clientPort, int clientUdpPort, PrintWriter writer) {
            this.id = id;
            this.clientAddress = clientAddress;
            this.clientPort = clientPort;
            this.clientUdpPort = clientUdpPort;
            this.writer = writer;
        }

        public int getId() {
            return id;
        }

        public InetAddress getClientAddress() {
            return clientAddress;
        }

        public int getClientPort() {
            return clientPort;
        }

        public int getClientUdpPort() {
            return clientUdpPort;
        }

        public PrintWriter getWriter() {
            return writer;
        }
    }
}