if (!Date.now) {
    Date.now = function now() {
        return new Date().getTime();
    };
}

var Communicate = function(settings) {
    this.settings = settings;
    this.connected = false;
    this.backlog = [];
    this.lastConnected = 0;
    this.lastSent = 0;
    this.lastReceived = 0;

    this.verifyDefault('host', window.location.host);
    this.verifyDefault('path', '/websocket');
    this.verifyDefault('pingDelay', 60000);
    this.verifyDefault('updateFrequency', 10000);
};
Communicate.prototype.listeners = {};
Communicate.prototype.on = function(key, f) {
    if (this.listeners[key] == null) {
        this.listeners[key] = [];
    }
    this.listeners[key].push(f);
};
Communicate.prototype.fire = function(key, evt) {
    if (this.listeners[key]) {
        for (var i = 0; i < this.listeners[key].length; i++) {
            this.listeners[key][i](evt);
        }
    }
};
Communicate.prototype.verifyDefault = function(key, def) {
    if (this.settings == null) {
        this.settings = {};
    }
    if (!this.settings[key]) {
        this.settings[key] = def;
    }
};
Communicate.prototype.setting = function(key, def) {
    if (this.settings && this.settings[key]) {
        return this.settings[key];
    }
    return def;
};
Communicate.prototype.connect = function() {
    var host = this.setting('host');
    var path = this.setting('path');
    var url = 'ws://' + host + path;
    this.socket = new WebSocket(url);
    var c = this;
    this.socket.onopen = function(evt) {
        c.connected = true;
        c.lastConnected = Date.now();
        c.sendBacklog();                        // Clear the backlog before we fire the event
        c.fire('open', evt);
        if (!c.updater) {
            c.update();
        }
    };
    this.socket.onclose = function(evt) {
        c.connected = false;
        c.fire('close', evt);
    };
    this.socket.onmessage = function(evt) {
        c.lastReceived = Date.now();
        c.fire('message', evt);
        if (evt.data.indexOf('::json::') == 0) {
            var obj = JSON.parse(evt.data.substring(8));
            c.fire('json', obj);
        }
    };
    this.socket.onerror = function(evt) {
        c.fire('error', evt);
    };
};
Communicate.prototype.sendBacklog = function() {
    for (var i = 0; i < this.backlog.length; i++) {
        this.send(this.backlog[i]);
    }
    this.backlog = [];
};
Communicate.prototype.send = function(message) {
    if (typeof message != 'string') {
        message = '::json::' + JSON.stringify(message);     // Convert non-strings into JSON strings
    }
    if (this.connected) {
        this.lastSent = Date.now();
        this.socket.send(message);
    } else {
        this.backlog.push(message);
    }
};
Communicate.prototype.update = function() {
    if (this.connected) {
        var now = Date.now();
        var pingDelay = this.setting('pingDelay');
        var lastCommunication = Math.max(this.lastConnected, this.lastSent, this.lastReceived);
        if (now - lastCommunication > pingDelay) {
            this.send('Ping');
        }

        var updateFrequency = this.setting('updateFrequency');
        var c = this;
        this.updater = setTimeout(function() {c.update.call(c); }, updateFrequency);
    } else {
        this.updater = null;
    }
};