package com.outr.net.communicator.client;

import com.google.gwt.json.client.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Matt Hicks <matt@outr.com>
 */
public class JSONConverter {
    private static final List<JSONSupport> supportList = new ArrayList<JSONSupport>();

    public static void add(JSONSupport support) {
        supportList.add(support);
    }

    public static JSONValue toJSONValue(Object obj) {
        if (obj instanceof List) {
            List<Object> list = (List<Object>)obj;
            JSONArray array = new JSONArray();
            int position = 0;
            for (Object item : list) {
                array.set(position++, toJSONValue(item));
            }
            return array;
        } else if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>)obj;
            JSONObject json = new JSONObject();
            for (Map.Entry<String, Object> entry: map.entrySet()) {
                JSONValue value = toJSONValue(entry.getValue());
                json.put(entry.getKey(), value);
            }
            return json;
        } else if (obj instanceof String) {
            return new JSONString((String)obj);
        } else if (obj instanceof Double) {
            return new JSONNumber((Double)obj);
        } else if (obj instanceof Integer) {
            return new JSONNumber((Integer)obj);
        } else if (obj instanceof Boolean) {
            return JSONBoolean.getInstance((Boolean)obj);
        } else {
            for (JSONSupport support : supportList) {
                JSONValue value = support.toJSON(obj);
                if (value != null) {
                    return value;
                }
            }
            log("Unsupported object type during JSON conversion: " + obj.getClass());
            return JSONNull.getInstance();
        }
    }

    public static Object fromString(String json) {
        JSONValue value = JSONParser.parseStrict(json);
        return fromJSONValue(value);
    }

    public static Object fromJSONValue(JSONValue value) {
        if (value instanceof JSONArray) {
            JSONArray array = value.isArray();
            List<Object> list = new ArrayList<Object>(array.size());
            for (int i = 0; i < array.size(); i++) {
                Object child = fromJSONValue(array.get(i));
                list.add(child);
            }
            return list;
        } else if (value instanceof JSONObject) {
            JSONObject obj = value.isObject();
            Map<String, Object> map = new HashMap<String, Object>(obj.size());
            for (String key : obj.keySet()) {
                map.put(key, fromJSONValue(obj.get(key)));
            }
            return map;
        } else if (value instanceof JSONString) {
            return value.isString();
        } else if (value instanceof JSONNumber) {
            return value.isNumber().doubleValue();
        } else if (value instanceof JSONBoolean) {
            return value.isBoolean().booleanValue();
        } else {
            log("Unsupported object type during conversion from JSON: " + value.getClass());
            return JSONNull.getInstance();
        }
    }

    private static native void log(String message) /*-{
        console.log(message);
    }-*/;
}
