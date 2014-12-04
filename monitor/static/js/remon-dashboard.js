

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
        var graph = new RemonGraph({ id: id, name: tagName });

        var source = $('#template-graph').html();
        var template = Handlebars.compile(source);
        var context = {
            name: graph.name,
            chartId: graph.getChartId(),
            valueId: graph.getValueId(),
        };

        $('#metric-box').append(template(context));
        graph.draw();
        this.graphs[tagName] = graph;
    }
}


RemonDashboard.prototype.addValue = function(tagName, value) {
    this.addGraph(tagName);
    var graph = this.graphs[tagName];
    graph.addValue(value);
}


RemonDashboard.prototype.addMessage = function(level, message) {
    console.log(level, message);
}


RemonDashboard.prototype.showAppList = function() {
    var source = $('#template-app-list').html();
    var template = Handlebars.compile(source);
    var context = {apps: this.appList};
    $('#metric-box').html(template(context));
}


RemonDashboard.prototype.changeApp = function(appId) {
    $('#metric-box').empty();
    this.rs.send({ op: 'unsubscribe', app_id: this.appId });
    this.appId = appId;
    this.graphs = {};
    this.rs.send({ op: 'subscribe', app_id: appId });
    this.rs.send({ op: 'history', app_id: appId });
}


RemonDashboard.prototype.callback = function(data) {
    if (data.op === 'metrics') {
        this.addValue(data.tag, data.value);
    }
    else if (data.op == 'messages') {
        this.addMessage(data.level, data.message);
    }
    else if (data.op === 'list') {
        this.appList = data.app_list;
        this.showAppList();
    }
}
