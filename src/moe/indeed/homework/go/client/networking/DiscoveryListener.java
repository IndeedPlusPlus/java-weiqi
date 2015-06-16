package moe.indeed.homework.go.client.networking;

import moe.indeed.homework.go.client.bus.MessageBus;
import moe.indeed.homework.go.client.data.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

public class DiscoveryListener extends Thread {
    final static int DISCOVERY_PORT = 13787;
    private final Logger LOGGER = Logger.getLogger(getClass().getName());
    public DiscoveryListener(ThreadGroup group, String name) {
        super(group, name);
    }

    @Override
    public void run() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(DISCOVERY_PORT, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);
            socket.setSoTimeout(1000);
            DatagramPacket packet = new DatagramPacket(new byte[8192], 8192);
            while (true) {
                try {
                    socket.receive(packet);
                    if (this.isInterrupted())
                        return;
                    String data = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                    if (data.startsWith("GO")) {
                        String[] parts = data.split(" ", 2);
                        if (parts.length > 1)
                            MessageBus.getInstance().getOrCreateChannel("DISCOVERY").put(new Server(packet.getAddress(), parts[1]));
                    }

                    LOGGER.info("Discovery received: " + data + " from " + packet.getAddress());
                } catch (SocketTimeoutException ignored) {

                } catch (InterruptedException e) {
                    return;
                }
                if (this.isInterrupted())
                    return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
