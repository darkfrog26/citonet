package com.outr.net.communicator.client.connection;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public class MessageReceiveFailure {
    public static final int ConnectionNotFound = 1;
    public static final int ConnectionAlreadyExists = 2;
    public static final int InvalidMessageId = 3;

    public final int error;

    public MessageReceiveFailure(int error) {
        this.error = error;
    }
}
