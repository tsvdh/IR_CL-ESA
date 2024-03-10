package edu.kit.aifb.wikipedia.sql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import edu.kit.aifb.document.ICollection;
import edu.kit.aifb.document.ICollectionIterator;
import edu.kit.aifb.document.IDocument;
import edu.kit.aifb.document.TextDocument;
import edu.kit.aifb.wikipedia.mlc.MLCDatabase;
import gnu.trove.TIntArrayList;

public class WikipediaMLCArticleCollection implements ICollection {

	private static Log logger = LogFactory.getLog( WikipediaMLCArticleCollection.class );
	
	WikipediaDatabase wp;
	MLCDatabase mlcArticleDb;
	
	@Required
	public void setWikipediaDatabase( WikipediaDatabase wp ) {
		this.wp = wp;
	}
	
	@Required
	public void setMlcArticleDatabase( MLCDatabase mlcDb ) {
		this.mlcArticleDb = mlcDb;
	}
	
	public IDocument getDocument( String docName ) {
		int articleId = MLCDatabase.getConceptId( docName );
		return buildDocument( articleId );
	}
	
	protected IDocument buildDocument( int conceptId ) {
		TextDocument doc = new TextDocument( MLCDatabase.getConceptName( conceptId ) );
		StringBuilder content = new StringBuilder();
		
		try {
			TIntArrayList articleIds = mlcArticleDb.getPageIds( conceptId, wp.getLanguage() );
			for( int i=0; i<articleIds.size(); i++ ) {
				int articleId = articleIds.get(i);

				Page p = new Page( articleId );
				wp.initializePage( p );
				String title = p.getTitle();
				logger.debug( "Building document for article " + title + " (" + articleId + ")" );
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
		catch( Exception e ) {
			logger.error( "Error while retrieving concept " + conceptId + ": " + e );
			//e.printStackTrace();
		}	
		
		doc.setText( wp.getLanguage().toString(), wp.getLanguage(), content.toString() );
		return doc;
	}

	public ICollectionIterator iterator() {
		return new WikipediaMLConceptCollectionIterator();
	}

	public int size() {
		return mlcArticleDb.size();
	}

	class WikipediaMLConceptCollectionIterator implements ICollectionIterator {

		private int m_index = -1;
		private TIntArrayList conceptIds;
		private IDocument currentDoc;
		
		public WikipediaMLConceptCollectionIterator() {
			m_index = -1;
			conceptIds = mlcArticleDb.getConceptIds();
		}
		
		public IDocument getDocument() {
			return currentDoc;
		}

		public boolean next() {
			m_index++;
			/*
			 * DEBUG stop after 500 articles
			 */
			/*if( m_index > 10000 ) {
				return false;
			}*/
			
			if( m_index % 100 == 0 ) {
				logger.info( "Read " + m_index + " concepts." );
			}
			
			if( m_index < conceptIds.size() ) {
				currentDoc = buildDocument( conceptIds.get( m_index ) );
				return true;
			}
			else {
				currentDoc = null;
				return false;
			}
		}
		
	}
}
