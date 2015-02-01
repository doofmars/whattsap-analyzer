package de.doofmars.whattsap.parser;

import java.util.UUID;

import org.joda.time.DateTime;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;


@Entity("WhatsappMessages")
public class WhatsappMessage {
	
	@Id
	private String id;
	private DateTime timestamp;
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
	public WhatsappMessage(DateTime timestamp, String sender, String message) {
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
	public WhatsappMessage(DateTime timestamp, String message) {
		super();
		this.timestamp = timestamp;
		this.sender = "SYSTEM";
		this.message = message;
		this.messageType = Type.STATUS;
	}
	
	public void appendLine(String line) {
		this.message = new StringBuilder(message).append("\n").append(line).toString();
	}

	public DateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(DateTime timestamp) {
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
	
	@Override
	public String toString() {
		return timestamp + " - " + sender + message ;
	}

	public enum Type {
		STATUS, MESSAGE
	}
}
