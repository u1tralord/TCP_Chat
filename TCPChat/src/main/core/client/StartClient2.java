package main.core.client;

import java.util.Scanner;

public class StartClient2 {
	public static void main(String[] args){
		Scanner input = new Scanner(System.in);
		
		Client c = new Client("localhost", 1234);
		String send = "";
		while(!send.equals("/done")){
			send = input.nextLine();
			c.sendMsg(send);
		}
	}
}
