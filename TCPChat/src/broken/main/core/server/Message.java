package broken.main.core.server;

import java.util.Date;

public class Message implements Comparable<Message>{
	String user, msg, time;
	
	public Message(String user, String msg){
		this.user = user;
		this.msg = msg;
		Date date = new Date();
		time = date.toString().substring(0, 19);
	}
	
	public String toString(){
		return String.format("[%s] %s: %s", time, user, msg);
	}
	
	public int compareTo(Message m){
		return this.time.compareTo(m.time);
	}
}
