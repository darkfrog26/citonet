// Make sure that the GWT library is loaded
//loadJavaScript('GWTCommunicator/GWTCommunicator.nocache.js');

var isGWTReady = false;
var communicatorInstances = [];

function Communicator() {
    communicatorInstances.push(this);
}

Communicator.prototype.queue = [];

Communicator.prototype.init = function() {
    // Initialize delayed functions waiting for GWT to init
    for (var index = 0; index < this.queue.length; index++) {
        this.queue[index]();
    }
};

Communicator.prototype.connect = function(settings) {
    console.log('Connect!');
    var delayed = function() {
        GWTCommunicator.connect(settings);
    };
    this.exec(delayed);
};

Communicator.prototype.on = function(event, f) {
    var delayed = function() {
        GWTCommunicator.on(event, f);
    };
    this.exec(delayed);
};

Communicator.prototype.send = function(event, data) {
    var delayed = function() {
        GWTCommunicator.send(event, data);
    };
    this.exec(delayed);
};

Communicator.prototype.exec = function(f) {
    if (isGWTReady) {
        f();
    } else {
        this.queue.push(f);
    }
}

function communicatorReady() {
    isGWTReady = true;
    for (var index = 0; index < communicatorInstances.length; index++) {
        var communicator = communicatorInstances[index];
        communicator.init();
    }
}

function loadJavaScript(filename) {
    var loader = function() {
        var script = document.createElement('script');
        script.setAttribute('type', 'text/javascript');
        script.setAttribute('src', filename);
        document.getElementsByTagName('body')[0].appendChild(script);
    };

    if (document.readyState == 'complete' || document.readyState == 'interactive') {        // Page loaded
        loader();
    } else {
        var previous = window.onload;
        var f = loader;
        if (previous) {
            f = function() {
                previous();
                loader();
            }
        }
        window.onload = f;
    }
}