package de.doofmars.whattsap.parser;

import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

import com.mongodb.MongoClient;

public class WhatsappMessageRepository extends BasicDAO<WhatsappMessage, String>{

	public WhatsappMessageRepository(Class<WhatsappMessage> entityClass,
			MongoClient mongoClient, Morphia morphia, String dbName) {
		super(entityClass, mongoClient, morphia, dbName);
		// TODO Auto-generated constructor stub
	}
	
}
