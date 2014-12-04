var dashboard;


$(document).ready(function() {

    if (!window['WebSocket']) {
        console.log('Websocket not supported.');
        return;
    }

    dashboard = new RemonDashboard();
});
