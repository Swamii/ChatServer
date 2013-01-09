package chatserver;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
	
	private final int PORT = 5000;
	private ArrayList<Connection> connections;
	private ArrayList<String> users;
	
	public Server() {
		connections = new ArrayList<Connection>();
		users = new ArrayList<String>();
		listen();
	}
	
	private void listen() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(PORT);
			// listening for a client wanting to connect
			while (true) {
				Socket s = serverSocket.accept();
				System.out.println("Accepting connection from " + s.getInetAddress());
				
				Connection c = new Connection(this, s);
				connections.add(c);

				Thread t = new Thread(c);
				t.start();
			}
			
		} catch (IOException e) {
			System.err.println("Error in the listen-loop. Watch out!");
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Closing server...");
		}
	}
	
	
	public synchronized void endConnection(Connection connection, String nick) {
		connections.remove(connection);
		users.remove(nick);
		for (Connection c : connections) {
			c.notifyUserLeft(nick);
		}
	}
	
	public synchronized String getUsersString() {
		String usersString = "";
		for (String user : users) {
			usersString = usersString + "," + user;
		}
		return usersString;
	}
	
	
	/**
	 * sends a new message to all clients (including the one who sent the message)
	 * @param nick
	 * @param message
	 */
	public synchronized void newMessage(String nick, String message) {
		for (Connection c : connections) {
			c.sendMessage(String.format("<%s> %s", nick, message));
		}
	}

	/**
	 * returns true if the nick wasn't taken
	 * @param nick
	 * @return true if nick isn't taken
	 */
	public synchronized boolean newUser(String nick) {
		if (nick != null && !nick.isEmpty() && 
							!users.contains(nick) && 
							!nick.contains(":") &&
							!nick.contains(" ")) {
			
			users.add(nick);
			
			for (Connection c : connections) {
				c.notifyNewUser(nick);
			}
			return true;
		}
		return false;
	}
	
}
