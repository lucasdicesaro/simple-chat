package client;
import java.io.*;
import java.net.*;

public class ClientTCP implements Runnable {

    private static final String SERVER_ADDRESS = "localhost"; // Cambia a la IP del servidor si no está en la misma máquina
    private static final int TCP_PORT = 12345;
    private static int clientId;
    private static final String UDP_PORT_MESSAGE_PATTERN = "UDP:<udpPort>";

    @Override
    public void run() {

        try (Socket socket = new Socket(SERVER_ADDRESS, TCP_PORT);
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
            System.out.println("Cliente UDP escuchando en el puerto " + udpSocket.getLocalPort());
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