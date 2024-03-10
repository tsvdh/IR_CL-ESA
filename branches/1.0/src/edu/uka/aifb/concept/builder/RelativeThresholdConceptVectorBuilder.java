package edu.uka.aifb.concept.builder;

import org.apache.commons.configuration.Configuration;

import edu.uka.aifb.api.concept.IConceptIterator;
import edu.uka.aifb.api.concept.IConceptVector;
import edu.uka.aifb.api.concept.IConceptVectorBuilder;
import edu.uka.aifb.concept.TroveConceptVector;
import edu.uka.aifb.tools.ConfigurationManager;


public class RelativeThresholdConceptVectorBuilder implements IConceptVectorBuilder {

	static final String[] REQUIRED_PROPERTIES = {
		"concepts.builder.threshold.relative_threshold"
	};
	
	double m_threshold;
	IConceptVector cv;
	
	public RelativeThresholdConceptVectorBuilder() {
		Configuration config = ConfigurationManager.getCurrentConfiguration();
		ConfigurationManager.checkProperties( config, REQUIRED_PROPERTIES );
		m_threshold = config.getDouble( "concepts.builder.threshold.relative_threshold" );
	}

	@Override
	public void addScores(int[] conceptIds, double[] conceptScores) {
		for( int i=0; i<conceptIds.length && conceptScores[i] > 0; i++ ) {
			cv.add( conceptIds[i], conceptScores[i] );
		}
	}

	@Override
	public void addScores( IConceptVector oldCv ) {
		IConceptIterator it = oldCv.iterator();
		while( it.next() ) {
			cv.add( it.getId(), it.getValue() );
		}
	}

	@Override
	public IConceptVector getConceptVector() {
		IConceptVector newCv = new TroveConceptVector( cv.getData().getDocName(), cv.size() );
		IConceptIterator it = cv.orderedIterator();
		if( it.next() ) {
			double maxScore = it.getValue();
			newCv.set( it.getId(), it.getValue() );
			
			while( it.next() && it.getValue() > maxScore * m_threshold ) {
				newCv.set( it.getId(), it.getValue() );
			}
		}
		return newCv;
	}

	@Override
	public void reset(String docName, int maxConceptId) {
		cv = new TroveConceptVector( docName, maxConceptId );
	}
}
