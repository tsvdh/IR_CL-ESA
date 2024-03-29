package edu.kit.aifb.wikipedia.wpm;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.wikipedia.miner.model.Article;
import org.wikipedia.miner.model.Article.AnchorText;
import org.wikipedia.miner.model.Redirect;
import org.wikipedia.miner.model.Wikipedia;
import org.wikipedia.miner.util.MarkupStripper;
import org.wikipedia.miner.util.SortedVector;

import edu.kit.aifb.document.ICollection;
import edu.kit.aifb.document.ICollectionIterator;
import edu.kit.aifb.document.IDocument;
import edu.kit.aifb.document.TextDocument;
import edu.kit.aifb.nlp.Language;
import edu.kit.aifb.wikipedia.mlc.MLCDatabase;
import gnu.trove.TIntArrayList;

public class WpmMLCArticleCollection implements ICollection {

	private static Log logger = LogFactory.getLog( WpmMLCArticleCollection.class );
	
	Wikipedia wp;
	MLCDatabase mlcArticleDb;
	boolean useAnchorText;
	boolean useRedirects;
	Language language;
	
	public WpmMLCArticleCollection() {
		useAnchorText = true;
		useRedirects = true;
	}
	
	public void setUseAnchorText( boolean useAnchorText ) {
		this.useAnchorText = useAnchorText;
	}
	
	public void setUseRedirects( boolean useRedirects ) {
		this.useRedirects = useRedirects;
	}

	@Required
	public void setLanguage( Language language ) {
		this.language = language;
	}
	
	@Required
	public void setWikipedia( Wikipedia wp ) {
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
		StringBuilder titleBuilder = new StringBuilder();
		StringBuilder redirectBuilder = new StringBuilder();
		StringBuilder contentBuilder = new StringBuilder();
		StringBuilder anchorBuilder = new StringBuilder();
		
		try {
			TIntArrayList articleIds = mlcArticleDb.getPageIds( conceptId, language );
			if( logger.isDebugEnabled() )
				logger.debug( "Building document for concept " + conceptId + " (Linked articles: " + articleIds.size() + ")" );

			
			for( int i=0; i<articleIds.size(); i++ ) {
				int articleId = articleIds.get(i);

				try {
					Article p = (Article)wp.getPageById( articleId );
					if( p == null ) {
						throw new Exception( "Article could not be initialized.");
					}
				
					String title = p.getTitleWithoutScope();
					logger.debug( "Building document for article " + title + " (" + articleId + ")" );
					titleBuilder.append( title ).append( "\n" );

					if( useRedirects ) {
						redirectBuilder.append( title ).append( "\n" );

						SortedVector<Redirect> redirects = p.getRedirects();
						for( Redirect redirect : redirects ) {
							redirectBuilder.append( redirect.getTitle() ).append( "\n" );
						}
					}

					String content = p.getContent();
					if( logger.isTraceEnabled() ) {
						System.out.println( content );
					}

					// remove wiki markup
					if( content != null ) {
						contentBuilder.append( MarkupStripper.stripEverything( content ) ).append( "\n" );
					}

					// add anchor text
					if( useAnchorText ) {
						SortedVector<AnchorText> anchorTexts = ((Article)p).getAnchorTexts();
						for( AnchorText anchorText : anchorTexts ) {
							for( int j=0; j<anchorText.getCount(); j++ ) {
								anchorBuilder.append( anchorText.getText() ).append( "\n" );
							}
						}
					}
				}
				catch( Exception e ) {
					logger.warn( "Error when processing article " + articleId + " for concept " + conceptId + ": " + e.getMessage() );
				}
			}
		}
		catch( SQLException e ) {
			logger.error( "Error while retrieving article ids for concept " + conceptId + ": " + e.getMessage() );
		}	
		
		doc.setText( "title", language, titleBuilder.toString() );
		doc.setText( "redirect", language, redirectBuilder.toString() );
		doc.setText( "content", language, contentBuilder.toString() );
		doc.setText( "anchor", language, anchorBuilder.toString() );
		return doc;
	}

	public ICollectionIterator iterator() {
		return new CollectionIterator( mlcArticleDb.getConceptIds() );
	}

	public int size() {
		return mlcArticleDb.size();
	}

	protected class CollectionIterator implements ICollectionIterator {

		private int m_index = -1;
		private TIntArrayList conceptIds;
		private IDocument currentDoc;
		
		public CollectionIterator( TIntArrayList conceptIds ) {
			m_index = -1;
			this.conceptIds = conceptIds; 
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
