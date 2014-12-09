

function RemonTimeline() {
    this.data = [];
    this.savedStart = {};
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
                      $('#timeline-text').text(datum.label);
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
            this.savedStart[ev.tag] = ev.time;
            break;

        case 'END':
            if (this.savedStart[ev.tag] !== undefined) {
                var startTime = this.savedStart[ev.tag];
                this.addEventLine(ev.tag, startTime, ev.time);
                delete this.savedStart[ev.tag];
            }
            break;

        default:
            console.error('Undefined event type:', ev.type);
    }
}


RemonTimeline.prototype.addEventLine = function(tag, startTime, endTime) {
    var line = {'starting_time': startTime, 'ending_time': endTime};

    var found = false;
    for (var i in this.data) {
        if (this.data[i].label === tag) {
            this.data[i].times.push(line);
            found = true;
        }
    }

    if (!found) {
        this.data.push({
            'label': tag,
            'times': [line],
        });
    }
}
