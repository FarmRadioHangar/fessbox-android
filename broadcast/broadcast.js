var ws = require('nodejs-websocket');

var data = '';

process.stdin.resume();

process.stdin.on('data', function(buf) { 
  data += buf.toString(); 
});

process.stdin.on('end', function() {

  console.log(data);

  var conn = ws.connect('ws://localhost:8001');
  
  conn.on('connect', function() {
    conn.sendText('broadcast!');
    conn.sendText(data);
    conn.close();
  });
  
  conn.on('error', function(error) {
    console.log(error);
  });

});
