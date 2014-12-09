

function RemonDashboard() {
    this.appList = [];
    this.appId = '';
    this.graphs = {};
    this.timeline = null;
    this.rs = new RemonSocket({ callback: this.callback.bind(this) });
}


RemonDashboard.prototype.addMetric = function(metric) {
    var name = metric.tag;

    /* Draw new graph if not exist */
    if (name in this.graphs === false) {
        var idx = Object.keys(this.graphs).length;
        var graph = new RemonGraph({ idx: idx, name: name });
        this.graphs[name] = graph;
        graph.draw();
    }

    var graph = this.graphs[name];
    graph.addMetric(metric);
    graph.update();
}


RemonDashboard.prototype.addEvent = function(ev) {
    this.timeline.addEvent(ev);
    this.timeline.update();
}


RemonDashboard.prototype.addMessage = function(message) {
    var message = new RemonMessage(message);
    message.draw();
}


RemonDashboard.prototype.showAppList = function() {
    $('#mainbody').empty();
    $('#message-box').empty();
    $('.navbar-brand').text('ReMon');
    var source = $('#template-app-list').html();
    var template = Handlebars.compile(source);
    $('#mainbody').html(template(this));
}


RemonDashboard.prototype.showApp = function() {
    $('#mainbody').empty();
    $('#message-box').empty();
    $('.navbar-brand').html('ReMon &raquo; ' + this.appId);
    this.graphs = {};
    this.timeline = new RemonTimeline();
    this.timeline.draw();
    this.rs.send({ op: 'history', app_id: this.appId });
}


RemonDashboard.prototype.requestAppList = function() {
    this.rs.send({
        op: 'list',
    });
}


RemonDashboard.prototype.requestSubscribe = function(appId) {
    this.rs.send({
        op: 'subscribe',
        app_id: appId,
    });
}


RemonDashboard.prototype.callback = function(data) {
    if (data.op === 'list') {
        this.appList = data.app_list;
        this.showAppList();
    }
    else if (data.op === 'subscribe') {
        this.appId = data.app_id;
        this.showApp();
    }
    else if (data.op === 'insert' || data.op === 'history') {
        for (var i in data.metrics) {
            this.addMetric(data.metrics[i]);
        }
        for (var i in data.messages) {
            this.addMessage(data.messages[i]);
        }
        for (var i in data.events) {
            this.addEvent(data.events[i]);
        }
    }
    else {
        console.error('Undefined opcode:', data.op);
    }
}
