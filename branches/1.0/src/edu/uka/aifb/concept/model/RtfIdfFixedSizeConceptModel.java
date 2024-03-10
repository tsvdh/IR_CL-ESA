package edu.uka.aifb.concept.model;

import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import uk.ac.gla.terrier.matching.models.WeightingModel;
import uk.ac.gla.terrier.structures.Index;
import uk.ac.gla.terrier.utility.HeapSort;
import edu.uka.aifb.api.concept.IConceptModel;
import edu.uka.aifb.api.concept.IConceptVector;
import edu.uka.aifb.api.ir.ITermEstimateModel;
import edu.uka.aifb.concept.TroveConceptVector;
import edu.uka.aifb.ir.model.IdfTermEstimateModel;
import edu.uka.aifb.ir.terrier.model.RtfModel;
import edu.uka.aifb.tools.ConfigurationManager;

public class RtfIdfFixedSizeConceptModel implements IConceptModel {

	static final String[] REQUIRED_PROPERTIES = {
		"concepts.builder.fixed_size.size"
	};

	static Logger logger = Logger.getLogger( RtfIdfFixedSizeConceptModel.class );
	
	ITermEstimateModel termEstimateModel;
	WeightingModel model;

	int fixedSize;
	
	double[] scores;
	int[] ids;
	
	public RtfIdfFixedSizeConceptModel() {
		Configuration config = ConfigurationManager.getCurrentConfiguration();
		ConfigurationManager.checkProperties( config, REQUIRED_PROPERTIES );
		
		fixedSize = config.getInt( "concepts.builder.fixed_size.size" );
		logger.info( "Initializing: size=" + fixedSize );

		termEstimateModel = new IdfTermEstimateModel();
		model = new RtfModel();
	}
	
	@Override
	public ITermEstimateModel getTermEstimatModel() {
		return termEstimateModel;
	}

	@Override
	public WeightingModel getWeightingModel() {
		return model;
	}

	private void initialize( int numberOfConcepts ) {
		if( scores == null || scores.length != numberOfConcepts ) {
			scores = new double[numberOfConcepts];
			ids = new int[numberOfConcepts];
		}
		else {
			Arrays.fill( scores, 0d );
		}
		for( int i=0; i<numberOfConcepts; i++ ) {
			ids[i] = i;
		}
	}
	
	@Override
	public IConceptVector getConceptVector(
			String docName,
			String[] queryTerms,
			int[] queryTermFrequencies, double[] queryTermEstimates,
			double[] smoothingWeights, double[][] docScores, short[] support )
	{
		initialize( support.length );
		
		for( int i=0; i<support.length; i++ ) {
			if( support[i] > 0 ) {
				for( int termId=0; termId<queryTerms.length; termId++ ) {
					scores[i] += queryTermFrequencies[termId] * docScores[termId][i] * queryTermEstimates[termId];
				}
			}
		}

		HeapSort.descendingHeapSort( scores, ids, support );
		
		IConceptVector cv = new TroveConceptVector( docName, ids.length );
		for( int i=0; i<fixedSize && i<ids.length; i++ ) {
			 cv.set( ids[i], scores[i] );
		}
		return cv;
	}

	@Override
	public void setIndex(Index index) {
		termEstimateModel.setIndex( index );
	}

}
