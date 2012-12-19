package chatserver;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Connection implements Runnable {
	
	Server server;
	Socket socket;
	BufferedWriter writer;
	BufferedReader reader;
	String nick;

	public Connection(Server server, Socket socket) {
		this.server = server;
		this.socket = socket;
	}
	
	@Override
	public void run() {
		init();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("NICK:")) {
					nick = line.split(":")[1];
					if (server.newUser(nick)) {
						write("NICK:OK");
						break;
					} else {
						write("NICK:TAKEN");
					}
				}
			}
			
			while ((line = reader.readLine()) != null) {
				handleMessages(line);
			}
			
		} catch (IOException e) {
			System.err.println("Error reading from client.");
		}
		
	}
	
	private void handleMessages(String line) {
		if (line.startsWith("MESSAGE:")) {
			String message = line.split(":")[1];
			server.newMessage(nick, message);
		}
	}
	
	public void sendMessage(String message) {
		write("MESSAGE:" + message);
	}
	
	private void write(String message) {
		try {
			writer.write(message);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void init() {
		try {
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			System.err.println("Error initializing reader and writer.");
		}
	}
	
	public void notifyNewUser(String nick) {
		write("NEW USER:" + nick);
	}

}
