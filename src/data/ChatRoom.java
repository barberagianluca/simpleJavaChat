/*
	DSChat - Version 0.1
	A simple Chat in Java
	Distributed System Project coded by Gianluca Barbera (Matr. 3515987 - UNIGE)
	Comments and JavaDOC are in Italian in this version.
*/

package data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** Chat Room del Server */
public final class ChatRoom {
	/** Lista messaggi pubblici */
	private List<ChatMessage> listaMessaggi;
	/** Lista messaggi privati degli utenti */
	private Map<String, List<ChatMessage>> messaggiPrivati;
	/** aka Progressivo, ID univoco messaggi */
	private AtomicInteger messageNumber;
	
	/**
	 * Costruttore senza parametri, inizializza la {@link ChatRoom}
	 * */
	public ChatRoom() {
		listaMessaggi = new LinkedList<ChatMessage> ();
		messaggiPrivati = new HashMap<String, List<ChatMessage>> ();
		messageNumber = new AtomicInteger(0);
	}
	
	/** Aggiunge un messaggio pubblico e restituisce la numerazione univoca del messaggio 
	 * @param msg Messaggio da aggiungere in {@link ChatRoom}
	 * @return Il ChatRoom.messageNumber corrente (aka Progressivo)
	 * */
	public int addMessage(ChatMessage msg) {
		int mNum=-1;
		
		synchronized(listaMessaggi) {
			msg.setProgressivo(this.messageNumber.incrementAndGet());
			listaMessaggi.add(msg);
			mNum = this.messageNumber.get();
		}
		
		return mNum;
	}
	
	/** Aggiunge un messaggio privato e restituiesce la numerazione univoca del messaggio 
	 * @param msg Messaggio da aggiungere in {@link ChatRoom}
	 * @param destinatario Destinatario del messaggio
	 * @return Il ChatRoom.messageNumber corrente (aka Progressivo)
	 * */
	public int addPrivateMessage(ChatMessage msg, String destinatario) {
		int mNum=-1;
		
		synchronized(listaMessaggi) {
			msg.setProgressivo(this.messageNumber.incrementAndGet());
			mNum = this.messageNumber.get();
			
			if(messaggiPrivati.containsKey(destinatario)) {
				LinkedList<ChatMessage> messaggi = (LinkedList<ChatMessage>) messaggiPrivati.get(destinatario);
				messaggi.add(msg);
			} else {
				LinkedList<ChatMessage> messaggi = new LinkedList<ChatMessage> ();
				messaggi.add(msg);
				messaggiPrivati.put(destinatario, messaggi);
			}
		}
		
		return mNum;
	}
	
	/**
	 * Restituisce la lista dei messaggi pubblici - MAX 20
	 * @param lastMessage Ultimo progressivo ricevuto dal richiedente
	 * @return Una SubList ({@link List} di {@link ChatMessage}) dei messaggi
	 * */
	public List<ChatMessage> listMessages(int lastMessage) {
		List<ChatMessage> messageList = new LinkedList<ChatMessage> ();
		// System.out.println("Last message ricevuto " + lastMessage); //DEBUG ONLY
		if(listaMessaggi.size()<=0)
			return null;
		
		//Max 20 messaggi
		if(lastMessage==-1 || lastMessage<=0) {
			lastMessage = listaMessaggi.size()-20;
			if(lastMessage<0)
				lastMessage = 0;
		} else {
			lastMessage = indexInlistOf(lastMessage) + 1;
			if(lastMessage>=listaMessaggi.size()) return null;
		}
		
		/* OLD
		messageList = listaMessaggi.subList(lastMessage, listaMessaggi.size());
		return messageList;
		*/
		
		//TEST NEW
		int fromIndex = lastMessage; // indexInlistOf(lastMessage)+1;
		// System.out.println("Richiesta lista da indice " + fromIndex); //DEBUG ONLY
		int toIndex = listaMessaggi.size();
		if (fromIndex!=toIndex)
			messageList = (listaMessaggi).subList(fromIndex, toIndex);
		else
			messageList = (listaMessaggi).subList(fromIndex-1, toIndex);
		
		if(messageList.size()>0)
			//Correggi problema possibile re-invio penultimo messaggio
			if(messageList.size()==1 && messageList.get(0).getProgressivo()<=lastMessage)
				return null;
			else
				return messageList;
		else
			return null;
	}
	
