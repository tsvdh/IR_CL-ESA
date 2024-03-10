package edu.uka.aifb.document.wikipedia;

import java.util.Formatter;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.uka.aifb.api.document.ICollection;
import edu.uka.aifb.api.document.ICollectionIterator;
import edu.uka.aifb.api.document.IDocument;
import edu.uka.aifb.api.wikipedia.IWikipediaDatabase;
import edu.uka.aifb.document.TextDocument;
import edu.uka.aifb.nlp.Language;
import edu.uka.aifb.tools.ConfigurationManager;
import edu.uka.aifb.wikipedia.Page;
import edu.uka.aifb.wikipedia.WikipediaTools;
import edu.uka.aifb.wikipedia.sql.SQLWikipediaDatabase;
import gnu.trove.TIntArrayList;

public class WikipediaCollection implements ICollection {

	static final String[] REQUIRED_PROPERTIES = {
		"collection.wikipedia.%1.ids_table",
		"collection.wikipedia.%1.ids_column",
	};
	
	static Logger logger = Logger.getLogger( WikipediaCollection.class );
	
	private Language m_language;
	
	private TIntArrayList m_articleIds;
	private IWikipediaDatabase m_wp;
	
	public WikipediaCollection( Configuration config, Language language ) throws Exception {
		ConfigurationManager.checkProperties( config, REQUIRED_PROPERTIES, language );
	
		m_language = language;
		
		m_wp = new SQLWikipediaDatabase( config, language );
		
		logger.info( "Reading article ids" );
		m_articleIds = m_wp.readArticleIds(
				config.getString( "collection.wikipedia." + language + ".ids_table" ),
				config.getString( "collection.wikipedia." + language + ".ids_column" ) );
	}
	
	@Override
	public IDocument getDocument( String docName ) {
		int articleId = buildArticleId( docName );
		return getDocument( articleId );
	}
	
	protected IDocument getDocument( int articleId ) {
		TextDocument doc = new TextDocument( buildDocumentName( articleId ) );

		try {
			Page p = new Page( articleId );
			m_wp.initializePage( p );
			String title = p.getTitle();
			logger.debug( "Building document for article " + title + " (" + articleId + ")" );
			String text = m_wp.getText( p );
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
			
			doc.setText( "title", m_language, title );
			doc.setText( "body", m_language, text );
		}
		catch( Exception e ) {
			logger.error( "Error while retrieving article " + articleId + ": " + e );
			e.printStackTrace();
		}	
		
		return doc;
	}

	@Override
	public ICollectionIterator iterator() {
		return new WikipediaCollectioIterator( this );
	}

	@Override
	public int size() {
		return m_articleIds.size();
	}

	public static String buildDocumentName( int articleId ) { 
		StringBuilder docIdStringBuilder = new StringBuilder();
		Formatter docIdFormatter = new Formatter( docIdStringBuilder );

		docIdFormatter.format( "%1$010d", articleId );
		return docIdStringBuilder.toString();
	}
	
	public static int buildArticleId( String docName ) {
		return Integer.parseInt( docName );
	}
	
	class WikipediaCollectioIterator implements ICollectionIterator {

		private int m_index = -1;
		private WikipediaCollection m_col;
		
		public WikipediaCollectioIterator( WikipediaCollection col ) {
			m_index = -1;
			m_col = col;
		}
		
		@Override
		public IDocument getDocument() {
			return m_col.getDocument( m_articleIds.get( m_index ) );
		}

		@Override
		public boolean next() {
			m_index++;
			
			if( m_index % 100 == 0 ) {
				logger.info( "Read " + m_index + " articles." );
				//if( m_index > 2000 ) return false;
			}
			
			if( m_index < m_articleIds.size() ) {
				return true;
			}
			else {
				return false;
			}
		}
		
	}
}
