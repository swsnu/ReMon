var graphs = {};
var lookup = {};

function addGraph(tagName) {
    if (tagName in lookup == false) {
        lookup[tagName] = Object.keys(lookup).length;
        var html = $('\
            <div class="col-sm-6 col-md-4">\
                <div class="panel panel-default">\
                    <div class="panel-heading">' + tagName + '</div>\
                    <div class="panel-body">\
                        <div id="chart' + lookup[tagName] + '" class="chart"></div>\
                    </div>\
                    <div class="panel-footer">\
                        <span id="value' + lookup[tagName] + '" class="value">NaN</span>\
                    </div>\
                </div>\
            </div>\
        ');
        $('#dashboard').append(html);
        var element = document.getElementById('chart' + lookup[tagName]);
        graphs[tagName] = new Rickshaw.Graph({
            element: element,
            width: element.offsetWidth,
            height: element.offsetWidth * 0.6,
            renderer: 'line',
            series: [{ color: 'steelblue', data: [] }],
        });
        graphs[tagName].render();
    }
}

function addValue(tagName, value)
{
    if (tagName in lookup == false)
        addGraph(tagName);
    var data = graphs[tagName].series[0].data;
    data.push({ x: data.length, y: value });
    graphs[tagName].update();
    $('#value' + lookup[tagName]).text(value);
}
