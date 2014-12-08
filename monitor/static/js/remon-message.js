

function RemonMessage(params) {
    params = params || {};
    this.level = params.level || {};
    this.message = params.message || "";
    this.time = params.time || 0;
}


RemonMessage.prototype.draw = function() {
    var levelType = "";

    switch (this.level) {
        case "FINEST":
        case "FINER":
        case "FINE":
            levelType = "success";
            break;

        case "CONFIG":
        case "INFO":
            levelType = "info";
            break;
 
        case "WARNING":
            levelType = "warning";
            break;

        case "SEVERE":
            levelType = "error";
            break;

        default:
            levelType = "success";
            break;
    }

    var source = $('#template-message').html();
    var template = Handlebars.compile(source);
    var context = {
        message: this.message,
        level: this.level,
        levelType: levelType,
    };
    $('#message-logs').append(template(context));
}
