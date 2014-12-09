

function RemonSocket(params) {
    params = params || {};
    this.url = params.url || this.getLocalUrl();
    this.callback = params.callback || function(e) { console.log(e); };
    this.reconnectIntervalTime = params.reconnectIntervalTime || 1000;
    this.reconnectIntervalId = 0;
    this.status = 'DISCONNECTED';
    this.connect();
}


RemonSocket.prototype.getLocalUrl = function() {
    var host = document.location.hostname;
    var port = document.location.port;
    var address = host + (port.length == 0 ? '' : ':' + port);
    var url = 'ws://' + address + '/websocket';
    return url;
}


RemonSocket.prototype.connect = function() {
    if (this.status == 'DISCONNECTED') {
        console.log('Connecting', this.url);
        this.ws = new WebSocket(this.url);
        this.ws.onopen = this.onOpen.bind(this);
        this.ws.onclose = this.onClose.bind(this);
        this.ws.onmessage = this.onMessage.bind(this);
    }
}


RemonSocket.prototype.send = function(data) {
    var text = JSON.stringify(data);
    this.ws.send(text);
    console.log('Sent:', text);
}


RemonSocket.prototype.onOpen = function() {
    console.log('Websocket opened.');
    this.send({ op: 'list' });
    $('#connection').removeClass('glyphicon-remove');
    $('#connection').addClass('glyphicon-ok');
    $('#loader').hide();
    this.status = 'CONNECTED';

    if (this.reconnectIntervalId > 0) {
        clearInterval(this.reconnectIntervalId);
        this.reconnectIntervalId = 0;
    }
}


RemonSocket.prototype.onClose = function() {
    console.log('Websocket closed.');
    $('#connection').removeClass('glyphicon-ok');
    $('#connection').addClass('glyphicon-remove');
    $('#loader').show();
    this.status = 'DISCONNECTED';

    if (!this.reconnectIntervalId) {
        var self = this;
        this.reconnectIntervalId = setInterval(
            function() {
                console.log('Reconnecting...');
                self.connect();
            },
            this.reconnectIntervalTime
        );
    }
}


RemonSocket.prototype.onMessage = function(event) {
    var data = JSON.parse(event.data);
    console.log('Received:', data);
    this.callback(data);
}
