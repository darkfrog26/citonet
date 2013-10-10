package com.outr.net.communicator.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.json.client.JSONObject;

import java.util.Map;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public class Communicator implements EntryPoint {
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

    public void connect(JavaScriptObject json) {
        try {
            Map<String, Object> settings = (Map<String, Object>)JSONConverter.fromJSONValue(new JSONObject(json));
            log("Settings created successfully!");
            for (Map.Entry<String, Object> entry : settings.entrySet()) {
                log("Setting: " + entry.getKey() + ", Value: " + entry.getValue());
            }
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
