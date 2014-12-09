

function RemonGraph(params) {
    params = params || {};
    this.id = params.id || 0;
    this.chartId = 'chart' + this.id;
    this.valueId = 'value' + this.id;
    this.name = params.name || '';
    this.data = params.data || [];
    this.graph = params.graph || null;
    this.maxSize = params.maxSize || 40;
    this.aspectRatio = params.aspectRatio || 0.6;
}


RemonGraph.prototype.draw = function() {
    console.log('RemonGraph is an abstract class.');
}

RemonGraph.prototype.addValue = function() {
    console.log('RemonGraph is an abstract class.');
}


RemonTimeseriesGraph.prototype = Object.create(RemonGraph.prototype);
RemonTimeseriesGraph.prototype.constructor = RemonTimeseriesGraph;

function RemonTimeseriesGraph(params) {
    params = params || {};
    RemonGraph.call(this, params);
}


RemonTimeseriesGraph.prototype.draw = function() {
    var source = $('#template-timeseries-graph').html();
    var template = Handlebars.compile(source);
    $('#metric-box').append(template(this));

    var element = document.getElementById(this.chartId);

    this.graph = new Rickshaw.Graph({
        element: element,
        width: element.offsetWidth,
        height: element.offsetWidth * this.aspectRatio,
        renderer: 'line',
        series: [{ color: 'steelblue', name: this.name, data: this.data }],
    });
    this.graph.render();

    var hoverDetail = new Rickshaw.Graph.HoverDetail({
        graph: this.graph,
        xFormatter: function(x) {
            return (new Date(x * 1000)).toString();
        },
    });

    var xAxis = new Rickshaw.Graph.Axis.Time({
        graph: this.graph,
        timeFixture: new Rickshaw.Fixtures.Time.Local(),
    });
    xAxis.render();
}


RemonTimeseriesGraph.prototype.addValue = function(time, value) {
    this.data.push({ x: time, y: value });
    this.data.sort(function(a, b) {
        return a.x - b.x;
    });
    this.graph.series[0].data = this.data;
    this.graph.update();

    if (this.data[this.data.length - 1].x === time)
        document.getElementById(this.valueId).innerHTML = value;
}


RemonLifecycleGraph.prototype = Object.create(RemonGraph.prototype);
RemonLifecycleGraph.prototype.constructor = RemonLifecycleGraph;

function RemonLifecycleGraph(params) {
    params = params || {};
    this.waitingStartEvents = {};
    this.waitingEndEvents = {};
    RemonGraph.call(this, params);
}

RemonLifecycleGraph.prototype.draw = function() {
    var source = $('#template-lifecycle-graph').html();
    var template = Handlebars.compile(source);
    $('#metric-box').append(template(this));
}

RemonLifecycleGraph.prototype.update = function() {
    var element = document.getElementById(this.chartId);
    var valueId = this.valueId;
    var width = element.offsetWidth;

    this.graph = d3.timeline()
                   .width(width)
                   .stack()
                   .hover(function (d, i, datum) {
                       $('#' + valueId).text(datum.label);
                   })

    $('#' + this.chartId).empty();
    var svg = d3.select('#' + this.chartId)
                .append('svg')
                .attr('width', width)
                .datum(this.data)
                .call(this.graph);
}

RemonLifecycleGraph.prototype.addValue = function(time, tag, type) {
    switch (type) {
        case 'START':
            if (this.waitingEndEvents[tag] !== undefined) {
                var pairedTime = this.waitingEndEvents[tag].shift();
                if (this.waitingEndEvents[tag].length == 0) {
                    delete this.waitingEndEvents[tag];
                }
                this.addTimeline(tag, time, pairedTime);
            } else {
                if (tag in this.waitingStartEvents === false) {
                    this.waitingStartEvents[tag] = [];
                }
                this.waitingStartEvents[tag].push(time);
            }
            break;

        case 'END':
            if (this.waitingStartEvents[tag] !== undefined) {
                var pairedTime = this.waitingStartEvents[tag].shift();
                if (this.waitingStartEvents[tag].length == 0) {
                    delete this.waitingStartEvents[tag];
                }
                this.addTimeline(tag, pairedTime, time);
            } else {
                if (tag in this.waitingEndEvents === false) {
                    this.waitingEndEvents[tag] = [];
                }
                this.waitingEndEvents[tag].push(time);
            }
            break;

        default:
            console.log('Undefined event type', type);
    }
}

RemonLifecycleGraph.prototype.addTimeline = function(tag, startTime, endTime) {
    var found = false;

    for (var i in this.data) {
        if (this.data[i].label === tag) {
            this.data[i].times.push({
                'starting_time': startTime,
                'ending_time': endTime,
            });
            found = true;
        }
    }

    if (!found) {
        this.data.push({
            'label': tag,
            'times': [{
                'starting_time': startTime,
                'ending_time': endTime,
            }],
        });
    }
}
