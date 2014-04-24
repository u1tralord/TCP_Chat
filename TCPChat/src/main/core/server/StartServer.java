package main.core.server;

public class StartServer {
	public static void main(String[] args){
		Server s = new Server(1234);
		s.start();
	}
}
