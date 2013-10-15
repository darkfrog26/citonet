package com.outr.net.communicator.client.connection;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public interface Connection {
    public void connect();

    /**
     * Invoked by ConnectionManager to let the connection know that a new message was added to the message queue for
     * sending.
     */
    public void messageReady();

    public void update(int delta);
}
