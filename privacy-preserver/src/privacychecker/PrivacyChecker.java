package privacychecker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
	static Links privacyLinks = new Links();
	static List<Thread> threads = new ArrayList<Thread>();
	static final int THREADCOUNT = 10;
	
	public static void main(String args[]) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader("websites.txt"));
		String line = "";
		
		while((line = br.readLine()) != null){
			line.trim();
			line = "http://www." + line;
			websites.add(line);
			//getURLs(line.trim());
		}
		
		ExecutorService fetchExecutor = Executors.newFixedThreadPool(THREADCOUNT);
				
		for(int count = 0; count < websites.size(); count++){
			Runnable fetch = new urlRunnable(websites.get(count));
			fetchExecutor.execute(fetch);
		}
		
		//fetchExecutor.shutdown();
		while(!fetchExecutor.isTerminated()){
			
		}
		
		System.out.println("Done Fetching All!");		
	}
	
	public static void getURLs(String url) throws IOException {
        //String url = "http://www.facebook.com";
        //print("Fetching %s...", url);

        Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/18.0.1025.142 Safari/535.2").get();
        
        Elements links = doc.select("a[href]");
        Elements media = doc.select("[src]");
        Elements imports = doc.select("link[href]");
        String tempURL = "";
        String tempText = "";
        for (Element link : links) {
        	tempURL = link.attr("abs:href");
        	tempText = link.text().trim();
        	
        	if(tempURL.toLowerCase().contains("privacy") || tempText.toLowerCase().contains("privacy"))
        		privacyLinks.add(tempURL, tempText);
        }
        //privacyLinks.print();
    }

    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

    private static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width-1) + ".";
        else
            return s;
    }
}


class Links{
	ArrayList<Link> links = new ArrayList<Link>();
	
	void add(String url, String text){
		links.add(new Link(url, text));
	}
	
	boolean containsURL(String url){
		for(int index = 0; index < links.size(); index++){
			if(url.equals(links.get(index).url))
				return true;
		}
		return false;
	}
	
	void print(){
		for(int index = 0; index < links.size(); index++){
			System.out.println(links.get(index).url);
		}			
	}
}

class Link{
	String domainURL = "";
	String url = "";
	String text = "";
	
	public Link(String urlData, String textData){
		String[] domain = urlData.split("/");
		this.domainURL = domain[2];
		this.url = urlData;
		this.text = textData;
	}
}

class urlRunnable implements Runnable{
	String url = "";
	
	public urlRunnable(String link){
		this.url = link;
	}
	
	@Override
	public void run() {
		try {
			PrivacyChecker.getURLs(url);
		} catch (IOException e) {
			//e.printStackTrace();
			System.err.println("Could not get: " + url);
		}
	}
	
}