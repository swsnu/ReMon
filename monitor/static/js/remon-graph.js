

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
    RemonGraph.call(this, params);
}

RemonLifecycleGraph.prototype.draw = function() {
    var source = $('#template-lifecycle-graph').html();
    var template = Handlebars.compile(source);
    $('#metric-box').append(template(this));

    var element = document.getElementById(this.chartId);

    this.graph = d3.timeline();

    var testData = [
        {label: "person a", times: [
              {"starting_time": 1355752800000, "ending_time": 1355759900000},
              {"starting_time": 1355767900000, "ending_time": 1355774400000}]},
        {label: "person b", times: [
              {"starting_time": 1355759910000, "ending_time": 1355761900000}]},
        {label: "person c", times: [
              {"starting_time": 1355761910000, "ending_time": 1355763910000}]},
    ];

    var svg = d3.select('#' + this.chartId).append('svg').attr('width', 500).datum(testData).call(chart);
}

RemonLifecycleGraph.prototype.addValue = function(time, tag, type) {
    /* FIXME */
    console.log('Lifecycle.addValue', time, tag, type);
}


RemonHistogramGraph.prototype = Object.create(RemonGraph.prototype);
RemonHistogramGraph.prototype.constructor = RemonHistogramGraph;

function RemonHistogramGraph(params) {
    params = params || {};
    RemonGraph.call(this, params);
}

RemonHistogramGraph.prototype.draw = function() {
    var source = $('#template-histogram-graph').html();
    var template = Handlebars.compile(source);
    $('#metric-box').append(template(this));

    var element = document.getElementById(this.chartId);

    if (element != null && this.graph == null) {
        this.graph = new Rickshaw.Graph({
            element: element,
            width: element.offsetWidth,
            height: element.offsetWidth * this.aspectRatio,
            renderer: 'bar',
            series: [{
                name: this.name,
                data: this.data,
                color: 'steelblue'
            }],
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
}

RemonHistogramGraph.prototype.addValue = function(time, value) {
    this.data.push({ x: time, y: value });
    this.data.sort(function(a, b) {
        return a.x - b.x;
    });
    this.graph.series[0].data = this.data;
    this.graph.update();
    document.getElementById(this.valueId).innerHTML = value;
}
