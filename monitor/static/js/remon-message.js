Object.defineProperty(Date.prototype, 'toPrettyString', {
    value: function() {
        function pad(n) {
            return (n < 10 ? '0' : '') + n;
        }

        return this.getFullYear() + '-' +
               pad(this.getMonth() + 1) + '-' +
               pad(this.getDate()) + ' ' +
               pad(this.getHours()) + ':' +
               pad(this.getMinutes()) + ':' +
               pad(this.getSeconds());
    }
});


function RemonMessage(params) {
    params = params || {};
    this.sourceId = params.source_id || '';
    this.level = params.level || '';
    this.message = params.message || '';
    this.time = params.time || 0;
    this.levelType = this.getLevelType();
    this.prettyTime = (new Date(this.time * 1000)).toPrettyString();
}


RemonMessage.prototype.getLevelType = function() {
    switch (this.level) {
        case 'FINEST':
        case 'FINER':
        case 'FINE':
            return 'success';

        case 'CONFIG':
        case 'INFO':
            return 'info';
 
        case 'WARNING':
            return 'warning';

        case 'SEVERE':
            return 'error';

        default:
            return 'default';
    }
}

RemonMessage.prototype.draw = function() {
    var source = $('#template-message').html();
    var template = Handlebars.compile(source);
    $('#message-logs').append(template(this));
}
