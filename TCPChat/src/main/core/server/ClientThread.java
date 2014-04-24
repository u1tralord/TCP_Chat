package main.core.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientThread implements Runnable{
	Socket socket;
	BufferedReader inFromClient;
	PrintStream outToClient;
	
	ArrayList<String> toSendData;
	ArrayList<String> fromClientData;

	Thread run, receive, send;
	boolean running = false;
	
	public ClientThread(Socket clientSocket){
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
		receive();
	}
	
	public void receive(){
		receive = new Thread(){
			public void run(){
				while(running){
					try {
						fromClientData.add(inFromClient.readLine());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};receive.start();
	}
	
	public String getNextMessage() {
		String msg = "";
		if(fromClientData.size() > 0){
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
}
