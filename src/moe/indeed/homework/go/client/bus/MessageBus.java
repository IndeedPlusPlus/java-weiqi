package moe.indeed.homework.go.client.bus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageBus {

    private final static MessageBus INSTANCE = new MessageBus();

    private final Map<String, BlockingQueue<Object>> channels = new HashMap<>();


    public static MessageBus getInstance() {
        return INSTANCE;
    }

    public BlockingQueue<Object> getOrCreateChannel(String channel) {
        synchronized (channels) {
            BlockingQueue<Object> queue = channels.get(channel);
            if (queue == null) {
                queue = new LinkedBlockingQueue<>();
                channels.put(channel, queue);
            }
            return queue;
        }
    }

    public Object waitForChannel(String channel) throws InterruptedException {
        return getOrCreateChannel(channel).take();
    }
}
