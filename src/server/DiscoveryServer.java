package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import static server.DiscoveryConfig.*;

/**
 * A server that listens for broadcast UDP packets on a given port.
 * When a packet is received, it checks for discover string and (if match)
 * sends a reply packet containing the server's IP address.
 * 
 * Some issues that may affect this:
 * 1. Server/client on different LAN or VLAN. Router generally does not
 * forward broadcasts.
 * 2. On a wireless network with "Wifi Isolation" enabled, which prevents
 * direct connections between Wifi clients.
 * 3. Server has more than one LAN IP address. Server might respond with
 * wrong IP address. This could be fixed by checking all IP addresses
 * and choosing best match to the client's IP address.
 * 
 * @see https://demey.io/network-discovery-using-udp-broadcast/
 */
public class DiscoveryServer implements Runnable {
	// how much data to accept from a broadcast client.
	private static final int MAX_PACKET_SIZE = 2048;
	private static final Logger logger;
	private DatagramSocket socket;

	/**
	 * Set an environment variable for logging format. This is for 1-line messages.
	 */
	static {
		// %1=datetime %2=methodname %3=loggername %4=level %5=message
		System.setProperty("java.util.logging.SimpleFormatter.format",
				"%1$tF %1$tT %3$s %4$-7s %5$s%n");
		logger = Logger.getLogger("DiscoveryServer");
	}

	@Override
	public void run() {
		try {
			// Keep a socket open to listen to all the UDP trafic that is destined for this
			// port
			socket = new DatagramSocket(8888, InetAddress.getByName("0.0.0.0"));
			socket.setBroadcast(true);

			while (true) {
				System.out.println(getClass().getName() + ">>>Ready to receive broadcast packets!");

				// Receive a packet
				byte[] recvBuf = new byte[15000];
				DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
				socket.receive(packet);

				// Packet received
				System.out.println(getClass().getName() + ">>>Discovery packet received from: "
						+ packet.getAddress().getHostAddress());
				System.out.println(getClass().getName() + ">>>Packet received; data: " + new String(packet.getData()));

				// See if the packet holds the right command (message)
				String message = new String(packet.getData()).trim();
				if (message.equals("DISCOVER_FUIFSERVER_REQUEST")) {
					byte[] sendData = "DISCOVER_FUIFSERVER_RESPONSE".getBytes();

					// Send a response
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(),
							packet.getPort());
					socket.send(sendPacket);

					System.out.println(
							getClass().getName() + ">>>Sent packet to: " + sendPacket.getAddress().getHostAddress());
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(DiscoveryServer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}