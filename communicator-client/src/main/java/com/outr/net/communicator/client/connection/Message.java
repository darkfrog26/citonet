package com.outr.net.communicator.client.connection;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public class Message {
    public final int id;
    public final Object data;

    public Message(int id, Object data) {
        this.id = id;
        this.data = data;
    }
}
