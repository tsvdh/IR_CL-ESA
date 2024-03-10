package demo;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.uka.aifb.api.document.ICollection;
import edu.uka.aifb.api.nlp.ITokenAnalyzer;
import edu.uka.aifb.document.wikipedia.WikipediaCollection;
import edu.uka.aifb.ir.terrier.TerrierIndexFactory;
import edu.uka.aifb.nlp.Language;
import edu.uka.aifb.nlp.MultiLingualAnalyzer;
import edu.uka.aifb.tools.ConfigurationManager;

public class BuildWikipediaIndex {

	static final String[] REQUIRED_PROPERTIES = {
		"language",
		"single_pass"
	};
	
	static Logger logger = Logger.getLogger( BuildWikipediaIndex.class );
	
	static public void main( String[] args ) throws Exception {
		Configuration config = ConfigurationManager.parseArgs( args );
		ConfigurationManager.checkProperties( config, REQUIRED_PROPERTIES );

		Language language = Language.getLanguage( config.getString( "language" ) );
		
		logger.info( "Initializing wikipedia collection, language: " + language );
		ICollection collection = new WikipediaCollection(
						config,
						language );
		
		ITokenAnalyzer analyzer = new MultiLingualAnalyzer( config );
		
		if( config.getBoolean( "single_pass" ) ) {
			TerrierIndexFactory.buildIndexSinglePass(
					"wikipedia",
					language,
					analyzer,
					collection,
					false );
		}
		else {
			TerrierIndexFactory.buildIndex(
					"wikipedia",
					language,
					analyzer,
					collection,
					false );
		}
	}

}
