package broken.main.core.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Client {
	Socket socket;
	int port;
	BufferedReader inFromServer;
	PrintStream outToServer;
	
	ArrayList<String> fromServerData;
	ArrayList<String> toSendData;
	
	Thread send, receive;
	boolean running = false;
	
	public Client(String ip, int port){
		this.port = port;
		socket = connectToServer(10, ip, this.port);
		createSocketInterface(socket);
		
		toSendData = new ArrayList<String>();
		fromServerData = new ArrayList<String>();
		running = true;
		send();
		receive();
	}
	
	public void sendMessage(String msg){
		toSendData.add(msg);
	}
	
	public void send(){
		send = new Thread(){
			public void run(){
				while(running){
					if(toSendData.size() > 0){
						outToServer.println(toSendData.get(0));
						//System.out.println(toSendData.get(0));
						toSendData.remove(0);
					}
				}
			}
		};send.start();
	}
	
	public void receive(){
		receive = new Thread(){
			public void run(){
				while(running){
					if (socket.isConnected()) {
						try {
							String x = inFromServer.readLine();
							fromServerData.add(x);
						} catch (IOException e) {
							//disconnect();
							//e.printStackTrace();
						}
					}
				}
			}
		};receive.start();
	}
	
	private void createSocketInterface(Socket clientSocket){
		try {
			inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outToServer = new PrintStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Socket connectToServer(int tries, String ip, int port) {
		Socket socket = null;
		InetAddress ipAddr = null;
		
		try {
			ipAddr = InetAddress.getByName(ip);
		} catch (UnknownHostException e2) {e2.printStackTrace();}
		
		int attemptsLeft = tries;
		try{
			socket = new Socket(ipAddr, port);
			System.out.println("Connection Successful");
		}
		catch(IOException e){
			System.out.println("Connection Failed. Attempting connection again");
			try {Thread.sleep(1000);} catch (InterruptedException e1) {e1.printStackTrace();}
			
			if(attemptsLeft > 0)
				return connectToServer(attemptsLeft - 1, ip, port);
			else{
				System.out.println("Connection Failed");
				return null;
			}
		}
		
		return socket;
	}
	
	public String getNextMessage() {
		String msg = null;
		if (fromServerData.size() > 0) {
			msg = fromServerData.get(0);
			fromServerData.remove(0);
		}
		return msg;
	}
}
