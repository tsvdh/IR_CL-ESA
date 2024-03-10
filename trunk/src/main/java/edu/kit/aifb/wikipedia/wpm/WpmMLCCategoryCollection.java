package edu.kit.aifb.wikipedia.wpm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import edu.kit.aifb.document.ICollectionIterator;
import edu.kit.aifb.document.IDocument;
import edu.kit.aifb.document.TextDocument;
import edu.kit.aifb.wikipedia.mlc.MLCDatabase;
import gnu.trove.TIntArrayList;

public class WpmMLCCategoryCollection extends WpmMLCArticleCollection {

	private static Log logger = LogFactory.getLog( WpmMLCCategoryCollection.class );
	
	MLCDatabase mlcCategoryDb;
	
	@Required
	public void setMlcCategoryDatabase( MLCDatabase mlcDb ) {
		mlcCategoryDb = mlcDb;
	}
	
	@Override
	public IDocument getDocument( String docName ) {
		int articleId = MLCDatabase.getConceptId( docName );
		return buildDocument( articleId );
	}
	
	@Override
	public ICollectionIterator iterator() {
		return new CollectionIterator( mlcCategoryDb.getConceptIds() );
	}

	@Override
	protected IDocument buildDocument( int categoryId ) {
		TextDocument doc = new TextDocument( MLCDatabase.getConceptName( categoryId ) );
		StringBuilder titleBuilder = new StringBuilder();
		StringBuilder redirectBuilder = new StringBuilder();
		StringBuilder contentBuilder = new StringBuilder();
		StringBuilder anchorBuilder = new StringBuilder();
		
		try {
			TIntArrayList mlcArticleIds = mlcCategoryDb.getMlcArticleIdsInCategory( categoryId );
			if( logger.isDebugEnabled() )
				logger.debug( "Building document for category " + categoryId + " (Linked concepts: " + mlcArticleIds.size() + ")" );

			for( int i=0; i<mlcArticleIds.size(); i++ ) {
				int conceptId = mlcArticleIds.get(i);
				
				try {
					if( logger.isDebugEnabled() )
						logger.debug( "Retrieving articles of concept " + conceptId + " in language " + language );

					IDocument articleDoc = super.buildDocument( conceptId );
					titleBuilder.append( articleDoc.getText( "title" ) );
					redirectBuilder.append( articleDoc.getText( "redirect" ) );
					contentBuilder.append( articleDoc.getText( "content" ) );
					anchorBuilder.append( articleDoc.getText( "anchor" ) );
				}
				catch( Exception e ) {
					logger.error( "Error while building document for concept " + conceptId + ": " + e );
				}
			}
		}
		catch( Exception e ) {
			logger.error( "Error while building document for category " + categoryId + ": " + e );
		}	
		
		doc.setText( "title", language, titleBuilder.toString() );
		doc.setText( "redirect", language, redirectBuilder.toString() );
		doc.setText( "content", language, contentBuilder.toString() );
		doc.setText( "anchor", language, anchorBuilder.toString() );
		return doc;
	}
	
	@Override
	public int size() {
		return mlcCategoryDb.size();
	}
}
