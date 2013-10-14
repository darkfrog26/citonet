package com.outr.net.communicator.client.event;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public interface Converter<E> {
    public Object convert(E e);
}