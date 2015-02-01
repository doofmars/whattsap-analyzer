package de.doofmars.whattsap.parser;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TreeMap;

public class WhatsappMessageAnalyzer {
	private boolean hasMessages = false;
	private Map<String, Integer> postsPerUser = new TreeMap<String, Integer>(); 
	private Map<String, Integer> wordsPerUser = new TreeMap<String, Integer>(); 
	private Map<String, Integer> wordCountTotal = new TreeMap<String, Integer>();
	private Map<String, Integer> postsPerDayOfWeek = new TreeMap<String, Integer>();
	private Map<String, Integer> postsDay = new TreeMap<String, Integer>();
	private final Pattern pattern = Pattern.compile("[\"!#()*+-,./']");
	private final static SimpleDateFormat dayOfWeek = new SimpleDateFormat("EEEE");
	private final static SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd");
	
	public void analyze(WhatsappMessage message) {
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
		String key = dayOfWeek.format(message.getTimestamp());
		if (postsPerDayOfWeek.containsKey(key)) {
			postsPerDayOfWeek.put(key, postsPerDayOfWeek.get(key) + 1  );
		} else {
			postsPerDayOfWeek.put(key, words.length);
		}
		key = date.format(message.getTimestamp());
		System.out.println(key);
		System.out.println(message.getTimestamp());
		if (postsDay.containsKey(key)) {
			postsDay.put(key, postsDay.get(key) + 1  );
		} else {
			postsDay.put(key, words.length);
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
			
			writer.println("---Posts per users---");
			writer.println("---------------------");
			for (Entry<String, Integer> posts : postsPerUser.entrySet()) {
				writer.println(posts.getKey() + "\t" + posts.getValue());
			}
			writer.println("---------------------");
			writer.println("---Words per users---");
			writer.println("---------------------");
			for (Entry<String, Integer> posts : wordsPerUser.entrySet()) {
				totalWords += posts.getValue();
				writer.println(posts.getKey() + "\t" + posts.getValue());
			}
			writer.println("---------------------");
			writer.write("TotalWords:" + totalWords + "\n");
			writer.println("---------------------");
			writer.println("---Avg. words per message per users---");
			writer.println("---------------------");
			for (Entry<String, Integer> posts : wordsPerUser.entrySet()) {
				double percentage = (posts.getValue().doubleValue() / postsPerUser.get(posts.getKey()).doubleValue());
				writer.format("%s \t %.3f%n", posts.getKey(), percentage);
			}
			writer.println("---------------------");
			writer.println("---Post per Day of Week---");
			writer.println("---------------------");
			for (Entry<String, Integer> posts : postsPerDayOfWeek.entrySet()) {
				writer.println(posts.getKey() + "\t" + posts.getValue());
			}
			writer.println("---------------------");
			writer.println("---Post per Day ---");
			writer.println("---------------------");
			for (Entry<String, Integer> posts : postsDay.entrySet()) {
				writer.println(posts.getKey() + "\t" + posts.getValue());
			}
			writer.println("---------------------");
//			writer.println("---Words count---");
//			writer.println("---------------------");
//			for (Entry<String, Integer> posts : wordCountTotal.entrySet()) {
//				writer.println(posts.getKey() + "\t" + posts.getValue());
//			}
//			writer.println("---------------------");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
	}

	public boolean hasMessages() {
		return hasMessages;
	}
	
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
