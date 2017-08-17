const WebSocket = require('ws');
const uuid = require('node-uuid');
const url = require('url');
const _ = require('lodash-node');
const wss = new WebSocket.Server({ port: 8080 });

var users = [];

wss.on('open', function() {
  console.log("Connected to server");
});

wss.on('connection', function connection(ws) {

  ws.id = uuid.v4();
  ws.send('User Joined.. ws.id: ['+ws.id+']');

  // var location = url.parse(ws.upgradeReq.url, true);
  console.log('location : ' + ws.upgradeReq.url);

  ws.on('message', function incoming(data) {
    console.log('data : ' + data);

    var resMsg = JSON.parse(data);
    switch (resMsg.cmd) {
      case 'register' :
        if (_.findIndex(users, { name: resMsg.clientid }) !== -1) {
          console.log('already login!!');
          ws.terminate();
          return;
        }

        users.push({
          name: resMsg.clientid,
          wsId: ws.id,
          wsObj: ws
        });
        break;

      default :
        break;
    }
    // wss.broadcastWithoutMe(data, ws);
  });

  ws.on('error', function(er) {
    console.log(er);
    deleteUsers(ws.id);
  });

  ws.on('close', function() {
    console.log('Connection closed [ws.id : '+ws.id+']');
    deleteUsers(ws.id);
    // console.log(_.size(users));
  });

});

function deleteUsers(argWsId) {
  var index = _.findIndex(users, { wsId: argWsId });
  if (index !== -1) {
    users.splice(index, 1);
  }
}

// Broadcast to all.
wss.broadcast = function broadcast(data) {
  wss.clients.forEach(function each(client) {
    if (client.readyState === WebSocket.OPEN) {
      client.send(data);
    }
  });
};

// Broadcast to everyone else.
wss.broadcastWithoutMe = function broadcastWithoutMe(data, ws) {
  wss.clients.forEach(function each(client) {
    if (client !== ws && client.readyState === WebSocket.OPEN) {
      console.log(JSON.stringify(client.toString()));
      client.send(data);
    }
  });
};

// socket.io process 에서 websocket process 모듈을 호출 테스트
function moduleTest(id) {
  this.id = id;
}

moduleTest.prototype.getTest = function () {
  console.log('################################');
  return this.id;
}

module.exports = new moduleTest('test');
