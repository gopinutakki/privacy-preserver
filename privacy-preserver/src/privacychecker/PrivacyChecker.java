package privacychecker;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
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

	static String USERAGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19";
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
//		url = "http://www.acrobat.com";
//		print("Fetching %s...", url);
		boolean homePage = true;
		Queue<String> urlQueue = new LinkedList<String>();
		HashSet<String> urlVisited = new HashSet<String>();
		String tempURL = "";
		String tempText = "";
		String baseURI = "";
		
		urlQueue.add(url);
		
		while (true) {
			if (urlQueue.isEmpty() || urlVisited.size() > 3)
				break;

			url = urlQueue.poll();
			baseURI = Jsoup.connect(url).userAgent(USERAGENT).get().baseUri();
			
//			Elements meta = doc.select("html head meta");
//		    if (meta.attr("http-equiv").contains("REFRESH"))
//		        doc = Jsoup.connect(meta.attr("content").split("=")[1]).get();
					    
		    Document doc = Jsoup.connect(baseURI).userAgent(USERAGENT).get();
		    
			urlVisited.add(url);
			if(!homePage){
				addURL(url, doc.body().text());
			}			
			homePage = false;
			
			Elements links = doc.select("a[href]");
			Elements media = doc.select("[src]");
			Elements imports = doc.select("link[href]");
			for (Element link : links) {
				tempURL = link.attr("abs:href");
				tempText = link.text().trim();

				if (tempURL.toLowerCase().contains("privacy")
						|| tempText.toLowerCase().contains("privacy")) {
					tempText.replaceAll("\n", "");
					tempText.replaceAll("\t", "");
					// System.out.println(doc.text());

					if (!urlVisited.contains(tempURL)) {
						if (!urlQueue.contains(tempURL))
							urlQueue.add(tempURL);
					}
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
		if (domainURL.contains(":"))
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
