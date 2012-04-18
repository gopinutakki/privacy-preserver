package privacychecker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class DatabaseToTextFiles {

	public static void main(String[] args) throws Exception{		
		writeToFiles();
		//filterArffFiles();
		System.out.println("Done, writing files!");
	}
	
	private static void filterArffFiles() throws Exception {
		FileReader file = new FileReader("dataset.arff");
		Instances insts = new Instances(file);
		
		StringToWordVector stringToVector = new StringToWordVector(1000);
		stringToVector.setInputFormat(insts);
		stringToVector.setOutputWordCounts(true);
		stringToVector.setUseStoplist(true);		
		Instances filteredData = Filter.useFilter(insts, stringToVector);

		
		ArffSaver saver = new ArffSaver();
		saver.setInstances(filteredData);
		saver.setFile(new File("filteredDataset.arff"));
		saver.writeBatch();
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
