package com.outr.net.communicator.client.connection;

import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
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
            json.put("data", JSONConverter.toJSONValue(message.data));
            return json;
        }
        return null;
    }

    @Override
    public Object fromJSON(JSONValue value) {
        if (value instanceof JSONObject) {
            JSONObject obj = (JSONObject)value;
            if (obj.size() == 2 && obj.containsKey("id") && obj.containsKey("data")) {
                int id = (int)Math.round(((JSONNumber)obj.get("id")).doubleValue());
                Object data = JSONConverter.fromJSONValue(obj.get("data"));
                return new Message(id, data);
            }
        }
        return null;
    }
}
