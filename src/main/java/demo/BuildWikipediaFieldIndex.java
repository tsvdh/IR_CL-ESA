package demo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import edu.kit.aifb.ConfigurationException;
import edu.kit.aifb.ConfigurationManager;
import edu.kit.aifb.document.ICollection;
import edu.kit.aifb.nlp.Language;
import edu.kit.aifb.terrier.MTTerrierIndexFactory;

public class BuildWikipediaFieldIndex {

	static Log logger = LogFactory.getLog( BuildWikipediaFieldIndex.class );

	static final String[] REQUIRED_PROPERTIES = {
		"collection_bean",
		"fields",
		"indexId",
		"language"
	};
	
	static public void main( String[] args ) throws Exception {
		try {
			ApplicationContext context = new FileSystemXmlApplicationContext( "*_context.xml" );
			ConfigurationManager confMan = (ConfigurationManager) context.getBean( ConfigurationManager.class );
			confMan.parseArgs( args );
			confMan.checkProperties( REQUIRED_PROPERTIES );
			Configuration config = confMan.getConfig();

			String indexId = config.getString( "indexId" );
			Language language = Language.getLanguage( config.getString( "language" ) );
						
			List<String> fields = new ArrayList<String>();
			for( String field : config.getStringArray( "fields" ) ) {
				fields.add( field );
			}
			
			ICollection collection = (ICollection) context.getBean(
					config.getString( "collection_bean" ) );

			MTTerrierIndexFactory factory = (MTTerrierIndexFactory) context.getBean(
					MTTerrierIndexFactory.class );
			
			factory.buildFieldIndexes( indexId, language, collection, fields );
		}
		catch( ConfigurationException e ) {
			e.printUsage();
		}
	}

}
