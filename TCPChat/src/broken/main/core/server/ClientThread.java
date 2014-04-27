package broken.main.core.server;

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
		running = true;
		send();
		receive();
		run = new Thread(this);
		run.start();
	}
  
	public void run() {
		toSendData.add("Name: ");
		//outToClient.println("Name: ");

		
		/*try {
			name = inFromClient.readLine();
			System.out.println(name);
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		String temp = null;
		while(temp == null){
			temp = getNextMessage();
			System.out.println("WAITING FOR NAME FROM CLIENT");
		}
		System.out.println(temp);
		System.out.println("GOT IT");
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
		String msg = null;
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
