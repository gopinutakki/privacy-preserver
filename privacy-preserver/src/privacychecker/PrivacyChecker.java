package privacychecker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PrivacyChecker {

	static List<String> websites = new ArrayList<String>();
	static List<Thread> threads = new ArrayList<Thread>();
	static Database dbconn = null;
	static final int THREADCOUNT = 10;

	public static void main(String args[]) throws IOException,
			ClassNotFoundException, SQLException {
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

		// fetchExecutor.shutdown();
		while (!fetchExecutor.isTerminated()) {

		}

		System.out.println("Done Fetching All!");
	}

	public static void getURLs(String url) throws IOException, SQLException {
		// String url = "http://www.facebook.com";
		// print("Fetching %s...", url);

		Document doc = Jsoup
				.connect(url)
				.userAgent(
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/18.0.1025.142 Safari/535.2")
				.get();

		Elements links = doc.select("a[href]");
		Elements media = doc.select("[src]");
		Elements imports = doc.select("link[href]");
		String tempURL = "";
		String tempText = "";
		for (Element link : links) {
			tempURL = link.attr("abs:href");
			tempText = link.text().trim();

			if (tempURL.toLowerCase().contains("privacy")
					|| tempText.toLowerCase().contains("privacy"))
				addURL(tempURL, tempText);
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
		text.replaceAll("'", "\'");		
		dbconn.addURL(domainURL, url, text);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
