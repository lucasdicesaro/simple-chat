package client;

import java.io.BufferedReader;
import java.io.IOException;

public class ClientTCPReader implements Runnable {

    private BufferedReader in;

    public ClientTCPReader(BufferedReader in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            String tcpMessageFromServer;
            while ((tcpMessageFromServer = in.readLine()) != null) {
                System.out.println("\nMensaje del servidor: " + tcpMessageFromServer);
                // Por ahora el Servidor no enviara mensajes TCP al cliente.
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
