

function RemonGraph(params) {
    params = params || {};
    this.id = params.id || 0;
    this.name = params.name || '';
    this.data = params.data || [];
    this.graph = params.graph || null;
    this.maxSize = params.maxSize || 40;
}


RemonGraph.prototype.getChartId = function() {
    return 'chart' + this.id;
}


RemonGraph.prototype.getValueId = function() {
    return 'value' + this.id;
}


RemonGraph.prototype.makeHTML = function() {
    return $('\
    <div class="col-sm-12 col-md-6">\
        <div class="panel panel-default">\
            <div class="panel-heading">' + this.name + '</div>\
            <div class="panel-body">\
                <div id="' + this.getChartId() + '" class="chart"></div>\
            </div>\
            <div class="panel-footer">\
                <span id="' + this.getValueId() + '" class="value">NaN</span>\
            </div>\
        </div>\
    </div>');
}


RemonGraph.prototype.draw = function() {
    var element = document.getElementById(this.getChartId());
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
    $('#' + this.getValueId()).text(value);
}


RemonGraph.prototype.shiftData = function(offset) {
    for (var i in this.data) {
        this.data[i].x -= offset;
    }
    this.data = this.data.slice(offset);
}
