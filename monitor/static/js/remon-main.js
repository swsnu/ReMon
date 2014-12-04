var socket = window['WebSocket'] || window['MozWebSocket'];
var dashboard;


$(document).ready(function() {
    if (!socket) {
        console.log('Websocket not supported.');
        return;
    }
    dashboard = new RemonDashboard();
});
