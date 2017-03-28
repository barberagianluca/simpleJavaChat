/*
	DSChat - Version 0.1
	A simple Chat in Java
	Distributed System Project coded by Gianluca Barbera (Matr. 3515987 - UNIGE)
	Comments and JavaDOC are in Italian in this version.
*/

package data;

import java.io.Serializable;

/** I messaggi della chat */
public class ChatMessage
implements Serializable {
	/** serial Version UID, -850761955162041524L */
	private static final long serialVersionUID = -850761955162041524L;
	/** timestamp - in secondi */
	private long timestamp;
	/** mittente del messaggio */
	private String nickMittente;
	/** destinatario del messaggio */
	private String nickDestinatario;
	/** testo del messaggio */
	private String testo;
	/** id univoco del messaggio */
	private int progressivo; //numero messaggio univoco del sistema
	
	/**
	 * Costruttore con parametri
	 * @param tst Timestamp
	 * @param nickMittente Mittente del messaggio
	 * @param nickDestinatario Destinatario, se vuoto messaggio globale
	 * @param testo Testo del messaggio
	 * @param progressivo ID univoco del messaggio
	 * */
	public ChatMessage(long tst, String nickMittente, String nickDestinatario, String testo, int progressivo) {
		this.timestamp = tst;
		this.nickMittente = nickMittente;
		this.nickDestinatario = nickDestinatario;
		this.testo = testo;
		this.progressivo = progressivo;
	}
	
	/** GETTER - timestamp
	 * @return timestamp */
	public long getTimestamp() {
		return timestamp;
	}
	/** SETTER - timestamp
	 * @param tst timestamp */
	public void setTst(long tst) {
		this.timestamp = tst;
	}
	/** GETTER - nickMittente
	 * @return nickMittente */
	public String getNickMittente() {
		return nickMittente;
	}
	/** SETTER - nickMittente
	 * @param nickMittente nickMittente */
	public void setNickMittente(String nickMittente) {
		this.nickMittente = nickMittente;
	}
	/** GETTER - nickDestinatario
	 * @return nickDestinatario */
	public String getNickDestinatario() {
		return nickDestinatario;
	}
	/** SETTER - nickDestinatario
	 * @param nickDestinatario nickDestinatario */
	public void setNickDestinatario(String nickDestinatario) {
		this.nickDestinatario = nickDestinatario;
	}
	/** GETTER - testo
	 * @return testo */
	public String getTesto() {
		return testo;
	}
	/** SETTER - testo
	 * @param testo testo */
	public void setTesto(String testo) {
		this.testo = testo;
	}
	/** GETTER - progressivo
	 * @return progressivo */
	public int getProgressivo() {
		return progressivo;
	}
	/** SETTER - progressivo
	 * @param progressivo progressivo */
	public void setProgressivo(int progressivo) {
		this.progressivo = progressivo;
	}
}
