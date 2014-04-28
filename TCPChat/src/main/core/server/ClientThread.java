package main.core.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientThread implements Runnable {
	Socket socket;
	
	InputStream input;
	OutputStream output;
	
	Thread run;

	public ClientThread(Socket clientSocket) {
		this.socket = clientSocket;
		initSocketInterface();
		
		run = new Thread(this);
		run.start();
	}
	
	public void run() {
		
	}

	private void initSocketInterface() {
		try {
			this.input = socket.getInputStream();
			this.output = socket.getOutputStream();
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	private void disconnect(){
		try {
			input.close();
			output.close();
			socket.close();
		} 
		catch (IOException e) {e.printStackTrace();}
	}
}
