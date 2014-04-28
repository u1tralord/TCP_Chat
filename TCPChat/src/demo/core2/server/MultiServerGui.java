package demo.core2.server;

/*
 * i don't know this is right way to do it , but i am beginner in programming, now it's all matter to me that is works :)
 * SERVER GUI - host
 */

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class MultiServerGui extends JFrame
{
	JLabel L_PortBr;
	JLabel L_IP;
	JTextField TF_Port;
	JTextField TF_Poruka;
	JTextField TF_IP;
	JButton B_StartServer;
	JButton B_Disconnect;
	JTextArea TA_Chat;
	JScrollPane SP_Scroll;
	
	static ServerSocket serverSocket; // we need this line to open port on our computer
	static Socket socket;  // and this socket to accept remote connections
	static int clientNiz[] = new int[100]; // array "clientNiz" is unique id of client , and we set 100 clients to allow on server
	// every client have his own unique input and output stream, so we need array to do job
	static ObjectInputStream ulaz[] = new ObjectInputStream[clientNiz.length + 1]; // set input stream to 100 users (we don't use index_0 here, because 0 fuck up everything)
	static ObjectOutputStream izlaz[] = new ObjectOutputStream[clientNiz.length + 1]; // set output stream to 100 users (we don't use here index_0 too)
	static String nicks[] = new String[clientNiz.length + 1];
	static String poruka; // string poruka is for reciving and sending text
	static boolean signal; // signal is true when port is created, so we go to part3
	static boolean start = true; // for enter event , to stop running it twice, if we don't have this, server will send blank messages , when we disconnect and connect
	
	MultiServerGui()
	{
		super("ServerGUI");
		setSize(430, 540);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		
		kreirajKomponente();
		podesiScrollBar();
		odrediPozicije();
		postaviKomponente();
		postaviSlusaoca();
		
		setVisible(true);
	}
	
	void kreirajKomponente()
	{
		L_PortBr = new JLabel("Port:");	
		TF_Port = new JTextField("56789");
		L_IP = new JLabel("IP:");
		TF_IP = new JTextField();
		B_StartServer = new JButton("Start Server");
		B_Disconnect = new JButton("Disconnect");
		TF_Poruka = new JTextField();
	}
	
	void podesiScrollBar()
	{ 
		TA_Chat = new JTextArea(); 
		TA_Chat.setLineWrap(true);
		TA_Chat.setEditable(false);
		SP_Scroll = new JScrollPane(TA_Chat); 
		SP_Scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	}
	
	void odrediPozicije()
	{
		L_PortBr.setBounds(10,10,50,20);
		TF_Port.setBounds(40,10,100,20);
		L_IP.setBounds(150,10,20,20);
		TF_IP.setBounds(170,10,240,20);
		B_StartServer.setBounds(10,40,110,20);
		B_Disconnect.setBounds(130,40,110,20);
		SP_Scroll.setBounds(10,70,400,400);
		TF_Poruka.setBounds(10,480,400,20);
	}
	
	void postaviKomponente()
	{
		// this is old method, that i using to put components on container, this can be replaced with setLayout, like Grid or FlowLayout
		Container kont = getContentPane(); 
		kont.setLayout(null);
		kont.add(L_PortBr);
		kont.add(TF_Port);
		kont.add(L_IP);
		kont.add(TF_IP);
		kont.add(B_StartServer);
		kont.add(B_Disconnect); B_Disconnect.setEnabled(false);
		kont.add(SP_Scroll);
		kont.add(TF_Poruka);
	}
	
	//set action listeners to buttons
	void postaviSlusaoca()
	{
		B_StartServer.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					try 
					{
						//get ip
						InetAddress thisIp = InetAddress.getLocalHost();
						String ip = thisIp.getHostAddress().toString();
						TF_IP.setText(ip);
					} 
					catch(Exception ex) 
					{ TF_IP.setText("Offline!"); }
				
					int p = 0;
					try 
					{ 
						String port = TF_Port.getText();
						p = Integer.parseInt(port);
						TA_Chat.append("Kreiram konekciju na portu " + p + "...\n");
						TA_Chat.setCaretPosition(TA_Chat.getText().length());
						serverSocket = new ServerSocket(p, 100); 
						signal = true;
					} 
					catch(Exception ex) 
					{ 
						TA_Chat.append("Error: Port " + p + " zauzet ili pogresan unos!\n"); 
						TA_Chat.setCaretPosition(TA_Chat.getText().length()); 
						signal = false; 
					}
					
					if(signal == true)
					{
						AcceptNewClients ob1 = new AcceptNewClients("RunServer");
						B_StartServer.setEnabled(false);
						B_Disconnect.setEnabled(true);
					}
				}
			}
		);
		
		B_Disconnect.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					
					TA_Chat.setCaretPosition(TA_Chat.getText().length());
					try 
					{ 
						serverSocket.close(); 
					}
					catch(Exception ex) 
					{ }
					
					for(int i = 0; i < ulaz.length; i++ )
					{
						try
						{ 
							ulaz[i].close(); 
							izlaz[i].close(); 
						} 
						catch(Exception ex) 
						{ }
					}
					TA_Chat.append("Server je diskonektovan!\n");
					B_StartServer.setEnabled(true);
					B_Disconnect.setEnabled(false);
				}
			}
		);
	}
		
	// method where server received message from one client and send it to all other clients
	void posaljiPoruku(int cli, String por) 
	{
		for(int i = 0; i < clientNiz.length; i++) // for loop trying to send message from server to all clients 
		{
			if(clientNiz[i] != 0) // this line stop server to send messages to offline clients (if "clientNiz[X] = 0" don't try to send him message, because that slot is empty)
			{
				if(cli != i+1) // don't repeat messages (when for ex. client_1 send message to all clients, this line stop server to send same message back to client_1)
				{ 
					try
					{
						izlaz[i+1].writeObject(por); 
						izlaz[i+1].flush();
					} 
					catch(Exception e) 
					{ }
				}
			}
		}
	}
	
	// method where Server send messages to all clients
	void posaljiServer(String por) 
	{
		for(int i = 0; i < clientNiz.length; i++) // for loop trying to send message from server to all clients 
		{
			if(clientNiz[i] != 0) // this line stop server to send messages to offline clients
			{
				try
				{
					izlaz[i+1].writeObject(por); 
					izlaz[i+1].flush();
				}
				catch(Exception e) 
				{ }
			}
		}
	}
	
	//pojedinacno slanje i obavestavanje 
	//individually inform the user that is connected on server
	void posaljiJednom(int id, String por) 
	{
		try
		{
			izlaz[id].writeObject(por); 
			izlaz[id].flush();
		} 
		catch(Exception e) 
		{ }
	}
	
	//THREADS SECTION
	public class AcceptNewClients implements Runnable
	{
		Thread t;
		
		AcceptNewClients(String imeNiti)
		{
			t = new Thread(this, imeNiti);
			t.start();
		}
		
		// part 3 - if socket is created wait for new clients to join channel 
		public void run() 
		{  
			while(true)
			{
				try
				{	
					TA_Chat.append("Cekam konekciju...\n");
					TA_Chat.setCaretPosition(TA_Chat.getText().length());
					try 
					{
						socket = serverSocket.accept(); // wait for someone to join on server
					} 
					catch (Exception ex) { break; }
					TA_Chat.append("Konekcija primljena od: " + socket.getInetAddress().getHostName() + "\n");
					TA_Chat.setCaretPosition(TA_Chat.getText().length());
					
					int broj = 0; // we need broj to catch client id
					for(int i = 0; i < clientNiz.length; i++)
					{
						if(clientNiz[i] == 0)  //find first 0 or "empty slot" in array and set client_id number
						{
							clientNiz[i] = i + 1;
							broj = i;
							break; // on first 0 in array, break for-loop
						}
					}
						
					// here we set output and input stream for every client individually
					izlaz[clientNiz[broj]] = new ObjectOutputStream(socket.getOutputStream()); 
					izlaz[clientNiz[broj]].flush();
					ulaz[clientNiz[broj]] = new ObjectInputStream(socket.getInputStream());
						
					TA_Chat.append("Client:[" + clientNiz[broj] +  "] Uspesno konektovan!\n"); // print message to all users that client[id] is connected on server
					TA_Chat.setCaretPosition(TA_Chat.getText().length());
					
					posaljiJednom(clientNiz[broj], "Uspesno si se konektovao na server!"); // send message to client[id] that is connected on server
					nicks[clientNiz[broj]] = (String) ulaz[clientNiz[broj]].readObject(); // recive nick from client
					posaljiServer(nicks[clientNiz[broj]] + " je online sada !!!"); // obavesti sve da se novi klijent konektovao na server
						
					ReceiveMsgs ob2 = new ReceiveMsgs(clientNiz[broj], "StartReceiveMsgs_" + clientNiz[broj]); // make new thread for every new client 
					
				} 
				catch(Exception e) 
				{ 
					e.printStackTrace(); 
				}
						
				// run this Thread only one time, so server can start sending messages to clients when someone is connected, so we need boolean start
				if(start == true) 
				{
					ServerSalje(); // send message Thread (part 4)
					start = false;
				}   
			}
		}
	}
	//part 3
	
	//Server salje poruke svima nit
	//part 4 - Server can type and send messages to all clients
	public void ServerSalje()
	{
		TF_Poruka.addKeyListener(new KeyListener()
		{
			public void keyPressed(KeyEvent e)
			{
				int tipka = e.getKeyCode();
				if(tipka == KeyEvent.VK_ENTER)
				{
					String poruka2 = TF_Poruka.getText(); //salji poruku
					posaljiServer("<Server>" + poruka2);
					TA_Chat.append("<Server>" + poruka2 + "\n");
					TA_Chat.setCaretPosition(TA_Chat.getText().length());
					TF_Poruka.setText("");
				}
			}
			public void keyReleased(KeyEvent e) { }
			public void keyTyped(KeyEvent e) { }
		});
	}
	//part 4
	
	// PRIMAJ PORUKE NIT
	//part 5 - receive message from one client and send to other clients
	public class ReceiveMsgs implements Runnable 
	{
		int C_id;
		ReceiveMsgs ob1;
		Thread t;
		
		ReceiveMsgs(int C_id, String imeNiti)
		{
			this.C_id = C_id; // Receive Client_id to C_id , so we can create new thread with specific id
			t = new Thread(this, imeNiti);
			t.start();
		}
		
		public void run() 
		{ 
			do
			{	
				try
				{	
					poruka = (String) ulaz[C_id].readObject(); //primi poruku - try to receive messages
					TA_Chat.append("<Join_"+ C_id + ">" + poruka + "\n"); // print received message on server
					if(SP_Scroll.getVerticalScrollBar().getValue() >= (SP_Scroll.getVerticalScrollBar().getMaximum() - SP_Scroll.getVerticalScrollBar().getVisibleAmount() ))
						TA_Chat.setCaretPosition(TA_Chat.getText().length());
					else if((SP_Scroll.getVerticalScrollBar().getValue() + 16) >= (SP_Scroll.getVerticalScrollBar().getMaximum() - SP_Scroll.getVerticalScrollBar().getVisibleAmount() ))
						TA_Chat.setCaretPosition(TA_Chat.getText().length());
				} 
				catch(Exception e) { poruka = "cao"; }// server can't receive message from client, set poruka to "cao", exit from do-while loop and then close thread with client
				
				if(poruka.equals("cao")) // if poruka = "cao" , close thread and connection with offline client
				{
					TA_Chat.append("<Join_"+ C_id + "> <Now offline!>\n"); // print on server that client is offline
					if(SP_Scroll.getVerticalScrollBar().getValue() >= (SP_Scroll.getVerticalScrollBar().getMaximum() - SP_Scroll.getVerticalScrollBar().getVisibleAmount() ))
						TA_Chat.setCaretPosition(TA_Chat.getText().length());
					else if((SP_Scroll.getVerticalScrollBar().getValue() + 16) >= (SP_Scroll.getVerticalScrollBar().getMaximum() - SP_Scroll.getVerticalScrollBar().getVisibleAmount() ))
						TA_Chat.setCaretPosition(TA_Chat.getText().length());
					posaljiPoruku(C_id, "<"+ nicks[C_id] + "> is now offline!"); // send to all that client is offline
					
					clientNiz[C_id - 1] = 0; // if someone leave or disconnect, clear that slot and set it to 0 in array, so other clients can join
					nicks[C_id] = ""; // free nick name slot
					try
					{
						ulaz[C_id].close(); // close input connection with client
						izlaz[C_id].close(); // close output connection with client
					} catch(Exception ex) { } 
				}
				// else send message to all users
				else
					posaljiPoruku(C_id, poruka);  //send message to all users
			} while(!poruka.equals("cao"));
		}
	}
	//part 5
	//THREADS SECTION ENDS
	
	public static void main(String args[])
	{
		SwingUtilities.invokeLater
		(
			new Runnable()
			{
				public void run()
				{
					new MultiServerGui();
				}
			}
		);
	}
}
