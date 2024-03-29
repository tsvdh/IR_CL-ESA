package edu.kit.aifb.concept.builder.category;

import edu.kit.aifb.concept.IConceptVector;
import gnu.trove.TIntArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

public class MeanScoreTopDownCatTreeVectorBuilder extends
		TopDownCatTreeVectorBuilder {
	private static Log logger = LogFactory.getLog( MeanScoreTopDownCatTreeVectorBuilder.class );

	@Required @Override
	public void setSize( int size ) {
		super.setSize( size );
	}
	
	@Override
	void propagateScores( double[] scores ) {
		int[] treeSize = new int[scores.length];
		
		logger.debug( "Using tree to propagate scores" );
		TIntArrayList orderedCatIds = tree.getLeafOrderedDocIds();
		for( int j=0; j<orderedCatIds.size(); j++ ) {
			int catId = orderedCatIds.get( j );
			TIntArrayList subCatIds = tree.getSubCategoryDocIds( catId );

			treeSize[catId] = 1;
			for( int i=0; i<subCatIds.size(); i++ ) {
				int subCatId = subCatIds.get( i );
				treeSize[catId] += treeSize[subCatId];

				scores[catId] += treeSize[subCatId] * scores[subCatId];
			}

			scores[catId] /= treeSize[catId];
		}
	}

	public IConceptVector getConceptVector(String docName, int maxConceptId,
			int[] conceptIds, double[] conceptScores, int count) {
		reset( docName, maxConceptId );
		addScores( conceptIds, conceptScores, count );
		return getConceptVector();
	}

}
