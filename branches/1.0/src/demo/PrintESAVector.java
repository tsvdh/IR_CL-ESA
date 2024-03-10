package demo;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import edu.uka.aifb.api.concept.IConceptExtractor;
import edu.uka.aifb.api.concept.IConceptIndex;
import edu.uka.aifb.api.concept.IConceptIterator;
import edu.uka.aifb.api.concept.IConceptVector;
import edu.uka.aifb.api.wikipedia.IPage;
import edu.uka.aifb.api.wikipedia.IWikipediaDatabase;
import edu.uka.aifb.document.TextDocument;
import edu.uka.aifb.nlp.Language;
import edu.uka.aifb.nlp.MultiLingualAnalyzer;
import edu.uka.aifb.terrier.TerrierESAIndex;
import edu.uka.aifb.tools.ConfigurationManager;
import edu.uka.aifb.wikipedia.Page;
import edu.uka.aifb.wikipedia.sql.SQLWikipediaDatabase;


public class PrintESAVector {

	static final String[] REQUIRED_PROPERTIES = {
		"language",
		"text",
	};

	static Logger logger = Logger.getLogger( PrintESAVector.class );

	/**
	 * @param args
	 * @throws ConfigurationException 
	 */
	public static void main( String[] args ) throws Exception {
		Configuration config = ConfigurationManager.parseArgs( args );
		ConfigurationManager.checkProperties( config, REQUIRED_PROPERTIES );
		
		Language language = Language.getLanguage( config.getString( "language" ) );
		IConceptIndex sourceIndex = new TerrierESAIndex( config, "wikipedia", language );
		logger.info( "size of source index: " + sourceIndex.size() );

		IWikipediaDatabase wp = new SQLWikipediaDatabase( config, language );

		IConceptExtractor esaExtractor = sourceIndex.getConceptExtractor();
		esaExtractor.setTokenAnalyzer( new MultiLingualAnalyzer( config ) );
		
		TextDocument doc = new TextDocument( "text" );
		doc.setText( "content", language, config.getString( "text" ) );
		
		logger.info( "Computing ESA vector of: " + doc.getText( "content" ) );
		IConceptVector cv = esaExtractor.extract( doc );
		
		logger.info( "Printing concept vector" );
		IConceptIterator it = cv.orderedIterator();
		while( it.next() ) {
			int articleId = Integer.parseInt( sourceIndex.getConceptName( it.getId() ) );
			IPage article = new Page( articleId );
			wp.initializePage( article );
			if( article.isInitialized() )
				System.out.println( it.getValue() + " " + article.getTitle() + " (" + articleId + ")" );
		}
	}
}
