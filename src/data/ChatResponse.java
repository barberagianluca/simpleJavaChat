/*
	DSChat - Version 0.1
	A simple Chat in Java
	Distributed System Project coded by Gianluca Barbera (Matr. 3515987 - UNIGE)
	Comments and JavaDOC are in Italian in this version.
*/

package data;

import java.io.Serializable;

/** Risposte serializzabili inviate via TCP */
public class ChatResponse
implements Serializable {
	//RESPONSE CODES
	/** Invalid Response Code */
	public static final int RESPONSE_ERROR = 0;
	/** Found the Chat User List Code */
	public static final int CHAT_USERS_LIST_FOUND = 1;
	/** Broadcast Message Code */
	public static final int BROADCAST_MESSAGE_SENT = 2;
	/** Private Message Code */
	public static final int PRIVATE_MESSAGE_SENT = 3;
	/** Messages List Code */
	public static final int MESSAGE_LIST = 4;
	
	/** serial Version UID, 1275149812805609011L */
	private static final long serialVersionUID = 1275149812805609011L;
	
	/** The Response Code */
	private int responseCode;
	/** The optional error message */
	private String error;
	/** The Response parameter */
	private Object param;
	
	/**
	 * Costruttore senza parametri - Invalid Request
	 * */
	public ChatResponse() {
		this.responseCode = RESPONSE_ERROR;
		this.error = "INVALID REQUEST";
		this.param = null;
	}
	
	/**
	 * Costruttore con parametri
	 * @param rCode The Response Code
	 * @param rError The optional Response Error
	 * @param responseObj The Response Object (aka Parameter)
	 * */
	public ChatResponse(int rCode, String rError, Object responseObj) {
		this.responseCode = rCode;
		this.error = rError;
		this.param = responseObj;
	}
	
	/** GETTER - responseCode
	 * @return responseCode */
	public int getResponseCode() {
		return responseCode;
	}
	/** SETTER - responseCode
	 * @param responseCode responseCode
	 * @deprecated Please use constructor with params */
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	/** GETTER - error
	 * @return error */
	public String getError() {
		return error;
	}
	/** SETTER - error
	 * @param error error
	 * @deprecated Please use constructor with params */
	public void setError(String error) {
		this.error = error;
	}
	/** GETTER - param
	 * @return param */
	public Object getParam() {
		return param;
	}
	/** SETTER - param
	 * @param param param
	 * @deprecated Please use constructor with params */
	public void setParam(Object param) {
		this.param = param;
	}
	
}
