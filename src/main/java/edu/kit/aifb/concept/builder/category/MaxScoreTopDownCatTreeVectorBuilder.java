package edu.kit.aifb.concept.builder.category;

import edu.kit.aifb.concept.IConceptVector;
import gnu.trove.TIntArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

public class MaxScoreTopDownCatTreeVectorBuilder extends
		TopDownCatTreeVectorBuilder {
	private static Log logger = LogFactory.getLog( MaxScoreTopDownCatTreeVectorBuilder.class );

	double factor = 1.0;
	
	@Required @Override
	public void setSize( int size ) {
		super.setSize( size );
	}

	public void setThreshold( double factor ) {
		logger.info( "Setting new factor: " + Double.toString( factor ) );
		this.factor = factor;
	}
	
	@Override
	void propagateScores( double[] scores ) {
		logger.debug( "Using tree to propagate scores" );
		TIntArrayList orderedCatIds = tree.getLeafOrderedDocIds();
		for( int j=0; j<orderedCatIds.size(); j++ ) {
			int catId = orderedCatIds.get( j );
			TIntArrayList subCatIds = tree.getSubCategoryDocIds( catId );

			for( int i=0; i<subCatIds.size(); i++ ) {
				int subCatId = subCatIds.get( i );

				scores[catId] = Math.max( scores[catId], scores[subCatId] * factor );
			}
		}
	}

	public IConceptVector getConceptVector(String docName, int maxConceptId,
			int[] conceptIds, double[] conceptScores, int count) {
		reset( docName, maxConceptId );
		addScores( conceptIds, conceptScores, count );
		return getConceptVector();
	}

}
