package com.outr.net.communicator.client.connection;

import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.outr.net.communicator.client.JSONConverter;
import com.outr.net.communicator.client.JSONSupport;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public class MessageConverter implements JSONSupport {
    @Override
    public JSONValue toJSON(Object obj) {
        if (obj instanceof Message) {
            Message message = (Message)obj;
            JSONObject json = new JSONObject();
            json.put("id", new JSONNumber(message.id));
            json.put("event", new JSONString(message.event));
            json.put("data", JSONConverter.toJSONValue(message.data));
            return json;
        }
        return null;
    }

    @Override
    public Object fromJSON(JSONValue value) {
        if (value instanceof JSONObject) {
            JSONObject obj = (JSONObject)value;
            if (obj.size() == 3 && obj.containsKey("id") && obj.containsKey("event") && obj.containsKey("data")) {
                int id = (int)Math.round(((JSONNumber)obj.get("id")).doubleValue());
                String event = ((JSONString)obj.get("event")).stringValue();
                Object data = JSONConverter.fromJSONValue(obj.get("data"));
                return new Message(id, event, data);
            }
        }
        return null;
    }
}
