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

  public static IncomeMessage parseMessage(String message) {
    String[] tokens = message.split("\\|");
    IncomeMessage incomeMessage = new IncomeMessage();
    for (String token : tokens) {
      parseToken(token, incomeMessage);
    }
    return incomeMessage;
  }

  public static void parseToken(String token, IncomeMessage incomeMessage) {
    String[] subtokens = token.split(":");
    if (subtokens.length < 2) {
      // Se espera que cada token tenga el formato <key>:<value>
      // Se ignora si no tiene ese formato.
      return;
    }
    String key = subtokens[0];
    String value = subtokens[1];
    if ("CID".equals(key)) {
      parseClientId(value, incomeMessage);
    } else if ("PLD".equals(key)) {
      parsePayload(value, incomeMessage);
    }
  }

  public static void parseClientId(String clientId, IncomeMessage incomeMessage) {
    incomeMessage.setClientId(clientId);
    System.out.println("Client ID: " + clientId);
  }

  public static void parsePayload(String payload, IncomeMessage incomeMessage) {
    System.out.println("Mensaje: [" + payload + "]");
    incomeMessage.setPayload(new Payload(payload));
  }
}
