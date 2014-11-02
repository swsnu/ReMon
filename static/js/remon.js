var screen_names = ['connect', 'snapshot', 'timeseries', 'logs', 'controls'];

function move_screen(screen_name) {
    screen_names.forEach(function(name) {
        $('#' + name + '-nav').removeClass('active');
        $('#' + name + '-screen').hide();
    });
    $('#' + screen_name + '-nav').addClass('active');
    $('#' + screen_name + '-screen').show();
}

function init() {
    screen_names.forEach(function(name) {
        $('#' + name + '-nav').click(function() { move_screen(name); });
    });
    move_screen('connect');
}

$(function() {
    init();
});
