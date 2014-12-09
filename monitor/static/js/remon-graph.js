

function RemonGraph(params) {
    params = params || {};
    this.idx = params.idx || 0;
    this.chartId = 'chart' + this.idx;
    this.valueId = 'value' + this.idx;
    this.sourceId = params.sourceId || '';
    this.tag = params.tag || '';
    this.name = params.name || '';
    this.data = params.data || [];
    this.graph = params.graph || null;
    this.aspectRatio = params.aspectRatio || 0.6;
}


RemonGraph.prototype.draw = function() {
    var source = $('#template-graph').html();
    var template = Handlebars.compile(source);
    $('#mainbody').append(template(this));

    var element = $('#' + this.chartId);
    this.graph = new Rickshaw.Graph({
        element: element[0],
        width: element.width(),
        height: element.width() * this.aspectRatio,
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


RemonGraph.prototype.update = function() {
    this.graph.update();

    if (this.data.length > 0) {
        var lastIdx = this.data.length - 1;
        var lastValue = this.data[lastIdx].y;
        $('#' + this.valueId).text(lastValue);
    }
}


RemonGraph.prototype.addMetric = function(metric) {
    this.data.push({ x: metric.time, y: metric.value });
    this.data.sort(function(a, b) {
        return a.x - b.x;
    });
    this.graph.series[0].data = this.data;
}
