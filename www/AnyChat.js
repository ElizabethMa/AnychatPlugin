var argscheck = require('cordova/argscheck'),
    exec = require('cordova/exec');

var anychatExport = {};

anychatExport.startChat = function(successCallback, errorCallback, options) {
    argscheck.checkArgs('fFO', 'AnyChatPlugin.startChat', arguments);
    options = options || {};

    var serverIp = argscheck.getValue(options.serverIp, "demo.anychat.cn");
    var userName = argscheck.getValue(options.userName, "myname");
    var serverPort = argscheck.getValue(options.serverPort, 8906);
    var roomId = argscheck.getValue(options.roomId, 1);
    var remoteUserid = argscheck.getValue(options.remoteUserid, -1337);
    var loginPassword = argscheck.getValue(options.loginPassword, "");
    var enterroomPassword = argscheck.getValue(options.enterroomPassword, "");
    var args = [serverIp, userName, serverPort, roomId, remoteUserid, loginPassword, enterroomPassword];
    exec(successCallback, errorCallback, "AnyChatPlugin", "startChat", args);
    // XXX: commented out
    //return new CameraPopoverHandle();
};

anychatExport.close = function(successCallback, errorCallback) {
    exec(successCallback, errorCallback, "AnyChatPlugin", "cleanup", []);
};

module.exports = anychatExport;
