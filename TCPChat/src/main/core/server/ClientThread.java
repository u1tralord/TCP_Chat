package main.core.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientThread implements Runnable {
	Socket socket;
	BufferedReader inFromClient;
	PrintStream outToClient;

	ArrayList<String> toSendData;
	ArrayList<String> fromClientData;

	Thread run, receive, send;
	boolean running = false;
	String name;

	public ClientThread(Socket clientSocket) {
		this.socket = clientSocket;
		createSocketInterface(socket);
		toSendData = new ArrayList<String>();
		fromClientData = new ArrayList<String>();

		System.out.println("Client Connected");
		run = new Thread(this, "Client Thread");
		run.start();
	}

	public void run() {
		running = true;
		outToClient.println("Name: ");

		System.out.println("WAITING FOR NAME FROM CLIENT");
		try {
			name = inFromClient.readLine();
			System.out.println(name);
		} catch (IOException e) {
			e.printStackTrace();
		}
		send();
		receive();
	}
	
	public void send() {
		send = new Thread() {
			public void run() {
				while (running) {
					if(toSendData.size() > 0){
						outToClient.println(toSendData.get(0));
						toSendData.remove(0);
					}
				}
			}
		};send.start();
	}

	public void receive() {
		receive = new Thread() {
			public void run() {
				while (running) {
					if (socket.isConnected()) {
						try {
							fromClientData.add(inFromClient.readLine());
						} catch (IOException e) {
							disconnect();
							//e.printStackTrace();
						}
					}
					else{
						disconnect();
					}
				}
			}
		};
		receive.start();
	}

	public String getNextMessage() {
		String msg = "";
		if (fromClientData.size() > 0) {
			msg = fromClientData.get(0);
			fromClientData.remove(0);
		}
		return msg;
	}

	public void sendMessage(String msg) {
		toSendData.add(msg);
	}

	private void createSocketInterface(Socket socket) {
		try {
			inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outToClient = new PrintStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void closeDataFlow() {
		try {
			socket.close();
			inFromClient.close();
			outToClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean disconnect() {
		running = false;

		closeDataFlow();

		if (run.isAlive() || send.isAlive() || receive.isAlive())
			return false;
		return true;
	}
	
	public String getName(){
		return this.name;
	}
}
