package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class ClientTCPReader implements Runnable {

    private Socket socket;
    private BufferedReader in;

    public ClientTCPReader(Socket socket, BufferedReader in) {
        this.socket = socket;
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
        } catch (SocketException e) {
            System.out.println("Socket cerrado");
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
