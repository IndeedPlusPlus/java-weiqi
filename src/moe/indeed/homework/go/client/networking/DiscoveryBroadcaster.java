package moe.indeed.homework.go.client.networking;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscoveryBroadcaster extends Thread {
    public DiscoveryBroadcaster(ThreadGroup group, String name, String content) {
        super(group, name);
        this.content = content;
    }

    private String content;
    final static int DISCOVERY_PORT = 13787;
    final static int INTERVAL = 1000;
    private final Logger LOGGER = Logger.getLogger(getClass().getName());

    public DiscoveryBroadcaster(String content) {
        super("discovery-broadcaster");
        this.content = content;
    }

    private void sendBroadcast(DatagramSocket socket, byte[] data, InetAddress broadcast)

    {
        LOGGER.info("Sending broadcast to " + broadcast);
        try {
            socket.send(new DatagramPacket(data, data.length, broadcast, DISCOVERY_PORT));
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Fail to broadcast to " + broadcast, ex);
        }
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            byte[] data = content.getBytes("UTF-8");
            while (!this.isInterrupted()) {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                try {
                    this.sendBroadcast(socket, data, InetAddress.getByName("255.255.255.255"));
                } catch (UnknownHostException ignored) {

                }
                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = interfaces.nextElement();
                    if (networkInterface.isUp()) {
                        for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                            InetAddress broadcast = interfaceAddress.getBroadcast();
                            if (broadcast != null) {
                                if (this.isInterrupted())
                                    return;
                                this.sendBroadcast(socket, data, broadcast);
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(INTERVAL);
                } catch (InterruptedException ex) {
                    break;
                }
            }
        } catch (SocketException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
