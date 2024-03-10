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

public class WikipediaMLCategoryCollection implements ICollection {

	static final String[] REQUIRED_PROPERTIES = {
		"collection.wikipedia.mlc_articles_table",
		"collection.wikipedia.mlc_categories_table",
		"collection.wikipedia.mlc_categorylinks_table",
	};
	
	static Logger logger = Logger.getLogger( WikipediaMLCategoryCollection.class );
	
	private Language language;
	private JdbcStatementBuffer jsb;
	
	private String mlcArticlesTable;
	private String mlcCategoriesTable;
	private String mlcCategorylinksTable;
	
	private TIntArrayList categoryIds;
	private IWikipediaDatabase wp;
	
	public WikipediaMLCategoryCollection( Configuration config, JdbcStatementBuffer jsb, Language language ) throws Exception {
		ConfigurationManager.checkProperties( config, REQUIRED_PROPERTIES );
	
		this.language = language;
		this.jsb = jsb;
	
		mlcArticlesTable = config.getString( "collection.wikipedia.mlc_articles_table" );
		mlcCategoriesTable = config.getString( "collection.wikipedia.mlc_categories_table" );
		mlcCategorylinksTable = config.getString( "collection.wikipedia.mlc_categorylinks_table" );
		
		wp = new SQLWikipediaDatabase( config, language );
		
		logger.info( "Reading category ids" );
		categoryIds = new TIntArrayList();
		ResultSet rs = jsb.getStatement().executeQuery(
				"select distinct mlc_id from " + mlcCategoriesTable + " order by mlc_id;" );
		while( rs.next() ) {
			categoryIds.add( rs.getInt( 1 ) );
		}
		logger.info( "Found " + categoryIds.size() + " categories." );
	}
	
	@Override
	public IDocument getDocument( String docName ) {
		int articleId = buildConceptId( docName );
		return buildDocument( articleId );
	}
	
	protected IDocument buildDocument( int categoryId ) {
		TextDocument doc = new TextDocument( buildConceptName( categoryId ) );
		StringBuilder content = new StringBuilder();
		
		int linkCount = 0;
		int articleCount = 0;
		try {
			PreparedStatement pstLinks = jsb.getPreparedStatement(
					"select mlcl_from from " + mlcCategorylinksTable
					+ " where mlcl_namespace=0 and mlcl_to=?;" );
			
			PreparedStatement pstArticles = jsb.getPreparedStatement(
					"select mlc_page from " + mlcArticlesTable + " where mlc_id=? and mlc_lang=?;" );
			
			if( logger.isDebugEnabled() )
				logger.debug( "Retrieving mlc concepts linking to category " + categoryId );
			pstLinks.setInt( 1, categoryId );
			ResultSet linkResultSet = pstLinks.executeQuery();
			while( linkResultSet.next() ) {
				int conceptId = linkResultSet.getInt( 1 );
				linkCount++;
				
				if( logger.isDebugEnabled() )
					logger.debug( "Retrieving articles of concept " + conceptId + " in language " + language );
				pstArticles.setInt( 1, conceptId );
				pstArticles.setString( 2, language.toString() );
				ResultSet articleResultSet = pstArticles.executeQuery();
				while( articleResultSet.next() ) {
					int pageId = articleResultSet.getInt( 1 );
					articleCount++;
					
					Page p = new Page( pageId );
					wp.initializePage( p );
					if( !p.isInitialized() ) {
						logger.warn( "Page " + pageId + "@" + language + " was not found." );
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
		
		doc.setText( language.toString(), language, content.toString() );
		logger.info( "Build document for category " + categoryId + " (Linked concepts: " + linkCount + ", articles: " + articleCount + ")" );
		return doc;
	}

	@Override
	public ICollectionIterator iterator() {
		return new WikipediaMLCategoryCollectionIterator();
	}

	@Override
	public int size() {
		return categoryIds.size();
	}

	public static String buildConceptName( int conceptId ) { 
		StringBuilder docNameBuilder = new StringBuilder();
		Formatter docIdFormatter = new Formatter( docNameBuilder );

		docIdFormatter.format( "%1$06d", conceptId );
		return docNameBuilder.toString();
	}
	
	public static int buildConceptId( String docName ) {
		return Integer.parseInt( docName );
	}
	
	class WikipediaMLCategoryCollectionIterator implements ICollectionIterator {

		private int m_index = -1;
		private IDocument currentDoc;
		
		public WikipediaMLCategoryCollectionIterator() {
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
			
			if( m_index < categoryIds.size() ) {
				currentDoc = buildDocument( categoryIds.get( m_index ) );
				return true;
			}
			else {
				currentDoc = null;
				return false;
			}
		}
		
	}
}
