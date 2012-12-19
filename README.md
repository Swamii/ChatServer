ChatServer

Ping pong:

Server listens -> client connects -> server starts thread -> thread listens until it recieves a non-taken 
nick name from the client, it responds with "NICK:OK", otherwise "NICK:TAKEN" -> thread starts a new
listen-loop where it checks everything sent by the client. If it recieves "END" from client it will end the
loop and terminate the connection (and notify all other clients).
