package com.outr.net.communicator.client.event;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public interface Listener<E> {
    public void process(E event);
}