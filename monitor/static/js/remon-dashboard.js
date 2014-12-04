

function RemonDashboard(params) {
    params = params || {};
    this.appList = params.appList || [];
    this.appId = params.appId || '';
    this.graphs = params.graphs || {};
    this.rs = new RemonSocket({ callback: this.callback.bind(this) });
}


RemonDashboard.prototype.addGraph = function(tagName) {
    if (tagName in this.graphs === false) {
        var id = Object.keys(this.graphs).length;
        var graph = new RemonTimeseriesGraph({ id: id, name: tagName });

        var source = $('#template-graph').html();
        var template = Handlebars.compile(source);

        $('#metric-box').append(template(graph));
        graph.draw();
        this.graphs[tagName] = graph;
    }
}


RemonDashboard.prototype.addMetric = function(tagName, value) {
    this.addGraph(tagName);
    var graph = this.graphs[tagName];
    graph.addValue(value);
}


RemonDashboard.prototype.addMessage = function(level, message) {
    var alertType = "";

    switch (level) {
        case "FINEST":
        case "FINER":
        case "FINE":
            alertType = "success";
            break;

        case "CONFIG":
        case "INFO":
            alertType = "info";
            break;
 
        case "WARNING":
            alertType = "warning";
            break;

        case "SEVERE":
            alertType = "error";
            break;

        default:
            alertType = "success";
            break;
    }

    var source = $('#template-message').html();
    var template = Handlebars.compile(source);
    var context = { alertType: alertType, message: message };
    $('#message-box').append(template(context));
}


RemonDashboard.prototype.showAppList = function() {
    var source = $('#template-app-list').html();
    var template = Handlebars.compile(source);
    var context = { apps: this.appList };
    $('#metric-box').html(template(context))
    $('.navbar-brand').text('App List');
}


RemonDashboard.prototype.changeApp = function(appId) {
    $('#metric-box').empty();
    $('#message-box').empty();
    $('.navbar-brand').html('App &raquo; ' + appId);
    this.rs.send({ op: 'unsubscribe', app_id: this.appId });
    this.appId = appId;
    this.graphs = {};
    this.rs.send({ op: 'subscribe', app_id: appId });
    this.rs.send({ op: 'history', app_id: appId });
}


RemonDashboard.prototype.callback = function(data) {
    if (data.op === 'list') {
        this.appList = data.app_list;
        this.showAppList();
    }
    else if (data.op === 'metrics') {
        this.addMetric(data.tag, data.value);
    }
    else if (data.op == 'messages') {
        this.addMessage(data.level, data.message);
    }
    else {
        console.log('Undefined opcode:', op);
    }
}
