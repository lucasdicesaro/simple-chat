package client;
import java.io.*;
import java.net.*;

public class ClientUDP implements Runnable {

    private DatagramSocket udpSocket;
    private int clientId;

    public ClientUDP(DatagramSocket udpSocket, int clientId) {
        this.udpSocket = udpSocket;
        this.clientId = clientId;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                udpSocket.receive(packet);
                String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Mensaje recibido por UDP: " + receivedMessage);
                parseMessageFromServer(receivedMessage);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseMessageFromServer(String message) {
        String[] tokens = message.split("\\|");
        for (String keyValue : tokens) {
            String key = keyValue.split(":")[0];
            String value = keyValue.split(":")[1];
            if ("CID".equals(key)) {
                System.out.println("Client ID: " + value);
                int clientIdFromServer = Integer.parseInt(value);
                if (clientId == clientIdFromServer) {
                    System.out.println("Soy yo mismo");
                }         
            } else if ("MSG".equals(key)) {
                System.out.println("Mensaje: " + value);
            }
        }
    }
}