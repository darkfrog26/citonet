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
    var f = function() {
        GWTCommunicator.connect(settings);
    };
    this.exec(f);
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
    var script = document.createElement('script');
    script.setAttribute('type', 'text/javascript');
    script.setAttribute('src', filename);
    document.getElementsByTagName('head')[0].appendChild(script);
}