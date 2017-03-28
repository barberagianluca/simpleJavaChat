/*
	DSChat - Version 0.1
	A simple Chat in Java
	Distributed System Project coded by Gianluca Barbera (Matr. 3515987 - UNIGE)
	Comments and JavaDOC are in Italian in this version.
*/

package data;

import java.io.Serializable;

/** Utente della chat, oggetto serializzabile */
public class ChatUser
implements Serializable {
	/** serial Version UID, 8201633507109863432L */
	private static final long serialVersionUID = 8201633507109863432L;
	
	/** Nome utente */
	private String nickname;
	/** Flag di receiver connesso */
	boolean receiver;
	/** Flag del sender connesso */
	boolean sender;
	
	/**
	 * Costruttore con parametri
	 * @param nick Nome utente
	 * */
	public ChatUser(String nick) {
		this.nickname = nick;
		this.receiver = false;
		this.sender = false;
	}

	/** GETTER - nickname
	 * @return nickname */
	public String getNickname() {
		return nickname;
	}
	/** SETTER - nickname
	 * @param nickname nickname */
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	/** GETTER - receiver
	 * @return receiver */
	public boolean isReceiver() {
		return receiver;
	}
	/** SETTER - receiver
	 * @param receiver receiver */
	public void setReceiver(boolean receiver) {
		this.receiver = receiver;
	}
	/** GETTER - sender
	 * @return sender */
	public boolean isSender() {
		return sender;
	}
	/** SETTER - sender
	 * @param sender sender */
	public void setSender(boolean sender) {
		this.sender = sender;
	}
	
	/** Confronta se due oggetti {@link ChatUser} sono identici
	 * @return <b>true</b> se esito positivo, <b>false</b> altrimenti  */
	@Override
	public boolean equals(Object obj) {
		return (((ChatUser)obj).nickname.equals(this.nickname));
	}
}
