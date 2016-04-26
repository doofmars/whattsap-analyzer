package de.doofmars.whattsap.parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * Loads data from file and creates an adds the parsed message to the analyzer
 * 
 * @author Jan
 *
 */
public class WhatsappMessageImporter {
	private final static Logger logger = LogManager.getLogger(WhatsappMessageImporter.class);
	private final static Pattern regex_android = Pattern.compile("[\\d]{2}\\.[\\d]{2}\\.[\\d]{2}, [0-9]{2}:[0-9]{2}");
	private final static DateTimeFormatter dtf_android = DateTimeFormat.forPattern("dd.MM.yy, HH:mm");
	
	private final static Pattern regex_ios = Pattern.compile("[\\d]{2}\\.[\\d]{2}\\.[\\d]{2}, [0-9]{1,2}:[0-9]{2}:[0-9]{2}");
	private final static DateTimeFormatter dtf_ios = DateTimeFormat.forPattern("dd.MM.yy, HH:mm:ss");
	
	private String plattform = "";
	
	public void importFromTxt() {
		String file = "input.txt";
		WhatsappMessage lastMessage = null;
		WhatsappMessageAnalyzer analyzer = new WhatsappMessageAnalyzer();
		Integer totalMessages = 0;
		
		logger.info("Importing messages");
		try {
			BufferedReader br;
			br = new BufferedReader(new FileReader(file));
			
			String line = br.readLine();
			choosePlattform(line);
			
			
			//Loop thru the history file
			do {
				WhatsappMessage message = matchLine(line, regex_ios, dtf_ios);
				if (plattform == "android") {
					message = matchLine(line, regex_android, dtf_android);
				}
				
				if (plattform == "ios") {
					message = matchLine(line, regex_ios, dtf_ios);										
				}
				if (message != null) {
					analyzer.analyze(message);
					lastMessage = message;
					totalMessages++;
				}
			} while ((line = br.readLine()) != null);
			br.close();
		} catch (FileNotFoundException e) {
			logger.error("File {} was not found", file, e);
		} catch (IOException e) {
			logger.error("IOException occured", e);
		}
		logger.info("Printing report");
		if (analyzer.hasMessages()) {
			analyzer.print();
		}
		logger.info("Done... {} Messages where analyzed", totalMessages);
	}
	
	private void choosePlattform(String line) {
		Matcher matcher = regex_ios.matcher(line);
		if (matcher.find()) {
			this.plattform = "ios";		
			logger.info("Selected Plattform {} based on regex", this.plattform);
			return;
		}
		matcher = regex_android.matcher(line);
		if (matcher.find()) {
			this.plattform = "android";
			logger.info("Selected Plattform {} based on regex", this.plattform);
			return;
		}
		throw new InputMismatchException("No target palttform found");
	}
	
	
	private WhatsappMessage matchLine(String line, Pattern pattern, DateTimeFormatter dtf) {	
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()) {
			String dateString = matcher.group();
			DateTime dateTime = dtf.parseDateTime(dateString);

			String truncatet = line.replace(dateString, "");
			if (this.plattform == "android") {
				if (truncatet.contains(":")) {
					String user = truncatet.substring(3).substring(0, truncatet.indexOf(':') - 3);
					String message = truncatet.replace(user, "").substring(4);			
					return new WhatsappMessage(dateTime, user, message);
				} else {
					String message = truncatet.substring(3);
					return new WhatsappMessage(dateTime, message);
				}				
			}
			if (this.plattform == "ios") {
				truncatet = truncatet.substring(2);
				if (truncatet.contains(": ")) {
					String user = truncatet.substring(0, truncatet.indexOf(':'));
					String message = truncatet.replace(user, "").substring(2);			
					return new WhatsappMessage(dateTime, user, message);
				} else {
					String message = truncatet;
					return new WhatsappMessage(dateTime, message);
				}				
			}
			return null;
		} else {
			return null;
		}
	}
}
