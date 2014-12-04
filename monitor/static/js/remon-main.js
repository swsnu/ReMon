var dashboard;


$(document).ready(function() {

    if (!window['WebSocket']) {
        console.log('Websocket not supported.');
        return;
    }

    $('#footer').click(function() {
        $(this).toggleClass('enlarged');
    });

    dashboard = new RemonDashboard();
});
