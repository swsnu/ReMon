

function RemonGraph(params) {
    params = params || {};
    this.id = params.id || 0;
    this.chartId = 'chart' + this.id;
    this.valueId = 'value' + this.id;
    this.name = params.name || '';
    this.data = params.data || [];
    this.graph = params.graph || null;
    this.maxSize = params.maxSize || 40;
}


RemonGraph.prototype.draw = function() {
    var element = document.getElementById(this.chartId);
    if (element !== null && this.graph === null) {
        this.graph = new Rickshaw.Graph({
            element: element,
            width: element.offsetWidth,
            height: element.offsetWidth * 0.6,
            renderer: 'line',
            series: [{ color: 'steelblue', data: this.data }],
        });
        this.graph.render();
    }
}


RemonGraph.prototype.addValue = function(value) {
    var idx = this.data.length;
    this.data.push({ x: idx, y: value });
    if (this.data.length > this.maxSize) {
        this.shiftData(this.data.length - this.maxSize);
    }
    this.graph.series[0].data = this.data;
    this.graph.update();
    document.getElementById(this.valueId).innerHTML = value;
}


RemonGraph.prototype.shiftData = function(offset) {
    for (var i in this.data) {
        this.data[i].x -= offset;
    }
    this.data = this.data.slice(offset);
}
