package chatserver;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
	
	private final int PORT = 5000;
	private ArrayList<Connection> connections;
	private ArrayList<String> users;
	private boolean usingConnections;
	
	public Server() {
		connections = new ArrayList<Connection>();
		users = new ArrayList<String>();
		usingConnections = false;
		listen();
	}
	
	private void listen() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(PORT);
			while (true) {
				Socket s = serverSocket.accept();
				System.out.println("Accepting connection from " + s.getInetAddress());
				Connection c = new Connection(this, s);
				
				synchronized (connections) {
					while (usingConnections) {}
					usingConnections = true;
					connections.add(c);
					usingConnections = false;
					connections.notifyAll();
				}
				
				Thread t = new Thread(c);
				t.start();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Closing server...");
		}
	}
	
	public void newMessage(String nick, String message) {
		synchronized (connections) {
			while (usingConnections) {}
			usingConnections = true;
			
			for (Connection c : connections) {
				c.sendMessage(String.format("%s: %s", nick, message));
			}
			
			usingConnections = false;
			connections.notifyAll();
		}
	}
	
	public boolean newUser(String nick) {
		synchronized (connections) {
			if (!nick.isEmpty() && !users.contains(nick)) {
				users.add(nick);
				while (usingConnections) {}
				usingConnections = true;
				
				for (Connection c : connections) {
					c.notifyNewUser(nick);
				}
				
				usingConnections = false;
				connections.notifyAll();
				return true;
			}
			return false;
		}
	}
	
}
