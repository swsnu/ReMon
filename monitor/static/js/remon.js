var graphs = {};
var lookup = {};
var max_size = 40;

function addGraph(tagName) {
    if (tagName in lookup == false) {
        lookup[tagName] = Object.keys(lookup).length;
        var html = $('\
            <div class="col-sm-6 col-md-4">\
                <div class="panel panel-default">\
                    <div class="panel-heading">' + tagName + '</div>\
                    <div class="panel-body">\
                        <div id="chart' + lookup[tagName] + '" class="chart"></div>\
                    </div>\
                    <div class="panel-footer">\
                        <span id="value' + lookup[tagName] + '" class="value">NaN</span>\
                    </div>\
                </div>\
            </div>\
        ');
        $('#dashboard').append(html);
        var element = document.getElementById('chart' + lookup[tagName]);
        graphs[tagName] = new Rickshaw.Graph({
            element: element,
            width: element.offsetWidth,
            height: element.offsetWidth * 0.6,
            renderer: 'line',
            series: [{ color: 'steelblue', data: [] }],
        });
        graphs[tagName].render();
    }
}

function addValue(tagName, value)
{
    if (tagName in lookup == false)
        addGraph(tagName);
    var data = graphs[tagName].series[0].data;
    data.push({ x: data.length, y: value });
    if (data.length > max_size)
        shift_data(tagName, data.length - max_size);
    graphs[tagName].update();
    $('#value' + lookup[tagName]).text(value);
}

function shift_data(tagName, offset)
{
    var data = graphs[tagName].series[0].data;
    for (var i in data)
        data[i].x -= offset;
    graphs[tagName].series[0].data = data.slice(offset);
}

$(window).load(function() {
    var socket = window['WebSocket'] || window['MozWebSocket'];
    if (!socket) { console.log('Websocket not supported.'); return; }
    var port = document.location.port;
    var address = document.location.hostname + (port.length == 0 ? '' : ':' + port);
    var websocket = new socket('ws://' + address + '/websocket');
    websocket.onopen = function() {
        console.log('Websocket opened.');
        websocket.send(JSON.stringify({op: 'register'}));
        websocket.send(JSON.stringify({op: 'history'}));
    }
    websocket.onclose = function() {
        console.log('Websocket closed.');
    }
    websocket.onmessage = function(event) {
        var data = JSON.parse(event.data);
        addValue(data.tag, data.value);
    }
});
