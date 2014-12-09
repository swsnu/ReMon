

function RemonSocket(params) {
    params = params || {};
    this.url = params.url || this.getLocalUrl();
    this.ws = new WebSocket(this.url);
    this.ws.onopen = this.onOpen.bind(this);
    this.ws.onclose = this.onClose.bind(this);
    this.ws.onmessage = this.onMessage.bind(this);
    this.callback = params.callback || function(e) { console.log(e); };
}


RemonSocket.prototype.getLocalUrl = function() {
    var host = document.location.hostname;
    var port = document.location.port;
    var address = host + (port.length == 0 ? '' : ':' + port);
    var url = 'ws://' + address + '/websocket';
    return url;
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
}


RemonSocket.prototype.onClose = function() {
    console.log('Websocket closed.');
    $('#connection').removeClass('glyphicon-ok');
    $('#connection').addClass('glyphicon-remove');
    $('#loader').show();
}


RemonSocket.prototype.onMessage = function(event) {
    var data = JSON.parse(event.data);
    console.log('Received:', data);
    this.callback(data);
}
