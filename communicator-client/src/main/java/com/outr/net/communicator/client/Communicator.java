package com.outr.net.communicator.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.json.client.JSONObject;
import com.outr.net.communicator.client.connection.AJAXConnection;
import com.outr.net.communicator.client.connection.Connection;

import java.util.Map;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public class Communicator implements EntryPoint {
    private Map<String, Object> settings;
    private Connection connection;

    private String ajaxURL;

    @Override
    public void onModuleLoad() {
        log("Communicator loaded special!");
        initialize();
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                notifyHostPage();
            }
        });
    }

    private <T> T setting(String name, T def) {
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
            ajaxURL = setting("ajaxURL", "/Communicator/connect.html");
            log("Settings created successfully!");
            // TODO: handle connection already exists

            connection = new AJAXConnection(this, ajaxURL);
            connection.connect();
        } catch(Throwable t) {
            log("Exception thrown: " + t.getMessage());
        }
    }

    public static native void log(String message) /*-{
        console.log(message);
    }-*/;

    private native void initialize() /*-{
        var c = this;
        $wnd.communicator = {
            connect: function(settings) {
                c.@com.outr.net.communicator.client.Communicator::connect(Lcom/google/gwt/core/client/JavaScriptObject;)(settings);
            }
        };
    }-*/;

    private native void notifyHostPage() /*-{
        $wnd.communicatorReady();
    }-*/;
}
