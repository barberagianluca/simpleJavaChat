/*
	DSChat - Version 0.1
	A simple Chat in Java
	Distributed System Project coded by Gianluca Barbera (Matr. 3515987 - UNIGE)
	Comments and JavaDOC are in Italian in this version.
*/

package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import data.ChatRoom;
import data.ChatUser;

/** THE CHAT SERVER OF THE DSChat - {@link Runnable}*/
public class ChatServer
implements Runnable {
	//DEFAULTS
	/** The DEFAULT ChatServer Port */
	public static final int DEFAULT_CHATSERVER_PORT = 4000;
	
	//PROPERTIES
	/** The {@link data.ChatRoom} */
	protected ChatRoom chatRoom;
	/** The list of {@link data.ChatUser}s */
	private Map<String, ChatUser> chatUsers;
	/** Lista di nickname */
	private List<String> nickNamesList;
	/** The Chat Server console */
	private ChatServerConsole csc;
	
	//CONSTRUTORS
	/**
	 * Costruttore, inizializza oggetti
	 * */
	public ChatServer() {
		this.chatRoom = new ChatRoom();
		this.chatUsers = new HashMap<String, ChatUser> ();
		this.csc = new ChatServerConsole();
		this.nickNamesList = new LinkedList<String> ();
	}
	
	//PROPERTIES MANIPULATION
	/**
	 * Aggiunge un nuovo utente nella chat
	 * @param cu {@link data.ChatUser}
	 * */
	synchronized protected void addChatUser(ChatUser cu) {
		this.chatUsers.put(cu.getNickname(), cu);
		this.nickNamesList.add(cu.getNickname());
		
	}
	
	/**
	 * Rimuove un utente dalla chat
	 * @param nickname Utente della Chat
	 */
	synchronized protected void delChatUser(String nickname) {
		try {
			this.chatUsers.remove(nickname);
			this.nickNamesList.remove(nickNamesList.indexOf(nickname));
		} catch(NullPointerException npe) {
			System.err.println("Utente non trovato in chatUsers");
		} catch(IndexOutOfBoundsException ioobe) {
			System.err.println("Nome utente non trovato in nickNamesList");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Dato un nick restituisce l'oggetto {@link data.ChatUser}
	 * @param nickname Nick utente
	 * @return A {@link ChatUser}
	 * */
	synchronized protected ChatUser getChatUser(String nickname) {
		return this.chatUsers.get(nickname);
	}
	
	/** Get ACTIVE Users' Nickname
	 * Ottieni lista di utenti attivi
	 * @return Lista di utenti attivi (connessi sia col sender che con il receiver)
	 *  */
	synchronized protected List<String> getUsersList() {
		List<String> utentiAttivi = new LinkedList<String> ();
		ChatUser cu = null;
		
		for(int i=0; i<this.nickNamesList.size(); i++) {
			cu = chatUsers.get(nickNamesList.get(i));
			if(cu.isReceiver() && cu.isSender())
				utentiAttivi.add(cu.getNickname());
		}
		
		return utentiAttivi;
	}
	
	//METHODS
	/** main, nessun argomento in questa versione 
	 * @param args Ignorato*/
	public static void main(String[] args) {
		ChatServer cs = new ChatServer();
		cs.run();
	}
	
	/** Esecuzione Server */
	@Override
	public void run() {
		ServerSocket ss = null;
		Socket s = null;
		ChatServerServant servant = null;
		
		try {
			ss = new ServerSocket(ChatServer.DEFAULT_CHATSERVER_PORT);
			System.out.println("DSChat Server ATTIVO - In ascolto sulla porta " + ss.getLocalPort());
			(new Thread(csc)).start();
			// System.out.println("In attesa di connessioni..."); //DEBUG_ONLY
			while(true) {
				s = ss.accept();
				// System.out.println("Connessione accettata - Indirizzo " + s.getInetAddress() + "(" + s.getPort() + ")"); //DEBUG_ONLY
				servant = new ChatServerServant(s, this);
				System.out.println("ChatThread " + servant.toString() + " chiamato");
				(new Thread(servant)).start();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				ss.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
	//CONSOLE - EXTRA - BETA VERSION
	private class ChatServerConsole
	implements Runnable {

		@Override
		public void run() {
			int erroriComandi = 0;
			System.out.println("Digita -help per lista comandi Server Console");
			
			InputStreamReader is = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(is);
		  
		    String comando = null;
			while(true) {
				try {
					comando = br.readLine();
					
					if(comando.trim().toLowerCase().equals("-help")) {
						printListaComandi(); erroriComandi=0;
					} else if(comando.trim().toLowerCase().equals("-active")) {
						printUtentiAttivi(); erroriComandi=0;
					} else if(comando.trim().toLowerCase().contains("-info")) {
						printInfoUtente( ((comando.trim().split(" "))[1]) ); erroriComandi=0;
					} else {
						erroriComandi++;
						System.out.println("Comando non valido.");
						if(erroriComandi>=3) {
							System.err.println("(-help per lista comandi)");
						}
					}
					
				} catch (IOException ioe) {
					ioe.printStackTrace();
					System.out.println("Comando non valido. (-help per lista comandi)");
				} catch (Exception e) {
					System.out.println("Comando non valido. (-help per lista comandi)");
				}
			}
		}
		
		/** PRINT - Lista Comandi */
		private void printListaComandi() {
			System.out.println("-help : Stampa questa lista");
			System.out.println("-active : Stampa lista utenti attivi");
			System.out.println("-info <username> : Mostra info utente");
		}
		
		/** PRINT - Lista Utenti Attivi */
		private void printUtentiAttivi() {
			List<String> activeUsers = getUsersList();
			if(activeUsers.size()==0)
				System.out.println("Nessun utente connesso al momento");
			for(int i=0; i<activeUsers.size(); i++) {
				System.out.println(activeUsers.get(i));
			}
		}
		
		/** PRINT - Info Utente
		 * @param nick Nickname utente */
		private void printInfoUtente(String nick) {
			try {
			ChatUser cu = chatUsers.get(nick);
			System.out.println(cu.getNickname() + ") In lista numero " + nickNamesList.indexOf(nick));
			} catch (NullPointerException npe) {
				System.out.println("Utente non trovato");
			}
		}
		
	}

}
