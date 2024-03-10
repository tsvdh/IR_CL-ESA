package edu.uka.aifb.concept.builder;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.uka.aifb.api.concept.IConceptIterator;
import edu.uka.aifb.api.concept.IConceptVector;
import edu.uka.aifb.api.concept.IConceptVectorBuilder;
import edu.uka.aifb.concept.TroveConceptVector;
import edu.uka.aifb.tools.ConfigurationManager;


public class SlidingWindowConceptVectorBuilder implements IConceptVectorBuilder {

	static final String[] REQUIRED_PROPERTIES = {
		"concepts.builder.sliding_window.rel_threshold",
		"concepts.builder.sliding_window.window_size"
	};

	static Logger logger = Logger.getLogger( SlidingWindowConceptVectorBuilder.class );
	IConceptVector cv;
	
	int m_windowSize;
	double m_relThreshold;
	
	public SlidingWindowConceptVectorBuilder() {
		Configuration config = ConfigurationManager.getCurrentConfiguration();
		ConfigurationManager.checkProperties( config, REQUIRED_PROPERTIES );
		m_windowSize = config.getInt( "concepts.builder.sliding_window.window_size" );
		m_relThreshold = config.getDouble( "concepts.builder.sliding_window.rel_threshold" );
	}

	@Override
	public void addScores(int[] conceptIds, double[] conceptScores) {
		for( int i=0; i<conceptIds.length && conceptScores[i] > 0; i++ ) {
			cv.add( conceptIds[i], conceptScores[i] );
		}
	}

	@Override
	public void addScores(IConceptVector cv) {
		logger.error( "addScore( IConceptVector) is not implemented!" );
		//TODO Implementation
	}

	@Override
	public IConceptVector getConceptVector() {
		IConceptVector newCv = new TroveConceptVector( cv.getData().getDocName(), cv.size() );

		IConceptIterator it = cv.orderedIterator();
		for( int i=0; i<m_windowSize && it.next(); i++ ) {
			newCv.set( it.getId(), it.getValue() );
		}

		IConceptIterator windowIt = cv.orderedIterator();
		if( windowIt.next() ) {
			double maxScore = windowIt.getValue();
			if( logger.isTraceEnabled() ) {
				logger.trace( "Max score=" + maxScore );
			}

			while( it.next() && windowIt.next() ) {
				double difference = windowIt.getValue() - it.getId(); 
				if( logger.isTraceEnabled() ) {
					logger.trace( "difference=" + difference );
				}

				if( difference <= maxScore * m_relThreshold ) {
					break;
				}
				
				newCv.add( it.getId(), it.getValue() );
			}
		}
		return newCv;
	}

	@Override
	public void reset(String docName, int maxConceptId) {
		cv = new TroveConceptVector( docName, maxConceptId );
	}


}
