package demo;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import edu.uka.aifb.api.concept.IConceptExtractor;
import edu.uka.aifb.api.concept.IConceptIndex;
import edu.uka.aifb.api.concept.IConceptVector;
import edu.uka.aifb.concept.ConceptVectorSimilarity;
import edu.uka.aifb.concept.scorer.CosineScorer;
import edu.uka.aifb.document.TextDocument;
import edu.uka.aifb.nlp.Language;
import edu.uka.aifb.nlp.MultiLingualAnalyzer;
import edu.uka.aifb.terrier.TerrierESAIndex;
import edu.uka.aifb.tools.ConfigurationManager;


public class ComputeESASimilarity {

	static final String[] REQUIRED_PROPERTIES = {
		"language",
		"text_a",
		"text_b"
	};

	static Logger logger = Logger.getLogger( ComputeESASimilarity.class );

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
		
		IConceptExtractor esaExtractor = sourceIndex.getConceptExtractor();
		esaExtractor.setTokenAnalyzer( new MultiLingualAnalyzer( config ) );
		
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
