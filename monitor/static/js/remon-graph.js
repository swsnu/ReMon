

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
    var element = document.getElementById(this.chartId);

    if (element !== null && this.graph === null) {
        this.graph = new Rickshaw.Graph({
            element: element,
            width: element.offsetWidth,
            height: element.offsetWidth * this.aspectRatio,
            renderer: 'line',
            series: [{ color: 'steelblue', data: this.data }],
        });
        this.graph.render();
    }
}


RemonGraph.prototype.addValue = function(value) {
    console.log('RemonGraph is an abstract class.');
}


RemonTimeseriesGraph.prototype = new RemonGraph();
RemonTimeseriesGraph.prototype.constructor = RemonTimeseriesGraph;

function RemonTimeseriesGraph(params) {
    params = params || {};
    RemonGraph(params);
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


RemonLifecycleGraph.prototype = new RemonGraph();
RemonLifecycleGraph.prototype.constructor = RemonLifecycleGraph;

function RemonLifecycleGraph(params) {
    params = params || {};
    RemonGraph(params);
}
