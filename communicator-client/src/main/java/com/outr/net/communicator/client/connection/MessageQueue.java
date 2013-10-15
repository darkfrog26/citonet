package com.outr.net.communicator.client.connection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public class MessageQueue {
    private int incrementor = 0;
    private final List<Message> queue = new ArrayList<Message>();
    private final List<Message> sent = new ArrayList<Message>();

    /**
     * Enqueues a message to be sent
     *
     * @param event the event name for the message
     * @param value the value to be sent
     */
    public void enqueue(String event, Object value) {
        queue.add(new Message(++incrementor, event, value));
    }

    public void enqueueHighPriority(String event, Object value) {
        int index = 0;
        while (queue.size() > index && queue.get(index).id == -1) {
            index++;
        }
        queue.add(index, new Message(-1, event, value));
    }

    /**
     * Retrieves and removes the next available message from the queue for sending. The message is then moved into the
     * sent queue for later removal or access.
     *
     * @return the next available message or null
     */
    public Message next() {
        if (queue.isEmpty()) {
            return null;
        }
        Message message = queue.remove(0);
        sent.add(message);
        return message;
    }

    public boolean hasNext() {
        return !queue.isEmpty();
    }

    public int waiting() {
        return queue.size();
    }

    /**
     * Confirms receipt of all messages that were sent.
     */
    public void confirm() {
        sent.clear();
    }

    /**
     * Lets the system know that a message send failed and the "sent" messages should be added back to the queue for
     * sending.
     */
    public void failed() {
        queue.addAll(0, sent);
        sent.clear();
    }
}