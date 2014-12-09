

function RemonDashboard(params) {
    params = params || {};
    this.appList = params.appList || [];
    this.appId = params.appId || '';
    this.graphs = params.graphs || {};
    this.rs = new RemonSocket({ callback: this.callback.bind(this) });
}


RemonDashboard.prototype.addMetric = function(metric) {
    if (metric.tag in this.graphs === false) {
        var id = Object.keys(this.graphs).length;
        var graph = new RemonTimeseriesGraph({ id: id, name: metric.tag });
        this.graphs[metric.tag] = graph;
        graph.draw();
    }

    var graph = this.graphs[metric.tag];
    graph.addValue(metric.time, metric.value);
}


RemonDashboard.prototype.addMessage = function(message) {
    var message = new RemonMessage(message);
    message.draw();
}


RemonDashboard.prototype.showAppList = function() {
    var source = $('#template-app-list').html();
    var template = Handlebars.compile(source);
    var context = { apps: this.appList };
    $('#metric-box').html(template(context));
    $('#message-logs').empty();
    $('.navbar-brand').text('ReMon');
}


RemonDashboard.prototype.changeApp = function(appId) {
    $('#metric-box').empty();
    $('#message-logs').empty();
    $('.navbar-brand').html('ReMon &raquo; ' + appId);
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
