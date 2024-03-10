package edu.kit.aifb.wikipedia.sql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import edu.kit.aifb.document.IDocument;
import edu.kit.aifb.document.TextDocument;
import edu.kit.aifb.wikipedia.mlc.MLCDatabase;
import gnu.trove.TIntArrayList;

public class WikipediaMLCCategoryCollection extends WikipediaMLCArticleCollection {

	private static Log logger = LogFactory.getLog( WikipediaMLCCategoryCollection.class );
	
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
	
	protected IDocument buildDocument( int categoryId ) {
		TextDocument doc = new TextDocument( MLCDatabase.getConceptName( categoryId ) );
		StringBuilder content = new StringBuilder();
		
		int conceptCount = 0;
		int articleCount = 0;

		try {
			TIntArrayList mlcArticleIds = mlcCategoryDb.getMlcArticleIdsInCategory( categoryId );
			conceptCount = mlcArticleIds.size();
			
			for( int i=0; i<mlcArticleIds.size(); i++ ) {
				int conceptId = mlcArticleIds.get(i);
				
				if( logger.isDebugEnabled() )
					logger.debug( "Retrieving articles of concept " + conceptId + " in language " + wp.getLanguage() );
				TIntArrayList articleIds = mlcArticleDb.getPageIds( conceptId, wp.getLanguage() );
				articleCount += articleIds.size();
				
				for( int j=0; j<articleIds.size(); j++ ) {
					int pageId = articleIds.get(j);
					articleCount++;
					
					Page p = new Page( pageId );
					wp.initializePage( p );
					if( !p.isInitialized() ) {
						logger.warn( "Page " + pageId + "@" + wp.getLanguage() + " was not found." );
						continue;
					}
					String title = p.getTitle();
					logger.debug( "Building document for article " + title + " (" + pageId + ")" );
					String text = wp.getText( p );
					if( logger.isTraceEnabled() ) {
						System.out.println( text );
					}

					// clean title
					content.append( WikipediaTools.extractPlainTitle( title ) ).append( "\n" );

					// remove wiki markup
					if( text != null ) {
						content.append( WikipediaTools.extractPlainText( text ) ).append( "\n" );
					}
				}
			}
		}
		catch( Exception e ) {
			logger.error( "Error while retrieving concept " + categoryId + ": " + e );
			//e.printStackTrace();
		}	
		
		doc.setText( wp.getLanguage().toString(), wp.getLanguage(), content.toString() );
		logger.info( "Build document for category " + categoryId + " (Linked concepts: " + conceptCount + ", articles: " + articleCount + ")" );
		return doc;
	}
}
