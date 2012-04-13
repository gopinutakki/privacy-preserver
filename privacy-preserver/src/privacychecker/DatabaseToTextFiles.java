package privacychecker;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;

public class DatabaseToTextFiles {

	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException{
		writeToFiles();
		System.out.println("Done, writing files!");
	}
	
	static void writeToFiles() throws ClassNotFoundException, SQLException, IOException{
		Database dbConnection = new Database();
		String[] fileNames = dbConnection.getWebsitesDistinct().split(" ");
		for(int index = 0; index < fileNames.length; index++){
			writeFileToDisk(fileNames[index], dbConnection.getWebsiteData(fileNames[index]));
		}
	}

	private static void writeFileToDisk(String website, String websiteData) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter("/home/gopi/PrivacyDataset/" + website + ".txt"));
		bw.write(websiteData);
		bw.flush();
		bw.close();
	}
}
