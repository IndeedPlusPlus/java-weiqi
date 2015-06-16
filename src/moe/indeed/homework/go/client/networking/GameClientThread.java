package moe.indeed.homework.go.client.networking;

import moe.indeed.homework.go.client.bus.MessageBus;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class GameClientThread extends Thread {
    public static final int GAME_PORT = 13788;
    private final Logger LOGGER = Logger.getLogger(getClass().getName());
    private InetAddress target;

    public GameClientThread(String name, InetAddress target) {
        super(name);
        this.target = target;
    }

    @Override
    public void run() {
        BlockingQueue<Object> toServer = MessageBus.getInstance().getOrCreateChannel("OUTGOING");
        BlockingQueue<Object> channel = MessageBus.getInstance().getOrCreateChannel("GAME");
        try (Socket socket = new Socket(target, GAME_PORT);) {
            channel.put("CONNECTED");
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
            while (!this.isInterrupted()) {
                String data = (String) toServer.poll();
                if (data == null)
                    data = "TICK";
                writer.write(data);
                writer.newLine();
                writer.flush();

                String line = reader.readLine();
                if (line == null)
                    throw new IOException("end of stream");
                if (!line.equals("OK"))
                    channel.put(line);
                if (!line.equals("OK") || !data.equals("TICK"))
                    LOGGER.info(data + " / " + line);
                Thread.sleep(100);
            }
        } catch (IOException e) {
            try {
                channel.put(e);
            } catch (InterruptedException e1) {

            }
        } catch (InterruptedException ignored) {

        }
    }
}
