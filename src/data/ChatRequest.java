/*
	DSChat - Version 0.1
	A simple Chat in Java
	Distributed System Project coded by Gianluca Barbera (Matr. 3515987 - UNIGE)
	Comments and JavaDOC are in Italian in this version.
*/

package data;

import java.io.Serializable;

/** Richieste serializzabili inviate via TCP */
public class ChatRequest
implements Serializable {
	//REQUEST CODES
	/** Invalid Request Code */
	public static final String INVALID_REQUEST = "0";
	/** Get Chat Users List Code */
	public static final String GET_CHATUSERS_LIST = "1";
	/** Send Message Code */
	public static final String SEND_MESSAGE = "2";
	/** Get Message Code */
	public static final String GET_MESSAGE = "3";
	/** Get Private Message Code */
	public static final String GET_PRIVATE_MESSAGE = "4";
	/** Logout Code */
	public static final String LOGOUT = "99";
	
	/** serial Version UID, -3483223809009696680L */
	private static final long serialVersionUID = -3483223809009696680L;
	
	/** The Request Code */
	private String requestCode;
	/** Parameter, any {@link Object}*/
	private Object param;
	
	/**
	 * Costruttore con parametri
	 * @param rCode The Request Code
	 * @param rObj The Request Object (aka Parameter)
	 * */
	public ChatRequest(String rCode, Object rObj) {
		this.requestCode = rCode;
		this.param = rObj;
	}
	
	/** GETTER - requestCode
	 * @return requestCode */
	public String getRequestCode() {
		return requestCode;
	}
	/** SETTER - requestCode
	 * @param requestCode requestCode */
	public void setRequestCode(String requestCode) {
		this.requestCode = requestCode;
	}
	/** GETTER - param
	 * @return param  N.B. tipo generico {@link Object} */
	public Object getParam() {
		return param;
	}
	/** SETTER - param
	 * @param param param */
	public void setParam(Object param) {
		this.param = param;
	}
	
}
