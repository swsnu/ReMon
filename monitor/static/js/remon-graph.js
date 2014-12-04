

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
    var element = document.getElementById(this.chartId);

    if (element !== null && this.graph === null) {
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
}


RemonTimeseriesGraph.prototype.addValue = function(time, value) {
    this.data.push({ x: time, y: value });
    this.data.sort(function(a, b) {
        return a.x - b.x;
    });
    this.graph.series[0].data = this.data;
    this.graph.update();
    document.getElementById(this.valueId).innerHTML = value;
}


RemonLifecycleGraph.prototype = Object.create(RemonGraph.prototype);
RemonLifecycleGraph.prototype.constructor = RemonLifecycleGraph;

function RemonLifecycleGraph(params) {
    params = params || {};
    RemonGraph.call(this, params);
}
