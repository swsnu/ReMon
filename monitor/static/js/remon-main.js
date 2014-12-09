var dashboard;


$(document).ready(function() {

    if (!window['WebSocket']) {
        console.error('Websocket not supported.');
        return;
    }

    $('#loader').show();
    $('#footer').click(function() {
        $(this).toggleClass('enlarged');
        $('body').toggleClass('enlarged');
    });

    dashboard = new RemonDashboard();
});
