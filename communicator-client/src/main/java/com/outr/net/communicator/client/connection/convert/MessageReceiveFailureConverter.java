package com.outr.net.communicator.client.connection.convert;

import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.outr.net.communicator.client.JSONSupport;
import com.outr.net.communicator.client.connection.MessageReceiveFailure;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public class MessageReceiveFailureConverter implements JSONSupport {
    @Override
    public JSONValue toJSON(Object obj) {
        if (obj instanceof MessageReceiveFailure) {
            MessageReceiveFailure f = (MessageReceiveFailure)obj;
            JSONObject json = new JSONObject();
            json.put("error", new JSONNumber(f.error));
            return json;
        }
        return null;
    }

    @Override
    public Object fromJSON(JSONValue value) {
        if (value instanceof JSONObject) {
            JSONObject obj = value.isObject();
            if (obj.size() == 1 && obj.containsKey("error")) {
                int error = (int)obj.get("error").isNumber().doubleValue();
                return new MessageReceiveFailure(error);
            }
        }
        return null;
    }
}
