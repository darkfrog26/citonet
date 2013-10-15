package com.outr.net.communicator.client;


import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.user.client.Event;

import static com.google.gwt.query.client.GQuery.$;
import static com.google.gwt.query.client.GQuery.document;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public class ErrorDialog {
    private final GWTCommunicator communicator;

    private final GQuery div = $(document.createDivElement());
    private final GQuery message = $(document.createDivElement());

    public ErrorDialog(GWTCommunicator communicator) {
        this.communicator = communicator;
    }

    public void init() {
        GQuery body = $(GQuery.body);
        div.id("outrnet_error");
        div.addClass("outrnet_error_hidden");
        GQuery inner = $(document.createDivElement());
        GQuery close = $(document.createAnchorElement());
        close.html("X");
        close.attr("title", "close");
        close.addClass("close");
        close.click(new Function() {
            @Override
            public boolean f(Event e) {
                close();

                return false;
            }
        });
        inner.append(close);
        inner.append(message);
        div.append(inner);
        body.append(div);
    }

    public void show(String title, String text) {
        update(title, text);
        div.removeClass("outrnet_error_hidden");
    }

    public void update(String title, String text) {
        message.html("<h2>" + title + "</h2><p>" + text + "</p>");
    }

    public void close() {
        if (!div.hasClass("outrnet_error_hidden")) {
            div.addClass("outrnet_error_hidden");
        }
    }
}
