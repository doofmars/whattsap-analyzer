package de.doofmars.whattsap.parser;

import java.util.Date;
import java.util.UUID;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;


@Entity("WhatsappMessages")
public class WhatsappMessage {
	
	@Id
	private String id;
	private Date timestamp;
	private Type messageType;
	private String sender;
	private String message;
	
	public WhatsappMessage() {
		this.id = UUID.randomUUID().toString();
	}
	
	public WhatsappMessage(String input) {
		super();
	}

	/**
	 * Whatsapp message containing a user
	 * 
	 * @param timestamp
	 * @param sender
	 * @param message
	 */
	public WhatsappMessage(Date timestamp, String sender, String message) {
		super();
		this.timestamp = timestamp;
		this.sender = sender;
		this.message = message;
		this.messageType = Type.MESSAGE;
	}

	/**
	 * Whatsapp message with status, like user added or chat subject changed.
	 * 
	 * @param timestamp
	 * @param message
	 */
	public WhatsappMessage(Date timestamp, String message) {
		super();
		this.timestamp = timestamp;
		this.sender = "SYSTEM";
		this.message = message;
		this.messageType = Type.STATUS;
	}
	
	public void appendLine(String line) {
		this.message = new StringBuilder(message).append("\n").append(line).toString();
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public Type getMessageType() {
		return messageType;
	}

	public void setMessageType(Type messageType) {
		this.messageType = messageType;
	}

	public enum Type {
		STATUS, MESSAGE
	}
}
