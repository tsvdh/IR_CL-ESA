package edu.kit.aifb.terrier.concept;

import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.Index;

import edu.kit.aifb.concept.IConceptVector;
import edu.kit.aifb.terrier.tem.ITermEstimateModel;

public interface IConceptModel {

	public WeightingModel getWeightingModel();
	
	public ITermEstimateModel getTermEstimatModel();
	
	public IConceptVector getConceptVector(
			String docName, 
			String[] queryTerms, int[] queryTermFrequencies,
			double[] queryTermEstimates, double[] smoothingWeights,
			double[][] docScores, short[] support );
	
	public void setIndex( Index index );
	
}
