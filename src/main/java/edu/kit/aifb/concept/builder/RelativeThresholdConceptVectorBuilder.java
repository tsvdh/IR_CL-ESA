package edu.kit.aifb.concept.builder;

import org.springframework.beans.factory.annotation.Required;

import edu.kit.aifb.concept.IConceptIterator;
import edu.kit.aifb.concept.IConceptVector;
import edu.kit.aifb.concept.IConceptVectorBuilder;
import edu.kit.aifb.concept.TroveConceptVector;


public class RelativeThresholdConceptVectorBuilder implements IConceptVectorBuilder {

	static final String[] REQUIRED_PROPERTIES = {
		"concepts.builder.threshold.relative_threshold"
	};
	
	double threshold;
	IConceptVector cv;
	
	@Required
	public void setThreshold( double threshold ) {
		this.threshold = threshold;
	}
	
	public void addScores(int[] conceptIds, double[] conceptScores, int count ) {
		for( int i=0; i<count && i<conceptIds.length && conceptScores[i] > 0; i++ ) {
			cv.add( conceptIds[i], conceptScores[i] );
		}
	}

	public void addScores( IConceptVector oldCv ) {
		IConceptIterator it = oldCv.iterator();
		while( it.next() ) {
			cv.add( it.getId(), it.getValue() );
		}
	}

	public IConceptVector getConceptVector() {
		IConceptVector newCv = new TroveConceptVector( cv.getData().getDocName(), cv.size() );
		IConceptIterator it = cv.orderedIterator();
		if( it.next() ) {
			double maxScore = it.getValue();
			newCv.set( it.getId(), it.getValue() );
			
			while( it.next() && it.getValue() > maxScore * threshold ) {
				newCv.set( it.getId(), it.getValue() );
			}
		}
		return newCv;
	}

	public void reset(String docName, int maxConceptId) {
		cv = new TroveConceptVector( docName, maxConceptId );
	}
	
	public IConceptVectorBuilder clone() {
		RelativeThresholdConceptVectorBuilder newBuilder = new RelativeThresholdConceptVectorBuilder();
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
