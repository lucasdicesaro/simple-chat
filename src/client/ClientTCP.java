package client;

import java.io.*;
import java.net.*;

public class ClientTCP implements Runnable {

    private static int clientId;
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
            String mensaje = entrada.readLine();
            clientId = MessageHandler.unpackClientId(mensaje);
            System.out.println("ClientId asignado por el servidor: " + clientId);

            // Enviar al servidor el puerto UDP asignado por el SO
            DatagramSocket udpSocket = new DatagramSocket();
            int udpPort = udpSocket.getLocalPort();
            System.out.println("Puerto UDP asignado por el SO: " + udpPort);
            String udpPortPackage = MessageHandler.packUDPPort(udpPort);
            salida.println(udpPortPackage);

            Runnable clientUDP = new ClientUDP(udpSocket, clientId);
            new Thread(clientUDP).start();

            Runnable clientTCPReader = new ClientTCPReader(entrada);
            new Thread(clientTCPReader).start();

            String mensajeUsuario;
            System.out.print("> ");
            while ((mensajeUsuario = teclado.readLine()) != null) {
                if (!mensajeUsuario.isBlank()) {
                    salida.println(mensajeUsuario);
                    System.out.println("Enviado: " + mensajeUsuario);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
