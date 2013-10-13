package com.outr.net.communicator.client.connection;

import com.outr.net.communicator.client.GWTCommunicator;
import com.outr.net.communicator.client.UUID;

import java.io.IOException;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public class ConnectionManager {
    public final GWTCommunicator communicator;
    public final String uuid;
    public final MessageQueue queue;

    private Connection connection;
    private boolean connected;
    private int lastReceiveId;

    public ConnectionManager(GWTCommunicator communicator) {
        this.communicator = communicator;
        uuid = UUID.unique();
        queue = new MessageQueue();
        queue.enqueueHighPriority("create");
    }

    public boolean isConnected() {
        return connection != null && connected;
    }

    public void connect() throws IOException {
        if (connection != null) {
            throw new IOException("Connection already established!");
        }
        // TODO: support multiple connection types based on settings
        connection = new AJAXConnection(this, communicator.getAJAXURL());
        queue.enqueueHighPriority("connect");
        connection.connect();
    }

    public void disconnected() {
        connection = null;
        connected = false;
        // TODO: reconnect
    }

    public int getLastReceiveId() {
        return lastReceiveId;
    }

    /**
     * Reliably enqueues a message to be sent to the server.
     *
     * @param value the data to send in the message.
     */
    public void send(Object value) {
        queue.enqueue(value);
        if (connection != null) {
            connection.messageReady();
        }
    }

    public void received(Message message) {
        int expectedId = lastReceiveId + 1;
        if (message.id != expectedId) {
            GWTCommunicator.log("Receive id is incorrect! Last Receive ID: " + lastReceiveId + ", Message Id: " + message.id + ", Expected: " + expectedId + ", Ignoring message and re-requesting!");
            return;
        }
        GWTCommunicator.log("Received: " + message.id + ", " + message.data);
        // TODO: fire received event
        lastReceiveId = expectedId;
    }
}
