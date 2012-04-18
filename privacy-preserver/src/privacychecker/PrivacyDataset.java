package privacychecker;

/**
 * 
 * Spam Classifier. Assignment 4.
 * Dr. Olfa Nasraoui. CECS 621. University of Louisville.
 * 
 * @author Gopi Chand Nutakki
 * @license GPLv3
 * 
 * @date March 9 2012
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.J48;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class PrivacyDataset {

	static Attribute websitePrivacyStmt;
	static FastVector websitePrivacyType;
	static Attribute websiteClass;
	static FastVector records;
	static Instances stmtSet;
	static Instances evaluationSet;

	/*
	 * Begin here....
	 */
	public static void main(String args[]) throws Exception {
		PrivacyDataset classifier = new PrivacyDataset();
		// classifier.createSet();
		// classifier.readDataset("datasetCleaned.arff");

		// classifier.performClustering(new SimpleKMeans(), "KMEANS CLUSTERER");

		//classifier.performClassification(new NaiveBayesMultinomial(),
			//	"NAIVE BAYES MULTINOMIAL");

		// Other algorithms
		// classifier.performClassification(new NaiveBayes(), "NAIVE BAYES");
		classifier.performClassification(new J48(), "J48 (C4.5)");
		// classifier.performClassification(new AdaBoostM1(), "ADA BOOST M1");
	}

	private void readDataset(String file) throws Exception {
		DataSource src = new DataSource(file);
		Instances data = src.getDataSet();

		StringToWordVector stringToVector = new StringToWordVector(1000);
		stringToVector.setInputFormat(data);
		stringToVector
				.setOptions(weka.core.Utils
						.splitOptions("-R first-last -W 1000 -prune-rate -1.0 -C -N 0 -S -stemmer weka.core.stemmers.SnowballStemmer -M 1 -tokenizer \"weka.core.tokenizers.NGramTokenizer -max 6 -min 2\"")); // -delimiters
																																																				// \"
																																																				// \r\n\t.,;:\'\"()?!\"
		Instances filteredData = Filter.useFilter(data, stringToVector);

		System.out.println(filteredData.attribute(1).index());
	}

	private void createSet() throws IOException, SQLException,
			ClassNotFoundException {

		websitePrivacyType = new FastVector(2);
		// websitePrivacyType.addElement("high");
		// websitePrivacyType.addElement("medium");
		websitePrivacyType.addElement("safe");
		websitePrivacyType.addElement("unsafe");
		websitePrivacyType.addElement("?");
		websiteClass = new Attribute("websitePrivacyType", websitePrivacyType);

		websitePrivacyStmt = new Attribute("websitePrivacyStmt",
				(FastVector) null);

		records = new FastVector(2);
		records.addElement(websiteClass);
		records.addElement(websitePrivacyStmt);

		stmtSet = new Instances("PrivacyStmtsDataset", records, 57);
		stmtSet.setClassIndex(0);

		readTrainingDataset("/home/gopi/PrivacyDataset/");

		ArffSaver saver = new ArffSaver();
		saver.setInstances(stmtSet);
		saver.setFile(new File("datasetCleaned.arff"));
		saver.writeBatch();
	}

	private void readTrainingDataset(String dataset) throws IOException {

		ArrayList<String> fileNames = new ArrayList<String>();
		ArrayList<String> dataFiles = this.listFiles(dataset, fileNames);
		String data = "";
		for (int looper = 0; looper < dataFiles.size(); looper++) {
			data = this.readFileAsString(dataFiles.get(looper));
			Instance rec = new Instance(2);
			if (dataFiles.get(looper).contains("high")) {
				rec.setValue((Attribute) records.elementAt(0), "safe");
			} else if (dataFiles.get(looper).contains("medium")) {
				rec.setValue((Attribute) records.elementAt(0), "safe");
			} else if (dataFiles.get(looper).contains("low")) {
				rec.setValue((Attribute) records.elementAt(0), "unsafe");
			} else if (dataFiles.get(looper).contains("unsafe")) {
				rec.setValue((Attribute) records.elementAt(0), "unsafe");
			}
			rec.setValue((Attribute) records.elementAt(1), data);
			stmtSet.add(rec);
		}
	}

	/*
	 * Read each file as String, and add it to the dataset.
	 */
	private String readFileAsString(String filePath) throws IOException {
		byte[] buffer = new byte[(int) new File(filePath).length()];
		BufferedInputStream f = null;
		try {
			f = new BufferedInputStream(new FileInputStream(filePath));
			f.read(buffer);
		} finally {
			if (f != null)
				try {
					f.close();
				} catch (IOException ignored) {
				}
		}
		return new String(buffer);
	}

	/*
	 * Listing files in a directory, the files contain the data.
	 */
	private ArrayList<String> listFiles(String path, ArrayList<String> fileNames) {
		System.out.println("PATH: " + path);
		File dir = new File(path);
		File[] files = dir.listFiles();

		for (int loop = 0; loop < files.length; loop++) {
			if (files[loop].isDirectory()) {
				listFiles(files[loop].getAbsolutePath(), fileNames);
			} else
				fileNames.add(files[loop].getAbsolutePath());
		}
		Collections.shuffle(fileNames);
		return fileNames;
	}

	/**
	 * Create the classification model and perform the classification.
	 * 
	 * @param model
	 * @param modelName
	 * @throws Exception
	 */
	private void performClassification(Object model, String modelName)
			throws Exception {
		System.out.println("**==" + modelName + "==**");
		DataSource src = new DataSource("datasetCleaned.arff");
		stmtSet = src.getDataSet();

		StringToWordVector stringToVector = new StringToWordVector(1000);
		// stringToVector.setUseStoplist(true);
		stringToVector.setInputFormat(stmtSet);
		// stringToVector.setOutputWordCounts(true);
		stringToVector
				.setOptions(weka.core.Utils
						.splitOptions("-R first-last -W 1000 -prune-rate -1.0 -C -N 0 -S -stemmer weka.core.stemmers.SnowballStemmer -M 1 -tokenizer \"weka.core.tokenizers.NGramTokenizer -delimiters=\" \r\n\t.,;:'\"()?!\" -max 7 -min 7\"")); // -delimiters
		// \"
		// \r\n\t.,;:\'\"()?!\"
		Instances filteredData = Filter.useFilter(stmtSet, stringToVector);
		filteredData.setClassIndex(0);
		// For now, using only training set of 80 samples. 40 for spam and 40
		// for non-spam.
		// Instances filteredTestData = Filter.useFilter(testingSet,
		// stringToVector);

		// Classifier cModel = (Classifier) new NaiveBayes();
		Classifier cModel = (Classifier) model;
		cModel.buildClassifier(filteredData);

		// Print the predictions.
		for (int i = 0; i < filteredData.numInstances(); i++) {
			double pred = cModel.classifyInstance(filteredData.instance(i));
			System.out.print("ID: " + filteredData.instance(i).value(0));
			System.out.print(", actual: "
					+ filteredData.classAttribute().value(
							(int) filteredData.instance(i).classValue()));
			System.out.println(", predicted: "
					+ filteredData.classAttribute().value((int) pred));
		}

		// Print the model (lot of results)
		// System.out.println(cModel);

		// Print other information, evaluation results.
		Evaluation eTest = new Evaluation(filteredData);
		eTest.evaluateModel(cModel, filteredData);
		System.out.println(eTest.toSummaryString(true));
		System.out.println(eTest.toClassDetailsString());
		System.out.println(eTest.toMatrixString());
		
		
		// other options
	    int seed  = 1;
	    int folds = 10;

	    
		// randomize data
	    Random rand = new Random(seed);
	    Instances randData = new Instances(filteredData);
	    randData.setClassIndex(0);
	    
	    randData.randomize(rand);
	    if (randData.classAttribute().isNominal())
	      randData.stratify(folds);

	    // perform cross-validation
	    Evaluation eval = new Evaluation(randData);
	    for (int n = 0; n < folds; n++) {
	      Instances train = randData.trainCV(folds, n);
	      Instances test = randData.testCV(folds, n);
	      // the above code is used by the StratifiedRemoveFolds filter, the
	      // code below by the Explorer/Experimenter:
	      // Instances train = randData.trainCV(folds, n, rand);

	      // build and evaluate classifier
	      Classifier clsCopy = Classifier.makeCopy(cModel);
	      clsCopy.buildClassifier(train);
	      eval.evaluateModel(clsCopy, test);
	    }
	    
	    System.out.println("===========Next Output (Cross Validation 10-Fold)==");
	    
	 // Print the predictions.
	 		for (int i = 0; i < filteredData.numInstances(); i++) {
	 			double pred = cModel.classifyInstance(filteredData.instance(i));
	 			System.out.print("ID: " + filteredData.instance(i).value(0));
	 			System.out.print(", actual: "
	 					+ filteredData.classAttribute().value(
	 							(int) filteredData.instance(i).classValue()));
	 			System.out.println(", predicted: "
	 					+ filteredData.classAttribute().value((int) pred));
	 		}

	 		
	    System.out.println(eval.toSummaryString(true));
		System.out.println(eval.toClassDetailsString());
		System.out.println(eval.toMatrixString());		
	}

	private void performClustering(SimpleKMeans model, String modelName)
			throws Exception {
		System.out.println("**==" + modelName + "==**");
		StringToWordVector stringToVector = new StringToWordVector(1000);
		stringToVector.setUseStoplist(true);
		stringToVector.setInputFormat(stmtSet);
		stringToVector.setOutputWordCounts(true);
		stringToVector
				.setOptions(weka.core.Utils
					.splitOptions("-R first-last -W 1000 -prune-rate -1.0 -C -N 0 -S -stemmer weka.core.stemmers.SnowballStemmer -M 1 -tokenizer \"weka.core.tokenizers.NGramTokenizer -max 4 -min 1\""));
		Instances filteredData = Filter.useFilter(stmtSet, stringToVector);

		// For now, using only training set of 80 samples. 40 for spam and 40
		// for non-spam.
		// Instances filteredTestData = Filter.useFilter(testingSet,
		// stringToVector);

		// Classifier cModel = (Classifier) new NaiveBayes();

		model.setOptions(weka.core.Utils
				.splitOptions("-N 3 -A \"weka.core.EuclideanDistance -R first-last\" -I 500 -S 10"));
		model.buildClusterer(filteredData);
		ClusterEvaluation eval = new ClusterEvaluation();
		eval.setClusterer(model);
		eval.evaluateClusterer(filteredData);
		System.out.println(eval.clusterResultsToString());
	}

	private void readDatasetDB() throws IOException, SQLException,
			ClassNotFoundException {

		Database websiteListDB = new Database();
		String[] websiteList = websiteListDB.getWebsitesDistinct().split(" ");
		String privacyStmt = "";
		Instance rec = new Instance(2);

		for (int index = 0; index < websiteList.length; index++) {
			privacyStmt = websiteListDB.getWebsiteData(websiteList[index]);
			rec.setValue((Attribute) records.elementAt(0), "?");
			rec.setValue((Attribute) records.elementAt(1), privacyStmt);
			stmtSet.add(rec);
		}
	}
}
