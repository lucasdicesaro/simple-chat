
import java.util.logging.Logger;

import client.ClientTCP;
import client.DiscoveryClient;

public class Client {

    private static final Logger logger;
    // TODO Moverlo a un archivo de configuracion no versionado
    // Cambia a la IP del servidor si no está en la misma máquina
    private static final String SERVER_ADDRESS = "localhost";
    private static final int TCP_PORT = 12345;

    static {
        // %1=datetime %2=methodname %3=loggername %4=level %5=message
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tF %1$tT %3$s %4$-7s %5$s%n");
        logger = Logger.getLogger("Client");
    }

    public static void main(String[] args) {

        DiscoveryClient client = new DiscoveryClient();
        String server = client.call();
        logger.info("Se obtuvo la IP del servidor: " + server);

        Runnable clientTCP = new ClientTCP(server != null ? server : SERVER_ADDRESS, TCP_PORT);
        new Thread(clientTCP).start();
    }
}
