package test.wp;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.uka.aifb.api.document.ICollection;
import edu.uka.aifb.api.document.ICollectionIterator;
import edu.uka.aifb.api.document.IDocument;
import edu.uka.aifb.api.nlp.ITokenAnalyzer;
import edu.uka.aifb.document.wikipedia.WikipediaMLCategoryCollection;
import edu.uka.aifb.ir.terrier.TerrierIndexFactory;
import edu.uka.aifb.nlp.Language;
import edu.uka.aifb.nlp.MultiLingualAnalyzer;
import edu.uka.aifb.tools.ConfigurationManager;
import edu.uka.aifb.tools.JdbcFactory;
import edu.uka.aifb.tools.JdbcStatementBuffer;

public class TestWikipediaCategoryGraph {

	static final String[] REQUIRED_PROPERTIES = {
		"language",
		"connection_id",
		"wikipedia_id"
	};
	
	static final String INDEX_ID_PREFIX = "test_wp_ml";
	static String index_id;
	
	static Logger logger = Logger.getLogger( TestWikipediaCategoryGraph.class );

	static TerrierIndexFactory factory = new TerrierIndexFactory();

	static public void main( String[] args ) throws Exception {
		Configuration config = ConfigurationManager.parseArgs( args );
		ConfigurationManager.checkProperties( config, REQUIRED_PROPERTIES );

		Language language = Language.getLanguage( config.getString( "language" ) );
		index_id = INDEX_ID_PREFIX + "_" + language;
		
		JdbcStatementBuffer jsb = new JdbcStatementBuffer(
				JdbcFactory.getConnection( config, config.getString( "connection_id" ) ) );
		
		logger.info( "Initializing wikipedia collection, language: " + language );
		ICollection collection = new WikipediaMLCategoryCollection(
						config,
						jsb,
						language );
		
		ITokenAnalyzer analyzer = new MultiLingualAnalyzer( config );
		
		int count = 0;
		ICollectionIterator it = collection.iterator();
		while( it.next() ) {
			IDocument doc = it.getDocument();
			count++;
			if( count > 10 ) {
				break;
			}

			
			System.out.println( "Category mlc_id=" + doc.getName() );
			//System.out.println( doc.getText() );
			//System.out.println( "\n---\n" );
		}
	}	

	
}
