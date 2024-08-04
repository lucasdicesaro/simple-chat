package client;

import java.io.*;
import java.net.*;

import messages.MessageContainer;
import messages.MessageHandler;

public class ClientTCP implements Runnable {

    private String serverAddress;
    private int tcpPort;

    public ClientTCP(String serverAddress, int tcpPort) {
        this.serverAddress = serverAddress;
        this.tcpPort = tcpPort;
    }

    @Override
    public void run() {

        try (Socket socket = new Socket(serverAddress, tcpPort);
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in))) {

            // Leer el ClientId asignado por el servidor
            String clientIdPackage = entrada.readLine();
            System.out.println("Recibido ClientId package [" + clientIdPackage + "]");

            MessageContainer messageContainer = MessageHandler.parseMessage(clientIdPackage);
            System.out.println("ClientId asignado por el servidor: " + messageContainer.getClientId());

            // Enviar al servidor el puerto UDP asignado por el SO
            DatagramSocket udpSocket = new DatagramSocket();
            String udpPortPackage = MessageHandler.packUDPPort(udpSocket.getLocalPort());
            salida.println(udpPortPackage);
            System.out.println("Enviado UDP port package [" + udpPortPackage + "]");

            int clientId = Integer.parseInt(messageContainer.getClientId());

            Runnable clientUDP = new ClientUDP(udpSocket, clientId);
            new Thread(clientUDP).start();

            Runnable clientTCPReader = new ClientTCPReader(socket, entrada);
            new Thread(clientTCPReader).start();

            String payload;
            System.out.print("> ");
            while ((payload = teclado.readLine()) != null) {
                if (!payload.isBlank()) {
                    String message = MessageHandler.packMessage(clientId, payload);
                    salida.println(message);
                    System.out.println("Enviado  [" + message + "]");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
