package com.outr.net.communicator.client;

import com.google.gwt.json.client.JSONValue;

/**
 * Provides pluggable functionality to convert objects to and from JSON.
 *
 * @author Matt Hicks <matt@outr.com>
 */
public interface JSONSupport {
    public JSONValue toJSON(Object obj);

    public Object fromJSON(JSONValue value);
}
