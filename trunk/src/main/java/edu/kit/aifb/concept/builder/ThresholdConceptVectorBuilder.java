package edu.kit.aifb.concept.builder;

import org.springframework.beans.factory.annotation.Required;

import edu.kit.aifb.concept.IConceptIterator;
import edu.kit.aifb.concept.IConceptVector;
import edu.kit.aifb.concept.IConceptVectorBuilder;
import edu.kit.aifb.concept.TroveConceptVector;


public class ThresholdConceptVectorBuilder implements IConceptVectorBuilder {

	static final String[] REQUIRED_PROPERTIES = {
		"concepts.builder.threshold.absolute_threshold"
	};
	
	double threshold;
	IConceptVector cv;
	
	@Required
	public void setThreshold( double threshold ) {
		this.threshold = threshold;
	}

	public void addScores(int[] conceptIds, double[] conceptScores, int count ) {
		for( int i=0; i<count && i<conceptIds.length && conceptScores[i] > 0; i++ ) {
			if( conceptScores[i] > threshold ) {
				cv.add( conceptIds[i], conceptScores[i] );
			}
		}
	}

	public void addScores(IConceptVector cv) {
		IConceptIterator it = cv.iterator();
		while( it.next() ) {
			if( it.getValue() > threshold ) {
				cv.add( it.getId(), it.getValue() );
			}
		}
	}

	public IConceptVector getConceptVector() {
		return cv;
	}

	public void reset(String docName, int maxConceptId) {
		cv = new TroveConceptVector( docName, maxConceptId );
	}

	@Override
	public IConceptVectorBuilder clone() {
		ThresholdConceptVectorBuilder newBuilder = new ThresholdConceptVectorBuilder();
		newBuilder.setThreshold( threshold );
		return newBuilder;
	}

	public IConceptVector getConceptVector(String docName, int maxConceptId,
			int[] conceptIds, double[] conceptScores, int count) {
		reset( docName, maxConceptId );
		addScores( conceptIds, conceptScores, count );
		return getConceptVector();
	}

}
