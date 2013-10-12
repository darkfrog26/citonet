package com.outr.net.communicator.client.connection;

import com.google.gwt.http.client.*;
import com.outr.net.communicator.client.Communicator;
import com.outr.net.communicator.client.JSONConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public class AJAXConnection implements Connection {
    private final Communicator communicator;
    private final RequestBuilder pollBuilder;
    private final RequestBuilder sendBuilder;

    private List<Object> sendQueue = new ArrayList<Object>();
    private List<Object> sentQueue = new ArrayList<Object>();

    private Request pollRequest;
    private Request sendRequest;
    private RequestCallback pollCallback = new RequestCallback() {
        @Override
        public void onResponseReceived(Request request, Response response) {
            pollRequest = null;
            // TODO: process data incoming
            Communicator.log("Received: " + response.getText());
            ArrayList<Map<String, Object>> data = (ArrayList<Map<String, Object>>)JSONConverter.fromString(response.getText());
            Communicator.log("Converted: " + data.size() + ", " + data.get(0).get("data"));
            // TODO: reconnect after validating proper response
        }

        @Override
        public void onError(Request request, Throwable exception) {
            Communicator.log("Error received from poll!");
            // TODO: log error polling
            // TODO: delayed retry
        }
    };
    private RequestCallback sendCallback = new RequestCallback() {
        @Override
        public void onResponseReceived(Request request, Response response) {
            Communicator.log("Response received from send! " + response.getText());
            sendRequest = null;
        }

        @Override
        public void onError(Request request, Throwable exception) {
            Communicator.log("Error received from send!");
            // TODO: add sentQueue back to sendQueue
            // TODO: log error sending
            // TODO: delayed retry
        }
    };

    public AJAXConnection(Communicator communicator, String ajaxURL) {
        this.communicator = communicator;
        pollBuilder = new RequestBuilder(RequestBuilder.GET, ajaxURL);
        pollBuilder.setTimeoutMillis(60000);
        sendBuilder = new RequestBuilder(RequestBuilder.POST, ajaxURL);
        sendBuilder.setTimeoutMillis(10000);
    }

    @Override
    public void connect() {
        connectPolling();
    }

    private void connectPolling() {
        try {
            pollRequest = pollBuilder.sendRequest(null, pollCallback);
            sendRequest = sendBuilder.sendRequest("{\"test\": \"true\"}", sendCallback);
        } catch(RequestException exc) {
            Communicator.log("PollingRequestError: " + exc.getMessage());
        }
    }

    private void sendData() {
        if (!sendQueue.isEmpty()) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", null);
            map.put("data", sendQueue);
            String json = JSONConverter.toJSONValue(map).toString();
            try {
                sendRequest = sendBuilder.sendRequest(json, sendCallback);
                sentQueue = sendQueue;
                sendQueue = new ArrayList<Object>();
            } catch(RequestException exc) {
                Communicator.log("SendRequestError: " + exc.getMessage());
            }
        }
    }

    @Override
    public void send(Object data) {
        sendQueue.add(data);
        if (sendRequest == null) {      // Nothing currently sending
            sendData();
        }
    }
}
