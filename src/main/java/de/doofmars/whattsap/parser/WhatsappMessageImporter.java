package de.doofmars.whattsap.parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
	private final static DateTimeFormatter dtf_ios = DateTimeFormat.forPattern("dd.MM.yy, HH:mm:ss").withDefaultYear(2015);
	
	public void importFromTxt() {
		String file = "input.txt";
		WhatsappMessage lastMessage = null;
		WhatsappMessageAnalyzer analyzer = new WhatsappMessageAnalyzer();
		Integer totalMessages = 0;
		
		try {
			BufferedReader br;
			br = new BufferedReader(new FileReader(file));
			String line;
			//Loop thru the history file
			while ((line = br.readLine()) != null) {
				WhatsappMessage message = matchLine(line, regex_ios, dtf_ios);
				if (message == null) {
					message = matchLine(line, regex_android, dtf_android);
					if (message == null) {
						lastMessage.appendLine(line);
					} else {
						analyzer.analyze(message);
						lastMessage = message;	
						totalMessages++;
					}
				} else {
					analyzer.analyze(message);
					lastMessage = message;
					totalMessages++;
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			logger.error("File {} was not found", file, e);
		} catch (IOException e) {
			logger.error("IOException occured", e);
		}
		if (analyzer.hasMessages()) {
			analyzer.print();
		}
		logger.info("Inport completed, {} Messages imported", totalMessages);
	}
	
	private WhatsappMessage matchLine(String line, Pattern pattern, DateTimeFormatter dtf) {	
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()) {
			String dateString = matcher.group();
			DateTime dateTime = dtf.parseDateTime(dateString);

			String truncatet = line.replace(dateString, "");
			if (truncatet.contains(":")) {
				String user = truncatet.substring(3).substring(0, truncatet.indexOf(':') - 3);
				String message = truncatet.replace(user, "").substring(4);			
				return new WhatsappMessage(dateTime, user, message);
			} else {
				String message = truncatet.substring(3);
				return new WhatsappMessage(dateTime, message);
			}
		} else {
			return null;
		}
	}
}
