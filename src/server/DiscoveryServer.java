package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @see https://jackiexie.com/2015/07/15/network-discovery-using-udp-broadcast-java/
 */
public class DiscoveryServer implements Runnable {
	// how much data to accept from a broadcast client.
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
		logger.info("Servidor UDP para Server-Discovery iniciado...");

		try {
			// Keep a socket open to listen to all the UDP trafic that is destined for this
			// port
			socket = new DatagramSocket(8888, InetAddress.getByName("0.0.0.0"));
			socket.setBroadcast(true);

			while (true) {
				logger.info(getClass().getName() + ">>>Ready to receive broadcast packets!");

				// Receive a packet
				byte[] recvBuf = new byte[15000];
				DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
				socket.receive(packet);

				// Packet received
				logger.info(getClass().getName() + ">>>Discovery packet received from: "
						+ packet.getAddress().getHostAddress());
				logger.info(getClass().getName() + ">>>Packet received; data: " + new String(packet.getData()));

				// See if the packet holds the right command (message)
				String message = new String(packet.getData()).trim();
				if (message.equals("DISCOVER_FUIFSERVER_REQUEST")) {
					byte[] sendData = "DISCOVER_FUIFSERVER_RESPONSE".getBytes();

					// Send a response
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(),
							packet.getPort());
					socket.send(sendPacket);

					logger.info(
							getClass().getName() + ">>>Sent packet to: " + sendPacket.getAddress().getHostAddress());
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(DiscoveryServer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}