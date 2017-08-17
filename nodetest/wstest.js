var _ = require('lodash-node');
var ws = require("nodejs-websocket");

var users = [];

// Scream server example: "hi" -> "HI!!!"
var server = ws.createServer(function (conn) {
    console.log("New connection");

    conn.on("text", function (str) {
      console.log("Received "+str);
      var rcvMsg = JSON.parse(str);

      if( rcvMsg.type == "LOGIN" ) {
        if (_.findIndex(users, { id: rcvMsg.msg }) !== -1) {
          conn.sendText("login fail : " + rcvMsg.msg);
          return;
        }
        users.push({
          id: rcvMsg.msg
        });
        conn.broadcast("login success : " + rcvMsg.msg);
      }

      // conn.sendText(str.toUpperCase()+"!!!");
    });

    conn.on("login", function (str) {
      console.log("login Received : " + str);

    });

    conn.on("close", function (code, reason) {
      console.log("Connection closed");
    });

}).listen(8001);
