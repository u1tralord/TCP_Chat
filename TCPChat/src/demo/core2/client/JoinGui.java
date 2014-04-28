package demo.core2.client;

/*
 * CLIENT GUI - chat program
 */

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class JoinGui extends JFrame
{
	JLabel L_PortBr;
	JLabel L_IP;
	JTextField TF_Port;
	JTextField TF_Poruka;
	JTextField TF_IP;
	JTextField TF_Nick;
	JButton B_Connect;
	JButton B_Disconnect;
	JTextArea TA_Chat;
	JScrollPane SP_Scroll;
	
	static Socket joinSocket;
	static ObjectOutputStream izlaz;
	static ObjectInputStream ulaz;	
	static String poruka;
	static String ip;
	static boolean signal = false;
	static boolean tipka = true;
	
	JoinGui()
	{
		super("JoinGUI");
		setSize(430, 540);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		
		kreirajKomponente();
		podesiScrollBar();
		odrediPozicije();
		postaviKomponente();
		setListener();
		
		setVisible(true);
	}
	
	void kreirajKomponente()
	{
		L_PortBr = new JLabel("Port:");
		TF_Port = new JTextField("56789");
		L_IP = new JLabel("IP:");
		TF_IP = new JTextField("127.0.0.1");
		B_Connect = new JButton("Connect");
		B_Disconnect = new JButton("Disconnect");
		TF_Nick = new JTextField("Nickname");
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
		B_Connect.setBounds(10,40,110,20);
		B_Disconnect.setBounds(130,40,110,20);
		TF_Nick.setBounds(250,40,160,20);
		SP_Scroll.setBounds(10,70,400,400);
		TF_Poruka.setBounds(10,480,400,20);
	}
	
	void postaviKomponente()
	{
		Container kont = getContentPane();
		kont.setLayout(null);
		kont.add(L_PortBr);
		kont.add(TF_Port);
		kont.add(L_IP);
		kont.add(TF_IP);
		kont.add(B_Connect);
		kont.add(B_Disconnect); B_Disconnect.setEnabled(false);
		kont.add(TF_Nick);
		kont.add(SP_Scroll);
		kont.add(TF_Poruka);
	}
	
	//set action listeners to buttons
	void setListener()
	{
		B_Connect.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					int p = 0;
					try 
					{ 
						String port = TF_Port.getText();
						p = Integer.parseInt(port);
						ip = TF_IP.getText();
						joinSocket = new Socket(ip, p); 
						signal = true;
						TA_Chat.append("Pokusavam da se konektujem na remote IP:" + ip + " i port:" + p + "\n");
						TA_Chat.setCaretPosition(TA_Chat.getText().length());
					} 
					catch(Exception ex) 
					{ 
						TA_Chat.append("Error: Server je nedostupan!\n");
						TA_Chat.setCaretPosition(TA_Chat.getText().length());
						signal = false; 
					}
					
					if(signal == true)
					{
						KonektujSe ob1 = new KonektujSe(); 
						B_Connect.setEnabled(false);
						B_Disconnect.setEnabled(true);
						TF_Nick.setEditable(false);
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
					TA_Chat.append("Client je diskonektovan!\n");
					TA_Chat.setCaretPosition(TA_Chat.getText().length());
					try { joinSocket.close(); } catch(Exception ex) { }
					try { ulaz.close(); } catch(Exception ex) { }
					try { izlaz.close(); } catch(Exception ex) { }
					B_Connect.setEnabled(true);
					B_Disconnect.setEnabled(false);
					TF_Nick.setEditable(true);
				}
			}
		);
	}
	
	//send message
	public void posPoruku()
	{
		TF_Poruka.addKeyListener
		(
			new KeyListener()
			{
				public void keyPressed(KeyEvent e)
				{
					int tipka = e.getKeyCode();
					if(tipka == KeyEvent.VK_ENTER)
					{
						 String poruka2 = TF_Poruka.getText(); //salji poruku
						 TA_Chat.append(TF_Nick.getText()+ ">" + poruka2 + "\n");
						 TA_Chat.setCaretPosition(TA_Chat.getText().length());
						 posaljiPoruku(TF_Nick.getText()+ ">" + poruka2);  //salji poruku
						 TF_Poruka.setText("");
					}
				}
				public void keyReleased(KeyEvent e) { }
				public void keyTyped(KeyEvent e) { }
			}
		);
	}
	
	//send messages
	void posaljiPoruku(String por)
	{
		try 
		{ 
			izlaz.writeObject(por);
			izlaz.flush(); 
		} 
		catch(Exception e) 
		{ }
	}
	
	//connect to server
	class KonektujSe implements Runnable
	{
		KonektujSe ob1;
		Thread t;

		KonektujSe()
		{
			t = new Thread(this, "RunServer");
			t.start();
		}
		
		public void run()
		{
			try
			{
				try
				{
					izlaz = new ObjectOutputStream(joinSocket.getOutputStream());
					izlaz.flush();
					ulaz = new ObjectInputStream(joinSocket.getInputStream());
					
					poruka = (String) ulaz.readObject(); // prima poruku od severa po prvi put
					TA_Chat.append("<Server>" + poruka + "\n"); // prima poruku od severa
					TA_Chat.setCaretPosition(TA_Chat.getText().length());
					posaljiPoruku(TF_Nick.getText());
					B_Connect.setEnabled(false);
					B_Disconnect.setEnabled(true);
				} 
				catch(Exception e) 
				{ }
			
				PrimajPoruku primaj1 = new PrimajPoruku();
				if(tipka == true)
				{
					posPoruku();
					tipka = false;
				}	
				
			}
			catch (Exception ex) 
			{ }
		}
	}	
	
	//Receive messages
	class PrimajPoruku implements Runnable
	{
		PrimajPoruku ob1;
		Thread t;

		PrimajPoruku()
		{
			t = new Thread(this, "PrimajPoruku");
			t.start();
		}
		
		public void run()
		{
			try
			{
				do
				{	
					try
					{	
						poruka = (String)ulaz.readObject(); //primi poruku
						TA_Chat.append(poruka + "\n");
						
						/* This part (if - else if) of the code was done a little lame, but it works as it should
						when someone read message log in our chat, 
						and scrollbar is for ex. in middle of text area, 
						we don't want our scrollbar jump to bottom of text area,
						when we receive new message
						+16px appears sometimes I do not know why yet, but it does't hurt 
						so our scrollbar will be auto-scrolling to bottom, 
						when current location of scrollbar is more then maximum size or equal to size of our text area
						 */
						if( SP_Scroll.getVerticalScrollBar().getValue() >= (SP_Scroll.getVerticalScrollBar().getMaximum() - SP_Scroll.getVerticalScrollBar().getVisibleAmount() ))
							TA_Chat.setCaretPosition(TA_Chat.getText().length());
						else if((SP_Scroll.getVerticalScrollBar().getValue() + 16) >= (SP_Scroll.getVerticalScrollBar().getMaximum() - SP_Scroll.getVerticalScrollBar().getVisibleAmount() ))
							TA_Chat.setCaretPosition(TA_Chat.getText().length());
					} 
					catch(Exception ex) 
					{ 
						TA_Chat.append("Server je diskonektovan, gasim vezu !!!\n");
						TA_Chat.setCaretPosition(TA_Chat.getText().length());
						poruka = "cao"; 
						try
						{
							ulaz.close();
							izlaz.close();
							joinSocket.close();
						} catch(Exception ex2) { }
						
						B_Connect.setEnabled(true);
						B_Disconnect.setEnabled(false);
					}
				} while(!poruka.equalsIgnoreCase("cao"));
			} catch (Exception ex) { }
		}
	}
	
	//main
	public static void main(String args[])
	{
		SwingUtilities.invokeLater
		(
			new Runnable()
			{
				public void run()
				{
					new JoinGui();
				}
			}
		);
	}
}
