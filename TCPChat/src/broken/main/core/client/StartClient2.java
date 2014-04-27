package broken.main.core.client;

import java.util.Scanner;

public class StartClient2 {
	static Client c;
	
	public static void main(String[] args){
		c = new Client("localhost", 1234);
		while(true){
			String x = c.getNextMessage();
			if(!x.equals("") || x != null)
				System.out.println(x);
		}
	}

	public void send(){
		Thread send = new Thread(){
			public void run(){
				Scanner input = new Scanner(System.in);
				String data = null;
				while(!data.equals("/done")){
					data = input.nextLine();
					
					if(data.equals("/get")){
						System.out.println(c.fromServerData);
					}
					else{
						if(data != null)
							c.sendMessage(data);
					}
				}
				input.close();
			}
		};
		send.start();
	}
}
