$(function() {
    var graph = [];
    var ngraphs = 6;
    var tv = 500;
    for (var j = 0; j < ngraphs; ++j) {
        var element = document.getElementById('chart' + j);
        graph[j] = new Rickshaw.Graph({
            element: element,
            width: element.offsetWidth,
            height: element.offsetWidth * 0.6,
            renderer: 'line',
            series: new Rickshaw.Series.FixedDuration([{ name: 'one' }], undefined, {
                timeInterval: tv,
                maxDataPoints: 100,
                timeBase: new Date().getTime() / 1000
            }) 
        });
    }
    var i = 0;
    var iv = setInterval(function() {
        for (var j = 0; j < ngraphs; ++j) {
            var data = { one: (Math.sin(i++ / 40) + 4) * (Math.floor(Math.random()*100) + 400) };
            graph[j].series.addData(data);
            graph[j].render();
            document.getElementById('value' + j).innerHTML = data.one;
        }
    }, tv);
});
