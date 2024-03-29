package edu.kit.aifb.concept.builder;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.terrier.sorting.HeapSort;

import edu.kit.aifb.concept.IConceptIterator;
import edu.kit.aifb.concept.IConceptVector;
import edu.kit.aifb.concept.IConceptVectorBuilder;
import edu.kit.aifb.concept.TroveConceptVector;


public class FixedSizeConceptVectorBuilder implements IConceptVectorBuilder {
	static Log logger = LogFactory.getLog( FixedSizeConceptVectorBuilder.class );
	
	int size;
	String docName;
	int[] ids;
	double[] values;
	
	@Required
	public void setSize( int size ) {
		logger.info( "New settings: size=" + size );
		this.size = size;
	}
	
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
	
	public void addScores( int[] conceptIds, double[] conceptScores, int count ) {
		for( int i=0; i<count && i<conceptIds.length && conceptScores[i] > 0; i++ ) {
			values[ conceptIds[i] ] += conceptScores[i];
		}
	}

	public void addScores( IConceptVector oldCv ) {
		IConceptIterator it = oldCv.iterator();
		while( it.next() ) {
			values[ it.getId() ] += it.getValue();
		}
	}
	
	public IConceptVector getConceptVector() {
		HeapSort.heapSort( values, ids, size );

		IConceptVector newCv = new TroveConceptVector( docName, ids.length );
		for( int i=ids.length-1; i>=0 && i>=ids.length - size; i-- ) {
			newCv.set( ids[i], values[i] );
		}
		return newCv;
	}
	
	public IConceptVectorBuilder clone() {
		FixedSizeConceptVectorBuilder newBuilder = new FixedSizeConceptVectorBuilder();
		newBuilder.setSize( size );
		return newBuilder;
	}

	public IConceptVector getConceptVector( String docName, int maxConceptId,
			int[] conceptIds, double[] conceptScores, int count ) {
		IConceptVector newCv = new TroveConceptVector( docName, maxConceptId );
		int maxI = Math.min( size, Math.min( count, conceptIds.length ) );
		for( int i=0; i<maxI && conceptScores[i] > 0; i++ ) {
			newCv.set( conceptIds[i], conceptScores[i] );
		}
		return newCv;
	}
}
