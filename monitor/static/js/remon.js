
var socket = window['WebSocket'] || window['MozWebSocket'];

function RemonSocket(params) {
    params = params || {};
    this.url = params.url || this.getLocalUrl();
    this.ws = new socket(this.url);
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
}

RemonSocket.prototype.onOpen = function() {
    console.log('Websocket opened.');
    var appId = 'TEST_APP_ID';
    this.send({ op: 'subscribe', app_id: appId });
    this.send({ op: 'history', app_id: appId });
    this.send({ op: 'list' });
}

RemonSocket.prototype.onClose = function() {
    console.log('Websocket closed.');
}

RemonSocket.prototype.onMessage = function(event) {
    var data = JSON.parse(event.data);
    console.log('Received:', data);
    this.callback(data);
}


function RemonGraph(params) {
    params = params || {};
    this.id = params.id || 0;
    this.name = params.name || '';
    this.data = params.data || [];
    this.graph = params.graph || null;
    this.maxSize = params.maxSize || 40;
}

RemonGraph.prototype.getChartId = function() {
    return 'chart' + this.id;
}

RemonGraph.prototype.getValueId = function() {
    return 'value' + this.id;
}

RemonGraph.prototype.makeHTML = function() {
    return $('\
    <div class="col-sm-6 col-md-4">\
        <div class="panel panel-default">\
            <div class="panel-heading">' + this.name + '</div>\
            <div class="panel-body">\
                <div id="' + this.getChartId() + '" class="chart"></div>\
            </div>\
            <div class="panel-footer">\
                <span id="' + this.getValueId() + '" class="value">NaN</span>\
            </div>\
        </div>\
    </div>');
}

RemonGraph.prototype.draw = function() {
    var element = document.getElementById(this.getChartId());
    if (element !== null && this.graph === null) {
        this.graph = new Rickshaw.Graph({
            element: element,
            width: element.offsetWidth,
            height: element.offsetWidth * 0.6,
            renderer: 'line',
            series: [{ color: 'steelblue', data: this.data }],
        });
        this.graph.render();
    }
}

RemonGraph.prototype.addValue = function(value) {
    var idx = this.data.length;
    this.data.push({ x: idx, y: value });
    if (this.data.length > this.maxSize) {
        this.shiftData(this.data.length - this.maxSize);
    }
    this.graph.series[0].data = this.data;
    this.graph.update();
    document.getElementById(this.getValueId()).innerText = value;
}

RemonGraph.prototype.shiftData = function(offset) {
    for (var i in this.data) {
        this.data[i].x -= offset;
    }
    this.data = this.data.slice(offset);
}


function RemonDashboard(params) {
    params = params || {};
    this.appId = params.appId || '';
    this.graphs = params.graphs || {};
}

RemonDashboard.prototype.addGraph = function(tagName) {
    if (tagName in this.graphs === false) {
        var id = Object.keys(this.graphs).length;
        var graph = new RemonGraph({ id: id, name: tagName });
        var html = graph.makeHTML();
        $('#dashboard').append(html);
        graph.draw();
        this.graphs[tagName] = graph;
    }
}

RemonDashboard.prototype.addValue = function(tagName, value) {
    this.addGraph(tagName);
    var graph = this.graphs[tagName];
    graph.addValue(value);
}

RemonDashboard.prototype.callback = function(data) {
    if (data.op === undefined && data.tag !== undefined) {
        this.addValue(data.tag, data.value);
    }
    else if (data.op === 'list') {
    }
}


$(document).ready(function() {
    if (!socket) {
        console.log('Websocket not supported.');
        return;
    }
    var remonDashboard = new RemonDashboard();
    var callback = remonDashboard.callback.bind(remonDashboard);
    var remonSocket = new RemonSocket({ callback: callback });
});
