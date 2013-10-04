package com.outr.citonet.communicator.client;

import com.google.gwt.core.client.EntryPoint;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public class Communicator implements EntryPoint {
    @Override
    public void onModuleLoad() {
        log("Communicator loaded!");
        initializeJQueryPlugin();
    }

    public static native void log(String message) /*-{
        console.log(message);
    }-*/;

    private native void initializeJQueryPlugin() /*-{
        (function($) {
            var dataKey = 'communicator_settings';
            var methods = {
                init: function(options) {
                    var settings = $.extend({
                        id: null,
                        webSocketURL: null,
                        pollingURL: null,
                        sendingURL: null,
                        timeout: 30000
                    }, options);
                    return this.each(function() {
                        var thisElement = this;
                        var $this = $(this);
                        $this.data(dataKey, settings);
                        $.each(settings, function(key, value) {
                            methods.updated.apply(thisElement, [key, value]);
                        });
                    });
                },
                option: function(key, value) {
                    if (value) {

                    }
                }
            }
        })($wnd.jQuery);
    }-*/;
}
