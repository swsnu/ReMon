

function RemonTimeline() {
    this.data = [];
    this.savedStart = {};
    this.colors = d3.scale.category10();
}


RemonTimeline.prototype.draw = function() {
    var source = $('#template-timeline').html();
    var template = Handlebars.compile(source);
    $('#mainbody').append(template(this));
}


RemonTimeline.prototype.update = function() {
    var width = $('#timeline').width();
    var graph = d3.timeline()
                  .width(width)
                  .stack()
                  .margin({ left: 150, right: 0, top: 0, bottom: 0 })
                  .hover(function (d, i, datum) {
                      var name = datum.label + '#' + d.tag;
                      $('#timeline-text').text(name);
                  })

    if (this.data.length > 0) {
        $('#timeline').empty();
        var svg = d3.select('#timeline')
                    .append('svg')
                    .attr('width', width)
                    .datum(this.data)
                    .call(graph);
    }
}


RemonTimeline.prototype.addEvent = function(ev) {
    switch (ev.type) {
        case 'START':
            this.savedStart[ev.source_id] = ev.time;
            break;

        case 'END':
            if (this.savedStart[ev.source_id] !== undefined) {
                var startTime = this.savedStart[ev.source_id];
                this.addEventLine(ev.source_id, ev.tag, startTime, ev.time);
                delete this.savedStart[ev.source_id];
            }
            break;

        default:
            console.error('Undefined event type:', ev.type);
    }
}


RemonTimeline.prototype.addEventLine = function(sourceId, tag, startTime, endTime) {
    var line = {
        'tag': tag,
        'starting_time': startTime,
        'ending_time': endTime,
    };

    var found = false;
    for (var i in this.data) {
        if (this.data[i].label === sourceId) {
            var idx = this.data[i].times.length;
            line.color = this.colors(idx + 1);
            this.data[i].times.push(line);
            found = true;
        }
    }

    if (!found) {
        line.color = this.colors(0);
        this.data.push({
            'label': sourceId,
            'times': [line],
        });
    }
}
