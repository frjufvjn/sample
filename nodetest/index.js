var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);
var _ = require('lodash-node');

var t_module = require('./ws_server');

var users = [];

app.use(function(req, res, next) {
  res.header("Access-Control-Allow-Origin", "*");
  res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
  next();
});

app.get('/', function (req, res){
  res.sendfile('index.html');
});

app.get('/message', function (req, res){
  res.end('res-data-----------------');
});

io.on('connection', function (socket) {
  socket.on('login', function (name) {
    // if this socket is already connected,
    // send a failed login message
    if (_.findIndex(users, { socket: socket.id }) !== -1) {
      socket.emit('login_error', 'You are already connected.');
    }

    // if this name is already registered,
    // send a failed login message
    if (_.findIndex(users, { name: name }) !== -1) {
      socket.emit('login_error', 'This name already exists.');
      return;
    }

    users.push({
      name: name,
      socket: socket.id
    });

    socket.emit('login_successful', _.pluck(users, 'name'));
    socket.broadcast.emit('online', name);

    console.log(name + ' logged in');
  });

  socket.on('sendMessage', function (name, message) {
    console.log('### sendMessage > name : ' +name + ' message :' + JSON.stringify(message));
    var currentUser = _.find(users, { socket: socket.id });
    if (!currentUser) { return; }

    var contact = _.find(users, { name: name });
    if (!contact) { return; }

    io.to(contact.socket)
      .emit('messageReceived', currentUser.name, message);
  });


  // socket.io process 에서 websocket process 모듈을 호출 테스트
  socket.on('message', function (message) {
    console.log('message : '+message);
    var rcvMsg = message;
    io.to(socket.id)
      .emit('message', rcvMsg);


    var ret = t_module.getTest();
    console.log('ret : ' + ret);
  });

  socket.on('disconnect', function () {
    var index = _.findIndex(users, { socket: socket.id });
    if (index !== -1) {
      socket.broadcast.emit('offline', users[index].name);
      console.log(users[index].name + ' disconnected');

      users.splice(index, 1);
    }
  });
});

http.listen(3000, function(){
  console.log('--listening on *:3000');
});
