package client;
import java.io.*;
import java.net.*;

public class ClientTCP implements Runnable {

    private static int clientId;
    private static final String UDP_PORT_MESSAGE_PATTERN = "UDP:<udpPort>";
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

            // Leer el ID asignado por el servidor
            String mensaje = entrada.readLine();
            if (mensaje.startsWith("CID:")) {
                clientId = Integer.parseInt(mensaje.substring(4));
                System.out.println("Tu ID es: " + clientId);
            }

            // Enviar el puerto UDP asignado por el SO al servidor
            DatagramSocket udpSocket = new DatagramSocket();
            int udpPort = udpSocket.getLocalPort();
            System.out.println("Cliente UDP escuchando en el puerto " + udpPort);
            String udpPortPackage = UDP_PORT_MESSAGE_PATTERN.replaceFirst("<udpPort>", String.valueOf(udpPort));
            salida.println(udpPortPackage);

            Runnable clientUDP = new ClientUDP(udpSocket, clientId);
            new Thread(clientUDP).start();

            Runnable clientTCPReader = new ClientTCPReader(entrada);
            new Thread(clientTCPReader).start();

            String mensajeUsuario;
            while ((mensajeUsuario = teclado.readLine()) != null) {
                if (!mensajeUsuario.isBlank()) {
                    salida.println(mensajeUsuario);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}