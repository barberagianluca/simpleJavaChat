/*
	DSChat - Version 0.1
	A simple Chat in Java
	Distributed System Project coded by Gianluca Barbera (Matr. 3515987 - UNIGE)
	Comments and JavaDOC are in Italian in this version.
*/

package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import data.ChatMessage;
import data.ChatRequest;
import data.ChatResponse;
import server.ChatServer;

/** Una delle due facce del Client - Il Sender si occupa di inviare al server le richieste e/o i messaggi dell'utente. */
public class ChatClientSender {
	/** Tipo di ChatClient - Sender */
	public static final String clientType = "SENDER";
	
	/** Si occupa di connettersi al Server
	 * @param args <br>
	 * [0] = Indirizzo IP Server; <br>
	 * [1] = Username (Primo tentativo)
	 *  */
	public static void main(String[] args) {
		//Variabili
		String nickname=null;
		
		//Stream e Buffer
		InputStream inputStream = null;
		OutputStream outputStream = null;
		InputStreamReader inputStreamReader = null;
		OutputStreamWriter outputStreamWriter = null;
		BufferedReader bufferedReader = null;
		BufferedWriter bufferedWriter = null;
		ObjectInputStream objectInputStream = null;
		ObjectOutputStream objectOutputStream = null;
		
		//CONVALIDA ARGOMENTI
		if(args.length<1) {
			System.err.println("Parametro mancante: SERVER_IP_ADDR - Local Server? Try 127.0.0.1");
			return;
		}

		//PRIMA IMPOSTAZIONE DEL NICKNAME
		if(args.length>1) {
			nickname = args[1];	//Posso passarlo per argomento oppure leggerlo
		} else {
			System.out.println("Inserisci un nickname");
			InputStreamReader is = new InputStreamReader(System.in);
			bufferedReader = new BufferedReader(is);
		  
		    nickname = null;
			while(true) {
				try {
					nickname = bufferedReader.readLine();
					if(!nickname.equals(""))	//Testa se nickname valido...
						break;	
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		
		//AVVIA CONNESSIONE COL SERVER
		String ipAddr = args[0];
		InetSocketAddress addr = new InetSocketAddress(ipAddr, ChatServer.DEFAULT_CHATSERVER_PORT);
		Socket s = new Socket();

		try {
			s.connect(addr);
			System.out.println("Connesso sul server (Porta " + s.getPort() + ")");
			
			//Apri Stream e Buffer - Prima Output / Writer
			outputStream = s.getOutputStream();
			inputStream = s.getInputStream();
			
			outputStreamWriter = new OutputStreamWriter(outputStream);
			inputStreamReader = new InputStreamReader(inputStream);
			
			bufferedWriter = new BufferedWriter(outputStreamWriter);
			bufferedReader = new BufferedReader(inputStreamReader);		
			
			// System.out.println("Tentativo di registrazione nickname..."); //DEBUG_ONLY
			//REGISTRA NICKNAME
			while(true) {
				if(registraNickname(nickname, bufferedWriter, bufferedReader))
					break;
				else {
					System.err.println("Nickname già presente o non valido, inserirne un altro!");
					nickname = null;
					InputStreamReader is = new InputStreamReader(System.in);
					BufferedReader retryBufferedReader = new BufferedReader(is);
					while(true) {
						try {
							nickname = retryBufferedReader.readLine();
							if(!nickname.equals(""))	//Testa se nickname valido...
								break;	
						} catch(IOException ioe) {
							ioe.printStackTrace();
						}
					}
				}
			}
			System.out.println("Nickname registrato.");
						
			//FUNZIONALITA' SPECIFICHE SENDER
			//Apro objectStream Output e Input
			objectOutputStream = new ObjectOutputStream(outputStream);
	        objectInputStream = new ObjectInputStream(inputStream);
			
			//Stampa menu e consenti scelta opzioni...
			InputStreamReader isrMenu = new InputStreamReader(System.in);
			BufferedReader brMenu = new BufferedReader(isrMenu);
			String scelta = null;
			int opzione;
			boolean logOut = false;
			while(true) {
				stampaMenu();
				scelta = brMenu.readLine();
				try {
					opzione = Integer.parseInt(scelta);
					switch(opzione) {
						case 1: stampaListaUtenti(objectOutputStream, objectInputStream, true); break;
						case 2: scriviMessaggi(nickname, objectOutputStream, objectInputStream); break;
						case 3: logOut = true; break;
						default: System.err.println("Inserire un valore numerico intero presente nel menu!");
					}
					
					if(logOut) break;
					
				} catch (NumberFormatException nfe) {
					System.err.println("Inserire un valore numerico intero presente nel menu!");
				}
			}
			
			//procedura di logout! 
			senderLogout(objectOutputStream, objectInputStream, nickname);
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	/**
	 * Clean logout
	 * @param oos L'{@link ObjectOutputStream}
	 * @param ois L'{@link ObjectInputStream}
	 * @param nickname Il nick che abbandona la chat
	 * @throws IOException Se fallisce lancia un'eccezione
	 * */
	private static void senderLogout(ObjectOutputStream oos, ObjectInputStream ois, String nickname) throws IOException {
		//Invialo sul server
		ChatRequest cr = new ChatRequest(ChatRequest.LOGOUT, nickname);
		oos.writeObject(cr);
		oos.flush();
		
		//Attendi risposta
		ChatResponse chatResponse = null;
		try {
			chatResponse = (ChatResponse) ois.readObject();
			if(chatResponse.getResponseCode() == ChatResponse.RESPONSE_ERROR)
				System.err.println("-- ERRORE MESSAGGIO NON INVIATO! --"); //Debug only...
			else
				System.out.println("LOGOUT AVVENUTO.");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	/** Tenta di registrare il nickname dato sulla comunicazione stabilita 
	 * @param nick Nickname da registrare
	 * @param bw Il {@link BufferedWriter} sul quale scrivere
	 * @param br Il {@link BufferedReader} sul quale leggere
	 * @return
	 * 		<b>true</b> se il nick è stato registrato con successo <br>
	 * 		<b>false</b> altrimenti
	 * @throws IOException Se fallisce lancia un'eccezione
	 * */
	private static boolean registraNickname(String nick, BufferedWriter bw, BufferedReader br) throws IOException {
		String msgReceived = null;
		while(true) {
			//INVIA INFO SENDER 
			// System.out.println("Invio clientType"); //DEBUG_ONLY
			bw.write(ChatClientSender.clientType);
			bw.newLine();
			bw.flush();
			
			//ATTENDI ACK
			// System.out.println("Attendo ACK"); //DEBUG_ONLY
			msgReceived = br.readLine();
			if(!msgReceived.trim().toUpperCase().equals("ACK"))	//INFO SENDER NON RICEVUTA, RITRASMETTI...
				continue;
			
			//INVIA NICK
			// System.out.println("Invio nick"); //DEBUG_ONLY
			bw.write(nick);
			bw.newLine();
			bw.flush();
		
			//ATTENDI ACK o NACK
			// System.out.println("Attendo risposta..."); //DEBUG_ONLY
			msgReceived = br.readLine();
			if(!(msgReceived.trim().toUpperCase().equals("ACK") || msgReceived.trim().toUpperCase().equals("NACK")))	//INFO NICK NON RICEVUTA, RITRASMETTI...
				continue;
			
			break;
		}
		
		// System.out.println("Ricevuto " + msgReceived); //DEBUG_ONLY
		
		if(msgReceived.trim().toUpperCase().equals("ACK"))
			return true;
		else
			return false;
	}

	/** Stampa menu console */
	private static void stampaMenu() {
		System.out.println("-- SENDER MENU --");
		System.out.println("1. Stampa lista utenti");
		System.out.println("2. Invia messaggi");
		System.out.println("3. Logout");
	}
	
	/** Richiede la stampa della lista degli utenti attivi (Vengono prelevati soltanto i nicknames)
	 * @param oos Il canale {@link ObjectOutputStream} attivo
	 * @param ois Il canale {@link ObjectInputStream} attivo
	 * @param verbose true se si desidera la stampa a schermo, false se si utilizzerà la lista in output
	 * @return Una lista degli utenti attivi
	 * @throws IOException Se fallisce lancia un'eccezione
	 * */
	@SuppressWarnings("unchecked")
	private static List<String> stampaListaUtenti(ObjectOutputStream oos, ObjectInputStream ois, boolean verbose) throws IOException {
		//Crea richiesta
		ChatRequest cr = new ChatRequest(ChatRequest.GET_CHATUSERS_LIST, null);
		//INVIA RICHIESTA
		oos.writeObject(cr);
		oos.flush();
		
		ChatResponse chatResponse = null;
		LinkedList<String> chatUserNickList = null;
		//ATTENDI RISPOSTA
		try {
			chatResponse = (ChatResponse) ois.readObject();
			if(chatResponse.getResponseCode() != ChatResponse.RESPONSE_ERROR)
					chatUserNickList = (LinkedList<String>) chatResponse.getParam();
			else 
				throw(new ClassNotFoundException(chatResponse.getError()));
			
		} catch (ClassNotFoundException e) {
			System.err.println("Impossibile recuperare la lista utenti. Risposta del server errata.");
			e.printStackTrace();
			return null;
		}
		
		List<String> activeUser = new LinkedList<String>();
		String user;
		if(verbose) System.out.println("Lista utenti attivi.");
		for(int i=0; i<chatUserNickList.size(); i++) {
			user = chatUserNickList.get(i);
			if(verbose) System.out.println((i+1)+") " + user);
			activeUser.add(user);
		}
		
		if(activeUser.size()==0 && verbose) System.out.println("NESSUN UTENTE CONNESSO AL MOMENTO!");
		
		return activeUser;
	}
	
	/** Gestisce l'invio di messaggi sulla chat... 
	 * @param nickname Il nickname della chat
	 * @param oos {@link ObjectOutputStream} necessaria per inviare {@link data.ChatRequest} conententi {@link data.ChatMessage}
	 * @param ois {@link ObjectInputStream} necessaria per ricevere {@link data.ChatResponse}
	 * @throws IOException Se fallisce lancia un'eccezione
	 * */
	private static void scriviMessaggi(String nickname, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
		
		System.out.println("Digita un messaggio e premi invio per inviarlo nella chat room");
		System.out.println("Per uscire digita /quit");
		System.out.println("Per inviare un messaggio privato anteponi al messaggio @nicknameDestinatario seguito da uno spazio");
		
		//Chiedi all'utente di inserire un messaggio
		//Stampa menu e consenti scelta opzioni...
		InputStreamReader scriviMessaggiISR = new InputStreamReader(System.in);
		BufferedReader scriviMessaggiBR = new BufferedReader(scriviMessaggiISR);
		String corpoMessaggio = null;
		String destinatario = null;
		ChatMessage cm = null;
		while(true) {
			corpoMessaggio = scriviMessaggiBR.readLine();
			
			//Gestisci uscita dalla modalità di inserimento
			if(corpoMessaggio.toLowerCase().startsWith("/quit")) {
				break;
			}
			
			//Verifica sempre che l'utente sia attivo = abbia connesso anche il receiver!!
			if(!isActive(oos, ois, nickname)) {
				System.err.println("Impossibile inviare messaggi, receiver non connesso!");
				continue;
			}
			
			if(corpoMessaggio.startsWith("@")) {
				try {
				destinatario = corpoMessaggio.substring( (corpoMessaggio.indexOf("@")+1) , (corpoMessaggio.indexOf(" ")) );
				System.out.println("Invio messaggio privato a " + destinatario); //Debug only
				//gestione destinatario non corretto - Lato server
				corpoMessaggio = new String(corpoMessaggio.substring((corpoMessaggio.indexOf(" ")))); 
				} catch(Exception e) {
					System.err.println("Per inviare un messaggio privato anteponi al messaggio @nicknameDestinatario seguito da uno spazio");
					continue;
				}
			}
			
			
			//Costruisci messaggio
			long time = Calendar.getInstance().getTime().getTime();
			cm = new ChatMessage(time, nickname, destinatario, corpoMessaggio, 0); //il progressivo viene generato una volta inviato
			
			//Invialo sul server
			ChatRequest cr = new ChatRequest(ChatRequest.SEND_MESSAGE, cm);
			oos.writeObject(cr);
			oos.flush();
			
			//Attendi risposta
			ChatResponse chatResponse = null;
			try {
				chatResponse = (ChatResponse) ois.readObject();
				if(chatResponse.getResponseCode() == ChatResponse.RESPONSE_ERROR)
					System.out.println("-- ERRORE MESSAGGIO NON INVIATO! --"); //Debug only...
				
				destinatario = null; //torno di default sulla chat globale e non scrivo al destinatario dell'ultimo messaggio privato...
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
		
		}
		//Ritorna al menù...
	}
	
	/**
	 * Verifica che l'utente sia attivo (abbia collegato il receiver, altrimenti non può inviare messaggi!)
	 * @param oos L'{@link ObjectOutputStream} necessario per la comunicazione col server
	 * @param ois L'{@link ObjectInputStream} necessario per la comunicazione col server
	 * @param nickname Il nickname dell'utente
	 * @return
	 * 		<b>true</b> se utente attivo <br>
	 * 		<b>false</b> altrimenti
	 * @throws IOException Se fallisce lancia un'eccezione
	 * */
	private static boolean isActive(ObjectOutputStream oos, ObjectInputStream ois, String nickname) throws IOException {
		LinkedList<String> active = (LinkedList<String>) stampaListaUtenti(oos, ois, false);
		for(int i=0; i<active.size(); i++) {
			if(active.get(i).equals(nickname))
				return true;
		}
		
		return false;
	}
	
}
