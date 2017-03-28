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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import data.ChatMessage;
import data.ChatRequest;
import data.ChatResponse;
import server.ChatServer;

/** Una delle due facce del Client - Il Receiver si occupa di ricevere informazioni dal server e di mostrarle all'utente. */
public class ChatClientReceiver {
	/** Tipo di ChatClient - Receiver */
	public static final String clientType = "RECEIVER";
	
	/** Si occupa di connettersi al Server
	 * @param args <br>
	 * [0] = Indirizzo IP Server; <br>
	 * [1] = Username (Primo tentativo)
	 *  */
	public static void main(String[] args) {
		//Variabili
		String nickname=null;
		
		//Stream e Buffer utilizzati
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
			
			// System.out.println("Tentativo di registrazione nickname..."); //DEBUG ONLY
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
			
			//Fino a qui tutto ok, riesco a registrare un nickname, fino a qui il codice Receiver = Sender
			//Che poi semplicemente dovrò attendere oggetti ChatResponse e mostrarli...
			objectOutputStream = new ObjectOutputStream(outputStream);
			objectInputStream = new ObjectInputStream(inputStream);
			riceviMessaggi(objectInputStream, objectOutputStream, nickname);
			
			/* INVIA MESSAGGIO AL SERVER PER DISISCRIVERSI DALLA LISTA */
			deregistraNickname(nickname, bufferedWriter, bufferedReader, objectOutputStream);
			
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
	
	/** Invia richiesta di rimozione utente dalla lista iscritti alla chat
	 * @param nickname Nickname da deregistrare
	 * @param bw Il {@link BufferedWriter} sul quale scrivere
	 * @param br Il {@link BufferedReader} sul quale leggere
	 * @param oos L'{@link ObjectOutputStream}
	 * @throws IOException Se fallisce lancia eccezione
	 * */
	private static void deregistraNickname(String nickname, BufferedWriter bw, BufferedReader br, ObjectOutputStream oos) throws IOException {
		
		ChatRequest cReq = new ChatRequest(ChatRequest.LOGOUT, -1);
		oos.writeObject(cReq);
		oos.flush();
		
		//INVIA NICK
		System.out.println("Invio deregistrazione..."); //DEBUG_ONLY
		bw.write(nickname);
		bw.newLine();
		bw.flush();
	
		//ATTENDI ACK o NACK
		// System.out.println("Attendo risposta..."); //DEBUG_ONLY
		String msgReceived = br.readLine();
		
		System.out.println(msgReceived);
		
	}

	/** Tenta di registrare il nickname dato sulla comunicazione stabilita 
	 * @param nick Nickname da registrare
	 * @param bw Il {@link BufferedWriter} sul quale scrivere
	 * @param br Il {@link BufferedReader} sul quale leggere
	 * @throws IOException Se fallisce lancia un'eccezione
	 * @return
	 * 		<b>true</b> se il nick è stato registrato con successo <br>
	 * 		<b>false</b> altrimenti
	 * */
	private static boolean registraNickname(String nick, BufferedWriter bw, BufferedReader br) throws IOException {
		//Variabile per lettura messaggi...
		String msgReceived = null;
		
		while(true) {
			//INVIA INFO RECEIVER 
			// System.out.println("Invio clientType"); //DEBUG_ONLY
			bw.write(ChatClientReceiver.clientType);
			bw.newLine();
			bw.flush();
			
			//ATTENDI ACK
			// System.out.println("Attendo ACK"); //DEBUG_ONLY
			msgReceived = br.readLine();
			if(!msgReceived.trim().toUpperCase().equals("ACK"))	//INFO RECEIVER NON RICEVUTA, RITRASMETTI...
				continue;	//al momento non è gestita una perdita di messaggi migliore...
			
			//INVIA NICK
			// System.out.println("Invio nick"); //DEBUG_ONLY
			bw.write(nick);
			bw.newLine();
			bw.flush();
		
			//ATTENDI ACK o NACK
			// System.out.println("Attendo risposta..."); //DEBUG_ONLY
			msgReceived = br.readLine();
			if(!(msgReceived.trim().toUpperCase().equals("ACK") || msgReceived.trim().toUpperCase().equals("NACK")))	//INFO NICK NON RICEVUTA, RITRASMETTI...
				continue;	//al momento non è gestita una perdita di messaggi migliore...
			
			break;
		}
		
		// System.out.println("Ricevuto " + msgReceived); //DEBUG_ONLY
		
		if(msgReceived.trim().toUpperCase().equals("ACK"))
			return true;
		else
			return false;
	}
	
	/** Ricevi e stampa i messaggi dal server <br>
	 * Il Receiver una volta autenticato rimarrà sempre in ascolto per poi terminare automaticamente
	 * @param ois L'{@link ObjectInputStream} sul quale ricevere {@link data.ChatResponse}} 
	 * @param oos L'{@link ObjectOutputStream} sul quale inviare {@link data.ChatRequest}}
	 * @param nickname Nome utente
	 * @throws IOException Se fallisce lancia un'eccezione
	 * */
	@SuppressWarnings({ "unchecked", "deprecation" })
	private static void riceviMessaggi(ObjectInputStream ois, ObjectOutputStream oos, String nickname) throws IOException {
		int lastPubblici = 0;
		int lastPrivati = 0;
		int lastMessageProgressivo = -1;	//Ultimo id messaggio visualizzato, inizio quinidi -1
		
		//First contact
		/* Invia richiesta al server per la lettura dei messaggi pubblici */
		ChatRequest cReq = new ChatRequest(ChatRequest.GET_MESSAGE, -1);
		oos.writeObject(cReq);
		oos.flush();
		
		//Attendi risposta
		ChatResponse cRes = null;
		try {
			cRes = (ChatResponse) ois.readObject();
			if(cRes.getResponseCode() != ChatResponse.RESPONSE_ERROR) {	//Esiste una lista non vuota
				
				//Aggiorna progressivo
				List<ChatMessage> messaggiPubblici = ((LinkedList<ChatMessage>)(cRes.getParam()));
				lastPubblici = messaggiPubblici.get(messaggiPubblici.size()-1).getProgressivo();
				
				Date date= null;
				
				for(int i=0; i<messaggiPubblici.size(); i++) {
					date = new Date(messaggiPubblici.get(i).getTimestamp());
					
					System.out.println( date.getHours() + ":" + date.getMinutes() + " | " +
							/* DEBUG ONLY */ messaggiPubblici.get(i).getProgressivo() + " | " +
							messaggiPubblici.get(i).getNickMittente() + ": " +
							messaggiPubblici.get(i).getTesto());
					
					if( (messaggiPubblici.get(i).getTesto()).equals("@LOGOUT " + nickname)) { 
						System.out.println("LOGOUT LATO SENDER!");
						return;
					}
					
						
				}
				
			} else {
				// System.out.println("Non ci sono messaggi pubblici..."); //DEBUG ONLY
				//throw(new ClassNotFoundException(cRes.getError()));
			}
		} catch (ClassNotFoundException cnfe) { cnfe.printStackTrace();}
		
		
		
		/* Invia richiesta al server per la lettura dei messaggi privati */
		cReq = new ChatRequest(ChatRequest.GET_PRIVATE_MESSAGE, -1);
		oos.writeObject(cReq);
		oos.flush();		
		
		//Attendi risposta
		cRes = null;
		try {
			cRes = (ChatResponse) ois.readObject();
			if(cRes.getResponseCode() != ChatResponse.RESPONSE_ERROR) {
				//Aggiorna progressivo 
				List<ChatMessage> messaggiPrivati = ((LinkedList<ChatMessage>)(cRes.getParam()));
				lastPrivati = messaggiPrivati.get(messaggiPrivati.size()-1).getProgressivo();
				
				Date date= null;
				
				for(int i=0; i<messaggiPrivati.size(); i++) {
					date = new Date(messaggiPrivati.get(i).getTimestamp());
					System.out.println(	date.getHours() + ":" + date.getMinutes() + " > " +
							/* DEBUG ONLY */ messaggiPrivati.get(i).getProgressivo() + " > " +
										messaggiPrivati.get(i).getNickMittente() + ":" +
										messaggiPrivati.get(i).getTesto());
				}
				
			} else {
				// System.out.println("Non ci sono messaggi privati..."); //DEBUG ONLY
				//throw(new ClassNotFoundException(cRes.getError()));	
			}
		} catch (ClassNotFoundException cnfe) { cnfe.printStackTrace();}
		
		
		//Aggiorna progressivo
		if(lastPrivati> lastPubblici) 	lastMessageProgressivo = lastPrivati;
		else							lastMessageProgressivo = lastPubblici;
		
		// System.out.println("Last message progressivo noto: " + lastMessageProgressivo); //DEBUG ONLY
		
		//LOOP
		
		while(true) {
			/* Invia richiesta al server per la lettura dei messaggi pubblici */
			//System.out.println("ChatRequest.GET_MESSAGE, " + lastMessageProgressivo); //DEBUG ONLY
			cReq = new ChatRequest(ChatRequest.GET_MESSAGE, lastMessageProgressivo);
			oos.writeObject(cReq);
			oos.flush();
			
			//Attendi risposta
			cRes = null;
			try {
				cRes = (ChatResponse) ois.readObject();
				if(cRes.getResponseCode() != ChatResponse.RESPONSE_ERROR) {
					//Aggiorna progressivo
					List<ChatMessage> messaggiPubblici = ((LinkedList<ChatMessage>)(cRes.getParam()));
					lastPubblici = messaggiPubblici.get(messaggiPubblici.size()-1).getProgressivo();
					
					Date date= null;
					
					for(int i=0; i<messaggiPubblici.size(); i++) {
						date = new Date(messaggiPubblici.get(i).getTimestamp());
						
						System.out.println(date.getHours() + ":" + date.getMinutes() + " | " +
								/* DEBUG ONLY */ messaggiPubblici.get(i).getProgressivo() + " | " +
								messaggiPubblici.get(i).getNickMittente() + ": " +
								messaggiPubblici.get(i).getTesto());
					
						if( (messaggiPubblici.get(i).getTesto()).equals("@LOGOUT " + nickname)) { 
							System.out.println("LOGOUT LATO SENDER!");
							return;
						}
					}
					
				} else {
					//System.out.println("Non ci sono messaggi...");
					Thread.sleep(1000);
					//throw(new ClassNotFoundException(cRes.getError()));
				}
			} catch (ClassNotFoundException cnfe) { cnfe.printStackTrace();
			} catch (InterruptedException e) { e.printStackTrace();	}
			
			///////////////////////////////
			//AGGIORNA IL PROGRESSIVO! IMPORTANTE
			if(lastPrivati > lastPubblici) 	lastMessageProgressivo = lastPrivati;
			else							lastMessageProgressivo = lastPubblici;
			
			/* Invia richiesta al server per la lettura dei messaggi privati */
			cReq = new ChatRequest(ChatRequest.GET_PRIVATE_MESSAGE, lastMessageProgressivo);
			oos.writeObject(cReq);
			oos.flush();		
			
			//Attendi risposta
			cRes = null;
			try {
				cRes = (ChatResponse) ois.readObject();
				if(cRes.getResponseCode() != ChatResponse.RESPONSE_ERROR) {
					//Aggiorna progressivo 2
					List<ChatMessage> messaggiPrivati = ((LinkedList<ChatMessage>)(cRes.getParam()));
					lastPrivati = messaggiPrivati.get(messaggiPrivati.size()-1).getProgressivo();

					Date date= null;
					
					for(int i=0; i<messaggiPrivati.size(); i++) {
						date = new Date(messaggiPrivati.get(i).getTimestamp());
						System.out.println(	date.getHours() + ":" + date.getMinutes() + " > " +
								/* DEBUG ONLY */ messaggiPrivati.get(i).getProgressivo() + " > " +
											messaggiPrivati.get(i).getNickMittente() + ":" +
											messaggiPrivati.get(i).getTesto());
					}
					Thread.sleep(1000);
				} else {
					//System.out.println("Non ci sono messaggi privati...");
					Thread.sleep(1000);
					//throw(new ClassNotFoundException(cRes.getError()));	
				}
			} catch (ClassNotFoundException cnfe) { cnfe.printStackTrace();
			} catch (InterruptedException e) { e.printStackTrace();	}
			
			//AGGIORNA IL PROGRESSIVO! IMPORTANTE
			if(lastPrivati > lastPubblici) 	lastMessageProgressivo = lastPrivati;
			else							lastMessageProgressivo = lastPubblici;
			// System.out.println("Ultimo progressivo noto: " + lastMessageProgressivo); //DEBUG ONLY 
		}
		
	}
}
