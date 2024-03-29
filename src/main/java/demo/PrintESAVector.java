package demo;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import edu.kit.aifb.ConfigurationException;
import edu.kit.aifb.ConfigurationManager;
import edu.kit.aifb.concept.IConceptDescription;
import edu.kit.aifb.concept.IConceptExtractor;
import edu.kit.aifb.concept.IConceptIndex;
import edu.kit.aifb.concept.IConceptIterator;
import edu.kit.aifb.concept.IConceptVector;
import edu.kit.aifb.document.TextDocument;


public class PrintESAVector {

	static final String[] REQUIRED_PROPERTIES = {
		"text",
		//"concept_index_bean",
		//"concept_description_bean",
	};

	static Log logger = LogFactory.getLog( PrintESAVector.class );

	/**
	 * @param args
	 */
	public static void main( String[] args ) throws Exception {
		try {
			ApplicationContext context = new FileSystemXmlApplicationContext( "*_context.xml" );
			ConfigurationManager confMan = (ConfigurationManager) context.getBean( ConfigurationManager.class );
			confMan.parseArgs( args );
			confMan.checkProperties( REQUIRED_PROPERTIES );
			Configuration config = confMan.getConfig();
		
			String conceptIndexBean = "default_concept_index";
			if( config.containsKey( "concept_index_bean" ) ) {
				conceptIndexBean = config.getString( "concept_index_bean" );
			}
			
			IConceptIndex index = (IConceptIndex) context.getBean( conceptIndexBean );
			logger.info( "size of source index: " + index.size() );
			IConceptExtractor esaExtractor = index.getConceptExtractor();

			String conceptDescriptionBean = "default_concept_description";
			if( config.containsKey( "concept_description_bean" ) ) {
				conceptDescriptionBean = config.getString( "concept_description_bean" );
			}
			IConceptDescription description = (IConceptDescription) context.getBean( conceptDescriptionBean );
			
			TextDocument doc = new TextDocument( "text" );
			doc.setText( "content", index.getLanguage(), config.getString( "text" ) );

			logger.info( "Computing ESA vector of: " + doc.getText( "content" ) );
			IConceptVector cv = esaExtractor.extract( doc );

			logger.info( "Printing concept vector" );
			IConceptIterator it = cv.orderedIterator();
			while( it.next() ) {
				String d = description.getDescription(
						index.getConceptName( it.getId() ),
						index.getLanguage() );
				
				System.out.println( it.getValue() + "\t"
						+ d + " (" + index.getConceptName( it.getId() ) + ")" );
			}				
		}
		catch( ConfigurationException e ) {
			e.printUsage();
		}
	}
}
