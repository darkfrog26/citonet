package com.outr.citonet.communicator.client;

import com.google.gwt.core.client.EntryPoint;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public class Communicator implements EntryPoint {
    @Override
    public void onModuleLoad() {
        log("Communicator loaded!");
    }

    public static native void log(String message) /*-{
        console.log(message);
    }-*/;
}
