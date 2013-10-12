// Make sure that the GWT library is loaded
//loadJavaScript('Communicator/Communicator.nocache.js');

var isGWTReady = false;
var communicatorInstances = [];

function Communicator() {
    communicatorInstances.push(this);
}

Communicator.prototype.queue = [];

Communicator.prototype.init = function() {
    console.log('Init! ' + this.queue.length);
};

Communicator.prototype.connect = function(settings) {
    console.log('Connect!');
};

function communicatorReady() {
    console.log('Ready! ' + communicatorInstances.length);
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