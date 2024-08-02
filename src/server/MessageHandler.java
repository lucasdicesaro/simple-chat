package server;

public class MessageHandler {

  private static final String CLIENT_ID_PACKAGE = "CID:<clientId>";
  private static final String MESSAGE_PATTERN = "CID:<clientId>|MSG:<message>";

  public static String packClientId(int clientId) {
    return CLIENT_ID_PACKAGE.replaceFirst("<clientId>", String.format("%05d", clientId));
  }

  public static int unpackPortUDP(String udpPortPackage) {
    String udpPort = udpPortPackage.split(":")[1];
    return Integer.parseInt(udpPort);
  }

  public static String packMessage(int clientId, String payload) {
    return MESSAGE_PATTERN.replaceFirst("<clientId>", String.format("%05d", clientId))
        .replaceFirst("<message>", payload);
  }
}
