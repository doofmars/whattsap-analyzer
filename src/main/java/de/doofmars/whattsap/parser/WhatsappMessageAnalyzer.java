package de.doofmars.whattsap.parser;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class WhatsappMessageAnalyzer {
	private boolean hasMessages = false;
	private Integer messagesAnalyzed = 0;
	private Map<String, Integer> postsPerUser = new TreeMap<String, Integer>(); 
	private Map<String, Integer> wordsPerUser = new TreeMap<String, Integer>(); 
	private Map<String, Integer> wordCountTotal = new TreeMap<String, Integer>();
	private Map<String, Integer> postsPerDayOfWeek = new LinkedHashMap<String, Integer>();
	private Map<String, Integer> postsDay = new TreeMap<String, Integer>();
	private Map<Integer, Integer> postsHour = new TreeMap<Integer, Integer>();
	private Map<String, TreeMap<String, Integer>> commonMessageTextPerUser = new HashMap<String, TreeMap<String, Integer>>();
	private final Pattern pattern = Pattern.compile("[\"!#()*+-,./']");
	private final static DateTimeFormatter dayOfWeek = DateTimeFormat.forPattern("EEEE");
	private final static DateTimeFormatter date = DateTimeFormat.forPattern("yyyy.MM.dd");

	public WhatsappMessageAnalyzer() {
		//Pre initialize postsDay
		LocalDate now = new LocalDate();
		postsPerDayOfWeek.put(dayOfWeek.print(now.withDayOfWeek(DateTimeConstants.MONDAY)), 0);
		postsPerDayOfWeek.put(dayOfWeek.print(now.withDayOfWeek(DateTimeConstants.TUESDAY)), 0);
		postsPerDayOfWeek.put(dayOfWeek.print(now.withDayOfWeek(DateTimeConstants.WEDNESDAY)), 0);
		postsPerDayOfWeek.put(dayOfWeek.print(now.withDayOfWeek(DateTimeConstants.THURSDAY)), 0);
		postsPerDayOfWeek.put(dayOfWeek.print(now.withDayOfWeek(DateTimeConstants.FRIDAY)), 0);
		postsPerDayOfWeek.put(dayOfWeek.print(now.withDayOfWeek(DateTimeConstants.SATURDAY)), 0);
		postsPerDayOfWeek.put(dayOfWeek.print(now.withDayOfWeek(DateTimeConstants.SUNDAY)), 0);
	}
	
	public void analyze(WhatsappMessage message) {
		messagesAnalyzed++;
		hasMessages = true;
		//count messages per user
		if (postsPerUser.containsKey(message.getSender())) {
			postsPerUser.put(message.getSender(), postsPerUser.get(message.getSender()) + 1);
		} else {
			postsPerUser.put(message.getSender(), 1);
		}
		//count words per user and occurenies of words
		String[] words = message.getMessage().trim().split("\\s+");
		if (wordsPerUser.containsKey(message.getSender())) {
			wordsPerUser.put(message.getSender(), wordsPerUser.get(message.getSender()) + words.length);
		} else {
			wordsPerUser.put(message.getSender(), words.length);
		}
		//count occurrence of each single word
		for (int i = 0; i < words.length; i++) {
			if (wordCountTotal.containsKey(filterStrings(words[i]))) {
				wordCountTotal.put(filterStrings(words[i]), wordCountTotal.get(filterStrings(words[i])) + 1);
			} else {
				wordCountTotal.put(filterStrings(words[i]), 1);
			}			
		}
		//Posts per day of week
		String key = dayOfWeek.print(message.getTimestamp());
		postsPerDayOfWeek.put(key, postsPerDayOfWeek.get(key) + 1  );
		//Posts per day
		key = date.print(message.getTimestamp());
		if (postsDay.containsKey(key)) {
			postsDay.put(key, postsDay.get(key) + 1  );
		} else {
			postsDay.put(key, 1);
		}
		
		if (postsHour.containsKey(message.getTimestamp().get(DateTimeFieldType.hourOfDay()))) {			
			postsHour.put(message.getTimestamp().get(DateTimeFieldType.hourOfDay()), 
					postsHour.get(message.getTimestamp().get(DateTimeFieldType.hourOfDay())) + 1);
		} else {
			postsHour.put(message.getTimestamp().get(DateTimeFieldType.hourOfDay()), 1);
		}
		
		//Common Message Text per User
		if (commonMessageTextPerUser.containsKey(message.getSender())) {
			TreeMap<String, Integer> innerSender = commonMessageTextPerUser.get(message.getSender());
			if (innerSender.containsKey(message.getMessage())) {
				innerSender.put(message.getMessage(), innerSender.get(message.getMessage()) + 1);
			} else {
				innerSender.put(message.getMessage(), 1);
			}
			commonMessageTextPerUser.put(message.getSender(), innerSender);			
		} else {
			TreeMap<String, Integer> innerSender = new TreeMap<String, Integer>();
			innerSender.put(message.getMessage(), 1);
			commonMessageTextPerUser.put(message.getSender(), innerSender);
		}
	}
	
	/**
	 * Writes the results into an output file
	 */
	public void print() {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("output.txt", "UTF-8");
			Integer totalWords = 0;
			
			writer.println("---Whatsapp log Analyzer---");
			writer.println("---------------------");
			writer.println("---Posts per user---");
			writer.println("---------------------");
			for (Entry<String, Integer> posts : MapUtil.sortByValueDsc(postsPerUser).entrySet()) {
				writer.println(posts.getKey() + "\t" + posts.getValue());
			}
			writer.println("---------------------");
			writer.println("---Words per user---");
			writer.println("---------------------");
			for (Entry<String, Integer> posts : MapUtil.sortByValueDsc(wordsPerUser).entrySet()) {
				totalWords += posts.getValue();
				writer.println(posts.getKey() + "\t" + posts.getValue());
			}
			writer.println("---------------------");
			writer.println("---Avg. words per message per user---");
			writer.println("---------------------");
			for (Entry<String, Integer> posts : MapUtil.sortByValueDsc(wordsPerUser).entrySet()) {
				double percentage = (posts.getValue().doubleValue() / postsPerUser.get(posts.getKey()).doubleValue());
				writer.format("%s \t %.3f%n", posts.getKey(), percentage);
			}
			writer.println("---------------------");
			writer.println("---Post per day of week---");
			writer.println("---------------------");
			for (Entry<String, Integer> posts : postsPerDayOfWeek.entrySet()) {
				writer.println(posts.getKey() + "\t" + posts.getValue());
			}
			writer.println("---------------------");
			writer.println("---Post per day---");
			writer.println("---------------------");
			for (Entry<String, Integer> posts : postsDay.entrySet()) {
				writer.println(posts.getKey() + "\t" + posts.getValue());
			}
			writer.println("---------------------");
			writer.println("---Post per hour---");
			writer.println("---------------------");
			for (Entry<Integer, Integer> posts : postsHour.entrySet()) {
				writer.println(posts.getKey() + "\t" + posts.getValue());
			}
			writer.println("---------------------");
			writer.println("---Top 20 Message Text per User---");
			writer.println("---------------------");
			for (Entry<String, TreeMap<String, Integer>> posts : commonMessageTextPerUser.entrySet()) {
				writer.println("********" + posts.getKey() + "********");
				
				Map<String, Integer> innerSender = MapUtil.sortByValueDsc(posts.getValue());
				Iterator<Entry<String, Integer>> it = innerSender.entrySet().iterator();
				int top20Break = 0;
				while (it.hasNext()) {
					Entry<String, Integer> pairs = it.next();
					if (pairs.getKey() != " <Medien weggelassen>") {
						writer.println(pairs.getKey() + "\t" + pairs.getValue());
						it.remove(); // avoids a ConcurrentModificationException
						top20Break++;						
					}
					if (top20Break > 20) {
						break;
					}
				}
			}
			writer.println("---------------------");
			writer.println("---Words count---");
			writer.println("---------------------");
			for (Entry<String, Integer> posts : wordCountTotal.entrySet()) {
				writer.println(posts.getKey() + "\t" + posts.getValue());
			}
			writer.println("---------------------");
			writer.println("---Other statistics---");
			writer.println("---------------------");
			writer.println("Messages Total:\t" + messagesAnalyzed);
			writer.write("Total Words:\t" + totalWords + "\n");
			double percentage = (messagesAnalyzed / postsDay.size());
			writer.format("Messages per Day: \t %.3f%n", percentage);
			percentage = (totalWords / postsDay.size());
			writer.format("Words per Day: \t %.3f%n", percentage);
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
	}

	public boolean hasMessages() {
		return hasMessages;
	}
	
	/**
	 * Filter words beginning or ending with one of the following special characters
	 * >>>>> \"!#()*+-,./' <<<<<
	 * 
	 * @param input the unfiltered string
	 * @return the filtered string
	 */
	private String filterStrings(String input) {
		if (input.length() > 2) {
			String firstChar = input.substring(0, 1);
			String lastChar = input.substring(input.length() -1, input.length());
			Matcher matcher = pattern.matcher(firstChar);
			if (matcher.find()) {
				input = input.substring(1);
			}
			matcher = pattern.matcher(lastChar);
			if (matcher.find()) {
				input = input.substring(0, input.length() -1);
			}
		}
		
		return input.toLowerCase();
	}
}
