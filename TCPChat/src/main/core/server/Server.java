package main.core.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable {
	ArrayList<ClientThread> clients;
	
	ServerSocket serverSocket;
	int port;

	Thread serverThread;
	boolean accepting = false;;
	boolean running = false;

	public Server(int port) {
		this.port = port;

		if (openServer())
			System.out.println("Server Opened");
		else
			return;
		
		clients = new ArrayList<ClientThread>();
		
		serverThread = new Thread(this);
		serverThread.start();
	}

	public void run() {
		running = true;
		accepting = true;

		while (running) {
			if (accepting) {
				try {
					Socket clientSocket = this.serverSocket.accept();
					ClientThread thread = new ClientThread(clientSocket);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private boolean openServer() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			// e.printStackTrace();
			System.err.println("Failed to start server");
			System.err.println("Port already in use");
			return false;
		} catch (IllegalArgumentException e) {
			// e.printStackTrace();
			System.err.println("Failed to start server");
			System.err.println("Invalid Port");
			return false;
		}
		return true;
	}
}
