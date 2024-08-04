package client;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.*; // For Callable, ExecutorServer, Executors, FutureTask
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @see https://jackiexie.com/2015/07/15/network-discovery-using-udp-broadcast-java/
 */
public class DiscoveryClient implements Callable<String> {

	private static final Logger logger;

	/* Set an environment variable for for 1-line log messages. */
	static {
		// %1=datetime %2=methodname %3=loggername %4=level %5=message
		System.setProperty("java.util.logging.SimpleFormatter.format",
				"%1$tF %1$tT %3$s %4$-7s %5$s%n");
		logger = Logger.getLogger("DiscoveryClient");
	}

	/**
	 * Send broadcast packets with service request string until a response
	 * is received. Return the response as String (even though it should
	 * contain an internet address).
	 * 
	 * @return String received from server. Should be server IP address.
	 *         Returns empty string if failed to get valid reply.
	 */
	public String call() {
		String serverIp = null;
		// Find the server using UDP broadcast
		try {
			// Open a random port to send the package
			DatagramSocket c = new DatagramSocket();
			c.setBroadcast(true);

			byte[] sendData = "DISCOVER_SERVER_REQUEST".getBytes();

			// Try the 255.255.255.255 first
			try {
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
						InetAddress.getByName("255.255.255.255"), 8888);
				c.send(sendPacket);
				logger.info(">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
			} catch (Exception e) {
			}

			// Broadcast the message over all the network interfaces
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

				if (networkInterface.isLoopback() || !networkInterface.isUp()) {
					continue; // Don't want to broadcast to the loopback interface
				}

				for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
					InetAddress broadcast = interfaceAddress.getBroadcast();
					if (broadcast == null) {
						continue;
					}

					// Send the broadcast package!
					try {
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
						c.send(sendPacket);
					} catch (Exception e) {
					}

					logger.info(">>> Request packet sent to: "
							+ broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
				}
			}

			logger.info(">>> Done looping over all network interfaces. Now waiting for a reply!");

			// Wait for a response
			byte[] recvBuf = new byte[15000];
			DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
			c.receive(receivePacket);

			// We have a response
			logger.info(">>> Broadcast response from server: "
					+ receivePacket.getAddress().getHostAddress());

			// Check if the message is correct
			String message = new String(receivePacket.getData()).trim();
			if (message.equals("DISCOVER_SERVER_RESPONSE")) {
				serverIp = receivePacket.getAddress().getHostAddress();
			}

			// Close the port!
			c.close();
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
		return serverIp;
	}
}
