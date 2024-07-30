

import client.ClientTCP;

public class Client {

    // Moverlo a un archivo de configuracion no versionado
    private static final String SERVER_ADDRESS = "localhost"; // Cambia a la IP del servidor si no está en la misma máquina
    private static final int TCP_PORT = 12345;

    public static void main(String[] args) {
        Runnable clientTCP = new ClientTCP(SERVER_ADDRESS, TCP_PORT);
        new Thread(clientTCP).start();
    }
}