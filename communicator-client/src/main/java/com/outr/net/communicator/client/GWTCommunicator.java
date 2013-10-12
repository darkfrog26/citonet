package com.outr.net.communicator.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.json.client.JSONObject;
import com.outr.net.communicator.client.connection.ConnectionManager;
import com.outr.net.communicator.client.connection.MessageConverter;

import java.util.Map;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public class GWTCommunicator implements EntryPoint {
    private Map<String, Object> settings;
    private final ConnectionManager connectionManager;

    static {
        JSONConverter.add(new MessageConverter());      // Add support to convert Messages to/from JSON
    }

    public String getAJAXURL() {
        return setting("ajaxURL", "/Communicator/connect.html");
    }

    public GWTCommunicator() {
        connectionManager = new ConnectionManager(this);
    }

    @Override
    public void onModuleLoad() {
        log("GWTCommunicator loaded special!");
        initialize();
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                notifyHostPage();
            }
        });
    }

    public <T> T setting(String name, T def) {
        T value = (T)settings.get(name);
        if (value == null) {
            return def;
        } else {
            return value;
        }
    }

    public void connect(JavaScriptObject json) {
        try {
            settings = (Map<String, Object>)JSONConverter.fromJSONValue(new JSONObject(json));
            log("Settings created successfully! " + settings);
            // TODO: handle connection already exists

            connectionManager.connect();
        } catch(Throwable t) {
            log("Exception thrown: " + t.getMessage());
        }
    }

    public static native void log(String message) /*-{
        $wnd.console.log(message);
    }-*/;

    private native void initialize() /*-{
        var c = this;
        $wnd.GWTCommunicator = {
            connect: function(settings) {
                c.@com.outr.net.communicator.client.GWTCommunicator::connect(Lcom/google/gwt/core/client/JavaScriptObject;)(settings);
            }
        };
    }-*/;

    private native void notifyHostPage() /*-{
        $wnd.communicatorReady();
    }-*/;
}
