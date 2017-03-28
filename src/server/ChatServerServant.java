/*
	DSChat - Version 0.1
	A simple Chat in Java
	Distributed System Project coded by Gianluca Barbera (Matr. 3515987 - UNIGE)
	Comments and JavaDOC are in Italian in this version.
*/

package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import client.ChatClientReceiver;
import client.ChatClientSender;
import data.ChatMessage;
import data.ChatRequest;
import data.ChatResponse;
import data.ChatUser;

/** ChatServerServant aka ChatThread */
public class ChatServerServant
implements Runnable {
	/** Socket per la comunicazione */
	private Socket commSocket;
	/** Link con il Server di Chat */
	private ChatServer chatServer;
	/** Tipo di Servant */
	private String servantType;	//UNDEFINED / SENDER_S / RECEIVER_S
	/** Utente servito da questo Thread */
	private String userNickname;
	
	//Stream e Buffer
	/** STREAM... */
	InputStream inputStream = null;
	/** STREAM... */
	InputStreamReader inputStreamReader = null;
	/** STREAM... */
	OutputStream outputStream = null;
	/** STREAM... */
	OutputStreamWriter outputStreamWriter = null;
	/** STREAM... */
	ObjectOutputStream objectOutputStream = null;
	/** STREAM... */
    ObjectInputStream objectInputStream = null;
	/** BUFFER... */
	BufferedReader bufferedReader = null;
	/** BUFFER... */
	BufferedWriter bufferedWriter = null;
	
	/**
	 * Costruttore con parametri
	 * @param s Socket per la comunicazione con gli utenti
	 * @param chatServer Server di chat
	 * */
	public ChatServerServant(Socket s, ChatServer chatServer) {
		this.commSocket = s;
		this.chatServer = chatServer;
		this.servantType = "UNDEFINED";
		this.userNickname = "UNDEFINED";
	}

	/** RUN... */
	@Override
	public void run() {
		try {
			//Apri Stream e Buffer
			inputStream = commSocket.getInputStream();
			outputStream = commSocket.getOutputStream();
			
			inputStreamReader = new InputStreamReader(inputStream);
			outputStreamWriter = new OutputStreamWriter(outputStream);
			
			bufferedReader = new BufferedReader(inputStreamReader);
			bufferedWriter = new BufferedWriter(outputStreamWriter);
				
			//Registra Sender o Receiver
			//ATTENDI INFO
			aggiungiNickname(bufferedReader, bufferedWriter);
			
			//ORA POSSO AVERE 2 COMPORTAMENTI -> SENDER o RECEIVER
			
	        objectInputStream = new ObjectInputStream(inputStream);
	        objectOutputStream = new ObjectOutputStream(outputStream);
			
			if(this.servantType.equals("SENDER_S")) {
				System.out.println("Sender dell'utente " + this.userNickname + " pronto!");
				gestioneClientSender(objectInputStream, objectOutputStream);
			} else if(this.servantType.equals("RECEIVER_S")) {
				System.out.println("Receiver dell'utente " + this.userNickname + " pronto!");
				gestioneClientReceiver(objectInputStream, objectOutputStream);
			} else {
				System.err.println("ERRORE CONFIGURAZIONE ERRATA - ServantType = " + this.servantType);
			}
			
		} catch (IOException e) {
			//e.printStackTrace();
		}	
		
	}
	
	/** Aggiungi Nickname al server
	 * @param br Buffer per lettura messaggi
	 * @param bw Buffer per scrittura messaggi
	 * @throws IOException Se fallisce lancia un'eccezione
	 *  */
	private void aggiungiNickname(BufferedReader br, BufferedWriter bw) throws IOException {
		String msgReceived = null;
		String response = null;
		ChatUser tempCU = null;
		
		while(true) {
			//ATTENDI INFO
			System.out.println("In attesa di comunicazione...");
			msgReceived = br.readLine();
			if(!(msgReceived.trim().toUpperCase().equals(ChatClientSender.clientType) || msgReceived.trim().toUpperCase().equals(ChatClientReceiver.clientType)))	//INFO NON RICEVUTA, ATTENDI
				continue;
			
			if(msgReceived.trim().toUpperCase().equals(ChatClientSender.clientType))
				this.servantType = "SENDER_S";
			else
				this.servantType = "RECEIVER_S";
			
			//INVIA ACK
			// System.out.println("Invio ACK"); //DEBUG ONLY
			bw.write("ACK");
			bw.newLine();
			bw.flush();
			
			//ATTENDI NICK
			// System.out.println("In attesa del nick..."); // DEBUG ONLY
			msgReceived = br.readLine();
			if((msgReceived.trim().toUpperCase().equals("")))	//INFO NICK NON RICEVUTA, RIAVVOLGI
				continue;
			
			//VERIFICA NICK
			synchronized(this.chatServer) {
				tempCU = this.chatServer.getChatUser(msgReceived);
				if(	(tempCU == null) ||		//non ci sono utenti registrati con quel nick. 
					( !tempCU.isReceiver() && this.servantType.equals("RECEIVER_S") ) || 	//ci sono utenti registrati con quel nick ma non hanno settato receiver
					( !tempCU.isSender() && this.servantType.equals("SENDER_S") )			//ci sono utenti registrati con quel nick ma non hanno settato sender
				) {
					response = "ACK";
					
					//AGGIUNGO UTENTE
					if(tempCU == null) {
						tempCU = new ChatUser(msgReceived);
						this.chatServer.addChatUser(tempCU);
					}
					
					//AGGIORNO CAMPO CORRISPONDEMTE
					if(this.servantType.equals("RECEIVER_S"))
						tempCU.setReceiver(true);
					else
						tempCU.setSender(true);
					
					// System.out.println("Nick valido"); //DEBUG_ONLY
					
				} else {
					// System.out.println("Nick non valido"); //DEBUG_ONLY
					response = "NACK";
				}
			}
		
			//INVIA ACK o NACK
			// System.out.println("Invio " + response); //DEBUG ONLY
			bw.write(response);
			bw.newLine();
			bw.flush();
			
			if(response.equals("NACK")) {
				continue;
			}
			else {
				this.userNickname = new String(tempCU.getNickname());
				break;
			}
		}
		
	}
	
	/** Rimuovi Nickname dal server - funzione di attesa prima dell'uscita
	 * @param br Buffer per lettura messaggi
	 * @param bw Buffer per scrittura messaggi
	 * @throws IOException Se fallisce lancia un'eccezione
	 *  */
	private void rimuoviNickname(BufferedReader br, BufferedWriter bw) throws IOException {
		//INVIA NICK
		// System.out.println("Attendo deregistrazione"); //DEBUG_ONLY
		String nickReceived = br.readLine();
		
		if(nickReceived.equals(userNickname)) {
			//System.out.println("Richiesta corretta"); //DEBUG_ONLY
			this.chatServer.delChatUser(this.userNickname);
		}
		
		bw.write("Disconnessione avvenuta con successo!");
		bw.newLine();
		bw.flush();
		return;
	}
	
	/**
	 * SENDER SIDE
	 * @param objectInputStream The ObjectInputStream
	 * @param objectOutputStream The ObjectOutputStream
	 * @throws IOException Se fallisce lancia un'eccezione
	 * */
	private void gestioneClientSender(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) throws IOException {
		//Attendo ChatRequest del Sender
		ChatRequest chatReq = null;
		
		while(true) {
			
			//Prendi la richiesta
			try {
				chatReq = (ChatRequest) objectInputStream.readObject();
			} catch (ClassNotFoundException e) {
				System.err.println("Ricevuta richiesta non corretta dall'utente " + this.userNickname);
				e.printStackTrace();
				continue;
			}
			
			//Gestisci la richiesta
			String requestType = chatReq.getRequestCode();
			ChatResponse cr = null;
			
			if(requestType.equals(ChatRequest.GET_CHATUSERS_LIST)) {
				//Recupera lista utenti
				List<String> listaNickUtenti = null;
				synchronized(this.chatServer) {	//non serve synchronized il metodo è già synchronized sul server...
					listaNickUtenti = this.chatServer.getUsersList();
				}
				
				cr = new ChatResponse(ChatResponse.CHAT_USERS_LIST_FOUND, null, listaNickUtenti);
				
			} else if(requestType.equals(ChatRequest.SEND_MESSAGE)) { 
				//Gestione invio messaggio
				ChatMessage cm = (ChatMessage) chatReq.getParam();
				
				if(!cm.getNickMittente().equals(userNickname)) { //mittente non valido
					System.err.println("Ricevuta richiesta non corretta dall'utente (" + requestType + ")" + this.userNickname);;
					objectOutputStream.writeObject((new ChatResponse()));	//Write empty response
					continue;
				}

				
				if(cm.getNickDestinatario() != null) {
					// System.out.println("Messaggio per " + cm.getNickDestinatario()); //DEBUG_ONLY
					this.chatServer.chatRoom.addPrivateMessage(cm, cm.getNickDestinatario()); //OK?
					
					cr = new ChatResponse(ChatResponse.PRIVATE_MESSAGE_SENT, null, null);
				} else {
					chatServer.chatRoom.addMessage(cm);	
					cr = new ChatResponse(ChatResponse.BROADCAST_MESSAGE_SENT, null, null);
				}
				
			} else if(requestType.equals(ChatRequest.LOGOUT)) {
				//Invia messaggio LOGOUT in CHAT
				String nickname = (String)chatReq.getParam();
				long time = Calendar.getInstance().getTime().getTime();
				ChatMessage cm = new ChatMessage(time, nickname, null, "@LOGOUT " + nickname, 0);
				chatServer.chatRoom.addMessage(cm);	
				cr = new ChatResponse(ChatResponse.BROADCAST_MESSAGE_SENT, null, null);
				objectOutputStream.writeObject(cr);
				objectOutputStream.flush();
				break; /** NEW */
			} else {
				System.err.println("Ricevuta richiesta non corretta dall'utente (" + requestType + ")" + this.userNickname);;
				objectOutputStream.writeObject((new ChatResponse()));	//Write empty response
				continue;
			}
			
			objectOutputStream.writeObject(cr);
			objectOutputStream.flush();
		}
	}
	
	
	/**
	 * RECEIVER SIDE
	 * @param objectInputStream The ObjectInputStream
	 * @param objectOutputStream The ObjectOutputStream
	 * @throws IOException Se fallisce lancia un'eccezione
	 * */
	private void gestioneClientReceiver(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) throws IOException {
		//Attendo ChatRequest del Receiver
		ChatRequest chatReq = null;
		ChatResponse cr = null;
		
		while(true) {
			//Prendi la richiesta
			try {
				chatReq = (ChatRequest) objectInputStream.readObject();
			} catch (ClassNotFoundException e) {
				System.err.println("Ricevuta richiesta non corretta dall'utente " + this.userNickname);
				e.printStackTrace();
				continue;
			}
			
			//Gestisci la richiesta
			String requestType = chatReq.getRequestCode();
			Object requestObj = chatReq.getParam();
			
			
			if(requestType.equals(ChatRequest.GET_MESSAGE)) {
				//Recupera messaggi
				int fromMessageNumber = Integer.parseInt(requestObj.toString());
				List<ChatMessage> messages = new LinkedList<ChatMessage>();
				
				synchronized(this.chatServer.chatRoom) {
					if(this.chatServer.chatRoom.listMessages(fromMessageNumber) != null) {
						messages.addAll(this.chatServer.chatRoom.listMessages(fromMessageNumber));
						cr = new ChatResponse(ChatResponse.MESSAGE_LIST, null, messages);
					} else {
						cr = new ChatResponse(ChatResponse.RESPONSE_ERROR, null, "No Message");
					}
				}
				
				
				
			} else if(requestType.equals(ChatRequest.GET_PRIVATE_MESSAGE)) { 
				int fromMessageNumber = Integer.parseInt(requestObj.toString());
				List<ChatMessage> messages = new LinkedList<ChatMessage>();
				
				synchronized(this.chatServer.chatRoom) {
					if(this.chatServer.chatRoom.listPrivateMessages(this.userNickname, fromMessageNumber) != null) {
						messages.addAll(this.chatServer.chatRoom.listPrivateMessages(this.userNickname, fromMessageNumber));
						cr = new ChatResponse(ChatResponse.MESSAGE_LIST, null, messages);
					} else {
						String progressivo = String.valueOf((this.chatServer.chatRoom.getLastProgressivo()));
						cr = new ChatResponse(ChatResponse.RESPONSE_ERROR, progressivo, "No Message");
					}
				}
			
			} else if(requestType.equals(ChatRequest.LOGOUT)) { /** NEW */
				// System.out.println("Ricevuta richiesta di Logout"); //DEBUG_ONLY
				break;
			} else {
				System.out.println("Richiesta non gestita");
			}
			
			objectOutputStream.writeObject(cr);
			objectOutputStream.flush();
		}
		
		rimuoviNickname(bufferedReader, bufferedWriter);
	}

}
