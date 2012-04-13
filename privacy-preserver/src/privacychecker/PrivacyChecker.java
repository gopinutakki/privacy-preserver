package privacychecker;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PrivacyChecker {

	static List<String> websites = new ArrayList<String>();
	static Links privacyLinks = new Links();
	static List<Thread> threads = new ArrayList<Thread>();
	static Database dbconn = null;

	static final int THREADCOUNT = 10;

	public static void main(String args[]) throws IOException,
			ClassNotFoundException, SQLException {

		getPrivacyURLs();
	}

	public static void getPrivacyURLs() throws ClassNotFoundException,
			SQLException, IOException {
		BufferedReader br = new BufferedReader(new FileReader("websites.txt"));
		String line = "";
		dbconn = new Database();

		while ((line = br.readLine()) != null) {
			line.trim();
			line = "http://www." + line;
			websites.add(line);
			// getURLs(line.trim());
		}

		ExecutorService fetchExecutor = Executors
				.newFixedThreadPool(THREADCOUNT);

		for (int count = 0; count < websites.size(); count++) {
			Runnable fetch = new urlRunnable(websites.get(count));
			fetchExecutor.execute(fetch);
		}

		fetchExecutor.shutdown();
		while (!fetchExecutor.isTerminated()) {

		}
		System.out.println("Done Fetching Website Policy URLs!");
	}

	public static void getURLs(String url) throws IOException, SQLException {
		// url = "http://www.facebook.com";
		// print("Fetching %s...", url);
		boolean urlFound = false;
		Queue<String> urlQueue = new LinkedList<String>();
		ArrayList<String> urlVisited = new ArrayList<String>();
		
		urlQueue.add(url);

		while (!urlQueue.isEmpty() && urlVisited.size() < 20) {
			urlFound = false;
			url = urlQueue.poll();			
			Document doc = Jsoup
					.connect(url)
					.userAgent(
							"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.151 Safari/535.19")
					.get();

			urlVisited.add(url);
			addURL(url, doc.text());
			
			Elements links = doc.select("a[href]");
			Elements media = doc.select("[src]");
			Elements imports = doc.select("link[href]");
			String tempURL = "";
			String tempText = "";
			for (Element link : links) {							
				tempURL = link.attr("abs:href");
				tempText = link.text().trim();				
				if (tempURL.toLowerCase().contains("privacy")
						|| tempText.toLowerCase().contains("privacy")) {
					tempText.replaceAll("\n", "");
					tempText.replaceAll("\t", "");
					// System.out.println(doc.text());

					for(int index = 0; index < urlVisited.size(); index++){
						if(urlVisited.get(index).equals(tempURL)){
							urlFound = true;
							break;
						}
					}
					if (!urlFound) {						
						urlQueue.add(tempURL);
					}
					urlFound = false;
				}
			}
		}
		// privacyLinks.print();
	}

	private static void print(String msg, Object... args) {
		System.out.println(String.format(msg, args));
	}

	private static String trim(String s, int width) {
		if (s.length() > width)
			return s.substring(0, width - 1) + ".";
		else
			return s;
	}

	private static void addURL(String url, String text) throws SQLException {
		String[] domain = url.split("/");
		String domainURL = domain[2];
		if(domainURL.contains(":"))
			domainURL = domainURL.substring(0, domainURL.indexOf(":"));			
		url.replaceAll("'", "");
		url.replaceAll("\"", "");
		url = url.replaceAll("[^a-zA-Z 0-9]+", "");
		text.replaceAll("'", "");
		text.replaceAll("\"", "");
		text = text.replaceAll("[^a-zA-Z 0-9]+", "");
		System.out.println(domainURL);
		dbconn.addURL(domainURL, url, text);
	}
}

class Links {
	ArrayList<Link> links = new ArrayList<Link>();

	void add(String url, String content) {

		for (int index = 0; index < links.size(); index++)
			if (links.get(index).equals(url)) {
				links.get(index).text += content;
				return;
			}
		links.add(new Link(url, content));
	}

	boolean containsURL(String url) {
		for (int index = 0; index < links.size(); index++) {
			if (url.equals(links.get(index).url))
				return true;
		}
		return false;
	}

	void print() {
		for (int index = 0; index < links.size(); index++) {
			System.out.println(links.get(index).url);
		}
	}
}

class Link {
	String domainURL = "";
	String url = "";
	String text = "";

	public Link(String urlData, String textData) {
		String[] domain = urlData.split("/");
		this.domainURL = domain[2];
		this.url = urlData;
		this.text = textData;
	}
}

class urlRunnable implements Runnable {
	String url = "";

	public urlRunnable(String link) {
		this.url = link;
	}

	@Override
	public void run() {
		try {
			PrivacyChecker.getURLs(url);
		} catch (IOException e) {
			// e.printStackTrace();
			System.err.println("Could not get: " + url);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
