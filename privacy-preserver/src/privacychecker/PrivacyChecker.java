package privacychecker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PrivacyChecker {
	
	static List<String> websites = new ArrayList<String>();
	static Links privacyLinks = new Links();
	static List<Thread> threads = new ArrayList<Thread>();
	
	public static void main(String args[]) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader("websites.txt"));
		String line = "";
		
		while((line = br.readLine()) != null){
			line.trim();
			line = "http://www." + line;
			websites.add(line);
			//getURLs(line.trim());
		}
		
		for(int count = 0; count < websites.size(); count++){
			Runnable fetch = new urlRunnable(websites.get(count));
			Thread fetcher = new Thread(fetch);
			fetcher.setName(websites.get(count));
			fetcher.start();
			threads.add(fetcher);
		}
		
		int running = 0;
		do{
			running = 0;
			for(Thread thread: threads){
				if(thread.isAlive()){
					running ++;
				}
			}
		}while(running > 0);
	}
	
	public static void getURLs(String url) throws IOException {
        //String url = "http://www.facebook.com";
        print("Fetching %s...", url);

        Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/18.0.1025.142 Safari/535.2").get();
        
        Elements links = doc.select("a[href]");
        Elements media = doc.select("[src]");
        Elements imports = doc.select("link[href]");
        String tempURL = "";
        String tempText = "";
//        print("\nMedia: (%d)", media.size());
//        for (Element src : media) {
//            if (src.tagName().equals("img"))
//                print(" * %s: <%s> %sx%s (%s)",
//                        src.tagName(), src.attr("abs:src"), src.attr("width"), src.attr("height"),
//                        trim(src.attr("alt"), 20));
//            else
//                print(" * %s: <%s>", src.tagName(), src.attr("abs:src"));
//        }
//
//        print("\nImports: (%d)", imports.size());
//        for (Element link : imports) {
//            print(" * %s <%s> (%s)", link.tagName(),link.attr("abs:href"), link.attr("rel"));
//        }
//
//        print("\nLinks: (%d)", links.size());
//        
        for (Element link : links) {
        	tempURL = link.attr("abs:href");
        	tempText = link.text().trim();
        	
        	if(tempURL.toLowerCase().contains("privacy") || tempText.toLowerCase().contains("privacy"))
        		privacyLinks.add(tempURL, tempText);
            //print(" * a: <%s>  (%s)", link.attr("abs:href"), trim(link.text(), 35));
        	//System.out.println(link.attr("abs:href"));
        }
        privacyLinks.print();
        //System.out.println(doc.text());
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
	String url = "";
	String text = "";
	
	public Link(String urlData, String textData){
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
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.err.println("Could not get: " + url);
		}
	}
	
}