	/**
	 * Restituisce la lista dei messaggi privati del richiedente
	 * @param chatUser nome Utente ({@link ChatUser})
	 * @param lastMessage Ultimo progressivo ricevuto dal richiedente
	 * @return Una SubList ({@link List} di {@link ChatMessage}) dei messaggi
	 * */
	public List<ChatMessage> listPrivateMessages(String chatUser, int lastMessage) {
		List<ChatMessage> messageList = new LinkedList<ChatMessage> ();

		if(!messaggiPrivati.containsKey(chatUser) || messaggiPrivati.get(chatUser).size() <= 0) {
			//System.out.println("Nessun messaggio privato per " + chatUser); // DEBUG-ONLY
			return null;
		}
		
		/*
		//Max 20 messaggi - si suppone l'utente sia attivo...
		if(lastMessage==-1 || lastMessage<=0) {
			lastMessage = messaggiPrivati.get(chatUser).size()-20;
			if(lastMessage<0)
				lastMessage = 0;
		} else {
			lastMessage = indexPrivateInlistOf(chatUser, lastMessage);
			if(lastMessage>=messaggiPrivati.get(chatUser).size()) return null;
		}
		*/
		
		//System.out.println("Cerco i messaggi dall'indice " + indexPrivateInlistOf(chatUser, lastMessage) + " totale messaggi " + messaggiPrivati.get(chatUser).size());
		
		/*NEW! FUNZIONAAAAA!!! :D */
		int fromIndex = indexPrivateInlistOf(chatUser, lastMessage)+1;
		int toIndex = messaggiPrivati.get(chatUser).size();
		if (fromIndex!=toIndex)
			messageList = (messaggiPrivati.get(chatUser)).subList(fromIndex, toIndex);
		else
			messageList = (messaggiPrivati.get(chatUser)).subList(fromIndex-1, toIndex);
		
		if(messageList.size()>0)
			//Correggi problema possibile re-invio penultimo messaggio
			if(messageList.size()==1 && messageList.get(0).getProgressivo()<=lastMessage)
				return null;
			else
				return messageList;
		else
			return null;
	}
	
	/** Converti il progressivo in indice della lista dei messaggi pubblici
	 * @param progressivo ID messaggio
	 * @return Indice della lista dei messaggi pubblici (ChatRoom.listaMessaggi)
	 * */
	protected int indexInlistOf(int progressivo) {
		// System.out.println("->->->-> " + progressivo); //DEBUG ONLY
		for(int i=0; i<listaMessaggi.size(); i++) {
			if(listaMessaggi.get(i).getProgressivo()==progressivo)
				return i;
		}
		// System.out.println("ultimo progressivo in lista " + listaMessaggi.get(listaMessaggi.size()-1).getProgressivo()); //DEBUG ONLY
		
		if(progressivo >=listaMessaggi.get(listaMessaggi.size()-1).getProgressivo()) {
			// System.out.println("----------" + (listaMessaggi.size()-1)); //DEBUG ONLY
			return listaMessaggi.size()-1;
		} else if ((listaMessaggi.size() > 0) //Messaggi privati tra quelli pubblici
					&& (progressivo < listaMessaggi.get(listaMessaggi.size()-1).getProgressivo())) {
			return listaMessaggi.size()-2;
		}
		else {
			return 0;	//altrimenti ritorna indice 0
		}

	}
	
	/** Converti il progressivo in indice della lista dei messaggi privati
	 * @param chatUser nome Utente ({@link ChatUser})
	 * @param progressivo ID messaggio
	 * @return Indice della lista dei messaggi pubblici (ChatRoom.listaMessaggi)
	 * */
	protected int indexPrivateInlistOf(String chatUser, int progressivo) {
		for(int i=messaggiPrivati.get(chatUser).size()-1; i>0; i--) {	//cerca nella lista messaggi
			if(messaggiPrivati.get(chatUser).get(i).getProgressivo()==progressivo)	//se il progressivo del messaggio è quello cercato
				return i;	//ritorna il suo indice
		}
		
		if(progressivo>=messaggiPrivati.get(chatUser).get(messaggiPrivati.get(chatUser).size()-1).getProgressivo()) {
			return messaggiPrivati.get(chatUser).size()-1;
		} else if( (messaggiPrivati.get(chatUser).size() > 0) && 
					progressivo < messaggiPrivati.get(chatUser).get(messaggiPrivati.get(chatUser).size()-1).getProgressivo()) { //Messaggi pubblici tra messaggi privati
			return messaggiPrivati.get(chatUser).size()-2;
		}
		else {
			return 0;	//altrimenti ritorna indice 0
		}
	}

	/**
	 * Restituisci l'ultimo ID univoco dei messaggi
	 * @return ultimo progressivo
	 * */
	public int getLastProgressivo() {
		return messageNumber.get();
	}
}
