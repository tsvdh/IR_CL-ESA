package edu.uka.aifb.document.wikipedia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import edu.uka.aifb.tools.JdbcStatementBuffer;
import edu.uka.aifb.wikipedia.Page;
import edu.uka.aifb.wikipedia.WikipediaTools;
import edu.uka.aifb.wikipedia.sql.SQLWikipediaDatabase;
import gnu.trove.TIntArrayList;

public class WikipediaMLConceptCollection implements ICollection {

	static final String[] REQUIRED_PROPERTIES = {
		"collection.wikipedia.mlc_articles_table",
	};
	
	static Logger logger = Logger.getLogger( WikipediaMLConceptCollection.class );
	
	private Language m_language;
	private JdbcStatementBuffer m_jsb;
	
	private String m_mlcTable;
	
	private TIntArrayList m_conceptIds;
	private IWikipediaDatabase m_wp;
	
	public WikipediaMLConceptCollection( Configuration config, JdbcStatementBuffer jsb, Language language ) throws Exception {
		ConfigurationManager.checkProperties( config, REQUIRED_PROPERTIES );
	
		m_language = language;
		m_jsb = jsb;
	
		m_mlcTable = config.getString( "collection.wikipedia.mlc_articles_table" );
		
		m_wp = new SQLWikipediaDatabase( config, language );
		
		logger.info( "Reading concept ids from " + m_mlcTable );
		m_conceptIds = new TIntArrayList();
		PreparedStatement pst = m_jsb.getPreparedStatement(
				"select distinct mlc_id from " + m_mlcTable + " order by mlc_id;" );
		ResultSet rs = pst.executeQuery();
		while( rs.next() ) {
			m_conceptIds.add( rs.getInt( 1 ) );
		}
		logger.info( "Found " + m_conceptIds.size() + " concepts." );
	}
	
	@Override
	public IDocument getDocument( String docName ) {
		int articleId = buildConceptId( docName );
		return buildDocument( articleId );
	}
	
	protected IDocument buildDocument( int conceptId ) {
		TextDocument doc = new TextDocument( buildConceptName( conceptId ) );
		StringBuilder content = new StringBuilder();
		
		try {
			PreparedStatement pst = m_jsb.getPreparedStatement(
					"select mlc_page from " + m_mlcTable + " where mlc_id=? and mlc_lang=?;" );
			pst.setInt( 1, conceptId );
			pst.setString( 2, m_language.toString() );
			ResultSet pageResultSet = pst.executeQuery();
			while( pageResultSet.next() ) {
				int pageId = pageResultSet.getInt( 1 );

				Page p = new Page( pageId );
				m_wp.initializePage( p );
				String title = p.getTitle();
				logger.debug( "Building document for article " + title + " (" + pageId + ")" );
				String text = m_wp.getText( p );
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
		
		doc.setText( m_language.toString(), m_language, content.toString() );
		return doc;
	}

	@Override
	public ICollectionIterator iterator() {
		return new WikipediaMLConceptCollectionIterator();
	}

	@Override
	public int size() {
		return m_conceptIds.size();
	}

	public static String buildConceptName( int conceptId ) { 
		StringBuilder docNameBuilder = new StringBuilder();
		Formatter docIdFormatter = new Formatter( docNameBuilder );

		docIdFormatter.format( "%1$010d", conceptId );
		return docNameBuilder.toString();
	}
	
	public static int buildConceptId( String docName ) {
		return Integer.parseInt( docName );
	}
	
	class WikipediaMLConceptCollectionIterator implements ICollectionIterator {

		private int m_index = -1;
		private IDocument currentDoc;
		
		public WikipediaMLConceptCollectionIterator() {
			m_index = -1;
		}
		
		@Override
		public IDocument getDocument() {
			return currentDoc;
		}

		@Override
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
			
			if( m_index < m_conceptIds.size() ) {
				currentDoc = buildDocument( m_conceptIds.get( m_index ) );
				return true;
			}
			else {
				currentDoc = null;
				return false;
			}
		}
		
	}
}
