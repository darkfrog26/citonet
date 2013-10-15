package com.outr.net.communicator.client.connection;

import java.util.List;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public class AJAXResponse {
    public final boolean status;
    public final List<Message> data;
    public final MessageReceiveFailure failure;

    public AJAXResponse(boolean status, List<Message> data, MessageReceiveFailure failure) {
        this.status = status;
        this.data = data;
        this.failure = failure;
    }
}
