package edu.kit.aifb.wikipedia.sql;

import java.sql.SQLException;
import java.util.Formatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import edu.kit.aifb.document.ICollection;
import edu.kit.aifb.document.ICollectionIterator;
import edu.kit.aifb.document.IDocument;
import edu.kit.aifb.document.TextDocument;
import gnu.trove.TIntArrayList;

public class WikipediaCollection implements ICollection {

	private static Log logger = LogFactory.getLog( WikipediaCollection.class );
	
	TIntArrayList articleIds;
	WikipediaDatabase wp;
	String idTable;
	String idColumn;
	
	@Required
	public void setWikipediaDatabase( WikipediaDatabase wp ) {
		this.wp = wp;
	}
	
	@Required
	public void setArticleIdTable( String idTable ) {
		this.idTable = idTable;
	}
	
	@Required
	public void setArticleIdColumn( String idColumn ) {
		this.idColumn = idColumn;
	}

	public void readArticles() throws SQLException {
		logger.info( "Reading article ids" );
		articleIds = wp.readArticleIds( idTable, idColumn );
	}
	
	public IDocument getDocument( String docName ) {
		int articleId = getArticleId( docName );
		return getDocument( articleId );
	}
	
	protected IDocument getDocument( int articleId ) {
		TextDocument doc = new TextDocument( getArticleName( articleId ) );

		try {
			Page p = new Page( articleId );
			wp.initializePage( p );
			String title = p.getTitle();
			logger.debug( "Building document for article " + title + " (" + articleId + ")" );
			String text = wp.getText( p );
			if( logger.isDebugEnabled() ) {
				System.out.println( text );
			}
			
			// clean title
			title = WikipediaTools.extractPlainTitle( title );
			
			// remove wiki markup
			if( text != null ) {
				text = WikipediaTools.extractPlainText( text );
			} else {
				text = "";
			}
			
			doc.setText( "title", wp.getLanguage(), title );
			doc.setText( "body", wp.getLanguage(), text );
		}
		catch( Exception e ) {
			logger.error( "Error while retrieving article " + articleId + ": " + e );
			e.printStackTrace();
		}	
		
		return doc;
	}

	public ICollectionIterator iterator() {
		return new WikipediaCollectioIterator( this );
	}

	public int size() {
		return articleIds.size();
	}

	public static String getArticleName( int articleId ) { 
		StringBuilder docIdStringBuilder = new StringBuilder();
		Formatter docIdFormatter = new Formatter( docIdStringBuilder );

		docIdFormatter.format( "%1$010d", articleId );
		return docIdStringBuilder.toString();
	}
	
	public static int getArticleId( String articleName ) {
		return Integer.parseInt( articleName );
	}
	
	class WikipediaCollectioIterator implements ICollectionIterator {

		private int m_index = -1;
		private WikipediaCollection m_col;
		
		public WikipediaCollectioIterator( WikipediaCollection col ) {
			m_index = -1;
			m_col = col;
		}
		
		public IDocument getDocument() {
			return m_col.getDocument( articleIds.get( m_index ) );
		}

		public boolean next() {
			m_index++;
			
			if( m_index % 100 == 0 ) {
				logger.info( "Read " + m_index + " articles." );
				//if( m_index > 2000 ) return false;
			}
			
			if( m_index < articleIds.size() ) {
				return true;
			}
			else {
				return false;
			}
		}
		
	}
}
