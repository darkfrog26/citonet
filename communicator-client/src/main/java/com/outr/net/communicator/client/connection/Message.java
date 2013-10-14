package com.outr.net.communicator.client.connection;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public class Message {
    public final int id;
    public final String event;
    public final Object data;

    public Message(int id, String event, Object data) {
        this.id = id;
        this.event = event;
        this.data = data;
    }
}
