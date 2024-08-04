package messages;

public class MessageHandler {

  private static final String UDP_PORT_MESSAGE_PATTERN = "UDP:<udpPort>";
  private static final String MESSAGE_PATTERN = "CID:<clientId>|PLD:<payload>";

  public static String packUDPPort(int udpPort) {
    return UDP_PORT_MESSAGE_PATTERN.replaceFirst("<udpPort>", String.valueOf(udpPort));
  }

  public static int unpackPortUDP(String udpPortPackage) {
    String udpPort = udpPortPackage.split(":")[1];
    return Integer.parseInt(udpPort);
  }

  public static String packMessage(int clientId, String payload) {
    return MESSAGE_PATTERN.replaceFirst("<clientId>", String.format("%05d", clientId))
        .replaceFirst("<payload>", payload);
  }

  public static String packMessage(MessageContainer messageContainer) {
    return MESSAGE_PATTERN.replaceFirst("<clientId>", messageContainer.getClientId())
        .replaceFirst("<payload>",
            messageContainer.getPayload() != null ? messageContainer.getPayload().getContent() : "");
  }

  public static MessageContainer parseMessage(String message) {
    String[] tokens = message.split("\\|");
    MessageContainer messageContainer = new MessageContainer();
    for (String token : tokens) {
      parseToken(token, messageContainer);
    }
    return messageContainer;
  }

  public static void parseToken(String token, MessageContainer messageContainer) {
    String[] subtokens = token.split(":");
    if (subtokens.length < 2) {
      // Se espera que cada token tenga el formato <key>:<value>
      // Se ignora si no tiene ese formato.
      return;
    }
    String key = subtokens[0];
    String value = subtokens[1];
    if ("CID".equals(key)) {
      parseClientId(value, messageContainer);
    } else if ("PLD".equals(key)) {
      parsePayload(value, messageContainer);
    }
  }

  public static void parseClientId(String clientId, MessageContainer messageContainer) {
    messageContainer.setClientId(clientId);
  }

  public static void parsePayload(String payload, MessageContainer messageContainer) {
    messageContainer.setPayload(new Payload(payload));
  }
}
