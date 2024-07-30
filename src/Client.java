

import client.ClientTCP;

public class Client {

    public static void main(String[] args) {
        Runnable clientTCP = new ClientTCP();
        new Thread(clientTCP).start();
    }
}