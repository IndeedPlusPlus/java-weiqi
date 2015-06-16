package moe.indeed.homework.go.client.networking;

import moe.indeed.homework.go.client.bus.MessageBus;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameServerThread extends Thread {
    public static final int GAME_PORT = 13788;
    private final Logger LOGGER = Logger.getLogger(getClass().getName());

    public GameServerThread(ThreadGroup group, String name) {
        super(group, name);
    }

    public GameServerThread() {
    }

    @Override
    public void run() {
        BlockingQueue<Object> channel = MessageBus.getInstance().getOrCreateChannel("GAME");
        BlockingQueue<Object> toClient = MessageBus.getInstance().getOrCreateChannel("OUTGOING");
        try (ServerSocket serverSocket = new ServerSocket(GAME_PORT);
             Socket socket = serverSocket.accept();) {
            channel.put("PLAYER_JOIN");
            while (!this.isInterrupted()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
                while (!this.isInterrupted()) {
                    String line = reader.readLine();

                    if (line == null) {
                        throw new IOException("end of stream");
                    }

                    if (!line.equals("TICK")) {
                        channel.put(line);
                    }
                    String data = (String) toClient.poll();
                    if (data == null)
                        data = "OK";
                    writer.write(data);
                    writer.newLine();
                    writer.flush();
                    if (!line.equals("TICK") || !data.equals("OK"))
                        LOGGER.info(line + " / " + data);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "main loop failed.", e);
            try {
                channel.put(e);
            } catch (InterruptedException ignored) {

            }
        } catch (InterruptedException ignored) {

        }

    }

}
