package edu.kit.aifb.concept.builder.category;

import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.context.ApplicationContextAware;

import edu.kit.aifb.concept.IConceptIndexAware;
import edu.kit.aifb.concept.IConceptVector;
import edu.kit.aifb.concept.IConceptVectorBuilder;
import edu.kit.aifb.concept.TroveConceptVector;
import edu.kit.aifb.wikipedia.mlc.MLCategoryTreeItem;
import gnu.trove.TIntArrayList;

public abstract class TopDownCatTreeVectorBuilder
		extends CatTreeVectorBuilder
		implements IConceptVectorBuilder, IConceptIndexAware, ApplicationContextAware {
	
	@Override
	public IConceptVector getConceptVector() {
		IConceptVector cv = new TroveConceptVector( docName, scores.length );

		if( loadTree() ) {
			propagateScores( scores );
			
			SortedSet<MLCategoryTreeItem> stack = new TreeSet<MLCategoryTreeItem>();
			int rootDocId = tree.getRootCategoryDocId();
			stack.add( new MLCategoryTreeItem( rootDocId, scores[rootDocId] ) );
			//logger.info( "First item: conceptId="  + rootDocId + ", score=" + conceptScores[rootDocId] );

			int conceptCount = 0;
			while( conceptCount < size && !stack.isEmpty() ) {
				MLCategoryTreeItem item = stack.first();
				stack.remove( item );

				TIntArrayList subCatDocIds = tree.getSubCategoryDocIds( item.docId );
				for( int i=0; i<subCatDocIds.size(); i++ ) {
					int subCatDocId = subCatDocIds.get( i );
					if( scores[subCatDocId] > 0 ) {
						stack.add( new MLCategoryTreeItem( subCatDocId, scores[subCatDocId] ) );
					}
				}

				cv.add( item.docId, item.score );
				conceptCount++;
			}
		}
		
		return cv;
	}
	
}
