package com.outr.citonet.communicator.client.connection;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public interface Connection {
    public void connect();

    public void send(Object data);
}
