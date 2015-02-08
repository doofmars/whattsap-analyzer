package de.doofmars.whattsap.parser;


/**
 * Hello world!
 * 
 */
public class Main {
	
	public static void main(String[] args) {
		long timer = System.currentTimeMillis();
		WhatsappMessageImporter importer = new WhatsappMessageImporter();
		importer.importFromTxt();
		System.out.println("Elapsed time in ms: " + (System.currentTimeMillis() - timer));
	}
}
