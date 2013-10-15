package com.outr.net.communicator.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.json.client.JSONObject;
import com.outr.net.communicator.client.connection.AJAXResponseConverter;
import com.outr.net.communicator.client.connection.ConnectionManager;
import com.outr.net.communicator.client.connection.Message;
import com.outr.net.communicator.client.connection.MessageConverter;
import com.outr.net.communicator.client.event.Listenable;
import com.outr.net.communicator.client.event.Listener;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public class GWTCommunicator implements EntryPoint {
    private Map<String, Object> settings;
    private final ConnectionManager connectionManager;
    public final ErrorDialog error;

    public final Listenable<Message> received = new Listenable<Message>();

    static {
        JSONConverter.add(new MessageConverter());      // Add support to convert Messages to/from JSON
        JSONConverter.add(new AJAXResponseConverter()); // Add support to convert AJAXResponses to/from JSON
    }

    public String getAJAXURL() {
        return setting("ajaxURL", "/Communicator/connect.html");
    }

    public GWTCommunicator() {
        connectionManager = new ConnectionManager(this);
        error = new ErrorDialog(this);
    }

    @Override
    public void onModuleLoad() {
        initialize();
        error.init();
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
            for (Map.Entry<String, JavaScriptObject> entry : setting("on", new HashMap<String, JavaScriptObject>()).entrySet()) {
                on(entry.getKey(), get(json, "on." + entry.getKey()));
            }
            // TODO: handle connection already exists

            connectionManager.connect();
        } catch(Throwable t) {
            log("Exception thrown: " + t.getMessage());
        }
    }

    public void on(final String event, final JavaScriptObject f) {
        received.on(new Listener<Message>() {
            @Override
            public void process(Message message) {
                if (event.equalsIgnoreCase(message.event)) {
                    Object data = message.data;
                    // TODO: provide conversion when necessary
                    Listenable.call(f, data);
                }
            }
        });
    }

    public void send(String event, JavaScriptObject data) {
        Object obj = JSONConverter.fromJavaScriptObject(data);
        connectionManager.send(event, obj);
    }

    public static native void log(String message) /*-{
        $wnd.console.log(message);
    }-*/;

    private native void initialize() /*-{
        var c = this;
        $wnd.GWTCommunicator = {
            connect: function(settings) {
                c.@com.outr.net.communicator.client.GWTCommunicator::connect(Lcom/google/gwt/core/client/JavaScriptObject;)(settings);
            },
            on: function(event, listener) {
                c.@com.outr.net.communicator.client.GWTCommunicator::on(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(event, listener);
            },
            send: function(event, data) {
                c.@com.outr.net.communicator.client.GWTCommunicator::send(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(event, data);
            }
        };
    }-*/;

    private native void notifyHostPage() /*-{
        $wnd.communicatorReady();
    }-*/;

    public static JavaScriptObject get(JavaScriptObject obj, String lookup) {
        int index = lookup.indexOf('.');
        if (index != -1) {
            String key = lookup.substring(0, index);
            String next = lookup.substring(index + 1);
            return get(getInternal(obj, key), next);
        }
        return getInternal(obj, lookup);
    }

    private static native JavaScriptObject getInternal(JavaScriptObject obj, String lookup) /*-{
        return obj[lookup];
    }-*/;
}
