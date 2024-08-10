package client;

import java.net.*;
import java.util.logging.Logger;

public class ClientUDP implements Runnable {

    private static final Logger logger;
    private DatagramSocket udpSocket;

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tF %1$tT %3$s %4$-7s %5$s%n");
        logger = Logger.getLogger("ClientUDP");
    }

    public ClientUDP(DatagramSocket udpSocket) {
        this.udpSocket = udpSocket;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                udpSocket.receive(packet);
                String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                logger.info("Recibido [" + receivedMessage + "]");
                logger.info("");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
