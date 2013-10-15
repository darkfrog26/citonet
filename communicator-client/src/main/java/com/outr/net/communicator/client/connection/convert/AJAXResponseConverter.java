package com.outr.net.communicator.client.connection.convert;

import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.outr.net.communicator.client.JSONConverter;
import com.outr.net.communicator.client.JSONSupport;
import com.outr.net.communicator.client.connection.AJAXResponse;
import com.outr.net.communicator.client.connection.Message;
import com.outr.net.communicator.client.connection.MessageReceiveFailure;

import java.util.List;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public class AJAXResponseConverter implements JSONSupport {
    @Override
    public JSONValue toJSON(Object obj) {
        if (obj instanceof AJAXResponse) {
            AJAXResponse r = (AJAXResponse)obj;
            JSONObject json = new JSONObject();
            json.put("status", JSONBoolean.getInstance(r.status));
            json.put("data", JSONConverter.toJSONValue(r.data));
            json.put("failure", JSONConverter.toJSONValue(r.failure));
            return json;
        } else {
            return null;
        }
    }

    @Override
    public Object fromJSON(JSONValue value) {
        if (value.isObject() != null) {
            JSONObject obj = value.isObject();
            if (obj.size() == 3 && obj.containsKey("status") && obj.containsKey("data") && obj.containsKey("failure")) {
                boolean status = obj.get("status").isBoolean().booleanValue();
                List<Message> data = (List<Message>)JSONConverter.fromJSONValue(obj.get("data"));
                MessageReceiveFailure failure = (MessageReceiveFailure)JSONConverter.fromJSONValue(obj.get("failure"));
                return new AJAXResponse(status, data, failure);
            }
        }
        return null;
    }
}
