package client;

public class MessageHandler {

  private static final String UDP_PORT_MESSAGE_PATTERN = "UDP:<udpPort>";

  public static int unpackClientId(String clientIdPackage) {
    int clientId = -1;
    if (clientIdPackage.startsWith("CID:")) {
      clientId = Integer.parseInt(clientIdPackage.substring(4));
    }
    return clientId;
  }

  public static String packUDPPort(int udpPort) {
    return UDP_PORT_MESSAGE_PATTERN.replaceFirst("<udpPort>", String.valueOf(udpPort));
  }

  public static void parseMessage(String message, int authorClientId) {
    String[] tokens = message.split("\\|");
    for (String token : tokens) {
      parseToken(token, authorClientId);
    }
  }

  public static void parseToken(String token, int authorClientId) {
    String key = token.split(":")[0];
    String value = token.split(":")[1];
    if ("CID".equals(key)) {
      parseClientId(value, authorClientId);
    } else if ("MSG".equals(key)) {
      parseMessage(value);
    }
  }

  public static void parseClientId(String clientId, int authorClientId) {
    System.out.println("Client ID: " + clientId);
    if (authorClientId == Integer.parseInt(clientId)) {
      System.out.println("Soy yo mismo");
    }
  }

  public static void parseMessage(String message) {
    System.out.println("Mensaje: [" + message + "]");
  }
}
