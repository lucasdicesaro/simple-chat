import java.io.*;
import java.net.*;

public class Cliente {
    private static final String DIRECCION_SERVIDOR = "localhost"; // Cambia a la IP del servidor si no está en la misma máquina
    private static final int PUERTO = 12345;
    private static int id;

    public static void main(String[] args) {
        try (Socket socket = new Socket(DIRECCION_SERVIDOR, PUERTO);
             BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in))) {

            // Leer el ID asignado por el servidor
            String mensaje = entrada.readLine();
            if (mensaje.startsWith("CID:")) {
                id = Integer.parseInt(mensaje.substring(4));
                System.out.println("Tu ID es: " + id);
            }

            Thread hiloLectura = new Thread(() -> {
                try {
                    String mensajeDelServidor;
                    while ((mensajeDelServidor = entrada.readLine()) != null) {
                        System.out.println("\nMensaje del servidor: " + mensajeDelServidor);
                        parsearMensajeDelServidor(mensajeDelServidor);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            hiloLectura.start();

            String mensajeUsuario;
            while ((mensajeUsuario = teclado.readLine()) != null) {
                salida.println(mensajeUsuario);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void parsearMensajeDelServidor(String mensajeDelServidor) {
        String[] tokens = mensajeDelServidor.split("\\|");
        for (String keyValue : tokens) {
            String key = keyValue.split(":")[0];
            String value = keyValue.split(":")[1];
            if ("CID".equals(key)) {
                System.out.println("Client ID: " + value);
                int clientId = Integer.parseInt(value);
                if (id == clientId) {
                    System.out.println("Soy yo mismo");
                }                
            } else if ("MSG".equals(key)) {
                System.out.println("Mensaje: " + value);
            }
        }
    }
}