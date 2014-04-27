package broken.main.core.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable {
	int port;
	ServerSocket serverSocket;
	Thread run;
	boolean running = false;
	ArrayList<ClientThread> clients;

	public Server(int port) {
		this.port = port;
		clients = new ArrayList<ClientThread>();
		openServer();

		run = new Thread(this, "Server Main");
	}

	public void start() {
		run.start();
	}

	public void terminate() {
		running = false;
	}

	public void run() {
		running = true;
		receive();
		System.out.println("Server started on port: " + port);
		while (running) {
			Socket clientSocket = null;
			System.out.println("Waiting for client...");
			try {
				clientSocket = serverSocket.accept();
				clients.add(new ClientThread(clientSocket));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		disconnectClients();
	}
	
	
	private void receive(){
		Thread receive = new Thread(){
			public void run(){
				while(running){
					for(ClientThread client: clients){
						if (client != null){
							String msg = client.getNextMessage();
							//msg = client.fromClientData.get(0);
							String name = client.getName();
							
							if(msg != ""){
								Message m = new Message(name, msg);
								//System.out.println(name + ": " + msg);
								System.out.println(m);
								client.sendMessage(msg);
							}
						}
					}
					
					/*for(int i = 0; i < clients.size(); i++){
						ClientThread c = clients.get(i);
						String msg = c.getNextMessage();
						if(msg != "")
							System.out.println("Server: " + msg);
						clients.set(i, c);
					}*/
				}
			}
		};receive.start();
	}

	private void openServer() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			// e.printStackTrace();
			System.out.println("Failed to start server");
			System.out.println("Port already in use");
			return;
		} catch (IllegalArgumentException e) {
			// e.printStackTrace();
			System.out.println("Failed to start server");
			System.out.println("Invalid Port");
			return;
		}
	}

	private void disconnectClients() {
		for (ClientThread client : clients) {
			client.disconnect();
		}

		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
