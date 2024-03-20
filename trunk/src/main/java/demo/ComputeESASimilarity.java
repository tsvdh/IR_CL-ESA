package demo;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import edu.kit.aifb.ConfigurationManager;
import edu.kit.aifb.concept.ConceptVectorSimilarity;
import edu.kit.aifb.concept.IConceptExtractor;
import edu.kit.aifb.concept.IConceptIndex;
import edu.kit.aifb.concept.IConceptVector;
import edu.kit.aifb.concept.scorer.CosineScorer;
import edu.kit.aifb.document.TextDocument;
import edu.kit.aifb.nlp.Language;


public class ComputeESASimilarity {

	static final String[] REQUIRED_PROPERTIES = {
		//"concept_index_bean",
		"language",
		"text_a",
		"text_b"
	};

	static Log logger = LogFactory.getLog( ComputeESASimilarity.class );

	/**
	 * @param args
	 * @throws ConfigurationException 
	 */
	public static void main( String[] args ) throws Exception {
		ApplicationContext context = new FileSystemXmlApplicationContext( "config/demo_context.xml" );
		ConfigurationManager confMan = (ConfigurationManager) context.getBean( ConfigurationManager.class );
		confMan.parseArgs( args );
		confMan.checkProperties( REQUIRED_PROPERTIES );
		Configuration config = confMan.getConfig();

		Language language = Language.getLanguage( config.getString( "language" ) );

		String conceptIndexBean = "default_concept_index";
		if( config.containsKey( "concept_index_bean" ) ) {
			conceptIndexBean = config.getString( "concept_index_bean" );
		}
		
		IConceptIndex index = (IConceptIndex) context.getBean( conceptIndexBean );
		logger.info( "size of source index: " + index.size() );

		IConceptExtractor esaExtractor = index.getConceptExtractor();
		
		TextDocument docA = new TextDocument( "text_a" );
		docA.setText( "content", language, config.getString( "text_a" ) );
		
		TextDocument docB = new TextDocument( "text_b" );
		docB.setText( "content", language, config.getString( "text_b" ) );

		logger.info( "Computing ESA vector of: " + docA.getText( "content" ) );
		IConceptVector cvA = esaExtractor.extract( docA );
		
		logger.info( "Computing ESA vector of: " + docB.getText( "content" ) );
		IConceptVector cvB = esaExtractor.extract( docB );

		ConceptVectorSimilarity sim = new ConceptVectorSimilarity( new CosineScorer() );
		logger.info( "Computing similarity" );
		
		double value = sim.calcSimilarity( cvA, cvB );
		System.out.println( "ESA Similarity: " + value );
	}

}
