$(function() {

    var tv = 250;

    var graph = new Rickshaw.Graph( {
        element: document.getElementById('chart'),
        width: 600,
        height: 400,
        renderer: 'line',
        series: new Rickshaw.Series.FixedDuration([{ name: 'one' }], undefined, {
            timeInterval: tv,
            maxDataPoints: 100,
            timeBase: new Date().getTime() / 1000
        }) 
    } );

    graph.render();

    var i = 0;
    var iv = setInterval( function() {
        
        var data = { one: (Math.sin(i++ / 40) + 4) * (Math.floor(Math.random()*100) + 400) };

        graph.series.addData(data);
        graph.render();

    }, tv );

});
