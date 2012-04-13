package privacychecker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class PrivacyClassifier {

	static Attribute emailMessage;
	static FastVector emailClass;
	static Attribute eClass;
	static FastVector records;
	static Instances trainingSet;
	static Instances testingSet;
	static Instances evaluationSet;

	/*
	 * Begin here....
	 */
	public static void main(String args[]) throws Exception {
		String dataset = "d1a";
		PrivacyClassifier classifier = new PrivacyClassifier();
		classifier.createTrainingSet(dataset + "\\training");
		classifier.createTestingSet(dataset + "\\testing");

		classifier.performClassification(new NaiveBayesMultinomial(), "NAIVE BAYES MULTINOMIAL");

		// Other algorithms
		// classifier.performClassification(new NaiveBayes(), "NAIVE BAYES");
		// classifier.performClassification(new J48(), "J48 (C4.5)");
		// classifier.performClassification(new AdaBoostM1(), "ADA BOOST M1");
	}

	/*
	 * Create the ARFF files from the dataset (in .txt files.)
	 */
	private void createTestingSet(String dataset) throws IOException {
		emailMessage = new Attribute("emailMessage", (FastVector) null);
		emailClass = new FastVector(3);
		emailClass.addElement("spam");
		emailClass.addElement("no spam");
		emailClass.addElement("?");
		eClass = new Attribute("emailClass", emailClass);

		records = new FastVector(2);
		records.addElement(eClass);
		records.addElement(emailMessage);

		testingSet = new Instances("SpamClsfyTesting", records, 40);
		testingSet.setClassIndex(0);

		this.readTestingDataset(dataset);

		ArffSaver saver = new ArffSaver();
		saver.setInstances(testingSet);
		saver.setFile(new File("test.arff"));
		saver.writeBatch();
	}

	private void createTrainingSet(String dataset) throws Exception {
		emailMessage = new Attribute("emailMessage", (FastVector) null);
		emailClass = new FastVector(3);
		emailClass.addElement("spam");
		emailClass.addElement("no spam");
		emailClass.addElement("?");
		eClass = new Attribute("emailClass", emailClass);

		records = new FastVector(2);
		records.addElement(eClass);
		records.addElement(emailMessage);

		trainingSet = new Instances("SpamClsfyTraining", records, 40);
		trainingSet.setClassIndex(0);

		this.readTrainingDataset(dataset);

		ArffSaver saver = new ArffSaver();
		saver.setInstances(trainingSet);
		saver.setFile(new File("training.arff"));
		saver.writeBatch();
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
		StringToWordVector stringToVector = new StringToWordVector(1000);
		stringToVector.setInputFormat(trainingSet);
		stringToVector.setOutputWordCounts(true);
		Instances filteredData = Filter.useFilter(trainingSet, stringToVector);

		// For now, using only training set of 80 samples. 40 for spam and 40 for non-spam.		
		// Instances filteredTestData = Filter.useFilter(testingSet, stringToVector);

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
		//System.out.println(cModel);
		
		// Print other information, evaluation results.
		Evaluation eTest = new Evaluation(filteredData);
		eTest.evaluateModel(cModel, filteredData);
		System.out.println(eTest.toSummaryString(true));
		System.out.println(eTest.toClassDetailsString());
		System.out.println(eTest.toMatrixString());
	}

	/**
	 * Reading the .txt files containing the data.
	 * 
	 * @param dataset
	 * @throws IOException
	 */
	private void readTrainingDataset(String dataset) throws IOException {

		ArrayList<String> fileNames = new ArrayList<String>();
		ArrayList<String> dataFiles = this.listFiles(dataset, fileNames);
		String data = "";
		for (int looper = 0; looper < dataFiles.size(); looper++) {
			data = this.readFileAsString(dataFiles.get(looper));
			Instance rec = new Instance(2);
			if (dataFiles.get(looper).contains("nospam")) {
				rec.setValue((Attribute) records.elementAt(0), "no spam");
			} else
				rec.setValue((Attribute) records.elementAt(0), "spam");
			rec.setValue((Attribute) records.elementAt(1), data);
			trainingSet.add(rec);
		}
	}

	private void readTestingDataset(String dataset) throws IOException {

		ArrayList<String> fileNames = new ArrayList<String>();
		ArrayList<String> dataFiles = this.listFiles(dataset, fileNames);
		String data = "";
		for (int looper = 0; looper < dataFiles.size(); looper++) {
			data = this.readFileAsString(dataFiles.get(looper));
			Instance rec = new Instance(2);
			if (dataFiles.get(looper).contains("nospam")) {
				rec.setValue((Attribute) records.elementAt(0), "no spam");
			} else
				rec.setValue((Attribute) records.elementAt(0), "spam");
			rec.setValue((Attribute) records.elementAt(1), data);
			testingSet.add(rec);
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
}
