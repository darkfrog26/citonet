package com.outr.net.communicator.client.event;

import com.google.gwt.core.client.JavaScriptObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public class Listenable<E> {
    protected final List<Listener<E>> listeners = new ArrayList<Listener<E>>(0);

    public void on(Listener<E> listener) {
        listeners.add(listener);
    }

    public void remove(Listener<E> listener) {
        listeners.remove(listener);
    }

    public void fire(E event) {
        for (Listener<E> listener : listeners) {
            listener.process(event);
        }
    }

    public boolean hasListeners() {
        return !listeners.isEmpty();
    }

    public void listen(final JavaScriptObject f, final Converter<E> converter) {
        on(new Listener<E>() {
            @Override
            public void process(E event) {
                call(f, converter.convert(event));
            }
        });
    }

    public static native void call(JavaScriptObject f, Object value) /*-{
        f(value);
    }-*/;
}