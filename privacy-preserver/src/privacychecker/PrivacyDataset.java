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
		classifier.createSet();

		classifier.performClustering(new SimpleKMeans(), "KMEANS CLUSTERER");

		// classifier.performClassification(new NaiveBayesMultinomial(),
		// "NAIVE BAYES MULTINOMIAL");

		// Other algorithms
		// classifier.performClassification(new NaiveBayes(), "NAIVE BAYES");
		// classifier.performClassification(new J48(), "J48 (C4.5)");
		// classifier.performClassification(new AdaBoostM1(), "ADA BOOST M1");
	}

	private void createSet() throws IOException, SQLException,
			ClassNotFoundException {

		websitePrivacyType = new FastVector(3);
		websitePrivacyType.addElement("good");
		websitePrivacyType.addElement("medium");
		websitePrivacyType.addElement("bad");
		websitePrivacyType.addElement("?");
		websiteClass = new Attribute("websitePrivacyType", websitePrivacyType);

		websitePrivacyStmt = new Attribute("websitePrivacyStmt",
				(FastVector) null);

		records = new FastVector(2);
		records.addElement(websiteClass);
		records.addElement(websitePrivacyStmt);

		stmtSet = new Instances("PrivacyStmtsDataset", records, 1303);
		stmtSet.setClassIndex(0);

		readDataset();

		// ArffSaver saver = new ArffSaver();
		// saver.setInstances(stmtSet);
		// saver.setFile(new File("dataset.arff"));
		// saver.writeBatch();
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
		stringToVector.setUseStoplist(true);
		stringToVector.setInputFormat(stmtSet);
		stringToVector.setOutputWordCounts(true);
		stringToVector
				.setOptions(weka.core.Utils
						.splitOptions("-R first-last -W 1000 -prune-rate -1.0 -C -N 0 -S -stemmer weka.core.stemmers.SnowballStemmer -M 1 -tokenizer \"weka.core.tokenizers.NGramTokenizer -max 6 -min 2\"")); // -delimiters \" \r\n\t.,;:\'\"()?!\"
		Instances filteredData = Filter.useFilter(stmtSet, stringToVector);

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
						.splitOptions("-R first-last -W 1000 -prune-rate -1.0 -C -N 0 -S -stemmer weka.core.stemmers.SnowballStemmer -M 1 -tokenizer \"weka.core.tokenizers.NGramTokenizer -max 6 -min 2\""));
		Instances filteredData = Filter.useFilter(stmtSet, stringToVector);

		// For now, using only training set of 80 samples. 40 for spam and 40
		// for non-spam.
		// Instances filteredTestData = Filter.useFilter(testingSet,
		// stringToVector);

		// Classifier cModel = (Classifier) new NaiveBayes();
		
		model.setOptions(weka.core.Utils.splitOptions("-N 3 -A \"weka.core.EuclideanDistance -R first-last\" -I 500 -S 10"));
		model.buildClusterer(filteredData);
		ClusterEvaluation eval = new ClusterEvaluation();
		eval.setClusterer(model);
		eval.evaluateClusterer(filteredData);
		System.out.println(eval.clusterResultsToString());
	}

	private void readDataset() throws IOException, SQLException,
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
