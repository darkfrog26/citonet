package com.outr.net.communicator.client.connection;

import com.google.gwt.http.client.*;
import com.outr.net.communicator.client.GWTCommunicator;
import com.outr.net.communicator.client.JSONConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public class AJAXConnection implements Connection {
    private final ConnectionManager manager;
    private final RequestBuilder pollBuilder;
    private final RequestBuilder sendBuilder;

    private Request pollRequest;
    private Request sendRequest;
    private RequestCallback pollCallback = new RequestCallback() {
        @Override
        public void onResponseReceived(Request request, Response response) {
            if (response.getStatusCode() == 200) {
                pollRequest = null;
                // TODO: process data incoming
                GWTCommunicator.log("Received (" + response.getStatusCode() + "): " + response.getText());
                ArrayList<Map<String, Object>> data = (ArrayList<Map<String, Object>>)JSONConverter.fromString(response.getText());
                GWTCommunicator.log("Converted: " + data.size() + ", " + data.get(0).get("data"));
                // TODO: reconnect after validating proper response
            } else {
                pollError("Bad Response: " + response.getStatusText() + " (" + response.getStatusCode() + ")");
            }
        }

        @Override
        public void onError(Request request, Throwable exception) {
            pollError(exception.getMessage());
        }
    };
    private RequestCallback sendCallback = new RequestCallback() {
        @Override
        public void onResponseReceived(Request request, Response response) {
            if (response.getStatusCode() == 200) {
                GWTCommunicator.log("Response received from send! " + response.getText());
                sendRequest = null;
                sendData();                         // Send more data if there is more to send to the server
            } else {
                sendError("Bad Response: " + response.getStatusText() + " (" + response.getStatusCode() + ")");
            }
        }

        @Override
        public void onError(Request request, Throwable exception) {
            sendError(exception.getMessage());
        }
    };

    public AJAXConnection(ConnectionManager manager, String ajaxURL) {
        this.manager = manager;
        pollBuilder = new RequestBuilder(RequestBuilder.POST, ajaxURL);
        pollBuilder.setTimeoutMillis(60000);
        sendBuilder = new RequestBuilder(RequestBuilder.POST, ajaxURL);
        sendBuilder.setTimeoutMillis(10000);
    }

    @Override
    public void connect() {
        connectPolling();
        sendData();
    }

    private void connectPolling() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", manager.uuid);
        map.put("type", "receive");
        map.put("lastReceiveId", manager.getLastReceiveId());
        String json = JSONConverter.toJSONValue(map).toString();
        try {
            pollRequest = pollBuilder.sendRequest(json, pollCallback);
        } catch(RequestException exc) {
            GWTCommunicator.log("PollingRequestError: " + exc.getMessage());
        }
    }

    private void sendData() {
        if (sendRequest == null && manager.queue.hasNext()) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", manager.uuid);
            map.put("type", "send");
            List<Message> messages = new ArrayList<Message>(manager.queue.waiting());
            while (manager.queue.hasNext()) {
                messages.add(manager.queue.next());
            }
            map.put("messages", messages);

            String json = JSONConverter.toJSONValue(map).toString();
            GWTCommunicator.log("sending: [" + json + "]");
            try {
                sendRequest = sendBuilder.sendRequest(json, sendCallback);
            } catch(RequestException exc) {
                GWTCommunicator.log("SendRequestError: " + exc.getMessage());
            }
        }
    }

    @Override
    public void messageReady() {
        sendData();
    }

    private void pollError(String error) {
        GWTCommunicator.log("Error received from poll: " + error);
        // TODO: log error polling
        // TODO: delayed retry
    }

    private void sendError(String error) {
        GWTCommunicator.log("Error received from send: " + error);
        // TODO: add sentQueue back to sendQueue
        // TODO: log error sending
        // TODO: delayed retry
    }
}
