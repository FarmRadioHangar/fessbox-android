var ws = require('nodejs-websocket');

var server = ws.createServer(function(conn) {

  var broadcast = false;

  console.log('New connection.');

  conn.on('text', function(str) {
    console.log('Received: ' + str);
    if (str === 'broadcast!') {
      broadcast = true;
    }
    if (broadcast === true) {
      server.connections.forEach(function(conn) {
        conn.sendText(str)
      });
    }
  });

  conn.on('close', function(code, reason) {
    console.log('Connection closed.');
  });

}).listen(8001);
