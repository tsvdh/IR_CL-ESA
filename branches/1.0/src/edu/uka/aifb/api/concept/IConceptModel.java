package edu.uka.aifb.api.concept;

import uk.ac.gla.terrier.matching.models.WeightingModel;
import uk.ac.gla.terrier.structures.Index;
import edu.uka.aifb.api.ir.ITermEstimateModel;

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
