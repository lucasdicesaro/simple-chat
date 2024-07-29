import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Servidor {
    private static final int PUERTO = 12345;
    private static Set<Cliente> clientes = new HashSet<>();
    private static final AtomicInteger clientIdCounter = new AtomicInteger(1);
    
    public static void main(String[] args) {
        System.out.println("Servidor iniciado...");
        try (ServerSocket servidorSocket = new ServerSocket(PUERTO)) {
            while (true) {
                Socket socketCliente = servidorSocket.accept();
                int clientId = clientIdCounter.getAndIncrement();
                System.out.println("Cliente conectado (ID: " + clientId + "): " +
                    socketCliente.getInetAddress() + ":" + socketCliente.getPort());
            
                new HiloCliente(socketCliente, clientId).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class HiloCliente extends Thread {
        private Socket socketCliente;
        private PrintWriter salida;
        private BufferedReader entrada;
        private int clientId;

        public HiloCliente(Socket socket, int clientId) {
            this.socketCliente = socket;
            this.clientId = clientId;
        }

        @Override
        public void run() {
            try {
                entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
                salida = new PrintWriter(socketCliente.getOutputStream(), true);

                // Enviar el ID al cliente
                salida.println("CID:" + clientId);

                synchronized (clientes) {
                    clientes.add(new Cliente(clientId, salida));
                }

                String mensajeDelCliente;
                while ((mensajeDelCliente = entrada.readLine()) != null) {
                    System.out.println("Mensaje recibido del cliente: " + clientId + ": "+ mensajeDelCliente);
                    synchronized (clientes) {
                        for (Cliente cliente : clientes) {                            
                            cliente.getWriter().println("CID:" + String.format("%05d", clientId) + "|MSG:" + mensajeDelCliente);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socketCliente.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientes) {
                    clientes.removeIf(c -> c.getId() == clientId);
                }
            }
        }
    }

    private static class Cliente {
        private int id;
        private PrintWriter writer;

        public Cliente(int id, PrintWriter writer) {
            this.id = id;
            this.writer = writer;
        }

        public int getId() {
            return id;
        }

        public PrintWriter getWriter() {
            return writer;
        }
    }
}
