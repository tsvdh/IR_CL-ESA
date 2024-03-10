package edu.kit.aifb.wikipedia.mlc;

import edu.kit.aifb.concept.IConceptIndex;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MLCategoryTree implements Serializable {

	private static final long serialVersionUID = 4290902683258041836L;

	static Log logger = LogFactory.getLog( MLCategoryTree.class );
	
	int rootCategoryDocId;
	
	TIntArrayList leafDocIds;

	TIntArrayList[] docIdToSubDocIds;
	TIntArrayList[] docIdToSuperDocIds;
	
	TIntIntHashMap catIdToDocId; 
	
	TIntArrayList orderedDocIds;
	
	public MLCategoryTree( MLCategory rootCategory, IConceptIndex catIndex ) throws SQLException {
		
		logger.info( "Reading all document ids from index" );
		catIdToDocId = new TIntIntHashMap();
		int indexSize = catIndex.size();
		for( int i=0; i<indexSize; i++ ) {
			String docName = catIndex.getConceptName( i );
			catIdToDocId.put( MLCDatabase.getConceptId( docName ), i );
		}

		rootCategoryDocId = catIdToDocId.get( rootCategory.getId() );

		logger.info( "Reading all categories (breadth-first)" );
		leafDocIds = new TIntArrayList();
		docIdToSubDocIds = new TIntArrayList[indexSize];
		docIdToSuperDocIds = new TIntArrayList[indexSize];
		
		for( int i=0; i<indexSize; i++ ) {
			docIdToSuperDocIds[i] = new TIntArrayList();
		}
		
		Queue<MLCategory> catQueue = new LinkedList<MLCategory>();
		TIntHashSet seenCategories = new TIntHashSet();
		
		catQueue.add( rootCategory );
		
		while( !catQueue.isEmpty() ) {
			MLCategory cat = catQueue.poll();
			if( seenCategories.contains( cat.getId() ) ) {
				continue;
			}
			seenCategories.add( cat.getId() );
			int catDocId = catIdToDocId.get( cat.getId() );
			
			TIntArrayList subDocIds = new TIntArrayList();
			for( MLCategory subCat : cat.getSubCategories() ) {
				if( !seenCategories.contains( subCat.getId() ) ) {
					int subCatDocId = catIdToDocId.get( subCat.getId() );
					subDocIds.add( subCatDocId );
					catQueue.add( subCat );
					
					docIdToSuperDocIds[ subCatDocId ].add( catDocId );
				}
			}
			
			if( subDocIds.size() == 0 ) {
				leafDocIds.add( catDocId );
			}

			docIdToSubDocIds[ catDocId ] = subDocIds;
		}
		
		logger.info( "Building ordered list (depth-first)" );
		orderedDocIds = new TIntArrayList();
		buildLeafOrderedDocIdList( rootCategoryDocId );
		
		logger.info( "Reverting and deleting duplicates of ordered list" );
		TIntArrayList revertedOrderedDocIds = new TIntArrayList();
		TIntHashSet seenDocIds = new TIntHashSet();
		for( int i=orderedDocIds.size()-1; i>=0; i-- ) {
			int docId = orderedDocIds.get(i);
			if( !seenDocIds.contains( docId ) ) {
				revertedOrderedDocIds.add( docId );
				seenDocIds.add( docId );
			}
		}
		logger.info( "orderedDocIds.size(): " + orderedDocIds.size() );
		logger.info( "revertedOrderedDocIds.size(): " + revertedOrderedDocIds.size() );
		orderedDocIds = revertedOrderedDocIds;
	}
	
	private void buildLeafOrderedDocIdList( int docId ) {
		orderedDocIds.add( docId );

		TIntArrayList subDocIds = docIdToSubDocIds[docId];
		for( int i=0; i<subDocIds.size(); i++ ) {
			buildLeafOrderedDocIdList( subDocIds.get(i) );
		}
	}
	
	public int getRootCategoryDocId() {
		return rootCategoryDocId;
	}

	public TIntArrayList getSubCategoryDocIds( int categoryDocId ) {
		return docIdToSubDocIds[ categoryDocId ];
	}

	public TIntArrayList getSuperCategoryDocIds( int categoryDocId ) {
		return docIdToSuperDocIds[ categoryDocId ];
	}

	public TIntArrayList getLeafOrderedDocIds() {
		return orderedDocIds;
	}
	
	public TIntArrayList getLeafDocIds() {
		return leafDocIds;
	}
	
	public int size() {
		return orderedDocIds.size();
	}
}
