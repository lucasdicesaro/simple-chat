package client;

import java.io.*;
import java.net.*;

import messages.MessageContainer;
import messages.MessageHandler;

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
                System.out.println("Recibido [" + receivedMessage + "]");
                MessageContainer messageContainer = MessageHandler.parseMessage(receivedMessage);
                if (clientId == Integer.parseInt(messageContainer.getClientId())) {
                    System.out.println("Soy yo mismo");
                }
                System.out.println("");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
