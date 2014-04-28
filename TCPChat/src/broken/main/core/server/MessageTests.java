package broken.main.core.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageTests {
	static String[] users = {"John", "Jacob", "Bob"};
	static String alphabet = "abcdefghijklmnopqrstuvwxyz";
	public static void main(String[] args){
		ArrayList<Message> messages = new ArrayList<Message>();
		
		for(int i = 0; i < 10; i++){
			messages.add(new Message(users[(int) (Math.random()*3)], alphabet.substring((int) (Math.random()*26))));
			try {
				Thread.sleep((int) (Math.random()*1000));
			} catch (InterruptedException e) {e.printStackTrace();}
		}
		
		Collections.shuffle((List)messages);
		printArray(messages);
		
		Collections.sort((List)messages);
		printArray(messages);
	}
	
	public static void printArray(ArrayList<Message> msgs){
		System.out.println("<<<--------------------------------");
		for(Message m: msgs){
			System.out.println(m);
		}
		System.out.println("-------------------------------->>>");
	}
}
