package edu.uka.aifb.concept.builder;

import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import uk.ac.gla.terrier.sorting.HeapSort;
import edu.uka.aifb.api.concept.IConceptIterator;
import edu.uka.aifb.api.concept.IConceptVector;
import edu.uka.aifb.api.concept.IConceptVectorBuilder;
import edu.uka.aifb.concept.TroveConceptVector;
import edu.uka.aifb.tools.ConfigurationManager;


public class FixedSizeConceptVectorBuilder implements IConceptVectorBuilder {

	static final String[] REQUIRED_PROPERTIES = {
		"concepts.builder.fixed_size.size"
	};
	
	static Logger logger = Logger.getLogger( FixedSizeConceptVectorBuilder.class );
	
	int m_size;
	String docName;
	int[] ids;
	double[] values;
	
	public FixedSizeConceptVectorBuilder() {
		Configuration config = ConfigurationManager.getCurrentConfiguration();
		ConfigurationManager.checkProperties( config, REQUIRED_PROPERTIES );
		
		m_size = config.getInt( "concepts.builder.fixed_size.size" );
		logger.info( "Initializing: size=" + m_size );
	}

	public void setSize( int size ) {
		m_size = size;
		logger.info( "New settings: size=" + m_size );
	}
	
	@Override
	public void reset( String docName, int maxConceptId ) {
		this.docName = docName;
		if( ids == null || maxConceptId != ids.length ) {
			ids = new int[maxConceptId];
			values = new double[maxConceptId];
		}
		else {
			Arrays.fill( values, 0 );
		}
		for( int i=0; i<ids.length; i++ ) {
			ids[i] = i;
		}
	}
	
	@Override
	public void addScores( int[] conceptIds, double[] conceptScores ) {
		for( int i=0; i<conceptIds.length && conceptScores[i] > 0; i++ ) {
			values[ conceptIds[i] ] += conceptScores[i];
		}
	}

	@Override
	public void addScores( IConceptVector oldCv ) {
		IConceptIterator it = oldCv.iterator();
		while( it.next() ) {
			values[ it.getId() ] += it.getValue();
		}
	}
	
	@Override
	public IConceptVector getConceptVector() {
		HeapSort.heapSort( values, ids );

		IConceptVector newCv = new TroveConceptVector( docName, ids.length );
		for( int i=ids.length-1; i>=0 && i>=ids.length - m_size; i-- ) {
			newCv.set( ids[i], values[i] );
		}
		return newCv;
	}
	
}
