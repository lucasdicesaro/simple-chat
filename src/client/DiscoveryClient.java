package client;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.*; // For Callable, ExecutorServer, Executors, FutureTask
import java.util.logging.Level;
import java.util.logging.Logger;

import static server.DiscoveryConfig.*;

/**
 * A client that tries to discover a service by sending a UDP broadcast on a
 * known port, containing a given request string. Waits for a reply from the
 * server. It resends the broadcast every TIMEOUT milliseonds (default 1 sec)
 * until a response is received.
 * 
 * I implemented it as a Callable<InetAddress> so it can be run in a separate
 * thread.
 * 
 * Some issues that may affect this: 1. Server/client on different LAN or VLAN.
 * Router generally does not forward broadcasts. 2. On a wireless network with
 * "Wifi Isolation" enabled, which prevents direct connections between Wifi
 * clients. 3. Client has more than one LAN IP address (at KU, machines have
 * both IPv4 and IPv6 addresses). Might broadcast on the wrong address. This can
 * be fixed by getting all IP addresses except localhost and loopback. See
 * NetworkUtil class for example.
 * 
 * @see https://demey.io/network-discovery-using-udp-broadcast/
 */
public class DiscoveryClient implements Callable<String> {
	private static final int MAX_PACKET_SIZE = 2048;
	/** maximum time to wait for a reply, in milliseconds. */
	private static final int TIMEOUT = 2000; // milliseonds
	private static final Logger logger;

	/* Set an environment variable for for 1-line log messages. */
	static {
		// %1=datetime %2=methodname %3=loggername %4=level %5=message
		System.setProperty("java.util.logging.SimpleFormatter.format",
				"%1$tF %1$tT %3$s %4$-7s %5$s%n");
		logger = Logger.getLogger("DiscoveryClient");
	}

	/**
	 * Create a UDP socket on the service discovery broadcast port.
	 * 
	 * @return open DatagramSocket if successful
	 * @throws RuntimeException
	 *                          if cannot create the socket
	 */
	public DatagramSocket createSocket() {
		// Create a Datagram (UDP) socket on any available port
		DatagramSocket socket = null;
		// Create a socket for sending UDP broadcast packets
		try {
			socket = new DatagramSocket();
			socket.setBroadcast(true);
			// use a timeout and resend broadcasts instead of waiting forever
			socket.setSoTimeout(TIMEOUT);
		} catch (SocketException sex) {
			logger.severe("SocketException creating broadcast socket: "
					+ sex.getMessage());
			throw new RuntimeException(sex);
		}
		return socket;
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

			byte[] sendData = "DISCOVER_FUIFSERVER_REQUEST".getBytes();

			// Try the 255.255.255.255 first
			try {
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
						InetAddress.getByName("255.255.255.255"), 8888);
				c.send(sendPacket);
				System.out.println(getClass().getName() + ">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
			} catch (Exception e) {
			}

			// Broadcast the message over all the network interfaces
			Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
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

					System.out.println(getClass().getName() + ">>> Request packet sent to: "
							+ broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
				}
			}

			System.out.println(
					getClass().getName() + ">>> Done looping over all network interfaces. Now waiting for a reply!");

			// Wait for a response
			byte[] recvBuf = new byte[15000];
			DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
			c.receive(receivePacket);

			// We have a response
			System.out.println(getClass().getName() + ">>> Broadcast response from server: "
					+ receivePacket.getAddress().getHostAddress());

			// Check if the message is correct
			String message = new String(receivePacket.getData()).trim();
			if (message.equals("DISCOVER_FUIFSERVER_RESPONSE")) {
				// DO SOMETHING WITH THE SERVER'S IP (for example, store it in your controller)
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
