package edu.kit.aifb.concept.builder.category;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.terrier.sorting.HeapSort;

import edu.kit.aifb.concept.IConceptIndex;
import edu.kit.aifb.concept.IConceptIndexAware;
import edu.kit.aifb.concept.IConceptIterator;
import edu.kit.aifb.concept.IConceptVector;
import edu.kit.aifb.concept.IConceptVectorBuilder;
import edu.kit.aifb.concept.TroveConceptVector;
import edu.kit.aifb.wikipedia.mlc.MLCFactory;
import edu.kit.aifb.wikipedia.mlc.MLCategory;
import edu.kit.aifb.wikipedia.mlc.MLCategoryTree;

public abstract class CatTreeVectorBuilder implements
		IConceptVectorBuilder,
		IConceptIndexAware,
		ApplicationContextAware {
	private static Log logger = LogFactory.getLog( CatTreeVectorBuilder.class );
	
	int rootCategoryId = -1;
	
	MLCategoryTree tree;
	int size;
	
	MLCFactory factory;
	IConceptIndex conceptIndex;
	
	double[] scores;
	String docName;
	
	public void setRootCategoryId( int id ) {
		logger.info( "Setting root category id: " + id );
		rootCategoryId = id;
	}
	
	@Required
	public void setSize( int size ) {
		logger.info( "New parameter: size=" + size );
		this.size = size;
	}
	
	public void addScores( int[] conceptIds, double[] conceptScores, int count ) {
		for( int i=0; i<conceptIds.length && i<count; i++ ) {
			if( conceptScores[i] > 0 ) {
				scores[conceptIds[i]] += conceptScores[i];
			}
		}
	}

	public void addScores( IConceptVector newCv ) {
		IConceptIterator it = newCv.iterator();
		while( it.next() ) {
			scores[ it.getId() ] += it.getValue();
		}
	}

	abstract void propagateScores( double[] scores );
	
	public IConceptVector getConceptVector() {
		IConceptVector cv = new TroveConceptVector( docName, scores.length );

		if( loadTree() ) {
			propagateScores( scores );
			
			int[] ids = new int[scores.length];
			for( int i=0; i<ids.length; i++ ) {
				ids[i] = i;
			}
			
			HeapSort.heapSort( scores, ids );

			for( int i=ids.length-1; i>=0 && i>=ids.length - size; i-- ) {
				cv.set( ids[i], scores[i] );
			}
		}

		return cv;
	}

	public void reset( String docName, int maxConceptId ) {
		this.docName = docName;
		scores = new double[maxConceptId];
	}

	public void setConceptIndex(IConceptIndex conceptIndex) {
		this.conceptIndex = conceptIndex;
	}

	public void setApplicationContext( ApplicationContext context )
			throws BeansException {
		factory = (MLCFactory)context.getBean( MLCFactory.class );
	}

	protected boolean loadTree() {
		if( tree == null ) {
			try {
				MLCategory rootCat;
				if( rootCategoryId >= 0 ) {
					rootCat = factory.createMLCategory( rootCategoryId );
				}
				else {
					rootCat = factory.getRootCat();
				}
				logger.info( "Initializing category tree using root category " + rootCat );
				tree = new MLCategoryTree(
						rootCat,
						conceptIndex );
			}
			catch( Exception e ) {
				logger.error( "Could not build category tree: " + e );
				return false;
			}
		}			
		return true;
	}
	
	@Override
	public IConceptVectorBuilder clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();	
	}
	
}
