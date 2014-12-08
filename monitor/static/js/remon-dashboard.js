

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


RemonDashboard.prototype.addMetric = function(metric) {
    this.addGraph(metric.tag);
    var graph = this.graphs[metric.tag];
    graph.addValue(metric.time, metric.value);
}


RemonDashboard.prototype.addMessage = function(message) {
    var levelType = "";

    switch (message.level) {
        case "FINEST":
        case "FINER":
        case "FINE":
            levelType = "success";
            break;

        case "CONFIG":
        case "INFO":
            levelType = "info";
            break;
 
        case "WARNING":
            levelType = "warning";
            break;

        case "SEVERE":
            levelType = "error";
            break;

        default:
            levelType = "success";
            break;
    }

    var source = $('#template-message').html();
    var template = Handlebars.compile(source);
    var context = {
        message: message.message,
        level: message.level,
        levelType: levelType,
    };
    $('#message-logs').append(template(context));
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
    $('#message-logs').empty();
    $('.navbar-brand').html('App &raquo; ' + appId);
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
    else if (data.op === 'insert' || data.op === 'history') {
        for (var i in data.metrics) {
            this.addMetric(data.metrics[i]);
        }
        for (var i in data.messages) {
            this.addMessage(data.messages[i]);
        }
    }
    else {
        console.log('Undefined opcode:', data.op);
    }
}
